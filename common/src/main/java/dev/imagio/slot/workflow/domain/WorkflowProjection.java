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
        LinkedHashMap<String, String> clusterLabels = new LinkedHashMap<>(current.clusterLabels());
        KitMap kitMap = current.kitMap();
        LinkedHashMap<ItemIdentity, Integer> playerDesiredCounts = new LinkedHashMap<>(current.playerDesiredCounts());
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitDesiredCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : current.kitDesiredCounts().entrySet()) {
            kitDesiredCounts.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }

        WorkflowEvent workflowEvent = record.event();
        if (workflowEvent instanceof WorkflowEvent.CollectionCreated event) {
                if (!event.collectionId().isBlank() && !event.name().isBlank() && userCollections.stream().noneMatch(def -> def.id().equals(event.collectionId()))) {
                    userCollections.add(new CollectionDefinition(event.collectionId(), event.name(), false));
                    loadoutsByCollection.putIfAbsent(event.collectionId(), List.of());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionRenamed event) {
                for (int index = 0; index < userCollections.size(); index++) {
                    CollectionDefinition existing = userCollections.get(index);
                    if (existing.id().equals(event.collectionId())) {
                        userCollections.set(index, new CollectionDefinition(existing.id(), event.name(), false));
                        break;
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionDeleted event) {
                userCollections.removeIf(definition -> definition.id().equals(event.collectionId()));
                loadoutsByCollection.remove(event.collectionId());
                memberships.replaceAll((identity, collectionIds) -> {
                    LinkedHashSet<String> updated = new LinkedHashSet<>(collectionIds);
                    updated.remove(event.collectionId());
                    return Set.copyOf(updated);
                });
                memberships.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionItemAdded event) {
                if (event.identity() != null && current.collectionIds().contains(event.collectionId())) {
                    LinkedHashSet<String> updated = new LinkedHashSet<>(memberships.getOrDefault(event.identity(), Set.of()));
                    updated.add(event.collectionId());
                    memberships.put(event.identity(), Set.copyOf(updated));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionItemRemoved event) {
                if (event.identity() != null) {
                    LinkedHashSet<String> updated = new LinkedHashSet<>(memberships.getOrDefault(event.identity(), Set.of()));
                    updated.remove(event.collectionId());
                    if (updated.isEmpty()) {
                        memberships.remove(event.identity());
                    } else {
                        memberships.put(event.identity(), Set.copyOf(updated));
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.LoadoutCreated event) {
                if (event.loadout() != null && current.collectionIds().contains(event.collectionId())) {
                    ArrayList<QuickAccessLoadoutDefinition> loadouts = new ArrayList<>(loadoutsByCollection.getOrDefault(event.collectionId(), List.of()));
                    loadouts.removeIf(existing -> existing.id().equals(event.loadout().id()));
                    loadouts.add(event.loadout());
                    loadoutsByCollection.put(event.collectionId(), List.copyOf(loadouts));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.LoadoutRenamed event) {
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
        else if (workflowEvent instanceof WorkflowEvent.LoadoutUpdated event) {
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
        else if (workflowEvent instanceof WorkflowEvent.LoadoutDeleted event) {
                ArrayList<QuickAccessLoadoutDefinition> loadouts = new ArrayList<>(loadoutsByCollection.getOrDefault(event.collectionId(), List.of()));
                loadouts.removeIf(loadout -> loadout.id().equals(event.loadoutId()));
                loadoutsByCollection.put(event.collectionId(), List.copyOf(loadouts));
            }
        else if (workflowEvent instanceof WorkflowEvent.FavoriteMarked event) {
                if (event.identity() != null) {
                    favorites.add(event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.FavoriteUnmarked event) {
                if (event.identity() != null) {
                    favorites.remove(event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.JunkMarked event) {
                if (event.identity() != null) {
                    junk.add(event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.JunkUnmarked event) {
                if (event.identity() != null) {
                    junk.remove(event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedIdentityMarked event) {
                if (event.identity() != null) {
                    protectedIdentities.add(event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedIdentityUnmarked event) {
                if (event.identity() != null) {
                    protectedIdentities.remove(event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedTargetMarked event) {
                if (event.target() != null) {
                    protectedTargets.add(event.target());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedTargetUnmarked event) {
                if (event.target() != null) {
                    protectedTargets.removeIf(candidate -> candidate.stableKey().equals(event.target().stableKey()));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.PortableContainerProtectionSet event) {
            protectPortableContainers = event.enabled();
        }
        else if (workflowEvent instanceof WorkflowEvent.RecentDismissedUpTo event) {
                if (event.identity() != null) {
                    recentDismissals.put(event.identity(), Math.max(
                            recentDismissals.getOrDefault(event.identity(), 0L),
                            event.dismissedUpToGlobalSequence()
                    ));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandCreated event) {
                if (event.island() != null) {
                    playerIslands.removeIf(island -> island.id().equals(event.island().id()));
                    playerIslands.add(event.island());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandMoved event) {
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
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandRenamed event) {
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
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandRecolored event) {
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
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandIconChanged event) {
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
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandDeleted event) {
                if (event.islandId() != null && !event.islandId().isBlank()) {
                    playerIslands.removeIf(island -> island.id().equals(event.islandId()));
                    visualHomes.entrySet().removeIf(entry ->
                            entry.getValue() != null && event.islandId().equals(entry.getValue().islandId()));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualIslandReordered event) {
                if (event.islandId() != null && !event.islandId().isBlank()) {
                    int currentIndex = -1;
                    for (int index = 0; index < playerIslands.size(); index++) {
                        if (playerIslands.get(index).id().equals(event.islandId())) {
                            currentIndex = index;
                            break;
                        }
                    }
                    if (currentIndex >= 0) {
                        VisualAtlasIsland moved = playerIslands.remove(currentIndex);
                        int target = Math.min(event.targetIndex(), playerIslands.size());
                        playerIslands.add(target, moved);
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualHomeAssigned event) {
                VisualHomeAssignment requested = event.assignment();
                if (requested != null && requested.identity() != null) {
                    applyVisualHomeAssignment(visualHomes, requested);
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.VisualHomeCleared event) {
                if (event.identity() != null) {
                    VisualHomeAssignment removed = visualHomes.remove(event.identity());
                    if (removed != null) {
                        compactOrdinalsAfterRemove(visualHomes, removed.islandId(), removed.ordinal());
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.TemplateIslandDismissed event) {
                if (event.templateId() != null && !event.templateId().isBlank()) {
                    dismissedTemplateIds.add(event.templateId());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestCreated event) {
                if (event.chest() != null) {
                    claimedChests.put(event.chest().storageId(), event.chest());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestMoved event) {
                if (event.storageId() != null) {
                    ClaimedChest existing = claimedChests.get(event.storageId());
                    if (existing != null) {
                        claimedChests.put(event.storageId(), existing.withAtlasPosition(event.atlasX(), event.atlasY()));
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestAnchorsChanged event) {
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
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestRelabeled event) {
                if (event.storageId() != null) {
                    ClaimedChest existing = claimedChests.get(event.storageId());
                    if (existing != null) {
                        claimedChests.put(event.storageId(), existing.withLabel(event.label()));
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestDeleted event) {
                if (event.storageId() != null) {
                    claimedChests.remove(event.storageId());
                    affinity.remove(event.storageId());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestDepositObserved event) {
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
        else if (workflowEvent instanceof WorkflowEvent.ChestAffinityForgotten event) {
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
        else if (workflowEvent instanceof WorkflowEvent.ChestAffinityCleared event) {
                if (event.storageId() != null) {
                    affinity.remove(event.storageId());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestClusterRelabeled event) {
                String clusterId = event.clusterId();
                if (clusterId != null && !clusterId.isBlank()) {
                    String label = event.label() == null ? "" : event.label().trim();
                    if (label.isBlank()) {
                        clusterLabels.remove(clusterId);
                    } else {
                        clusterLabels.put(clusterId, label);
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitCreated event) {
                if (event.kit() != null && !event.kit().id().isBlank()) {
                    kitMap = kitMap.withKit(event.kit());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitUpdated event) {
                if (event.kit() != null && !event.kit().id().isBlank() && kitMap.kit(event.kit().id()) != null) {
                    kitMap = kitMap.withKit(event.kit());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDeleted event) {
                if (!event.kitId().isBlank()) {
                    kitMap = kitMap.withoutKit(event.kitId());
                    // Drop any kit-scoped desired counts that referenced
                    // this kit. Without the cascade, a re-created kit with
                    // the same id would inherit stale counts from the
                    // deleted one.
                    kitDesiredCounts.remove(event.kitId());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitActivated event) {
                if (!event.kitId().isBlank() && kitMap.kit(event.kitId()) != null) {
                    kitMap = kitMap.withActivation(new KitActivation(event.kitId(), event.pageIndex()));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDeactivated event) {
                kitMap = kitMap.withActivation(KitActivation.NONE);
            }
        else if (workflowEvent instanceof WorkflowEvent.KitPageSwitched event) {
                KitActivation activeAssignment = kitMap.activation();
                if (activeAssignment.isActive()) {
                    KitDefinition activeKit = kitMap.kit(activeAssignment.kitId());
                    if (activeKit != null && event.pageIndex() < activeKit.pageCount()) {
                        kitMap = kitMap.withActivation(new KitActivation(activeAssignment.kitId(), event.pageIndex()));
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.PlayerDesiredCountSet event) {
                if (event.identity() != null) {
                    if (event.count() <= 0) {
                        playerDesiredCounts.remove(event.identity());
                    } else {
                        playerDesiredCounts.put(event.identity(), event.count());
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDesiredCountSet event) {
                if (event.identity() != null && !event.kitId().isBlank()) {
                    Map<ItemIdentity, Integer> existing = kitDesiredCounts.getOrDefault(event.kitId(), Map.of());
                    LinkedHashMap<ItemIdentity, Integer> updated = new LinkedHashMap<>(existing);
                    if (event.count() <= 0) {
                        updated.remove(event.identity());
                    } else {
                        updated.put(event.identity(), event.count());
                    }
                    if (updated.isEmpty()) {
                        kitDesiredCounts.remove(event.kitId());
                    } else {
                        kitDesiredCounts.put(event.kitId(), Map.copyOf(updated));
                    }
                }
            }

        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitDesiredCountsCopy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : kitDesiredCounts.entrySet()) {
            kitDesiredCountsCopy.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        return new Snapshot(
                userCollections,
                memberships,
                loadoutsByCollection,
                favorites,
                junk,
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                recentDismissals,
                new VisualHomeMap(playerIslands, visualHomes, dismissedTemplateIds),
                new ClaimedChestMap(new ArrayList<>(claimedChests.values())),
                new ChestAffinityMap(affinity),
                Map.copyOf(clusterLabels),
                kitMap,
                Map.copyOf(playerDesiredCounts),
                Map.copyOf(kitDesiredCountsCopy)
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
            Map<String, List<QuickAccessLoadoutDefinition>> loadoutsByCollection,
            Set<ItemIdentity> favoriteTags,
            Set<ItemIdentity> junkTags,
            ProtectionSnapshotPolicy protection,
            Map<ItemIdentity, Long> recentDismissedUpToByIdentity,
            VisualHomeMap visualHomeMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap chestAffinityMap,
            Map<String, String> clusterLabels,
            KitMap kitMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts
    ) {
        public Snapshot {
            userCollections = userCollections == null ? List.of() : List.copyOf(userCollections);
            memberships = CollectionProjection.copyMemberships(memberships);
            loadoutsByCollection = CollectionProjection.copyLoadouts(loadoutsByCollection);
            favoriteTags = favoriteTags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(favoriteTags));
            junkTags = junkTags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(junkTags));
            protection = protection == null ? new ProtectionSnapshotPolicy(Set.of(), Set.of(), false) : protection;
            recentDismissedUpToByIdentity = copyRecentDismissals(recentDismissedUpToByIdentity);
            visualHomeMap = visualHomeMap == null ? VisualHomeMap.empty() : visualHomeMap;
            claimedChestMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
            chestAffinityMap = chestAffinityMap == null ? ChestAffinityMap.empty() : chestAffinityMap;
            clusterLabels = clusterLabels == null ? Map.of() : Map.copyOf(clusterLabels);
            kitMap = kitMap == null ? KitMap.empty() : kitMap;
            playerDesiredCounts = playerDesiredCounts == null ? Map.of() : Map.copyOf(playerDesiredCounts);
            kitDesiredCounts = kitDesiredCounts == null ? Map.of() : Map.copyOf(kitDesiredCounts);
        }

        public static Snapshot empty() {
            return new Snapshot(
                    List.of(),
                    Map.of(),
                    Map.of(),
                    Set.of(),
                    Set.of(),
                    new ProtectionSnapshotPolicy(Set.of(), Set.of(), false),
                    Map.of(),
                    VisualHomeMap.empty(),
                    ClaimedChestMap.empty(),
                    ChestAffinityMap.empty(),
                    Map.of(),
                    KitMap.empty(),
                    Map.of(),
                    Map.of()
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
