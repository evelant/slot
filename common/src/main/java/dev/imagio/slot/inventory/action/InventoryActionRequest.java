package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import net.minecraft.world.item.ItemStack;

public record InventoryActionRequest(
        HostInstanceKey hostId,
        ServerMenuRef serverMenuRef,
        String requestId,
        InventoryActionKind kind,
        InventoryActionMode mode,
        InventoryActionQuantity quantity,
        InventoryActionScope scope,
        InventoryActionConflictPolicy conflictPolicy,
        String origin,
        String correlationId,
        String causationId,
        String sessionId,
        InventoryActionTarget primaryTarget,
        InventoryActionTarget secondaryTarget,
        int requestedCount,
        ItemIdentity identity,
        ItemStack stack,
        InventoryToolActionId toolActionId,
        InventoryToolToggleId toolToggleId,
        boolean desiredToggleState,
        String diagnostics
) {
    public InventoryActionRequest {
        hostId = hostId == null ? HostInstanceKey.empty() : hostId;
        serverMenuRef = serverMenuRef == null ? new ServerMenuRef("", -1) : serverMenuRef;
        requestId = requestId == null ? "" : requestId;
        kind = kind == null ? InventoryActionKind.TRANSFER : kind;
        mode = mode == null ? InventoryActionMode.EXECUTE : mode;
        quantity = quantity == null ? InventoryActionQuantity.DEFAULT : quantity;
        scope = scope == null ? InventoryActionScope.BEST_SINGLE_SOURCE : scope;
        conflictPolicy = conflictPolicy == null ? InventoryActionConflictPolicy.DEFAULT : conflictPolicy;
        origin = origin == null ? "" : origin;
        correlationId = correlationId == null ? "" : correlationId;
        causationId = causationId == null ? "" : causationId;
        sessionId = sessionId == null ? "" : sessionId;
        requestedCount = Math.max(0, requestedCount);
        stack = stack == null ? ItemStack.EMPTY : stack;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public InventoryActionRequest(
            HostInstanceKey hostId,
            ServerMenuRef serverMenuRef,
            String requestId,
            InventoryActionKind kind,
            InventoryActionMode mode,
            InventoryActionQuantity quantity,
            InventoryActionScope scope,
            InventoryActionConflictPolicy conflictPolicy,
            String origin,
            InventoryActionTarget primaryTarget,
            InventoryActionTarget secondaryTarget,
            int requestedCount,
            ItemIdentity identity,
            ItemStack stack,
            InventoryToolActionId toolActionId,
            InventoryToolToggleId toolToggleId,
            boolean desiredToggleState,
            String diagnostics
    ) {
        this(
                hostId,
                serverMenuRef,
                requestId,
                kind,
                mode,
                quantity,
                scope,
                conflictPolicy,
                origin,
                "",
                "",
                "",
                primaryTarget,
                secondaryTarget,
                requestedCount,
                identity,
                stack,
                toolActionId,
                toolToggleId,
                desiredToggleState,
                diagnostics
        );
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
}
