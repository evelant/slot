package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.action.ProjectedRowTransferPlan;
import dev.imagio.slot.workflow.domain.LoadoutApplyService;

import java.util.List;
import java.util.Objects;

public record InventoryIntentRoutingResult(
        InventoryRoutingStatus status,
        InventorySessionSnapshot session,
        List<InventoryCommandReasonCode> reasonCodes,
        List<InventoryActionRequest> dispatchedRequests,
        ProjectedRowTransferPlan transferPlan,
        LoadoutApplyService.LoadoutApplyPlan loadoutPlan,
        String diagnostics
) {
    public InventoryIntentRoutingResult {
        status = status == null ? InventoryRoutingStatus.REJECTED : status;
        session = session == null ? InventorySessionSnapshot.empty() : session;
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes.stream().filter(Objects::nonNull).distinct().toList());
        dispatchedRequests = dispatchedRequests == null ? List.of() : List.copyOf(dispatchedRequests.stream().filter(Objects::nonNull).toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static InventoryIntentRoutingResult rejected(
            InventorySessionSnapshot session,
            List<InventoryCommandReasonCode> reasonCodes,
            String diagnostics
    ) {
        InventoryRoutingStatus status = diagnostics != null && diagnostics.contains("stale_session_revision")
                ? InventoryRoutingStatus.STALE
                : InventoryRoutingStatus.REJECTED;
        return new InventoryIntentRoutingResult(status, session, reasonCodes, List.of(), null, null, diagnostics);
    }
}
