package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public final class GoalTabsUiBuilder {
    public static final int TAB_ROW_HEIGHT_PX = 18;

    private final Context context;

    public GoalTabsUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public SlotUiElement tabs() {
        SlotUiElement row = SlotUiElement.panel(0xB810171D)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(TAB_ROW_HEIGHT_PX)
                        .paddingAll(2)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        row.addChild(tabButton("All", !context.goalActive(), "Show all wall cards", 34, context::selectAll));
        List<GoalTab> tabs = context.goalTabs();
        String status = "";
        for (GoalTab tab : tabs) {
            if (tab == null) {
                continue;
            }
            if (tab.active()) {
                status = tab.status();
            }
            row.addChild(goalButton(tab));
        }
        if (status != null && !status.isBlank()) {
            row.addChild(SlotUiElement.label(status, WorkspaceUiPalette.MUTED)
                    .layout(layout -> layout.flex(1).heightPercent(100))
                    .textStyle(style -> style
                            .color(WorkspaceUiPalette.MUTED)
                            .fontSize(6)
                            .horizontal(SlotUiTextStyle.Horizontal.RIGHT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
        }
        return row;
    }

    private SlotUiElement goalButton(GoalTab tab) {
        String goalId = tab.goalId();
        return tabButton(
                goalText(tab),
                tab.active(),
                "Recipe goal. Ctrl-scroll adjusts target count; right-click removes it.",
                112,
                () -> context.selectGoal(goalId)
        ).on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.button() != 1) {
                return;
            }
            event.stopPropagation();
            context.removeGoal(goalId);
        }, true).on(SlotUiEventKind.MOUSE_WHEEL, event -> {
            if (!event.controlDown() || event.wheelDelta() == 0f) {
                return;
            }
            event.stopPropagation();
            context.adjustGoalTargetCount(goalId, event.wheelDelta() > 0f ? 1 : -1);
        }, true);
    }

    private SlotUiElement tabButton(String text, boolean active, String tooltip, int width, Runnable onClick) {
        int color = active ? WorkspaceUiPalette.SELECTED : WorkspaceUiPalette.ROW_DIM;
        return SlotUiElement.button(text, true, color)
                .tooltip(Component.literal(tooltip == null ? "" : tooltip))
                .layout(layout -> layout
                        .width(width)
                        .height(14)
                        .paddingHorizontal(6))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (onClick != null) {
                        onClick.run();
                    }
                });
    }

    private static String goalText(GoalTab tab) {
        String label = tab == null ? "Goal" : tab.label();
        if (label == null || label.isBlank()) {
            label = "Goal";
        }
        if (label.length() > 15) {
            label = label.substring(0, 14) + ".";
        }
        return label + " x" + Math.max(1, tab == null ? 1 : tab.targetCount());
    }

    public record GoalTab(
            String goalId,
            String label,
            int targetCount,
            String status,
            boolean active
    ) {
        public GoalTab {
            goalId = goalId == null ? "" : goalId.trim();
            label = label == null || label.isBlank() ? "Goal" : label.trim();
            targetCount = Math.max(1, targetCount);
            status = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        }
    }

    public interface Context {
        boolean goalActive();

        List<GoalTab> goalTabs();

        void selectAll();

        void selectGoal(String goalId);

        void removeGoal(String goalId);

        void adjustGoalTargetCount(String goalId, int delta);
    }
}
