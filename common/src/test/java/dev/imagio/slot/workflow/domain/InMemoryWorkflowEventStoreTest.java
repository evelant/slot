package dev.imagio.slot.workflow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryWorkflowEventStoreTest {
    @Test
    void nextStreamSequenceSurvivesCompactionWithoutRetainingRecords() {
        InMemoryWorkflowEventStore store = new InMemoryWorkflowEventStore();

        for (int index = 1; index <= 1_000; index++) {
            store.append(
                    new DomainEventEnvelope(
                            index,
                            index,
                            DomainEventStreamKind.WORKFLOW,
                            0L,
                            "test",
                            "",
                            "",
                            ""),
                    new WorkflowEvent.CollectionCreated("collection-" + index, "Collection " + index));
        }

        assertEquals(1_001L, store.nextStreamSequence());
        assertEquals(1_000, store.records().size());

        store.compact();

        assertEquals(1_001L, store.nextStreamSequence());
        assertEquals(0, store.records().size());

        store.append(null, new WorkflowEvent.CollectionCreated("next", "Next"));

        assertEquals(1_002L, store.nextStreamSequence());
        assertEquals(1, store.records().size());
        assertEquals(1_001L, store.records().get(0).envelope().streamSequence());
    }
}
