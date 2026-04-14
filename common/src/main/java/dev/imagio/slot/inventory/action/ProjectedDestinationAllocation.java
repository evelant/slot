package dev.imagio.slot.inventory.action;

record ProjectedDestinationAllocation(
        InventoryActionTarget destinationTarget,
        int acceptedCount,
        boolean capacityUncertain,
        String diagnostics
) {
    ProjectedDestinationAllocation {
        if (destinationTarget == null) {
            throw new IllegalArgumentException("destination target must not be null");
        }
        acceptedCount = Math.max(0, acceptedCount);
        capacityUncertain = capacityUncertain && acceptedCount > 0;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
