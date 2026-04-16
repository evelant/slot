package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SlotWorkspaceLdlibMenus {
    public static final ResourceLocation WORKSPACE_ID = SlotCommon.id("workspace");
    private static boolean registered;

    private SlotWorkspaceLdlibMenus() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        PlayerUIMenuType.register(WORKSPACE_ID, player -> new SlotWorkspaceUiHolder());
        registered = true;
    }

    public static boolean open(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        boolean opened = PlayerUIMenuType.openUI(player, WORKSPACE_ID);
        SlotDebugLog.log("SLOT LDLib workspace menu open {}", opened);
        return opened;
    }
}
