package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record InventoryMutationRequest(
        InventoryMutationKind kind,
        String sourceId,
        int slotIndex,
        String entryId,
        ItemIdentity identity,
        ItemStack stack,
        InventoryTransferMode transferMode,
        String targetId,
        InventoryHostDescriptor host,
        ServerPlayer player
) {
    public InventoryMutationRequest {
        kind = kind == null ? InventoryMutationKind.UNSPECIFIED : kind;
        sourceId = sourceId == null ? "" : sourceId;
        slotIndex = Math.max(-1, slotIndex);
        entryId = entryId == null ? "" : entryId;
        stack = stack == null ? ItemStack.EMPTY : stack;
        transferMode = transferMode == null ? InventoryTransferMode.ONE : transferMode;
        targetId = targetId == null ? "" : targetId;
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, -1, "", identity, mode);
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, slotIndex, "", identity, mode);
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            String entryId,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, -1, entryId, identity, mode);
    }

    private static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            String entryId,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return new InventoryMutationRequest(
                InventoryMutationKind.EXTRACT,
                sourceId,
                slotIndex,
                entryId,
                identity,
                ItemStack.EMPTY,
                mode,
                "",
                host,
                player
        );
    }

    public static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            ItemStack stack
    ) {
        return insert(host, player, sourceId, -1, "", stack);
    }

    public static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            ItemStack stack
    ) {
        return insert(host, player, sourceId, slotIndex, "", stack);
    }

    public static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            String entryId,
            ItemStack stack
    ) {
        return insert(host, player, sourceId, -1, entryId, stack);
    }

    private static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            String entryId,
            ItemStack stack
    ) {
        return new InventoryMutationRequest(
                InventoryMutationKind.INSERT,
                sourceId,
                slotIndex,
                entryId,
                null,
                stack,
                InventoryTransferMode.ALL,
                "",
                host,
                player
        );
    }

    public boolean targetsExactSlot() {
        return slotIndex >= 0;
    }

    public boolean targetsProviderEntry() {
        return !entryId.isBlank();
    }
}
