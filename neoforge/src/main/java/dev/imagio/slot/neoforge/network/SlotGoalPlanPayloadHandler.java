package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotGoalPlanPayloadHandler {
    private SlotGoalPlanPayloadHandler() {
    }

    public static void handle(SlotGoalPlanPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload == null) {
                return;
            }
            WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
            if (runtime == null) {
                return;
            }
            boolean changed = SlotGoalPlanPayload.ACTION_REMOVE.equals(payload.action())
                    ? runtime.goalPlanWorkflow().remove(payload.goalId())
                    : runtime.goalPlanWorkflow().save(payload.goal());
            SlotDebugLog.log(
                    "[goal] persisted goal plan action={} goal={} changed={}",
                    payload.action(),
                    payload.goalId(),
                    changed);
        });
    }
}
