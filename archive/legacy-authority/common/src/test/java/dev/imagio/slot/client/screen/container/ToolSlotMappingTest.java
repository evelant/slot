package dev.imagio.slot.client.screen.container;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolSlotMappingTest {
    @Test
    void logicalMenuSlotIdPreservesDisplaySlotId() {
        assertEquals(MenuSlotId.of(23), ToolSlotMapping.logicalMenuSlotId(23));
    }
}
