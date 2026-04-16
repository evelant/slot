package net.minecraft.world.entity.player;

import net.minecraft.world.Container;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class Inventory implements Container {
    public final Player player;
    public final NonNullList<ItemStack> items = NonNullList.create();
    public final NonNullList<ItemStack> armor = NonNullList.create();
    public final NonNullList<ItemStack> offhand = NonNullList.create();
    public int selected;

    public Inventory(Player player) {
        this.player = player;
        for (int index = 0; index < 36; index++) {
            items.add(ItemStack.EMPTY);
        }
        for (int index = 0; index < 4; index++) {
            armor.add(ItemStack.EMPTY);
        }
        offhand.add(ItemStack.EMPTY);
    }
}
