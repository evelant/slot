package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedGridPackerTest {

    @Test
    void emptyInputReturnsEmpty() {
        assertTrue(WeightedGridPacker.pack(List.of(), 200, 8, 4, 4).isEmpty());
        assertTrue(WeightedGridPacker.pack(null, 200, 8, 4, 4).isEmpty());
    }

    @Test
    void singleRowOfUniformCells() {
        List<WeightedGridPacker.Cell> cells = uniform(4, 32, 32);
        List<WeightedGridPacker.Placement> placements = WeightedGridPacker.pack(cells, 200, 8, 4, 4);
        assertEquals(4, placements.size());
        for (int i = 0; i < placements.size(); i++) {
            WeightedGridPacker.Placement p = placements.get(i);
            assertEquals(8 + i * 36, p.localX(), "card " + i + " localX");
            assertEquals(4, p.localY());
            assertEquals(32, p.width());
            assertEquals(32, p.height());
        }
    }

    @Test
    void wrapsToNextRow() {
        // containerWidth=100, padding=8 → availableWidth=84 → 2 columns
        List<WeightedGridPacker.Cell> cells = uniform(5, 32, 32);
        List<WeightedGridPacker.Placement> placements = WeightedGridPacker.pack(cells, 100, 8, 4, 4);
        assertEquals(8, placements.get(0).localX());
        assertEquals(4, placements.get(0).localY());
        assertEquals(44, placements.get(1).localX());
        assertEquals(4, placements.get(1).localY());
        assertEquals(8, placements.get(2).localX());
        assertEquals(40, placements.get(2).localY());
        assertEquals(44, placements.get(3).localX());
        assertEquals(40, placements.get(3).localY());
        assertEquals(8, placements.get(4).localX());
        assertEquals(76, placements.get(4).localY());
    }

    @Test
    void heterogeneousCellsRowHeightIsMaxInRow() {
        List<WeightedGridPacker.Cell> cells = List.of(
                new WeightedGridPacker.Cell(32, 32),
                new WeightedGridPacker.Cell(32, 48),
                new WeightedGridPacker.Cell(32, 32),
                new WeightedGridPacker.Cell(32, 32)
        );
        List<WeightedGridPacker.Placement> placements = WeightedGridPacker.pack(cells, 100, 8, 4, 4);
        assertEquals(4, placements.get(0).localY());
        assertEquals(4, placements.get(1).localY());
        // Row 0's height is 48 (max), so row 1 starts at 4 + 48 + 4 = 56
        assertEquals(56, placements.get(2).localY());
        assertEquals(56, placements.get(3).localY());
    }

    @Test
    void variableWidthCellsPackTightly() {
        List<WeightedGridPacker.Cell> cells = List.of(
                new WeightedGridPacker.Cell(64, 32),
                new WeightedGridPacker.Cell(32, 32),
                new WeightedGridPacker.Cell(32, 32)
        );
        // contentRight = 200 - 8 = 192. Row 0: 8, 8+64+4=76, 76+32+4=112.
        // 112 + 32 + 4 = 148 ≤ 192 → fits.
        List<WeightedGridPacker.Placement> placements = WeightedGridPacker.pack(cells, 200, 8, 4, 4);
        assertEquals(8, placements.get(0).localX());
        assertEquals(76, placements.get(1).localX());
        assertEquals(112, placements.get(2).localX());
    }

    @Test
    void oversizedCellStillPlacesAtLeastOnePerRow() {
        List<WeightedGridPacker.Cell> cells = List.of(
                new WeightedGridPacker.Cell(500, 32),
                new WeightedGridPacker.Cell(500, 32)
        );
        List<WeightedGridPacker.Placement> placements = WeightedGridPacker.pack(cells, 100, 8, 4, 4);
        assertEquals(8, placements.get(0).localX());
        assertEquals(4, placements.get(0).localY());
        assertEquals(8, placements.get(1).localX());
        assertEquals(40, placements.get(1).localY());
    }

    /**
     * Phase 1 invariant: with all cells at the standard
     * {@code CARD_WIDTH × CARD_HEIGHT} and the same padding/gap as the
     * legacy server-side placement helper, the packer output must
     * match the simple {@code (ordinal % cols, ordinal / cols)}
     * grid math across a range of container widths. Regression guard
     * for uniform-cell layouts when relevance lift is zero.
     */
    @Test
    void uniformCellsMatchOrdinalGridAcrossWidths() {
        int padX = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X;
        int padTop = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP;
        int gap = SlotWorkspaceAtlasLayout.CARD_GAP;
        int cardW = SlotWorkspaceAtlasLayout.CARD_WIDTH;
        int cardH = SlotWorkspaceAtlasLayout.CARD_HEIGHT;
        for (int containerWidth : new int[]{96, 100, 104, 108, 116, 120, 123, 124, 144, 180, 240, 360, 540}) {
            List<WeightedGridPacker.Cell> cells = uniform(40, cardW, cardH);
            List<WeightedGridPacker.Placement> packerOutput = WeightedGridPacker.pack(
                    cells, containerWidth, padX, padTop, gap
            );

            int availableWidth = Math.max(cardW, containerWidth - padX * 2);
            int columns = Math.max(1, availableWidth / (cardW + gap));
            for (int ordinal = 0; ordinal < cells.size(); ordinal++) {
                int col = ordinal % columns;
                int row = ordinal / columns;
                int expectedX = padX + col * (cardW + gap);
                int expectedY = padTop + row * (cardH + gap);
                WeightedGridPacker.Placement packed = packerOutput.get(ordinal);
                assertEquals(expectedX, packed.localX(),
                        "containerWidth=" + containerWidth + " ordinal=" + ordinal + " localX");
                assertEquals(expectedY, packed.localY(),
                        "containerWidth=" + containerWidth + " ordinal=" + ordinal + " localY");
            }
        }
    }

    private static List<WeightedGridPacker.Cell> uniform(int count, int width, int height) {
        ArrayList<WeightedGridPacker.Cell> cells = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cells.add(new WeightedGridPacker.Cell(width, height));
        }
        return Collections.unmodifiableList(cells);
    }
}
