package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotTrashIdentityPayloadHandler {
    private SlotTrashIdentityPayloadHandler() {
    }

    public static void handle(SlotTrashIdentityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload == null) {
                return;
            }
            WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.trashIdentity(
                    player,
                    SlotPlayerWorkflowRuntimeService.runtime(player),
                    payload.itemId(),
                    payload.comparisonMode(),
                    payload.componentFingerprint());
            SlotCommon.LOGGER.info(
                    "[SLOT] trash hover hotkey: player={} item={} status={} diagnostics={}",
                    player.getName().getString(),
                    payload.itemId(),
                    outcome.status(),
                    outcome.diagnostics());
        });
    }
}
