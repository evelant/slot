package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.world.entity.player.Player;

final class SlotWorkspaceUiHolder implements PlayerUIMenuType.PlayerUIHolder {
    @Override
    public ModularUI createUI(Player player) {
        SlotWorkspaceUiSession session = new SlotWorkspaceUiSession(player);
        return SlotWorkspaceUiFactory.create(session, player)
                .shouldCloseOnEsc(true)
                .shouldCloseOnKeyInventory(true);
    }
}
