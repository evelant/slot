package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.world.entity.player.Player;

final class SlotWorkspaceUiFactory {
    private SlotWorkspaceUiFactory() {
    }

    static ModularUI create(SlotWorkspaceUiSession session, Player player) {
        return new SlotWorkspaceUiController(session, player).create();
    }
}
