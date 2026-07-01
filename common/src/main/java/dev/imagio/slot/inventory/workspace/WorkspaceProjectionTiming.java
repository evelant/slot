package dev.imagio.slot.inventory.workspace;

/**
 * Per-call phase timings for the common projection cache.
 */
public record WorkspaceProjectionTiming(
        long inputKeyNanos,
        long projectNanos,
        long contentKeyNanos,
        long totalNanos
) {
    public WorkspaceProjectionTiming {
        inputKeyNanos = Math.max(0L, inputKeyNanos);
        projectNanos = Math.max(0L, projectNanos);
        contentKeyNanos = Math.max(0L, contentKeyNanos);
        totalNanos = Math.max(0L, totalNanos);
    }

    public static WorkspaceProjectionTiming empty() {
        return new WorkspaceProjectionTiming(0L, 0L, 0L, 0L);
    }

    public static double millis(long nanos) {
        return nanos / 1_000_000.0D;
    }
}
