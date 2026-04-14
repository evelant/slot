package dev.imagio.slot.client.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotSessionKindTest {
    @Test
    void recentLootFlagsMatchSessionKinds() {
        assertTrue(SlotSessionKind.GENERAL.recordsRecentLoot());
        assertFalse(SlotSessionKind.PLAYER_INVENTORY.recordsRecentLoot());
        assertTrue(SlotSessionKind.SLOT_WORKSPACE.recordsRecentLoot());
        assertFalse(SlotSessionKind.SLOT_CARRIED.recordsRecentLoot());
        assertTrue(SlotSessionKind.EXTERNAL_CONTAINER.recordsRecentLoot());
        assertFalse(SlotSessionKind.CARRIED_CONTAINER.recordsRecentLoot());
        assertTrue(SlotSessionKind.NON_STORAGE_CONTAINER.recordsRecentLoot());
    }
}
