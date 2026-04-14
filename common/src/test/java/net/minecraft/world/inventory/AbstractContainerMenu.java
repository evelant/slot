package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractContainerMenu {
    public final int containerId;
    public final NonNullList<Slot> slots = NonNullList.create();

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

    public abstract boolean stillValid(Player player);
}
