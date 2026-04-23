package dev.imagio.slot.workflow.domain.undo;

import java.util.function.Consumer;

/**
 * Target that workspace commands record undo entries against after a successful mutation.
 *
 * <p>Commands should snapshot whatever pre-state they need for the inverse operation, run
 * the mutation, then call {@link #record} with a human-readable label, a closure that
 * reverses the effect (invoked with live runtime context at undo time), and a closure
 * that replays the effect (invoked when the user redoes).
 *
 * <p>Implementations are expected to be idempotent-safe during undo/redo execution — any
 * recording attempts while an undo is in flight must be suppressed so inverse calls
 * don't pollute the stack.
 */
@FunctionalInterface
public interface UndoSink {
    void record(String label, Consumer<UndoContext> undo, Consumer<UndoContext> redo);

    /** A sink that drops every record. Useful for tests or code paths where undo is not desired. */
    UndoSink NO_OP = (label, undo, redo) -> {
    };
}
