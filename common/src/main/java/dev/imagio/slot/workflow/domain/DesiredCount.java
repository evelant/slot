package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record DesiredCount(
        String collectionId,
        ItemIdentity identity,
        int count
) {
    public DesiredCount {
        collectionId = collectionId == null ? "" : collectionId;
        count = Math.max(1, count);
    }
}
