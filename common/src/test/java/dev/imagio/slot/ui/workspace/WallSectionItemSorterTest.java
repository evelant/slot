package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallSectionItemSorterTest {

    @AfterEach
    void resetFacetIndex() {
        FacetIndexHolder.reset();
    }

    @Test
    void sectionSplitsCarriedBandBeforeGhostBandAndFacetSortsWithinEachBand() {
        FacetIndexHolder.install(FacetIndex.load(new StringReader(facetLayer())));
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem carriedBlack = item("minecraft:black_wool", true);
        SlotWorkspaceViewModel.AtlasItem carriedWhite = item("minecraft:white_wool", true);
        SlotWorkspaceViewModel.AtlasItem ghostGray = item("minecraft:gray_wool", false);
        SlotWorkspaceViewModel.AtlasIsland island = new SlotWorkspaceViewModel.AtlasIsland(
                "building",
                "Building",
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                0xFFFFFFFF,
                3);

        SlotUiElement section = builder.section(
                island,
                List.of(ghostGray, carriedBlack, carriedWhite),
                3,
                false,
                StorageGhostRevealMode.PROXIMATE,
                false,
                false);

        assertEquals(3, section.children().size());
        SlotUiElement carriedGrid = section.children().get(1);
        SlotUiElement ghostGrid = section.children().get(2);
        assertTrue(carriedGrid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID));
        assertTrue(ghostGrid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID));
        List<?> carried = carriedGrid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
        List<?> ghosts = ghostGrid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class);
        assertEquals(List.of(carriedWhite, carriedBlack), carried);
        assertEquals(List.of(ghostGray), ghosts);
        assertSame(island, carriedGrid.attachment(
                WorkspaceUiAttachments.ATLAS_ISLAND,
                SlotWorkspaceViewModel.AtlasIsland.class));
        assertSame(island, ghostGrid.attachment(
                WorkspaceUiAttachments.ATLAS_ISLAND,
                SlotWorkspaceViewModel.AtlasIsland.class));
    }

    @Test
    void ghostsWithDesiredCountsSortBeforeOtherGhosts() {
        SlotWorkspaceViewModel.AtlasItem ordinaryGhost = item("minecraft:apple", false, 0);
        SlotWorkspaceViewModel.AtlasItem desiredGhost = item("minecraft:zinc_ingot", false, 12);

        WallSectionItemSorter.Groups groups = WallSectionItemSorter.groupAndSort(List.of(
                ordinaryGhost,
                desiredGhost
        ));

        assertEquals(List.of(desiredGhost, ordinaryGhost), groups.ghosts());
    }

    @Test
    void ghostsWithWantedCountsSortBeforeOtherGhosts() {
        SlotWorkspaceViewModel.AtlasItem ordinaryGhost = item("minecraft:apple", false, 0, 0);
        SlotWorkspaceViewModel.AtlasItem wantedGhost = item("minecraft:zinc_ingot", false, 0, 1);

        WallSectionItemSorter.Groups groups = WallSectionItemSorter.groupAndSort(List.of(
                ordinaryGhost,
                wantedGhost
        ));

        assertEquals(List.of(wantedGhost, ordinaryGhost), groups.ghosts());
    }

    @Test
    void sectionHidesOrdinaryProximateGhostsBehindNearbyChipByDefault() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem carriedStone = item("minecraft:stone", true);
        SlotWorkspaceViewModel.AtlasItem ghostTorch = item("minecraft:torch", false);
        SlotWorkspaceViewModel.AtlasIsland island = island();

        SlotUiElement section = builder.section(
                island,
                List.of(carriedStone, ghostTorch),
                2,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertEquals(2, section.children().size());
        SlotUiElement carriedGrid = section.children().get(1);
        assertEquals(List.of(carriedStone), carriedGrid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
        assertEquals(1, carriedGrid.attachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT, Integer.class));
        assertEquals(Boolean.FALSE, carriedGrid.attachment(
                WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_EXPANDED,
                Boolean.class));
    }

    @Test
    void collapsedTrackedOnlyGhostsDoNotInflateHeaderCounts() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem carriedStone = item("minecraft:stone", true);
        SlotWorkspaceViewModel.AtlasItem remoteGhost = item(
                "minecraft:redstone",
                false,
                0,
                0,
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 12)));
        SlotWorkspaceViewModel.AtlasIsland island = island(1);

        SlotUiElement section = builder.section(
                island,
                List.of(carriedStone, remoteGhost),
                2,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertEquals(List.of(carriedStone), section.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));
        assertEquals(List.of("Building", "1/1\u25CF"), descendantText(section.children().get(0)));
    }

    @Test
    void expandedSectionRevealsProximateGhostsAndKeepsCollapseChip() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem carriedStone = item("minecraft:stone", true);
        SlotWorkspaceViewModel.AtlasItem ghostTorch = item("minecraft:torch", false);
        SlotWorkspaceViewModel.AtlasIsland island = island();

        SlotUiElement section = builder.section(
                island,
                List.of(carriedStone, ghostTorch),
                2,
                false,
                StorageGhostRevealMode.COLLAPSED,
                true,
                false);

        assertEquals(3, section.children().size());
        SlotUiElement ghostGrid = section.children().get(2);
        assertEquals(List.of(ghostTorch), ghostGrid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
        assertEquals(Boolean.TRUE, ghostGrid.attachment(
                WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_EXPANDED,
                Boolean.class));
    }

    @Test
    void nearbyChipUsesCompactGhostCardLabel() {
        SlotUiElement chip = WallSectionUiBuilder.nearbyChip(island(), 12, false);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX, chip.layout().width());
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, chip.layout().height());
        assertEquals(List.of("+12"), descendantText(chip));
    }

    @Test
    void filteringRevealsMatchingGhostsWithoutNearbyChip() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem ghostTorch = item("minecraft:torch", false);

        SlotUiElement section = builder.section(
                island(),
                List.of(ghostTorch),
                2,
                true,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        SlotUiElement ghostGrid = section.children().get(1);
        assertEquals(List.of(ghostTorch), ghostGrid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
        assertTrue(!ghostGrid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT));
    }

    @Test
    void desiredGhostsRevealWithoutManualExpansion() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem desiredGhost = item("minecraft:torch", false, 16);

        SlotUiElement section = builder.section(
                island(),
                List.of(desiredGhost),
                1,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertEquals(List.of(desiredGhost), section.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));
        assertTrue(!section.children().get(1).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT));
    }

    @Test
    void wantedGhostsRevealWithoutManualExpansion() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem wantedGhost = item("minecraft:torch", false, 0, 1);

        SlotUiElement section = builder.section(
                island(),
                List.of(wantedGhost),
                1,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertEquals(List.of(wantedGhost), section.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));
        assertTrue(!section.children().get(1).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT));
    }

    @Test
    void forceRevealShowsGoalGhostsWithoutNearbyChip() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem ordinaryGhost = item("minecraft:torch", false);

        SlotUiElement section = builder.section(
                island(),
                List.of(ordinaryGhost),
                1,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                true);

        assertEquals(List.of(ordinaryGhost), section.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));
        assertTrue(!section.children().get(1).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT));
    }

    @Test
    void trackedXrayRevealsElsewhereOnlyGhosts() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem remoteGhost = item(
                "minecraft:redstone",
                false,
                0,
                0,
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 12)));

        SlotUiElement collapsed = builder.section(
                island(),
                List.of(remoteGhost),
                1,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);
        assertEquals(List.of(), collapsed.children().get(1).attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));

        SlotUiElement xray = builder.section(
                island(),
                List.of(remoteGhost),
                1,
                false,
                StorageGhostRevealMode.TRACKED,
                false,
                false);
        assertEquals(List.of(remoteGhost), xray.children().get(1).attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
    }

    private static SlotWorkspaceViewModel.AtlasItem item(String itemId, boolean carried) {
        return item(itemId, carried, 0);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(String itemId, boolean carried, int desiredCount) {
        return item(itemId, carried, desiredCount, 0);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            boolean carried,
            int desiredCount,
            int wantedCount
    ) {
        return item(itemId, carried, desiredCount, wantedCount, carried ? 0 : 8, List.of());
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            boolean carried,
            int desiredCount,
            int proximateCount,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        return item(itemId, carried, desiredCount, 0, proximateCount, elsewhere);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            boolean carried,
            int desiredCount,
            int wantedCount,
            int proximateCount,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                itemId,
                ItemComparisonMode.ITEM_ID.name(),
                "");
        return new SlotWorkspaceViewModel.AtlasItem(
                identity,
                new ItemStack(itemId, carried ? 1 : 8, 64),
                itemId.substring(itemId.indexOf(':') + 1),
                carried ? 1 : 8,
                0,
                "building",
                false,
                false,
                carried,
                !carried,
                proximateCount,
                List.of(),
                proximateCount > 0
                        ? List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("nearby", "Nearby", proximateCount))
                        : List.of(),
                elsewhere,
                false,
                0,
                0,
                false,
                desiredCount,
                false,
                wantedCount,
                "",
                -1,
                0);
    }

    private static SlotWorkspaceViewModel.AtlasIsland island() {
        return island(2);
    }

    private static SlotWorkspaceViewModel.AtlasIsland island(int carriedCount) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                "building",
                "Building",
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                0xFFFFFFFF,
                carriedCount)
                .withCarriedCount(carriedCount);
    }

    private static List<String> descendantText(SlotUiElement root) {
        java.util.ArrayList<String> text = new java.util.ArrayList<>();
        collectText(root, text);
        return text;
    }

    private static void collectText(SlotUiElement element, java.util.ArrayList<String> text) {
        if (element == null) {
            return;
        }
        if (element.text() != null && !element.text().isBlank()) {
            text.add(element.text());
        }
        for (SlotUiElement child : element.children()) {
            collectText(child, text);
        }
    }

    private static String facetLayer() {
        return """
                {
                  "schema_version": 1,
                  "layer": "player",
                  "entries": {
                    "minecraft:white_wool": {
                      "facets": {
                        "dye_color": {"value": "white"},
                        "role": {"value": "decorative_block"}
                      }
                    },
                    "minecraft:gray_wool": {
                      "facets": {
                        "dye_color": {"value": "gray"},
                        "role": {"value": "decorative_block"}
                      }
                    },
                    "minecraft:black_wool": {
                      "facets": {
                        "dye_color": {"value": "black"},
                        "role": {"value": "decorative_block"}
                      }
                    }
                  }
                }
                """;
    }

    private static final class HeaderContext implements WallSectionHeaderUiBuilder.Context {
        @Override
        public void beginIslandEdit(
                SlotWorkspaceViewModel.AtlasIsland island,
                float screenX,
                float screenY
        ) {
        }
    }
}
