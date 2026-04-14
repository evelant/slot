package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryMenu extends AbstractContainerMenu {
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 35;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 44;
    public static final int SHIELD_SLOT = 45;

    public InventoryMenu() {
        super(null, 0);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

}
