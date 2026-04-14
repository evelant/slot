package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.ActionRequestPayload;
import dev.imagio.slot.network.BackpackContentsSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ActionRequestPayloadHandler {
    private ActionRequestPayloadHandler() {
    }

    public static void handle(ActionRequestPayload payload, IPayloadContext context) {
        handle(payload == null ? null : payload.request(), context);
    }

    static void handle(ActionRequest request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || request == null) {
                return;
            }

            SlotServerDispatchResult result = SlotServerActionDispatcher.dispatch(player, request);
            AbstractContainerMenu menu = player.containerMenu;
            if (menu != null) {
                SlotActionOutcomeSupport.send(player, menu, result.outcome());
                if (result.broadcastChanges()) {
                    menu.broadcastChanges();
                }
            }
            result.syncedContents().forEach((uuid, contents) ->
                    PacketDistributor.sendToPlayer(player, new BackpackContentsSyncPayload(uuid, contents))
            );
        });
    }
}
