package dev.imagio.slot.operation;

import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequestId;

import java.util.List;
import java.util.Objects;

public record ActionOutcome(
        ActionRequestId requestId,
        ActionFamily actionFamily,
        ActionStatus status,
        ActionReason reason,
        int affectedCount,
        RefreshScope refreshScope,
        List<String> affectedSourceIds,
        List<String> acquisitionItemIds,
        String acquisitionProducerId,
        String summaryKey
) {
    public ActionOutcome {
        requestId = requestId == null ? ActionRequestId.none() : requestId;
        Objects.requireNonNull(actionFamily, "actionFamily");
        Objects.requireNonNull(status, "status");
        reason = reason == null ? defaultReason(status) : reason;
        refreshScope = refreshScope == null ? RefreshScope.NONE : refreshScope;
        affectedCount = Math.max(0, affectedCount);
        affectedSourceIds = affectedSourceIds == null ? List.of() : List.copyOf(affectedSourceIds);
        acquisitionItemIds = acquisitionItemIds == null ? List.of() : List.copyOf(acquisitionItemIds);
        acquisitionProducerId = acquisitionProducerId == null ? "" : acquisitionProducerId;
        summaryKey = summaryKey == null ? "" : summaryKey;
    }

    public static ActionOutcome confirmed(
            ActionRequestId requestId,
            ActionFamily actionFamily,
            int affectedCount,
            RefreshScope refreshScope
    ) {
        return new ActionOutcome(
                requestId,
                actionFamily,
                ActionStatus.CONFIRMED,
                ActionReason.NONE,
                affectedCount,
                refreshScope,
                List.of(),
                List.of(),
                "",
                ""
        );
    }

    public static ActionOutcome blocked(
            ActionRequestId requestId,
            ActionFamily actionFamily,
            ActionReason reason,
            RefreshScope refreshScope
    ) {
        return new ActionOutcome(
                requestId,
                actionFamily,
                ActionStatus.BLOCKED,
                reason == null ? ActionReason.UNSPECIFIED : reason,
                0,
                refreshScope,
                List.of(),
                List.of(),
                "",
                ""
        );
    }

    public static ActionOutcome failed(
            ActionRequestId requestId,
            ActionFamily actionFamily,
            ActionReason reason,
            RefreshScope refreshScope
    ) {
        return new ActionOutcome(
                requestId,
                actionFamily,
                ActionStatus.FAILED,
                reason == null ? ActionReason.INTERNAL_ERROR : reason,
                0,
                refreshScope,
                List.of(),
                List.of(),
                "",
                ""
        );
    }

    public boolean successful() {
        return status == ActionStatus.CONFIRMED;
    }

    private static ActionReason defaultReason(ActionStatus status) {
        return status == ActionStatus.CONFIRMED ? ActionReason.NONE : ActionReason.UNSPECIFIED;
    }
}
