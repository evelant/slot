package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.storage.ChestClaimServerService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotChestClaimPayloadHandler {
    private SlotChestClaimPayloadHandler() {
    }

    public static void handle(SlotChestClaimPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            MinecraftServer server = player.getServer();
            if (server == null) {
                return;
            }
            ServerLevel level = server.getLevel(payload.dimension());
            if (level == null || level != player.serverLevel()) {
                SlotCommon.LOGGER.debug("[SLOT] claim rejected: dimension mismatch payload={} player={}",
                        payload.dimension().location(),
                        player.serverLevel().dimension().location());
                return;
            }
            if (!player.canInteractWithBlock(payload.pos(), 4.0)) {
                SlotCommon.LOGGER.debug("[SLOT] claim rejected: out of reach at {}", payload.pos());
                return;
            }
            ClaimedChest claimed = ChestClaimServerService.claim(player, payload.pos());
            if (claimed == null) {
                SlotCommon.LOGGER.debug("[SLOT] claim failed at {}", payload.pos());
            }
        });
    }
}
