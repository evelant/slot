package dev.imagio.slot.storage.adapter;

import net.minecraft.world.item.ItemStack;

public record ExternalStorageStackSnapshot(int handle, ItemStack stack, int count) {
    public ExternalStorageStackSnapshot {
        if (handle < 0) {
            throw new IllegalArgumentException("handle must not be negative");
        }
        stack = stack == null ? ItemStack.EMPTY : stack;
        count = Math.max(0, count);
    }
}
