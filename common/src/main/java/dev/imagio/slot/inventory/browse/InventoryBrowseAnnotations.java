package dev.imagio.slot.inventory.browse;

import java.util.LinkedHashSet;
import java.util.Set;

public record InventoryBrowseAnnotations(
        boolean favorite,
        boolean junk,
        boolean recent,
        Set<String> collectionIds,
        int desiredCount
) {
    public InventoryBrowseAnnotations {
        collectionIds = collectionIds == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(collectionIds));
        desiredCount = Math.max(0, desiredCount);
    }

    public static InventoryBrowseAnnotations empty() {
        return new InventoryBrowseAnnotations(false, false, false, Set.of(), 0);
    }
}
