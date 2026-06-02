package dev.imagio.slot.inventory.workspace;

record WorkspaceProjectionFrame(
        String status,
        String diagnostics,
        int pendingCount,
        int selectedQuickAccessSlot,
        long revision
) {
    WorkspaceProjectionFrame {
        status = status == null || status.isBlank() ? "ready" : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
        pendingCount = Math.max(0, pendingCount);
    }
}
