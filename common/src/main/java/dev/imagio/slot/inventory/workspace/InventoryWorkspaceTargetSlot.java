package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionTarget;

public record InventoryWorkspaceTargetSlot(
        String id,
        InventoryActionTarget target,
        InventoryWorkspaceSubjectRef subjectRef,
        int logicalIndex,
        boolean selected,
        boolean active,
        String diagnostics
) {
    public InventoryWorkspaceTargetSlot {
        id = id == null ? "" : id;
        logicalIndex = Math.max(0, logicalIndex);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
