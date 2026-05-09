package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.workspace.KitGatherService;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-side handler for the in-world gather hotkey. Delegates to
 * {@link KitGatherService} so the in-screen RPC path and the in-world
 * packet path produce identical state changes.
 */
public final class SlotGatherActiveKitPayloadHandler {
    private SlotGatherActiveKitPayloadHandler() {
    }

    public static void handle(SlotGatherActiveKitPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            KitGatherService.Outcome outcome = KitGatherService.gatherActiveKit(
                    player,
                    SlotPlayerWorkflowRuntimeService.runtime(player));
            SlotCommon.LOGGER.info(
                    "[SLOT] gather hotkey (in-world): player={} reason={} pulled={} unreachable={}",
                    player.getName().getString(),
                    outcome.reason(),
                    outcome.totalItemsPulled(),
                    outcome.identitiesUnreachable()
            );
        });
    }
}
