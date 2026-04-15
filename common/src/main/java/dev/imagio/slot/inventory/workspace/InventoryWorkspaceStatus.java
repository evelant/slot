package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;

import java.util.List;
import java.util.Objects;

public record InventoryWorkspaceStatus(
        InventoryHostFamilyHint hostFamilyHint,
        boolean carriedOnly,
        int pendingActionCount,
        boolean toolDockPresent,
        boolean craftingPresent,
        List<String> diagnostics
) {
    public InventoryWorkspaceStatus {
        hostFamilyHint = hostFamilyHint == null ? InventoryHostFamilyHint.UNKNOWN : hostFamilyHint;
        pendingActionCount = Math.max(0, pendingActionCount);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList());
    }
}
