package dev.imagio.slot.client.screen;

import dev.imagio.slot.action.session.ActionSessionStore;
import dev.imagio.slot.action.session.QuickAccessRequestedTarget;
import dev.imagio.slot.intent.ActionRequestId;

import java.util.List;

public final class QuickAccessPendingState {
    private QuickAccessPendingState() {
    }

    public static void recordRequestedChanges(List<QuickAccessMutationResult.RequestedChange> pendingChanges) {
        List<QuickAccessRequestedTarget> targets = pendingChanges == null
                ? List.of()
                : pendingChanges.stream()
                .map(change -> new QuickAccessRequestedTarget(change.quickAccessIndex(), change.identity(), change.requestId()))
                .toList();
        ActionSessionStore.recordQuickAccessTargets(targets);
    }

    public static boolean isPendingTarget(int quickAccessIndex) {
        return ActionSessionStore.isPendingQuickAccessIndex(quickAccessIndex);
    }

    public static boolean hasPendingTargets() {
        return ActionSessionStore.hasPendingQuickAccessTargets();
    }

    public static void completeRequest(ActionRequestId requestId) {
        ActionSessionStore.completeQuickAccessRequest(requestId);
    }

    public static void clear() {
        ActionSessionStore.clear();
    }
}
