package dev.imagio.slot.client.screen.container;

public final class ToolSlotMapping {
    private ToolSlotMapping() {
    }

    public static MenuSlotId logicalMenuSlotId(int displaySlotId) {
        return MenuSlotId.of(displaySlotId);
    }
}
