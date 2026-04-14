package dev.imagio.slot.client.screen;

import net.minecraft.network.chat.Component;

public final class InventoryActionOrchestrator {
    private static final SlotActionResult TRANSFER_REJECTED = SlotActionResult.blocked(
            Component.translatable("slot.screen.action.blocked.transfer_rejected")
    );

    private InventoryActionOrchestrator() {
    }

    public static void applyUndoHistoryResult(
            SlotUndoHistory.ApplyResult result,
            boolean undo,
            Hooks hooks,
            Runnable transferSyncHandler
    ) {
        if (result == null || hooks == null || !result.applied()) {
            if (hooks != null) {
                hooks.updateDynamicButtons();
            }
            return;
        }

        hooks.showActionFeedback(result.transferSyncExpected()
                ? SlotActionResult.requested(Component.translatable(
                        undo ? "slot.screen.action.undo.requested" : "slot.screen.action.redo.requested"
                ))
                : SlotActionResult.applied(Component.translatable(
                        undo ? "slot.screen.action.undo.applied" : "slot.screen.action.redo.applied"
                )));

        if (result.transferSyncExpected()) {
            if (transferSyncHandler != null) {
                transferSyncHandler.run();
            }
            return;
        }

        hooks.schedulePostActionRefresh();
        hooks.refreshInventoryData();
    }

    public static void applyConfirmedActionOutcome(Hooks hooks) {
        if (hooks == null) {
            return;
        }

        java.util.List<SlotActionOutcomeState.PublishedOutcome> outcomes =
                SlotActionOutcomeState.pollAll(hooks.historyContextKey());
        if (outcomes.isEmpty()) {
            return;
        }

        OutcomeSummary summary = summarizeOutcomes(outcomes);
        if (summary.feedback().visible()) {
            hooks.showActionFeedback(summary.feedback());
        }
        if (!summary.anySuccessful()) {
            hooks.clearPendingPostActionRefresh();
        }
        hooks.updateDynamicButtons();
    }

    public static OutcomeSummary summarizeOutcomes(java.util.List<SlotActionOutcomeState.PublishedOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty()) {
            return new OutcomeSummary(SlotActionResult.NONE, false);
        }

        SlotActionResult latestVisible = SlotActionResult.NONE;
        boolean anySuccessful = false;
        boolean anyUnsuccessful = false;
        for (SlotActionOutcomeState.PublishedOutcome outcome : outcomes) {
            if (outcome == null || outcome.result() == null) {
                continue;
            }
            if (outcome.result().visible()) {
                latestVisible = outcome.result();
            }
            if (outcome.result().successful()) {
                anySuccessful = true;
            } else if (outcome.result().visible()) {
                anyUnsuccessful = true;
            }
        }

        if (anySuccessful && anyUnsuccessful) {
            return new OutcomeSummary(
                    SlotActionResult.applied(Component.translatable("slot.screen.action.outcome.batch.partial")),
                    true
            );
        }
        return new OutcomeSummary(latestVisible, anySuccessful);
    }

    public static void handleActionResult(SlotActionResult result, Hooks hooks, ActionPlan actionPlan) {
        if (result == null || hooks == null) {
            return;
        }

        hooks.showActionFeedback(result);
        if (!result.successful()) {
            run(actionPlan == null ? null : actionPlan.onFailure());
            return;
        }

        applySuccessPlan(hooks, actionPlan);
    }

    public static boolean handleRequestedAction(
            boolean requested,
            SlotActionResult requestedResult,
            Hooks hooks,
            ActionPlan actionPlan
    ) {
        if (!requested) {
            if (hooks != null) {
                hooks.showActionFeedback(TRANSFER_REJECTED);
            }
            return false;
        }

        handleActionResult(requestedResult, hooks, actionPlan);
        return true;
    }

    private static void applySuccessPlan(Hooks hooks, ActionPlan actionPlan) {
        if (hooks == null || actionPlan == null) {
            return;
        }

        if (actionPlan.suppressPositiveDeltas()) {
            RecentLootTracker.suppressPositiveDeltas();
        }
        run(actionPlan.onSuccess());
        if (actionPlan.schedulePostActionRefresh()) {
            hooks.schedulePostActionRefresh();
        }
        if (actionPlan.refreshImmediately()) {
            hooks.refreshInventoryData();
        }
    }

    private static void run(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public interface Hooks {
        void showActionFeedback(SlotActionResult result);

        void schedulePostActionRefresh();

        void refreshInventoryData();

        void clearPendingPostActionRefresh();

        void updateDynamicButtons();

        String historyContextKey();
    }

    public record OutcomeSummary(SlotActionResult feedback, boolean anySuccessful) {
    }

    public record ActionPlan(
            boolean suppressPositiveDeltas,
            boolean schedulePostActionRefresh,
            boolean refreshImmediately,
            Runnable onSuccess,
            Runnable onFailure
    ) {
        public static ActionPlan of(
                boolean suppressPositiveDeltas,
                boolean schedulePostActionRefresh,
                boolean refreshImmediately
        ) {
            return new ActionPlan(suppressPositiveDeltas, schedulePostActionRefresh, refreshImmediately, null, null);
        }

        public ActionPlan withCallbacks(Runnable onSuccess, Runnable onFailure) {
            return new ActionPlan(
                    suppressPositiveDeltas,
                    schedulePostActionRefresh,
                    refreshImmediately,
                    onSuccess,
                    onFailure
            );
        }
    }
}
