package dev.imagio.slot.inventory.core;

import net.minecraft.world.item.ItemStack;

public record InventoryStackSnapshot(
        int handle,
        ItemStack stack,
        int count
) {
    public InventoryStackSnapshot {
        stack = stack == null ? ItemStack.EMPTY : stack;
        count = Math.max(0, count);
    }
}
