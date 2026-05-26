package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;

import java.util.List;
import java.util.Set;

public final class WallSectionUiBuilder {
    public static final int CARD_GAP_PX = 2;
    private static final Set<String> HIDDEN_SUGGESTION_LANES = Set.of(
            SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW);

    private final WallSectionHeaderUiBuilder headerBuilder;

    public WallSectionUiBuilder(WallSectionHeaderUiBuilder headerBuilder) {
        if (headerBuilder == null) {
            throw new IllegalArgumentException("headerBuilder is required");
        }
        this.headerBuilder = headerBuilder;
    }

    public SlotUiElement section(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> visibleCards,
            int totalCards,
            boolean filtering
    ) {
        return section(
                island,
                visibleCards,
                totalCards,
                filtering,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);
    }

    public SlotUiElement section(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> visibleCards,
            int totalCards,
            boolean filtering,
            StorageGhostRevealMode revealMode,
            boolean nearbyExpanded,
            boolean forceRevealGhosts
    ) {
        return section(
                island,
                visibleCards,
                totalCards,
                filtering,
                revealMode,
                nearbyExpanded,
                forceRevealGhosts,
                true);
    }

    public SlotUiElement section(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> visibleCards,
            int totalCards,
            boolean filtering,
            StorageGhostRevealMode revealMode,
            boolean nearbyExpanded,
            boolean forceRevealGhosts,
            boolean allowCollapsedNearbyToggle
    ) {
        WallSectionVisibility.Result visibility = WallSectionVisibility.classify(
                visibleCards,
                filtering,
                nearbyExpanded,
                revealMode,
                forceRevealGhosts,
                allowCollapsedNearbyToggle);
        List<SlotWorkspaceViewModel.AtlasItem> cards = WallSectionItemSorter.sort(visibility.visibleCards());
        int headerTotalCount = headerTotalCount(totalCards, filtering, cards.size());
        boolean compactHeader = cards.isEmpty();
        SlotUiElement section = SlotUiElement.element()
                .id(island.islandId())
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(2)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        section.addChild(headerBuilder.header(
                island,
                cards.size(),
                headerTotalCount,
                filtering,
                visibility.nearbyToggleCount(),
                visibility.nearbyExpanded(),
                compactHeader));
        if (cards.isEmpty()) {
            return section;
        }
        section.addChild(grid(island, cards));
        return section;
    }

    public SlotUiElement suggestionLane(SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
        SlotWorkspaceViewModel.ContextualSuggestionLane resolved = lane == null
                ? new SlotWorkspaceViewModel.ContextualSuggestionLane("", "", List.of())
                : lane;
        SlotUiElement section = SlotUiElement.element()
                .id("suggestion:" + resolved.id())
                .attach(WorkspaceUiAttachments.WALL_SUGGESTION_LANE, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.CONTEXTUAL_SUGGESTION_LANE, resolved)
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(2)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        SlotUiElement header = SlotUiElement.panel(0xA01E2A34)
                .allowHitTest(false)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(WallSectionHeaderUiBuilder.HEADER_HEIGHT_PX)
                        .paddingHorizontal(4)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        header.addChild(SlotUiElement.label(resolved.label(), 0xFFE6EDF3)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(7)
                        .color(0xFFE6EDF3)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        header.addChild(SlotUiElement.label(Integer.toString(resolved.items().size()), 0xFF8EA0AE)
                .layout(layout -> layout.width(16).heightPercent(100))
                .textStyle(style -> style
                        .fontSize(6)
                        .color(0xFF8EA0AE)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        section.addChild(header);
        section.addChild(suggestionGrid(resolved));
        return section;
    }

    public static boolean shouldRenderSuggestionLane(SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
        if (lane == null || !lane.displayable()) {
            return false;
        }
        // Defensive UI-side filter for legacy or debug projections; common
        // projection currently skips these hidden experimental rows.
        return !HIDDEN_SUGGESTION_LANES.contains(lane.id());
    }

    private static int headerTotalCount(
            int totalCards,
            boolean filtering,
            int visibleCount
    ) {
        if (filtering) {
            return totalCards;
        }
        return visibleCount;
    }

    private static SlotUiElement grid(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> cards
    ) {
        return SlotUiElement.element()
                .attach(WorkspaceUiAttachments.WALL_SECTION_GRID, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ISLAND, island)
                .attach(WorkspaceUiAttachments.ATLAS_ITEMS, cards)
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(CARD_GAP_PX)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW)
                        .flexWrap(SlotUiLayout.FlexWrap.WRAP)
                        .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                        .alignContent(SlotUiLayout.AlignContent.FLEX_START));
    }

    private static SlotUiElement suggestionGrid(SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
        SlotUiElement grid = SlotUiElement.element()
                .attach(WorkspaceUiAttachments.WALL_SUGGESTION_GRID, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.CONTEXTUAL_SUGGESTION_LANE, lane)
                .attach(WorkspaceUiAttachments.ATLAS_ITEMS, lane.items())
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(CARD_GAP_PX)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW)
                        .flexWrap(SlotUiLayout.FlexWrap.WRAP)
                        .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                        .alignContent(SlotUiLayout.AlignContent.FLEX_START));
        if (lane.items().isEmpty() && !lane.placeholderText().isBlank()) {
            grid.addChild(SlotUiElement.label(lane.placeholderText(), 0xFF8EA0AE)
                    .allowHitTest(false)
                    .layout(layout -> layout.widthPercent(100).height(14))
                    .textStyle(style -> style
                            .fontSize(7)
                            .color(0xFF8EA0AE)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        return grid;
    }

}
