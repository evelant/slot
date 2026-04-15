package dev.imagio.slot.neoforge.client.host;

import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ObservedScreenContext(
        AbstractContainerScreen<?> screen,
        AbstractContainerMenu menu,
        String screenClassName,
        Component title,
        Inventory playerInventory,
        InventoryHostObservationHints observationHints
) {
    public ObservedScreenContext {
        screenClassName = screenClassName == null ? "" : screenClassName;
        title = title == null ? Component.empty() : title;
        observationHints = observationHints == null ? InventoryHostObservationHints.defaults() : observationHints;
    }

    public InventoryHostContext toHostContext() {
        return new InventoryHostContext(menu, playerInventory, title, screenClassName, observationHints);
    }
}
