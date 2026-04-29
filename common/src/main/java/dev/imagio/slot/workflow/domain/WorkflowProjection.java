package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WorkflowProjection {
    private WorkflowProjection() {
    }

    public static Snapshot empty() {
        return Snapshot.empty();
    }

    public static Snapshot apply(Snapshot snapshot, WorkflowEventRecord record) {
        Snapshot current = snapshot == null ? Snapshot.empty() : snapshot;
        if (record == null || record.event() == null) {
            return current;
        }

        ArrayList<CollectionDefinition> userCollections = new ArrayList<>(current.userCollections());
        LinkedHashMap<ItemIdentity, Set<String>> memberships = new LinkedHashMap<>(current.memberships());
        LinkedHashMap<String, Map<ItemIdentity, Integer>> desiredCountsByCollection = new LinkedHashMap<>(current.desiredCountsByCollection());
        LinkedHashMap<String, List<QuickAccessLoadoutDefinition>> loadoutsByCollection = new LinkedHashMap<>(current.loadoutsByCollection());
        LinkedHashSet<ItemIdentity> favorites = new LinkedHashSet<>(current.favoriteTags());
        LinkedHashSet<ItemIdentity> junk = new LinkedHashSet<>(current.junkTags());
        LinkedHashSet<ItemIdentity> protectedIdentities = new LinkedHashSet<>(current.protection().protectedIdentities());
        LinkedHashSet<InventoryActionTarget> protectedTargets = new LinkedHashSet<>(current.protection().protectedTargets());
        boolean protectPortableContainers = current.protection().protectPortableContainers();
        LinkedHashMap<ItemIdentity, Long> recentDismissals = new LinkedHashMap<>(current.recentDismissedUpToByIdentity());
        ArrayList<VisualAtlasIsland> playerIslands = new ArrayList<>(current.visualHomeMap().playerIslands());
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> visualHomes = new LinkedHashMap<>(current.visualHomeMap().assignments());
        LinkedHashSet<String> dismissedTemplateIds = new LinkedHashSet<>(current.visualHomeMap().dismissedTemplateIds());
        LinkedHashMap<UUID, ClaimedChest> claimedChests = indexChests(current.claimedChestMap().chests());
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> affinity = copyAffinity(current.chestAffinityMap().entries());
        KitMap kitMap = current.kitMap();

        switch (record.event()) {
            case WorkflowEvent.CollectionCreated event -> {
                if (!event.collectionId().isBlank() && !event.name().isBlank() && userCollections.stream().noneMatch(def -> def.id().equals(event.collectionId()))) {
                    userCollections.add(new CollectionDefinition(event.collectionId(), event.name(), false));
                    desiredCountsByCollection.putIfAbsent(event.collectionId(), Map.of());
                    loadoutsByCollection.putIfAbsent(event.collectionId(), List.of());
                }
            }
            case WorkflowEvent.CollectionRenamed event -> {
                for (int index = 0; index < userCollections.size(); index++) {
                    CollectionDefinition existing = userCollections.get(index);
                    if (existing.id().equals(event.collectionId())) {
                        userCollections.set(index, new CollectionDefinition(existing.id(), event.name(), false));
                        break;
                    }
                }
            }
            case WorkflowEvent.CollectionDeleted event -> {
                userCollections.removeIf(definition -> definition.id().equals(event.collectionId()));
                desiredCountsByCollection.remove(event.collectionId());
                loadoutsByCollection.remove(event.collectionId());
                memberships.replaceAll((identity, collectionIds) -> {
                    LinkedHashSet<String> updated = new LinkedHashSet<>(collectionIds);
                    updated.remove(event.collectionId());
                    return Set.copyOf(updated);
                });
                memberships.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            }
            case WorkflowEvent.CollectionItemAdded event -> {
                if (event.identity() != null && current.collectionIds().contains(event.collectionId())) {
                    LinkedHashSet<String> updated = new LinkedHashSet<>(memberships.getOrDefault(event.identity(), Set.of()));
                    updated.add(event.collectionId());
                    memberships.put(event.identity(), Set.copyOf(updated));
                    LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>(desiredCountsByCollection.getOrDefault(event.collectionId(), Map.of()));
                    counts.putIfAbsent(event.identity(), 1);
                    desiredCountsByCollection.put(event.collectionId(), Map.copyOf(counts));
                }
            }
            case WorkflowEvent.CollectionItemRemoved event -> {
                if (event.identity() != null) {
                    LinkedHashSet<String> updated = new LinkedHashSet<>(memberships.getOrDefault(event.identity(), Set.of()));
                    updated.remove(event.collectionId());
                    if (updated.isEmpty()) {
                        memberships.remove(event.identity());
                    } else {
                        memberships.put(event.identity(), Set.copyOf(updated));
                    }
                    LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>(desiredCountsByCollection.getOrDefault(event.collectionId(), Map.of()));
                    counts.remove(event.identity());
                    desiredCountsByCollection.put(event.collectionId(), Map.copyOf(counts));
                }
            }
            case WorkflowEvent.DesiredCountSet event -> {
                if (event.identity() != null && memberships.getOrDefault(event.identity(), Set.of()).contains(event.collectionId())) {
                    LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>(desiredCountsByCollection.getOrDefault(event.collectionId(), Map.of()));
                    counts.put(event.identity(), Math.max(1, event.desiredCount()));
                    desiredCountsByCollection.put(event.collectionId(), Map.copyOf(counts));
                }
            }
            case WorkflowEvent.LoadoutCreated event -> {
                if (event.loadout() != null && current.collectionIds().contains(event.collectionId())) {
                    ArrayList<QuickAccessLoadoutDefinition> loadouts = new ArrayList<>(loadoutsByCollection.getOrDefault(event.collectionId(), List.of()));
                    loadouts.removeIf(existing -> existing.id().equals(event.loadout().id()));
                    loadouts.add(event.loadout());
                    loadoutsByCollection.put(event.collectionId(), List.copyOf(loadouts));
                }
            }
            case WorkflowEvent.LoadoutRenamed event -> {
                ArrayList<QuickAccessLoadoutDefinition> loadouts = new ArrayList<>(loadoutsByCollection.getOrDefault(event.collectionId(), List.of()));
                for (int index = 0; index < loadouts.size(); index++) {
                    QuickAccessLoadoutDefinition existing = loadouts.get(index);
                    if (existing.id().equals(event.loadoutId())) {
                        loadouts.set(index, new QuickAccessLoadoutDefinition(existing.id(), event.name(), existing.entries()));
                        break;
                    }
                }
                loadoutsByCollection.put(event.collectionId(), List.copyOf(loadouts));
            }
            case WorkflowEvent.LoadoutUpdated event -> {
                ArrayList<QuickAccessLoadoutDefinition> loadouts = new ArrayList<>(loadoutsByCollection.getOrDefault(event.collectionId(), List.of()));
                for (int index = 0; index < loadouts.size(); index++) {
                    QuickAccessLoadoutDefinition existing = loadouts.get(index);
                    if (existing.id().equals(event.loadoutId())) {
                        loadouts.set(index, new QuickAccessLoadoutDefinition(existing.id(), existing.name(), event.entries()));
                        break;
                    }
                }
                loadoutsByCollection.put(event.collectionId(), List.copyOf(loadouts));
            }
            case WorkflowEvent.LoadoutDeleted event -> {
                ArrayList<QuickAccessLoadoutDefinition> loadouts = new ArrayList<>(loadoutsByCollection.getOrDefault(event.collectionId(), List.of()));
                loadouts.removeIf(loadout -> loadout.id().equals(event.loadoutId()));
                loadoutsByCollection.put(event.collectionId(), List.copyOf(loadouts));
            }
            case WorkflowEvent.FavoriteMarked event -> {
                if (event.identity() != null) {
                    favorites.add(event.identity());
                }
            }
            case WorkflowEvent.FavoriteUnmarked event -> {
                if (event.identity() != null) {
                    favorites.remove(event.identity());
                }
            }
            case WorkflowEvent.JunkMarked event -> {
                if (event.identity() != null) {
                    junk.add(event.identity());
                }
            }
            case WorkflowEvent.JunkUnmarked event -> {
                if (event.identity() != null) {
                    junk.remove(event.identity());
                }
            }
            case WorkflowEvent.ProtectedIdentityMarked event -> {
                if (event.identity() != null) {
                    protectedIdentities.add(event.identity());
                }
            }
            case WorkflowEvent.ProtectedIdentityUnmarked event -> {
                if (event.identity() != null) {
                    protectedIdentities.remove(event.identity());
                }
            }
            case WorkflowEvent.ProtectedTargetMarked event -> {
                if (event.target() != null) {
                    protectedTargets.add(event.target());
                }
            }
            case WorkflowEvent.ProtectedTargetUnmarked event -> {
                if (event.target() != null) {
                    protectedTargets.removeIf(candidate -> candidate.stableKey().equals(event.target().stableKey()));
                }
            }
            case WorkflowEvent.PortableContainerProtectionSet event -> protectPortableContainers = event.enabled();
            case WorkflowEvent.RecentDismissedUpTo event -> {
                if (event.identity() != null) {
                    recentDismissals.put(event.identity(), Math.max(
                            recentDismissals.getOrDefault(event.identity(), 0L),
                            event.dismissedUpToGlobalSequence()
                    ));
                }
            }
            case WorkflowEvent.VisualIslandCreated event -> {
                if (event.island() != null) {
                    playerIslands.removeIf(island -> island.id().equals(event.island().id()));
                    playerIslands.add(event.island());
                }
            }
            case WorkflowEvent.VisualIslandMoved event -> {
                if (event.islandId() != null && !event.islandId().isBlank()) {
                    for (int index = 0; index < playerIslands.size(); index++) {
                        VisualAtlasIsland existing = playerIslands.get(index);
                        if (existing.id().equals(event.islandId())) {
                            playerIslands.set(index, new VisualAtlasIsland(
                                    existing.id(),
                                    existing.label(),
                                    existing.kind(),
                                    event.x(),
                                    event.y(),
                                    existing.color(),
                                    existing.iconIdentity()
                            ));
                            break;
                        }
                    }
                }
            }
            case WorkflowEvent.VisualIslandRenamed event -> {
                if (event.islandId() != null && !event.islandId().isBlank() && !event.label().isBlank()) {
                    for (int index = 0; index < playerIslands.size(); index++) {
                        VisualAtlasIsland existing = playerIslands.get(index);
                        if (existing.id().equals(event.islandId())) {
                            playerIslands.set(index, new VisualAtlasIsland(
                                    existing.id(),
                                    event.label(),
                                    existing.kind(),
                                    existing.x(),
                                    existing.y(),
                                    existing.color(),
                                    existing.iconIdentity()
                            ));
                            break;
                        }
                    }
                }
            }
            case WorkflowEvent.VisualIslandRecolored event -> {
                if (event.islandId() != null && !event.islandId().isBlank()) {
                    for (int index = 0; index < playerIslands.size(); index++) {
                        VisualAtlasIsland existing = playerIslands.get(index);
                        if (existing.id().equals(event.islandId())) {
                            playerIslands.set(index, new VisualAtlasIsland(
                                    existing.id(),
                                    existing.label(),
                                    existing.kind(),
                                    existing.x(),
                                    existing.y(),
                                    event.color(),
                                    existing.iconIdentity()
                            ));
                            break;
                        }
                    }
                }
            }
            case WorkflowEvent.VisualIslandIconChanged event -> {
                if (event.islandId() != null && !event.islandId().isBlank()) {
                    for (int index = 0; index < playerIslands.size(); index++) {
                        VisualAtlasIsland existing = playerIslands.get(index);
                        if (existing.id().equals(event.islandId())) {
                            playerIslands.set(index, new VisualAtlasIsland(
                                    existing.id(),
                                    existing.label(),
                                    existing.kind(),
                                    existing.x(),
                                    existing.y(),
                                    existing.color(),
                                    event.iconIdentity()
                            ));
                            break;
                        }
                    }
                }
            }
            case WorkflowEvent.VisualIslandDeleted event -> {
                if (event.islandId() != null && !event.islandId().isBlank()) {
                    playerIslands.removeIf(island -> island.id().equals(event.islandId()));
                    visualHomes.entrySet().removeIf(entry ->
                            entry.getValue() != null && event.islandId().equals(entry.getValue().islandId()));
                }
            }
            case WorkflowEvent.VisualHomeAssigned event -> {
                VisualHomeAssignment requested = event.assignment();
                if (requested != null && requested.identity() != null) {
                    applyVisualHomeAssignment(visualHomes, requested);
                }
            }
            case WorkflowEvent.VisualHomeCleared event -> {
                if (event.identity() != null) {
                    VisualHomeAssignment removed = visualHomes.remove(event.identity());
                    if (removed != null) {
                        compactOrdinalsAfterRemove(visualHomes, removed.islandId(), removed.ordinal());
                    }
                }
            }
            case WorkflowEvent.TemplateIslandDismissed event -> {
                if (event.templateId() != null && !event.templateId().isBlank()) {
                    dismissedTemplateIds.add(event.templateId());
                }
            }
            case WorkflowEvent.ClaimedChestCreated event -> {
                if (event.chest() != null) {
                    claimedChests.put(event.chest().storageId(), event.chest());
                }
            }
            case WorkflowEvent.ClaimedChestMoved event -> {
                if (event.storageId() != null) {
                    ClaimedChest existing = claimedChests.get(event.storageId());
                    if (existing != null) {
                        claimedChests.put(event.storageId(), existing.withAtlasPosition(event.atlasX(), event.atlasY()));
                    }
                }
            }
            case WorkflowEvent.ClaimedChestAnchorsChanged event -> {
                if (event.storageId() != null) {
                    ClaimedChest existing = claimedChests.get(event.storageId());
                    if (existing != null) {
                        if (event.anchors().isEmpty()) {
                            claimedChests.remove(event.storageId());
                            affinity.remove(event.storageId());
                        } else {
                            claimedChests.put(event.storageId(), existing.withAnchors(event.anchors()));
                        }
                    }
                }
            }
            case WorkflowEvent.ClaimedChestRelabeled event -> {
                if (event.storageId() != null) {
                    ClaimedChest existing = claimedChests.get(event.storageId());
                    if (existing != null) {
                        claimedChests.put(event.storageId(), existing.withLabel(event.label()));
                    }
                }
            }
            case WorkflowEvent.ClaimedChestDeleted event -> {
                if (event.storageId() != null) {
                    claimedChests.remove(event.storageId());
                    affinity.remove(event.storageId());
                }
            }
            case WorkflowEvent.ChestDepositObserved event -> {
                if (event.storageId() != null && event.identity() != null
                        && claimedChests.containsKey(event.storageId())) {
                    LinkedHashMap<ItemIdentity, ChestAffinity> bonds =
                            new LinkedHashMap<>(affinity.getOrDefault(event.storageId(), Map.of()));
                    ChestAffinity existing = bonds.get(event.identity());
                    ChestAffinity bumped = existing == null
                            ? new ChestAffinity(event.identity(), 1, event.tick())
                            : existing.bump(1, event.tick());
                    bonds.put(event.identity(), bumped);
                    affinity.put(event.storageId(), Map.copyOf(bonds));
                }
            }
            case WorkflowEvent.ChestAffinityForgotten event -> {
                if (event.storageId() != null && event.identity() != null) {
                    Map<ItemIdentity, ChestAffinity> bonds = affinity.get(event.storageId());
                    if (bonds != null && bonds.containsKey(event.identity())) {
                        LinkedHashMap<ItemIdentity, ChestAffinity> next = new LinkedHashMap<>(bonds);
                        next.remove(event.identity());
                        if (next.isEmpty()) {
                            affinity.remove(event.storageId());
                        } else {
                            affinity.put(event.storageId(), Map.copyOf(next));
                        }
                    }
                }
            }
            case WorkflowEvent.ChestAffinityCleared event -> {
                if (event.storageId() != null) {
                    affinity.remove(event.storageId());
                }
            }
            case WorkflowEvent.KitCreated event -> {
                if (event.kit() != null && !event.kit().id().isBlank()) {
                    kitMap = kitMap.withKit(event.kit());
                }
            }
            case WorkflowEvent.KitUpdated event -> {
                if (event.kit() != null && !event.kit().id().isBlank() && kitMap.kit(event.kit().id()) != null) {
                    kitMap = kitMap.withKit(event.kit());
                }
            }
            case WorkflowEvent.KitDeleted event -> {
                if (!event.kitId().isBlank()) {
                    kitMap = kitMap.withoutKit(event.kitId());
                }
            }
            case WorkflowEvent.KitActivated event -> {
                if (!event.kitId().isBlank() && kitMap.kit(event.kitId()) != null) {
                    kitMap = kitMap.withActivation(new KitActivation(event.kitId(), event.pageIndex()));
                }
            }
            case WorkflowEvent.KitDeactivated event -> {
                kitMap = kitMap.withActivation(KitActivation.NONE);
            }
            case WorkflowEvent.KitPageSwitched event -> {
                KitActivation activeAssignment = kitMap.activation();
                if (activeAssignment.isActive()) {
                    KitDefinition activeKit = kitMap.kit(activeAssignment.kitId());
                    if (activeKit != null && event.pageIndex() < activeKit.pageCount()) {
                        kitMap = kitMap.withActivation(new KitActivation(activeAssignment.kitId(), event.pageIndex()));
                    }
                }
            }
        }

        return new Snapshot(
                userCollections,
                memberships,
                desiredCountsByCollection,
                loadoutsByCollection,
                favorites,
                junk,
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                recentDismissals,
                new VisualHomeMap(playerIslands, visualHomes, dismissedTemplateIds),
                new ClaimedChestMap(new ArrayList<>(claimedChests.values())),
                new ChestAffinityMap(affinity),
                kitMap
        );
    }

    static void applyVisualHomeAssignment(
            LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments,
            VisualHomeAssignment requested
    ) {
        ItemIdentity identity = requested.identity();
        String dstIslandId = requested.islandId();
        int dstOrdinal = Math.max(0, requested.ordinal());

        VisualHomeAssignment previous = assignments.remove(identity);
        if (previous != null) {
            compactOrdinalsAfterRemove(assignments, previous.islandId(), previous.ordinal());
        }

        int dstSize = islandSize(assignments, dstIslandId);
        int insertOrdinal = Math.min(dstOrdinal, dstSize);
        if (previous != null
                && previous.islandId().equals(dstIslandId)
                && previous.ordinal() < dstOrdinal) {
            insertOrdinal = Math.max(0, Math.min(dstOrdinal - 1, dstSize));
        }
        for (Map.Entry<ItemIdentity, VisualHomeAssignment> entry : assignments.entrySet()) {
            VisualHomeAssignment existing = entry.getValue();
            if (existing != null
                    && dstIslandId.equals(existing.islandId())
                    && existing.ordinal() >= insertOrdinal) {
                entry.setValue(new VisualHomeAssignment(
                        existing.identity(),
                        existing.islandId(),
                        existing.ordinal() + 1,
                        existing.origin(),
                        existing.locked()
                ));
            }
        }
        assignments.put(identity, new VisualHomeAssignment(
                identity,
                dstIslandId,
                insertOrdinal,
                requested.origin(),
                requested.locked()
        ));
    }

    static void compactOrdinalsAfterRemove(
            LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments,
            String islandId,
            int removedOrdinal
    ) {
        for (Map.Entry<ItemIdentity, VisualHomeAssignment> entry : assignments.entrySet()) {
            VisualHomeAssignment existing = entry.getValue();
            if (existing != null
                    && islandId.equals(existing.islandId())
                    && existing.ordinal() > removedOrdinal) {
                entry.setValue(new VisualHomeAssignment(
                        existing.identity(),
                        existing.islandId(),
                        existing.ordinal() - 1,
                        existing.origin(),
                        existing.locked()
                ));
            }
        }
    }

    private static int islandSize(
            Map<ItemIdentity, VisualHomeAssignment> assignments,
            String islandId
    ) {
        int count = 0;
        for (VisualHomeAssignment assignment : assignments.values()) {
            if (assignment != null && islandId.equals(assignment.islandId())) {
                count++;
            }
        }
        return count;
    }

    private static LinkedHashMap<UUID, ClaimedChest> indexChests(List<ClaimedChest> source) {
        LinkedHashMap<UUID, ClaimedChest> indexed = new LinkedHashMap<>();
        if (source == null) {
            return indexed;
        }
        for (ClaimedChest chest : source) {
            if (chest != null) {
                indexed.put(chest.storageId(), chest);
            }
        }
        return indexed;
    }

    private static LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> copyAffinity(
            Map<UUID, Map<ItemIdentity, ChestAffinity>> source
    ) {
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> copy = new LinkedHashMap<>();
        if (source == null) {
            return copy;
        }
        for (Map.Entry<UUID, Map<ItemIdentity, ChestAffinity>> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                copy.put(entry.getKey(), Map.copyOf(entry.getValue()));
            }
        }
        return copy;
    }

    public static Snapshot replay(WorkflowEventStore.Snapshot storeSnapshot, Snapshot checkpoint) {
        Snapshot current = checkpoint == null ? Snapshot.empty() : checkpoint;
        WorkflowEventStore.Snapshot resolved = storeSnapshot == null ? WorkflowEventStore.Snapshot.empty() : storeSnapshot;
        for (WorkflowEventRecord record : resolved.records()) {
            current = apply(current, record);
        }
        return current;
    }

    public record Snapshot(
            List<CollectionDefinition> userCollections,
            Map<ItemIdentity, Set<String>> memberships,
            Map<String, Map<ItemIdentity, Integer>> desiredCountsByCollection,
            Map<String, List<QuickAccessLoadoutDefinition>> loadoutsByCollection,
            Set<ItemIdentity> favoriteTags,
            Set<ItemIdentity> junkTags,
            ProtectionSnapshotPolicy protection,
            Map<ItemIdentity, Long> recentDismissedUpToByIdentity,
            VisualHomeMap visualHomeMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap chestAffinityMap,
            KitMap kitMap
    ) {
        public Snapshot {
            userCollections = userCollections == null ? List.of() : List.copyOf(userCollections);
            memberships = CollectionProjection.copyMemberships(memberships);
            desiredCountsByCollection = CollectionProjection.copyDesiredCounts(desiredCountsByCollection);
            loadoutsByCollection = CollectionProjection.copyLoadouts(loadoutsByCollection);
            favoriteTags = favoriteTags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(favoriteTags));
            junkTags = junkTags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(junkTags));
            protection = protection == null ? new ProtectionSnapshotPolicy(Set.of(), Set.of(), false) : protection;
            recentDismissedUpToByIdentity = copyRecentDismissals(recentDismissedUpToByIdentity);
            visualHomeMap = visualHomeMap == null ? VisualHomeMap.empty() : visualHomeMap;
            claimedChestMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
            chestAffinityMap = chestAffinityMap == null ? ChestAffinityMap.empty() : chestAffinityMap;
            kitMap = kitMap == null ? KitMap.empty() : kitMap;
        }

        public static Snapshot empty() {
            return new Snapshot(
                    List.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    Set.of(),
                    Set.of(),
                    new ProtectionSnapshotPolicy(Set.of(), Set.of(), false),
                    Map.of(),
                    VisualHomeMap.empty(),
                    ClaimedChestMap.empty(),
                    ChestAffinityMap.empty(),
                    KitMap.empty()
            );
        }

        public Set<String> collectionIds() {
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            for (CollectionDefinition collection : userCollections) {
                if (collection != null && !collection.id().isBlank()) {
                    ids.add(collection.id());
                }
            }
            return Set.copyOf(ids);
        }

        public CollectionProjection collections() {
            return new CollectionProjection(
                    userCollections,
                    memberships,
                    desiredCountsByCollection,
                    loadoutsByCollection,
                    favoriteTags,
                    junkTags
            );
        }

        private static Map<ItemIdentity, Long> copyRecentDismissals(Map<ItemIdentity, Long> source) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<ItemIdentity, Long> copied = new LinkedHashMap<>();
            source.forEach((identity, value) -> {
                if (identity != null && value != null && value >= 0L) {
                    copied.put(identity, value);
                }
            });
            return Map.copyOf(copied);
        }
    }
}
