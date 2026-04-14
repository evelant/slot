package dev.imagio.slot.workflow.domain;

public record InventoryActivityRecord(
        DomainEventEnvelope envelope,
        InventoryActivityEvent event
) {
    public InventoryActivityRecord {
        envelope = envelope == null ? DomainEventEnvelope.empty(DomainEventStreamKind.ACTIVITY) : envelope;
    }
}
