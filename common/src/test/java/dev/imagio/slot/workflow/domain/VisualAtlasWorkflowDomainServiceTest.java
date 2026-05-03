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
                "Machines", 744, 104, 0xCC5A4A6E, apple
        );
        VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 0);

        assertEquals(island.id(), assignment.islandId());
        assertEquals(0, assignment.ordinal());
        assertTrue(runtime.snapshot().visualHomeMap().playerIslands().stream().anyMatch(candidate -> candidate.id().equals(island.id())));
        assertEquals(assignment, runtime.snapshot().visualHomeMap().assignment(apple));
        assertTrue(runtime.visualAtlasWorkflow().clearHome(apple));
        assertNull(runtime.snapshot().visualHomeMap().assignment(apple));
    }

    @Test
    void renameIslandUpdatesLabelAndReturnsExistingWhenUnchanged() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines", 10, 20, 0xCC5A4A6E, null
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
                "Machines", 10, 20, 0xCC5A4A6E, null
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
                "Food", 10, 20, 0xCC5A4A6E, apple
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
                "Food", 10, 20, 0xCC5A4A6E, apple
        );

        runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 0);
        assertFalse(runtime.visualAtlasWorkflow().deleteIsland(island.id()));
        assertNotNull(runtime.snapshot().visualHomeMap().island(island.id()));

        runtime.visualAtlasWorkflow().clearHome(apple);
        assertTrue(runtime.visualAtlasWorkflow().deleteIsland(island.id()));
        assertNull(runtime.snapshot().visualHomeMap().island(island.id()));

        assertFalse(runtime.visualAtlasWorkflow().deleteIsland(island.id()));
        assertFalse(runtime.visualAtlasWorkflow().deleteIsland(""));
    }

    @Test
    void reorderIslandMovesIslandToTargetIndex() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        VisualAtlasIsland a = runtime.visualAtlasWorkflow().createIslandWithId("a", "A", 0, 0, 0xFF000001, null);
        VisualAtlasIsland b = runtime.visualAtlasWorkflow().createIslandWithId("b", "B", 0, 0, 0xFF000002, null);
        VisualAtlasIsland c = runtime.visualAtlasWorkflow().createIslandWithId("c", "C", 0, 0, 0xFF000003, null);
        VisualAtlasIsland d = runtime.visualAtlasWorkflow().createIslandWithId("d", "D", 0, 0, 0xFF000004, null);

        // Move "a" (index 0) to index 2 → expected order [B, C, A, D].
        VisualAtlasIsland reordered = runtime.visualAtlasWorkflow().reorderIsland("a", 2);
        assertNotNull(reordered);
        java.util.List<VisualAtlasIsland> order = runtime.snapshot().visualHomeMap().playerIslands();
        assertEquals(java.util.List.of("b", "c", "a", "d"),
                order.stream().map(VisualAtlasIsland::id).toList());

        // Move "d" (now last, index 3) up to index 0.
        runtime.visualAtlasWorkflow().reorderIsland("d", 0);
        order = runtime.snapshot().visualHomeMap().playerIslands();
        assertEquals(java.util.List.of("d", "b", "c", "a"),
                order.stream().map(VisualAtlasIsland::id).toList());

        // Reorder past the end clamps to the tail.
        runtime.visualAtlasWorkflow().reorderIsland("d", 999);
        order = runtime.snapshot().visualHomeMap().playerIslands();
        assertEquals(java.util.List.of("b", "c", "a", "d"),
                order.stream().map(VisualAtlasIsland::id).toList());

        // Reorder onto current index is a no-op (returns existing, no event).
        VisualAtlasIsland sameSlot = runtime.visualAtlasWorkflow().reorderIsland("a", 2);
        assertNotNull(sameSlot);
        assertEquals(java.util.List.of("b", "c", "a", "d"),
                runtime.snapshot().visualHomeMap().playerIslands().stream().map(VisualAtlasIsland::id).toList());

        // Unknown / blank ids reject.
        assertNull(runtime.visualAtlasWorkflow().reorderIsland("nope", 0));
        assertNull(runtime.visualAtlasWorkflow().reorderIsland("", 0));
        assertNull(runtime.visualAtlasWorkflow().reorderIsland(null, 0));
        assertEquals(-1, runtime.visualAtlasWorkflow().playerIslandIndex("nope"));
        assertEquals(0, runtime.visualAtlasWorkflow().playerIslandIndex("b"));
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
    void moveIslandUpdatesOriginWithoutChangingOrdinals() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");

        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines", 744, 104, 0xCC5A4A6E, apple
        );
        runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 0);

        VisualAtlasIsland moved = runtime.visualAtlasWorkflow().moveIsland(island.id(), 988, 244);
        VisualHomeAssignment assignment = runtime.snapshot().visualHomeMap().assignment(apple);

        assertEquals(988, moved.x());
        assertEquals(244, moved.y());
        assertEquals(0, assignment.ordinal());
    }

    @Test
    void appendThenInsertShiftsExistingOrdinals() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");
        ItemIdentity bread = ItemIdentity.of("minecraft:bread");
        ItemIdentity carrot = ItemIdentity.of("minecraft:carrot");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Food", 0, 0, 0xCC000000, apple
        );

        runtime.visualAtlasWorkflow().assignHome(apple, island.id(), 0);
        runtime.visualAtlasWorkflow().assignHome(bread, island.id(), 1);
        // Insert carrot at ordinal 0 — apple/bread should both shift +1.
        runtime.visualAtlasWorkflow().assignHome(carrot, island.id(), 0);

        VisualHomeMap map = runtime.snapshot().visualHomeMap();
        assertEquals(0, map.assignment(carrot).ordinal());
        assertEquals(1, map.assignment(apple).ordinal());
        assertEquals(2, map.assignment(bread).ordinal());
    }

    @Test
    void sameIslandMoveDownPositionsAfterTarget() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity a = ItemIdentity.of("minecraft:a");
        ItemIdentity b = ItemIdentity.of("minecraft:b");
        ItemIdentity c = ItemIdentity.of("minecraft:c");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Pile", 0, 0, 0xCC000000, a
        );
        runtime.visualAtlasWorkflow().assignHome(a, island.id(), 0);
        runtime.visualAtlasWorkflow().assignHome(b, island.id(), 1);
        runtime.visualAtlasWorkflow().assignHome(c, island.id(), 2);

        // Move a (ordinal 0) onto c (current ordinal 2) — same-island
        // forward move: a should land at ordinal 1 (immediately before
        // c's old position) so the visible order becomes [b, a, c].
        runtime.visualAtlasWorkflow().assignHome(a, island.id(), 2);

        VisualHomeMap map = runtime.snapshot().visualHomeMap();
        assertEquals(0, map.assignment(b).ordinal());
        assertEquals(1, map.assignment(a).ordinal());
        assertEquals(2, map.assignment(c).ordinal());
    }

    @Test
    void crossIslandMoveCompactsSourceAndShiftsDestination() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity a = ItemIdentity.of("minecraft:a");
        ItemIdentity b = ItemIdentity.of("minecraft:b");
        ItemIdentity c = ItemIdentity.of("minecraft:c");
        VisualAtlasIsland src = runtime.visualAtlasWorkflow().createIsland(
                "Src", 0, 0, 0xCC000000, a
        );
        VisualAtlasIsland dst = runtime.visualAtlasWorkflow().createIsland(
                "Dst", 200, 0, 0xCC000000, c
        );
        runtime.visualAtlasWorkflow().assignHome(a, src.id(), 0);
        runtime.visualAtlasWorkflow().assignHome(b, src.id(), 1);
        runtime.visualAtlasWorkflow().assignHome(c, dst.id(), 0);

        // Move a (src ord 0) into dst at ordinal 0 — c shifts from 0 to 1,
        // and src compacts so b becomes ord 0.
        runtime.visualAtlasWorkflow().assignHome(a, dst.id(), 0);

        VisualHomeMap map = runtime.snapshot().visualHomeMap();
        assertEquals(0, map.assignment(b).ordinal());
        assertEquals(src.id(), map.assignment(b).islandId());
        assertEquals(0, map.assignment(a).ordinal());
        assertEquals(dst.id(), map.assignment(a).islandId());
        assertEquals(1, map.assignment(c).ordinal());
    }

    @Test
    void clearHomeCompactsTrailingOrdinals() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity a = ItemIdentity.of("minecraft:a");
        ItemIdentity b = ItemIdentity.of("minecraft:b");
        ItemIdentity c = ItemIdentity.of("minecraft:c");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Pile", 0, 0, 0xCC000000, a
        );
        runtime.visualAtlasWorkflow().assignHome(a, island.id(), 0);
        runtime.visualAtlasWorkflow().assignHome(b, island.id(), 1);
        runtime.visualAtlasWorkflow().assignHome(c, island.id(), 2);

        runtime.visualAtlasWorkflow().clearHome(b);

        VisualHomeMap map = runtime.snapshot().visualHomeMap();
        assertNull(map.assignment(b));
        assertEquals(0, map.assignment(a).ordinal());
        assertEquals(1, map.assignment(c).ordinal());
    }

    @Test
    void appendOrdinalBeyondEndClampsToTail() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity a = ItemIdentity.of("minecraft:a");
        ItemIdentity b = ItemIdentity.of("minecraft:b");
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Pile", 0, 0, 0xCC000000, a
        );
        runtime.visualAtlasWorkflow().assignHome(a, island.id(), 0);
        runtime.visualAtlasWorkflow().assignHome(b, island.id(), 999);

        VisualHomeMap map = runtime.snapshot().visualHomeMap();
        assertEquals(0, map.assignment(a).ordinal());
        assertEquals(1, map.assignment(b).ordinal(),
                "out-of-range ordinal should clamp to size-of-island after the prior items");
    }
}
