package dev.imagio.slot.storage.provider;

import net.minecraft.world.item.ItemStack;

public record SupplementalCarriedStackSnapshot(
        String sourceId,
        int slotIndex,
        ItemStack stack
) {
    public SupplementalCarriedStackSnapshot {
        sourceId = sourceId == null ? "" : sourceId;
        stack = stack == null ? ItemStack.EMPTY : stack;
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must not be negative");
        }
    }
}
