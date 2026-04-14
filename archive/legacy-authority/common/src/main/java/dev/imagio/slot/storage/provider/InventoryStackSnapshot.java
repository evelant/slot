package dev.imagio.slot.storage.provider;

import net.minecraft.world.item.ItemStack;

public record InventoryStackSnapshot(
        int handle,
        ItemStack stack,
        int count
) {
    public InventoryStackSnapshot {
        handle = Math.max(-1, handle);
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        count = Math.max(0, count);
    }
}
