package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Shared read model for storage lookups used by the workspace, goals,
 * search/x-ray, wayfinding, and planner previews.
 *
 * <p>Proximate entries are read live once per index build. Non-proximate
 * tracked storage uses the remembered read model so ordinary workspace
 * refreshes do not enumerate every loaded chest in the world. Mutation
 * planners must use {@link #liveChestContentPresence()} so remembered-only
 * data never authorizes a transfer. They must also use
 * {@link #liveStorageAffinityEligibility()} so tiny station inventories that
 * happen to hold a matching item do not become deposit homes.
 */
public final class WorkspaceStorageIndex {
    private final Map<String, StorageEntry> entriesByStorageId;
    private final Map<ItemIdentity, Integer> carriedCountsByIdentity;
    private final List<WorldDisplayStorageSource> displaySources;
    private final long memoryRevision;

    private WorkspaceStorageIndex(
            Map<String, StorageEntry> entriesByStorageId,
            Map<ItemIdentity, Integer> carriedCountsByIdentity,
            List<WorldDisplayStorageSource> displaySources,
            long memoryRevision
    ) {
        this.entriesByStorageId = entriesByStorageId == null ? Map.of() : Map.copyOf(entriesByStorageId);
        this.carriedCountsByIdentity = carriedCountsByIdentity == null
                ? Map.of()
                : Map.copyOf(carriedCountsByIdentity);
        this.displaySources = displaySources == null ? List.of() : List.copyOf(displaySources);
        this.memoryRevision = Math.max(0L, memoryRevision);
    }

    public static WorkspaceStorageIndex empty() {
        return new WorkspaceStorageIndex(Map.of(), Map.of(), List.of(), 0L);
    }

    public static WorkspaceStorageIndex build(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            long tick
    ) {
        WorkspaceStorageMemoryStore memory = WorkspaceStorageMemoryStore.forServer(server);
        Map<String, RememberedStorageContents> remembered = memory == null
                ? Map.of()
                : memory.rememberedContents();
        long revision = memory == null ? 0L : memory.revision();
        return buildInternal(
                server,
                authority,
                workflow == null ? ClaimedChestMap.empty() : workflow.claimedChestMap(),
                worldStorage,
                proximateStorageIds,
                displaySources,
                memory,
                remembered,
                tick,
                revision);
    }

    public static WorkspaceStorageIndex forTesting(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            Map<String, RememberedStorageContents> remembered
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
                0L,
                0L);
    }

    private static WorkspaceStorageIndex buildInternal(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            ClaimedChestMap claimedChestMap,
            WorldStorageAccess worldStorage,
            Set<String> proximateStorageIds,
            List<WorldDisplayStorageSource> displaySources,
            WorkspaceStorageMemoryStore memory,
            Map<String, RememberedStorageContents> remembered,
            long tick,
            long memoryRevision
    ) {
        ClaimedChestMap resolvedMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
        Set<String> proximate = proximateStorageIds == null ? Set.of() : proximateStorageIds;
        Map<String, RememberedStorageContents> rememberedById = remembered == null ? Map.of() : remembered;
        LinkedHashMap<String, StorageEntry> entries = new LinkedHashMap<>();

        for (ClaimedChest chest : resolvedMap.chests()) {
            if (chest == null) {
                continue;
            }
            String storageId = chest.storageId().toString();
            boolean proximateTarget = proximate.contains(storageId);
            if (proximateTarget) {
                StorageEntry live = liveClaimedEntry(server, authority, worldStorage, chest, true, memory, tick);
                if (live != null) {
                    entries.put(storageId, live);
                    continue;
                }
            }
            RememberedStorageContents rememberedContents = rememberedById.get(storageId);
            if (rememberedContents != null) {
                entries.put(storageId, rememberedEntry(rememberedContents, proximateTarget));
            } else {
                StorageTargetRef ref = StorageTargetRef.claimed(chest, false, false, proximateTarget);
                entries.put(storageId, new StorageEntry(
                        ref,
                        SlotWorkspaceViewModel.ChestContentsSnapshot.empty(),
                        Map.of(),
                        false,
                        false));
            }
        }

        List<WorldDisplayStorageSource> liveDisplays = displaySources == null ? List.of() : List.copyOf(displaySources);
        for (WorldDisplayStorageSource source : liveDisplays) {
            if (source == null || source.storageId().isBlank()) {
                continue;
            }
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = snapshotFromDisplay(source);
            StorageTargetRef ref = StorageTargetRef.display(source, false, true);
            entries.put(source.storageId(), new StorageEntry(
                    ref,
                    snapshot,
                    countsFromSnapshot(snapshot),
                    true,
                    false));
            if (memory != null && source.kind().trackedStorage()) {
                memory.observeSnapshot(ref, snapshot, tick, "workspace_index_display_live_read");
            }
        }

        for (RememberedStorageContents rememberedContents : rememberedById.values()) {
            if (rememberedContents == null
                    || entries.containsKey(rememberedContents.storageId())
                    || !isTrackedDisplayMemory(rememberedContents)) {
                continue;
            }
            entries.put(rememberedContents.storageId(), rememberedEntry(rememberedContents, false));
        }

        return new WorkspaceStorageIndex(entries, carriedCounts(authority), liveDisplays, memoryRevision);
    }

    private static StorageEntry liveClaimedEntry(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            WorldStorageAccess worldStorage,
            ClaimedChest chest,
            boolean proximate,
            WorkspaceStorageMemoryStore memory,
            long tick
    ) {
        if (worldStorage == null || chest == null) {
            return null;
        }
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        boolean accessible;
        try {
            accessible = worldStorage.isAccessible(server, target);
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] storage index accessibility failed for {}: {}",
                    chest.storageId(), safeMessage(exception));
            return null;
        }
        if (!accessible) {
            return null;
        }
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot;
        try {
            snapshot = readSnapshot(server, worldStorage, target);
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] storage index read failed for {}: {}",
                    chest.storageId(), safeMessage(exception));
            return null;
        }
        boolean takeTarget = StorageMutationProbe.canExtractAny(server, worldStorage, target, snapshot);
        boolean depositTarget = !hasCarriedProbe(authority)
                || canInsertAnyCarried(server, worldStorage, target, authority);
        StorageTargetRef ref = StorageTargetRef.claimed(
                chest,
                true,
                false,
                proximate,
                depositTarget,
                takeTarget);
        if (memory != null) {
            memory.observeSnapshot(ref, snapshot, tick, "workspace_index_live_read");
        }
        return new StorageEntry(ref, snapshot, countsFromSnapshot(snapshot), true, false);
    }

    private static SlotWorkspaceViewModel.ChestContentsSnapshot readSnapshot(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorldStorageAccess.Target target
    ) {
        int slots = Math.max(0, worldStorage.slotCount(server, target));
        List<WorldStorageAccess.SlotContent> contents = worldStorage.enumerate(server, target);
        ArrayList<ItemStack> stacks = new ArrayList<>();
        ArrayList<Integer> slotIndices = new ArrayList<>();
        if (contents != null) {
            for (WorldStorageAccess.SlotContent content : contents) {
                if (content == null || content.stack() == null || content.stack().isEmpty()) {
                    continue;
                }
                stacks.add(content.stack().copy());
                slotIndices.add(content.slotIndex());
            }
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(slots, stacks, slotIndices);
    }

    private static StorageEntry rememberedEntry(RememberedStorageContents remembered, boolean proximate) {
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = remembered.toSnapshot();
        return new StorageEntry(
                remembered.targetRef(false, proximate),
                snapshot,
                remembered.countsByIdentity(),
                false,
                true);
    }

    private static SlotWorkspaceViewModel.ChestContentsSnapshot snapshotFromDisplay(WorldDisplayStorageSource source) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        for (WorldStorageAccess.SlotContent content : source.contents()) {
            if (content == null || content.stack() == null || content.stack().isEmpty()) {
                continue;
            }
            stacks.add(content.stack().copy());
            indices.add(content.slotIndex());
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(source.slotCount(), stacks, indices);
    }

    public Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver() {
        return this::contents;
    }

    public SlotWorkspaceViewModel.ChestContentsSnapshot contents(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
        }
        StorageEntry entry = entriesByStorageId.get(storageId);
        return entry == null ? SlotWorkspaceViewModel.ChestContentsSnapshot.empty() : entry.snapshot();
    }

    public StorageTargetRef target(String storageId) {
        StorageEntry entry = storageId == null ? null : entriesByStorageId.get(storageId);
        return entry == null ? null : entry.target();
    }

    public Collection<StorageEntry> entries() {
        return entriesByStorageId.values();
    }

    public List<WorldDisplayStorageSource> displaySources() {
        return displaySources;
    }

    public List<StorageEntry> trackedDisplayEntries() {
        ArrayList<StorageEntry> out = new ArrayList<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry == null || entry.target() == null || !entry.target().displayTarget()) {
                continue;
            }
            if (entry.target().displayKind() != null && entry.target().displayKind().trackedStorage()) {
                out.add(entry);
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    public Set<String> liveDepositStorageIds() {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry == null || !entry.live() || entry.target() == null || entry.target().displayTarget()) {
                continue;
            }
            if (!entry.target().depositTarget()) {
                continue;
            }
            if (!StorageAffinityPolicy.isEligibleSlotCount(entry.snapshot().slotCount())) {
                continue;
            }
            out.add(entry.target().storageId());
        }
        return out.isEmpty() ? Set.of() : Set.copyOf(out);
    }

    public Map<ItemIdentity, Integer> carriedCountsByIdentity() {
        return carriedCountsByIdentity;
    }

    public long memoryRevision() {
        return memoryRevision;
    }

    public Map<ItemIdentity, Integer> liveWorldCountsByIdentity() {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry.live()) {
                mergeCounts(counts, entry.countsByIdentity());
            }
        }
        return Map.copyOf(counts);
    }

    public Map<ItemIdentity, Integer> rememberedWorldCountsByIdentity() {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry.remembered()) {
                mergeCounts(counts, entry.countsByIdentity());
            }
        }
        return Map.copyOf(counts);
    }

    public DepositPlanner.ChestContentPresence liveChestContentPresence() {
        LinkedHashMap<UUID, Set<ItemIdentity>> identitiesByChest = new LinkedHashMap<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry == null || !entry.live() || entry.target() == null || entry.target().displayTarget()) {
                continue;
            }
            if (!entry.target().depositTarget()) {
                continue;
            }
            if (!StorageAffinityPolicy.isEligibleSlotCount(entry.snapshot().slotCount())) {
                continue;
            }
            try {
                UUID storageId = UUID.fromString(entry.target().storageId());
                identitiesByChest.put(storageId, Set.copyOf(entry.countsByIdentity().keySet()));
            } catch (IllegalArgumentException ignored) {
                // Non-UUID ids are display storage and are intentionally not
                // used as claimed-chest deposit authorization.
            }
        }
        return (chest, identity) -> {
            if (chest == null || identity == null) {
                return false;
            }
            Set<ItemIdentity> identities = identitiesByChest.getOrDefault(chest.storageId(), Set.of());
            return identities.contains(ItemIdentityMatcher.normalizeMovable(identity));
        };
    }

    public DepositPlanner.ChestEligibility liveStorageAffinityEligibility() {
        LinkedHashMap<UUID, Boolean> eligibleByChest = new LinkedHashMap<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry == null || !entry.live() || entry.target() == null || entry.target().displayTarget()) {
                continue;
            }
            try {
                UUID storageId = UUID.fromString(entry.target().storageId());
                eligibleByChest.put(storageId,
                        entry.target().depositTarget()
                                && StorageAffinityPolicy.isEligibleSlotCount(entry.snapshot().slotCount()));
            } catch (IllegalArgumentException ignored) {
                // Non-UUID ids are display storage and are intentionally not
                // used as claimed-chest deposit authorization.
            }
        }
        return chest -> chest != null && eligibleByChest.getOrDefault(chest.storageId(), false);
    }

    private static boolean hasCarriedProbe(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return false;
        }
        for (String sourceId : carriedSourceIds(authority)) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry != null && entry.present() && entry.stack() != null && !entry.stack().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canInsertAnyCarried(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorldStorageAccess.Target target,
            InventoryAuthoritySnapshot authority
    ) {
        if (authority == null) {
            return true;
        }
        for (String sourceId : carriedSourceIds(authority)) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                if (StorageMutationProbe.canInsertAny(server, worldStorage, target, entry.stack(), entry.count())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Collection<String> carriedSourceIds(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return List.of();
        }
        var carriedSources = authority.carriedSources();
        return carriedSources.isEmpty()
                ? authority.sourcesById().keySet()
                : carriedSources.stream().map(source -> source.id()).toList();
    }

    private static Map<ItemIdentity, Integer> carriedCounts(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        Collection<String> sourceIds = carriedSourceIds(authority);
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(entry.stack()));
                if (identity != null) {
                    counts.merge(identity, entry.count(), Integer::sum);
                }
            }
        }
        return Map.copyOf(counts);
    }

    private static Map<ItemIdentity, Integer> countsFromSnapshot(
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot
    ) {
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (ItemStack stack : snapshot.contents()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
            if (identity != null) {
                counts.merge(identity, stack.getCount(), Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    private static boolean isTrackedDisplayMemory(RememberedStorageContents remembered) {
        if (remembered == null) {
            return false;
        }
        if (!remembered.targetKind().startsWith(StorageTargetRef.KIND_DISPLAY_PREFIX)) {
            return false;
        }
        var kind = dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.fromKey(
                remembered.targetKind().substring(StorageTargetRef.KIND_DISPLAY_PREFIX.length()));
        return kind != null && kind.trackedStorage();
    }

    private static void mergeCounts(Map<ItemIdentity, Integer> target, Map<ItemIdentity, Integer> source) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<ItemIdentity, Integer> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                target.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
    }

    private static String safeMessage(RuntimeException exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "runtime_exception";
        }
        return exception.getMessage().replace('\n', ' ').replace('\r', ' ');
    }

    public record StorageEntry(
            StorageTargetRef target,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot,
            Map<ItemIdentity, Integer> countsByIdentity,
            boolean live,
            boolean remembered
    ) {
        public StorageEntry {
            snapshot = snapshot == null ? SlotWorkspaceViewModel.ChestContentsSnapshot.empty() : snapshot;
            countsByIdentity = countsByIdentity == null ? Map.of() : Map.copyOf(countsByIdentity);
        }
    }
}
