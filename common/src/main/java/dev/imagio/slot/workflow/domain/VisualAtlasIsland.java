package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record VisualAtlasIsland(
        String id,
        String label,
        VisualAtlasIslandKind kind,
        int x,
        int y,
        int width,
        int height,
        int color,
        ItemIdentity iconIdentity
) {
    public VisualAtlasIsland {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        id = id.trim();
        label = label == null || label.isBlank() ? id : label.trim();
        kind = kind == null ? VisualAtlasIslandKind.PLAYER : kind;
        width = Math.max(96, width);
        height = Math.max(72, height);
    }
}
