package dev.imagio.slot.client.screen;

public final class InventoryHotbarClickSupport {
    private InventoryHotbarClickSupport() {
    }

    public static HotbarClickIntent resolve(
            int hotbarSlot,
            boolean shiftDown,
            int button,
            boolean cursorCarryingStack,
            boolean hotbarSlotOccupied
    ) {
        if (hotbarSlot < 0) {
            return HotbarClickIntent.IGNORED;
        }
        if (shiftDown) {
            return HotbarClickIntent.STASH_SLOT;
        }
        if (button != 0 && button != 1) {
            return HotbarClickIntent.IGNORED;
        }
        if (cursorCarryingStack || hotbarSlotOccupied) {
            return HotbarClickIntent.CLICK_SLOT;
        }
        return HotbarClickIntent.CLEAR_CURSOR;
    }

    public enum HotbarClickIntent {
        IGNORED,
        STASH_SLOT,
        CLICK_SLOT,
        CLEAR_CURSOR
    }
}
