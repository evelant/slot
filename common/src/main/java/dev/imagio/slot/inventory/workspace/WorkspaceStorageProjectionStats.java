package dev.imagio.slot.inventory.workspace;

public record WorkspaceStorageProjectionStats(
        int reusedStorageChips,
        int rebuiltStorageChips,
        int removedStorageChips
) {
    public WorkspaceStorageProjectionStats {
        reusedStorageChips = Math.max(0, reusedStorageChips);
        rebuiltStorageChips = Math.max(0, rebuiltStorageChips);
        removedStorageChips = Math.max(0, removedStorageChips);
    }

    public static WorkspaceStorageProjectionStats empty() {
        return new WorkspaceStorageProjectionStats(0, 0, 0);
    }
}
