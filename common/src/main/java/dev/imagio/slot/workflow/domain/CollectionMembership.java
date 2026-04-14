package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.Set;

public record CollectionMembership(
        ItemIdentity identity,
        Set<String> collectionIds
) {
    public CollectionMembership {
        collectionIds = collectionIds == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(collectionIds));
    }
}
