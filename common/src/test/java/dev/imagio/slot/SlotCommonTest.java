package dev.imagio.slot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotCommonTest {
    @Test
    void modIdMatchesExpectedValue() {
        assertEquals("slot", SlotCommon.MOD_ID);
    }
}
