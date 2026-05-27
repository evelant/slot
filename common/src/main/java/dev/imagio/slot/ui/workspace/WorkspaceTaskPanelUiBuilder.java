package dev.imagio.slot.ui.workspace;

public final class WorkspaceTaskPanelUiBuilder {
    public static final int PANEL_WIDTH_PX = 188;

    private WorkspaceTaskPanelUiBuilder() {
    }

    public static String title(boolean hasSuggestionRows, boolean hasCraftingRows) {
        if (hasSuggestionRows && hasCraftingRows) {
            return "Tasks";
        }
        if (hasSuggestionRows) {
            return "Inventory";
        }
        return "Crafting";
    }
}
