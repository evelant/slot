package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.client.screen.container.MenuSlotId;

public final class BackpackTransferRequester {
    private BackpackTransferRequester() {
    }

    public static boolean requestExternalToCarried(int containerId, ItemIdentity identity, BackpackTransferPayload.Mode mode) {
        if (identity == null || identity.itemId().isBlank()) {
            return false;
        }
        return ActionRequestRequester.request(
                BackpackTransferActionRequests.externalToCarried(
                        containerId,
                        ActionRequestClientContext.currentSessionFingerprint(containerId),
                        identity,
                        mode
                )
        );
    }

    public static boolean requestExternalToCarried(int containerId, ItemIdentity identity, int count) {
        return requestExternalToCarriedId(containerId, identity, count).present();
    }

    public static ActionRequestId requestExternalToCarriedId(int containerId, ItemIdentity identity, int count) {
        if (identity == null || identity.itemId().isBlank()) {
            return ActionRequestId.none();
        }
        ActionRequest request =
                BackpackTransferActionRequests.externalToCarried(
                        containerId,
                        ActionRequestClientContext.currentSessionFingerprint(containerId),
                        identity,
                        count
                );
        return ActionRequestRequester.request(request) ? request.requestId() : ActionRequestId.none();
    }

    public static boolean requestMenuToExternal(int containerId, int menuSlot, BackpackTransferPayload.Mode mode) {
        if (menuSlot < 0) {
            return false;
        }
        return ActionRequestRequester.request(
                BackpackTransferActionRequests.menuToExternal(
                        containerId,
                        ActionRequestClientContext.currentSessionFingerprint(containerId),
                        menuSlot,
                        mode
                )
        );
    }

    public static boolean requestMenuToExternal(int containerId, MenuSlotId menuSlot, BackpackTransferPayload.Mode mode) {
        if (menuSlot == null || !menuSlot.isValid()) {
            return false;
        }
        return requestMenuToExternal(containerId, menuSlot.value(), mode);
    }

    public static boolean requestBackpackToExternal(int containerId, ItemIdentity identity, BackpackTransferPayload.Mode mode) {
        if (identity == null || identity.itemId().isBlank()) {
            return false;
        }
        return ActionRequestRequester.request(
                BackpackTransferActionRequests.backpackToExternal(
                        containerId,
                        ActionRequestClientContext.currentSessionFingerprint(containerId),
                        identity,
                        mode
                )
        );
    }

    public static boolean requestCarriedToExternal(int containerId, ItemIdentity identity, int count) {
        return requestCarriedToExternalId(containerId, identity, count).present();
    }

    public static ActionRequestId requestCarriedToExternalId(int containerId, ItemIdentity identity, int count) {
        if (identity == null || identity.itemId().isBlank() || count <= 0) {
            return ActionRequestId.none();
        }
        ActionRequest request =
                BackpackTransferActionRequests.carriedToExternal(
                        containerId,
                        ActionRequestClientContext.currentSessionFingerprint(containerId),
                        identity,
                        count
                );
        return ActionRequestRequester.request(request) ? request.requestId() : ActionRequestId.none();
    }

    public static boolean requestBackpackToMenu(int containerId, ItemIdentity identity, int menuSlot) {
        return requestBackpackToMenu(containerId, identity, menuSlot, 0);
    }

    public static boolean requestBackpackToMenu(int containerId, ItemIdentity identity, MenuSlotId menuSlot) {
        return requestBackpackToMenu(containerId, identity, menuSlot, 0);
    }

    public static boolean requestBackpackToMenu(int containerId, ItemIdentity identity, int menuSlot, int requestedCount) {
        return requestBackpackToMenu(containerId, identity, menuSlot, requestedCount, BackpackTransferActionRequests.TargetPolicy.FILL_ONLY);
    }

    public static boolean requestBackpackToMenuReplacingTarget(int containerId, ItemIdentity identity, int menuSlot, int requestedCount) {
        return requestBackpackToMenu(containerId, identity, menuSlot, requestedCount, BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING);
    }

    public static boolean requestBackpackToMenuReplacingTarget(int containerId, ItemIdentity identity, MenuSlotId menuSlot, int requestedCount) {
        if (menuSlot == null || !menuSlot.isValid()) {
            return false;
        }
        return requestBackpackToMenuReplacingTarget(containerId, identity, menuSlot.value(), requestedCount);
    }

    private static boolean requestBackpackToMenu(
            int containerId,
            ItemIdentity identity,
            int menuSlot,
            int requestedCount,
            BackpackTransferActionRequests.TargetPolicy targetPolicy
    ) {
        if (identity == null || identity.itemId().isBlank() || menuSlot < 0) {
            return false;
        }
        return ActionRequestRequester.request(
                BackpackTransferActionRequests.backpackToMenu(
                        containerId,
                        ActionRequestClientContext.currentSessionFingerprint(containerId),
                        identity,
                        menuSlot,
                        requestedCount,
                        targetPolicy
                )
        );
    }

    public static boolean requestBackpackToMenu(int containerId, ItemIdentity identity, MenuSlotId menuSlot, int requestedCount) {
        if (menuSlot == null || !menuSlot.isValid()) {
            return false;
        }
        return requestBackpackToMenu(containerId, identity, menuSlot.value(), requestedCount);
    }
}
