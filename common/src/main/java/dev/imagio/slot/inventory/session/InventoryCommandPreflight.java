package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryCommandAvailability;
import dev.imagio.slot.inventory.action.ProjectedRowTransferPlan;
import dev.imagio.slot.inventory.intent.InventoryIntent;
import dev.imagio.slot.workflow.domain.LoadoutApplyService;

import java.util.List;
import java.util.Objects;

public record InventoryCommandPreflight(
        InventoryCommandInvocation invocation,
        InventoryCommandAvailability availability,
        InventoryIntent resolvedIntent,
        List<InventoryActionRequest> requests,
        ProjectedRowTransferPlan transferPlan,
        LoadoutApplyService.LoadoutApplyPlan loadoutPlan,
        String diagnostics
) {
    public InventoryCommandPreflight {
        availability = availability == null ? InventoryCommandAvailability.unavailable(null, "") : availability;
        requests = requests == null ? List.of() : List.copyOf(requests.stream().filter(Objects::nonNull).toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
