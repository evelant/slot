package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.action.InventoryActionTarget;

import java.util.List;

public record CraftingSurfaceDescriptor(
        List<InventoryActionTarget.SourceSlotTarget> inputSlotTargets,
        InventoryActionTarget.SourceSlotTarget outputSlotTarget,
        int gridWidth,
        int gridHeight,
        boolean supportsImmediateCraft,
        boolean supportsClearGrid,
        boolean supportsBalanceGrid,
        boolean supportsRotateGrid,
        String diagnostics
) {
    public CraftingSurfaceDescriptor {
        inputSlotTargets = inputSlotTargets == null ? List.of() : List.copyOf(inputSlotTargets);
        if (gridWidth <= 0 || gridHeight <= 0) {
            int[] inferred = inferGrid(inputSlotTargets.size());
            gridWidth = inferred[0];
            gridHeight = inferred[1];
        }
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public boolean present() {
        return !inputSlotTargets.isEmpty()
                && outputSlotTarget != null
                && gridWidth > 0
                && gridHeight > 0
                && (gridWidth * gridHeight) >= inputSlotTargets.size();
    }

    public InventoryActionTarget.SourceSlotTarget inputSlotTarget(int index) {
        return index < 0 || index >= inputSlotTargets.size() ? null : inputSlotTargets.get(index);
    }

    public int inputSlotCount() {
        return inputSlotTargets.size();
    }

    private static int[] inferGrid(int inputCount) {
        if (inputCount == 4) {
            return new int[]{2, 2};
        }
        if (inputCount == 9) {
            return new int[]{3, 3};
        }
        if (inputCount <= 0) {
            return new int[]{0, 0};
        }
        return new int[]{inputCount, 1};
    }
}
