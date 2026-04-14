package dev.imagio.slot.inventory.query;

import net.minecraft.world.item.ItemStack;

public record InventoryEntrySnapshot(
        InventoryEntryKey entryKey,
        ItemStack stack,
        int count,
        String diagnostics
) {
    public InventoryEntrySnapshot {
        if (entryKey == null) {
            throw new IllegalArgumentException("entry key must not be null");
        }
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        count = Math.max(0, count);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public String sourceId() {
        return entryKey.sourceId();
    }

    public boolean slotBacked() {
        return entryKey.slotBacked();
    }

    public int slotIndex() {
        return entryKey.slotIndex();
    }

    public String entryId() {
        return entryKey.entryId();
    }

    public boolean present() {
        return !stack.isEmpty() && count > 0;
    }
}
