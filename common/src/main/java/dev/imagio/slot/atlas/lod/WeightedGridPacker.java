package dev.imagio.slot.atlas.lod;

import java.util.ArrayList;
import java.util.List;

/**
 * Packs items in canonical order into a heterogeneous-cell grid.
 * Each row's height is the max cell height in that row; each cell's
 * width is its own. Wrapping happens when the next cell would exceed
 * the container's content width.
 *
 * <p>Phase 1 ships with all cells the same size (binary weights), so
 * output matches the existing uniform-cell
 * {@code SlotWorkspaceAtlasLayout.placementForOrdinal} ladder
 * exactly. Phase 2 produces variable cell sizes per band.
 */
public final class WeightedGridPacker {
    private WeightedGridPacker() {
    }

    public record Cell(int width, int height) {
        public Cell {
            width = Math.max(1, width);
            height = Math.max(1, height);
        }
    }

    public record Placement(int localX, int localY, int width, int height) {
    }

    /**
     * @param cellsInOrder cells to place, in the order the caller wants them packed
     * @param containerWidth island width in world units
     * @param paddingX horizontal inset on both sides of the container
     * @param contentTop vertical inset at the top of the container
     * @param gap spacing between adjacent cells (both axes)
     * @return one {@link Placement} per input cell, in the same order
     */
    public static List<Placement> pack(
            List<Cell> cellsInOrder,
            int containerWidth,
            int paddingX,
            int contentTop,
            int gap
    ) {
        if (cellsInOrder == null || cellsInOrder.isEmpty()) {
            return List.of();
        }
        int safePadding = Math.max(0, paddingX);
        int safeTop = Math.max(0, contentTop);
        int safeGap = Math.max(0, gap);
        int contentRight = Math.max(safePadding, containerWidth - safePadding);

        ArrayList<Placement> placements = new ArrayList<>(cellsInOrder.size());
        int cursorX = safePadding;
        int rowTop = safeTop;
        int rowHeight = 0;
        boolean rowHasContent = false;

        for (Cell cell : cellsInOrder) {
            Cell resolved = cell == null ? new Cell(1, 1) : cell;
            // Reserve a trailing gap on the wrap check so uniform-cell
            // packing matches the existing
            // SlotWorkspaceAtlasLayout.placementForOrdinal columns
            // calculation (which divides by cardWidth + gap, treating
            // each card as occupying width + gap of horizontal real
            // estate).
            int cellRightWithGap = cursorX + resolved.width() + safeGap;
            if (rowHasContent && cellRightWithGap > contentRight) {
                rowTop += rowHeight + safeGap;
                cursorX = safePadding;
                rowHeight = 0;
                rowHasContent = false;
            }
            placements.add(new Placement(cursorX, rowTop, resolved.width(), resolved.height()));
            cursorX += resolved.width() + safeGap;
            rowHeight = Math.max(rowHeight, resolved.height());
            rowHasContent = true;
        }
        return List.copyOf(placements);
    }
}
