package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.query.ProjectedInventoryRow;

import java.util.List;
import java.util.Objects;

public record PlannedRowTransferOperation(
        ProjectedInventoryRow row,
        List<PlannedTransferStep> steps,
        InventoryActionStatus status,
        List<InventoryCommandReasonCode> reasonCodes,
        int requestedCount,
        int plannedCount,
        boolean capacityUncertain,
        List<String> diagnostics
) {
    public PlannedRowTransferOperation {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null");
        }
        steps = steps == null ? List.of() : List.copyOf(steps.stream().filter(Objects::nonNull).toList());
        status = status == null ? InventoryActionStatus.FAILED : status;
        reasonCodes = reasonCodes == null || reasonCodes.isEmpty()
                ? InventoryCommandReasonCode.fromDiagnostics(diagnostics == null ? List.of() : diagnostics)
                : List.copyOf(reasonCodes.stream().filter(Objects::nonNull).distinct().toList());
        requestedCount = Math.max(0, requestedCount);
        plannedCount = Math.max(0, plannedCount);
        capacityUncertain = capacityUncertain && plannedCount > 0;
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics.stream().filter(Objects::nonNull).toList());
    }
}
