package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void renameIslandUpdatesLabelAndReturnsExistingWhenUnchanged() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines", 10, 20, 320, 196, 0xCC5A4A6E, null
        );

        VisualAtlasIsland renamed = runtime.visualAtlasWorkflow().renameIsland(island.id(), "Factory");
        assertNotNull(renamed);
        assertEquals("Factory", renamed.label());
        assertEquals("Factory", runtime.snapshot().visualHomeMap().island(island.id()).label());

        VisualAtlasIsland unchanged = runtime.visualAtlasWorkflow().renameIsland(island.id(), "Factory");
        assertEquals("Factory", unchanged.label());

        assertNull(runtime.visualAtlasWorkflow().renameIsland("does-not-exist", "Nope"));
    }

    @Test
    void recolorIslandUpdatesColorAndReturnsExistingWhenUnchanged() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines", 10, 20, 320, 196, 0xCC5A4A6E, null
        );

        VisualAtlasIsland recolored = runtime.visualAtlasWorkflow().recolorIsland(island.id(), 0xFF112233);
        assertEquals(0xFF112233, recolored.color());
        assertEquals(0xFF112233, runtime.snapshot().visualHomeMap().island(island.id()).color());

        VisualAtlasIsland unchanged = runtime.visualAtlasWorkflow().recolorIsland(island.id(), 0xFF112233);
        assertEquals(0xFF112233, unchanged.color());

        assertNull(runtime.visualAtlasWorkflow().recolorIsland("does-not-exist", 0x0));
    }

    @Test
    void setIslandIconReplacesIconAndNoOpsOnEquality() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");
        ItemIdentity pumpkin = ItemIdentity.of("minecraft:pumpkin");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Food", 10, 20, 320, 196, 0xCC5A4A6E, apple
        );

        VisualAtlasIsland updated = runtime.visualAtlasWorkflow().setIslandIcon(island.id(), pumpkin);
        assertEquals(pumpkin, updated.iconIdentity());
        assertEquals(pumpkin, runtime.snapshot().visualHomeMap().island(island.id()).iconIdentity());

        VisualAtlasIsland unchanged = runtime.visualAtlasWorkflow().setIslandIcon(island.id(), pumpkin);
        assertEquals(pumpkin, unchanged.iconIdentity());

        VisualAtlasIsland cleared = runtime.visualAtlasWorkflow().setIslandIcon(island.id(), null);
        assertNull(cleared.iconIdentity());
    }

    @Test
    void deleteIslandRemovesEmptyIslandButRefusesWhenAssigned() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Food", 10, 20, 320, 196, 0xCC5A4A6E, apple
        );

        runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 4, 4);
        assertFalse(runtime.visualAtlasWorkflow().deleteIsland(island.id()));
        assertNotNull(runtime.snapshot().visualHomeMap().island(island.id()));

        runtime.visualAtlasWorkflow().clearHome(apple);
        assertTrue(runtime.visualAtlasWorkflow().deleteIsland(island.id()));
        assertNull(runtime.snapshot().visualHomeMap().island(island.id()));

        assertFalse(runtime.visualAtlasWorkflow().deleteIsland(island.id()));
        assertFalse(runtime.visualAtlasWorkflow().deleteIsland(""));
    }

    @Test
    void dismissTemplateAddsToDismissedSetAndIsIdempotent() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);

        assertTrue(runtime.visualAtlasWorkflow().dismissTemplate("food"));
        assertTrue(runtime.snapshot().visualHomeMap().dismissedTemplateIds().contains("food"));
        assertTrue(runtime.snapshot().visualHomeMap().templateDismissed("food"));

        assertFalse(runtime.visualAtlasWorkflow().dismissTemplate("food"));
        assertFalse(runtime.visualAtlasWorkflow().dismissTemplate(""));
        assertFalse(runtime.visualAtlasWorkflow().dismissTemplate(null));
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
