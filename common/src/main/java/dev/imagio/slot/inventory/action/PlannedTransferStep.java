package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.query.ProjectedEntryRef;

public record PlannedTransferStep(
        ProjectedEntryRef sourceEntry,
        InventoryActionRequest request,
        InventoryActionStatus status,
        java.util.List<InventoryCommandReasonCode> reasonCodes,
        int requestedCount,
        int plannedCount,
        boolean capacityUncertain,
        String diagnostics
) {
    public PlannedTransferStep {
        if (sourceEntry == null) {
            throw new IllegalArgumentException("source entry must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        status = status == null ? InventoryActionStatus.FAILED : status;
        reasonCodes = reasonCodes == null || reasonCodes.isEmpty()
                ? InventoryCommandReasonCode.fromDiagnostics(diagnostics == null || diagnostics.isBlank() ? java.util.List.of() : java.util.List.of(diagnostics))
                : java.util.List.copyOf(reasonCodes.stream().filter(java.util.Objects::nonNull).distinct().toList());
        requestedCount = Math.max(0, requestedCount);
        plannedCount = Math.max(0, plannedCount);
        capacityUncertain = capacityUncertain && plannedCount > 0;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
