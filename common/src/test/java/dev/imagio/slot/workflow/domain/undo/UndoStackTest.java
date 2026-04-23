package dev.imagio.slot.workflow.domain.undo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoStackTest {
    private static final UndoContext NO_CTX = UndoContext.workflowOnly(
            new dev.imagio.slot.workflow.domain.WorkflowDomainRuntime(
                    new dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository(),
                    null
            )
    );

    @Test
    void emptyStackReportsCannotUndoOrRedo() {
        UndoStack stack = new UndoStack();
        assertFalse(stack.canUndo());
        assertFalse(stack.canRedo());
        assertTrue(stack.undo(NO_CTX).isEmpty());
        assertTrue(stack.redo(NO_CTX).isEmpty());
    }

    @Test
    void recordPushThenUndoRedoWalksStackAndReportsLabels() {
        UndoStack stack = new UndoStack();
        List<String> log = new ArrayList<>();
        stack.record("first",
                ctx -> log.add("undo:first"),
                ctx -> log.add("redo:first"));

        assertTrue(stack.canUndo());
        assertFalse(stack.canRedo());

        Optional<UndoRecord> undone = stack.undo(NO_CTX);
        assertTrue(undone.isPresent());
        assertEquals("first", undone.get().label());
        assertEquals(List.of("undo:first"), log);
        assertFalse(stack.canUndo());
        assertTrue(stack.canRedo());

        Optional<UndoRecord> redone = stack.redo(NO_CTX);
        assertTrue(redone.isPresent());
        assertEquals("first", redone.get().label());
        assertEquals(List.of("undo:first", "redo:first"), log);
        assertTrue(stack.canUndo());
        assertFalse(stack.canRedo());
    }

    @Test
    void recordsExecutedDuringUndoOrRedoAreSuppressed() {
        UndoStack stack = new UndoStack();
        List<String> log = new ArrayList<>();
        // The inverse closure re-enters the sink: this simulates a domain-service mutation
        // that itself tries to push a new record while running under an undo/redo.
        stack.record(
                "outer",
                ctx -> {
                    log.add("undo:outer");
                    stack.record("inner", c -> log.add("undo:inner"), c -> log.add("redo:inner"));
                },
                ctx -> log.add("redo:outer")
        );

        stack.undo(NO_CTX);
        assertEquals(List.of("undo:outer"), log);
        // Inner record must NOT have landed on either stack; redo of outer should still be possible.
        assertFalse(stack.canUndo());
        assertTrue(stack.canRedo());

        stack.redo(NO_CTX);
        assertEquals(List.of("undo:outer", "redo:outer"), log);
        assertTrue(stack.canUndo());
        assertFalse(stack.canRedo());
    }

    @Test
    void newRecordClearsRedoStack() {
        UndoStack stack = new UndoStack();
        stack.record("first", ctx -> {}, ctx -> {});
        stack.undo(NO_CTX);
        assertTrue(stack.canRedo());

        stack.record("second", ctx -> {}, ctx -> {});
        assertFalse(stack.canRedo());
        assertTrue(stack.canUndo());
    }

    @Test
    void capacityIsEnforcedAsRingBuffer() {
        UndoStack stack = new UndoStack(3);
        stack.record("a", ctx -> {}, ctx -> {});
        stack.record("b", ctx -> {}, ctx -> {});
        stack.record("c", ctx -> {}, ctx -> {});
        stack.record("d", ctx -> {}, ctx -> {});
        stack.record("e", ctx -> {}, ctx -> {});

        assertEquals(3, stack.undoSize());
        assertEquals(List.of("c", "d", "e"), stack.undoLabels());
    }

    @Test
    void sequentialUndoRedoPreservesChronologicalOrder() {
        UndoStack stack = new UndoStack();
        List<String> events = new ArrayList<>();
        stack.record("a", ctx -> events.add("-a"), ctx -> events.add("+a"));
        stack.record("b", ctx -> events.add("-b"), ctx -> events.add("+b"));
        stack.record("c", ctx -> events.add("-c"), ctx -> events.add("+c"));

        stack.undo(NO_CTX);
        stack.undo(NO_CTX);
        stack.undo(NO_CTX);
        // Undo pops from the end: c, b, a.
        assertEquals(List.of("-c", "-b", "-a"), events);

        stack.redo(NO_CTX);
        stack.redo(NO_CTX);
        stack.redo(NO_CTX);
        // Redo replays in forward order: a, b, c.
        assertEquals(List.of("-c", "-b", "-a", "+a", "+b", "+c"), events);
    }

    @Test
    void clearEmptiesBothStacks() {
        UndoStack stack = new UndoStack();
        stack.record("a", ctx -> {}, ctx -> {});
        stack.undo(NO_CTX);
        assertTrue(stack.canRedo());

        stack.clear();
        assertFalse(stack.canUndo());
        assertFalse(stack.canRedo());
    }

}
