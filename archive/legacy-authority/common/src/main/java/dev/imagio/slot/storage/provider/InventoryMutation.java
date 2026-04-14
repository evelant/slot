package dev.imagio.slot.storage.provider;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record InventoryMutation(
        Kind kind,
        String sourceId,
        ItemIdentity identity,
        ItemStack stack,
        StorageTransferMode transferMode,
        String targetId,
        InventoryHostDescriptor host,
        ServerPlayer player
) {
    public InventoryMutation {
        kind = kind == null ? Kind.UNSPECIFIED : kind;
        sourceId = sourceId == null ? "" : sourceId;
        stack = stack == null ? ItemStack.EMPTY : stack;
        transferMode = transferMode == null ? StorageTransferMode.ONE : transferMode;
        targetId = targetId == null ? "" : targetId;
    }

    public static InventoryMutation extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            ItemIdentity identity,
            StorageTransferMode mode
    ) {
        return new InventoryMutation(Kind.EXTRACT, sourceId, identity, ItemStack.EMPTY, mode, "", host, player);
    }

    public static InventoryMutation insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            ItemStack stack
    ) {
        return new InventoryMutation(Kind.INSERT, sourceId, null, stack, StorageTransferMode.ALL, "", host, player);
    }

    public enum Kind {
        EXTRACT,
        INSERT,
        ACTIVATE_TARGET,
        UNSPECIFIED
    }
}
