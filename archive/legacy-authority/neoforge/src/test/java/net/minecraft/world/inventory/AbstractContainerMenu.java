package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractContainerMenu {
    public final NonNullList<Slot> slots = new NonNullList<>();
    public final int containerId;
    private net.minecraft.world.item.ItemStack carried = net.minecraft.world.item.ItemStack.EMPTY;

    protected AbstractContainerMenu(Object menuType, int containerId) {
        this.containerId = containerId;
    }

    protected Slot addSlot(Slot slot) {
        slots.add(slot);
        return slot;
    }

    public Slot getSlot(int index) {
        return slots.get(index);
    }

    public net.minecraft.world.item.ItemStack getCarried() {
        return carried;
    }

    public void setCarried(net.minecraft.world.item.ItemStack carried) {
        this.carried = carried;
    }

    public void slotsChanged(Container container) {
    }

    public abstract boolean stillValid(Player player);
}
