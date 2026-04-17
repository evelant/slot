package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;

public record WorkspaceTransferExecution(
        InventoryHostDescriptor host,
        InventoryActionRequest request,
        InventoryActionOutcome outcome,
        WorkspaceTransferFeedback feedback
) {
    public WorkspaceTransferExecution {
        feedback = feedback == null ? WorkspaceTransferFeedback.rejected("missing_feedback") : feedback;
    }

    public static WorkspaceTransferExecution rejected(String diagnostics) {
        return new WorkspaceTransferExecution(null, null, null, WorkspaceTransferFeedback.rejected(diagnostics));
    }

    public boolean appliedCompletely() {
        return feedback.appliedCompletely();
    }
}
