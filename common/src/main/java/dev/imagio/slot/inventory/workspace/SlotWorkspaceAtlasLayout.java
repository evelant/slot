package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Authoritative atlas-layer constants and the server-side island
 * projection that builds the view-model {@code AtlasIsland} list.
 *
 * <p>Phase 2.2+ owns no item placement logic — that lives client-side
 * in {@code dev.imagio.slot.atlas.lod.AtlasLayout}. The freeform
 * coordinate helpers (placementForOrdinal, placementForDrop,
 * resolvePlacement, clampPlacement, LocalPlacement, Placement) were
 * removed when {@code VisualHomeAssignment} dropped {@code localX} /
 * {@code localY} in favour of an explicit {@code ordinal}. See
 * {@code docs/decisions/0005-relevance-score-and-layout-locality.md}.
 */
public final class SlotWorkspaceAtlasLayout {
    public static final int CANVAS_WIDTH = 2200;
    public static final int CANVAS_HEIGHT = 1480;
    public static final int CARD_WIDTH = 32;
    public static final int CARD_HEIGHT = 32;
    public static final int CARD_GAP = 4;
    public static final int CHEST_TILE_CELL = 16;
    public static final int CHEST_TILE_COLUMNS = 9;
    public static final int CHEST_TILE_HEADER_HEIGHT = 14;
    public static final int CHEST_TILE_PADDING = 4;
    public static final int STORAGE_ZONE_PADDING = 32;
    public static final String ISLAND_TRIAGE = "triage";
    public static final String ISLAND_MISC = "misc";
    public static final String ISLAND_MISC_LABEL = "Misc";
    public static final int ISLAND_MISC_COLOR = 0xCC6E5A4A;

    public static int chestTileWidth() {
        return CHEST_TILE_PADDING * 2 + CHEST_TILE_COLUMNS * CHEST_TILE_CELL;
    }

    public static int chestTileHeight(int filledStackCount) {
        int bounded = Math.max(0, filledStackCount);
        int rows = bounded == 0 ? 1 : (bounded - 1) / CHEST_TILE_COLUMNS + 1;
        return CHEST_TILE_HEADER_HEIGHT + rows * CHEST_TILE_CELL + CHEST_TILE_PADDING;
    }

    private static final int PLAYER_COLOR = 0xCC5A4A6E;
    public static final int ISLAND_CONTENT_PADDING_X = 8;
    public static final int ISLAND_CONTENT_PADDING_Y = 8;
    // With the carriedBadge and edit control now living above the island (on
    // the title-bar strip), the first row no longer has to leave room for
    // them inside the panel. 4 px of breathing room from the panel border is
    // enough.
    public static final int ISLAND_CONTENT_TOP = 4;
    /** Empty/single-card island width floor so the chrome reads as an island. */
    public static final int PLAYER_ISLAND_MIN_WIDTH = 96;
    /** Empty/single-card island height floor so the chrome reads as an island. */
    public static final int PLAYER_ISLAND_MIN_HEIGHT = 72;
    /**
     * World-unit ceiling on the island header strip. The header is
     * sized to keep its screen-pixel height roughly constant; without
     * a world-height cap, zooming out grows the header without bound
     * (at scale 0.2 it would otherwise be ~50 wu) and crashes into
     * neighbours above it.
     *
     * <p>Used in two places that must stay in sync:
     * <ul>
     *   <li>{@code IslandChestBuilder.applyHeaderScale} clamps
     *       {@code worldHeaderHeight} so it never exceeds this.</li>
     *   <li>{@code AtlasLayout.packAtlas} reserves this band above
     *       each island when probing for collisions, so de-overlap
     *       leaves room for the worst-case header.</li>
     * </ul>
     *
     * <p>Trade-off: at extreme zoom-out the header text gets small
     * in screen pixels (say ~5 px at scale 0.2 with this ceiling).
     * Players aren't reading labels at that zoom — they're seeing
     * the island constellation — so graceful LOD beats unbounded
     * growth.
     */
    public static final int ISLAND_HEADER_RESERVE = 24;

    private SlotWorkspaceAtlasLayout() {
    }

    public static List<SlotWorkspaceViewModel.AtlasIsland> baseIslands(VisualHomeMap visualHomeMap) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        if (visualHomeMap != null) {
            // List order is authoritative: WorkflowEvent.VisualIslandReordered
            // mutates this list and TOC drag-to-reorder writes through it.
            // Older y/x/label sorting was canvas-era dead weight.
            for (VisualAtlasIsland island : visualHomeMap.playerIslands()) {
                islands.add(new SlotWorkspaceViewModel.AtlasIsland(
                        island.id(),
                        island.label(),
                        island.kind(),
                        island.x(),
                        island.y(),
                        island.color(),
                        0
                ));
            }
        }
        return List.copyOf(islands);
    }

    public static SlotWorkspaceViewModel.AtlasIsland island(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId
    ) {
        if (islands == null || islandId == null || islandId.isBlank()) {
            return null;
        }
        return islands.stream()
                .filter(island -> island != null && island.islandId().equals(islandId))
                .findFirst()
                .orElse(null);
    }

    public static PlayerIslandDraft createNextPlayerIslandDraft(
            String label,
            ItemIdentity iconIdentity,
            VisualHomeMap visualHomeMap
    ) {
        int index = visualHomeMap == null ? 0 : visualHomeMap.playerIslands().size();
        int column = index % 2;
        int row = index / 2;
        return new PlayerIslandDraft(
                label,
                1144 + column * (PLAYER_ISLAND_MIN_WIDTH + 48),
                148 + row * (PLAYER_ISLAND_MIN_HEIGHT + 36),
                PLAYER_COLOR,
                iconIdentity
        );
    }

    public record PlayerIslandDraft(
            String label,
            int x,
            int y,
            int color,
            ItemIdentity iconIdentity
    ) {
    }
}
