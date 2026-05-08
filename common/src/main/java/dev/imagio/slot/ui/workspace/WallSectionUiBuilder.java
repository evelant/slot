package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiLayout;

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
        List<SlotWorkspaceViewModel.AtlasItem> cards = visibleCards == null ? List.of() : List.copyOf(visibleCards);
        SlotUiElement section = SlotUiElement.element()
                .id(island.islandId())
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(2)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        section.addChild(headerBuilder.header(island, cards.size(), totalCards, filtering));
        if (filtering && cards.isEmpty()) {
            return section;
        }
        section.addChild(SlotUiElement.element()
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
                        .alignContent(SlotUiLayout.AlignContent.FLEX_START)));
        return section;
    }
}
