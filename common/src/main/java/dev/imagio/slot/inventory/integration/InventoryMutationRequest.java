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
        int requestedCount,
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
        requestedCount = Math.max(0, requestedCount);
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
        return extract(host, player, sourceId, -1, "", 0, identity, mode);
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, slotIndex, "", 0, identity, mode);
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            int requestedCount,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, slotIndex, "", requestedCount, identity, mode);
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            String entryId,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, -1, entryId, 0, identity, mode);
    }

    public static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            String entryId,
            int requestedCount,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return extract(host, player, sourceId, -1, entryId, requestedCount, identity, mode);
    }

    private static InventoryMutationRequest extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            String entryId,
            int requestedCount,
            ItemIdentity identity,
            InventoryTransferMode mode
    ) {
        return new InventoryMutationRequest(
                InventoryMutationKind.EXTRACT,
                sourceId,
                slotIndex,
                entryId,
                requestedCount,
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
        return insert(host, player, sourceId, -1, "", 0, stack);
    }

    public static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            ItemStack stack
    ) {
        return insert(host, player, sourceId, slotIndex, "", 0, stack);
    }

    public static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            int requestedCount,
            ItemStack stack
    ) {
        return insert(host, player, sourceId, slotIndex, "", requestedCount, stack);
    }

    public static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            String entryId,
            ItemStack stack
    ) {
        return insert(host, player, sourceId, -1, entryId, 0, stack);
    }

    private static InventoryMutationRequest insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            String entryId,
            int requestedCount,
            ItemStack stack
    ) {
        return new InventoryMutationRequest(
                InventoryMutationKind.INSERT,
                sourceId,
                slotIndex,
                entryId,
                requestedCount,
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

    /**
     * Translate this request's {@code requestedCount} + {@code transferMode}
     * into a concrete extract amount, clamped to what's actually {@code available}
     * in the source slot. Used by extensions implementing
     * {@link PlayerInventoryExtension#mutate} to size EXTRACT operations
     * consistently across providers.
     *
     * <ul>
     *   <li>Explicit {@code requestedCount > 0} wins: clamped to
     *       {@code min(requestedCount, max(1, available))}.</li>
     *   <li>Otherwise falls back to {@link #transferMode()}: {@code ONE}→1,
     *       {@code STACK}/{@code ALL}→{@code max(1, available)}.</li>
     * </ul>
     */
    public int requestedAmount(int available) {
        if (requestedCount > 0) {
            return Math.min(requestedCount, Math.max(1, available));
        }
        return switch (transferMode) {
            case ONE -> 1;
            case STACK, ALL -> Math.max(1, available);
        };
    }
}
