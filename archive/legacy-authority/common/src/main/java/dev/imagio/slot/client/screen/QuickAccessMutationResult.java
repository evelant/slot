package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;

import java.util.List;
import java.util.Objects;

public record QuickAccessMutationResult(
        boolean changed,
        boolean transferSyncExpected,
        HotbarLoadoutCapture localHistoryAfter,
        HotbarLoadoutCapture historyAfter,
        List<RequestedChange> pendingChanges
) {
    static final QuickAccessMutationResult NONE = new QuickAccessMutationResult(false, false, null, null, List.of());

    public QuickAccessMutationResult {
        pendingChanges = pendingChanges == null ? List.of() : List.copyOf(pendingChanges);
    }

    static QuickAccessMutationResult of(
            HotbarLoadoutCapture before,
            HotbarLoadoutCapture localHistoryAfter,
            HotbarLoadoutCapture historyAfter,
            boolean transferSyncExpected,
            List<RequestedChange> pendingChanges
    ) {
        if (before == null || historyAfter == null || before.equals(historyAfter)) {
            return NONE;
        }
        return new QuickAccessMutationResult(
                true,
                transferSyncExpected,
                localHistoryAfter == null ? historyAfter : localHistoryAfter,
                historyAfter,
                pendingChanges
        );
    }

    public record RequestedChange(int quickAccessIndex, ItemIdentity identity, ActionRequestId requestId) {
        public RequestedChange {
            Objects.requireNonNull(identity, "identity");
            Objects.requireNonNull(requestId, "requestId");
        }
    }
}
