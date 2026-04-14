package dev.imagio.slot.workflow.domain;

public record DomainEventEnvelope(
        long globalSequence,
        long streamSequence,
        DomainEventStreamKind streamKind,
        long occurredAtEpochMillis,
        String origin,
        String correlationId,
        String causationId,
        String sessionId
) {
    public DomainEventEnvelope {
        globalSequence = Math.max(0L, globalSequence);
        streamSequence = Math.max(0L, streamSequence);
        streamKind = streamKind == null ? DomainEventStreamKind.WORKFLOW : streamKind;
        occurredAtEpochMillis = Math.max(0L, occurredAtEpochMillis);
        origin = origin == null ? "" : origin;
        correlationId = correlationId == null ? "" : correlationId;
        causationId = causationId == null ? "" : causationId;
        sessionId = sessionId == null ? "" : sessionId;
    }

    public static DomainEventEnvelope empty(DomainEventStreamKind streamKind) {
        return new DomainEventEnvelope(0L, 0L, streamKind, 0L, "", "", "", "");
    }
}
