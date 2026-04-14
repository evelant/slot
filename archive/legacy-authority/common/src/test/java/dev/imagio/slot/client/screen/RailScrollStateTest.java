package dev.imagio.slot.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RailScrollStateTest {
    @Test
    void scrollWheelClampsIntoVisibleRange() {
        RailScrollState state = new RailScrollState();

        assertTrue(state.scrollWheel(-2.0, 10, 25));
        assertEquals(20, state.offset());

        assertTrue(state.scrollWheel(-2.0, 10, 25));
        assertEquals(25, state.offset());

        assertTrue(state.scrollWheel(10.0, 10, 25));
        assertEquals(0, state.offset());
    }

    @Test
    void scrollWheelResetsWhenNoOverflowExists() {
        RailScrollState state = new RailScrollState();
        state.setOffset(15, 25);

        assertFalse(state.scrollWheel(-1.0, 10, 0));
        assertEquals(0, state.offset());
    }

    @Test
    void scrollbarThumbReflectsCurrentOffset() {
        RailScrollState state = new RailScrollState();
        state.setOffset(50, 100);

        RailScrollState.ScrollbarThumb thumb = state.scrollbar(10, 110, 50, 150);
        assertNotNull(thumb);
        assertTrue(thumb.top() > 10);
        assertTrue(thumb.height() >= 14);
    }

    @Test
    void maxScrollUsesContentMinusViewport() {
        assertEquals(0, RailScrollState.maxScroll(20, 20));
        assertEquals(15, RailScrollState.maxScroll(35, 20));
    }
}
