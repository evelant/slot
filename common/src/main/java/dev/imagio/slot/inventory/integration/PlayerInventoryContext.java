package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record PlayerInventoryContext(
        Inventory playerInventory,
        AbstractContainerMenu activeMenu,
        String screenClassName,
        InventoryHostDescriptor activeHost
) {
    public PlayerInventoryContext {
        screenClassName = screenClassName == null ? "" : screenClassName;
    }
}
