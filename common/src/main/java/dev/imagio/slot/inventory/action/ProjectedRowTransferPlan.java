package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.ProjectedInventoryRow;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public record ProjectedRowTransferPlan(
        ProjectedRowTransferIntent intent,
        List<PlannedRowTransferOperation> operations,
        List<ProjectedInventoryRow> blockedRows,
        List<InventoryEntryKey> blockedEntries,
        InventoryActionStatus status,
        List<InventoryCommandReasonCode> reasonCodes,
        List<String> diagnostics,
        int requestedTotalCount,
        int plannedTotalCount,
        boolean capacityUncertain
) {
    public ProjectedRowTransferPlan {
        operations = operations == null ? List.of() : List.copyOf(operations.stream().filter(Objects::nonNull).toList());
        blockedRows = blockedRows == null ? List.of() : List.copyOf(blockedRows.stream().filter(Objects::nonNull).toList());
        blockedEntries = blockedEntries == null ? List.of() : List.copyOf(blockedEntries.stream().filter(Objects::nonNull).toList());
        status = status == null ? InventoryActionStatus.FAILED : status;
        reasonCodes = reasonCodes == null || reasonCodes.isEmpty()
                ? InventoryCommandReasonCode.fromDiagnostics(diagnostics == null ? List.of() : diagnostics)
                : List.copyOf(reasonCodes.stream().filter(Objects::nonNull).distinct().toList());
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics.stream().filter(Objects::nonNull).toList());
        requestedTotalCount = Math.max(0, requestedTotalCount);
        plannedTotalCount = Math.max(0, plannedTotalCount);
        capacityUncertain = capacityUncertain || operations.stream().anyMatch(PlannedRowTransferOperation::capacityUncertain);
    }

    public static ProjectedRowTransferPlan empty(ProjectedRowTransferIntent intent, List<String> diagnostics) {
        InventoryActionStatus status = intent == null ? InventoryActionStatus.FAILED : InventoryActionStatus.BLOCKED;
        return new ProjectedRowTransferPlan(
                intent,
                List.of(),
                List.of(),
                List.of(),
                status,
                InventoryCommandReasonCode.fromDiagnostics(diagnostics),
                diagnostics,
                0,
                0,
                false
        );
    }

    public List<InventoryActionRequest> requests() {
        return operations.stream()
                .flatMap(operation -> operation.steps().stream())
                .map(PlannedTransferStep::request)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<String> blockedEntryStableKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (InventoryEntryKey entryKey : blockedEntries) {
            if (entryKey != null) {
                keys.add(entryKey.stableKey());
            }
        }
        return List.copyOf(keys);
    }
}
