package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.network.CursorTransferRequester;

public final class CursorTransferService {
    private static final CursorRequester DEFAULT_REQUESTER = new CursorRequester() {
        @Override
        public boolean requestPickupMatching(int containerId, InventoryPane pane, ItemIdentity identity, CursorTransferPayload.Mode mode) {
            return CursorTransferRequester.requestPickupMatching(containerId, pane, identity, mode);
        }

        @Override
        public boolean requestDropCarried(int containerId, InventoryPane pane, boolean singleItem) {
            return CursorTransferRequester.requestDropCarried(containerId, pane, singleItem);
        }

        @Override
        public boolean requestTrashCarried(int containerId, boolean singleItem) {
            return CursorTransferRequester.requestTrashCarried(containerId, singleItem);
        }
    };

    private CursorTransferService() {
    }

    public static CursorTransferOutcome requestPickup(
            int containerId,
            InventoryPane pane,
            ItemIdentity identity,
            CursorTransferPayload.Mode mode
    ) {
        return requestPickup(containerId, pane, identity, mode, DEFAULT_REQUESTER);
    }

    public static CursorTransferOutcome requestPickup(
            int containerId,
            InventoryPane pane,
            ItemIdentity identity,
            CursorTransferPayload.Mode mode,
            CursorRequester requester
    ) {
        if (containerId < 0 || pane == null || identity == null || mode == null || requester == null) {
            return CursorTransferOutcome.notRequested();
        }

        boolean requested = requester.requestPickupMatching(containerId, pane, identity, mode);
        return requested
                ? new CursorTransferOutcome(true, pane, mode, pane == InventoryPane.CARRIED)
                : CursorTransferOutcome.notRequested();
    }

    public static CursorTransferOutcome requestDrop(
            int containerId,
            InventoryPane pane,
            boolean singleItem
    ) {
        return requestDrop(containerId, pane, singleItem, DEFAULT_REQUESTER::requestDropCarried);
    }

    public static CursorTransferOutcome requestDrop(
            int containerId,
            InventoryPane pane,
            boolean singleItem,
            DropRequester requester
    ) {
        if (containerId < 0 || pane == null || requester == null) {
            return CursorTransferOutcome.notRequested();
        }

        CursorTransferPayload.Mode mode = singleItem ? CursorTransferPayload.Mode.ONE : CursorTransferPayload.Mode.STACK;
        boolean requested = requester.requestDropCarried(containerId, pane, singleItem);
        return requested
                ? new CursorTransferOutcome(true, pane, mode, pane == InventoryPane.CARRIED)
                : CursorTransferOutcome.notRequested();
    }

    public static CursorTransferOutcome requestDrop(
            int containerId,
            InventoryPane pane,
            boolean singleItem,
            CursorRequester requester
    ) {
        return requestDrop(containerId, pane, singleItem, requester == null ? null : requester::requestDropCarried);
    }

    public static CursorTransferOutcome requestTrash(
            int containerId,
            boolean singleItem
    ) {
        return requestTrash(containerId, singleItem, DEFAULT_REQUESTER::requestTrashCarried);
    }

    public static CursorTransferOutcome requestTrash(
            int containerId,
            boolean singleItem,
            TrashRequester requester
    ) {
        if (containerId < 0 || requester == null) {
            return CursorTransferOutcome.notRequested();
        }

        CursorTransferPayload.Mode mode = singleItem ? CursorTransferPayload.Mode.ONE : CursorTransferPayload.Mode.STACK;
        boolean requested = requester.requestTrashCarried(containerId, singleItem);
        return requested
                ? new CursorTransferOutcome(true, InventoryPane.CARRIED, mode, true)
                : CursorTransferOutcome.notRequested();
    }

    public static CursorTransferOutcome requestTrash(
            int containerId,
            boolean singleItem,
            CursorRequester requester
    ) {
        return requestTrash(containerId, singleItem, requester == null ? null : requester::requestTrashCarried);
    }

    @FunctionalInterface
    public interface CursorRequester {
        boolean requestPickupMatching(int containerId, InventoryPane pane, ItemIdentity identity, CursorTransferPayload.Mode mode);

        default boolean requestDropCarried(int containerId, InventoryPane pane, boolean singleItem) {
            return false;
        }

        default boolean requestTrashCarried(int containerId, boolean singleItem) {
            return false;
        }
    }

    @FunctionalInterface
    public interface DropRequester {
        boolean requestDropCarried(int containerId, InventoryPane pane, boolean singleItem);
    }

    @FunctionalInterface
    public interface TrashRequester {
        boolean requestTrashCarried(int containerId, boolean singleItem);
    }

    public record CursorTransferOutcome(
            boolean requested,
            InventoryPane pane,
            CursorTransferPayload.Mode mode,
            boolean suppressPositiveDeltas
    ) {
        private static final CursorTransferOutcome NOT_REQUESTED = new CursorTransferOutcome(false, null, null, false);

        public static CursorTransferOutcome notRequested() {
            return NOT_REQUESTED;
        }

        public String modeLabel() {
            if (mode == null) {
                return "";
            }
            return switch (mode) {
                case ONE -> "ONE";
                case HALF -> "HALF";
                case STACK -> "STACK";
            };
        }

        public String pickupFeedbackKey() {
            if (mode == null) {
                return "";
            }
            return switch (mode) {
                case ONE -> "slot.screen.action.pickup_one.requested";
                case HALF -> "slot.screen.action.pickup_half.requested";
                case STACK -> "slot.screen.action.pickup_stack.requested";
            };
        }

        public String dropFeedbackKey() {
            if (mode == null) {
                return "";
            }
            return mode == CursorTransferPayload.Mode.ONE
                    ? "slot.screen.action.drop_one.requested"
                    : "slot.screen.action.drop_stack.requested";
        }

        public String trashFeedbackKey() {
            if (mode == null) {
                return "";
            }
            return mode == CursorTransferPayload.Mode.ONE
                    ? "slot.screen.action.trash_one.requested"
                    : "slot.screen.action.trash_stack.requested";
        }
    }
}
