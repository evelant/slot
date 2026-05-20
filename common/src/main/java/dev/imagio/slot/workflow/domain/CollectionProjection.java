package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CollectionProjection(
        List<CollectionDefinition> userCollections,
        Map<ItemIdentity, Set<String>> memberships,
        Map<String, List<QuickAccessLoadoutDefinition>> loadoutsByCollection,
        Set<ItemIdentity> favoriteTags,
        Set<ItemIdentity> junkTags
) {
    public CollectionProjection {
        userCollections = userCollections == null ? List.of() : List.copyOf(userCollections);
        memberships = copyMemberships(memberships);
        loadoutsByCollection = copyLoadouts(loadoutsByCollection);
        favoriteTags = ItemIdentityCollections.normalizedSet(favoriteTags);
        junkTags = ItemIdentityCollections.normalizedSet(junkTags);
    }

    public static CollectionProjection empty() {
        return new CollectionProjection(List.of(), Map.of(), Map.of(), Set.of(), Set.of());
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

    public static Map<ItemIdentity, Set<String>> copyMemberships(Map<ItemIdentity, Set<String>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Set<String>> copied = new LinkedHashMap<>();
        source.forEach((identity, collections) -> {
            if (identity != null) {
                copied.put(ItemIdentityCollections.key(identity), collections == null ? Set.of() : Set.copyOf(collections));
            }
        });
        return Map.copyOf(copied);
    }

    public static Map<String, List<QuickAccessLoadoutDefinition>> copyLoadouts(Map<String, List<QuickAccessLoadoutDefinition>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, List<QuickAccessLoadoutDefinition>> copied = new LinkedHashMap<>();
        source.forEach((collectionId, loadouts) -> copied.put(
                collectionId == null ? "" : collectionId,
                loadouts == null ? List.of() : List.copyOf(loadouts)
        ));
        return Map.copyOf(copied);
    }
}
