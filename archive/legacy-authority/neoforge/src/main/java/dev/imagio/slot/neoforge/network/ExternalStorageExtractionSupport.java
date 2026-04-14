package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

final class ExternalStorageExtractionSupport {
    private ExternalStorageExtractionSupport() {
    }

    static ItemStack extractMatchingIdentity(
            AbstractContainerMenu menu,
            ServerPlayer player,
            StorageViewProviderSession session,
            ItemIdentity identity,
            StorageTransferMode mode
    ) {
        if (session == null || identity == null) {
            return ItemStack.EMPTY;
        }
        return session.extractFromPrimary(menu, player, identity, mode);
    }
}
