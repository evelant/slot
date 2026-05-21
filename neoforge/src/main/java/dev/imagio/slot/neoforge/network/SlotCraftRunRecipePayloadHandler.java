package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotCraftRunRecipePayloadHandler {
    private SlotCraftRunRecipePayloadHandler() {
    }

    public static void handle(SlotCraftRunRecipePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload == null) {
                return;
            }
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            if (runtime == null) {
                return;
            }
            boolean changed = runtime.craftRunWorkflow().add(payload.capture());
            SlotDebugLog.log(
                    "[craft-run] add visible EMI recipe changed={} player={}",
                    changed,
                    player.getGameProfile().getName());
        });
    }
}
