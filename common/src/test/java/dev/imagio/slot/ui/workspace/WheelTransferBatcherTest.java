package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WheelTransferBatcherTest {
    @Test
    void mergesSameIdentityAndActionIntoOneCountedIntent() {
        WheelTransferBatcher batcher = new WheelTransferBatcher();
        SlotWorkspaceViewModel.IdentityRef stone = identity("minecraft:stone");

        assertNull(batcher.enqueue(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, stone, 3, "taking Stone"));
        assertNull(batcher.enqueue(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, stone, 4, "taking Stone"));

        WheelTransferBatcher.Pending pending = batcher.flush();
        assertEquals(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, pending.action());
        assertEquals(stone, pending.identity());
        assertEquals(7, pending.count());
        assertNull(batcher.flush());
    }

    @Test
    void flushesExistingIntentWhenActionChanges() {
        WheelTransferBatcher batcher = new WheelTransferBatcher();
        SlotWorkspaceViewModel.IdentityRef stone = identity("minecraft:stone");

        assertNull(batcher.enqueue(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, stone, 2, "taking Stone"));
        WheelTransferBatcher.Pending flushed = batcher.enqueue(
                WorkspaceActionId.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST,
                stone,
                1,
                "depositing Stone");

        assertEquals(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, flushed.action());
        assertEquals(2, flushed.count());
        assertEquals(WorkspaceActionId.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST, batcher.flush().action());
    }

    @Test
    void idleFlushWaitsForQuietTicks() {
        WheelTransferBatcher batcher = new WheelTransferBatcher(2);
        SlotWorkspaceViewModel.IdentityRef stone = identity("minecraft:stone");

        assertNull(batcher.enqueue(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, stone, 1, "taking Stone"));
        assertNull(batcher.flushIfIdle());

        WheelTransferBatcher.Pending pending = batcher.flushIfIdle();
        assertEquals(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, pending.action());
        assertEquals(1, pending.count());
        assertNull(batcher.flushIfIdle());
    }

    @Test
    void enqueueResetsIdleFlushWindow() {
        WheelTransferBatcher batcher = new WheelTransferBatcher(2);
        SlotWorkspaceViewModel.IdentityRef stone = identity("minecraft:stone");

        assertNull(batcher.enqueue(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, stone, 1, "taking Stone"));
        assertNull(batcher.flushIfIdle());
        assertNull(batcher.enqueue(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, stone, 2, "taking Stone"));
        assertNull(batcher.flushIfIdle());

        WheelTransferBatcher.Pending pending = batcher.flushIfIdle();
        assertEquals(3, pending.count());
    }

    private static SlotWorkspaceViewModel.IdentityRef identity(String itemId) {
        return new SlotWorkspaceViewModel.IdentityRef(itemId, ItemComparisonMode.ITEM_ID.name(), "");
    }
}
