package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.network.BackpackContentsRequestPayload;
import dev.imagio.slot.network.BackpackContentsSyncPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.UUID;

public final class BackpackContentsRequestPayloadHandler {
    private BackpackContentsRequestPayloadHandler() {
    }

    public static void handle(BackpackContentsRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Map<UUID, CompoundTag> syncedContents = SophisticatedBackpackTransferSupport.capturePlayerBackpackContents(player);
            SlotDebugLog.log(
                    "Backpack contents sync requested: player={} backpacks={}",
                    player.getGameProfile().getName(),
                    syncedContents.size()
            );
            syncedContents.forEach((uuid, contents) -> PacketDistributor.sendToPlayer(player, new BackpackContentsSyncPayload(uuid, contents)));
        });
    }
}
