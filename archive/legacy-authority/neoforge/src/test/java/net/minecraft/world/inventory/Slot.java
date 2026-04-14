package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
    public final Container container;
    private ItemStack item = ItemStack.EMPTY;

    public Slot(Container container, int slot, int x, int y) {
        this.container = container;
    }

    public ItemStack getItem() {
        return item;
    }

    public void set(ItemStack stack) {
        this.item = stack == null ? ItemStack.EMPTY : stack;
    }

    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    public boolean mayPickup(Player player) {
        return true;
    }

    public int getMaxStackSize(ItemStack stack) {
        return stack == null || stack.isEmpty() ? 64 : stack.getMaxStackSize();
    }

    public ItemStack safeInsert(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !mayPlace(stack)) {
            return stack == null ? ItemStack.EMPTY : stack;
        }

        if (item.isEmpty()) {
            int moved = Math.min(stack.getCount(), getMaxStackSize(stack));
            set(stack.copy());
            item.setCount(moved);
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - moved);
            return remainder;
        }

        if (!ItemStack.isSameItemSameComponents(item, stack)) {
            return stack;
        }

        int capacity = Math.max(0, Math.min(item.getMaxStackSize(), getMaxStackSize(item)) - item.getCount());
        if (capacity <= 0) {
            return stack;
        }

        int moved = Math.min(capacity, stack.getCount());
        item.grow(moved);
        ItemStack remainder = stack.copy();
        remainder.setCount(stack.getCount() - moved);
        return remainder;
    }
}
