package dev.imagio.slot.client.collection;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.model.ItemIdentitySupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CollectionStore {
    public static final String FAVORITES_ID = "favorites";
    public static final String JUNK_ID = "junk";

    private final Map<String, CollectionDefinition> collections = new LinkedHashMap<>();
    private final Map<ItemIdentity, Set<String>> memberships = new HashMap<>();
    private final Map<String, Map<ItemIdentity, Integer>> desiredCountsByCollection = new LinkedHashMap<>();
    private final Map<String, List<HotbarLoadoutDefinition>> hotbarLoadouts = new LinkedHashMap<>();
    private Runnable changeListener = () -> {
    };

    public CollectionStore() {
        resetCollections();
    }

    public CollectionDefinition createCollection(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Collection name must not be blank");
        }

        String baseId = slugify(normalizedName);
        String candidate = baseId;
        int counter = 2;
        while (collections.containsKey(candidate)) {
            candidate = baseId + "-" + counter++;
        }

        CollectionDefinition definition = new CollectionDefinition(candidate, normalizedName, false, CollectionDisplayMode.OWNED_ONLY);
        collections.put(candidate, definition);
        desiredCountsByCollection.put(candidate, new LinkedHashMap<>());
        hotbarLoadouts.put(candidate, List.of());
        notifyChanged();
        return definition;
    }

    public void renameCollection(String id, String newName) {
        CollectionDefinition existing = requireUserCollection(id);
        String normalizedName = newName == null ? "" : newName.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Collection name must not be blank");
        }

        collections.put(id, new CollectionDefinition(existing.id(), normalizedName, false, existing.displayMode()));
        notifyChanged();
    }

    public void deleteCollection(String id) {
        requireUserCollection(id);
        collections.remove(id);
        desiredCountsByCollection.remove(id);
        hotbarLoadouts.remove(id);

        for (Set<String> membership : memberships.values()) {
            membership.remove(id);
        }
        memberships.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        notifyChanged();
    }

    public void addToCollection(String collectionId, ItemIdentity itemIdentity) {
        if (!collections.containsKey(collectionId)) {
            throw new IllegalArgumentException("Unknown collection: " + collectionId);
        }
        itemIdentity = trackedIdentity(itemIdentity);

        if (memberships.computeIfAbsent(itemIdentity, ignored -> new HashSet<>()).add(collectionId)) {
            desiredCountsByCollection
                    .computeIfAbsent(collectionId, ignored -> new LinkedHashMap<>())
                    .putIfAbsent(itemIdentity, 1);
            notifyChanged();
        }
    }

    public void removeFromCollection(String collectionId, ItemIdentity itemIdentity) {
        itemIdentity = trackedIdentity(itemIdentity);
        Set<String> membership = memberships.get(itemIdentity);
        if (membership == null) {
            return;
        }

        membership.remove(collectionId);
        Map<ItemIdentity, Integer> desiredCounts = desiredCountsByCollection.get(collectionId);
        if (desiredCounts != null) {
            desiredCounts.remove(itemIdentity);
        }
        if (membership.isEmpty()) {
            memberships.remove(itemIdentity);
        }
        notifyChanged();
    }

    public void setFavorite(ItemIdentity itemIdentity, boolean favorite) {
        if (favorite) {
            addToCollection(FAVORITES_ID, itemIdentity);
        } else {
            removeFromCollection(FAVORITES_ID, itemIdentity);
        }
    }

    public boolean isFavorite(ItemIdentity itemIdentity) {
        return memberships.getOrDefault(trackedIdentity(itemIdentity), Set.of()).contains(FAVORITES_ID);
    }

    public boolean isJunk(ItemIdentity itemIdentity) {
        return memberships.getOrDefault(trackedIdentity(itemIdentity), Set.of()).contains(JUNK_ID);
    }

    public Set<String> collectionsFor(ItemIdentity itemIdentity) {
        return Set.copyOf(memberships.getOrDefault(trackedIdentity(itemIdentity), Set.of()));
    }

    public List<CollectionItemTarget> trackedItems(String collectionId) {
        requireCollection(collectionId);
        List<CollectionItemTarget> items = new ArrayList<>();
        Map<ItemIdentity, Integer> desiredCounts = desiredCountsByCollection.getOrDefault(collectionId, Map.of());
        for (Map.Entry<ItemIdentity, Set<String>> entry : memberships.entrySet()) {
            if (!entry.getValue().contains(collectionId)) {
                continue;
            }
            items.add(new CollectionItemTarget(entry.getKey(), Math.max(1, desiredCounts.getOrDefault(entry.getKey(), 1))));
        }
        items.sort(Comparator.comparing(item -> item.identity().itemId(), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(items);
    }

    public int desiredCount(String collectionId, ItemIdentity itemIdentity) {
        requireCollection(collectionId);
        itemIdentity = trackedIdentity(itemIdentity);
        if (!collectionsFor(itemIdentity).contains(collectionId)) {
            return 0;
        }
        return Math.max(1, desiredCountsByCollection.getOrDefault(collectionId, Map.of()).getOrDefault(itemIdentity, 1));
    }

    public void setDesiredCount(String collectionId, ItemIdentity itemIdentity, int desiredCount) {
        requireCollection(collectionId);
        itemIdentity = trackedIdentity(itemIdentity);
        if (!collectionsFor(itemIdentity).contains(collectionId)) {
            throw new IllegalArgumentException("Item is not tracked by collection: " + collectionId);
        }
        int normalizedDesiredCount = Math.max(1, desiredCount);
        Map<ItemIdentity, Integer> desiredCounts = desiredCountsByCollection.computeIfAbsent(collectionId, ignored -> new LinkedHashMap<>());
        Integer previous = desiredCounts.put(itemIdentity, normalizedDesiredCount);
        if (!Objects.equals(previous, normalizedDesiredCount)) {
            notifyChanged();
        }
    }

    public CollectionDefinition favorites() {
        return collections.get(FAVORITES_ID);
    }

    public CollectionDefinition junk() {
        return collections.get(JUNK_ID);
    }

    public CollectionDefinition collectionOrNull(String id) {
        return id == null ? null : collections.get(id);
    }

    public boolean isUserCollection(String id) {
        CollectionDefinition definition = collectionOrNull(id);
        return definition != null && !definition.builtIn();
    }

    public List<CollectionDefinition> userCollections() {
        List<CollectionDefinition> definitions = new ArrayList<>();
        for (CollectionDefinition definition : collections.values()) {
            if (!definition.builtIn()) {
                definitions.add(definition);
            }
        }

        definitions.sort(Comparator.comparing(CollectionDefinition::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(definitions);
    }

    public List<HotbarLoadoutDefinition> loadoutsFor(String collectionId) {
        requireCollection(collectionId);
        return hotbarLoadouts.getOrDefault(collectionId, List.of());
    }

    public HotbarLoadoutDefinition loadout(String collectionId, String loadoutId) {
        requireCollection(collectionId);
        return hotbarLoadouts.getOrDefault(collectionId, List.of()).stream()
                .filter(loadout -> loadout.id().equals(loadoutId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown hotbar loadout: " + loadoutId));
    }

    public HotbarLoadoutDefinition createHotbarLoadout(String collectionId, String name, List<HotbarLoadoutSlot> slots) {
        return createHotbarLoadout(collectionId, name, slots, null);
    }

    public HotbarLoadoutDefinition createHotbarLoadout(
            String collectionId,
            String name,
            List<HotbarLoadoutSlot> slots,
            ItemIdentity offhandIdentity
    ) {
        requireCollection(collectionId);
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Loadout name must not be blank");
        }
        if ((slots == null || slots.isEmpty()) && offhandIdentity == null) {
            throw new IllegalArgumentException("Loadout must contain at least one quick access item");
        }

        List<HotbarLoadoutDefinition> existingLoadouts = new ArrayList<>(hotbarLoadouts.getOrDefault(collectionId, List.of()));
        String baseId = slugify(normalizedName);
        String candidate = baseId;
        int counter = 2;
        while (containsLoadoutId(existingLoadouts, candidate)) {
            candidate = baseId + "-" + counter++;
        }

        HotbarLoadoutDefinition loadout = new HotbarLoadoutDefinition(candidate, normalizedName, null, slots, offhandIdentity);
        existingLoadouts.add(loadout);
        hotbarLoadouts.put(collectionId, List.copyOf(existingLoadouts));
        notifyChanged();
        return loadout;
    }

    public void renameHotbarLoadout(String collectionId, String loadoutId, String newName) {
        requireCollection(collectionId);
        String normalizedName = newName == null ? "" : newName.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Loadout name must not be blank");
        }

        List<HotbarLoadoutDefinition> existingLoadouts = new ArrayList<>(hotbarLoadouts.getOrDefault(collectionId, List.of()));
        boolean updated = false;
        for (int index = 0; index < existingLoadouts.size(); index++) {
            HotbarLoadoutDefinition existing = existingLoadouts.get(index);
            if (!existing.id().equals(loadoutId)) {
                continue;
            }

            existingLoadouts.set(index, new HotbarLoadoutDefinition(existing.id(), normalizedName, existing.hotkeySlot(), existing.slots(), existing.offhandIdentity()));
            updated = true;
            break;
        }

        if (!updated) {
            throw new IllegalArgumentException("Unknown hotbar loadout: " + loadoutId);
        }

        hotbarLoadouts.put(collectionId, List.copyOf(existingLoadouts));
        notifyChanged();
    }

    public void updateHotbarLoadout(String collectionId, String loadoutId, List<HotbarLoadoutSlot> slots) {
        updateHotbarLoadout(collectionId, loadoutId, slots, null);
    }

    public void updateHotbarLoadout(
            String collectionId,
            String loadoutId,
            List<HotbarLoadoutSlot> slots,
            ItemIdentity offhandIdentity
    ) {
        requireCollection(collectionId);
        if ((slots == null || slots.isEmpty()) && offhandIdentity == null) {
            throw new IllegalArgumentException("Loadout must contain at least one quick access item");
        }

        List<HotbarLoadoutDefinition> existingLoadouts = new ArrayList<>(hotbarLoadouts.getOrDefault(collectionId, List.of()));
        boolean updated = false;
        for (int index = 0; index < existingLoadouts.size(); index++) {
            HotbarLoadoutDefinition existing = existingLoadouts.get(index);
            if (!existing.id().equals(loadoutId)) {
                continue;
            }

            existingLoadouts.set(index, new HotbarLoadoutDefinition(existing.id(), existing.name(), existing.hotkeySlot(), slots, offhandIdentity));
            updated = true;
            break;
        }

        if (!updated) {
            throw new IllegalArgumentException("Unknown hotbar loadout: " + loadoutId);
        }

        hotbarLoadouts.put(collectionId, List.copyOf(existingLoadouts));
        notifyChanged();
    }

    public void setHotbarLoadoutHotkey(String collectionId, String loadoutId, Integer hotkeySlot) {
        requireCollection(collectionId);
        loadout(collectionId, loadoutId);
        Integer normalizedHotkey = hotkeySlot == null || hotkeySlot < 0 || hotkeySlot > 8 ? null : hotkeySlot;
        for (Map.Entry<String, List<HotbarLoadoutDefinition>> entry : hotbarLoadouts.entrySet()) {
            List<HotbarLoadoutDefinition> existingLoadouts = new ArrayList<>(entry.getValue());
            boolean collectionChanged = false;
            for (int index = 0; index < existingLoadouts.size(); index++) {
                HotbarLoadoutDefinition existing = existingLoadouts.get(index);
                boolean isTarget = entry.getKey().equals(collectionId) && existing.id().equals(loadoutId);
                Integer nextHotkey = existing.hotkeySlot();

                if (normalizedHotkey != null && normalizedHotkey.equals(existing.hotkeySlot()) && !isTarget) {
                    nextHotkey = null;
                }
                if (isTarget) {
                    nextHotkey = normalizedHotkey;
                }

                if (!Objects.equals(nextHotkey, existing.hotkeySlot())) {
                    existingLoadouts.set(index, new HotbarLoadoutDefinition(existing.id(), existing.name(), nextHotkey, existing.slots(), existing.offhandIdentity()));
                    collectionChanged = true;
                }
            }

            if (collectionChanged) {
                entry.setValue(List.copyOf(existingLoadouts));
            }
        }
        notifyChanged();
    }

    public void deleteHotbarLoadout(String collectionId, String loadoutId) {
        requireCollection(collectionId);
        List<HotbarLoadoutDefinition> existingLoadouts = new ArrayList<>(hotbarLoadouts.getOrDefault(collectionId, List.of()));
        boolean removed = existingLoadouts.removeIf(loadout -> loadout.id().equals(loadoutId));
        if (!removed) {
            throw new IllegalArgumentException("Unknown hotbar loadout: " + loadoutId);
        }

        hotbarLoadouts.put(collectionId, List.copyOf(existingLoadouts));
        notifyChanged();
    }

    public HotbarLoadoutDefinition loadoutForHotkey(int hotkeySlot) {
        if (hotkeySlot < 0 || hotkeySlot > 8) {
            return null;
        }

        for (CollectionDefinition definition : allCollections()) {
            for (HotbarLoadoutDefinition loadout : hotbarLoadouts.getOrDefault(definition.id(), List.of())) {
                if (Objects.equals(loadout.hotkeySlot(), hotkeySlot)) {
                    return loadout;
                }
            }
        }
        return null;
    }

    public Collection<CollectionDefinition> allCollections() {
        List<CollectionDefinition> definitions = new ArrayList<>();
        definitions.add(favorites());
        definitions.add(junk());
        definitions.addAll(userCollections());
        return List.copyOf(definitions);
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = Objects.requireNonNullElse(changeListener, () -> {
        });
    }

    public Snapshot snapshot() {
        Map<ItemIdentity, Set<String>> membershipSnapshot = new LinkedHashMap<>();
        memberships.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().itemId()))
                .forEach(entry -> membershipSnapshot.put(entry.getKey(), Set.copyOf(entry.getValue())));

        Map<String, List<HotbarLoadoutDefinition>> loadoutSnapshot = new LinkedHashMap<>();
        for (CollectionDefinition definition : allCollections()) {
            List<HotbarLoadoutDefinition> loadouts = hotbarLoadouts.getOrDefault(definition.id(), List.of());
            if (!loadouts.isEmpty()) {
                loadoutSnapshot.put(definition.id(), List.copyOf(loadouts));
            }
        }

        Map<String, Map<ItemIdentity, Integer>> desiredCountSnapshot = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : desiredCountsByCollection.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            desiredCountSnapshot.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }

        return new Snapshot(userCollections(), membershipSnapshot, loadoutSnapshot, desiredCountSnapshot);
    }

    public void replaceWith(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");

        resetCollections();
        for (CollectionDefinition definition : snapshot.userCollections()) {
            if (definition == null || definition.builtIn()) {
                continue;
            }

            String id = definition.id() == null || definition.id().isBlank() ? slugify(definition.name()) : definition.id().trim();
            String name = definition.name() == null ? "" : definition.name().trim();
            if (name.isEmpty() || collections.containsKey(id)) {
                continue;
            }

            collections.put(id, new CollectionDefinition(id, name, false, definition.displayMode() == null ? CollectionDisplayMode.OWNED_ONLY : definition.displayMode()));
            desiredCountsByCollection.put(id, new LinkedHashMap<>());
            hotbarLoadouts.put(id, List.of());
        }

        memberships.clear();
        for (Map.Entry<ItemIdentity, Set<String>> entry : snapshot.memberships().entrySet()) {
            ItemIdentity identity = trackedIdentity(entry.getKey());
            if (identity == null) {
                continue;
            }

            Set<String> resolvedMembership = entry.getValue().stream()
                    .filter(collectionId -> collections.containsKey(collectionId))
                    .collect(HashSet::new, Set::add, Set::addAll);
            if (!resolvedMembership.isEmpty()) {
                memberships.computeIfAbsent(identity, ignored -> new HashSet<>()).addAll(resolvedMembership);
            }
        }

        for (Map.Entry<String, List<HotbarLoadoutDefinition>> entry : snapshot.loadoutsByCollection().entrySet()) {
            if (!collections.containsKey(entry.getKey())) {
                continue;
            }

            Map<String, HotbarLoadoutDefinition> resolvedLoadouts = new LinkedHashMap<>();
            for (HotbarLoadoutDefinition loadout : entry.getValue()) {
                if (loadout == null || loadout.id() == null || loadout.name() == null) {
                    continue;
                }
                resolvedLoadouts.put(loadout.id(), loadout);
            }
            hotbarLoadouts.put(entry.getKey(), List.copyOf(resolvedLoadouts.values()));
        }

        for (Map.Entry<String, Map<ItemIdentity, Integer>> entry : snapshot.desiredCountsByCollection().entrySet()) {
            if (!collections.containsKey(entry.getKey())) {
                continue;
            }
            Map<ItemIdentity, Integer> desiredCounts = desiredCountsByCollection.computeIfAbsent(entry.getKey(), ignored -> new LinkedHashMap<>());
            for (Map.Entry<ItemIdentity, Integer> desiredEntry : entry.getValue().entrySet()) {
                ItemIdentity identity = trackedIdentity(desiredEntry.getKey());
                if (identity == null || !memberships.getOrDefault(identity, Set.of()).contains(entry.getKey())) {
                    continue;
                }
                desiredCounts.merge(identity, Math.max(1, desiredEntry.getValue()), Math::max);
            }
        }

        notifyChanged();
    }

    public record Snapshot(
            List<CollectionDefinition> userCollections,
            Map<ItemIdentity, Set<String>> memberships,
            Map<String, List<HotbarLoadoutDefinition>> loadoutsByCollection,
            Map<String, Map<ItemIdentity, Integer>> desiredCountsByCollection
    ) {
        public Snapshot {
            userCollections = userCollections == null ? List.of() : List.copyOf(userCollections);
            memberships = memberships == null ? Map.of() : Map.copyOf(memberships);
            loadoutsByCollection = loadoutsByCollection == null ? Map.of() : Map.copyOf(loadoutsByCollection);
            desiredCountsByCollection = desiredCountsByCollection == null ? Map.of() : Map.copyOf(desiredCountsByCollection);
        }
    }

    public record CollectionItemTarget(ItemIdentity identity, int desiredCount) {
        public CollectionItemTarget {
            desiredCount = Math.max(1, desiredCount);
        }
    }

    private CollectionDefinition requireCollection(String id) {
        CollectionDefinition definition = collections.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown collection: " + id);
        }
        return definition;
    }

    private CollectionDefinition requireUserCollection(String id) {
        CollectionDefinition definition = requireCollection(id);
        if (definition.builtIn()) {
            throw new IllegalArgumentException("Built-in collection cannot be modified: " + id);
        }
        return definition;
    }

    private static boolean containsLoadoutId(List<HotbarLoadoutDefinition> loadouts, String loadoutId) {
        return loadouts.stream().anyMatch(loadout -> loadout.id().equals(loadoutId));
    }

    private static String slugify(String value) {
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return slug.isEmpty() ? "collection" : slug;
    }

    private static ItemIdentity trackedIdentity(ItemIdentity itemIdentity) {
        return ItemIdentitySupport.normalizeTrackedIdentity(itemIdentity);
    }

    private void resetCollections() {
        collections.clear();
        memberships.clear();
        desiredCountsByCollection.clear();
        hotbarLoadouts.clear();
        collections.put(FAVORITES_ID, new CollectionDefinition(FAVORITES_ID, "Favorites", true, CollectionDisplayMode.OWNED_ONLY));
        desiredCountsByCollection.put(FAVORITES_ID, new LinkedHashMap<>());
        hotbarLoadouts.put(FAVORITES_ID, List.of());
        collections.put(JUNK_ID, new CollectionDefinition(JUNK_ID, "Junk", true, CollectionDisplayMode.OWNED_ONLY));
        desiredCountsByCollection.put(JUNK_ID, new LinkedHashMap<>());
        hotbarLoadouts.put(JUNK_ID, List.of());
    }

    private void notifyChanged() {
        changeListener.run();
    }
}
