package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.network.CursorTransferPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;
import java.util.UUID;

final class CursorTransferOperations {
    private CursorTransferOperations() {
    }

    static int handlePickupMatching(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            CursorTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        return CursorPickupOperations.handlePickupMatching(player, menu, layout, payload, syncedContents);
    }

    static int handleDropCarried(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            CursorTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        return CursorDropOperations.handleDropCarried(player, menu, layout, payload, syncedContents);
    }

    static int handleDropCarriedToSlot(
            AbstractContainerMenu menu,
            CursorTransferPayload payload
    ) {
        return CursorDropOperations.handleDropCarriedToSlot(menu, payload);
    }

    static int handleTrashCarried(
            AbstractContainerMenu menu,
            CursorTransferPayload payload
    ) {
        return CursorDropOperations.handleTrashCarried(menu, payload);
    }

    static int handleVoidMatchingCarried(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            CursorTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        return CursorVoidOperations.handleVoidMatchingCarried(player, menu, layout, payload, syncedContents);
    }
}
