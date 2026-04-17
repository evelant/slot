package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class SlotWorkspaceAtlasLayout {
    static final int CANVAS_WIDTH = 2200;
    static final int CANVAS_HEIGHT = 1480;
    static final int CARD_WIDTH = 32;
    static final int CARD_HEIGHT = 32;
    static final int CARD_GAP = 4;
    static final String ISLAND_TRIAGE = "triage";
    private static final int CANVAS_MARGIN = 24;

    private static final int TRIAGE_COLOR = 0xCC2D4455;
    private static final int PLAYER_COLOR = 0xCC5A4A6E;
    static final int ISLAND_CONTENT_PADDING_X = 14;
    static final int ISLAND_CONTENT_PADDING_Y = 14;
    static final int ISLAND_CONTENT_TOP = 56;
    private static final int TRIAGE_MIN_WIDTH = 420;
    private static final int TRIAGE_MIN_HEIGHT = 260;
    static final int PLAYER_ISLAND_MIN_WIDTH = 260;
    static final int PLAYER_ISLAND_MIN_HEIGHT = 180;
    private static final int ISLAND_TRAILING_BUFFER_X = CARD_WIDTH + CARD_GAP;
    private static final int ISLAND_TRAILING_BUFFER_Y = CARD_HEIGHT + CARD_GAP;

    private SlotWorkspaceAtlasLayout() {
    }

    static List<SlotWorkspaceViewModel.AtlasIsland> baseIslands(VisualHomeMap visualHomeMap) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        islands.add(new SlotWorkspaceViewModel.AtlasIsland(
                ISLAND_TRIAGE,
                "Triage / Inbox",
                VisualAtlasIslandKind.TRIAGE,
                84,
                148,
                TRIAGE_MIN_WIDTH,
                TRIAGE_MIN_HEIGHT,
                TRIAGE_COLOR,
                0
        ));
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
                            PLAYER_ISLAND_MIN_WIDTH,
                            PLAYER_ISLAND_MIN_HEIGHT,
                            island.color(),
                            0
                    )));
        }
        return List.copyOf(islands);
    }

    static List<SlotWorkspaceViewModel.AtlasIsland> fittedIslands(
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

    static Placement placementForOrdinal(
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

    static Placement placementForDrop(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            int worldX,
            int worldY
    ) {
        SlotWorkspaceViewModel.AtlasIsland island = resolvedIsland(islands, islandId);
        if (island == null) {
            return new Placement(ISLAND_TRIAGE, ISLAND_CONTENT_PADDING_X, ISLAND_CONTENT_TOP, 0, 0);
        }
        return clampPlacement(islands, islandId, new LocalPlacement(
                worldX - island.x() - CARD_WIDTH / 2,
                worldY - island.y() - CARD_HEIGHT / 2
        ));
    }

    static Placement clampPlacement(
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

    static Placement resolvePlacement(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            int localX,
            int localY
    ) {
        return clampPlacement(islands, islandId, new LocalPlacement(localX, localY));
    }

    static IslandOrigin clampIslandOrigin(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            String islandId,
            int requestedX,
            int requestedY
    ) {
        SlotWorkspaceViewModel.AtlasIsland island = island(islands, islandId);
        if (island == null) {
            return new IslandOrigin(CANVAS_MARGIN, CANVAS_MARGIN);
        }
        int minX = CANVAS_MARGIN;
        int maxX = Math.max(minX, CANVAS_WIDTH - island.width() - CANVAS_MARGIN);
        int minY = CANVAS_MARGIN;
        int maxY = Math.max(minY, CANVAS_HEIGHT - island.height() - CANVAS_MARGIN);
        return new IslandOrigin(
                Math.max(minX, Math.min(maxX, requestedX)),
                Math.max(minY, Math.min(maxY, requestedY))
        );
    }

    static SlotWorkspaceViewModel.AtlasIsland island(
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
        SlotWorkspaceViewModel.AtlasIsland island = island(islands, islandId);
        return island != null ? island : island(islands, ISLAND_TRIAGE);
    }

    static PlayerIslandDraft createNextPlayerIslandDraft(
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
        int maxWidth = Math.max(minWidth, CANVAS_WIDTH - island.x() - CANVAS_MARGIN);
        int maxHeight = Math.max(minHeight, CANVAS_HEIGHT - island.y() - CANVAS_MARGIN);

        return new SlotWorkspaceViewModel.AtlasIsland(
                island.islandId(),
                island.label(),
                island.kind(),
                island.x(),
                island.y(),
                Math.max(minWidth, Math.min(maxWidth, fittedWidth)),
                Math.max(minHeight, Math.min(maxHeight, fittedHeight)),
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

    record Placement(
            String islandId,
            int localX,
            int localY,
            int x,
            int y
    ) {
    }

    record LocalPlacement(
            int x,
            int y
    ) {
    }

    record IslandOrigin(
            int x,
            int y
    ) {
    }

    record PlayerIslandDraft(
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
