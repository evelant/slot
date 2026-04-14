package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.network.CursorTransferPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;

final class CursorDropOperations {
    private CursorDropOperations() {
    }

    static int handleDropCarried(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            CursorTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return 0;
        }

        ItemStack moving = carried.copy();
        if (payload.mode() == CursorTransferPayload.Mode.ONE) {
            moving.setCount(1);
        }

        InventoryPane pane = CursorTransferSupport.toInventoryPane(payload.targetPane());
        int originalMovingCount = moving.getCount();
        ItemStack remainder;
        if (layout == null) {
            if (pane != InventoryPane.CARRIED) {
                return 0;
            }
            remainder = CursorTransferSupport.insertIntoInventoryCarried(player, menu, null, moving, syncedContents);
        } else {
            remainder = switch (pane) {
                case OPEN_CONTAINER -> CursorTransferSupport.insertIntoOpenPane(player, menu, layout, moving);
                case CARRIED -> CursorTransferSupport.insertIntoCarriedPane(player, menu, layout, moving, syncedContents);
            };
        }
        int movedCount = originalMovingCount - remainder.getCount();
        if (movedCount <= 0) {
            return 0;
        }

        if (payload.mode() == CursorTransferPayload.Mode.ONE) {
            ItemStack updatedCarried = carried.copy();
            updatedCarried.shrink(movedCount);
            menu.setCarried(updatedCarried);
        } else {
            menu.setCarried(remainder);
        }

        SlotDebugLog.log(
                "Cursor drop into pane: pane={} moved={} mode={}",
                pane,
                movedCount,
                payload.mode()
        );
        return movedCount;
    }

    static int handleDropCarriedToSlot(
            AbstractContainerMenu menu,
            CursorTransferPayload payload
    ) {
        Slot targetSlot = CraftingGridPlacementOperations.resolveMenuSlot(menu, payload.targetMenuSlot());
        if (targetSlot == null) {
            return 0;
        }
        return CraftingGridPlacementOperations.placeFromCursorCarried(
                menu,
                targetSlot,
                payload.targetMenuSlot(),
                payload.mode() == CursorTransferPayload.Mode.ONE
        );
    }

    static int handleTrashCarried(
            AbstractContainerMenu menu,
            CursorTransferPayload payload
    ) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return 0;
        }

        int removedCount = payload.mode() == CursorTransferPayload.Mode.ONE ? 1 : carried.getCount();
        if (removedCount <= 0) {
            return 0;
        }

        if (payload.mode() == CursorTransferPayload.Mode.ONE) {
            ItemStack updatedCarried = carried.copy();
            updatedCarried.shrink(1);
            menu.setCarried(updatedCarried);
        } else {
            menu.setCarried(ItemStack.EMPTY);
        }

        SlotDebugLog.log(
                "Cursor trash applied: removed={} mode={}",
                removedCount,
                payload.mode()
        );
        return removedCount;
    }
}
