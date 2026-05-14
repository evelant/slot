package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class WallSectionUiBuilder {
    public static final int CARD_GAP_PX = 2;

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
        WallSectionVisibility.Result visibility = WallSectionVisibility.classify(
                visibleCards,
                filtering,
                nearbyExpanded,
                revealMode,
                forceRevealGhosts);
        WallSectionItemSorter.Groups cards = WallSectionItemSorter.groupAndSort(visibility.visibleCards());
        int headerTotalCount = headerTotalCount(totalCards, filtering, visibility, cards.size());
        SlotUiElement section = SlotUiElement.element()
                .id(island.islandId())
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(2)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        section.addChild(headerBuilder.header(island, cards.size(), headerTotalCount, filtering));
        if (filtering && cards.isEmpty() && !visibility.showNearbyToggle()) {
            return section;
        }
        boolean chipOnCarriedRow = visibility.showNearbyToggle() && !visibility.nearbyExpanded();
        boolean chipOnGhostRow = visibility.showNearbyToggle() && visibility.nearbyExpanded();
        if (!cards.carried().isEmpty() || chipOnCarriedRow) {
            section.addChild(grid(island, cards.carried(), chipOnCarriedRow ? visibility : null));
        }
        if (!cards.ghosts().isEmpty() || chipOnGhostRow) {
            section.addChild(grid(island, cards.ghosts(), chipOnGhostRow ? visibility : null));
        }
        if (cards.isEmpty() && !visibility.showNearbyToggle()) {
            section.addChild(grid(island, List.of()));
        }
        return section;
    }

    private static int headerTotalCount(
            int totalCards,
            boolean filtering,
            WallSectionVisibility.Result visibility,
            int visibleCount
    ) {
        if (filtering) {
            return totalCards;
        }
        if (visibility == null || !visibility.showNearbyToggle() || visibility.nearbyExpanded()) {
            return visibleCount;
        }
        return visibleCount + visibility.nearbyToggleCount();
    }

    private static SlotUiElement grid(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> cards
    ) {
        return grid(island, cards, null);
    }

    private static SlotUiElement grid(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            WallSectionVisibility.Result nearby
    ) {
        SlotUiElement grid = SlotUiElement.element()
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
        if (nearby != null && nearby.showNearbyToggle()) {
            grid.attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT, nearby.nearbyToggleCount());
            grid.attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_EXPANDED, nearby.nearbyExpanded());
        }
        return grid;
    }

    public static SlotUiElement nearbyChip(
            SlotWorkspaceViewModel.AtlasIsland island,
            int count,
            boolean expanded
    ) {
        int normalizedCount = Math.max(0, count);
        String label = (expanded ? "-" : "+") + normalizedCount;
        SlotUiElement chip = SlotUiElement.button(
                        "",
                        true,
                        WallCardUiBuilder.cardChromeColor(false, false, false, false, false))
                .noText()
                .id(island == null ? "" : island.islandId() + ":nearby")
                .attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ISLAND, island)
                .attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_COUNT, normalizedCount)
                .attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_CHIP_EXPANDED, expanded)
                .tooltipLines(List.of(Component.literal(
                        (expanded ? "Hide " : "Show ") + normalizedCount + " nearby storage cards")))
                .layout(layout -> layout
                        .width(WallCardUiBuilder.CARD_CELL_PX)
                        .height(WallCardUiBuilder.CARD_CELL_PX)
                        .paddingAll(0)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW)
                        .alignContent(SlotUiLayout.AlignContent.FLEX_START));
        chip.addChild(SlotUiElement.label(label, 0xFFC5D4DD)
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .textStyle(style -> style
                        .color(0xFFC5D4DD)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        return chip;
    }
}
