package dev.imagio.slot.workflow.domain.undo;

import java.util.Objects;
import java.util.function.Consumer;

public record UndoRecord(
        String label,
        long timestamp,
        Consumer<UndoContext> undo,
        Consumer<UndoContext> redo
) {
    public UndoRecord {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(undo, "undo");
        Objects.requireNonNull(redo, "redo");
    }
}
