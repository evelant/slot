package dev.imagio.slot.client.screen;

import dev.imagio.slot.action.session.ActionSessionStore.ConfirmedTransferRecordSettlement;
import dev.imagio.slot.action.session.ActionSessionStore.HistoryIdentityCount;
import dev.imagio.slot.action.session.ActionSessionStore.HistoryReplayDirection;
import dev.imagio.slot.action.session.ActionSessionStore.HistorySettlement;
import dev.imagio.slot.action.session.ActionSessionStore.HistoryTransferDirection;
import dev.imagio.slot.action.session.ActionSessionStore.QuickAccessRecordSettlement;
import dev.imagio.slot.action.session.ActionSessionStore.QuickAccessTransitionSettlement;
import dev.imagio.slot.action.session.ActionSessionStore.RequestedHistoryTransfer;
import dev.imagio.slot.action.session.ActionSessionStore.TransferTransitionSettlement;
import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.network.BackpackTransferRequester;
import dev.imagio.slot.operation.ActionStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class SlotUndoHistory {
    private static final int MAX_HISTORY = 16;

    private static String contextKey = "";
    private static final Deque<RecordedAction> undoStack = new ArrayDeque<>();
    private static final Deque<RecordedAction> redoStack = new ArrayDeque<>();

    private SlotUndoHistory() {
    }

    public static void bindContext(String key) {
        String resolvedKey = key == null ? "" : key;
        processDeferredHistorySettlements();
        SlotActionRequestRoutingState.bindContext(resolvedKey);
        if (Objects.equals(contextKey, resolvedKey)) {
            return;
        }

        contextKey = resolvedKey;
        ActionSessionStore.clearDeferredHistoryState();
        undoStack.clear();
        redoStack.clear();
    }

    public static void clear() {
        contextKey = "";
        SlotActionRequestRoutingState.clear();
        undoStack.clear();
        redoStack.clear();
    }

    public static boolean canUndo(String key) {
        processDeferredHistorySettlements();
        return matchesContext(key)
                && !undoStack.isEmpty()
                && !hasPendingHistoryMutation(key);
    }

    public static boolean canRedo(String key) {
        processDeferredHistorySettlements();
        return matchesContext(key)
                && !redoStack.isEmpty()
                && !hasPendingHistoryMutation(key);
    }

    public static void recordTransferRequest(ActionRequest request) {
        processDeferredHistorySettlements();
        if (request == null || !request.requestId().present() || contextKey.isBlank()) {
            return;
        }
        ActionSessionStore.recordDeferredHistoryTransferRequest(contextKey, request);
    }

    public static void recordTransferOutcome(ActionRequestId requestId, ActionStatus status, int affectedCount) {
        processDeferredHistorySettlements();
        if (requestId == null || !requestId.present()) {
            return;
        }
        ActionSessionStore.recordDeferredHistoryOutcome(requestId, status, affectedCount);
        processDeferredHistorySettlements();
    }

    public static void recordQuickAccess(String key, HotbarLoadoutCapture before, HotbarLoadoutCapture after) {
        if (before == null || after == null || before.equals(after)) {
            return;
        }

        bindContext(key);
        push(new QuickAccessAction(before, after));
    }

    static void beginQuickAccessBatch(String key) {
        bindContext(key);
    }

    static void cancelQuickAccessBatch(String key) {
        bindContext(key);
    }

    static void finishQuickAccessBatch(String key, HotbarLoadoutCapture before, QuickAccessMutationResult result) {
        recordQuickAccessMutation(key, before, result);
    }

    static void recordQuickAccessMutation(String key, HotbarLoadoutCapture before, QuickAccessMutationResult result) {
        bindContext(key);
        if (result == null || !result.changed()) {
            return;
        }

        if (!result.transferSyncExpected() || result.pendingChanges().isEmpty()) {
            recordQuickAccess(key, before, result.historyAfter());
            return;
        }
        ActionSessionStore.registerDeferredQuickAccessHistoryRecord(
                key,
                before,
                result.localHistoryAfter(),
                result.pendingChanges()
        );
    }

    public static ApplyResult undo(ActionContext context) {
        processDeferredHistorySettlements();
        if (context == null
                || !matchesContext(context.contextKey())
                || undoStack.isEmpty()
                || hasPendingHistoryMutation(context.contextKey())) {
            return ApplyResult.NONE;
        }

        RecordedAction action = undoStack.pop();
        ApplyResult result = action.undo(context);
        if (result.applied()) {
            if (result.historyDeferred()) {
                return result;
            }
            redoStack.push(action);
            trim(redoStack);
            return result;
        }

        undoStack.push(action);
        return ApplyResult.NONE;
    }

    public static ApplyResult redo(ActionContext context) {
        processDeferredHistorySettlements();
        if (context == null
                || !matchesContext(context.contextKey())
                || redoStack.isEmpty()
                || hasPendingHistoryMutation(context.contextKey())) {
            return ApplyResult.NONE;
        }

        RecordedAction action = redoStack.pop();
        ApplyResult result = action.redo(context);
        if (result.applied()) {
            if (result.historyDeferred()) {
                return result;
            }
            undoStack.push(action);
            trim(undoStack);
            return result;
        }

        redoStack.push(action);
        return ApplyResult.NONE;
    }

    private static void push(RecordedAction action) {
        undoStack.push(action);
        trim(undoStack);
        redoStack.clear();
    }

    private static void pushIfChanged(HotbarLoadoutCapture before, HotbarLoadoutCapture after) {
        if (before == null || after == null || before.equals(after)) {
            return;
        }
        push(new QuickAccessAction(before, after));
    }

    private static void pushOnStackIfChanged(
            Deque<RecordedAction> stack,
            HotbarLoadoutCapture before,
            HotbarLoadoutCapture after
    ) {
        if (stack == null || before == null || after == null || before.equals(after)) {
            return;
        }
        stack.push(new QuickAccessAction(before, after));
        trim(stack);
    }

    private static void trim(Deque<RecordedAction> stack) {
        while (stack.size() > MAX_HISTORY) {
            stack.removeLast();
        }
    }

    private static boolean matchesContext(String key) {
        return Objects.equals(contextKey, key == null ? "" : key);
    }

    private static boolean hasPendingHistoryMutation(String key) {
        return ActionSessionStore.hasPendingHistoryMutation(key);
    }

    private static void settleQuickAccessTransition(
            HistoryReplayDirection direction,
            QuickAccessAction action,
            HotbarLoadoutCapture startBefore,
            HotbarLoadoutCapture settledAfter
    ) {
        if (direction == null || action == null || startBefore == null || settledAfter == null) {
            return;
        }

        switch (direction) {
            case UNDO -> {
                pushOnStackIfChanged(undoStack, action.before(), settledAfter);
                pushOnStackIfChanged(redoStack, settledAfter, startBefore);
            }
            case REDO -> {
                pushOnStackIfChanged(undoStack, startBefore, settledAfter);
                pushOnStackIfChanged(redoStack, settledAfter, action.after());
            }
        }
    }

    private static void pushTransferOnStackIfChanged(
            Deque<RecordedAction> stack,
            TransferDirection direction,
            List<IdentityCount> moved
    ) {
        if (stack == null || direction == null || moved == null || moved.isEmpty()) {
            return;
        }

        stack.push(new TransferAction(direction, List.copyOf(moved)));
        trim(stack);
    }

    public enum TransferDirection {
        EXTERNAL_TO_CARRIED,
        CARRIED_TO_EXTERNAL;

        public TransferDirection reverse() {
            return this == EXTERNAL_TO_CARRIED ? CARRIED_TO_EXTERNAL : EXTERNAL_TO_CARRIED;
        }
    }

    public record IdentityCount(ItemIdentity identity, int count) {
        public IdentityCount {
            Objects.requireNonNull(identity, "identity");
            if (count <= 0) {
                throw new IllegalArgumentException("count must be positive");
            }
        }
    }

    public record ActionContext(
            String contextKey,
            InventoryScreenContext screenContext,
            QuickAccessService hotbarLoadoutController
    ) {
    }

    public record ApplyResult(boolean applied, boolean transferSyncExpected, boolean historyDeferred) {
        public static final ApplyResult NONE = new ApplyResult(false, false, false);
    }

    private interface RecordedAction {
        ApplyResult undo(ActionContext context);

        ApplyResult redo(ActionContext context);
    }

    private record TransferAction(
            TransferDirection direction,
            List<IdentityCount> moved
    ) implements RecordedAction {
        @Override
        public ApplyResult undo(ActionContext context) {
            return execute(direction.reverse(), context, HistoryReplayDirection.UNDO);
        }

        @Override
        public ApplyResult redo(ActionContext context) {
            return execute(direction, context, HistoryReplayDirection.REDO);
        }

        private ApplyResult execute(
                TransferDirection direction,
                ActionContext context,
                HistoryReplayDirection transitionDirection
        ) {
            if (context.screenContext() == null || context.screenContext().carriedOnly()) {
                return ApplyResult.NONE;
            }

            int containerId = context.screenContext().menu().containerId;
            List<RequestedTransfer> requestedTransfers = new ArrayList<>();
            for (IdentityCount item : moved) {
                ActionRequestId requestId = switch (direction) {
                    case EXTERNAL_TO_CARRIED -> BackpackTransferRequester.requestExternalToCarriedId(
                            containerId,
                            item.identity(),
                            item.count()
                    );
                    case CARRIED_TO_EXTERNAL -> BackpackTransferRequester.requestCarriedToExternalId(
                            containerId,
                            item.identity(),
                            item.count()
                    );
                };
                requestedTransfers.add(new RequestedTransfer(
                        requestId.present() ? requestId.value() : "",
                        item.identity(),
                        item.count()
                ));
            }
            boolean sentAny = requestedTransfers.stream().anyMatch(RequestedTransfer::pending);
            if (!sentAny) {
                return ApplyResult.NONE;
            }
            ActionSessionStore.registerDeferredTransferHistoryTransition(
                    context.contextKey(),
                    transitionDirection,
                    toHistoryTransferDirection(this.direction()),
                    requestedTransfers.stream()
                            .map(requestedTransfer -> new RequestedHistoryTransfer(
                                    requestedTransfer.requestId(),
                                    requestedTransfer.identity(),
                                    requestedTransfer.requestedCount()
                            ))
                            .toList()
            );
            return new ApplyResult(true, true, true);
        }
    }

    private record QuickAccessAction(
            HotbarLoadoutCapture before,
            HotbarLoadoutCapture after
    ) implements RecordedAction {
        @Override
        public ApplyResult undo(ActionContext context) {
            return restore(before, context, HistoryReplayDirection.UNDO);
        }

        @Override
        public ApplyResult redo(ActionContext context) {
            return restore(after, context, HistoryReplayDirection.REDO);
        }

        private ApplyResult restore(
                HotbarLoadoutCapture capture,
                ActionContext context,
                HistoryReplayDirection direction
        ) {
            if (context.hotbarLoadoutController() == null) {
                return ApplyResult.NONE;
            }
            HotbarLoadoutCapture startBefore = context.hotbarLoadoutController().captureCurrentLoadout();
            QuickAccessMutationResult result = context.hotbarLoadoutController().restoreCapturedLoadoutMutation(capture);
            if (result == null || !result.changed()) {
                return ApplyResult.NONE;
            }
            if (!result.transferSyncExpected() || result.pendingChanges().isEmpty()) {
                return new ApplyResult(true, false, false);
            }
            ActionSessionStore.registerDeferredQuickAccessHistoryTransition(
                    context.contextKey(),
                    direction,
                    before,
                    after,
                    startBefore,
                    result.localHistoryAfter(),
                    result.pendingChanges()
            );
            return new ApplyResult(true, true, true);
        }
    }

    private static void processDeferredHistorySettlements() {
        for (HistorySettlement settlement : ActionSessionStore.drainHistorySettlements()) {
            if (settlement == null || !matchesContext(settlement.contextKey())) {
                continue;
            }

            if (settlement instanceof ConfirmedTransferRecordSettlement confirmedTransferRecord) {
                push(new TransferAction(
                        fromHistoryTransferDirection(confirmedTransferRecord.direction()),
                        confirmedTransferRecord.moved().stream()
                                .map(SlotUndoHistory::fromHistoryIdentityCount)
                                .toList()
                ));
                continue;
            }

            if (settlement instanceof QuickAccessRecordSettlement quickAccessRecord) {
                pushIfChanged(quickAccessRecord.before(), quickAccessRecord.settledAfter());
                continue;
            }

            if (settlement instanceof QuickAccessTransitionSettlement quickAccessTransition) {
                settleQuickAccessTransition(
                        quickAccessTransition.direction(),
                        new QuickAccessAction(quickAccessTransition.actionBefore(), quickAccessTransition.actionAfter()),
                        quickAccessTransition.startBefore(),
                        quickAccessTransition.settledAfter()
                );
                continue;
            }

            if (settlement instanceof TransferTransitionSettlement transferTransition) {
                List<IdentityCount> confirmed = transferTransition.confirmed().stream()
                        .map(SlotUndoHistory::fromHistoryIdentityCount)
                        .toList();
                List<IdentityCount> residual = transferTransition.residual().stream()
                        .map(SlotUndoHistory::fromHistoryIdentityCount)
                        .toList();

                switch (transferTransition.direction()) {
                    case UNDO -> {
                        pushTransferOnStackIfChanged(undoStack, fromHistoryTransferDirection(transferTransition.actionDirection()), residual);
                        pushTransferOnStackIfChanged(redoStack, fromHistoryTransferDirection(transferTransition.actionDirection()), confirmed);
                    }
                    case REDO -> {
                        pushTransferOnStackIfChanged(redoStack, fromHistoryTransferDirection(transferTransition.actionDirection()), residual);
                        pushTransferOnStackIfChanged(undoStack, fromHistoryTransferDirection(transferTransition.actionDirection()), confirmed);
                    }
                }
            }
        }
    }

    private static TransferDirection fromHistoryTransferDirection(HistoryTransferDirection direction) {
        return switch (direction) {
            case EXTERNAL_TO_CARRIED -> TransferDirection.EXTERNAL_TO_CARRIED;
            case CARRIED_TO_EXTERNAL -> TransferDirection.CARRIED_TO_EXTERNAL;
        };
    }

    private static HistoryTransferDirection toHistoryTransferDirection(TransferDirection direction) {
        return switch (direction) {
            case EXTERNAL_TO_CARRIED -> HistoryTransferDirection.EXTERNAL_TO_CARRIED;
            case CARRIED_TO_EXTERNAL -> HistoryTransferDirection.CARRIED_TO_EXTERNAL;
        };
    }

    private static IdentityCount fromHistoryIdentityCount(HistoryIdentityCount item) {
        return new IdentityCount(item.identity(), item.count());
    }

    private record RequestedTransfer(String requestId, ItemIdentity identity, int requestedCount) {
        private RequestedTransfer {
            requestId = requestId == null ? "" : requestId;
            Objects.requireNonNull(identity, "identity");
        }

        private boolean pending() {
            return !requestId.isBlank();
        }
    }
}
