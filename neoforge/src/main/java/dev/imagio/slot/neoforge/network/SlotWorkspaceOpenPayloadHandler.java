package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceLdlibMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SlotWorkspaceOpenPayloadHandler {
    private SlotWorkspaceOpenPayloadHandler() {
    }

    public static void handle(SlotWorkspaceOpenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof InventoryMenu || player.containerMenu instanceof CraftingMenu)) {
                SlotDebugLog.log(
                        "Ignoring SLOT workspace open request while {} is active",
                        player.containerMenu == null ? "null" : player.containerMenu.getClass().getName()
                );
                return;
            }
            SlotWorkspaceLdlibMenus.open(player);
        });
    }
}
