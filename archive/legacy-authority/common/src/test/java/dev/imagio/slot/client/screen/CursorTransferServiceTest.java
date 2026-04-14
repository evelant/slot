package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CursorTransferServiceTest {
    @Test
    void dropIntoOpenContainerDoesNotSuppressCarriedRecents() {
        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestDrop(
                7,
                InventoryPane.OPEN_CONTAINER,
                false,
                (containerId, pane, singleItem) -> true
        );

        assertTrue(outcome.requested());
        assertFalse(outcome.suppressPositiveDeltas());
        assertEquals("STACK", outcome.modeLabel());
        assertEquals("slot.screen.action.drop_stack.requested", outcome.dropFeedbackKey());
    }

    @Test
    void dropIntoCarriedSuppressesPositiveDeltas() {
        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestDrop(
                7,
                InventoryPane.CARRIED,
                true,
                (containerId, pane, singleItem) -> true
        );

        assertTrue(outcome.requested());
        assertTrue(outcome.suppressPositiveDeltas());
        assertEquals("ONE", outcome.modeLabel());
        assertEquals("slot.screen.action.drop_one.requested", outcome.dropFeedbackKey());
    }

    @Test
    void trashAlwaysSuppressesPositiveDeltas() {
        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestTrash(
                7,
                true,
                (containerId, singleItem) -> true
        );

        assertTrue(outcome.requested());
        assertTrue(outcome.suppressPositiveDeltas());
        assertEquals("ONE", outcome.modeLabel());
        assertEquals("slot.screen.action.trash_one.requested", outcome.trashFeedbackKey());
    }

    @Test
    void rejectedRequestReturnsNotRequestedOutcome() {
        CursorTransferService.CursorTransferOutcome outcome = CursorTransferService.requestTrash(
                7,
                true,
                (containerId, singleItem) -> false
        );

        assertFalse(outcome.requested());
        assertEquals("", outcome.modeLabel());
    }
}
