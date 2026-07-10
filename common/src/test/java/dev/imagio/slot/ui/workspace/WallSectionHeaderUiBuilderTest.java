package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", false, true);

        SlotUiElement section = sectionBuilder.section(island, java.util.List.of(item), 1, false);
        SlotUiElement grid = section.children().get(1);

        assertTrue(grid.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID));
        assertSame(island, grid.attachment(WorkspaceUiAttachments.ATLAS_ISLAND, SlotWorkspaceViewModel.AtlasIsland.class));
        assertEquals(java.util.List.of(item), grid.attachment(WorkspaceUiAttachments.ATLAS_ITEMS, java.util.List.class));
    }

    @Test
    void emptySectionUsesCompactHeaderWithoutEmptyGrid() {
        RecordingContext context = new RecordingContext();
        WallSectionHeaderUiBuilder headerBuilder = new WallSectionHeaderUiBuilder(context);
        WallSectionUiBuilder sectionBuilder = new WallSectionUiBuilder(headerBuilder);

        SlotUiElement section = sectionBuilder.section(island("empty", "Empty", 0), java.util.List.of(), 0, false);
        SlotUiElement header = section.children().get(0);

        assertEquals(1, section.children().size());
        assertEquals(WallSectionHeaderUiBuilder.COMPACT_HEADER_HEIGHT_PX, header.layout().height());
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
    void wallCardRendersElsewhereBadgeDuringTrackedXray() {
        RecordingCardContext context = new RecordingCardContext();
        context.storageGhostRevealMode = StorageGhostRevealMode.TRACKED;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                false,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 32))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertTrue(descendantText(card).contains("+32"));
    }

    @Test
    void wallCardStockPipLabelsUseReadableFontSize() {
        RecordingCardContext context = new RecordingCardContext();
        context.searchQuery = "stone";
        context.searchMatches = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:stone",
                "Stone",
                false,
                true,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("nearby", "Nearby", 32)),
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 64))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        java.util.Set<String> pipTexts = java.util.Set.of("32", "+64");
        java.util.List<SlotUiElement> pipLabels = descendantLabels(card).stream()
                .filter(label -> pipTexts.contains(label.text()))
                .toList();

        assertEquals(2, pipLabels.size());
        assertTrue(pipLabels.stream().allMatch(label ->
                Math.abs(label.textStyle().fontSize() - 5.0f) <= 0.001f));
    }

    @Test
    void wallCardCapsTopBadgesToHalfCardWhenBothStorageCountsRender() {
        RecordingCardContext context = new RecordingCardContext();
        context.searchQuery = "stone";
        context.searchMatches = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:stone",
                "Stone",
                false,
                true,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("nearby", "Nearby", 128)),
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 128))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement nearby = firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_NEARBY_BADGE);
        SlotUiElement distant = firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_DISTANT_BADGE);

        assertNotNull(nearby);
        assertNotNull(distant);
        assertEquals("99+", nearby.text());
        assertEquals("+99", distant.text());
        assertFalse(nearby.layout().hasWidth());
        assertFalse(distant.layout().hasWidth());
        assertEquals(1.0f, nearby.layout().paddingHorizontal());
        assertEquals(1.0f, distant.layout().paddingHorizontal());
        assertTrue(nearby.textStyle().adaptiveWidth());
        assertTrue(distant.textStyle().adaptiveWidth());
        assertEquals(WorkspaceUiPalette.NEARBY_BADGE, nearby.backgroundColor());
        assertEquals(WorkspaceUiPalette.DISTANT_BADGE, distant.backgroundColor());
    }

    @Test
    void expandedWallCardKeepsChromeAnchoredToSquareIconCell() {
        RecordingCardContext context = new RecordingCardContext();
        context.forceWayfinding = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:support",
                "Blackwood Support",
                false,
                true,
                128,
                128,
                0,
                java.util.List.of(),
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 128))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement body = card.children().get(0);
        SlotUiElement iconCell = body.children().get(0);
        SlotUiElement strip = body.children().get(1);
        SlotUiElement countBadge = firstDescendantWithAttachment(iconCell,
                WorkspaceUiAttachments.WALL_CARD_COUNT_BADGE);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX + WallCardUiBuilder.WAYFINDING_STRIP_WIDTH_PX,
                card.layout().width());
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, iconCell.layout().width());
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, iconCell.layout().height());
        assertNotNull(countBadge);
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, countBadge.layout().width());
        assertEquals(5.5f, countBadge.textStyle().fontSize(), 0.001f);
        assertFalse(countBadge.textStyle().adaptiveWidth());
        assertNull(firstDescendantWithAttachment(strip, WorkspaceUiAttachments.WALL_CARD_COUNT_BADGE));
        assertTrue(descendantText(card).contains("128/128"));
    }

    @Test
    void expandedWallCardRendersPrimaryRingInsideSquareIconCell() {
        RecordingCardContext context = new RecordingCardContext();
        context.forceWayfinding = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                1,
                64,
                0,
                java.util.List.of(),
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 64))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement iconCell = card.children().get(0).children().get(0);

        assertNotNull(firstDescendantWithAttachment(iconCell, WorkspaceUiAttachments.WALL_CARD_RING));
        assertEquals(WallCardUiBuilder.CARD_CELL_PX, iconCell.layout().width());
    }

    @Test
    void wallCardKeepsNearbyAndElsewhereBadgesSeparateForTargetGap() {
        RecordingCardContext context = new RecordingCardContext();
        context.searchQuery = "stone";
        context.searchMatches = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:stone",
                "Stone",
                false,
                true,
                1,
                64,
                0,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("nearby", "Nearby", 32)),
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote", "Warehouse", 64))
        );

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertTrue(descendantText(card).contains("1/64"));
        assertTrue(descendantText(card).contains("32"));
        assertTrue(descendantText(card).contains("+64"));
        WallCardChromeSpec spec = card.children().get(0).attachment(
                WorkspaceUiAttachments.WALL_CARD_CHROME_SPEC,
                WallCardChromeSpec.class);
        assertNotNull(spec);
        assertEquals(WallCardChromeSpec.Gap.STORED, spec.gap());
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
    void wantedOrDesiredGapWithoutKnownStorageRendersNeedState() {
        RecordingCardContext context = new RecordingCardContext();
        context.forceWayfinding = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                27,
                64,
                0,
                java.util.List.of(),
                java.util.List.of());

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement body = card.children().get(0);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX + WallCardUiBuilder.WAYFINDING_STRIP_WIDTH_PX,
                card.layout().width());
        assertNull(body.attachment(
                WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                SlotWorkspaceViewModel.ChestPresenceEntry.class));
        WallCardUiBuilder.MissingTargetDisplay missing = body.attachment(
                WorkspaceUiAttachments.WALL_CARD_MISSING_TARGET,
                WallCardUiBuilder.MissingTargetDisplay.class);
        assertNotNull(missing);
        assertEquals(37, missing.count());
        assertTrue(descendantText(card).contains("27/64"));
        assertTrue(descendantText(card).contains("need 37"));
    }

    @Test
    void partialNearbyStorageStillShowsNeedStripForRemainingTargetGap() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                27,
                64,
                0,
                java.util.List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("nearby", "Nearby", 10)),
                java.util.List.of());

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX + WallCardUiBuilder.WAYFINDING_STRIP_WIDTH_PX,
                card.layout().width());
        assertTrue(descendantText(card).contains("need 27"));
    }

    @Test
    void fluidCardsRenderFluidIconWithoutTextTag() {
        SlotWorkspaceViewModel.AtlasItem item = fluidAtlasItem(
                SlotResourceIdentity.fluid("gtceu:cryogenized_fluix"),
                "Cryogenized Fluix");

        SlotUiElement card = new WallCardUiBuilder(new RecordingCardContext()).card(item);
        SlotUiElement icon = descendants(card).stream()
                .filter(element -> element.kind() == SlotUiElement.Kind.FLUID_ICON)
                .findFirst()
                .orElse(null);

        assertTrue(item.displayStack().isEmpty());
        assertNotNull(icon);
        assertEquals("gtceu:cryogenized_fluix", icon.fluidId());
        assertTrue(descendantText(card).stream().noneMatch("CRYO"::equals));
        assertTrue(card.tooltipStack().isEmpty());
        java.util.List<String> tooltipText = card.tooltipLines().stream()
                .map(Component::getString)
                .toList();
        assertTrue(tooltipText.contains("Cryogenized Fluix"));
        assertTrue(tooltipText.contains("Carried amount: 1 B"));
        assertFalse(tooltipText.contains("Water Bucket"));
    }

    @Test
    void wantedOrDesiredGapWithRemoteStorageUsesWayfindingInsteadOfCraftState() {
        RecordingCardContext context = new RecordingCardContext();
        context.forceWayfinding = true;
        SlotWorkspaceViewModel.ChestPresenceEntry remote =
                new SlotWorkspaceViewModel.ChestPresenceEntry("chest:remote", "Remote", 37);
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                27,
                64,
                0,
                java.util.List.of(),
                java.util.List.of(remote));

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement body = card.children().get(0);

        assertSame(remote, body.attachment(
                WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                SlotWorkspaceViewModel.ChestPresenceEntry.class));
        assertNull(body.attachment(
                WorkspaceUiAttachments.WALL_CARD_MISSING_TARGET,
                WallCardUiBuilder.MissingTargetDisplay.class));
    }

    @Test
    void forcedWayfindingDoesNotUseNearbyPresenceForPutAwayCards() {
        RecordingCardContext context = new RecordingCardContext();
        context.forceWayfinding = true;
        SlotWorkspaceViewModel.ChestPresenceEntry nearby =
                new SlotWorkspaceViewModel.ChestPresenceEntry("chest:near", "Nearby", 5);
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:stone",
                "Stone",
                false,
                true,
                java.util.List.of(nearby),
                java.util.List.of());

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement body = card.children().get(0);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX, card.layout().width());
        assertNull(body.attachment(
                WorkspaceUiAttachments.WALL_CARD_WAYFINDING_ENTRY,
                SlotWorkspaceViewModel.ChestPresenceEntry.class));
        assertTrue(descendantText(card).contains("5"));
    }

    @Test
    void proximatePipCanShowDepositRouteWithoutStoredCount() {
        RecordingCardContext context = new RecordingCardContext();
        context.proximateDepositRoute = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", false, true);

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertEquals(WallCardUiBuilder.CARD_CELL_PX, card.layout().width());
        assertNotNull(firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_NEARBY_ROUTE_NOTCH));
        assertFalse(descendantText(card).contains("+"));
    }

    @Test
    void countBadgeColorFollowsDisplayedTargetSource() {
        RecordingCardContext context = new RecordingCardContext();

        assertEquals(
                WorkspaceUiPalette.COUNT_BADGE_NEUTRAL,
                countBadge(atlasItem("minecraft:stone", "Stone", false, true, 7, 0, 0,
                        java.util.List.of(), java.util.List.of()), context).backgroundColor());
        assertEquals(
                WorkspaceUiPalette.COUNT_BADGE_DESIRED,
                countBadge(atlasItem("minecraft:stone", "Stone", false, true, 2, 5, 0,
                        java.util.List.of(), java.util.List.of()), context).backgroundColor());
        assertEquals(
                WorkspaceUiPalette.COUNT_BADGE_WANTED,
                countBadge(atlasItem("minecraft:stone", "Stone", false, true, 2, 0, 5,
                        java.util.List.of(), java.util.List.of()), context).backgroundColor());
        assertEquals(
                WorkspaceUiPalette.COUNT_BADGE_WORKFLOW,
                countBadge(atlasItem("minecraft:stone", "Stone", false, true, 2, 5, true, false, 5,
                        java.util.List.of(), java.util.List.of()), context).backgroundColor());
    }

    @Test
    void targetGapUsesRingWithoutBottomLeftMarker() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:torch",
                "Torch",
                false,
                true,
                1,
                5,
                0,
                java.util.List.of(),
                java.util.List.of());

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertNotNull(firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_RING));
        assertTrue(descendants(card).stream().noneMatch(element ->
                Integer.valueOf(0xE0FFD166).equals(element.backgroundColor())
                        && element.layout().hasWidth()
                        && element.layout().width() == 5
                        && element.layout().hasHeight()
                        && element.layout().height() == 5));
    }

    @Test
    void choiceStateDoesNotRenderQuestionMarker() {
        RecordingCardContext context = new RecordingCardContext();
        context.choiceInvolved = true;
        context.choiceCard = true;
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", false, true);

        SlotUiElement card = new WallCardUiBuilder(context).card(item);

        assertFalse(descendantText(card).contains("?"));
    }

    @Test
    void putAwayStateWinsPrimaryRingBeforeTargetGap() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem(
                "minecraft:dirt",
                "Dirt",
                false,
                true,
                1,
                5,
                0,
                java.util.List.of(),
                java.util.List.of())
                .withPutAwayState(SlotWorkspaceViewModel.PutAwayState.ROUTED);

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement ring = firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_RING);

        assertNotNull(ring);
        assertEquals(WorkspaceUiPalette.PUT_AWAY_ROUTED, ring.backgroundColor());
    }

    @Test
    void putAwayNoRouteUsesPrimaryRing() {
        RecordingCardContext context = new RecordingCardContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:dirt", "Dirt", false, true)
                .withPutAwayState(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE);

        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement ring = firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_RING);

        assertNotNull(ring);
        assertEquals(WorkspaceUiPalette.PUT_AWAY_NO_ROUTE, ring.backgroundColor());
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
        SlotUiElement label = strip.children().get(0);
        assertEquals("Recent", label.text());
        assertEquals(SlotUiLayout.PositionType.ABSOLUTE, label.layout().positionType());
        assertEquals(RecentsStripUiBuilder.PADDING_PX + 1, label.layout().left());
        SlotUiElement grid = strip.children().get(1);
        assertEquals("nothing yet", grid.children().get(0).text());
    }

    @Test
    void recentsStripUsesThreeRowsAndFixedFloatingSize() {
        RecordingRecentsContext context = new RecordingRecentsContext();

        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of());

        assertEquals(3, RecentsStripUiBuilder.MAX_ROWS);
        assertEquals(24, RecentsStripUiBuilder.MAX_ICONS);
        assertEquals(RecentsStripUiBuilder.STRIP_WIDTH_PX, strip.layout().width());
        assertEquals(RecentsStripUiBuilder.STRIP_HEIGHT_PX, strip.layout().height());
        assertEquals(
                RecentsStripUiBuilder.GRID_WIDTH_PX + RecentsStripUiBuilder.PADDING_PX * 2,
                RecentsStripUiBuilder.STRIP_WIDTH_PX);
        assertEquals(
                RecentsStripUiBuilder.CARD_SIZE_PX * 3
                        + RecentsStripUiBuilder.GAP_PX * 2
                        + RecentsStripUiBuilder.PADDING_PX * 2,
                RecentsStripUiBuilder.STRIP_HEIGHT_PX);
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
        assertEquals("not visible", strip.children().get(1).children().get(0).text());
    }

    @Test
    void recentsStripRendersCardsAndSkipsMissingIdentities() {
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
        SlotUiElement grid = strip.children().get(1);
        SlotUiElement firstCard = grid.children().get(0);

        assertEquals(2, strip.children().size());
        assertEquals(RecentsStripUiBuilder.MAX_ICONS, grid.children().size());
        assertTrue(firstCard.hasAttachment(WorkspaceUiAttachments.RECENTS_CARD));
        assertTrue(firstCard.hasAttachment(WorkspaceUiAttachments.WALL_CARD));
        assertSame(first, firstCard.attachment(
                WorkspaceUiAttachments.ATLAS_ITEM,
                SlotWorkspaceViewModel.AtlasItem.class));
        assertTrue(firstCard.children().get(0).hasAttachment(WorkspaceUiAttachments.WALL_CARD_BODY));
    }

    @Test
    void recentsCardMouseDownDoesNotConsumeNormalCardActions() {
        RecordingRecentsContext context = new RecordingRecentsContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", true, true);
        context.items.put(item.identity(), item);
        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of(item.identity()));
        SlotUiElement card = strip.children().get(1).children().get(0);

        SlotUiEvent event = new SlotUiEvent(SlotUiEventKind.MOUSE_DOWN, 0, 0, 0, false);
        card.dispatch(event);

        assertFalse(event.propagationStopped());
    }

    @Test
    void recentsIconHoverTracksItem() {
        RecordingRecentsContext context = new RecordingRecentsContext();
        SlotWorkspaceViewModel.AtlasItem item = atlasItem("minecraft:stone", "Stone", true, true);
        context.items.put(item.identity(), item);
        SlotUiElement strip = new RecentsStripUiBuilder(context).overlay(java.util.List.of(item.identity()));
        SlotUiElement icon = strip.children().get(1).children().get(0);

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
        return atlasItem(itemId, name, recent, carried, java.util.List.of(), elsewhere);
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            String itemId,
            String name,
            boolean recent,
            boolean carried,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> presence,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        return atlasItem(itemId, name, recent, carried, 1, 0, 0, presence, elsewhere);
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            String itemId,
            String name,
            boolean recent,
            boolean carried,
            int totalCount,
            int desiredCount,
            int wantedCount,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> presence,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        return atlasItem(itemId, name, recent, carried, totalCount, desiredCount, false, false, wantedCount,
                presence, elsewhere);
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            String itemId,
            String name,
            boolean recent,
            boolean carried,
            int totalCount,
            int desiredCount,
            boolean desiredCountFromKit,
            boolean kitNeeded,
            int wantedCount,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> presence,
            java.util.List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                itemId,
                dev.imagio.slot.inventory.core.ItemComparisonMode.ITEM_ID.name(),
                ""
        );
        return new SlotWorkspaceViewModel.AtlasItem(
                identity,
                new ItemStack(itemId, Math.max(1, totalCount), 64),
                name,
                totalCount,
                0,
                "tools",
                recent,
                false,
                carried,
                !carried,
                0,
                java.util.List.of(),
                presence,
                elsewhere,
                false,
                0,
                0,
                kitNeeded,
                desiredCount,
                desiredCountFromKit,
                wantedCount,
                "",
                -1,
                0
        );
    }

    private static SlotWorkspaceViewModel.AtlasItem fluidAtlasItem(
            SlotResourceIdentity resource,
            String name
    ) {
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                resource.syntheticItemId(),
                dev.imagio.slot.inventory.core.ItemComparisonMode.ITEM_ID.name(),
                "");
        return new SlotWorkspaceViewModel.AtlasItem(
                identity,
                ItemStack.EMPTY,
                name,
                1000,
                0,
                "tools",
                false,
                false,
                true,
                false,
                0,
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                false,
                false,
                "",
                -1,
                0,
                SlotWorkspaceViewModel.PutAwayState.NONE,
                SlotWorkspaceViewModel.ResourceRef.from(resource),
                1000L);
    }

    private static java.util.List<String> descendantText(SlotUiElement root) {
        java.util.ArrayList<String> text = new java.util.ArrayList<>();
        collectText(root, text);
        return text;
    }

    private static java.util.List<SlotUiElement> descendantLabels(SlotUiElement root) {
        java.util.ArrayList<SlotUiElement> labels = new java.util.ArrayList<>();
        collectLabels(root, labels);
        return labels;
    }

    private static java.util.List<SlotUiElement> descendants(SlotUiElement root) {
        java.util.ArrayList<SlotUiElement> elements = new java.util.ArrayList<>();
        collectDescendants(root, elements);
        return elements;
    }

    private static SlotUiElement firstDescendantWithAttachment(SlotUiElement root, String attachment) {
        return descendants(root).stream()
                .filter(element -> element.hasAttachment(attachment))
                .findFirst()
                .orElse(null);
    }

    private static SlotUiElement countBadge(
            SlotWorkspaceViewModel.AtlasItem item,
            RecordingCardContext context
    ) {
        SlotUiElement card = new WallCardUiBuilder(context).card(item);
        SlotUiElement badge = firstDescendantWithAttachment(card, WorkspaceUiAttachments.WALL_CARD_COUNT_BADGE);
        assertNotNull(badge);
        return badge;
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

    private static void collectDescendants(SlotUiElement element, java.util.ArrayList<SlotUiElement> elements) {
        if (element == null) {
            return;
        }
        elements.add(element);
        for (SlotUiElement child : element.children()) {
            collectDescendants(child, elements);
        }
    }

    private static void collectLabels(SlotUiElement element, java.util.ArrayList<SlotUiElement> labels) {
        if (element == null) {
            return;
        }
        if (element.kind() == SlotUiElement.Kind.LABEL) {
            labels.add(element);
        }
        for (SlotUiElement child : element.children()) {
            collectLabels(child, labels);
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

        @Override
        public void toggleNearbySection(SlotWorkspaceViewModel.AtlasIsland island) {
        }
    }

    private static final class RecordingCardContext implements WallCardUiBuilder.Context {
        SlotWorkspaceViewModel.IdentityRef activeIdentity;
        SlotWorkspaceViewModel.IdentityRef hovered;
        SlotWorkspaceViewModel.IdentityRef cleared;
        String searchQuery = "";
        boolean searchMatches;
        boolean focused;
        boolean forceWayfinding;
        boolean proximateDepositRoute;
        boolean choiceInvolved;
        boolean choiceCard;
        StorageGhostRevealMode storageGhostRevealMode = StorageGhostRevealMode.COLLAPSED;

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

        @Override
        public StorageGhostRevealMode storageGhostRevealMode() {
            return storageGhostRevealMode;
        }

        @Override
        public boolean forceWayfindingStrip(SlotWorkspaceViewModel.AtlasItem item) {
            return forceWayfinding;
        }

        @Override
        public boolean hasProximateDepositRoute(SlotWorkspaceViewModel.AtlasItem item) {
            return proximateDepositRoute;
        }

        @Override
        public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
            return choiceInvolved;
        }

        @Override
        public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
            return choiceCard;
        }
    }

    private static final class RecordingRecentsContext implements RecentsStripUiBuilder.Context {
        final java.util.Map<SlotWorkspaceViewModel.IdentityRef, SlotWorkspaceViewModel.AtlasItem> items =
                new java.util.LinkedHashMap<>();
        SlotWorkspaceViewModel.AtlasItem hovered;
        SlotWorkspaceViewModel.AtlasItem cleared;

        @Override
        public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
            return items.get(identity);
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
