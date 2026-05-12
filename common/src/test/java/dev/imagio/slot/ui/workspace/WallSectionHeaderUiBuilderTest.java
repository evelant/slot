package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallSectionHeaderUiBuilderTest {
    @Test
    void playerHeaderLeftClickDoesNotEnterSelectionMode() {
        RecordingContext context = new RecordingContext();
        SlotUiElement header = new WallSectionHeaderUiBuilder(context)
                .header(island("blocks", "Blocks", 2), 2, 2, false);

        SlotUiEvent click = new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false);
        header.dispatch(click);

        assertTrue(!click.propagationStopped());
        assertNull(context.editedIsland);
    }

    @Test
    void rightMouseDownBeginsIslandEdit() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.AtlasIsland island = island("food", "Food", 7);
        SlotUiElement header = new WallSectionHeaderUiBuilder(context)
                .header(island, 3, 9, true);

        SlotUiEvent event = new SlotUiEvent(SlotUiEventKind.MOUSE_DOWN, 1, 44, 55, false);
        header.dispatch(event);

        assertTrue(event.propagationStopped());
        assertSame(island, context.editedIsland);
        assertEquals(44, context.editX);
        assertEquals(55, context.editY);
    }

    @Test
    void shiftMouseDownDoesNotInstallSelectionSuppression() {
        RecordingContext context = new RecordingContext();
        SlotUiElement header = new WallSectionHeaderUiBuilder(context)
                .header(island("tools", "Tools", 4), 4, 4, false);

        SlotUiEvent mouseDown = new SlotUiEvent(SlotUiEventKind.MOUSE_DOWN, 0, 4, 5, true);
        header.dispatch(mouseDown);
        SlotUiEvent click = new SlotUiEvent(SlotUiEventKind.CLICK, 0, 4, 5, false);
        header.dispatch(click);

        assertTrue(!mouseDown.propagationStopped());
        assertTrue(!click.propagationStopped());
        assertNull(context.editedIsland);
    }

    @Test
    void headerCarriesIslandAttachmentAndCountLabels() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.AtlasIsland island = island("misc", "Misc", 5)
                .withCarriedCount(2);

        SlotUiElement header = new WallSectionHeaderUiBuilder(context)
                .header(island, 5, 5, false);

        assertSame(island, header.attachment(WorkspaceUiAttachments.ATLAS_ISLAND, SlotWorkspaceViewModel.AtlasIsland.class));
        assertTrue(header.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_HEADER));
        assertEquals("Misc", header.children().get(0).text());
        assertEquals("2/5\u25CF", header.children().get(1).text());
    }

    @Test
    void sectionKeepsHeaderButCollapsesGridWhenFilteringMatchesNothing() {
        RecordingContext context = new RecordingContext();
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(context);
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);

        SlotUiElement section = sectionBuilder.section(island("empty", "Empty", 3), java.util.List.of(), 3, true);

        assertEquals("empty", section.id());
        assertEquals(1, section.children().size());
        assertTrue(section.children().get(0).hasAttachment(WorkspaceUiAttachments.WALL_SECTION_HEADER));
    }

    @Test
    void sectionGridCarriesIslandAndVisibleCardAttachment() {
        RecordingContext context = new RecordingContext();
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(context);
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);
        SlotWorkspaceViewModel.AtlasIsland island = island("building", "Building", 0);

        SlotUiElement section = sectionBuilder.section(island, java.util.List.of(), 0, false);
        SlotUiElement grid = section.children().get(1);

        assertTrue(grid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID));
        assertSame(island, grid.attachment(WorkspaceUiAttachments.ATLAS_ISLAND, SlotWorkspaceViewModel.AtlasIsland.class));
        assertEquals(java.util.List.of(), grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, java.util.List.class));
    }

    @Test
    void wallCardShellCarriesBodyAndAtlasItemAttachments() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", false, true);

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertTrue(card.hasAttachment(WorkspaceUiAttachments.WALL_CARD));
        assertSame(item, card.attachment(WorkspaceUiAttachments.ATLAS_ITEM, SlotWorkspaceViewModel.AtlasItem.class));
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, card.layout().width());
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, card.layout().height());
        SlotUiElement body = card.children().get(0);
        assertTrue(body.hasAttachment(WorkspaceUiAttachments.WALL_CARD_BODY));
        assertSame(item, body.attachment(WorkspaceUiAttachments.ATLAS_ITEM, SlotWorkspaceViewModel.AtlasItem.class));
    }

    @Test
    void carriedCardChromeDoesNotChangeForRecentItems() {
        int normal = WallCardUiBuilder.cardChromeColor(false, false, false, true, false);
        int recent = WallCardUiBuilder.cardChromeColor(false, false, true, true, false);

        assertEquals(normal, recent);
    }

    @Test
    void wallCardDoesNotRenderElsewherePlusBadgeWithoutSearch() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 32))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertTrue(descendantText(card).stream().noneMatch(text -> text.startsWith("+")));
    }

    @Test
    void wallCardRendersStoredPlusBadgeForSearchMatch() {
        RecordingCardContext context = new RecordingCardContext();
        context.searchQuery = "torch";
        context.searchMatches = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 32))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertTrue(descendantText(card).contains("+32"));
    }

    @Test
    void wallCardExpandsForBestElsewhereSearchHit() {
        RecordingCardContext context = new RecordingCardContext();
        context.searchQuery = "stone";
        context.searchMatches = true;
        SlotWorkspaceViewModel.ChestPresenceEntry small =
                new SlotWorkspaceViewModel.ChestPresenceEntry("chest:a", "A", 2);
        SlotWorkspaceViewModel.ChestPresenceEntry large =
                new SlotWorkspaceViewModel.ChestPresenceEntry("chest:b", "B", 9);
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:stone",
                "Stone",
                false,
                true,
                java.util.List.of(small, large)
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement body = card.children().get(0);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX + WallCardUiBuilder.WAYFINDING_STRIP_WIDTH_PX,
                card.layout().width());
        assertSame(large, body.attachment(
                WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                SlotWorkspaceViewModel.ChestPresenceEntry.class));
        assertEquals(Boolean.TRUE, body.attachment(
                WorkspaceUiAttachments.WALL_CARD_ACTIVE_SEARCH_MATCH,
                Boolean.class));
    }

    @Test
    void wallCardHoverAndTickMutateCommonShellState() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:torch", "Torch", false, true);
        context.activeIdentity = item.identity();

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        card.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_ENTER, 0, 0, 0, false));
        context.focused = true;
        card.dispatch(new SlotUiEvent(SlotUiEventKind.TICK, 0, 0, 0, false));
        card.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_LEAVE, 0, 0, 0, false));

        assertEquals(item.identity(), context.hovered);
        assertEquals(item.identity(), context.cleared);
        assertEquals(10, card.zIndex());
        assertEquals(WorkspaceUiPalette.SELECTED, card.buttonColor());
    }

    @Test
    void recentsStripKeepsEmptyStateMounted() {
        RecordingRecentsContext context = new RecordingRecentsContext();

        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of());

        assertTrue(strip.hasAttachment(WorkspaceUiAttachments.RECENTS_STRIP));
        assertEquals(2, strip.children().size());
        assertEquals("Recent", strip.children().get(0).text());
        assertEquals("nothing yet", strip.children().get(1).text());
    }

    @Test
    void recentsStripDistinguishesUnresolvedRecentIdentitiesFromEmptyHistory() {
        RecordingRecentsContext context = new RecordingRecentsContext();
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                "minecraft:missing",
                dev.imagio.slot.inventory.core.ItemComparisonMode.ITEM_ID.name(),
                ""
        );

        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of(identity));

        assertEquals(2, strip.children().size());
        assertEquals("not visible", strip.children().get(1).text());
    }

    @Test
    void recentsStripRendersIconsAndSkipsMissingIdentities() {
        RecordingRecentsContext context = new RecordingRecentsContext();
        java.util.ArrayList<SlotWorkspaceViewModel.IdentityRef> recents = new java.util.ArrayList<>();
        recents.add(new SlotWorkspaceViewModel.IdentityRef(
                "minecraft:missing",
                dev.imagio.slot.inventory.core.ItemComparisonMode.ITEM_ID.name(),
                ""
        ));
        SlotWorkspaceViewModel.AtlasItem first = null;
        for (int index = 0; index < RecentsStripUiBuilder.MAX_ICONS + 2; index++) {
            SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                    "minecraft:item_" + index,
                    "Item " + index,
                    true,
                    true
            );
            if (first == null) {
                first = item;
            }
            context.items.put(item.identity(), item);
            recents.add(item.identity());
        }

        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(recents);
        SlotUiElement firstIcon = strip.children().get(1);

        assertEquals(RecentsStripUiBuilder.MAX_ICONS + 1, strip.children().size());
        assertTrue(firstIcon.hasAttachment(WorkspaceUiAttachments.RECENTS_ICON));
        assertSame(first, firstIcon.attachment(
                WorkspaceUiAttachments.ATLAS_ITEM,
                SlotWorkspaceViewModel.AtlasItem.class));
        assertEquals(SlotUiElement.Kind.ITEM_ICON, firstIcon.children().get(0).kind());
    }

    @Test
    void recentsIconMouseDownFocusesItem() {
        RecordingRecentsContext context = new RecordingRecentsContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", true, true);
        context.items.put(item.identity(), item);
        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of(item.identity()));
        SlotUiElement icon = strip.children().get(1);

        SlotUiEvent event = new SlotUiEvent(SlotUiEventKind.MOUSE_DOWN, 0, 0, 0, false);
        icon.dispatch(event);

        assertTrue(event.propagationStopped());
        assertSame(item, context.focused);
    }

    @Test
    void recentsIconHoverTracksItem() {
        RecordingRecentsContext context = new RecordingRecentsContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", true, true);
        context.items.put(item.identity(), item);
        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of(item.identity()));
        SlotUiElement icon = strip.children().get(1);

        icon.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_ENTER, 0, 0, 0, false));
        icon.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_LEAVE, 0, 0, 0, false));

        assertSame(item, context.hovered);
        assertSame(item, context.cleared);
    }


    private static SlotWorkspaceViewModel.AtlasIsland island(String id, String label, int count) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                id,
                label,
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                0xCC334455,
                count,
                0
        );
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            String itemId,
            String name,
            boolean recent,
            boolean carried
    ) {
        return atlasItem(itemId, name, recent, carried, java.util.List.of());
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            String itemId,
            String name,
            boolean recent,
            boolean carried,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                itemId,
                dev.imagio.slot.inventory.core.ItemComparisonMode.ITEM_ID.name(),
                ""
        );
        return new SlotWorkspaceViewModel.AtlasItem(
                identity,
                new ItemStack(itemId, 1, 64),
                name,
                1,
                0,
                "tools",
                recent,
                false,
                carried,
                !carried,
                0,
                java.util.List.of(),
                java.util.List.of(),
                elsewhere,
                false,
                0,
                0,
                false,
                0,
                false,
                "",
                -1,
                0
        );
    }

    private static java.util.List<String> descendantText(SlotUiElement root) {
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

    private static final class RecordingContext implements WallSectionHeaderUiBuilder.Context {
        SlotWorkspaceViewModel.AtlasIsland editedIsland;
        float editX;
        float editY;

        @Override
        public void beginIslandEdit(
                SlotWorkspaceViewModel.AtlasIsland island,
                float screenX,
                float screenY
        ) {
            editedIsland = island;
            editX = screenX;
            editY = screenY;
        }
    }

    private static final class RecordingCardContext implements WallCardUiBuilder.Context {
        SlotWorkspaceViewModel.IdentityRef activeIdentity;
        SlotWorkspaceViewModel.IdentityRef hovered;
        SlotWorkspaceViewModel.IdentityRef cleared;
        String searchQuery = "";
        boolean searchMatches;
        boolean focused;

        @Override
        public SlotWorkspaceViewModel.IdentityRef activeIdentity() {
            return activeIdentity;
        }

        @Override
        public String normalizedSearchQuery() {
            return searchQuery;
        }

        @Override
        public boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
            return searchMatches;
        }

        @Override
        public boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
            return focused;
        }

        @Override
        public void hoverAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            hovered = identity;
        }

        @Override
        public void clearHoveredAtlasIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            cleared = identity;
        }
    }

    private static final class RecordingRecentsContext implements RecentsStripUiBuilder.Context {
        final java.util.Map<SlotWorkspaceViewModel.IdentityRef, SlotWorkspaceViewModel.AtlasItem> items =
                new java.util.LinkedHashMap<>();
        SlotWorkspaceViewModel.AtlasItem focused;
        SlotWorkspaceViewModel.AtlasItem hovered;
        SlotWorkspaceViewModel.AtlasItem cleared;

        @Override
        public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
            return items.get(identity);
        }

        @Override
        public void focusRecent(SlotWorkspaceViewModel.AtlasItem item) {
            focused = item;
        }

        @Override
        public void hoverRecent(SlotWorkspaceViewModel.AtlasItem item) {
            hovered = item;
        }

        @Override
        public void clearHoveredRecent(SlotWorkspaceViewModel.AtlasItem item) {
            cleared = item;
        }
    }
}
