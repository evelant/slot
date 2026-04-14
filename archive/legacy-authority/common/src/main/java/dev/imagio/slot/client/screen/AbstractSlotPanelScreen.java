package dev.imagio.slot.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public abstract class AbstractSlotPanelScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> implements SlotPanelScreen {
    protected AbstractSlotPanelScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public final SlotPanelBounds slotPanelBounds() {
        return new SlotPanelBounds(slotPanelLeft(), slotPanelTop(), slotPanelVisualWidth(), slotPanelHeight());
    }

    protected abstract int slotPanelLeft();

    protected abstract int slotPanelTop();

    protected abstract int slotPanelVisualWidth();

    protected abstract int slotPanelHeight();
}
