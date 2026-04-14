package dev.imagio.slot.storage.provider;

import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public interface SupplementalCarriedSourceProvider {
    String providerId();

    default int priority() {
        return 0;
    }

    default List<SupplementalCarriedSourceDescriptor> describeDefault(Set<String> sourceIds) {
        return List.of();
    }

    List<SupplementalCarriedSourceDescriptor> describe(InventoryHostDescriptor host);

    List<SupplementalCarriedStackSnapshot> readSnapshots(
            LocalPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor
    );

    int slotCapacity(
            LocalPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor
    );

    default ItemStack extract(
            ServerPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor,
            dev.imagio.slot.client.model.ItemIdentity identity,
            StorageTransferMode mode
    ) {
        return ItemStack.EMPTY;
    }

    default ItemStack insert(
            ServerPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor,
            ItemStack stack
    ) {
        return stack;
    }

    default boolean activateEquipmentTarget(
            ServerPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor,
            String targetId
    ) {
        return false;
    }
}
