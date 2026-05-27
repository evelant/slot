package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

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
        LinkedHashMap<ItemIdentity, Long> junkMarkedAt = new LinkedHashMap<>(current.junkMarkedAtEpochMillis());
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
        LinkedHashMap<ItemIdentity, Integer> playerWantedCounts = new LinkedHashMap<>(current.playerWantedCounts());
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitWantedCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : current.kitWantedCounts().entrySet()) {
            kitWantedCounts.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
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
                    ItemIdentity identity = ItemIdentityCollections.key(event.identity());
                    LinkedHashSet<String> updated = new LinkedHashSet<>(ItemIdentityCollections.findOrDefault(
                            memberships,
                            identity,
                            Set.of()));
                    updated.add(event.collectionId());
                    memberships.put(identity, Set.copyOf(updated));
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.CollectionItemRemoved event) {
                if (event.identity() != null) {
                    ItemIdentity identity = ItemIdentityCollections.key(event.identity());
                    LinkedHashSet<String> updated = new LinkedHashSet<>(ItemIdentityCollections.findOrDefault(
                            memberships,
                            identity,
                            Set.of()));
                    updated.remove(event.collectionId());
                    ItemIdentityCollections.removeMatching(memberships, identity);
                    if (updated.isEmpty()) {
                        memberships.remove(identity);
                    } else {
                        memberships.put(identity, Set.copyOf(updated));
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
                    ItemIdentityCollections.add(favorites, event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.FavoriteUnmarked event) {
                if (event.identity() != null) {
                    ItemIdentityCollections.removeMatching(favorites, event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.JunkMarked event) {
                if (event.identity() != null && record.envelope().occurredAtEpochMillis() > 0L) {
                    ItemIdentity identity = ItemIdentityCollections.key(event.identity());
                    ItemIdentityCollections.add(junk, identity);
                    ItemIdentityCollections.removeMatching(junkMarkedAt, identity);
                    junkMarkedAt.put(identity, record.envelope().occurredAtEpochMillis());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.JunkUnmarked event) {
                if (event.identity() != null) {
                    ItemIdentityCollections.removeMatching(junk, event.identity());
                    ItemIdentityCollections.removeMatching(junkMarkedAt, event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedIdentityMarked event) {
                if (event.identity() != null) {
                    ItemIdentityCollections.add(protectedIdentities, event.identity());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ProtectedIdentityUnmarked event) {
                if (event.identity() != null) {
                    ItemIdentityCollections.removeMatching(protectedIdentities, event.identity());
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
                    ItemIdentity identity = ItemIdentityCollections.key(event.identity());
                    recentDismissals.put(identity, Math.max(
                            ItemIdentityCollections.findOrDefault(recentDismissals, identity, 0L),
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
                    VisualHomeAssignment removed = ItemIdentityCollections.find(visualHomes, event.identity());
                    ItemIdentityCollections.removeMatching(visualHomes, event.identity());
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
        else if (workflowEvent instanceof WorkflowEvent.ClaimedChestRoleChanged event) {
                if (event.storageId() != null) {
                    ClaimedChest existing = claimedChests.get(event.storageId());
                    if (existing != null) {
                        claimedChests.put(event.storageId(), existing.withRole(event.role()));
                        if (!event.role().learnsAffinity()) {
                            affinity.remove(event.storageId());
                        }
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
                        && claimedChests.containsKey(event.storageId())
                        && claimedChests.get(event.storageId()).role().learnsAffinity()) {
                    affinity = copyAffinity(new ChestAffinityMap(affinity)
                            .recordDeposit(event.storageId(), event.identity(), event.tick())
                            .entries());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.ChestAffinityForgotten event) {
                if (event.storageId() != null && event.identity() != null) {
                    affinity = copyAffinity(new ChestAffinityMap(affinity)
                            .forget(event.storageId(), event.identity())
                            .entries());
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
        else if (workflowEvent instanceof WorkflowEvent.KitReordered event) {
                if (!event.kitId().isBlank()) {
                    kitMap = kitMap.withKitReordered(event.kitId(), event.targetIndex());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDeleted event) {
                if (!event.kitId().isBlank()) {
                    Set<String> removedKitIds = kitMap.idsRemovedByDeleting(event.kitId());
                    kitMap = kitMap.withoutKit(event.kitId());
                    // Drop any kit-scoped desired counts that referenced
                    // this kit. Without the cascade, a re-created kit with
                    // the same id would inherit stale counts from the
                    // deleted one.
                    for (String removedKitId : removedKitIds) {
                        kitDesiredCounts.remove(removedKitId);
                        kitWantedCounts.remove(removedKitId);
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitActivated event) {
                if (!event.kitId().isBlank() && kitMap.kit(event.kitId()) != null) {
                    kitMap = kitMap.withActivation(new KitActivation(
                            event.kitId(),
                            event.pageIndex(),
                            event.putAwayIdentities()));
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
                        kitMap = kitMap.withActivation(new KitActivation(
                                activeAssignment.kitId(),
                                event.pageIndex(),
                                activeAssignment.putAwayIdentities()));
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.PlayerDesiredCountSet event) {
                if (event.identity() != null) {
                    ItemIdentityCollections.putOrClear(playerDesiredCounts, event.identity(), event.count());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitDesiredCountSet event) {
                if (event.identity() != null && !event.kitId().isBlank()) {
                    Map<ItemIdentity, Integer> existing = kitDesiredCounts.getOrDefault(event.kitId(), Map.of());
                    LinkedHashMap<ItemIdentity, Integer> updated = new LinkedHashMap<>(existing);
                    ItemIdentityCollections.putOrClear(updated, event.identity(), event.count());
                    if (updated.isEmpty()) {
                        kitDesiredCounts.remove(event.kitId());
                    } else {
                        kitDesiredCounts.put(event.kitId(), Map.copyOf(updated));
                    }
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.PlayerWantedCountSet event) {
                if (event.identity() != null) {
                    ItemIdentityCollections.putOrClear(playerWantedCounts, event.identity(), event.count());
                }
            }
        else if (workflowEvent instanceof WorkflowEvent.KitWantedCountSet event) {
                if (event.identity() != null && !event.kitId().isBlank()) {
                    Map<ItemIdentity, Integer> existing = kitWantedCounts.getOrDefault(event.kitId(), Map.of());
                    LinkedHashMap<ItemIdentity, Integer> updated = new LinkedHashMap<>(existing);
                    ItemIdentityCollections.putOrClear(updated, event.identity(), event.count());
                    if (updated.isEmpty()) {
                        kitWantedCounts.remove(event.kitId());
                    } else {
                        kitWantedCounts.put(event.kitId(), Map.copyOf(updated));
                    }
                }
            }
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitDesiredCountsCopy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : kitDesiredCounts.entrySet()) {
            kitDesiredCountsCopy.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        LinkedHashMap<String, Map<ItemIdentity, Integer>> kitWantedCountsCopy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : kitWantedCounts.entrySet()) {
            kitWantedCountsCopy.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        return new Snapshot(
                userCollections,
                memberships,
                loadoutsByCollection,
                favorites,
                junk,
                junkMarkedAt,
                new ProtectionSnapshotPolicy(protectedIdentities, protectedTargets, protectPortableContainers),
                recentDismissals,
                new VisualHomeMap(playerIslands, visualHomes, dismissedTemplateIds),
                new ClaimedChestMap(new ArrayList<>(claimedChests.values())),
                new ChestAffinityMap(affinity),
                Map.copyOf(clusterLabels),
                kitMap,
                Map.copyOf(playerDesiredCounts),
                Map.copyOf(kitDesiredCountsCopy),
                Map.copyOf(playerWantedCounts),
                Map.copyOf(kitWantedCountsCopy)
        );
    }

    static void applyVisualHomeAssignment(
            LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments,
            VisualHomeAssignment requested
    ) {
        ItemIdentity identity = ItemIdentityCollections.key(requested.identity());
        String dstIslandId = requested.islandId();
        int dstOrdinal = Math.max(0, requested.ordinal());

        VisualHomeAssignment previous = ItemIdentityCollections.find(assignments, identity);
        ItemIdentityCollections.removeMatching(assignments, identity);
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
            Map<ItemIdentity, Long> junkMarkedAtEpochMillis,
            ProtectionSnapshotPolicy protection,
            Map<ItemIdentity, Long> recentDismissedUpToByIdentity,
            VisualHomeMap visualHomeMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap chestAffinityMap,
            Map<String, String> clusterLabels,
            KitMap kitMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts,
            Map<String, Map<ItemIdentity, Integer>> kitWantedCounts
    ) {
        public Snapshot(
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
                Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
                Map<ItemIdentity, Integer> playerWantedCounts
        ) {
            this(
                    userCollections,
                    memberships,
                    loadoutsByCollection,
                    favoriteTags,
                    junkTags,
                    Map.of(),
                    protection,
                    recentDismissedUpToByIdentity,
                    visualHomeMap,
                    claimedChestMap,
                    chestAffinityMap,
                    clusterLabels,
                    kitMap,
                    playerDesiredCounts,
                    kitDesiredCounts,
                    playerWantedCounts,
                    Map.of()
            );
        }

        public Snapshot {
            userCollections = userCollections == null ? List.of() : List.copyOf(userCollections);
            memberships = CollectionProjection.copyMemberships(memberships);
            loadoutsByCollection = CollectionProjection.copyLoadouts(loadoutsByCollection);
            favoriteTags = ItemIdentityCollections.normalizedSet(favoriteTags);
            Set<ItemIdentity> normalizedJunkTags = ItemIdentityCollections.normalizedSet(junkTags);
            Map<ItemIdentity, Long> normalizedJunkMarkedAt = copyJunkMarkedAt(
                    normalizedJunkTags,
                    junkMarkedAtEpochMillis);
            junkTags = junkTagsWithTimestamps(normalizedJunkTags, normalizedJunkMarkedAt);
            junkMarkedAtEpochMillis = copyJunkMarkedAt(junkTags, normalizedJunkMarkedAt);
            protection = protection == null ? new ProtectionSnapshotPolicy(Set.of(), Set.of(), false) : protection;
            recentDismissedUpToByIdentity = copyRecentDismissals(recentDismissedUpToByIdentity);
            visualHomeMap = visualHomeMap == null ? VisualHomeMap.empty() : visualHomeMap;
            claimedChestMap = claimedChestMap == null ? ClaimedChestMap.empty() : claimedChestMap;
            chestAffinityMap = chestAffinityMap == null ? ChestAffinityMap.empty() : chestAffinityMap;
            clusterLabels = clusterLabels == null ? Map.of() : Map.copyOf(clusterLabels);
            kitMap = kitMap == null ? KitMap.empty() : kitMap;
            playerDesiredCounts = playerDesiredCounts == null ? Map.of() : Map.copyOf(playerDesiredCounts);
            kitDesiredCounts = kitDesiredCounts == null ? Map.of() : Map.copyOf(kitDesiredCounts);
            playerWantedCounts = playerWantedCounts == null ? Map.of() : Map.copyOf(playerWantedCounts);
            kitWantedCounts = kitWantedCounts == null ? Map.of() : Map.copyOf(kitWantedCounts);
        }

        public static Snapshot empty() {
            return new Snapshot(
                    List.of(),
                    Map.of(),
                    Map.of(),
                    Set.of(),
                    Set.of(),
                    Map.of(),
                    new ProtectionSnapshotPolicy(Set.of(), Set.of(), false),
                    Map.of(),
                    VisualHomeMap.empty(),
                    ClaimedChestMap.empty(),
                    ChestAffinityMap.empty(),
                    Map.of(),
                    KitMap.empty(),
                    Map.of(),
                    Map.of(),
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
                    copied.merge(ItemIdentityCollections.key(identity), value, Math::max);
                }
            });
            return Map.copyOf(copied);
        }

        private static Map<ItemIdentity, Long> copyJunkMarkedAt(
                Set<ItemIdentity> junkTags,
                Map<ItemIdentity, Long> source
        ) {
            if (junkTags == null || junkTags.isEmpty() || source == null || source.isEmpty()) {
                return Map.of();
            }
            LinkedHashMap<ItemIdentity, Long> copied = new LinkedHashMap<>();
            source.forEach((identity, value) -> {
                if (identity != null
                        && value != null
                        && value > 0L
                        && ItemIdentityCollections.containsCanonical(junkTags, identity)) {
                    copied.merge(ItemIdentityCollections.key(identity), value, Math::max);
                }
            });
            return copied.isEmpty() ? Map.of() : Map.copyOf(copied);
        }

        private static Set<ItemIdentity> junkTagsWithTimestamps(
                Set<ItemIdentity> junkTags,
                Map<ItemIdentity, Long> junkMarkedAtEpochMillis
        ) {
            if (junkTags == null || junkTags.isEmpty()
                    || junkMarkedAtEpochMillis == null || junkMarkedAtEpochMillis.isEmpty()) {
                return Set.of();
            }
            LinkedHashSet<ItemIdentity> retained = new LinkedHashSet<>();
            for (ItemIdentity identity : junkTags) {
                if (identity != null && ItemIdentityCollections.findCanonical(junkMarkedAtEpochMillis, identity) != null) {
                    ItemIdentityCollections.add(retained, identity);
                }
            }
            return retained.isEmpty() ? Set.of() : Set.copyOf(retained);
        }

    }
}
