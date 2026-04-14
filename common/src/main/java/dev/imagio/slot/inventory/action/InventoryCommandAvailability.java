package dev.imagio.slot.inventory.action;

import java.util.List;
import java.util.Objects;

public record InventoryCommandAvailability(
        boolean available,
        List<InventoryCommandReasonCode> reasonCodes,
        boolean capacityUncertain,
        String diagnostics
) {
    public InventoryCommandAvailability {
        reasonCodes = reasonCodes == null
                ? List.of()
                : List.copyOf(reasonCodes.stream().filter(Objects::nonNull).distinct().toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static InventoryCommandAvailability enabled() {
        return new InventoryCommandAvailability(true, List.of(), false, "");
    }

    public static InventoryCommandAvailability unavailable(
            InventoryCommandReasonCode reasonCode,
            String diagnostics
    ) {
        return new InventoryCommandAvailability(false, reasonCode == null ? List.of() : List.of(reasonCode), false, diagnostics);
    }
}
