package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryWorkflowEventStore implements WorkflowEventStore {
    private final ArrayList<WorkflowEventRecord> records = new ArrayList<>();
    private long nextStreamSequence = 1L;

    @Override
    public WorkflowEventRecord append(DomainEventEnvelope envelope, WorkflowEvent event) {
        DomainEventEnvelope resolvedEnvelope = envelope == null
                ? new DomainEventEnvelope(0L, nextStreamSequence, DomainEventStreamKind.WORKFLOW, 0L, "", "", "", "")
                : new DomainEventEnvelope(
                envelope.globalSequence(),
                envelope.streamSequence() <= 0L ? nextStreamSequence : envelope.streamSequence(),
                DomainEventStreamKind.WORKFLOW,
                envelope.occurredAtEpochMillis(),
                envelope.origin(),
                envelope.correlationId(),
                envelope.causationId(),
                envelope.sessionId()
        );
        WorkflowEventRecord record = new WorkflowEventRecord(resolvedEnvelope, event);
        records.add(record);
        nextStreamSequence = Math.max(nextStreamSequence, resolvedEnvelope.streamSequence() + 1L);
        return record;
    }

    @Override
    public List<WorkflowEventRecord> records() {
        return List.copyOf(records);
    }

    @Override
    public long nextStreamSequence() {
        return nextStreamSequence;
    }

    @Override
    public Snapshot snapshot() {
        return new Snapshot(nextStreamSequence, records());
    }

    @Override
    public void replaceWith(Snapshot snapshot) {
        Snapshot resolved = snapshot == null ? Snapshot.empty() : snapshot;
        records.clear();
        records.addAll(resolved.records());
        nextStreamSequence = resolved.nextStreamSequence();
    }

    @Override
    public void compact() {
        records.clear();
    }
}
