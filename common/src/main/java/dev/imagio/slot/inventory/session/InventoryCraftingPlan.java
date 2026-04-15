package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;

import java.util.List;
import java.util.Objects;

public record InventoryCraftingPlan(
        List<InventoryActionRequest> requests,
        List<InventoryCommandReasonCode> reasonCodes,
        String diagnostics
) {
    public InventoryCraftingPlan {
        requests = requests == null ? List.of() : List.copyOf(requests.stream().filter(Objects::nonNull).toList());
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes.stream().filter(Objects::nonNull).distinct().toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static InventoryCraftingPlan rejected(
            String diagnostics,
            InventoryCommandReasonCode... reasonCodes
    ) {
        return new InventoryCraftingPlan(
                List.of(),
                reasonCodes == null ? List.of() : java.util.Arrays.stream(reasonCodes).filter(Objects::nonNull).toList(),
                diagnostics
        );
    }

    public boolean dispatchable() {
        return !requests.isEmpty();
    }
}
