package dev.imagio.slot.inventory.workspace;

public record WorkspaceProjectionSliceStats(
        int reusedSlices,
        int rebuiltSlices
) {
    public WorkspaceProjectionSliceStats {
        reusedSlices = Math.max(0, reusedSlices);
        rebuiltSlices = Math.max(0, rebuiltSlices);
    }

    public static WorkspaceProjectionSliceStats empty() {
        return new WorkspaceProjectionSliceStats(0, 0);
    }
}
