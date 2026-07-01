package dev.imagio.slot.workflow.domain;

import java.util.List;

public interface WorkflowEventStore {
    WorkflowEventRecord append(DomainEventEnvelope envelope, WorkflowEvent event);

    List<WorkflowEventRecord> records();

    long nextStreamSequence();

    Snapshot snapshot();

    void replaceWith(Snapshot snapshot);

    default void compact() {
        replaceWith(new Snapshot(nextStreamSequence(), java.util.List.of()));
    }

    record Snapshot(
            long nextStreamSequence,
            List<WorkflowEventRecord> records
    ) {
        public Snapshot {
            nextStreamSequence = Math.max(1L, nextStreamSequence);
            records = records == null ? List.of() : List.copyOf(records);
        }

        public static Snapshot empty() {
            return new Snapshot(1L, List.of());
        }
    }
}
