package dev.imagio.slot.inventory.workspace;

public record WorkspaceEdgeProjectionStats(
        int reusedWayfindingTargets,
        int rebuiltWayfindingTargets,
        int removedWayfindingTargets,
        int reusedDepositabilitySets,
        int rebuiltDepositabilitySets
) {
    public WorkspaceEdgeProjectionStats {
        reusedWayfindingTargets = Math.max(0, reusedWayfindingTargets);
        rebuiltWayfindingTargets = Math.max(0, rebuiltWayfindingTargets);
        removedWayfindingTargets = Math.max(0, removedWayfindingTargets);
        reusedDepositabilitySets = Math.max(0, reusedDepositabilitySets);
        rebuiltDepositabilitySets = Math.max(0, rebuiltDepositabilitySets);
    }

    public static WorkspaceEdgeProjectionStats empty() {
        return new WorkspaceEdgeProjectionStats(0, 0, 0, 0, 0);
    }
}
