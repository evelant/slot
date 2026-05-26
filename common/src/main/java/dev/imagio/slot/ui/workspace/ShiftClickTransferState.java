package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

/**
 * Client-side memory for the "repeat take while Shift stays held" gesture.
 * The state stores only UI intent; the server still resolves all inventory
 * authority and desired-count limits.
 */
public final class ShiftClickTransferState {
    private SlotWorkspaceViewModel.IdentityRef takeIdentity;

    public void observeShiftDown(boolean shiftDown) {
        if (!shiftDown) {
            reset();
        }
    }

    public void reset() {
        takeIdentity = null;
    }

    public boolean continuingTake(SlotWorkspaceViewModel.IdentityRef identity, boolean shiftDown) {
        if (!shiftDown || identity == null || takeIdentity == null) {
            return false;
        }
        return takeIdentity.equals(identity);
    }

    public SlotWorkspaceViewModel.IdentityRef takeIdentity(boolean shiftDown) {
        observeShiftDown(shiftDown);
        return shiftDown ? takeIdentity : null;
    }

    public void record(
            WallCardTransferGesturePolicy.Decision decision,
            SlotWorkspaceViewModel.IdentityRef identity,
            boolean shiftDown
    ) {
        observeShiftDown(shiftDown);
        if (!shiftDown || decision == null || identity == null) {
            return;
        }
        switch (decision.action()) {
            case TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY, TAKE_STACK_BY_IDENTITY, TAKE_ITEMS_BY_IDENTITY ->
                    takeIdentity = identity;
            case DEPOSIT_HOME_TO_LINKED_CHEST, DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST, CROSS_SURFACE_QUICK_MOVE -> reset();
            default -> {
            }
        }
    }
}
