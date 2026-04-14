package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.client.screen.SlotPanelScreen;
import dev.imagio.slot.neoforge.client.SlotRecentLootHooks;
import dev.imagio.slot.network.BackpackContentsSyncPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class BackpackContentsSyncPayloadHandler {
    private BackpackContentsSyncPayloadHandler() {
    }

    public static void handle(BackpackContentsSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SophisticatedBackpackTransferSupport.applyClientContents(payload.backpackUuid(), payload.backpackContents());
            if (Minecraft.getInstance().player != null) {
                SophisticatedBackpackSupport.refreshClientBackpackContents(Minecraft.getInstance().player, payload.backpackUuid());
                SlotRecentLootHooks.onBackpackContentsSynced();
            }
            if (Minecraft.getInstance().screen instanceof SlotPanelScreen slotPanelScreen) {
                slotPanelScreen.slotRefreshContents();
            }
        });
    }
}
