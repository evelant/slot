package dev.imagio.slot.client.screen;

public interface SlotPanelScreen {
    SlotPanelBounds slotPanelBounds();

    default void slotRefreshContents() {
    }
}
