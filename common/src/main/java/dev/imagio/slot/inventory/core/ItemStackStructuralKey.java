package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

/**
 * Component-insensitive key for deciding whether an inventory slot changed
 * enough to rebuild SLOT's organization view.
 */
public record ItemStackStructuralKey(String itemId, int count) {
    public static final ItemStackStructuralKey EMPTY = new ItemStackStructuralKey("", 0);

    public ItemStackStructuralKey {
        itemId = itemId == null ? "" : itemId;
        count = Math.max(0, count);
    }

    public static ItemStackStructuralKey from(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EMPTY;
        }
        return new ItemStackStructuralKey(SlotStackAccess.current().itemId(stack), stack.getCount());
    }
}
