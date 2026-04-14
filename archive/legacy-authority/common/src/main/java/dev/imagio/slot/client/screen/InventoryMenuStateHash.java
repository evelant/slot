package dev.imagio.slot.client.screen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntPredicate;

public final class InventoryMenuStateHash {
    private InventoryMenuStateHash() {
    }

    public static int allSlotsAndCarried(AbstractContainerMenu menu) {
        if (menu == null) {
            return 0;
        }

        int hash = sourceSlots(menu, ignored -> true, 1);
        return appendStack(hash, menu.getCarried());
    }

    public static int sourceSlots(AbstractContainerMenu menu, IntPredicate includeMenuSlot, int seed) {
        if (menu == null) {
            return 0;
        }

        int hash = seed;
        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            if (includeMenuSlot != null && !includeMenuSlot.test(menuSlot)) {
                continue;
            }
            hash = appendStack(hash, menu.getSlot(menuSlot).getItem());
        }
        return hash;
    }

    public static int appendInt(int hash, int value) {
        return 31 * hash + value;
    }

    public static int appendStack(int hash, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 31 * hash;
        }

        hash = 31 * hash + BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
        hash = 31 * hash + stack.getCount();
        return 31 * hash + stack.getComponentsPatch().hashCode();
    }
}
