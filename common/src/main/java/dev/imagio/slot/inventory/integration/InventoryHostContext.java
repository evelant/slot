package dev.imagio.slot.inventory.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record InventoryHostContext(
        AbstractContainerMenu menu,
        Inventory playerInventory,
        Component title,
        String screenClassName,
        boolean slotOwned,
        boolean recordsRecent,
        boolean carriedOnly
) {
    public InventoryHostContext {
        title = title == null ? Component.empty() : title;
        screenClassName = screenClassName == null ? "" : screenClassName;
    }
}
