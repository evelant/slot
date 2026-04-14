package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryInventoryActivityStore implements InventoryActivityStore {
    private final ArrayList<InventoryActivityRecord> records = new ArrayList<>();
    private int maxEvents;
    private long nextStreamSequence = 1L;

    public InMemoryInventoryActivityStore() {
        this(DEFAULT_MAX_EVENTS);
    }

    public InMemoryInventoryActivityStore(int maxEvents) {
        this.maxEvents = Math.max(1, maxEvents);
    }

    @Override
    public InventoryActivityRecord append(DomainEventEnvelope envelope, InventoryActivityEvent event) {
        DomainEventEnvelope resolvedEnvelope = envelope == null
                ? new DomainEventEnvelope(0L, nextStreamSequence, DomainEventStreamKind.ACTIVITY, 0L, "", "", "", "")
                : new DomainEventEnvelope(
                envelope.globalSequence(),
                envelope.streamSequence() <= 0L ? nextStreamSequence : envelope.streamSequence(),
                DomainEventStreamKind.ACTIVITY,
                envelope.occurredAtEpochMillis(),
                envelope.origin(),
                envelope.correlationId(),
                envelope.causationId(),
                envelope.sessionId()
        );
        InventoryActivityRecord record = new InventoryActivityRecord(resolvedEnvelope, event);
        records.add(record);
        while (records.size() > maxEvents) {
            records.removeFirst();
        }
        nextStreamSequence = Math.max(nextStreamSequence, resolvedEnvelope.streamSequence() + 1L);
        return record;
    }

    @Override
    public List<InventoryActivityRecord> records() {
        return List.copyOf(records);
    }

    @Override
    public Snapshot snapshot() {
        return new Snapshot(maxEvents, nextStreamSequence, records());
    }

    @Override
    public void replaceWith(Snapshot snapshot) {
        Snapshot resolved = snapshot == null ? Snapshot.empty() : snapshot;
        records.clear();
        records.addAll(resolved.records());
        maxEvents = resolved.maxEvents();
        nextStreamSequence = resolved.nextStreamSequence();
        while (records.size() > maxEvents) {
            records.removeFirst();
        }
    }
}
