package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class CursorPickupOperations {
    private CursorPickupOperations() {
    }

    static int handlePickupMatching(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            CursorTransferPayload payload,
            Map<UUID, CompoundTag> syncedContents
    ) {
        ItemIdentity identity = payload.identity();
        if (identity == null) {
            return 0;
        }

        ItemStack carried = menu.getCarried();
        if (!carried.isEmpty() && !ItemBehaviorPolicy.matchesMovableIdentity(carried, identity)) {
            return 0;
        }

        if (!carried.isEmpty() && payload.mode() == CursorTransferPayload.Mode.HALF) {
            return halveCarriedSelection(player, menu, layout, payload, syncedContents);
        }

        if (!carried.isEmpty() && CursorTransferSupport.remainingCursorSpace(menu) <= 0) {
            return 0;
        }

        InventoryPane pane = CursorTransferSupport.toInventoryPane(payload.targetPane());
        if (payload.mode() == CursorTransferPayload.Mode.STACK) {
            int movedCount = handlePickupMatchingStack(player, menu, layout, pane, identity, syncedContents);
            if (movedCount > 0) {
                SlotDebugLog.log(
                        "Cursor stack pickup from unified sources: pane={} identity={} moved={}",
                        pane,
                        identity.itemId(),
                        movedCount
                );
                return movedCount;
            }
        }

        int movedFromMenuSources = 0;
        if (layout != null) {
            Set<String> sourceIds = layout.actionSourceIdsForPane(pane);
            movedFromMenuSources = moveFromMenuSourcesToCursor(player, menu, layout, sourceIds, identity, payload.mode());
        } else if (pane == InventoryPane.CARRIED) {
            movedFromMenuSources = moveFromMenuSlotsToCursor(
                    player,
                    menu,
                    CursorTransferSupport.inventoryPickupSourceSlots(menu, null),
                    identity,
                    payload.mode()
            );
        }
        if (movedFromMenuSources > 0) {
            SlotDebugLog.log(
                    "Cursor pickup from menu source: pane={} identity={} mode={} moved={}",
                    pane,
                    identity.itemId(),
                    payload.mode(),
                    movedFromMenuSources
            );
            return movedFromMenuSources;
        }

        if (layout != null && CursorTransferSupport.shouldUsePrimaryStorage(layout, pane)) {
            StorageViewProviderSession session = layout.primaryStorageSession();
            ItemStack extracted = ExternalStorageExtractionSupport.extractMatchingIdentity(
                    menu,
                    player,
                    session,
                    identity,
                    CursorTransferSupport.toPickupExtractionMode(payload.mode())
            );
            if (!extracted.isEmpty()) {
                CursorPickupResolution pickupResolution = CursorTransferSupport.resolvePickupIntoCursor(menu, extracted, payload.mode());
                if (!pickupResolution.remainderToSource().isEmpty()) {
                    session.insertIntoPrimary(menu, player, pickupResolution.remainderToSource());
                }
                int movedCount = pickupResolution.movedCount();
                if (movedCount > 0) {
                    SlotDebugLog.log(
                            "Cursor pickup from primary storage: pane={} provider={} identity={} mode={} moved={}",
                            pane,
                            session.providerId(),
                            identity.itemId(),
                            payload.mode(),
                            movedCount
                    );
                    return movedCount;
                }
            }
        }

        if (pane == InventoryPane.CARRIED) {
            int movedCount = SophisticatedBackpackTransferSupport.moveFirstMatchingBackpackStack(
                    player,
                    identity,
                    count -> CursorTransferSupport.pickupAmountForMode(count, payload.mode()),
                    extracted -> {
                        CursorPickupResolution pickupResolution = CursorTransferSupport.resolvePickupIntoCursor(menu, extracted, payload.mode());
                        return pickupResolution.remainderToSource();
                    },
                    syncedContents
            );
            if (movedCount > 0) {
                SlotDebugLog.log(
                        "Cursor pickup from backpack storage: identity={} mode={} moved={}",
                        identity.itemId(),
                        payload.mode(),
                        movedCount
                );
            }
            return movedCount;
        }

        return 0;
    }

    private static int halveCarriedSelection(
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

        int retainedCount = CursorTransferSupport.pickupAmountForMode(carried.getCount(), CursorTransferPayload.Mode.HALF);
        int releasedCount = carried.getCount() - retainedCount;
        if (releasedCount <= 0) {
            return 0;
        }

        ItemStack released = carried.copy();
        released.setCount(releasedCount);

        InventoryPane pane = CursorTransferSupport.toInventoryPane(payload.targetPane());
        ItemStack remainder;
        if (layout == null) {
            if (pane != InventoryPane.CARRIED) {
                return 0;
            }
            remainder = CursorTransferSupport.insertIntoInventoryCarried(player, menu, null, released, syncedContents);
        } else {
            remainder = switch (pane) {
                case OPEN_CONTAINER -> CursorTransferSupport.insertIntoOpenPane(player, menu, layout, released);
                case CARRIED -> CursorTransferSupport.insertIntoCarriedPane(player, menu, layout, released, syncedContents);
            };
        }

        int movedCount = releasedCount - remainder.getCount();
        if (movedCount <= 0) {
            return 0;
        }

        ItemStack updatedCarried = carried.copy();
        updatedCarried.shrink(movedCount);
        menu.setCarried(updatedCarried);
        return movedCount;
    }

    private static int handlePickupMatchingStack(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            InventoryPane pane,
            ItemIdentity identity,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int movedCount = 0;
        if (layout != null) {
            movedCount += moveAllFromMenuSourcesToCursor(
                    player,
                    menu,
                    layout,
                    layout.actionSourceIdsForPane(pane),
                    identity
            );
        } else if (pane == InventoryPane.CARRIED) {
            movedCount += moveAllFromMenuSlotsToCursor(
                    player,
                    menu,
                    CursorTransferSupport.inventoryPickupSourceSlots(menu, null),
                    identity
            );
        }

        if (CursorTransferSupport.remainingCursorSpace(menu) <= 0) {
            return movedCount;
        }

        if (layout != null && CursorTransferSupport.shouldUsePrimaryStorage(layout, pane) && !layout.primaryStorageMenuBacked()) {
            movedCount += moveAllFromPrimaryStorageToCursor(player, menu, layout.primaryStorageSession(), identity);
            if (CursorTransferSupport.remainingCursorSpace(menu) <= 0) {
                return movedCount;
            }
        }

        if (pane == InventoryPane.CARRIED) {
            final int[] movedFromBackpacks = new int[]{0};
            SophisticatedBackpackTransferSupport.moveMatchingBackpackStack(
                    player,
                    identity,
                    Integer.MAX_VALUE,
                    extracted -> {
                        ItemStack remainder = CursorTransferSupport.mergeIntoCarried(menu, extracted);
                        movedFromBackpacks[0] += extracted.getCount() - remainder.getCount();
                        return remainder;
                    },
                    syncedContents
            );
            movedCount += movedFromBackpacks[0];
        }

        return movedCount;
    }

    private static int moveFromMenuSourcesToCursor(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Set<String> sourceIds,
            ItemIdentity identity,
            CursorTransferPayload.Mode mode
    ) {
        for (String sourceId : sourceIds) {
            int movedCount = moveFromMenuSlotsToCursor(player, menu, layout.menuSlotsForSource(sourceId), identity, mode);
            if (movedCount > 0) {
                return movedCount;
            }
        }
        return 0;
    }

    private static int moveAllFromMenuSourcesToCursor(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Set<String> sourceIds,
            ItemIdentity identity
    ) {
        int movedCount = 0;
        for (String sourceId : sourceIds) {
            movedCount += moveAllFromMenuSlotsToCursor(player, menu, layout.menuSlotsForSource(sourceId), identity);
            if (CursorTransferSupport.remainingCursorSpace(menu) <= 0) {
                break;
            }
        }
        return movedCount;
    }

    private static int moveFromMenuSlotsToCursor(
            ServerPlayer player,
            AbstractContainerMenu menu,
            Iterable<Integer> menuSlots,
            ItemIdentity identity,
            CursorTransferPayload.Mode mode
    ) {
        for (int menuSlot : menuSlots) {
            Slot sourceSlot = MenuSlotResolver.safeSlot(menu, menuSlot);
            if (sourceSlot == null) {
                continue;
            }
            ItemStack sourceStack = sourceSlot.getItem();
            if (sourceStack.isEmpty() || !sourceSlot.mayPickup(player) || !ItemBehaviorPolicy.matchesMovableIdentity(sourceStack, identity)) {
                continue;
            }

            ItemStack extracted = sourceSlot.safeTake(sourceStack.getCount(), sourceStack.getCount(), player);
            if (extracted.isEmpty()) {
                continue;
            }

            CursorPickupResolution pickupResolution = CursorTransferSupport.resolvePickupIntoCursor(menu, extracted, mode);
            if (!pickupResolution.remainderToSource().isEmpty()) {
                sourceSlot.safeInsert(pickupResolution.remainderToSource());
            }
            if (pickupResolution.movedCount() > 0) {
                return pickupResolution.movedCount();
            }
        }
        return 0;
    }

    private static int moveAllFromMenuSlotsToCursor(
            ServerPlayer player,
            AbstractContainerMenu menu,
            Iterable<Integer> menuSlots,
            ItemIdentity identity
    ) {
        int movedCount = 0;
        for (int menuSlot : menuSlots) {
            if (CursorTransferSupport.remainingCursorSpace(menu) <= 0) {
                continue;
            }

            Slot sourceSlot = MenuSlotResolver.safeSlot(menu, menuSlot);
            if (sourceSlot == null) {
                continue;
            }
            ItemStack sourceStack = sourceSlot.getItem();
            if (sourceStack.isEmpty() || !sourceSlot.mayPickup(player) || !ItemBehaviorPolicy.matchesMovableIdentity(sourceStack, identity)) {
                continue;
            }

            ItemStack extracted = sourceSlot.safeTake(sourceStack.getCount(), sourceStack.getCount(), player);
            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = CursorTransferSupport.mergeIntoCarried(menu, extracted);
            int insertedCount = extracted.getCount() - remainder.getCount();
            movedCount += insertedCount;
            if (!remainder.isEmpty()) {
                sourceSlot.safeInsert(remainder);
            }
            if (insertedCount <= 0 || CursorTransferSupport.remainingCursorSpace(menu) <= 0) {
                break;
            }
        }
        return movedCount;
    }

    private static int moveAllFromPrimaryStorageToCursor(
            ServerPlayer player,
            AbstractContainerMenu menu,
            StorageViewProviderSession session,
            ItemIdentity identity
    ) {
        int movedCount = 0;
        while (CursorTransferSupport.remainingCursorSpace(menu) > 0) {
            ItemStack extracted = ExternalStorageExtractionSupport.extractMatchingIdentity(
                    menu,
                    player,
                    session,
                    identity,
                    StorageTransferMode.STACK
            );
            if (extracted.isEmpty()) {
                break;
            }

            ItemStack remainder = CursorTransferSupport.mergeIntoCarried(menu, extracted);
            int insertedCount = extracted.getCount() - remainder.getCount();
            movedCount += insertedCount;
            if (!remainder.isEmpty()) {
                session.insertIntoPrimary(menu, player, remainder);
            }
            if (insertedCount <= 0) {
                break;
            }
        }
        return movedCount;
    }
}
