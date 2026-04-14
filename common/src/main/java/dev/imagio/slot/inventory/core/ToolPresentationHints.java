package dev.imagio.slot.inventory.core;

public record ToolPresentationHints(
        String title,
        int priority,
        String preferredPlacement,
        int preferredHeight
) {
    public ToolPresentationHints {
        title = title == null ? "" : title;
        preferredPlacement = preferredPlacement == null ? "" : preferredPlacement;
        preferredHeight = Math.max(0, preferredHeight);
    }
}
