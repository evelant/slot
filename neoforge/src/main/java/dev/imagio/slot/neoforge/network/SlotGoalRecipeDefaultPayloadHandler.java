package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotGoalRecipeDefaultPayloadHandler {
    private SlotGoalRecipeDefaultPayloadHandler() {
    }

    public static void handle(SlotGoalRecipeDefaultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload == null) {
                return;
            }
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            if (runtime == null) {
                return;
            }
            boolean changed = runtime.goalRecipeDefaultWorkflow()
                    .set(payload.outputItemId(), payload.recipeId());
            SlotDebugLog.log(
                    "[goal] remembered recipe default output={} recipe={} changed={}",
                    payload.outputItemId(),
                    payload.recipeId(),
                    changed);
        });
    }
}
