package dev.imagio.slot.workflow.domain;

import java.util.List;

public interface InventoryActivityStore {
    int DEFAULT_MAX_EVENTS = 512;

    InventoryActivityRecord append(DomainEventEnvelope envelope, InventoryActivityEvent event);

    List<InventoryActivityRecord> records();

    long nextStreamSequence();

    Snapshot snapshot();

    void replaceWith(Snapshot snapshot);

    record Snapshot(
            int maxEvents,
            long nextStreamSequence,
            List<InventoryActivityRecord> records
    ) {
        public Snapshot {
            maxEvents = Math.max(1, maxEvents);
            nextStreamSequence = Math.max(1L, nextStreamSequence);
            records = records == null ? List.of() : List.copyOf(records);
        }

        public static Snapshot empty() {
            return new Snapshot(DEFAULT_MAX_EVENTS, 1L, List.of());
        }
    }
}
