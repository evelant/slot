package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarUiHandles;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotSidebarClosePayloadHandler {
    private SlotSidebarClosePayloadHandler() {
    }

    public static void handle(SlotSidebarClosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SlotSidebarUiHandles.close(player);
            }
        });
    }
}
