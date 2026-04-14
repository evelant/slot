package dev.imagio.slot.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryRefreshDelayStateTest {
    @Test
    void normalRefreshCanRefreshOnEveryWaitingTick() {
        InventoryRefreshDelayState state = new InventoryRefreshDelayState();
        state.schedule(2);

        assertTrue(state.active());
        assertTrue(state.tick(false));
        assertEquals(1, state.ticksRemaining());
        assertTrue(state.tick(false));
        assertEquals(0, state.ticksRemaining());
        assertFalse(state.active());
    }

    @Test
    void delayedRefreshOnlyFiresWhenExpired() {
        InventoryRefreshDelayState state = new InventoryRefreshDelayState();
        state.schedule(2);

        assertFalse(state.tick(true));
        assertEquals(1, state.ticksRemaining());
        assertTrue(state.tick(true));
        assertEquals(0, state.ticksRemaining());
    }

    @Test
    void clearCancelsPendingRefresh() {
        InventoryRefreshDelayState state = new InventoryRefreshDelayState();
        state.schedule(8);
        state.clear();

        assertFalse(state.active());
        assertFalse(state.tick(false));
    }
}
