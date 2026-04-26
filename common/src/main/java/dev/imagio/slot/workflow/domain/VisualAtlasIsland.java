package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Authored island chrome record. Position ({@code x}, {@code y}) is the
 * top-left of the island in atlas world coordinates and is player-driven
 * (drag-to-move). Size is no longer authored — the client-side
 * {@code AtlasLayout} packer computes per-island bounds from cell sizes
 * and the auto-square wrap target every refresh. See
 * {@code docs/decisions/0005-relevance-score-and-layout-locality.md}.
 */
public record VisualAtlasIsland(
        String id,
        String label,
        VisualAtlasIslandKind kind,
        int x,
        int y,
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
    }
}
