package dev.imagio.slot.client.screen;

import dev.imagio.slot.action.session.ActionSessionResult;
import dev.imagio.slot.action.session.ActionSessionStore.HistoryReplayDirection;
import dev.imagio.slot.action.session.ActionSessionStore.HistorySettlement;
import dev.imagio.slot.action.session.ActionSessionStore.HistoryTransferDirection;
import dev.imagio.slot.action.session.ActionSessionStore.RequestedHistoryTransfer;
import dev.imagio.slot.action.session.PendingQuickAccessFollowUp;
import dev.imagio.slot.action.session.PublishedActionOutcome;
import dev.imagio.slot.action.session.QuickAccessFollowUpActionType;
import dev.imagio.slot.action.session.QuickAccessRequestedTarget;
import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.operation.ActionStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ActionSessionStore {
    private static String boundRoutingKey = "";

    private ActionSessionStore() {
    }

    public static void bindContext(String key) {
        boundRoutingKey = key == null ? "" : key;
    }

    public static void clear() {
        boundRoutingKey = "";
        dev.imagio.slot.action.session.ActionSessionStore.clear();
    }

    public static void recordRequest(ActionRequest request) {
        dev.imagio.slot.action.session.ActionSessionStore.recordRequest(currentHostId(), request, boundRoutingKey);
    }

    public static void recordRequest(ActionRequest request, String routingKey) {
        dev.imagio.slot.action.session.ActionSessionStore.recordRequest(currentHostId(), request, routingKey);
    }

    public static String resolveContextKey(ActionRequestId requestId, String fallbackKey) {
        return dev.imagio.slot.action.session.ActionSessionStore.resolveContextKey(requestId, fallbackKey);
    }

    public static void recordQuickAccessTargets(List<QuickAccessMutationResult.RequestedChange> pendingChanges) {
        List<QuickAccessRequestedTarget> targets = pendingChanges == null
                ? List.of()
                : pendingChanges.stream()
                .map(change -> new QuickAccessRequestedTarget(change.quickAccessIndex(), change.identity(), change.requestId()))
                .toList();
        dev.imagio.slot.action.session.ActionSessionStore.recordQuickAccessTargets(targets);
    }

    public static boolean isPendingQuickAccessIndex(int quickAccessIndex) {
        return dev.imagio.slot.action.session.ActionSessionStore.isPendingQuickAccessIndex(currentHostKey(), quickAccessIndex);
    }

    public static boolean hasPendingQuickAccessTargets() {
        return dev.imagio.slot.action.session.ActionSessionStore.hasPendingQuickAccessTargets(currentHostKey());
    }

    public static void recordUseOffhand(
            ActionRequestId requestId,
            String routingKey,
            Object expectedMenu,
            ItemIdentity identity
    ) {
        dev.imagio.slot.action.session.ActionSessionStore.recordUseOffhand(requestId, routingKey, expectedMenu, identity);
    }

    public static void recordDropMenuSlot(
            ActionRequestId requestId,
            String routingKey,
            Object expectedMenu,
            ItemIdentity identity,
            int targetMenuSlot
    ) {
        dev.imagio.slot.action.session.ActionSessionStore.recordDropMenuSlot(
                requestId,
                routingKey,
                expectedMenu,
                identity,
                targetMenuSlot
        );
    }

    public static boolean handleFollowUpTransferOutcome(ActionRequestId requestId, ActionStatus status) {
        return dev.imagio.slot.action.session.ActionSessionStore.handleFollowUpTransferOutcome(requestId, status);
    }

    public static List<QuickAccessFollowUpState.PendingAction> readyFollowUps() {
        return dev.imagio.slot.action.session.ActionSessionStore.readyFollowUps(currentHostKey()).stream()
                .map(ActionSessionStore::toPendingAction)
                .toList();
    }

    public static void completeFollowUpApplied(ActionRequestId requestId) {
        dev.imagio.slot.action.session.ActionSessionStore.completeFollowUpApplied(requestId);
    }

    public static void completeFollowUpFailed(ActionRequestId requestId) {
        dev.imagio.slot.action.session.ActionSessionStore.completeFollowUpFailed(requestId);
    }

    public static boolean hasPendingFollowUpIdentity(ItemIdentity identity) {
        return dev.imagio.slot.action.session.ActionSessionStore.hasPendingFollowUpIdentity(currentHostKey(), identity);
    }

    public static boolean hasPendingFollowUps() {
        return dev.imagio.slot.action.session.ActionSessionStore.hasPendingFollowUps(currentHostKey());
    }

    public static void completeQuickAccessRequest(ActionRequestId requestId) {
        dev.imagio.slot.action.session.ActionSessionStore.completeQuickAccessRequest(requestId);
    }

    public static void completeRequest(ActionRequestId requestId) {
        dev.imagio.slot.action.session.ActionSessionStore.completeRequest(requestId);
    }

    public static void publishOutcome(String routingKey, ActionRequestId requestId, SlotActionResult result) {
        dev.imagio.slot.action.session.ActionSessionStore.publishOutcome(
                routingKey,
                requestId,
                toActionSessionResult(result)
        );
    }

    public static List<SlotActionOutcomeState.PublishedOutcome> pollAllOutcomes(String routingKey) {
        return dev.imagio.slot.action.session.ActionSessionStore.pollAllOutcomes(routingKey).stream()
                .map(ActionSessionStore::toPublishedOutcome)
                .toList();
    }

    public static List<SlotActionOutcomeState.PublishedOutcome> pollOutcomes(String routingKey, Set<String> requestIds) {
        return dev.imagio.slot.action.session.ActionSessionStore.pollOutcomes(routingKey, requestIds).stream()
                .map(ActionSessionStore::toPublishedOutcome)
                .toList();
    }

    public static void clearDeferredHistoryState() {
        dev.imagio.slot.action.session.ActionSessionStore.clearDeferredHistoryState();
    }

    public static boolean hasPendingHistoryMutation(String contextKey) {
        return dev.imagio.slot.action.session.ActionSessionStore.hasPendingHistoryMutation(currentHostKey(), contextKey);
    }

    public static void recordDeferredHistoryTransferRequest(String contextKey, ActionRequest request) {
        dev.imagio.slot.action.session.ActionSessionStore.recordDeferredHistoryTransferRequest(contextKey, request);
    }

    public static void registerDeferredQuickAccessHistoryRecord(
            String contextKey,
            HotbarLoadoutCapture before,
            HotbarLoadoutCapture localAfter,
            List<QuickAccessMutationResult.RequestedChange> pendingChanges
    ) {
        dev.imagio.slot.action.session.ActionSessionStore.registerDeferredQuickAccessHistoryRecord(
                contextKey,
                before,
                localAfter,
                toQuickAccessTargets(pendingChanges)
        );
    }

    public static void registerDeferredQuickAccessHistoryTransition(
            String contextKey,
            HistoryReplayDirection direction,
            HotbarLoadoutCapture actionBefore,
            HotbarLoadoutCapture actionAfter,
            HotbarLoadoutCapture startBefore,
            HotbarLoadoutCapture localAfter,
            List<QuickAccessMutationResult.RequestedChange> pendingChanges
    ) {
        dev.imagio.slot.action.session.ActionSessionStore.registerDeferredQuickAccessHistoryTransition(
                contextKey,
                direction,
                actionBefore,
                actionAfter,
                startBefore,
                localAfter,
                toQuickAccessTargets(pendingChanges)
        );
    }

    public static void registerDeferredTransferHistoryTransition(
            String contextKey,
            HistoryReplayDirection direction,
            HistoryTransferDirection actionDirection,
            List<RequestedHistoryTransfer> requestedTransfers
    ) {
        dev.imagio.slot.action.session.ActionSessionStore.registerDeferredTransferHistoryTransition(
                contextKey,
                direction,
                actionDirection,
                requestedTransfers
        );
    }

    public static void recordDeferredHistoryOutcome(ActionRequestId requestId, ActionStatus status, int affectedCount) {
        dev.imagio.slot.action.session.ActionSessionStore.recordDeferredHistoryOutcome(requestId, status, affectedCount);
    }

    public static List<HistorySettlement> drainHistorySettlements() {
        return dev.imagio.slot.action.session.ActionSessionStore.drainHistorySettlements();
    }

    private static QuickAccessFollowUpState.PendingAction toPendingAction(PendingQuickAccessFollowUp pendingAction) {
        return new QuickAccessFollowUpState.PendingAction(
                pendingAction.requestId(),
                pendingAction.routingKey(),
                toActionType(pendingAction.type()),
                pendingAction.expectedMenu(),
                pendingAction.identity(),
                pendingAction.targetMenuSlot(),
                pendingAction.createdAtNanos(),
                pendingAction.confirmedAtNanos()
        );
    }

    private static SlotActionOutcomeState.PublishedOutcome toPublishedOutcome(PublishedActionOutcome outcome) {
        return new SlotActionOutcomeState.PublishedOutcome(
                outcome.routingKey(),
                outcome.requestId(),
                fromActionSessionResult(outcome.result()),
                outcome.publishedAtNanos()
        );
    }

    private static ActionSessionResult toActionSessionResult(SlotActionResult result) {
        if (result == null) {
            return ActionSessionResult.NONE;
        }
        return new ActionSessionResult(
                switch (result.status()) {
                    case NONE -> ActionSessionResult.Status.NONE;
                    case APPLIED -> ActionSessionResult.Status.APPLIED;
                    case REQUESTED -> ActionSessionResult.Status.REQUESTED;
                    case BLOCKED -> ActionSessionResult.Status.BLOCKED;
                    case FAILED -> ActionSessionResult.Status.FAILED;
                },
                result.message(),
                result.transferSyncExpected()
        );
    }

    private static SlotActionResult fromActionSessionResult(ActionSessionResult result) {
        if (result == null) {
            return SlotActionResult.NONE;
        }
        return new SlotActionResult(
                switch (result.status()) {
                    case NONE -> SlotActionResult.Status.NONE;
                    case APPLIED -> SlotActionResult.Status.APPLIED;
                    case REQUESTED -> SlotActionResult.Status.REQUESTED;
                    case BLOCKED -> SlotActionResult.Status.BLOCKED;
                    case FAILED -> SlotActionResult.Status.FAILED;
                },
                result.message(),
                result.transferSyncExpected()
        );
    }

    private static QuickAccessFollowUpState.ActionType toActionType(QuickAccessFollowUpActionType actionType) {
        return switch (actionType) {
            case USE_HAND -> QuickAccessFollowUpState.ActionType.USE_HAND;
            case USE_OFFHAND -> QuickAccessFollowUpState.ActionType.USE_OFFHAND;
            case DROP_MENU_SLOT -> QuickAccessFollowUpState.ActionType.DROP_MENU_SLOT;
        };
    }

    private static List<QuickAccessRequestedTarget> toQuickAccessTargets(List<QuickAccessMutationResult.RequestedChange> pendingChanges) {
        return pendingChanges == null
                ? List.of()
                : pendingChanges.stream()
                .map(change -> new QuickAccessRequestedTarget(change.quickAccessIndex(), change.identity(), change.requestId()))
                .toList();
    }

    private static UUID currentHostId() {
        String hostKey = currentHostKey();
        if (hostKey.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(hostKey);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String currentHostKey() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            if (minecraft == null) {
                return "";
            }

            Field playerField = minecraftClass.getField("player");
            Field screenField = minecraftClass.getField("screen");
            Object player = playerField.get(minecraft);
            Object screen = screenField.get(minecraft);
            if (player == null) {
                return "";
            }

            Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
            Class<?> localPlayerClass = Class.forName("net.minecraft.client.player.LocalPlayer");
            Class<?> resolverClass = Class.forName("dev.imagio.slot.client.session.SlotScreenSessionResolver");
            Method resolveMethod = resolverClass.getMethod("resolve", screenClass, localPlayerClass);
            Object session = resolveMethod.invoke(null, screen, player);
            if (session == null) {
                return "";
            }

            Method hostMethod = session.getClass().getMethod("host");
            Object host = hostMethod.invoke(session);
            if (host == null) {
                return "";
            }

            Method hostIdMethod = host.getClass().getMethod("hostId");
            Object hostId = hostIdMethod.invoke(host);
            return hostId instanceof UUID uuid ? uuid.toString() : "";
        } catch (Throwable ignored) {
            return "";
        }
    }
}
