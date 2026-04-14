package dev.imagio.slot.client.screen;

import net.minecraft.world.item.ItemStack;

public record SlotTrashWarningState(ItemStack nextStack, boolean pausedForExternalStorage) {
    public static final SlotTrashWarningState NONE = new SlotTrashWarningState(ItemStack.EMPTY, false);

    public SlotTrashWarningState {
        nextStack = nextStack == null || nextStack.isEmpty() ? ItemStack.EMPTY : nextStack.copy();
    }

    public boolean active() {
        return !nextStack.isEmpty();
    }
}
