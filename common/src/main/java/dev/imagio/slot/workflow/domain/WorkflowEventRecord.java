package dev.imagio.slot.workflow.domain;

public record WorkflowEventRecord(
        DomainEventEnvelope envelope,
        WorkflowEvent event
) {
    public WorkflowEventRecord {
        envelope = envelope == null ? DomainEventEnvelope.empty(DomainEventStreamKind.WORKFLOW) : envelope;
    }
}
