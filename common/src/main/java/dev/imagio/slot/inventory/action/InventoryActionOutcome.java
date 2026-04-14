package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.List;

public record InventoryActionOutcome(
        HostInstanceKey hostId,
        ServerMenuRef serverMenuRef,
        String requestId,
        InventoryActionKind kind,
        InventoryActionMode mode,
        String origin,
        String correlationId,
        String causationId,
        String sessionId,
        InventoryActionTarget primaryTarget,
        InventoryActionTarget secondaryTarget,
        InventoryActionStatus status,
        List<InventoryCommandReasonCode> reasonCodes,
        int requestedCount,
        int appliedCount,
        boolean capacityUncertain,
        List<InventoryActivityEvent> activityEvents,
        ItemStack stackRemainder,
        String diagnostics
) {
    public InventoryActionOutcome {
        hostId = hostId == null ? HostInstanceKey.empty() : hostId;
        serverMenuRef = serverMenuRef == null ? new ServerMenuRef("", -1) : serverMenuRef;
        requestId = requestId == null ? "" : requestId;
        kind = kind == null ? InventoryActionKind.TRANSFER_ONE : kind;
        mode = mode == null ? InventoryActionMode.EXECUTE : mode;
        origin = origin == null ? "" : origin;
        correlationId = correlationId == null ? "" : correlationId;
        causationId = causationId == null ? "" : causationId;
        sessionId = sessionId == null ? "" : sessionId;
        status = status == null ? InventoryActionStatus.FAILED : status;
        reasonCodes = reasonCodes == null || reasonCodes.isEmpty()
                ? InventoryCommandReasonCode.fromDiagnostics(diagnostics == null || diagnostics.isBlank() ? List.of() : List.of(diagnostics))
                : List.copyOf(reasonCodes.stream().filter(java.util.Objects::nonNull).distinct().toList());
        requestedCount = Math.max(0, requestedCount);
        appliedCount = Math.max(0, appliedCount);
        capacityUncertain = capacityUncertain && appliedCount > 0;
        activityEvents = activityEvents == null
                ? List.of()
                : List.copyOf(activityEvents.stream().filter(InventoryActivityEvent::present).toList());
        stackRemainder = stackRemainder == null ? ItemStack.EMPTY : stackRemainder;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public InventoryActionOutcome(
            HostInstanceKey hostId,
            ServerMenuRef serverMenuRef,
            String requestId,
            InventoryActionKind kind,
            InventoryActionMode mode,
            String origin,
            InventoryActionTarget primaryTarget,
            InventoryActionTarget secondaryTarget,
            boolean successful,
            List<InventoryActivityEvent> activityEvents,
            ItemStack stackRemainder,
            String diagnostics
    ) {
        this(
                hostId,
                serverMenuRef,
                requestId,
                kind,
                mode,
                origin,
                "",
                "",
                "",
                primaryTarget,
                secondaryTarget,
                successful ? InventoryActionStatus.SUCCESS : InventoryActionStatus.BLOCKED,
                InventoryCommandReasonCode.fromDiagnostics(diagnostics == null || diagnostics.isBlank() ? List.of() : List.of(diagnostics)),
                0,
                successful ? 1 : 0,
                false,
                activityEvents,
                stackRemainder,
                diagnostics
        );
    }

    public InventoryActionOutcome(
            HostInstanceKey hostId,
            ServerMenuRef serverMenuRef,
            String requestId,
            InventoryActionKind kind,
            InventoryActionMode mode,
            String origin,
            InventoryActionTarget primaryTarget,
            InventoryActionTarget secondaryTarget,
            InventoryActionStatus status,
            List<InventoryCommandReasonCode> reasonCodes,
            int requestedCount,
            int appliedCount,
            boolean capacityUncertain,
            List<InventoryActivityEvent> activityEvents,
            ItemStack stackRemainder,
            String diagnostics
    ) {
        this(
                hostId,
                serverMenuRef,
                requestId,
                kind,
                mode,
                origin,
                "",
                "",
                "",
                primaryTarget,
                secondaryTarget,
                status,
                reasonCodes,
                requestedCount,
                appliedCount,
                capacityUncertain,
                activityEvents,
                stackRemainder,
                diagnostics
        );
    }

    public boolean successful() {
        return status.successful();
    }

    public java.util.Set<InventoryActionTarget> targets() {
        java.util.LinkedHashSet<InventoryActionTarget> targets = new java.util.LinkedHashSet<>();
        if (primaryTarget != null) {
            targets.add(primaryTarget);
        }
        if (secondaryTarget != null) {
            targets.add(secondaryTarget);
        }
        return java.util.Set.copyOf(targets);
    }

    public java.util.Set<String> targetKeys(dev.imagio.slot.inventory.core.InventoryHostDescriptor host) {
        LinkedHashSet<String> targetKeys = new LinkedHashSet<>();
        for (InventoryActionTarget target : targets()) {
            if (target != null) {
                targetKeys.add(InventoryTargetCanonicalizer.canonicalKey(host, target));
            }
        }
        return java.util.Set.copyOf(targetKeys);
    }
}
