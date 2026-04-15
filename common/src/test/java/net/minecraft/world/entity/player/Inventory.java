package net.minecraft.world.entity.player;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Inventory implements Container {
    public final Player player;
    public final List<ItemStack> items = new ArrayList<>();
    public final List<ItemStack> armor = new ArrayList<>();
    public final List<ItemStack> offhand = new ArrayList<>();
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
