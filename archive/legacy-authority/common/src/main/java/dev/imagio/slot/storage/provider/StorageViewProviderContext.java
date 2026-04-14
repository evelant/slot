package dev.imagio.slot.storage.provider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record StorageViewProviderContext(
        AbstractContainerMenu menu,
        Inventory playerInventory,
        Component openContainerTitle,
        String screenClassName
) {
    public StorageViewProviderContext {
        if (menu == null) {
            throw new IllegalArgumentException("menu must not be null");
        }
        if (playerInventory == null) {
            throw new IllegalArgumentException("playerInventory must not be null");
        }
        screenClassName = screenClassName == null ? "" : screenClassName;
    }
}
