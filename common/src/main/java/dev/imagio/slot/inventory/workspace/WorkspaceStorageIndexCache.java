package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Session-scoped cache for projection-only storage summaries.
 *
 * <p>Mutation paths still route through live {@link WorldStorageAccess}. This
 * cache only reuses read-model pieces used by workspace projection.
 */
public final class WorkspaceStorageIndexCache {
    private static final long LIVE_BUCKET_TICKS = 10L;
    private static final long TRACKED_POLL_BUCKET_TICKS = 20L;
    private static final int TRACKED_POLL_BUDGET = 2;

    private Layer<RememberedKey> rememberedLayer = Layer.empty();
    private DisplayLayer displayLayer = DisplayLayer.empty();
    private Layer<LiveSnapshotKey> liveSnapshotLayer = Layer.empty();
    private Layer<DepositOverlayKey> depositOverlayLayer = Layer.empty();
    private IndexKey lastIndexKey;
    private WorkspaceStorageIndex lastIndex;
    private long lastTrackedPollBucket = -1L;
    private int trackedPollCursor;
    private long hits;
    private long misses;
    private Diagnostics diagnostics = Diagnostics.empty();

    public WorkspaceStorageIndex build(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            long tick
    ) {
        WorkspaceStorageMemoryStore memory = WorkspaceStorageMemoryStore.forServer(server);
        ClaimedChestMap claimedChestMap = workflow == null ? ClaimedChestMap.empty() : workflow.claimedChestMap();
        Set<String> proximate = proximateStorageIds == null ? Set.of() : Set.copyOf(proximateStorageIds);
        PollDiagnostics poll = pollTrackedStorageChanges(
                memory,
                server,
                worldStorage,
                claimedChestMap,
                proximate,
                tick);
        Map<String, RememberedStorageContents> remembered = memory == null
                ? Map.of()
                : memory.rememberedContents();
        long revision = memory == null ? 0L : memory.revision();
        return buildInternal(
                server,
                authority,
                claimedChestMap,
                worldStorage,
                proximate,
                displaySources,
                memory,
                remembered,
                revision,
                tick,
                poll);
    }

    WorkspaceStorageIndex buildForTesting(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            Map<String, RememberedStorageContents> remembered,
            long memoryRevision,
            long tick
    ) {
        return buildInternal(
                server,
                authority,
                claimedChestMap,
                worldStorage,
                proximateStorageIds,
                displaySources,
                null,
                remembered,
                memoryRevision,
                tick,
                PollDiagnostics.empty());
    }

    WorkspaceStorageIndex buildWithMemoryForTesting(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            WorkspaceStorageMemoryStore memory,
            long tick
    ) {
        Set<String> proximate = proximateStorageIds == null ? Set.of() : Set.copyOf(proximateStorageIds);
        ClaimedChestMap resolvedMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
        PollDiagnostics poll = pollTrackedStorageChanges(
                memory,
                server,
                worldStorage,
                resolvedMap,
                proximate,
                tick);
        Map<String, RememberedStorageContents> remembered = memory == null
                ? Map.of()
                : memory.rememberedContents();
        long revision = memory == null ? 0L : memory.revision();
        return buildInternal(
                server,
                authority,
                resolvedMap,
                worldStorage,
                proximate,
                displaySources,
                memory,
                remembered,
                revision,
                tick,
                poll);
    }

    public Diagnostics diagnostics() {
        return diagnostics;
    }

    public void clear() {
        rememberedLayer = Layer.empty();
        displayLayer = DisplayLayer.empty();
        liveSnapshotLayer = Layer.empty();
        depositOverlayLayer = Layer.empty();
        lastIndexKey = null;
        lastIndex = null;
        lastTrackedPollBucket = -1L;
        trackedPollCursor = 0;
        hits = 0L;
        misses = 0L;
        diagnostics = Diagnostics.empty();
    }

    private WorkspaceStorageIndex buildInternal(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            WorkspaceStorageMemoryStore memory,
            Map<String, RememberedStorageContents> remembered,
            long memoryRevision,
            long tick,
            PollDiagnostics pollDiagnostics
    ) {
        ClaimedChestMap resolvedMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
        InventoryAuthoritySnapshot resolvedAuthority = authority == null
                ? InventoryAuthoritySnapshot.empty()
                : authority;
        Set<String> proximate = proximateStorageIds == null ? Set.of() : Set.copyOf(proximateStorageIds);
        List<WorldDisplayStorageSource> resolvedDisplays = displaySources == null
                ? List.of()
                : List.copyOf(displaySources);
        Map<String, RememberedStorageContents> rememberedById = remembered == null ? Map.of() : remembered;

        RememberedKey rememberedKey = new RememberedKey(
                claimedTopologyKey(resolvedMap, false, proximate),
                rememberedDisplayMemoryKey(rememberedById),
                memoryRevision);
        boolean rememberedHit = rememberedLayer.matches(rememberedKey);
        if (!rememberedHit) {
            rememberedLayer = new Layer<>(
                    rememberedKey,
                    buildRememberedEntries(resolvedMap, proximate, rememberedById));
        }

        DisplayKey displayKey = displayKey(resolvedDisplays);
        boolean displayHit = displayLayer.matches(displayKey);
        if (!displayHit) {
            displayLayer = new DisplayLayer(
                    displayKey,
                    buildDisplayEntries(resolvedDisplays, memory, tick),
                    resolvedDisplays);
        }

        LiveSnapshotKey liveSnapshotKey = new LiveSnapshotKey(
                claimedTopologyKey(resolvedMap, true, proximate),
                proximate,
                Math.max(0L, tick) / LIVE_BUCKET_TICKS);
        boolean liveSnapshotHit = liveSnapshotLayer.matches(liveSnapshotKey);
        if (!liveSnapshotHit) {
            liveSnapshotLayer = new Layer<>(
                    liveSnapshotKey,
                    buildLiveSnapshotEntries(server, worldStorage, resolvedMap, proximate, memory, tick));
            depositOverlayLayer = Layer.empty();
        }

        DepositOverlayKey depositOverlayKey = new DepositOverlayKey(
                liveSnapshotKey,
                carriedKey(resolvedAuthority));
        boolean depositOverlayHit = depositOverlayLayer.matches(depositOverlayKey);
        if (!depositOverlayHit) {
            depositOverlayLayer = new Layer<>(
                    depositOverlayKey,
                    buildDepositOverlay(server, worldStorage, resolvedAuthority, resolvedMap, liveSnapshotLayer.entries()));
        }

        IndexKey indexKey = new IndexKey(rememberedKey, displayKey, depositOverlayKey);
        boolean indexHit = lastIndex != null && indexKey.equals(lastIndexKey);
        if (indexHit) {
            hits++;
            diagnostics = new Diagnostics(
                    true,
                    rememberedHit,
                    displayHit,
                    liveSnapshotHit,
                    depositOverlayHit,
                    hits,
                    misses,
                    pollDiagnostics);
            return lastIndex;
        }

        misses++;
        LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> combined = new LinkedHashMap<>();
        combined.putAll(rememberedLayer.entries());
        combined.putAll(depositOverlayLayer.entries());
        combined.putAll(displayLayer.entries());
        WorkspaceStorageIndex.AliasCorrection aliasCorrection = WorkspaceStorageIndex.correctDisplayAliases(
                combined,
                resolvedMap,
                displayLayer.displaySources());
        lastIndex = new WorkspaceStorageIndex(
                aliasCorrection.entries(),
                WorkspaceStorageIndex.carriedCounts(resolvedAuthority),
                aliasCorrection.displaySources(),
                memoryRevision);
        lastIndexKey = indexKey;
        diagnostics = new Diagnostics(
                false,
                rememberedHit,
                displayHit,
                liveSnapshotHit,
                depositOverlayHit,
                hits,
                misses,
                pollDiagnostics);
        return lastIndex;
    }

    private PollDiagnostics pollTrackedStorageChanges(
            WorkspaceStorageMemoryStore memory,
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            ClaimedChestMap claimedChestMap,
            Set<String> proximate,
            long tick
    ) {
        if (memory == null || worldStorage == null || claimedChestMap == null || claimedChestMap.chests().isEmpty()) {
            return PollDiagnostics.empty();
        }
        ArrayList<ClaimedChest> candidates = new ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null || !chest.role().visibleToWorkspace()) {
                continue;
            }
            String storageId = chest.storageId().toString();
            if (proximate != null && proximate.contains(storageId)) {
                continue;
            }
            candidates.add(chest);
        }
        if (candidates.isEmpty()) {
            trackedPollCursor = 0;
            return PollDiagnostics.empty();
        }
        long bucket = Math.max(0L, tick) / TRACKED_POLL_BUCKET_TICKS;
        if (bucket == lastTrackedPollBucket) {
            return new PollDiagnostics(candidates.size(), 0, 0, 0);
        }
        lastTrackedPollBucket = bucket;
        int budget = Math.min(TRACKED_POLL_BUDGET, candidates.size());
        int start = Math.floorMod(trackedPollCursor, candidates.size());
        int checked = 0;
        int changed = 0;
        int failed = 0;
        for (int offset = 0; offset < budget; offset++) {
            ClaimedChest chest = candidates.get((start + offset) % candidates.size());
            if (chest == null) {
                continue;
            }
            checked++;
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            try {
                if (!worldStorage.isAccessible(server, target)) {
                    continue;
                }
                StorageTargetRef ref = StorageTargetRef.claimed(chest, true, false, false);
                if (WorkspaceStorageMemoryStore.observeLiveTarget(
                        memory,
                        server,
                        worldStorage,
                        ref,
                        target,
                        tick,
                        "workspace_index_tracked_poll",
                        false)) {
                    changed++;
                }
            } catch (RuntimeException exception) {
                failed++;
                SlotCommon.LOGGER.warn(
                        "[SLOT] tracked storage poll failed for {}: {}",
                        chest.storageId(),
                        WorkspaceStorageIndex.safeMessage(exception));
            }
        }
        trackedPollCursor = (start + budget) % candidates.size();
        return new PollDiagnostics(candidates.size(), checked, changed, failed);
    }

    private static Map<String, WorkspaceStorageIndex.StorageEntry> buildRememberedEntries(
            ClaimedChestMap claimedChestMap,
            Set<String> proximate,
            Map<String, RememberedStorageContents> rememberedById
    ) {
        LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> entries = new LinkedHashMap<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null || !chest.role().visibleToWorkspace()) {
                continue;
            }
            String storageId = chest.storageId().toString();
            if (proximate.contains(storageId)) {
                continue;
            }
            RememberedStorageContents remembered = rememberedById.get(storageId);
            if (remembered != null) {
                entries.put(storageId, WorkspaceStorageIndex.rememberedEntry(remembered, false));
            } else {
                entries.put(storageId, new WorkspaceStorageIndex.StorageEntry(
                        StorageTargetRef.claimed(chest, false, false, false),
                        SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                        Map.of(),
                        false,
                        false));
            }
        }
        for (RememberedStorageContents remembered : rememberedById.values()) {
            if (remembered == null
                    || entries.containsKey(remembered.storageId())
                    || !WorkspaceStorageIndex.isTrackedDisplayMemory(remembered)) {
                continue;
            }
            entries.put(remembered.storageId(), WorkspaceStorageIndex.rememberedEntry(remembered, false));
        }
        return entries.isEmpty() ? Map.of() : Map.copyOf(entries);
    }

    private static Map<String, WorkspaceStorageIndex.StorageEntry> buildDisplayEntries(
            List<WorldDisplayStorageSource> displaySources,
            WorkspaceStorageMemoryStore memory,
            long tick
    ) {
        if (displaySources == null || displaySources.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> entries = new LinkedHashMap<>();
        for (WorldDisplayStorageSource source : displaySources) {
            if (source == null || source.storageId().isBlank()) {
                continue;
            }
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot =
                    WorkspaceStorageIndex.snapshotFromDisplay(source);
            StorageTargetRef ref = StorageTargetRef.display(source, false, true);
            entries.put(source.storageId(), new WorkspaceStorageIndex.StorageEntry(
                    ref,
                    snapshot,
                    WorkspaceStorageIndex.countsFromSnapshot(snapshot),
                    true,
                    false));
            if (memory != null && source.kind().trackedStorage()) {
                memory.observeSnapshot(ref, snapshot, tick, "workspace_index_display_live_read", false);
            }
        }
        return entries.isEmpty() ? Map.of() : Map.copyOf(entries);
    }

    private static Map<String, WorkspaceStorageIndex.StorageEntry> buildLiveSnapshotEntries(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            ClaimedChestMap claimedChestMap,
            Set<String> proximate,
            WorkspaceStorageMemoryStore memory,
            long tick
    ) {
        if (claimedChestMap == null || claimedChestMap.chests().isEmpty() || proximate == null || proximate.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> entries = new LinkedHashMap<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (chest == null || !chest.role().visibleToWorkspace()) {
                continue;
            }
            String storageId = chest.storageId().toString();
            if (!proximate.contains(storageId)) {
                continue;
            }
            WorkspaceStorageIndex.StorageEntry live = WorkspaceStorageIndex.liveClaimedBaseEntry(
                    server,
                    worldStorage,
                    chest,
                    true,
                    memory,
                    tick);
            if (live == null) {
                if (memory != null) {
                    memory.forget(storageId);
                }
                continue;
            }
            entries.put(storageId, live);
        }
        return entries.isEmpty() ? Map.of() : Map.copyOf(entries);
    }

    private static Map<String, WorkspaceStorageIndex.StorageEntry> buildDepositOverlay(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            Map<String, WorkspaceStorageIndex.StorageEntry> liveEntries
    ) {
        if (liveEntries == null || liveEntries.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> entries = new LinkedHashMap<>();
        for (Map.Entry<String, WorkspaceStorageIndex.StorageEntry> entry : liveEntries.entrySet()) {
            ClaimedChest chest = claimedChest(claimedChestMap, entry.getKey());
            WorkspaceStorageIndex.StorageEntry overlay = WorkspaceStorageIndex.applyDepositOverlay(
                    server,
                    authority,
                    worldStorage,
                    chest,
                    entry.getValue());
            if (overlay != null) {
                entries.put(entry.getKey(), overlay);
            }
        }
        return entries.isEmpty() ? Map.of() : Map.copyOf(entries);
    }

    private static ClaimedChest claimedChest(ClaimedChestMap map, String storageId) {
        if (map == null || storageId == null || storageId.isBlank()) {
            return null;
        }
        try {
            return map.chest(UUID.fromString(storageId));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static List<String> claimedTopologyKey(
            ClaimedChestMap map,
            boolean proximateOnly,
            Set<String> proximate
    ) {
        if (map == null || map.chests().isEmpty()) {
            return List.of();
        }
        ArrayList<String> keys = new ArrayList<>();
        for (ClaimedChest chest : map.chests()) {
            if (chest == null || !chest.role().visibleToWorkspace()) {
                continue;
            }
            String storageId = chest.storageId().toString();
            boolean isProximate = proximate != null && proximate.contains(storageId);
            if (proximateOnly != isProximate) {
                continue;
            }
            keys.add(chestKey(chest));
        }
        keys.sort(String::compareTo);
        return keys.isEmpty() ? List.of() : List.copyOf(keys);
    }

    private static String chestKey(ClaimedChest chest) {
        StringBuilder out = new StringBuilder();
        out.append(chest.storageId())
                .append('|')
                .append(chest.role().name())
                .append('|')
                .append(chest.label());
        ArrayList<String> anchors = new ArrayList<>();
        for (ChestAnchor anchor : chest.anchors()) {
            if (anchor != null) {
                anchors.add(anchor.dimensionId() + '@' + anchor.x() + ',' + anchor.y() + ',' + anchor.z());
            }
        }
        anchors.sort(String::compareTo);
        out.append('|').append(anchors);
        return out.toString();
    }

    private static List<String> rememberedDisplayMemoryKey(Map<String, RememberedStorageContents> rememberedById) {
        if (rememberedById == null || rememberedById.isEmpty()) {
            return List.of();
        }
        ArrayList<String> keys = new ArrayList<>();
        for (RememberedStorageContents remembered : rememberedById.values()) {
            if (WorkspaceStorageIndex.isTrackedDisplayMemory(remembered)) {
                keys.add(remembered.storageId());
            }
        }
        keys.sort(String::compareTo);
        return keys.isEmpty() ? List.of() : List.copyOf(keys);
    }

    private static DisplayKey displayKey(List<WorldDisplayStorageSource> displaySources) {
        if (displaySources == null || displaySources.isEmpty()) {
            return new DisplayKey(List.of());
        }
        ArrayList<String> keys = new ArrayList<>();
        for (WorldDisplayStorageSource source : displaySources) {
            if (source == null) {
                continue;
            }
            StringBuilder out = new StringBuilder();
            out.append(source.storageId())
                    .append('|')
                    .append(source.kind())
                    .append('|')
                    .append(source.label())
                    .append('|')
                    .append(source.dimensionId())
                    .append('@')
                    .append(source.x()).append(',').append(source.y()).append(',').append(source.z())
                    .append('|')
                    .append(source.slotCount());
            ArrayList<String> contents = new ArrayList<>();
            for (WorldStorageAccess.SlotContent content : source.contents()) {
                if (content == null || content.stack() == null || content.stack().isEmpty()) {
                    continue;
                }
                contents.add(content.slotIndex() + ":" + stackKey(content.stack()) + ':' + content.count());
            }
            contents.sort(String::compareTo);
            out.append('|').append(contents);
            ArrayList<String> aliases = new ArrayList<>();
            for (WorldDisplayStorageSource.AliasedBlock alias : source.aliasedBlocks()) {
                if (alias != null) {
                    aliases.add(alias.dimensionId() + '@' + alias.x() + ',' + alias.y() + ',' + alias.z());
                }
            }
            aliases.sort(String::compareTo);
            out.append('|').append(aliases);
            keys.add(out.toString());
        }
        keys.sort(String::compareTo);
        return new DisplayKey(keys.isEmpty() ? List.of() : List.copyOf(keys));
    }

    private static List<String> carriedKey(InventoryAuthoritySnapshot authority) {
        Map<ItemIdentity, Integer> counts = WorkspaceStorageIndex.carriedCounts(authority);
        if (counts.isEmpty()) {
            return List.of();
        }
        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<ItemIdentity, Integer> entry : counts.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                keys.add(entry.getKey().comparisonMode().name()
                        + ':'
                        + entry.getKey().itemId()
                        + ':'
                        + entry.getKey().componentFingerprint()
                        + '='
                        + entry.getValue());
            }
        }
        keys.sort(String::compareTo);
        return keys.isEmpty() ? List.of() : List.copyOf(keys);
    }

    private static String stackKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        return SlotStackAccess.current().itemId(stack)
                + '|'
                + SlotStackAccess.current().dataFingerprint(stack)
                + '|'
                + stack.getCount()
                + '|'
                + stack.getMaxStackSize();
    }

    private record Layer<K>(
            K key,
            Map<String, WorkspaceStorageIndex.StorageEntry> entries
    ) {
        private Layer {
            entries = entries == null || entries.isEmpty() ? Map.of() : Map.copyOf(entries);
        }

        static <K> Layer<K> empty() {
            return new Layer<>(null, Map.of());
        }

        boolean matches(K otherKey) {
            return key != null && key.equals(otherKey);
        }
    }

    private record DisplayLayer(
            DisplayKey key,
            Map<String, WorkspaceStorageIndex.StorageEntry> entries,
            List<WorldDisplayStorageSource> displaySources
    ) {
        private DisplayLayer {
            entries = entries == null || entries.isEmpty() ? Map.of() : Map.copyOf(entries);
            displaySources = displaySources == null ? List.of() : List.copyOf(displaySources);
        }

        static DisplayLayer empty() {
            return new DisplayLayer(null, Map.of(), List.of());
        }

        boolean matches(DisplayKey otherKey) {
            return key != null && key.equals(otherKey);
        }
    }

    private record RememberedKey(List<String> claimedTopology, List<String> trackedDisplayStorageIds, long revision) {
        private RememberedKey {
            claimedTopology = claimedTopology == null ? List.of() : List.copyOf(claimedTopology);
            trackedDisplayStorageIds = trackedDisplayStorageIds == null
                    ? List.of()
                    : List.copyOf(trackedDisplayStorageIds);
            revision = Math.max(0L, revision);
        }
    }

    private record DisplayKey(List<String> entries) {
        private DisplayKey {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }
    }

    private record LiveSnapshotKey(List<String> proximateTopology, Set<String> proximateStorageIds, long bucket) {
        private LiveSnapshotKey {
            proximateTopology = proximateTopology == null ? List.of() : List.copyOf(proximateTopology);
            proximateStorageIds = proximateStorageIds == null ? Set.of() : Set.copyOf(proximateStorageIds);
            bucket = Math.max(0L, bucket);
        }
    }

    private record DepositOverlayKey(LiveSnapshotKey liveSnapshotKey, List<String> carriedSummary) {
        private DepositOverlayKey {
            carriedSummary = carriedSummary == null ? List.of() : List.copyOf(carriedSummary);
        }
    }

    private record IndexKey(RememberedKey rememberedKey, DisplayKey displayKey, DepositOverlayKey depositOverlayKey) {
    }

    public record Diagnostics(
            boolean indexHit,
            boolean rememberedHit,
            boolean displayHit,
            boolean liveSnapshotHit,
            boolean depositOverlayHit,
            long hits,
            long misses,
            PollDiagnostics trackedStoragePoll
    ) {
        public Diagnostics {
            trackedStoragePoll = trackedStoragePoll == null ? PollDiagnostics.empty() : trackedStoragePoll;
        }

        public static Diagnostics empty() {
            return new Diagnostics(false, false, false, false, false, 0L, 0L, PollDiagnostics.empty());
        }
    }

    public record PollDiagnostics(
            int candidates,
            int checked,
            int changed,
            int failed
    ) {
        public PollDiagnostics {
            candidates = Math.max(0, candidates);
            checked = Math.max(0, checked);
            changed = Math.max(0, changed);
            failed = Math.max(0, failed);
        }

        public static PollDiagnostics empty() {
            return new PollDiagnostics(0, 0, 0, 0);
        }
    }
}
