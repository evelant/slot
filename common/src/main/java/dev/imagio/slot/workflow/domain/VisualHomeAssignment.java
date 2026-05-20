package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

public record VisualHomeAssignment(
        ItemIdentity identity,
        String islandId,
        int ordinal,
        VisualHomeOrigin origin,
        boolean locked
) {
    public VisualHomeAssignment {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        identity = ItemIdentityCollections.key(identity);
        if (islandId == null || islandId.isBlank()) {
            throw new IllegalArgumentException("islandId must not be blank");
        }
        islandId = islandId.trim();
        ordinal = Math.max(0, ordinal);
        origin = origin == null ? VisualHomeOrigin.PLAYER_PLACED : origin;
    }
}
