package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record VisualHomeAssignment(
        ItemIdentity identity,
        String islandId,
        int localX,
        int localY,
        VisualHomeOrigin origin,
        boolean locked
) {
    public VisualHomeAssignment {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        if (islandId == null || islandId.isBlank()) {
            throw new IllegalArgumentException("islandId must not be blank");
        }
        islandId = islandId.trim();
        origin = origin == null ? VisualHomeOrigin.PLAYER_PLACED : origin;
    }
}
