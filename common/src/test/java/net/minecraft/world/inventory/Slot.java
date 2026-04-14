package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
    public final Object container;
    private final int containerSlot;
    private ItemStack item;

    public Slot(Object container, int containerSlot) {
        this(container, containerSlot, ItemStack.EMPTY);
    }

    public Slot(Object container, int containerSlot, ItemStack item) {
        this.container = container;
        this.containerSlot = containerSlot;
        this.item = item == null ? ItemStack.EMPTY : item;
    }

    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    public ItemStack getItem() {
        return item == null ? ItemStack.EMPTY : item;
    }

    public void set(ItemStack stack) {
        this.item = stack == null ? ItemStack.EMPTY : stack;
    }

    public int getContainerSlot() {
        return containerSlot;
    }

    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    public boolean mayPickup(Player player) {
        return true;
    }

    public int getMaxStackSize(ItemStack stack) {
        return stack == null ? 64 : stack.getMaxStackSize();
    }

    public ItemStack safeInsert(ItemStack sourceStack) {
        if (sourceStack == null || sourceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (item == null || item.isEmpty()) {
            set(sourceStack.copy());
            return ItemStack.EMPTY;
        }
        if (!ItemStack.isSameItemSameComponents(item, sourceStack)) {
            return sourceStack;
        }
        int capacity = Math.max(0, Math.min(item.getMaxStackSize(), getMaxStackSize(item)) - item.getCount());
        if (capacity <= 0) {
            return sourceStack;
        }
        int moved = Math.min(capacity, sourceStack.getCount());
        item.setCount(item.getCount() + moved);
        ItemStack remainder = sourceStack.copy();
        remainder.setCount(sourceStack.getCount() - moved);
        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    public ItemStack safeTake(int requestedAmount, int limit, Player player) {
        if (item == null || item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int extracted = Math.min(item.getCount(), Math.max(0, Math.min(requestedAmount, limit)));
        if (extracted <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = item.copy();
        result.setCount(extracted);
        item.setCount(item.getCount() - extracted);
        if (item.getCount() <= 0) {
            item = ItemStack.EMPTY;
        }
        return result;
    }
}
