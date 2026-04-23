package dev.imagio.slot.workflow.domain.undo;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;

import java.util.Objects;
import java.util.function.Function;

/**
 * Execution context handed to an {@link UndoRecord}'s undo / redo closures.
 *
 * <p>Workflow-only inverse operations (home assign/clear, island create/delete, etc.)
 * only need {@link #runtime()}. Inverse operations that touch real inventory (kit
 * activation, chest deposit / take-all, hotbar transfer) also need the remaining
 * fields so they can issue authoritative mutation requests through the shared
 * executor pipeline.
 */
public record UndoContext(
        WorkflowDomainRuntime runtime,
        InventoryAuthoritySnapshot authority,
        ProtectionPolicy protection,
        Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor
) {
    public UndoContext {
        Objects.requireNonNull(runtime, "runtime");
    }

    /** Shortcut for workflow-only undo closures that don't touch inventory. */
    public static UndoContext workflowOnly(WorkflowDomainRuntime runtime) {
        return new UndoContext(runtime, null, null, null, null);
    }

    public boolean hasInventoryExecutor() {
        return actionExecutor != null;
    }
}
