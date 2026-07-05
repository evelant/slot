package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.InventoryActivityEvent;

import java.util.List;

/**
 * Result of a SlotWorkspaceCommandService invocation. The platform adapter (UI session)
 * copies {@link #status()} / {@link #diagnostics()} into its state fields so the next
 * view broadcast reflects the outcome; {@link #success()} decides whether a post-apply
 * refresh should be skipped on rejection paths.
 */
public record WorkspaceCommandOutcome(
        boolean success,
        String status,
        String diagnostics,
        List<InventoryActivityEvent> activityEvents,
        List<WorkspaceInvalidation> invalidations
) {
    public WorkspaceCommandOutcome {
        status = status == null ? "" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
        activityEvents = activityEvents == null ? List.of() : List.copyOf(activityEvents);
        invalidations = invalidations == null ? List.of() : List.copyOf(invalidations);
    }

    public WorkspaceCommandOutcome(boolean success, String status, String diagnostics) {
        this(success, status, diagnostics, List.of());
    }

    public WorkspaceCommandOutcome(
            boolean success,
            String status,
            String diagnostics,
            List<InventoryActivityEvent> activityEvents
    ) {
        this(success, status, diagnostics, activityEvents, List.of());
    }

    public static WorkspaceCommandOutcome accepted(String status, String diagnostics) {
        return new WorkspaceCommandOutcome(true, status, diagnostics);
    }

    public WorkspaceCommandOutcome withActivityEvents(List<InventoryActivityEvent> events) {
        return new WorkspaceCommandOutcome(success, status, diagnostics, events, invalidations);
    }

    public WorkspaceCommandOutcome withInvalidations(List<WorkspaceInvalidation> invalidations) {
        return new WorkspaceCommandOutcome(success, status, diagnostics, activityEvents, invalidations);
    }

    public static WorkspaceCommandOutcome rejected(String diagnostics) {
        return new WorkspaceCommandOutcome(false, "rejected", diagnostics == null || diagnostics.isBlank() ? "rejected" : diagnostics);
    }
}
