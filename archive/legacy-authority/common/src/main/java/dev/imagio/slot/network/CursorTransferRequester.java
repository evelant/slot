package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;

public final class CursorTransferRequester {
    private CursorTransferRequester() {
    }

    public static boolean requestPickupMatching(int containerId, InventoryPane pane, ItemIdentity identity, CursorTransferPayload.Mode mode) {
        if (pane == null || identity == null || mode == null) {
            return false;
        }
        return ActionRequestRequester.request(CursorTransferActionRequests.pickupMatching(
                containerId,
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                pane,
                identity,
                toRequestMode(mode)
        ));
    }

    public static boolean requestDropCarried(int containerId, InventoryPane pane, boolean singleItem) {
        if (pane == null) {
            return false;
        }
        return ActionRequestRequester.request(CursorTransferActionRequests.dropCarried(
                containerId,
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                pane,
                singleItem ? CursorTransferActionRequests.Mode.ONE : CursorTransferActionRequests.Mode.STACK
        ));
    }

    public static boolean requestDropCarriedToMenuSlot(int containerId, int targetMenuSlot, boolean singleItem) {
        if (targetMenuSlot < 0) {
            return false;
        }
        return ActionRequestRequester.request(CursorTransferActionRequests.dropCarriedToMenuSlot(
                containerId,
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                targetMenuSlot,
                singleItem ? CursorTransferActionRequests.Mode.ONE : CursorTransferActionRequests.Mode.STACK
        ));
    }

    public static boolean requestTrashCarried(int containerId, boolean singleItem) {
        return ActionRequestRequester.request(CursorTransferActionRequests.trashCarried(
                containerId,
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                singleItem ? CursorTransferActionRequests.Mode.ONE : CursorTransferActionRequests.Mode.STACK
        ));
    }

    public static boolean requestVoidMatchingCarried(int containerId, ItemIdentity identity) {
        if (identity == null) {
            return false;
        }
        return ActionRequestRequester.request(CursorTransferActionRequests.voidMatchingCarried(
                containerId,
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                identity,
                CursorTransferActionRequests.Mode.ONE
        ));
    }

    public static boolean requestVoidMatchingCarriedAll(int containerId, ItemIdentity identity) {
        if (identity == null) {
            return false;
        }
        return ActionRequestRequester.request(CursorTransferActionRequests.voidMatchingCarried(
                containerId,
                ActionRequestClientContext.currentSessionFingerprint(containerId),
                identity,
                CursorTransferActionRequests.Mode.STACK
        ));
    }

    private static CursorTransferActionRequests.Mode toRequestMode(CursorTransferPayload.Mode mode) {
        if (mode == null) {
            return CursorTransferActionRequests.Mode.STACK;
        }
        return switch (mode) {
            case ONE -> CursorTransferActionRequests.Mode.ONE;
            case HALF -> CursorTransferActionRequests.Mode.HALF;
            case STACK -> CursorTransferActionRequests.Mode.STACK;
        };
    }
}
