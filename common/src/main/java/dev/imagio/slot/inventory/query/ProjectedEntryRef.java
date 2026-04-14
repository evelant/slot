package dev.imagio.slot.inventory.query;

import net.minecraft.world.item.ItemStack;

public record ProjectedEntryRef(
        InventoryEntryKey entryKey,
        String sourceId,
        ItemStack stack,
        int count
) {
    public ProjectedEntryRef {
        if (entryKey == null) {
            throw new IllegalArgumentException("projected entry ref key must not be null");
        }
        sourceId = sourceId == null ? entryKey.sourceId() : sourceId;
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        count = Math.max(0, count);
    }

    public boolean slotBacked() {
        return entryKey.slotBacked();
    }
}
