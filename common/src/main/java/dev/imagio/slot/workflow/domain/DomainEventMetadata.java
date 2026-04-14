package dev.imagio.slot.workflow.domain;

public record DomainEventMetadata(
        String origin,
        String correlationId,
        String causationId,
        String sessionId
) {
    public DomainEventMetadata {
        origin = origin == null ? "" : origin;
        correlationId = correlationId == null ? "" : correlationId;
        causationId = causationId == null ? "" : causationId;
        sessionId = sessionId == null ? "" : sessionId;
    }

    public static DomainEventMetadata origin(String origin) {
        return new DomainEventMetadata(origin, "", "", "");
    }

    public DomainEventMetadata withOrigin(String nextOrigin) {
        return new DomainEventMetadata(nextOrigin, correlationId, causationId, sessionId);
    }
}
