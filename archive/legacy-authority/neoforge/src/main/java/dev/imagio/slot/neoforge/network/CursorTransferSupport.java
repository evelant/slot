package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.inventory.CarriedPlacementPolicy;
import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

final class CursorTransferSupport {
    private CursorTransferSupport() {
    }

    static ItemStack insertIntoOpenPane(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            ItemStack moving
    ) {
        StorageViewProviderSession session = layout.primaryStorageSession();
        if (!session.primaryStorageMenuBacked()) {
            return session.insertIntoPrimary(menu, player, moving);
        }

        String primarySourceId = layout.primaryStorageIsCarried()
                ? ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE
                : ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER;
        return BackpackTransferOperations.insertIntoMenuSlots(moving, menu, layout.menuSlotsForSource(primarySourceId));
    }

    static ItemStack insertIntoCarriedPane(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            ItemStack moving,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemStack remainder;
        if (layout.primaryStorageIsCarried()) {
            remainder = CarriedPlacementPolicy.insertIntoCarriedMenuSlots(
                    moving,
                    menu,
                    layout,
                    CarriedPlacementPolicy.Intent.GENERAL
            );
            remainder = SophisticatedBackpackTransferSupport.insertIntoPlayerBackpacks(player, remainder, syncedContents);
        } else {
            remainder = SophisticatedBackpackTransferSupport.insertIntoPlayerBackpacks(player, moving, syncedContents);
            remainder = CarriedPlacementPolicy.insertIntoCarriedMenuSlots(
                    remainder,
                    menu,
                    layout,
                    CarriedPlacementPolicy.Intent.GENERAL
            );
        }
        if (layout.primaryStorageIsCarried() && !layout.primaryStorageMenuBacked()) {
            remainder = layout.primaryStorageSession().insertIntoPrimary(menu, player, remainder);
        }
        return remainder;
    }

    static ItemStack insertIntoInventoryCarried(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            ItemStack moving,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemStack remainder = SophisticatedBackpackTransferSupport.insertIntoPlayerBackpacks(player, moving, syncedContents);
        return BackpackTransferOperations.insertIntoMenuSlots(
                remainder,
                menu,
                policy(menu, layout).playerInsertTargetSlots()
        );
    }

    static ItemStack mergeIntoCarried(AbstractContainerMenu menu, ItemStack moving) {
        if (moving.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            int movedCount = Math.min(cursorStackLimit(moving), moving.getCount());
            if (movedCount <= 0) {
                return moving;
            }

            ItemStack updatedCarried = moving.copy();
            updatedCarried.setCount(movedCount);
            menu.setCarried(updatedCarried);
            if (movedCount == moving.getCount()) {
                return ItemStack.EMPTY;
            }

            ItemStack remainder = moving.copy();
            remainder.shrink(movedCount);
            return remainder;
        }

        if (!ItemStack.isSameItemSameComponents(carried, moving)) {
            return moving;
        }

        int space = Math.max(0, cursorStackLimit(carried) - carried.getCount());
        if (space <= 0) {
            return moving;
        }

        int movedCount = Math.min(space, moving.getCount());
        ItemStack updatedCarried = carried.copy();
        updatedCarried.grow(movedCount);
        menu.setCarried(updatedCarried);
        if (movedCount == moving.getCount()) {
            return ItemStack.EMPTY;
        }

        ItemStack remainder = moving.copy();
        remainder.shrink(movedCount);
        return remainder;
    }

    static CursorPickupResolution resolvePickupIntoCursor(
            AbstractContainerMenu menu,
            ItemStack extracted,
            CursorTransferPayload.Mode mode
    ) {
        if (extracted.isEmpty()) {
            return new CursorPickupResolution(0, ItemStack.EMPTY);
        }

        int pickupCount = pickupAmountForMode(extracted.getCount(), mode);
        ItemStack pickupStack = extracted.copy();
        pickupStack.setCount(pickupCount);

        ItemStack sourceRemainder = ItemStack.EMPTY;
        if (pickupCount < extracted.getCount()) {
            sourceRemainder = extracted.copy();
            sourceRemainder.shrink(pickupCount);
        }

        ItemStack cursorRemainder = mergeIntoCarried(menu, pickupStack);
        int movedCount = pickupCount - cursorRemainder.getCount();
        return new CursorPickupResolution(movedCount, combineRemainders(sourceRemainder, cursorRemainder));
    }

    static int pickupAmountForMode(int sourceCount, CursorTransferPayload.Mode mode) {
        if (sourceCount <= 0) {
            return 0;
        }
        return switch (mode) {
            case ONE -> 1;
            case HALF -> Math.max(1, (sourceCount + 1) / 2);
            case STACK -> sourceCount;
        };
    }

    static int remainingCursorSpace(AbstractContainerMenu menu) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, cursorStackLimit(carried) - carried.getCount());
    }

    static List<Integer> inventoryPickupSourceSlots(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        return policy(menu, layout).playerPickupSourceSlots();
    }

    static boolean shouldUsePrimaryStorage(ChestLikeMenuLayout layout, InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER || layout.primaryStorageIsCarried();
    }

    static InventoryPane toInventoryPane(CursorTransferPayload.TargetPane targetPane) {
        return targetPane == CursorTransferPayload.TargetPane.OPEN_CONTAINER
                ? InventoryPane.OPEN_CONTAINER
                : InventoryPane.CARRIED;
    }

    static StorageTransferMode toPickupExtractionMode(CursorTransferPayload.Mode mode) {
        return mode == CursorTransferPayload.Mode.ONE ? StorageTransferMode.ONE : StorageTransferMode.STACK;
    }

    private static ActionableSourcePolicy policy(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        return new ActionableSourcePolicy(new MenuSlotResolver(menu, layout));
    }

    private static ItemStack combineRemainders(ItemStack first, ItemStack second) {
        if (first.isEmpty()) {
            return second;
        }
        if (second.isEmpty()) {
            return first;
        }

        ItemStack combined = first.copy();
        combined.grow(second.getCount());
        return combined;
    }

    private static int cursorStackLimit(ItemStack stack) {
        return Math.max(0, stack.getMaxStackSize());
    }
}

record CursorPickupResolution(int movedCount, ItemStack remainderToSource) {
}
