package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceUndoTest {

    @Test
    void renameIslandUndoRestoresPriorLabelAndRedoReplays() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Original", 10, 20, 0xFF0000FF, null);

        WorkspaceCommandOutcome rename = SlotWorkspaceCommandService.renameIsland(
                runtime, island.id(), "Renamed");
        assertTrue(rename.success());
        assertEquals("Renamed", runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).label());
        assertTrue(runtime.undoStack().canUndo());

        WorkspaceCommandOutcome undo = SlotWorkspaceCommandService.performUndo(runtime);
        assertTrue(undo.success());
        assertEquals("Original", runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).label());
        assertFalse(runtime.undoStack().canUndo());
        assertTrue(runtime.undoStack().canRedo());

        WorkspaceCommandOutcome redo = SlotWorkspaceCommandService.performRedo(runtime);
        assertTrue(redo.success());
        assertEquals("Renamed", runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).label());
    }

    @Test
    void deleteIslandUndoRecreatesIslandWithSameIdAndProperties() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Ghost", 42, 42, 0xFFDDCC00, null);
        String islandId = island.id();

        WorkspaceCommandOutcome delete = SlotWorkspaceCommandService.deleteIsland(runtime, islandId);
        assertTrue(delete.success());
        assertNull(runtime.visualAtlasWorkflow().visualHomeMap().island(islandId));

        SlotWorkspaceCommandService.performUndo(runtime);
        VisualAtlasIsland recreated = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
        assertNotNull(recreated);
        assertEquals("Ghost", recreated.label());
        assertEquals(42, recreated.x());
        assertEquals(42, recreated.y());
        assertEquals(0xFFDDCC00, recreated.color());

        SlotWorkspaceCommandService.performRedo(runtime);
        assertNull(runtime.visualAtlasWorkflow().visualHomeMap().island(islandId));
    }

    @Test
    void deleteIslandClearsAssignedHomesAndUndoRestoresThem() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Bad Section", 42, 42, 0xFFDDCC00, apple);
        runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 0);

        WorkspaceCommandOutcome delete = SlotWorkspaceCommandService.deleteIsland(runtime, island.id());

        assertTrue(delete.success());
        assertNull(runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()));
        assertNull(runtime.visualAtlasWorkflow().visualHomeMap().assignment(apple));

        SlotWorkspaceCommandService.performUndo(runtime);
        assertNotNull(runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()));
        assertNotNull(runtime.visualAtlasWorkflow().visualHomeMap().assignment(apple));
        assertEquals(island.id(), runtime.visualAtlasWorkflow().visualHomeMap().assignment(apple).islandId());

        SlotWorkspaceCommandService.performRedo(runtime);
        assertNull(runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()));
        assertNull(runtime.visualAtlasWorkflow().visualHomeMap().assignment(apple));
    }

    @Test
    void recolorIslandUndoRestoresPriorColor() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Painted", 0, 0, 0xFFAA0000, null);

        SlotWorkspaceCommandService.recolorIsland(runtime, island.id(), 0xFF00AA00);
        assertEquals(0xFF00AA00, runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).color());

        SlotWorkspaceCommandService.performUndo(runtime);
        assertEquals(0xFFAA0000, runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).color());

        SlotWorkspaceCommandService.performRedo(runtime);
        assertEquals(0xFF00AA00, runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).color());
    }

    @Test
    void performUndoOnEmptyStackRejects() {
        WorkflowDomainRuntime runtime = runtime();
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.performUndo(runtime);
        assertFalse(outcome.success());
        assertEquals("nothing_to_undo", outcome.diagnostics());
    }

    @Test
    void performRedoAfterFreshActionRejects() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Fresh", 0, 0, 0xFF123456, null);
        SlotWorkspaceCommandService.renameIsland(runtime, island.id(), "Renamed");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.performRedo(runtime);
        assertFalse(outcome.success());
        assertEquals("nothing_to_redo", outcome.diagnostics());
    }

    @Test
    void newActionClearsRedoStack() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Base", 0, 0, 0xFF111111, null);

        SlotWorkspaceCommandService.renameIsland(runtime, island.id(), "First");
        SlotWorkspaceCommandService.performUndo(runtime);
        assertTrue(runtime.undoStack().canRedo());

        SlotWorkspaceCommandService.renameIsland(runtime, island.id(), "Second");
        assertFalse(runtime.undoStack().canRedo());
        assertTrue(runtime.undoStack().canUndo());
    }

    private static WorkflowDomainRuntime runtime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }
}
