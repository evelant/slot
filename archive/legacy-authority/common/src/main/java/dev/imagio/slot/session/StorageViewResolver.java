package dev.imagio.slot.session;

import dev.imagio.slot.storage.provider.StorageViewProviderContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class StorageViewResolver {
    private StorageViewResolver() {
    }

    public static InventoryHostDescriptor resolve(
            Component title,
            AbstractContainerMenu menu,
            Inventory playerInventory,
            String screenClassName
    ) {
        return resolve(title, menu, playerInventory, screenClassName, false, false, false);
    }

    public static InventoryHostDescriptor resolve(
            Component title,
            AbstractContainerMenu menu,
            Inventory playerInventory,
            String screenClassName,
            boolean slotOwned,
            boolean recordsRecent,
            boolean carriedOnly
    ) {
        if (menu == null || playerInventory == null) {
            return null;
        }

        ChestLikeMenuLayout layout = ChestLikeMenuLayout.resolve(
                new StorageViewProviderContext(menu, playerInventory, title, screenClassName)
        );
        if (layout == null) {
            return null;
        }

        return InventoryHostDescriptor.create(
                screenClassName,
                title,
                menu,
                layout,
                playerInventory.selected,
                slotOwned,
                recordsRecent,
                carriedOnly
        );
    }
}
