package dev.imagio.slot.client.screen;

import org.junit.jupiter.api.Test;

import static dev.imagio.slot.client.screen.InventoryHotbarClickSupport.HotbarClickIntent;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryHotbarClickSupportTest {
    @Test
    void invalidSlotIsIgnoredBeforeModifiers() {
        assertEquals(
                HotbarClickIntent.IGNORED,
                InventoryHotbarClickSupport.resolve(-1, true, 0, true, true)
        );
    }

    @Test
    void shiftClickStashesAnyValidSlot() {
        assertEquals(
                HotbarClickIntent.STASH_SLOT,
                InventoryHotbarClickSupport.resolve(2, true, 2, false, false)
        );
    }

    @Test
    void onlyPrimaryAndSecondaryClicksCanMutateSlots() {
        assertEquals(
                HotbarClickIntent.IGNORED,
                InventoryHotbarClickSupport.resolve(2, false, 2, true, true)
        );
    }

    @Test
    void occupiedSlotOrCarriedCursorClicksSlot() {
        assertEquals(
                HotbarClickIntent.CLICK_SLOT,
                InventoryHotbarClickSupport.resolve(2, false, 0, false, true)
        );
        assertEquals(
                HotbarClickIntent.CLICK_SLOT,
                InventoryHotbarClickSupport.resolve(2, false, 1, true, false)
        );
    }

    @Test
    void emptySlotWithEmptyCursorClearsPendingCursorInteraction() {
        assertEquals(
                HotbarClickIntent.CLEAR_CURSOR,
                InventoryHotbarClickSupport.resolve(2, false, 0, false, false)
        );
    }
}
