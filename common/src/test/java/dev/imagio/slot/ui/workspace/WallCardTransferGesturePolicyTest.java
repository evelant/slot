package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.CURSOR_CANCEL;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.ADJUST_PLAYER_DESIRED_COUNT;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.CROSS_SURFACE_QUICK_MOVE;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.DEPOSIT_HOME_TO_LINKED_CHEST;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.PICKUP_TO_CURSOR;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.STATUS;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.TAKE_ITEMS_BY_IDENTITY;
import static dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy.Action.TAKE_STACK_BY_IDENTITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WallCardTransferGesturePolicyTest {
    @Test
    void plainClickPicksUpToCursor() {
        var decision = WallCardTransferGesturePolicy.click(context(carriedItem(), 0, false, false, null, false));

        assertEquals(PICKUP_TO_CURSOR, decision.action());
        assertEquals(WallCardTransferGesturePolicy.PICKUP_MAX, decision.count());
    }

    @Test
    void clickWhileCarryingSameIdentityPicksUpMoreInsteadOfSmartDeposit() {
        SlotWorkspaceViewModel.AtlasItem item = ghostItem();

        var decision = WallCardTransferGesturePolicy.click(context(item, 0, false, false, item.identity(), true));

        assertEquals(PICKUP_TO_CURSOR, decision.action());
    }

    @Test
    void clickWhileCarryingDifferentIdentityCancelsThenPicksUp() {
        var decision = WallCardTransferGesturePolicy.click(context(
                ghostItem(),
                0,
                false,
                false,
                new SlotWorkspaceViewModel.IdentityRef("minecraft:dirt", ItemComparisonMode.ITEM_ID.name(), ""),
                true));

        assertEquals(CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR, decision.action());
    }

    @Test
    void rightClickWhileCarryingCancelsCursor() {
        var decision = WallCardTransferGesturePolicy.pointerDown(context(
                carriedItem(), 1, false, false, carriedItem().identity(), true));

        assertEquals(CURSOR_CANCEL, decision.action());
    }

    @Test
    void controlRightClickPicksUpHalfStack() {
        var decision = WallCardTransferGesturePolicy.pointerDown(context(
                carriedItem(), 1, false, true, null, false));

        assertEquals(PICKUP_TO_CURSOR, decision.action());
        assertEquals(32, decision.count());
    }

    @Test
    void shiftLeftClickCarriedItemInSidebarQuickMovesOneStackToHost() {
        var decision = WallCardTransferGesturePolicy.click(new WallCardTransferGesturePolicy.Context(
                carriedItem(), 0, true, false, null, false, true, 9, true));

        assertEquals(CROSS_SURFACE_QUICK_MOVE, decision.action());
        assertEquals(1, decision.count());
    }

    @Test
    void shiftLeftClickOutsideSidebarReportsHostRequired() {
        var decision = WallCardTransferGesturePolicy.click(context(
                carriedItem(), 0, true, false, null, false));

        assertEquals(STATUS, decision.action());
        assertEquals("open a machine or crafting interface first", decision.status());
    }

    @Test
    void shiftLeftClickGhostInSidebarReportsNotCarried() {
        var decision = WallCardTransferGesturePolicy.click(new WallCardTransferGesturePolicy.Context(
                ghostItem(), 0, true, false, null, false, true, 9, true));

        assertEquals(STATUS, decision.action());
        assertEquals("Stone not carried", decision.status());
    }

    @Test
    void shiftRightClickGhostWithNearbyChestTakesDesiredGapOrStack() {
        var decision = WallCardTransferGesturePolicy.pointerDown(context(ghostItem(), 1, true, false, null, false));

        assertEquals(TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY, decision.action());
    }

    @Test
    void shiftRightClickCarriedContinuesTakingDuringSameShiftHold() {
        var decision = WallCardTransferGesturePolicy.pointerDown(new WallCardTransferGesturePolicy.Context(
                item(true, true), 1, true, false, null, false, false, 0, true, true));

        assertEquals(TAKE_STACK_BY_IDENTITY, decision.action());
    }

    @Test
    void shiftRightClickCarriedWithUnsatisfiedWantedTargetTakesGapFromNearbyChest() {
        var decision = WallCardTransferGesturePolicy.pointerDown(context(
                item(true, true, true, 16, 0, 32),
                1,
                true,
                false,
                null,
                false));

        assertEquals(TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY, decision.action());
    }

    @Test
    void shiftRightClickCarriedDepositsStackToLinkedChest() {
        var decision = WallCardTransferGesturePolicy.pointerDown(context(carriedItem(), 1, true, false, null, false));

        assertEquals(DEPOSIT_HOME_TO_LINKED_CHEST, decision.action());
    }

    @Test
    void shiftClickTakeStateResetsWhenShiftIsReleased() {
        ShiftClickTransferState state = new ShiftClickTransferState();
        SlotWorkspaceViewModel.AtlasItem item = ghostItem();
        state.observeShiftDown(true);
        state.record(
                WallCardTransferGesturePolicy.Decision.action(TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY),
                item.identity(),
                true);
        assertEquals(true, state.continuingTake(item.identity(), true));

        state.observeShiftDown(false);

        assertEquals(false, state.continuingTake(item.identity(), true));
    }

    @Test
    void shiftRightClickCarriedWithoutNearbyChestReportsStatus() {
        var decision = WallCardTransferGesturePolicy.pointerDown(new WallCardTransferGesturePolicy.Context(
                carriedItem(), 1, true, false, null, false, false, 9, false));

        assertEquals(STATUS, decision.action());
        assertEquals("no nearby chest to push Stone", decision.status());
    }

    @Test
    void shiftRightClickGhostWithoutNearbyChestReportsStatus() {
        var decision = WallCardTransferGesturePolicy.pointerDown(new WallCardTransferGesturePolicy.Context(
                item(false, false), 1, true, false, null, false, false, 9, true));

        assertEquals(STATUS, decision.action());
        assertEquals("no nearby chest has Stone", decision.status());
    }

    @Test
    void shiftRightClickGhostFailsClosedWhenProximateCountHasNoPresenceBreakdown() {
        var decision = WallCardTransferGesturePolicy.pointerDown(context(item(false, true, false), 1, true, false, null, false));

        assertEquals(STATUS, decision.action());
        assertEquals("nearby chest data missing", decision.status());
    }

    @Test
    void shiftWheelGhostFailsClosedWhenProximateCountHasNoPresenceBreakdown() {
        var decision = WallCardTransferGesturePolicy.wheel(
                context(item(false, true, false), 0, true, false, null, false),
                1);

        assertEquals(STATUS, decision.action());
        assertEquals("nearby chest data missing", decision.status());
    }

    @Test
    void shiftRightClickGhostWithFullCarryReportsStatus() {
        var decision = WallCardTransferGesturePolicy.pointerDown(new WallCardTransferGesturePolicy.Context(
                ghostItem(), 1, true, false, null, false, false, 0, true));

        assertEquals(STATUS, decision.action());
        assertEquals("carry full - drop something first", decision.status());
    }

    @Test
    void shiftRightClickCarriedInSidebarStillDepositsToLinkedChest() {
        var decision = WallCardTransferGesturePolicy.pointerDown(new WallCardTransferGesturePolicy.Context(
                carriedItem(), 1, true, false, null, false, true, 9, true));

        assertEquals(DEPOSIT_HOME_TO_LINKED_CHEST, decision.action());
    }

    @Test
    void shiftRightClickCarriedWithOpenChestButNoProximateChestDeposits() {
        var decision = WallCardTransferGesturePolicy.pointerDown(new WallCardTransferGesturePolicy.Context(
                carriedItem(), 1, true, false, null, false, true, 9, false, true, false, false));

        assertEquals(DEPOSIT_HOME_TO_LINKED_CHEST, decision.action());
    }

    @Test
    void shiftWheelUpTakesOnePerStepFromNearbyChest() {
        var decision = WallCardTransferGesturePolicy.wheel(
                context(ghostItem(), 0, true, false, null, false),
                3);

        assertEquals(TAKE_ITEMS_BY_IDENTITY, decision.action());
        assertEquals(3, decision.count());
    }

    @Test
    void shiftWheelDownDepositsOnePerStepToLinkedChest() {
        var decision = WallCardTransferGesturePolicy.wheel(
                context(carriedItem(), 0, true, false, null, false),
                -2);

        assertEquals(DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST, decision.action());
        assertEquals(2, decision.count());
    }

    @Test
    void shiftWheelInSidebarKeepsOneAtATimeStorageSemantics() {
        var decision = WallCardTransferGesturePolicy.wheel(
                new WallCardTransferGesturePolicy.Context(
                        carriedItem(), 0, true, false, null, false, true, 9, true),
                -1);

        assertEquals(DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST, decision.action());
        assertEquals(1, decision.count());
    }

    @Test
    void shiftWheelUpInSidebarDoesNotQuickMoveIntoOpenMenu() {
        var decision = WallCardTransferGesturePolicy.wheel(
                new WallCardTransferGesturePolicy.Context(
                        carriedItem(), 0, true, false, null, false, true, 9, true),
                1);

        assertEquals(STATUS, decision.action());
        assertEquals("no nearby chest has Stone", decision.status());
    }

    @Test
    void controlWheelAdjustsDesiredCount() {
        var decision = WallCardTransferGesturePolicy.wheel(
                context(carriedItem(), 0, false, true, null, false),
                -4);

        assertEquals(ADJUST_PLAYER_DESIRED_COUNT, decision.action());
        assertEquals(-4, decision.count());
    }

    private static WallCardTransferGesturePolicy.Context context(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shift,
            boolean control,
            SlotWorkspaceViewModel.IdentityRef cursor,
            boolean cursorCarrying
    ) {
        return new WallCardTransferGesturePolicy.Context(
                item, button, shift, control, cursor, cursorCarrying, false, 9, true);
    }

    private static SlotWorkspaceViewModel.AtlasItem carriedItem() {
        return item(true, false);
    }

    private static SlotWorkspaceViewModel.AtlasItem ghostItem() {
        return item(false, true);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(boolean carried, boolean proximate) {
        return item(carried, proximate, proximate);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(boolean carried, boolean proximate, boolean includePresence) {
        return item(carried, proximate, includePresence, 16, 0, 0);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            boolean carried,
            boolean proximate,
            boolean includePresence,
            int totalCount,
            int desiredCount,
            int wantedCount
    ) {
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                "minecraft:stone",
                ItemComparisonMode.ITEM_ID.name(),
                "");
        return new SlotWorkspaceViewModel.AtlasItem(
                identity,
                new ItemStack("minecraft:stone", totalCount, 64),
                "Stone",
                totalCount,
                0,
                "building",
                false,
                false,
                carried,
                !carried,
                carried || !proximate ? 0 : 16,
                List.of(),
                proximate && includePresence
                        ? List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("00000000-0000-0000-0000-000000000001", "Chest", 16))
                        : List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                desiredCount,
                false,
                wantedCount,
                false,
                false,
                "",
                0,
                totalCount,
                SlotWorkspaceViewModel.PutAwayState.NONE);
    }
}
