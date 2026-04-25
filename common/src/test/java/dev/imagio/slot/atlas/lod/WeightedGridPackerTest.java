package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
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
     * Phase 1 invariant: with all cells at the existing
     * {@code CARD_WIDTH × CARD_HEIGHT} and the same padding/gap as
     * {@link SlotWorkspaceAtlasLayout#placementForOrdinal}, the packer
     * output must match {@code placementForOrdinal} for every ordinal
     * across a range of container widths. Regression guard for the
     * carried-only Phase 1 layout.
     */
    @Test
    void uniformCellsMatchPlacementForOrdinalAcrossWidths() {
        for (int containerWidth : new int[]{96, 100, 104, 108, 116, 120, 123, 124, 144, 180, 240, 360, 540}) {
            List<WeightedGridPacker.Cell> cells = uniform(40, SlotWorkspaceAtlasLayout.CARD_WIDTH, SlotWorkspaceAtlasLayout.CARD_HEIGHT);
            List<WeightedGridPacker.Placement> packerOutput = WeightedGridPacker.pack(
                    cells,
                    containerWidth,
                    SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X,
                    SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP,
                    SlotWorkspaceAtlasLayout.CARD_GAP
            );

            List<SlotWorkspaceViewModel.AtlasIsland> islands = List.of(synthIsland("test", containerWidth, 800));
            for (int ordinal = 0; ordinal < cells.size(); ordinal++) {
                SlotWorkspaceAtlasLayout.Placement reference =
                        SlotWorkspaceAtlasLayout.placementForOrdinal(islands, "test", ordinal);
                WeightedGridPacker.Placement packed = packerOutput.get(ordinal);
                assertEquals(reference.localX(), packed.localX(),
                        "containerWidth=" + containerWidth + " ordinal=" + ordinal + " localX");
                assertEquals(reference.localY(), packed.localY(),
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

    private static SlotWorkspaceViewModel.AtlasIsland synthIsland(String id, int width, int height) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                id,
                "Test",
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                width,
                height,
                0xFF000000,
                0
        );
    }
}
