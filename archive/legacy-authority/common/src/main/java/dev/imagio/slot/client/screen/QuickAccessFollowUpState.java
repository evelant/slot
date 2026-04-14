package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.operation.ActionStatus;

import java.util.List;
import java.util.Objects;

public final class QuickAccessFollowUpState {
    private static final long REQUEST_TIMEOUT_NANOS = 60_000_000_000L;
    private static final long CONFIRMED_FOLLOW_UP_TIMEOUT_NANOS = 3_000_000_000L;

    private QuickAccessFollowUpState() {
    }

    public static void recordUseOffhand(
            ActionRequestId requestId,
            String routingKey,
            Object expectedMenu,
            ItemIdentity identity
    ) {
        ActionSessionStore.recordUseOffhand(requestId, routingKey, expectedMenu, identity);
    }

    public static void recordDropMenuSlot(
            ActionRequestId requestId,
            String routingKey,
            Object expectedMenu,
            ItemIdentity identity,
            int targetMenuSlot
    ) {
        ActionSessionStore.recordDropMenuSlot(requestId, routingKey, expectedMenu, identity, targetMenuSlot);
    }

    public static boolean handleTransferOutcome(ActionRequestId requestId, ActionStatus status) {
        return ActionSessionStore.handleFollowUpTransferOutcome(requestId, status);
    }

    public static boolean hasPendingIdentity(ItemIdentity identity) {
        return ActionSessionStore.hasPendingFollowUpIdentity(identity);
    }

    public static boolean hasPendingActions() {
        return ActionSessionStore.hasPendingFollowUps();
    }

    static List<PendingAction> readyActions() {
        return ActionSessionStore.readyFollowUps();
    }

    public static void completeApplied(ActionRequestId requestId) {
        ActionSessionStore.completeFollowUpApplied(requestId);
    }

    public static void completeFailed(ActionRequestId requestId) {
        ActionSessionStore.completeFollowUpFailed(requestId);
    }

    public static void clear() {
        ActionSessionStore.clear();
    }

    enum ActionType {
        USE_HAND,
        USE_OFFHAND,
        DROP_MENU_SLOT
    }

    public record PendingAction(
            String requestId,
            String routingKey,
            ActionType type,
            Object expectedMenu,
            ItemIdentity identity,
            int targetMenuSlot,
            long createdAtNanos,
            long confirmedAtNanos
    ) {
        public PendingAction {
            Objects.requireNonNull(requestId, "requestId");
            Objects.requireNonNull(routingKey, "routingKey");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(identity, "identity");
        }

        public boolean confirmed() {
            return confirmedAtNanos >= 0L;
        }

        private boolean expired(long now) {
            if (!confirmed()) {
                return now - createdAtNanos > REQUEST_TIMEOUT_NANOS;
            }
            return now - confirmedAtNanos > CONFIRMED_FOLLOW_UP_TIMEOUT_NANOS;
        }

        private PendingAction confirmed(long confirmedAtNanos) {
            return new PendingAction(requestId, routingKey, type, expectedMenu, identity, targetMenuSlot, createdAtNanos, confirmedAtNanos);
        }
    }
}
