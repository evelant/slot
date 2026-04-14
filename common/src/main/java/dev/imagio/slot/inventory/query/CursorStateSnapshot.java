package dev.imagio.slot.inventory.query;

import net.minecraft.world.item.ItemStack;

public record CursorStateSnapshot(
        ItemStack stack,
        String diagnostics
) {
    public CursorStateSnapshot {
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static CursorStateSnapshot empty() {
        return new CursorStateSnapshot(ItemStack.EMPTY, "");
    }

    public boolean present() {
        return stack != null && !stack.isEmpty();
    }
}
