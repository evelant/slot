package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;

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
        String diagnostics,
        SlotResourceIdentity resourceIdentity,
        long amount
) {
    public InventoryActivityEvent(
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
        this(
                kind,
                producer,
                confidence,
                identity,
                count,
                fromTarget,
                toTarget,
                requestId,
                recoveryToken,
                reasonCodes,
                diagnostics,
                SlotResourceIdentity.item(identity),
                count);
    }

    public InventoryActivityEvent {
        kind = kind == null ? InventoryActivityKind.ACQUIRED : kind;
        producer = producer == null ? InventoryActivityProducer.UNKNOWN_EXTERNAL : producer;
        confidence = confidence == null ? InventoryActivityConfidence.OBSERVED : confidence;
        identity = ItemIdentityCollections.key(identity);
        resourceIdentity = SlotResourceCollections.key(resourceIdentity != null
                ? resourceIdentity
                : SlotResourceIdentity.item(identity));
        count = Math.max(0, count);
        amount = Math.max(0L, amount);
        if (resourceIdentity != null && resourceIdentity.item()) {
            if (count <= 0 && amount > 0L) {
                count = saturatedInt(amount);
            }
            amount = count;
            identity = resourceIdentity.toItemIdentity();
        } else if (resourceIdentity != null && resourceIdentity.fluid()) {
            if (amount <= 0L && count > 0) {
                amount = count;
            }
            count = saturatedInt(amount);
            identity = null;
        } else {
            amount = count;
        }
        requestId = requestId == null ? "" : requestId;
        recoveryToken = recoveryToken == null ? "" : recoveryToken;
        reasonCodes = reasonCodes == null
                ? List.of()
                : List.copyOf(reasonCodes.stream().filter(java.util.Objects::nonNull).distinct().toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public boolean present() {
        return resourceIdentity != null && amount > 0L;
    }

    public boolean itemPresent() {
        return identity != null && count > 0;
    }

    private static int saturatedInt(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
