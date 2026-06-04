package dev.imagio.slot.ui.workspace;

import java.util.List;

public final class WorkspaceHelpContent {
    public static final int POPOVER_WIDTH_PX = 248;
    public static final int POPOVER_HEIGHT_PX = 190;
    public static final int KEY_WIDTH_PX = 82;

    private static final List<Line> GESTURES = List.of(
            new Line("Left-click", "pick up; drag to move home"),
            new Line("Right-click", "open item actions"),
            new Line("Shift+right-click", "pull gaps or push carried home"),
            new Line("Shift+scroll", "move counts; up takes, down stores"),
            new Line("Shift+left-click", "insert one into open container"),
            new Line("Ctrl+scroll", "change desired count"),
            new Line("Alt+scroll", "change wanted count"),
            new Line("W on hover", "set wanted to a useful count")
    );

    private static final List<Line> TERMS = List.of(
            new Line("Home", "remembered section for a card"),
            new Line("Desired", "persistent keep-on-hand count"),
            new Line("Wanted", "temporary target for now"),
            new Line("Workflow", "task tab with members and inputs"),
            new Line("Gather", "pull target gaps from nearby chests"),
            new Line("Deposit", "put away via learned routes"),
            new Line("Crafting helper", "tracked EMI recipes stage inputs")
    );

    private WorkspaceHelpContent() {
    }

    public static List<Line> gestures() {
        return GESTURES;
    }

    public static List<Line> terms() {
        return TERMS;
    }

    public record Line(String key, String text) {
        public Line {
            key = key == null ? "" : key;
            text = text == null ? "" : text;
        }
    }
}
