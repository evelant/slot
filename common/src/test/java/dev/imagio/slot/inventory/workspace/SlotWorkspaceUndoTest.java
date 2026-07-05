package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
        assertSectionMetadataInvalidation(rename, island.id(), "island_label_changed");
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
        assertSectionMetadataInvalidation(delete, islandId, "island_deleted");
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
        assertSectionRemovedInvalidation(delete, apple, island.id(), "island_deleted");
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

        WorkspaceCommandOutcome recolor = SlotWorkspaceCommandService.recolorIsland(runtime, island.id(), 0xFF00AA00);
        assertSectionMetadataInvalidation(recolor, island.id(), "island_color_changed");
        assertEquals(0xFF00AA00, runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).color());

        SlotWorkspaceCommandService.performUndo(runtime);
        assertEquals(0xFFAA0000, runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).color());

        SlotWorkspaceCommandService.performRedo(runtime);
        assertEquals(0xFF00AA00, runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).color());
    }

    @Test
    void setIslandIconEmitsSectionMetadataInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Iconic", 0, 0, 0xFFAA0000, null);

        WorkspaceCommandOutcome icon = SlotWorkspaceCommandService.setIslandIcon(
                runtime,
                island.id(),
                "minecraft:apple",
                "ITEM_ID",
                "");

        assertTrue(icon.success(), icon.diagnostics());
        assertSectionMetadataInvalidation(icon, island.id(), "island_icon_changed");
        assertEquals(ItemIdentity.of("minecraft:apple"),
                runtime.visualAtlasWorkflow().visualHomeMap().island(island.id()).iconIdentity());
    }

    @Test
    void reorderIslandEmitsSectionMetadataInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        VisualAtlasIsland first = runtime.visualAtlasWorkflow().createIsland(
                "First", 0, 0, 0xFFAA0000, null);
        runtime.visualAtlasWorkflow().createIsland(
                "Second", 20, 0, 0xFF00AA00, null);
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot(),
                "ready",
                "",
                0,
                -1,
                0);

        WorkspaceCommandOutcome reorder = SlotWorkspaceCommandService.reorderIsland(
                runtime,
                viewModel,
                first.id(),
                1);

        assertSectionMetadataInvalidation(reorder, first.id(), "island_reordered");
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

    private static void assertSectionMetadataInvalidation(
            WorkspaceCommandOutcome outcome,
            String sectionId,
            String diagnostics
    ) {
        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.COMMAND_OUTCOME, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertTrue(invalidation.storageIds().isEmpty());
        assertEquals(Set.of(sectionId), invalidation.sectionIds());
        assertEquals(Set.of(WorkspaceProjectionSlice.SECTION, WorkspaceProjectionSlice.FRAME), invalidation.slices());
        assertEquals(diagnostics, invalidation.diagnostics());
    }

    private static void assertSectionRemovedInvalidation(
            WorkspaceCommandOutcome outcome,
            ItemIdentity identity,
            String sectionId,
            String diagnostics
    ) {
        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.COMMAND_OUTCOME, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of(identity), invalidation.identities());
        assertTrue(invalidation.storageIds().isEmpty());
        assertEquals(Set.of(sectionId, SlotWorkspaceAtlasLayout.ISLAND_TRIAGE), invalidation.sectionIds());
        assertEquals(
                Set.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION, WorkspaceProjectionSlice.FRAME),
                invalidation.slices());
        assertEquals(diagnostics, invalidation.diagnostics());
    }
}
