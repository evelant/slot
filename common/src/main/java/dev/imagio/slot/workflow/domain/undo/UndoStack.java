package dev.imagio.slot.workflow.domain.undo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Per-player undo/redo stack. Session-scoped (in-memory only, cleared on server
 * restart). Commands push records via {@link UndoSink#record}; the UI invokes
 * {@link #undo} / {@link #redo} to walk the stack.
 *
 * <p>Executing a record sets a {@code suppressed} flag so inverse mutations issued by
 * the record's closure don't themselves push new records and create a loop. Pushing
 * a fresh record (when not suppressed) clears the redo stack — the usual "new action
 * invalidates forward history" behavior.
 */
public final class UndoStack implements UndoSink {
    public static final int DEFAULT_CAPACITY = 32;

    private final Deque<UndoRecord> undoStack = new ArrayDeque<>();
    private final Deque<UndoRecord> redoStack = new ArrayDeque<>();
    private final int capacity;
    private boolean suppressed;

    public UndoStack() {
        this(DEFAULT_CAPACITY);
    }

    public UndoStack(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    @Override
    public void record(String label, Consumer<UndoContext> undo, Consumer<UndoContext> redo) {
        if (suppressed) {
            return;
        }
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(undo, "undo");
        Objects.requireNonNull(redo, "redo");
        redoStack.clear();
        while (undoStack.size() >= capacity) {
            undoStack.removeFirst();
        }
        undoStack.addLast(new UndoRecord(label, System.currentTimeMillis(), undo, redo));
    }

    public Optional<UndoRecord> peekUndo() {
        return Optional.ofNullable(undoStack.peekLast());
    }

    public Optional<UndoRecord> peekRedo() {
        return Optional.ofNullable(redoStack.peekLast());
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public Optional<UndoRecord> undo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        if (undoStack.isEmpty()) {
            return Optional.empty();
        }
        UndoRecord record = undoStack.removeLast();
        suppressed = true;
        try {
            record.undo().accept(context);
        } finally {
            suppressed = false;
        }
        redoStack.addLast(record);
        return Optional.of(record);
    }

    public Optional<UndoRecord> redo(UndoContext context) {
        Objects.requireNonNull(context, "context");
        if (redoStack.isEmpty()) {
            return Optional.empty();
        }
        UndoRecord record = redoStack.removeLast();
        suppressed = true;
        try {
            record.redo().accept(context);
        } finally {
            suppressed = false;
        }
        undoStack.addLast(record);
        return Optional.of(record);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public int undoSize() {
        return undoStack.size();
    }

    public int redoSize() {
        return redoStack.size();
    }

    public List<String> undoLabels() {
        ArrayList<String> labels = new ArrayList<>(undoStack.size());
        for (UndoRecord record : undoStack) {
            labels.add(record.label());
        }
        return List.copyOf(labels);
    }

    public List<String> redoLabels() {
        ArrayList<String> labels = new ArrayList<>(redoStack.size());
        for (UndoRecord record : redoStack) {
            labels.add(record.label());
        }
        return List.copyOf(labels);
    }
}
