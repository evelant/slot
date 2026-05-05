package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarUiHandles;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotSidebarOpenPayloadHandler {
    private SlotSidebarOpenPayloadHandler() {
    }

    public static void handle(SlotSidebarOpenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SlotSidebarUiHandles.open(player);
            }
        });
    }
}
