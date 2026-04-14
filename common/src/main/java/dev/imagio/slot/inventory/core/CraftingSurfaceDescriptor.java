package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.action.InventoryActionTarget;

import java.util.List;

public record CraftingSurfaceDescriptor(
        List<InventoryActionTarget.SourceSlotTarget> inputSlotTargets,
        InventoryActionTarget.SourceSlotTarget outputSlotTarget,
        boolean supportsImmediateCraft,
        boolean supportsClearGrid,
        boolean supportsBalanceGrid,
        boolean supportsRotateGrid,
        String diagnostics
) {
    public CraftingSurfaceDescriptor {
        inputSlotTargets = inputSlotTargets == null ? List.of() : List.copyOf(inputSlotTargets);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public boolean present() {
        return !inputSlotTargets.isEmpty() && outputSlotTarget != null;
    }
}
