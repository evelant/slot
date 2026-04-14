package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.List;

public record InventoryActivityEvent(
        InventoryActivityKind kind,
        InventoryActivityProducer producer,
        InventoryActivityConfidence confidence,
        ItemIdentity identity,
        int count,
        InventoryActionTarget fromTarget,
        InventoryActionTarget toTarget,
        String requestId,
        String recoveryToken,
        List<InventoryCommandReasonCode> reasonCodes,
        String diagnostics
) {
    public InventoryActivityEvent {
        kind = kind == null ? InventoryActivityKind.ACQUIRED : kind;
        producer = producer == null ? InventoryActivityProducer.UNKNOWN_EXTERNAL : producer;
        confidence = confidence == null ? InventoryActivityConfidence.OBSERVED : confidence;
        count = Math.max(0, count);
        requestId = requestId == null ? "" : requestId;
        recoveryToken = recoveryToken == null ? "" : recoveryToken;
        reasonCodes = reasonCodes == null
                ? List.of()
                : List.copyOf(reasonCodes.stream().filter(java.util.Objects::nonNull).distinct().toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public boolean present() {
        return identity != null && count > 0;
    }
}
