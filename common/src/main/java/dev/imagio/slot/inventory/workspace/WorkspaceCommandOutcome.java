package dev.imagio.slot.inventory.workspace;

/**
 * Result of a SlotWorkspaceCommandService invocation. The platform adapter (UI session)
 * copies {@link #status()} / {@link #diagnostics()} into its state fields so the next
 * view broadcast reflects the outcome; {@link #success()} decides whether a post-apply
 * refresh should be skipped on rejection paths.
 */
public record WorkspaceCommandOutcome(
        boolean success,
        String status,
        String diagnostics
) {
    public WorkspaceCommandOutcome {
        status = status == null ? "" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static WorkspaceCommandOutcome accepted(String status, String diagnostics) {
        return new WorkspaceCommandOutcome(true, status, diagnostics);
    }

    public static WorkspaceCommandOutcome rejected(String diagnostics) {
        return new WorkspaceCommandOutcome(false, "rejected", diagnostics == null || diagnostics.isBlank() ? "rejected" : diagnostics);
    }
}
