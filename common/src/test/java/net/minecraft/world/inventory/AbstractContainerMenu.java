package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractContainerMenu {
    public final int containerId;
    public final NonNullList<Slot> slots = NonNullList.create();
    private ItemStack carried = ItemStack.EMPTY;

    protected AbstractContainerMenu(Object menuType, int containerId) {
        this.containerId = containerId;
    }

    public Slot getSlot(int slotId) {
        return slots.get(slotId);
    }

    protected Slot addSlot(Slot slot) {
        slots.add(slot);
        return slot;
    }

    public ItemStack getCarried() {
        return carried == null ? ItemStack.EMPTY : carried;
    }

    public void setCarried(ItemStack stack) {
        this.carried = stack == null ? ItemStack.EMPTY : stack;
    }

    public void broadcastChanges() {
    }

    public abstract boolean stillValid(Player player);
}
