package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record TrackedCollectionItem(
        ItemIdentity identity,
        int desiredCount
) {
    public TrackedCollectionItem {
        desiredCount = Math.max(1, desiredCount);
    }
}
