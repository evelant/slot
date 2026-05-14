package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ACCENT;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class WallSectionHeaderUiBuilder {
    public static final int HEADER_HEIGHT_PX = 9;

    private final Context context;

    public WallSectionHeaderUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement header(
            SlotWorkspaceViewModel.AtlasIsland island,
            int visibleCount,
            int totalCount,
            boolean filtering
    ) {
        SlotUiElement header = SlotUiElement.button("", true, island.color())
                .noText()
                .zIndex(3)
                .attach(WorkspaceUiAttachments.ATLAS_ISLAND, island)
                .attach(WorkspaceUiAttachments.WALL_SECTION_HEADER, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(HEADER_HEIGHT_PX)
                        .paddingHorizontal(4)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));

        header.addChild(SlotUiElement.label(island.label(), TEXT)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(TEXT)
                        .shadow(true)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .allowHitTest(false));

        boolean hasCarried = island.carriedCount() > 0;
        int countColor = hasCarried ? ACCENT : MUTED;
        header.addChild(SlotUiElement.label(countText(island, visibleCount, totalCount, filtering, hasCarried), countColor)
                .layout(layout -> layout.heightPercent(100))
                .textStyle(style -> style
                        .color(countColor)
                        .shadow(false)
                        .fontSize(6)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .allowHitTest(false));

        if (island.kind() == VisualAtlasIslandKind.PLAYER) {
            header.on(SlotUiEventKind.MOUSE_DOWN, event -> {
                if (event.button() == 1) {
                    event.stopPropagation();
                    context.beginIslandEdit(island, event.x(), event.y());
                }
            }, true);
        }

        return header;
    }

    private static String countText(
            SlotWorkspaceViewModel.AtlasIsland island,
            int visibleCount,
            int totalCount,
            boolean filtering,
            boolean hasCarried
    ) {
        if (filtering && visibleCount != totalCount) {
            return visibleCount + " / " + totalCount;
        }
        if (hasCarried) {
            return island.carriedCount() + "/" + totalCount + "\u25CF";
        }
        return String.valueOf(totalCount);
    }

    public interface Context {
        void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY);
    }
}
