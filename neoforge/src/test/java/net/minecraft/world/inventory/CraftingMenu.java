package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Player;

public class CraftingMenu extends AbstractContainerMenu {
    public CraftingMenu() {
        super(null, 0);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
