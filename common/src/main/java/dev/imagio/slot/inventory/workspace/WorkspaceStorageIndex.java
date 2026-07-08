package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAnchor;
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

    WorkspaceStorageIndex(
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
            if (!chest.role().visibleToWorkspace()) {
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
                if (memory != null) {
                    memory.forget(storageId);
                }
                continue;
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
            StorageTargetRef ref = displaySourceRef(source, rememberedById, false, true);
            entries.put(source.storageId(), new StorageEntry(
                    ref,
                    snapshot,
                    countsFromSnapshot(snapshot),
                    true,
                    false));
        }

        AliasCorrection aliasCorrection = correctDisplayAliases(
                server,
                worldStorage,
                memory,
                tick,
                entries,
                resolvedMap,
                liveDisplays);
        entries = new LinkedHashMap<>(aliasCorrection.entries());
        liveDisplays = aliasCorrection.displaySources();
        rememberDisplaySources(memory, tick, entries, liveDisplays, "workspace_index_display_live_read");

        for (RememberedStorageContents rememberedContents : rememberedById.values()) {
            if (rememberedContents == null
                    || entries.containsKey(rememberedContents.storageId())
                    || !isTrackedStorageMemory(rememberedContents)) {
                continue;
            }
            entries.put(rememberedContents.storageId(), rememberedEntry(rememberedContents, false));
        }

        return new WorkspaceStorageIndex(entries, carriedCounts(authority), liveDisplays, memoryRevision);
    }

    static AliasCorrection correctDisplayAliases(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorkspaceStorageMemoryStore memory,
            long tick,
            Map<String, StorageEntry> entries,
            ClaimedChestMap claimedChestMap,
            List<WorldDisplayStorageSource> displaySources
    ) {
        if (entries == null || entries.isEmpty() || displaySources == null || displaySources.isEmpty()) {
            return new AliasCorrection(entries, displaySources);
        }
        Map<WorldDisplayStorageSource.AliasedBlock, List<String>> storageIdsByBlock =
                storageIdsByAliasedBlock(claimedChestMap, displaySources);
        if (storageIdsByBlock.isEmpty()) {
            return new AliasCorrection(entries, displaySources);
        }
        LinkedHashMap<String, StorageEntry> correctedEntries = new LinkedHashMap<>(entries);
        ArrayList<WorldDisplayStorageSource> correctedSources = new ArrayList<>(displaySources.size());
        boolean changed = false;
        for (WorldDisplayStorageSource source : displaySources) {
            if (source == null || source.aliasedBlocks().isEmpty()) {
                correctedSources.add(source);
                continue;
            }
            StorageEntry displayEntry = correctedEntries.get(source.storageId());
            if (displayEntry == null || displayEntry.target() == null || !displayEntry.target().displayTarget()) {
                correctedSources.add(source);
                continue;
            }
            Map<ItemIdentity, Integer> aliasedCounts =
                    aliasedCounts(
                            server,
                            worldStorage,
                            memory,
                            tick,
                            source.storageId(),
                            source.aliasedBlocks(),
                            storageIdsByBlock,
                            correctedEntries,
                            claimedChestMap);
            if (aliasedCounts.isEmpty()) {
                correctedSources.add(source);
                continue;
            }
            StorageEntry corrected = subtractAliasedCounts(displayEntry, aliasedCounts);
            if (corrected == displayEntry) {
                correctedSources.add(source);
                continue;
            }
            correctedEntries.put(source.storageId(), corrected);
            correctedSources.add(sourceWithSnapshot(source, corrected.snapshot()));
            changed = true;
        }
        if (!correctedEntries.equals(entries)) {
            changed = true;
        }
        if (!changed) {
            return new AliasCorrection(entries, displaySources);
        }
        return new AliasCorrection(correctedEntries, correctedSources);
    }

    private static Map<WorldDisplayStorageSource.AliasedBlock, List<String>> storageIdsByAliasedBlock(
            ClaimedChestMap claimedChestMap,
            List<WorldDisplayStorageSource> displaySources
    ) {
        LinkedHashMap<WorldDisplayStorageSource.AliasedBlock, ArrayList<String>> ids = new LinkedHashMap<>();
        if (claimedChestMap != null && !claimedChestMap.chests().isEmpty()) {
            for (ClaimedChest chest : claimedChestMap.chests()) {
                if (chest == null || !chest.role().visibleToWorkspace()) {
                    continue;
                }
                String storageId = chest.storageId().toString();
                for (ChestAnchor anchor : chest.anchors()) {
                    if (anchor == null || anchor.dimensionId().isBlank()) {
                        continue;
                    }
                    WorldDisplayStorageSource.AliasedBlock block =
                            new WorldDisplayStorageSource.AliasedBlock(
                                    anchor.dimensionId(), anchor.x(), anchor.y(), anchor.z());
                    ids.computeIfAbsent(block, ignored -> new ArrayList<>()).add(storageId);
                }
            }
        }
        if (displaySources != null) {
            for (WorldDisplayStorageSource source : displaySources) {
                if (source == null
                        || source.kind() != dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.AE2_NETWORK
                        || source.dimensionId().isBlank()
                        || source.storageId().isBlank()) {
                    continue;
                }
                WorldDisplayStorageSource.AliasedBlock block =
                        new WorldDisplayStorageSource.AliasedBlock(
                                source.dimensionId(), source.x(), source.y(), source.z());
                ids.computeIfAbsent(block, ignored -> new ArrayList<>()).add(source.storageId());
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<WorldDisplayStorageSource.AliasedBlock, List<String>> out = new LinkedHashMap<>();
        for (Map.Entry<WorldDisplayStorageSource.AliasedBlock, ArrayList<String>> entry : ids.entrySet()) {
            out.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(out);
    }

    private static Map<ItemIdentity, Integer> aliasedCounts(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorkspaceStorageMemoryStore memory,
            long tick,
            String sourceStorageId,
            List<WorldDisplayStorageSource.AliasedBlock> aliases,
            Map<WorldDisplayStorageSource.AliasedBlock, List<String>> storageIdsByBlock,
            Map<String, StorageEntry> entries,
            ClaimedChestMap claimedChestMap
    ) {
        if (aliases == null || aliases.isEmpty() || storageIdsByBlock.isEmpty() || entries == null) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        LinkedHashSet<String> seenStorageIds = new LinkedHashSet<>();
        for (WorldDisplayStorageSource.AliasedBlock alias : aliases) {
            if (alias == null) {
                continue;
            }
            List<String> mappedStorageIds = storageIdsByBlock.getOrDefault(alias, List.of());
            List<String> displayStorageIds = mappedStorageIds.stream()
                    .filter(id -> id != null
                            && !id.equals(sourceStorageId)
                            && entries.get(id) != null
                            && entries.get(id).target() != null
                            && entries.get(id).target().displayTarget())
                    .toList();
            if (displayStorageIds.size() > 1) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] AE2 alias {} maps to multiple display storages {}; skipping display alias correction",
                        alias,
                        displayStorageIds);
            }
            for (String storageId : mappedStorageIds) {
                if (storageId == null
                        || storageId.isBlank()
                        || storageId.equals(sourceStorageId)
                        || !seenStorageIds.add(storageId)) {
                    continue;
                }
                StorageEntry entry = entries.get(storageId);
                if (entry != null
                        && entry.target() != null
                        && entry.target().displayTarget()
                        && displayStorageIds.size() != 1) {
                    continue;
                }
                StorageEntry readableEntry = aliasReadableEntry(
                        server,
                        worldStorage,
                        memory,
                        tick,
                        claimedChestMap,
                        storageId,
                        entry);
                if (readableEntry == null || readableEntry.target() == null) {
                    continue;
                }
                if (readableEntry != entry) {
                    entries.put(storageId, readableEntry);
                }
                if (readableEntry != entry && readableEntry.countsByIdentity().isEmpty()) {
                    continue;
                }
                mergeCounts(counts, readableEntry.countsByIdentity());
            }
        }
        return counts.isEmpty() ? Map.of() : Map.copyOf(counts);
    }

    private static StorageEntry aliasReadableEntry(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            WorkspaceStorageMemoryStore memory,
            long tick,
            ClaimedChestMap claimedChestMap,
            String storageId,
            StorageEntry current
    ) {
        if (current != null
                && current.target() != null
                && !current.target().displayTarget()
                && !current.countsByIdentity().isEmpty()) {
            return current;
        }
        ClaimedChest chest = claimedChest(claimedChestMap, storageId);
        if (chest == null || worldStorage == null) {
            return current;
        }
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        try {
            if (!worldStorage.isAccessible(server, target)) {
                return current;
            }
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = readSnapshot(server, worldStorage, target);
            StorageTargetRef ref = StorageTargetRef.claimed(chest, true, current != null && current.remembered(), false);
            if (memory != null) {
                memory.observeSnapshot(ref, snapshot, tick, "workspace_index_ae2_alias_read", false);
            }
            return new StorageEntry(ref, snapshot, countsFromSnapshot(snapshot), false, true);
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] AE2 alias storage read failed for {}: {}",
                    storageId,
                    safeMessage(exception));
            return current;
        }
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

    private static StorageEntry subtractAliasedCounts(
            StorageEntry displayEntry,
            Map<ItemIdentity, Integer> aliasedCounts
    ) {
        if (displayEntry == null || displayEntry.snapshot() == null || aliasedCounts == null || aliasedCounts.isEmpty()) {
            return displayEntry;
        }
        Map<ItemIdentity, Integer> sourceCounts = displayEntry.countsByIdentity();
        if (sourceCounts.isEmpty()) {
            return displayEntry;
        }
        LinkedHashMap<ItemIdentity, Integer> correctedCounts = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<ItemIdentity, Integer> entry : sourceCounts.entrySet()) {
            ItemIdentity identity = ItemIdentityCollections.key(entry.getKey());
            int count = Math.max(0, entry.getValue() == null ? 0 : entry.getValue());
            int aliased = ItemIdentityCollections.count(aliasedCounts, identity);
            int corrected = Math.max(0, count - aliased);
            if (corrected != count) {
                changed = true;
            }
            if (corrected > 0) {
                correctedCounts.put(identity, corrected);
            }
        }
        if (!changed) {
            return displayEntry;
        }
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot =
                snapshotWithCounts(displayEntry.snapshot(), correctedCounts);
        return new StorageEntry(displayEntry.target(), snapshot, correctedCounts, displayEntry.live(), displayEntry.remembered());
    }

    private static SlotWorkspaceViewModel.ChestContentsSnapshot snapshotWithCounts(
            SlotWorkspaceViewModel.ChestContentsSnapshot source,
            Map<ItemIdentity, Integer> correctedCounts
    ) {
        if (source == null || correctedCounts == null || correctedCounts.isEmpty()) {
            return new SlotWorkspaceViewModel.ChestContentsSnapshot(
                    source == null ? 0 : source.slotCount(),
                    List.of(),
                    List.of(),
                    Map.of());
        }
        LinkedHashMap<ItemIdentity, Integer> remaining = new LinkedHashMap<>(correctedCounts);
        ArrayList<ItemStack> stacks = new ArrayList<>();
        ArrayList<Integer> slotIndices = new ArrayList<>();
        List<ItemStack> sourceStacks = source.contents();
        List<Integer> sourceSlots = source.slotIndices();
        for (int i = 0; i < sourceStacks.size(); i++) {
            ItemStack stack = sourceStacks.get(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemIdentity identity = ItemIdentityCollections.key(ItemIdentityMatcher.create(stack));
            int remainingCount = Math.max(0, remaining.getOrDefault(identity, 0));
            if (remainingCount <= 0) {
                continue;
            }
            ItemStack display = stack.copy();
            display.setCount(Math.min(remainingCount, Math.max(1, display.getMaxStackSize())));
            stacks.add(display);
            slotIndices.add(i < sourceSlots.size() ? sourceSlots.get(i) : i);
            remaining.put(identity, 0);
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(
                source.slotCount(),
                stacks,
                slotIndices,
                correctedCounts);
    }

    private static WorldDisplayStorageSource sourceWithSnapshot(
            WorldDisplayStorageSource source,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot
    ) {
        ArrayList<WorldStorageAccess.SlotContent> contents = new ArrayList<>();
        if (snapshot != null) {
            List<ItemStack> stacks = snapshot.contents();
            List<Integer> slots = snapshot.slotIndices();
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityCollections.key(ItemIdentityMatcher.create(stack));
                int count = ItemIdentityCollections.count(snapshot.countsByIdentity(), identity);
                if (count <= 0) {
                    continue;
                }
                contents.add(new WorldStorageAccess.SlotContent(
                        i < slots.size() ? slots.get(i) : i,
                        stack,
                        count));
            }
        }
        return new WorldDisplayStorageSource(
                source.storageId(),
                source.kind(),
                source.label(),
                source.dimensionId(),
                source.x(),
                source.y(),
                source.z(),
                source.slotCount(),
                contents,
                source.aliasedBlocks(),
                source.mediaIds(),
                source.mediaObservations(),
                source.target());
    }

    static void rememberDisplaySources(
            WorkspaceStorageMemoryStore memory,
            long tick,
            Map<String, StorageEntry> entries,
            List<WorldDisplayStorageSource> displaySources,
            String sourceName
    ) {
        if (memory == null || displaySources == null || displaySources.isEmpty()) {
            return;
        }
        for (WorldDisplayStorageSource source : displaySources) {
            if (source == null || source.storageId().isBlank()) {
                continue;
            }
            if (!source.mediaObservations().isEmpty()) {
                memory.observeMediaObservations(source.mediaObservations(), tick, sourceName, false);
            }
            if (!source.trackedStorage()) {
                continue;
            }
            StorageEntry entry = entries == null ? null : entries.get(source.storageId());
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = entry == null
                    ? snapshotFromDisplay(source)
                    : entry.snapshot();
            StorageTargetRef ref = entry == null
                    ? StorageTargetRef.display(source, false, true)
                    : entry.target();
            memory.observe(RememberedStorageContents.fromSourceSnapshot(
                    ref,
                    snapshot,
                    source,
                    tick,
                    sourceName), false);
        }
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
        StorageEntry base = liveClaimedBaseEntry(server, worldStorage, chest, proximate, memory, tick);
        return applyDepositOverlay(server, authority, worldStorage, chest, base);
    }

    static StorageEntry liveClaimedBaseEntry(
            MinecraftServer server,
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
        StorageTargetRef ref = StorageTargetRef.claimed(
                chest,
                true,
                false,
                proximate,
                false,
                takeTarget);
        if (memory != null) {
            memory.observeSnapshot(ref, snapshot, tick, "workspace_index_live_read", false);
        }
        return new StorageEntry(ref, snapshot, countsFromSnapshot(snapshot), true, false);
    }

    static StorageEntry applyDepositOverlay(
            MinecraftServer server,
            InventoryAuthoritySnapshot authority,
            WorldStorageAccess worldStorage,
            ClaimedChest chest,
            StorageEntry base
    ) {
        if (base == null || base.target() == null || chest == null) {
            return base;
        }
        boolean depositTarget = chest.role().quickDepositTarget()
                && (!hasCarriedProbe(authority)
                || canInsertAnyCarried(server, worldStorage, new WorldStorageAccess.Target.Chest(chest), authority));
        return new StorageEntry(
                base.target().withDepositTarget(depositTarget),
                base.snapshot(),
                base.countsByIdentity(),
                base.live(),
                base.remembered());
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
        java.util.LinkedHashMap<ItemIdentity, Integer> counts = new java.util.LinkedHashMap<>();
        if (contents != null) {
            for (WorldStorageAccess.SlotContent content : contents) {
                if (content == null || content.stack() == null || content.stack().isEmpty()) {
                    continue;
                }
                stacks.add(content.stack().copy());
                slotIndices.add(content.slotIndex());
                ItemIdentityCollections.mergeCount(
                        counts,
                        ItemIdentityMatcher.create(content.stack()),
                        content.count());
            }
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(slots, stacks, slotIndices, counts);
    }

    static StorageEntry rememberedEntry(RememberedStorageContents remembered, boolean proximate) {
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = remembered.toSnapshot();
        return new StorageEntry(
                remembered.targetRef(false, proximate),
                snapshot,
                remembered.countsByIdentity(),
                false,
                true);
    }

    static StorageTargetRef displaySourceRef(
            WorldDisplayStorageSource source,
            Map<String, RememberedStorageContents> rememberedById,
            boolean remembered,
            boolean proximate
    ) {
        StorageTargetRef ref = StorageTargetRef.display(source, remembered, proximate);
        if (ref == null || source == null || !ref.ae2Network()
                || !(source.target() instanceof WorldStorageAccess.Target.Virtual virtual)
                || !"open_terminal".equals(virtual.routeKind())) {
            return ref;
        }
        RememberedStorageContents previous = rememberedById == null ? null : rememberedById.get(source.storageId());
        if (previous == null || previous.dimensionId().isBlank()) {
            return ref;
        }
        return new StorageTargetRef(
                ref.storageId(),
                ref.targetKind(),
                ref.label(),
                previous.dimensionId(),
                previous.x(),
                previous.y(),
                previous.z(),
                ref.liveReadable(),
                ref.depositTarget(),
                ref.takeTarget(),
                ref.remembered(),
                ref.proximate());
    }

    static SlotWorkspaceViewModel.ChestContentsSnapshot snapshotFromDisplay(WorldDisplayStorageSource source) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        java.util.LinkedHashMap<ItemIdentity, Integer> counts = new java.util.LinkedHashMap<>();
        for (WorldStorageAccess.SlotContent content : source.contents()) {
            if (content == null || content.stack() == null || content.stack().isEmpty()) {
                continue;
            }
            stacks.add(content.stack().copy());
            indices.add(content.slotIndex());
            ItemIdentityCollections.mergeCount(
                    counts,
                    ItemIdentityMatcher.create(content.stack()),
                    content.count());
        }
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(source.slotCount(), stacks, indices, counts);
    }

    public Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver() {
        return this::contents;
    }

    public Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> liveContentsResolver() {
        return this::liveContents;
    }

    public SlotWorkspaceViewModel.ChestContentsSnapshot contents(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
        }
        StorageEntry entry = entriesByStorageId.get(storageId);
        return entry == null ? SlotWorkspaceViewModel.ChestContentsSnapshot.empty() : entry.snapshot();
    }

    public SlotWorkspaceViewModel.ChestContentsSnapshot liveContents(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
        }
        StorageEntry entry = entriesByStorageId.get(storageId);
        return entry == null || !entry.live()
                ? SlotWorkspaceViewModel.ChestContentsSnapshot.empty()
                : entry.snapshot();
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
            if (entry.target().trackedWorldStorage()) {
                out.add(entry);
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    public List<StorageEntry> liveDisplayEntries() {
        ArrayList<StorageEntry> out = new ArrayList<>();
        for (StorageEntry entry : entriesByStorageId.values()) {
            if (entry == null || !entry.live() || entry.target() == null || !entry.target().displayTarget()) {
                continue;
            }
            if (entry.target().displayKind() != null) {
                out.add(entry);
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    public List<StorageEntry> liveTrackedDisplayEntries() {
        ArrayList<StorageEntry> out = new ArrayList<>();
        for (StorageEntry entry : trackedDisplayEntries()) {
            if (entry.live()) {
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
            return ItemIdentityCollections.contains(identities, identity);
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

    static boolean hasCarriedProbe(InventoryAuthoritySnapshot authority) {
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

    static boolean canInsertAnyCarried(
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

    static Map<ItemIdentity, Integer> carriedCounts(InventoryAuthoritySnapshot authority) {
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
                ItemIdentityCollections.mergeCount(counts, ItemIdentityMatcher.create(entry.stack()), entry.count());
            }
        }
        return Map.copyOf(counts);
    }

    static Map<ItemIdentity, Integer> countsFromSnapshot(
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot
    ) {
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return Map.of();
        }
        if (!snapshot.countsByIdentity().isEmpty()) {
            return snapshot.countsByIdentity();
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (ItemStack stack : snapshot.contents()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemIdentityCollections.mergeCount(counts, ItemIdentityMatcher.create(stack), stack.getCount());
        }
        return Map.copyOf(counts);
    }

    static boolean isTrackedDisplayMemory(RememberedStorageContents remembered) {
        if (remembered == null) {
            return false;
        }
        return isTrackedStorageMemory(remembered);
    }

    static boolean isTrackedStorageMemory(RememberedStorageContents remembered) {
        if (remembered == null) {
            return false;
        }
        if (StorageTargetRef.KIND_AE2_NETWORK.equals(remembered.targetKind())) {
            return true;
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

    static String safeMessage(RuntimeException exception) {
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
            if (snapshot.countsByIdentity().isEmpty() && !countsByIdentity.isEmpty()) {
                snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                        snapshot.slotCount(),
                        snapshot.contents(),
                        snapshot.slotIndices(),
                        countsByIdentity);
            }
        }
    }

    record AliasCorrection(
            Map<String, StorageEntry> entries,
            List<WorldDisplayStorageSource> displaySources
    ) {
        AliasCorrection {
            entries = entries == null || entries.isEmpty() ? Map.of() : Map.copyOf(entries);
            displaySources = displaySources == null ? List.of() : List.copyOf(displaySources);
        }
    }
}
