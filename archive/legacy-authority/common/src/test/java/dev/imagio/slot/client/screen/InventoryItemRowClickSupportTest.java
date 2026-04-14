package dev.imagio.slot.client.screen;

import org.junit.jupiter.api.Test;

import static dev.imagio.slot.client.screen.InventoryItemRowClickSupport.ClickIntent;
import static dev.imagio.slot.client.screen.InventoryItemRowClickSupport.EmptyPrimaryClick;
import static dev.imagio.slot.client.screen.InventoryItemRowSupport.ClickTarget;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryItemRowClickSupportTest {
    @Test
    void actionColumnsWinBeforeTransferSemantics() {
        assertEquals(
                ClickIntent.OPEN_ICON_MENU,
                resolve(0, ClickTarget.ICON, false, false, true, true, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.EDIT_DESIRED_COUNT,
                resolve(0, ClickTarget.DESIRED_COUNT, true, true, false, false, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.OPEN_ACTION_MENU,
                resolve(1, ClickTarget.ACTION, false, false, true, false, 12, EmptyPrimaryClick.CONSUME)
        );
    }

    @Test
    void carriedCursorDropsUnlessRightClickingMatchingRow() {
        assertEquals(
                ClickIntent.PICKUP_HALF,
                resolve(1, ClickTarget.BODY, false, false, true, true, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.DROP_CURSOR,
                resolve(1, ClickTarget.BODY, false, false, true, false, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.DROP_CURSOR,
                resolve(0, ClickTarget.BODY, false, false, true, true, 12, EmptyPrimaryClick.CONSUME)
        );
    }

    @Test
    void modifiersResolveBeforePrimaryPickup() {
        assertEquals(
                ClickIntent.MOVE_ALL,
                resolve(0, ClickTarget.BODY, true, true, false, false, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.MOVE_ALL,
                resolve(0, ClickTarget.BODY, true, false, false, false, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.MOVE_ONE,
                resolve(0, ClickTarget.BODY, false, true, false, false, 12, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.PICKUP_STACK,
                resolve(0, ClickTarget.BODY, false, false, false, false, 12, EmptyPrimaryClick.CONSUME)
        );
    }

    @Test
    void emptyPrimaryClicksFollowScreenPolicy() {
        assertEquals(
                ClickIntent.START_HOTBAR_DRAG,
                resolve(0, ClickTarget.BODY, false, false, false, false, 0, EmptyPrimaryClick.START_HOTBAR_DRAG)
        );
        assertEquals(
                ClickIntent.CONSUME,
                resolve(0, ClickTarget.BODY, false, false, false, false, 0, EmptyPrimaryClick.CONSUME)
        );
        assertEquals(
                ClickIntent.IGNORED,
                resolve(1, ClickTarget.BODY, false, false, false, false, 0, EmptyPrimaryClick.CONSUME)
        );
    }

    @Test
    void executeFallsBackToHotbarDragWhenStackPickupFails() {
        int[] dragCount = {0};
        boolean handled = InventoryItemRowClickSupport.execute(
                ClickIntent.PICKUP_STACK,
                actions(
                        () -> false,
                        () -> dragCount[0]++
                )
        );

        assertTrue(handled);
        assertEquals(1, dragCount[0]);
    }

    @Test
    void executeDoesNotFallbackWhenStackPickupSucceeds() {
        int[] dragCount = {0};
        boolean handled = InventoryItemRowClickSupport.execute(
                ClickIntent.PICKUP_STACK,
                actions(
                        () -> true,
                        () -> dragCount[0]++
                )
        );

        assertTrue(handled);
        assertEquals(0, dragCount[0]);
    }

    @Test
    void executeIgnoredReturnsFalse() {
        assertFalse(InventoryItemRowClickSupport.execute(ClickIntent.IGNORED, actions(() -> true, () -> {
        })));
    }

    private static ClickIntent resolve(
            int button,
            ClickTarget target,
            boolean shiftDown,
            boolean controlDown,
            boolean cursorCarryingStack,
            boolean cursorMatchesRow,
            int rowCount,
            EmptyPrimaryClick emptyPrimaryClick
    ) {
        return InventoryItemRowClickSupport.resolve(
                button,
                target,
                shiftDown,
                controlDown,
                cursorCarryingStack,
                cursorMatchesRow,
                rowCount,
                emptyPrimaryClick
        );
    }

    private static InventoryItemRowClickSupport.RowClickActions actions(
            java.util.function.BooleanSupplier pickupStack,
            Runnable startHotbarDrag
    ) {
        return new InventoryItemRowClickSupport.RowClickActions(
                null,
                null,
                null,
                () -> false,
                () -> false,
                null,
                null,
                pickupStack,
                startHotbarDrag
        );
    }
}
