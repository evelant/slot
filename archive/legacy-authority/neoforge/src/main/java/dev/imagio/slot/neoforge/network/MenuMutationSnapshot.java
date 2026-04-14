package dev.imagio.slot.neoforge.network;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class MenuMutationSnapshot {
    private final List<ItemStack> slotStacks;
    private final ItemStack carriedStack;

    private MenuMutationSnapshot(List<ItemStack> slotStacks, ItemStack carriedStack) {
        this.slotStacks = slotStacks == null ? List.of() : List.copyOf(slotStacks);
        this.carriedStack = carriedStack == null ? ItemStack.EMPTY : carriedStack.copy();
    }

    static MenuMutationSnapshot capture(AbstractContainerMenu menu) {
        if (menu == null) {
            return new MenuMutationSnapshot(List.of(), ItemStack.EMPTY);
        }

        List<ItemStack> slotStacks = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            slotStacks.add(slot == null ? ItemStack.EMPTY : slot.getItem().copy());
        }
        return new MenuMutationSnapshot(slotStacks, menu.getCarried().copy());
    }

    void restore(AbstractContainerMenu menu) {
        if (menu == null || slotStacks.size() != menu.slots.size()) {
            return;
        }

        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            Slot slot = menu.getSlot(menuSlot);
            if (slot == null) {
                continue;
            }
            slot.set(slotStacks.get(menuSlot).copy());
            slot.setChanged();
        }
        menu.setCarried(carriedStack.copy());
        menu.broadcastChanges();
    }
}
