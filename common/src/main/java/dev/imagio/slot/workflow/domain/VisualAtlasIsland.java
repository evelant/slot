package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * Authored island chrome record. Position ({@code x}, {@code y}) is the
 * top-left of the island in atlas world units and represents the
 * player's intent (drag-to-move). Size is not authored — it is derived
 * each render by the client-side packer
 * ({@code AtlasLayout.packIsland}) from the cell sizes of the items
 * homed to this island. The client then runs
 * {@code AtlasNudgeLayout} to produce the rendered position; the
 * authored {@code (x, y)} stored here is the player's home — the
 * stable target, not the rendered output.
 *
 * <p>See {@code docs/plans/atlas-nudge-layout.md}.
 */
public record VisualAtlasIsland(
        String id,
        String label,
        VisualAtlasIslandKind kind,
        double x,
        double y,
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
