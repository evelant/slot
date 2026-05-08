package dev.imagio.slot.ui.spi;

@FunctionalInterface
public interface SlotUiEventHandler {
    void handle(SlotUiEvent event);
}
