package dev.imagio.slot.ui.spi;

public record SlotUiEventBinding(
        SlotUiEventKind kind,
        SlotUiEventHandler handler,
        boolean includeChildren
) {
    public SlotUiEventBinding {
        if (kind == null) {
            throw new IllegalArgumentException("kind is required");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is required");
        }
    }
}
