package dev.imagio.slot.workflow.domain;

public record ContextualSignalRecord(
        DomainEventEnvelope envelope,
        ContextualSignalEvent event
) {
    public ContextualSignalRecord {
        envelope = envelope == null ? DomainEventEnvelope.empty(DomainEventStreamKind.CONTEXTUAL) : envelope;
    }
}
