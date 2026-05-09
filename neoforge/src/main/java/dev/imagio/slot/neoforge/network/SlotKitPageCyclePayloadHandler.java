package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.KitPageCycleService;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.neoforge.storage.NeoForgeCarriedActivityTracker;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-side handler for the in-world kit-page cycle hotkey. Mirrors
 * {@code SlotWorkspaceUiSession.switchKitPage} so the result is
 * identical regardless of whether the player triggered the swap from
 * inside the SLOT UI (RPC path) or while in-world (this packet).
 */
public final class SlotKitPageCyclePayloadHandler {
    private SlotKitPageCyclePayloadHandler() {
    }

    public static void handle(SlotKitPageCyclePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            int direction = Integer.signum(payload.direction());
            if (direction == 0) {
                direction = 1;
            }
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            if (runtime == null) {
                return;
            }
            int directionFinal = direction;
            WorkspaceCommandOutcome outcome = KitPageCycleService.switchActivePage(
                    player,
                    runtime,
                    directionFinal,
                    "in_world",
                    actionOutcome -> {
                        if (actionOutcome != null && actionOutcome.successful()) {
                            NeoForgeCarriedActivityTracker.suppressNext(player);
                        }
                    }
            );
            SlotDebugLog.log(
                    "In-world kit-page cycle: direction={} success={} diagnostics={}",
                    directionFinal,
                    outcome.success(),
                    outcome.diagnostics()
            );
        });
    }
}
