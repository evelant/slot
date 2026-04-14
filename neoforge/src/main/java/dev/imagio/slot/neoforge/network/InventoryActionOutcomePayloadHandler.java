package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.inventory.action.InventoryActionOutcomePayload;
import dev.imagio.slot.neoforge.client.SlotNeoForgeClient;
import dev.imagio.slot.inventory.session.InventorySessionCoordinator;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class InventoryActionOutcomePayloadHandler {
    private InventoryActionOutcomePayloadHandler() {
    }

    public static void handle(InventoryActionOutcomePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload == null || payload.outcome() == null) {
                return;
            }
            InventorySessionCoordinator sessionCoordinator = SlotNeoForgeClient.sessionCoordinator();
            if (sessionCoordinator != null) {
                sessionCoordinator.ingestOutcome(payload.outcome());
                return;
            }
            WorkflowDomainRuntime workflowRuntime = SlotNeoForgeClient.workflowRuntime();
            if (workflowRuntime != null) {
                workflowRuntime.recordOutcome(payload.outcome());
            }
        });
    }
}
