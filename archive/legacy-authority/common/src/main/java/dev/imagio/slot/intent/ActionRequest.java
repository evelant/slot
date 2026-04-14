package dev.imagio.slot.intent;

import dev.imagio.slot.source.SourceSlotRef;

import java.util.Objects;

public record ActionRequest(
        int requestSchemaVersion,
        ActionRequestId requestId,
        String expectedSessionFingerprint,
        int expectedContainerId,
        ActionFamily actionFamily,
        SourceSlotRef primarySourceRef,
        SourceSlotRef secondarySourceRef,
        String toolRef,
        String identityKey,
        int requestedCount
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public ActionRequest {
        requestSchemaVersion = requestSchemaVersion <= 0 ? CURRENT_SCHEMA_VERSION : requestSchemaVersion;
        requestId = requestId == null ? ActionRequestId.none() : requestId;
        expectedSessionFingerprint = expectedSessionFingerprint == null ? "" : expectedSessionFingerprint;
        expectedContainerId = Math.max(-1, expectedContainerId);
        Objects.requireNonNull(actionFamily, "actionFamily");
        toolRef = toolRef == null ? "" : toolRef;
        identityKey = identityKey == null ? "" : identityKey;
        requestedCount = Math.max(0, requestedCount);
    }

    public static ActionRequest forSession(ActionFamily actionFamily, String expectedSessionFingerprint, int expectedContainerId) {
        return new ActionRequest(
                CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint,
                expectedContainerId,
                actionFamily,
                null,
                null,
                "",
                "",
                0
        );
    }
}
