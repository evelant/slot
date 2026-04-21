package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public static int chestTileWidth() {
        return CHEST_TILE_PADDING * 2 + CHEST_TILE_COLUMNS * CHEST_TILE_CELL;
    }

    public static int chestTileHeight(int filledStackCount) {
        int bounded = Math.max(0, filledStackCount);
        int rows = bounded == 0 ? 1 : (bounded - 1) / CHEST_TILE_COLUMNS + 1;
        return CHEST_TILE_HEADER_HEIGHT + rows * CHEST_TILE_CELL + CHEST_TILE_PADDING;
    }
    private static final int CANVAS_MARGIN = 24;

    private static final int TRIAGE_COLOR = 0xCC2D4455;
    private static final int PLAYER_COLOR = 0xCC5A4A6E;
    public static final int ISLAND_CONTENT_PADDING_X = 8;
    public static final int ISLAND_CONTENT_PADDING_Y = 8;
    public static final int ISLAND_CONTENT_TOP = 10;
    private static final int TRIAGE_MIN_WIDTH = 420;
    private static final int TRIAGE_MIN_HEIGHT = 260;
    public static final int PLAYER_ISLAND_MIN_WIDTH = 180;
    public static final int PLAYER_ISLAND_MIN_HEIGHT = 120;
    private static final int ISLAND_TRAILING_BUFFER_X = CARD_WIDTH + CARD_GAP;
    private static final int ISLAND_TRAILING_BUFFER_Y = CARD_HEIGHT + CARD_GAP;

    private SlotWorkspaceAtlasLayout() {
    }

    public static List<SlotWorkspaceViewModel.AtlasIsland> baseIslands(VisualHomeMap visualHomeMap) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        if (visualHomeMap != null) {
            visualHomeMap.playerIslands().stream()
                    .sorted(Comparator
                            .comparingInt(VisualAtlasIsland::y)
                            .thenComparingInt(VisualAtlasIsland::x)
                            .thenComparing(VisualAtlasIsland::label, String.CASE_INSENSITIVE_ORDER))
                    .forEach(island -> islands.add(new SlotWorkspaceViewModel.AtlasIsland(
                            island.id(),
                            island.label(),
                            island.kind(),
                            island.x(),
                            island.y(),
                            Math.max(PLAYER_ISLAND_MIN_WIDTH, island.width()),
                            Math.max(PLAYER_ISLAND_MIN_HEIGHT, island.height()),
                            island.color(),
                            0
                    )));
        }
        return List.copyOf(islands);
    }

    public static List<SlotWorkspaceViewModel.AtlasIsland> fittedIslands(
            List<SlotWorkspaceViewModel.AtlasIsland> baseIslands,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems
    ) {
        List<SlotWorkspaceViewModel.AtlasIsland> resolvedIslands = baseIslands == null ? List.of() : baseIslands;
        List<SlotWorkspaceViewModel.AtlasItem> resolvedItems = atlasItems == null ? List.of() : atlasItems;
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> fitted = new ArrayList<>(resolvedIslands.size());
        for (SlotWorkspaceViewModel.AtlasIsland island : resolvedIslands) {
            if (island == null) {
                continue;
            }
            fitted.add(fitIsland(island, resolvedItems));
        }
        return List.copyOf(fitted);
    }

    public static Placement placementForOrdinal(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            int ordinal
    ) {
        SlotWorkspaceViewModel.AtlasIsland island = resolvedIsland(islands, islandId);
        if (island == null) {
            return new Placement(ISLAND_TRIAGE, ISLAND_CONTENT_PADDING_X, ISLAND_CONTENT_TOP, 0, 0);
        }
        int availableWidth = Math.max(CARD_WIDTH, island.width() - ISLAND_CONTENT_PADDING_X * 2);
        int columns = Math.max(1, availableWidth / (CARD_WIDTH + CARD_GAP));
        int index = Math.max(0, ordinal);
        int column = index % columns;
        int row = index / columns;
        return clampPlacement(islands, islandId, new LocalPlacement(
                ISLAND_CONTENT_PADDING_X + column * (CARD_WIDTH + CARD_GAP),
                ISLAND_CONTENT_TOP + row * (CARD_HEIGHT + CARD_GAP)
        ));
    }

    public static Placement placementForDrop(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            int worldX,
            int worldY
    ) {
        SlotWorkspaceViewModel.AtlasIsland island = resolvedIsland(islands, islandId);
        if (island == null) {
            return new Placement(ISLAND_TRIAGE, ISLAND_CONTENT_PADDING_X, ISLAND_CONTENT_TOP, 0, 0);
        }
        int requestedX = worldX - island.x() - CARD_WIDTH / 2;
        int requestedY = worldY - island.y() - CARD_HEIGHT / 2;
        int localX = snapToGrid(requestedX, ISLAND_CONTENT_PADDING_X, CARD_WIDTH + CARD_GAP);
        int localY = snapToGrid(requestedY, ISLAND_CONTENT_TOP, CARD_HEIGHT + CARD_GAP);
        return new Placement(
                island.islandId(),
                localX,
                localY,
                island.x() + localX,
                island.y() + localY
        );
    }

    private static int snapToGrid(int value, int origin, int step) {
        if (step <= 0) {
            return Math.max(origin, value);
        }
        int offset = value - origin;
        int cells = Math.max(0, Math.round(offset / (float) step));
        return origin + cells * step;
    }

    public static Placement clampPlacement(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            LocalPlacement placement
    ) {
        SlotWorkspaceViewModel.AtlasIsland island = resolvedIsland(islands, islandId);
        if (island == null) {
            return new Placement(ISLAND_TRIAGE, ISLAND_CONTENT_PADDING_X, ISLAND_CONTENT_TOP, 0, 0);
        }

        LocalPlacement requested = placement == null ? new LocalPlacement(
                ISLAND_CONTENT_PADDING_X,
                ISLAND_CONTENT_TOP
        ) : placement;
        int minX = ISLAND_CONTENT_PADDING_X;
        int maxX = Math.max(minX, island.width() - ISLAND_CONTENT_PADDING_X - CARD_WIDTH);
        int minY = ISLAND_CONTENT_TOP;
        int maxY = Math.max(minY, island.height() - ISLAND_CONTENT_PADDING_Y - CARD_HEIGHT);
        int localX = Math.max(minX, Math.min(maxX, requested.x()));
        int localY = Math.max(minY, Math.min(maxY, requested.y()));
        return new Placement(
                island.islandId(),
                localX,
                localY,
                island.x() + localX,
                island.y() + localY
        );
    }

    public static Placement resolvePlacement(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            int localX,
            int localY
    ) {
        SlotWorkspaceViewModel.AtlasIsland island = resolvedIsland(islands, islandId);
        if (island == null) {
            return new Placement(ISLAND_TRIAGE, ISLAND_CONTENT_PADDING_X, ISLAND_CONTENT_TOP, 0, 0);
        }
        int clampedX = Math.max(ISLAND_CONTENT_PADDING_X, localX);
        int clampedY = Math.max(ISLAND_CONTENT_TOP, localY);
        return new Placement(
                island.islandId(),
                clampedX,
                clampedY,
                island.x() + clampedX,
                island.y() + clampedY
        );
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

    private static SlotWorkspaceViewModel.AtlasIsland resolvedIsland(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId
    ) {
        return island(islands, islandId);
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
                PLAYER_ISLAND_MIN_WIDTH,
                PLAYER_ISLAND_MIN_HEIGHT,
                PLAYER_COLOR,
                iconIdentity
        );
    }

    private static SlotWorkspaceViewModel.AtlasIsland fitIsland(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems
    ) {
        int minWidth = minIslandWidth(island.kind());
        int minHeight = minIslandHeight(island.kind());
        int maxRight = island.x() + ISLAND_CONTENT_PADDING_X;
        int maxBottom = island.y() + ISLAND_CONTENT_TOP;
        int itemCount = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : atlasItems) {
            if (item == null || !island.islandId().equals(item.islandId())) {
                continue;
            }
            itemCount++;
            maxRight = Math.max(maxRight, item.x() + item.width());
            maxBottom = Math.max(maxBottom, item.y() + item.height());
        }

        int fittedWidth = itemCount == 0
                ? minWidth
                : maxRight - island.x() + ISLAND_CONTENT_PADDING_X + ISLAND_TRAILING_BUFFER_X;
        int fittedHeight = itemCount == 0
                ? minHeight
                : maxBottom - island.y() + ISLAND_CONTENT_PADDING_Y + ISLAND_TRAILING_BUFFER_Y;

        return new SlotWorkspaceViewModel.AtlasIsland(
                island.islandId(),
                island.label(),
                island.kind(),
                island.x(),
                island.y(),
                Math.max(minWidth, fittedWidth),
                Math.max(minHeight, fittedHeight),
                island.color(),
                itemCount
        );
    }

    private static int minIslandWidth(VisualAtlasIslandKind kind) {
        return switch (kind == null ? VisualAtlasIslandKind.PLAYER : kind) {
            case TRIAGE -> TRIAGE_MIN_WIDTH;
            case PLAYER -> PLAYER_ISLAND_MIN_WIDTH;
        };
    }

    private static int minIslandHeight(VisualAtlasIslandKind kind) {
        return switch (kind == null ? VisualAtlasIslandKind.PLAYER : kind) {
            case TRIAGE -> TRIAGE_MIN_HEIGHT;
            case PLAYER -> PLAYER_ISLAND_MIN_HEIGHT;
        };
    }

    public record Placement(
            String islandId,
            int localX,
            int localY,
            int x,
            int y
    ) {
    }

    public record LocalPlacement(
            int x,
            int y
    ) {
    }

    public record PlayerIslandDraft(
            String label,
            int x,
            int y,
            int width,
            int height,
            int color,
            ItemIdentity iconIdentity
    ) {
    }
}
