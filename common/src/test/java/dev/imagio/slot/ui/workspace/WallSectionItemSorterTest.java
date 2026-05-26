package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
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
    void sectionUsesOneStableGridAndFacetSortsWithoutGhostCarriedBands() {
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

        assertEquals(2, section.children().size());
        SlotUiElement grid = section.children().get(1);
        assertTrue(grid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID));
        assertEquals(List.of(carriedWhite, ghostGray, carriedBlack),
                grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
        assertSame(island, grid.attachment(
                WorkspaceUiAttachments.ATLAS_ISLAND,
                SlotWorkspaceViewModel.AtlasIsland.class));
    }

    @Test
    void desiredCountDoesNotPromoteGhostAheadOfStablePosition() {
        SlotWorkspaceViewModel.AtlasItem ordinaryGhost = item("minecraft:apple", false, 0);
        SlotWorkspaceViewModel.AtlasItem desiredGhost = item("minecraft:zinc_ingot", false, 12);

        List<SlotWorkspaceViewModel.AtlasItem> items = WallSectionItemSorter.sort(List.of(
                ordinaryGhost,
                desiredGhost
        ));

        assertEquals(List.of(ordinaryGhost, desiredGhost), items);
    }

    @Test
    void wantedCountDoesNotPromoteGhostAheadOfStablePosition() {
        SlotWorkspaceViewModel.AtlasItem ordinaryGhost = item("minecraft:apple", false, 0, 0);
        SlotWorkspaceViewModel.AtlasItem wantedGhost = item("minecraft:zinc_ingot", false, 0, 1);

        List<SlotWorkspaceViewModel.AtlasItem> items = WallSectionItemSorter.sort(List.of(
                ordinaryGhost,
                wantedGhost
        ));

        assertEquals(List.of(ordinaryGhost, wantedGhost), items);
    }

    @Test
    void acquiringGhostKeepsIdentityInTheSameRelativePosition() {
        SlotWorkspaceViewModel.AtlasItem carriedApple = item("minecraft:apple", true);
        SlotWorkspaceViewModel.AtlasItem ghostIron = item("minecraft:iron_ingot", false);
        SlotWorkspaceViewModel.AtlasItem carriedZinc = item("minecraft:zinc_ingot", true);
        SlotWorkspaceViewModel.AtlasItem acquiredIron = item("minecraft:iron_ingot", true);

        assertEquals(
                List.of("minecraft:apple", "minecraft:iron_ingot", "minecraft:zinc_ingot"),
                identities(WallSectionItemSorter.sort(List.of(carriedZinc, ghostIron, carriedApple))));
        assertEquals(
                List.of("minecraft:apple", "minecraft:iron_ingot", "minecraft:zinc_ingot"),
                identities(WallSectionItemSorter.sort(List.of(carriedZinc, acquiredIron, carriedApple))));
    }

    @Test
    void sectionHidesOrdinaryProximateGhostsBehindHeaderToggleByDefault() {
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
        SlotUiElement header = section.children().get(0);
        SlotUiElement toggle = header.children().get(1);
        SlotUiElement carriedGrid = section.children().get(1);
        assertEquals(List.of(carriedStone), carriedGrid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
        assertEquals(1, toggle.attachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_COUNT, Integer.class));
        assertEquals(Boolean.FALSE, toggle.attachment(
                WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_EXPANDED,
                Boolean.class));
        assertEquals(List.of("Building", "\u25B8 +1", "2\u25CF"), descendantText(header));
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
    void expandedSectionRevealsProximateGhostsAndKeepsHeaderCollapseToggle() {
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

        assertEquals(2, section.children().size());
        SlotUiElement header = section.children().get(0);
        SlotUiElement toggle = header.children().get(1);
        SlotUiElement grid = section.children().get(1);
        assertEquals(List.of(carriedStone, ghostTorch), grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, List.class));
        assertEquals(Boolean.TRUE, toggle.attachment(
                WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_EXPANDED,
                Boolean.class));
        assertEquals(List.of("Building", "\u25BE 1", "2\u25CF"), descendantText(header));
    }

    @Test
    void nearbyHeaderToggleUsesCompactLabelAndClickHandler() {
        HeaderContext context = new HeaderContext();
        SlotWorkspaceViewModel.AtlasIsland island = island(0);
        SlotUiElement header = new WallSectionHeaderUiBuilder(context)
                .header(island, 0, 0, false, 12, false, true);
        SlotUiElement toggle = header.children().get(1);

        assertEquals(WallSectionHeaderUiBuilder.COMPACT_HEADER_HEIGHT_PX, header.layout().height());
        assertEquals(List.of("\u25B8 +12"), descendantText(toggle));
        assertTrue(toggle.textStyle().adaptiveWidth());
        SlotUiEvent headerClick = new SlotUiEvent(SlotUiEventKind.CLICK, 0, 1, 1, false);
        header.dispatch(headerClick);
        assertTrue(headerClick.propagationStopped());
        assertEquals(island, context.toggled);
        context.toggled = null;

        SlotUiEvent click = new SlotUiEvent(SlotUiEventKind.CLICK, 0, 1, 1, false);
        toggle.dispatch(click);
        assertTrue(click.propagationStopped());
        assertEquals(island, context.toggled);
    }

    @Test
    void expandedNearbyOnlySectionDoesNotRepeatNearbyCount() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem ghostTorch = item("minecraft:torch", false);

        SlotUiElement section = builder.section(
                island(0),
                List.of(ghostTorch),
                1,
                false,
                StorageGhostRevealMode.COLLAPSED,
                true,
                false);

        assertEquals(List.of("Building", "\u25BE 1"), descendantText(section.children().get(0)));
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
        assertTrue(!ghostGrid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_COUNT));
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
        assertTrue(!section.children().get(1).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_COUNT));
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
        assertTrue(!section.children().get(1).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_COUNT));
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
        assertTrue(!section.children().get(1).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_COUNT));
    }

    @Test
    void proximateXrayRevealsPreviouslyHiddenGhostInStablePosition() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem carriedApple = item("minecraft:apple", true);
        SlotWorkspaceViewModel.AtlasItem carriedZinc = item("minecraft:zinc_ingot", true);
        SlotWorkspaceViewModel.AtlasItem hiddenIron = item("minecraft:iron_ingot", false);

        SlotUiElement collapsed = builder.section(
                island(),
                List.of(carriedZinc, hiddenIron, carriedApple),
                3,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);
        assertEquals(List.of(carriedApple, carriedZinc), collapsed.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));

        SlotUiElement xray = builder.section(
                island(),
                List.of(carriedZinc, hiddenIron, carriedApple),
                3,
                false,
                StorageGhostRevealMode.PROXIMATE,
                false,
                false);
        assertEquals(List.of(carriedApple, hiddenIron, carriedZinc), xray.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));
    }

    @Test
    void trackedXrayRevealsElsewhereOnlyGhosts() {
        WallSectionUiBuilder builder = new WallSectionUiBuilder(new WallSectionHeaderUiBuilder(new HeaderContext()));
        SlotWorkspaceViewModel.AtlasItem carriedApple = item("minecraft:apple", true);
        SlotWorkspaceViewModel.AtlasItem carriedZinc = item("minecraft:zinc_ingot", true);
        SlotWorkspaceViewModel.AtlasItem remoteGhost = item(
                "minecraft:iron_ingot",
                false,
                0,
                0,
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 12)));

        SlotUiElement collapsed = builder.section(
                island(),
                List.of(carriedZinc, remoteGhost, carriedApple),
                3,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);
        assertEquals(List.of(carriedApple, carriedZinc), collapsed.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));

        SlotUiElement xray = builder.section(
                island(),
                List.of(carriedZinc, remoteGhost, carriedApple),
                3,
                false,
                StorageGhostRevealMode.TRACKED,
                false,
                false);
        assertEquals(List.of(carriedApple, remoteGhost, carriedZinc), xray.children().get(1).attachment(
                WorkspaceUiAttachments.ATLAS_ITEMS,
                List.class));
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

    private static List<String> identities(List<SlotWorkspaceViewModel.AtlasItem> items) {
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            ids.add(item.identity().itemId());
        }
        return ids;
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
        SlotWorkspaceViewModel.AtlasIsland toggled;

        @Override
        public void beginIslandEdit(
                SlotWorkspaceViewModel.AtlasIsland island,
                float screenX,
                float screenY
        ) {
        }

        @Override
        public void toggleNearbySection(SlotWorkspaceViewModel.AtlasIsland island) {
            toggled = island;
        }
    }
}
