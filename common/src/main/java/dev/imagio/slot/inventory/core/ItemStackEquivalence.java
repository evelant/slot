package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

public final class ItemStackEquivalence {
    private ItemStackEquivalence() {
    }

    public static boolean sameItemAndData(ItemStack first, ItemStack second) {
        return SlotStackAccess.current().sameItemAndData(first, second);
    }
}
