package dev.imagio.slot.debug;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record ChestContentEntry(int slot, ItemStack stack) {
    public ChestContentEntry {
        Objects.requireNonNull(stack, "stack");
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be non-negative");
        }
    }
}
