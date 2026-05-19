package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.network.chat.Component;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.ACCENT;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.MUTED;
import static dev.imagio.slot.ui.workspace.WorkspaceUiPalette.TEXT;

public final class WallSectionHeaderUiBuilder {
    public static final int HEADER_HEIGHT_PX = 9;
    public static final int COMPACT_HEADER_HEIGHT_PX = 6;

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
        return header(island, visibleCount, totalCount, filtering, 0, false, false);
    }

    public SlotUiElement header(
            SlotWorkspaceViewModel.AtlasIsland island,
            int visibleCount,
            int totalCount,
            boolean filtering,
            int nearbyToggleCount,
            boolean nearbyExpanded,
            boolean compact
    ) {
        int height = compact ? COMPACT_HEADER_HEIGHT_PX : HEADER_HEIGHT_PX;
        int titleFontSize = compact ? 6 : 7;
        int countFontSize = compact ? 5 : 6;
        SlotUiElement header = SlotUiElement.button("", true, island.color())
                .noText()
                .zIndex(3)
                .attach(WorkspaceUiAttachments.ATLAS_ISLAND, island)
                .attach(WorkspaceUiAttachments.WALL_SECTION_HEADER, Boolean.TRUE)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(height)
                        .paddingHorizontal(compact ? 2 : 4)
                        .gapAll(compact ? 1 : 3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));

        header.addChild(SlotUiElement.label(island.label(), TEXT)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(TEXT)
                        .shadow(true)
                        .fontSize(titleFontSize)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .allowHitTest(false));

        boolean hasCarried = island.carriedCount() > 0;
        int countColor = hasCarried ? ACCENT : MUTED;
        int normalizedNearbyCount = Math.max(0, nearbyToggleCount);
        if (normalizedNearbyCount > 0) {
            header.tooltipLines(nearbyToggleTooltip(normalizedNearbyCount, nearbyExpanded));
            header.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                context.toggleNearbySection(island);
            });
            header.addChild(nearbyToggle(island, normalizedNearbyCount, nearbyExpanded, compact));
        }
        String countText = countText(island, visibleCount, totalCount, filtering, hasCarried);
        if (!countText.isBlank()) {
            header.addChild(SlotUiElement.label(countText, countColor)
                    .layout(layout -> layout.heightPercent(100))
                    .textStyle(style -> style
                            .color(countColor)
                            .shadow(false)
                            .fontSize(countFontSize)
                            .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER))
                    .allowHitTest(false));
        }

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

    private SlotUiElement nearbyToggle(
            SlotWorkspaceViewModel.AtlasIsland island,
            int count,
            boolean expanded,
            boolean compact
    ) {
        String label = (expanded ? "-" : "+") + count;
        return SlotUiElement.label(label, ACCENT)
                .allowHitTest(true)
                .attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ISLAND, island)
                .attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_COUNT, count)
                .attach(WorkspaceUiAttachments.WALL_SECTION_NEARBY_TOGGLE_EXPANDED, expanded)
                .tooltipLines(nearbyToggleTooltip(count, expanded))
                .layout(layout -> layout
                        .width(Math.max(compact ? 10 : 14, label.length() * (compact ? 4 : 5)))
                        .heightPercent(100))
                .textStyle(style -> style
                        .color(ACCENT)
                        .shadow(false)
                        .fontSize(compact ? 5 : 6)
                        .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    context.toggleNearbySection(island);
                });
    }

    private static List<Component> nearbyToggleTooltip(int count, boolean expanded) {
        return List.of(Component.literal((expanded ? "Hide " : "Show ")
                + Math.max(0, count)
                + " nearby storage cards"));
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
        if (!filtering && totalCount <= 0) {
            return "";
        }
        return String.valueOf(totalCount);
    }

    public interface Context {
        void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY);

        default void toggleNearbySection(SlotWorkspaceViewModel.AtlasIsland island) {
        }
    }
}
