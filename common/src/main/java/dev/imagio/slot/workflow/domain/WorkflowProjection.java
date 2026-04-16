package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                                    existing.width(),
                                    existing.height(),
                                    existing.color(),
                                    existing.iconIdentity()
                            ));
                            break;
                        }
                    }
                }
            }
            case WorkflowEvent.VisualHomeAssigned event -> {
                if (event.assignment() != null && event.assignment().identity() != null) {
                    visualHomes.put(event.assignment().identity(), event.assignment());
                }
            }
            case WorkflowEvent.VisualHomeCleared event -> {
                if (event.identity() != null) {
                    visualHomes.remove(event.identity());
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
                new VisualHomeMap(playerIslands, visualHomes)
        );
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
            VisualHomeMap visualHomeMap
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
                    VisualHomeMap.empty()
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
