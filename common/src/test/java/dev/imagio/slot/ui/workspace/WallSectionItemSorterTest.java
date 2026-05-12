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

    private static SlotWorkspaceViewModel.AtlasItem item(String itemId, boolean carried) {
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
                carried ? 0 : 8,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                "",
                -1,
                0);
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
