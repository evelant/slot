package dev.imagio.slot.client.session;

import dev.imagio.slot.session.SlotSessionMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotScreenSessionTest {
    @Test
    void generalSessionDefaultsToRecentEnabledWithoutStorageView() {
        SlotScreenSession session = new SlotScreenSession(SlotSessionKind.GENERAL, "test.General", null);

        assertTrue(session.recordsRecentLoot());
        assertFalse(session.hasStorageView());
        assertNull(session.inventoryContextOrNull());
        assertEquals(SlotSessionMode.GENERAL, session.descriptor().mode());
        assertTrue(session.descriptor().recordsRecentLoot());
        assertTrue(session.descriptor().sources().isEmpty());
    }

    @Test
    void carriedModesRemainDistinctFromWorkspaceModes() {
        SlotScreenSession carried = new SlotScreenSession(SlotSessionKind.CARRIED_CONTAINER, "test.Carried", null);
        SlotScreenSession workspace = new SlotScreenSession(SlotSessionKind.EXTERNAL_CONTAINER, "test.Workspace", null);

        assertTrue(carried.carriedOnlyMode());
        assertFalse(carried.recordsRecentLoot());
        assertFalse(workspace.carriedOnlyMode());
        assertTrue(workspace.recordsRecentLoot());
        assertTrue(carried.descriptor().carriedOnlyMode());
        assertFalse(workspace.descriptor().carriedOnlyMode());
        assertEquals(SlotSessionMode.CARRIED_ONLY, carried.descriptor().mode());
        assertEquals(SlotSessionMode.DUAL_PANE, workspace.descriptor().mode());
    }

    @Test
    void slotOwnedModesRemainExplicit() {
        assertTrue(new SlotScreenSession(SlotSessionKind.SLOT_CARRIED, "slot.carried", null).slotOwned());
        assertTrue(new SlotScreenSession(SlotSessionKind.SLOT_WORKSPACE, "slot.workspace", null).slotOwned());
        assertFalse(new SlotScreenSession(SlotSessionKind.PLAYER_INVENTORY, "inventory", null).slotOwned());
        assertTrue(new SlotScreenSession(SlotSessionKind.SLOT_CARRIED, "slot.carried", null).descriptor().slotOwned());
        assertTrue(new SlotScreenSession(SlotSessionKind.SLOT_WORKSPACE, "slot.workspace", null).descriptor().slotOwned());
        assertFalse(new SlotScreenSession(SlotSessionKind.PLAYER_INVENTORY, "inventory", null).descriptor().slotOwned());
    }
}
