package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.action.InventoryTargetCanonicalizer;

import java.util.LinkedHashSet;
import java.util.Set;

public record PendingInventoryAction(
        String sequenceId,
        String requestId,
        HostInstanceKey hostId,
        String correlationId,
        String causationId,
        String sessionId,
        Set<String> targetKeys
) {
    public PendingInventoryAction {
        sequenceId = sequenceId == null ? "" : sequenceId;
        requestId = requestId == null ? "" : requestId;
        hostId = hostId == null ? HostInstanceKey.empty() : hostId;
        correlationId = correlationId == null ? "" : correlationId;
        causationId = causationId == null ? "" : causationId;
        sessionId = sessionId == null ? "" : sessionId;
        targetKeys = targetKeys == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(targetKeys));
    }

    public static PendingInventoryAction of(
            String sequenceId,
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        LinkedHashSet<String> targetKeys = new LinkedHashSet<>();
        if (request != null) {
            for (InventoryActionTarget target : request.targets()) {
                if (target != null) {
                    targetKeys.add(InventoryTargetCanonicalizer.canonicalKey(host, target));
                }
            }
        }
        return new PendingInventoryAction(
                sequenceId,
                request == null ? "" : request.requestId(),
                request == null ? HostInstanceKey.empty() : request.hostId(),
                request == null ? "" : request.correlationId(),
                request == null ? "" : request.causationId(),
                request == null ? "" : request.sessionId(),
                targetKeys
        );
    }
}
