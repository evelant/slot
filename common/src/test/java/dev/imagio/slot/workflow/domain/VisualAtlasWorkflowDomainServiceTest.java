package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualAtlasWorkflowDomainServiceTest {
    @Test
    void createIslandAssignAndClearHomeMutatesProjection() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");

        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                320,
                196,
                0xCC5A4A6E,
                apple
        );
        VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 16, 60);

        assertEquals(island.id(), assignment.islandId());
        assertEquals(16, assignment.localX());
        assertEquals(60, assignment.localY());
        assertTrue(runtime.snapshot().visualHomeMap().playerIslands().stream().anyMatch(candidate -> candidate.id().equals(island.id())));
        assertEquals(assignment, runtime.snapshot().visualHomeMap().assignment(apple));
        assertTrue(runtime.visualAtlasWorkflow().clearHome(apple));
        assertNull(runtime.snapshot().visualHomeMap().assignment(apple));
    }

    @Test
    void moveIslandUpdatesOriginWithoutChangingLocalHomes() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");

        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                320,
                196,
                0xCC5A4A6E,
                apple
        );
        runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 16, 60);

        VisualAtlasIsland moved = runtime.visualAtlasWorkflow().moveIsland(island.id(), 988, 244);
        VisualHomeAssignment assignment = runtime.snapshot().visualHomeMap().assignment(apple);

        assertEquals(988, moved.x());
        assertEquals(244, moved.y());
        assertEquals(16, assignment.localX());
        assertEquals(60, assignment.localY());
    }
}
