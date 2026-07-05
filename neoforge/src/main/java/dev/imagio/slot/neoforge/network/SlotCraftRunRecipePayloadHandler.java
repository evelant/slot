package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceCraftRunCommandService;
import dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarUiHandles;
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
            WorkspaceCommandOutcome outcome = WorkspaceCraftRunCommandService.addRecipe(runtime, payload.capture());
            boolean broadcast = SlotSidebarUiHandles.applyExternalOutcome(player, outcome);
            SlotDebugLog.log(
                    "[craft-run] add visible EMI recipe status={} broadcast={} player={}",
                    outcome.status(),
                    broadcast,
                    player.getGameProfile().getName());
        });
    }
}
