package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class CursorVoidOperations {
    private CursorVoidOperations() {
    }

    static int handleVoidMatchingCarried(
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

        boolean deleteAll = payload.mode() == CursorTransferPayload.Mode.STACK;
        if (layout != null) {
            int deletedFromMenu = deleteAll
                    ? deleteAllFromMenuSources(player, menu, layout, layout.actionSourceIdsForPane(InventoryPane.CARRIED), identity)
                    : deleteFromMenuSources(player, menu, layout, layout.actionSourceIdsForPane(InventoryPane.CARRIED), identity);
            if (deletedFromMenu > 0) {
                SlotDebugLog.log("Void matched carried item from menu-backed carried source: identity={}", identity.itemId());
                return deletedFromMenu;
            }

            if (layout.primaryStorageIsCarried() && !layout.primaryStorageMenuBacked()) {
                int deletedFromPrimary = deleteAll
                        ? deleteAllFromPrimaryCarriedStorage(player, menu, layout, identity)
                        : deleteFromPrimaryCarriedStorage(player, menu, layout, identity);
                if (deletedFromPrimary > 0) {
                    SlotDebugLog.log("Void matched carried item from primary carried storage: identity={}", identity.itemId());
                    return deletedFromPrimary;
                }
            }
        } else {
            int deletedFromInventoryMenu = deleteAll
                    ? deleteAllFromInventoryMenu(player, menu, identity)
                    : deleteFromInventoryMenu(player, menu, identity);
            if (deletedFromInventoryMenu > 0) {
                SlotDebugLog.log("Void matched carried item from player inventory menu: identity={}", identity.itemId());
                return deletedFromInventoryMenu;
            }
        }

        int deletedFromBackpacks = deleteAll
                ? deleteAllFromBackpacks(player, identity, syncedContents)
                : deleteFromBackpacks(player, identity, syncedContents);
        if (deletedFromBackpacks > 0) {
            SlotDebugLog.log("Void matched carried item from backpack storage: identity={}", identity.itemId());
            return deletedFromBackpacks;
        }
        return 0;
    }

    private static int deleteFromMenuSources(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Set<String> sourceIds,
            ItemIdentity identity
    ) {
        for (String sourceId : sourceIds) {
            int deletedCount = deleteFromMenuSlots(player, menu, layout.menuSlotsForSource(sourceId), identity);
            if (deletedCount > 0) {
                return deletedCount;
            }
        }
        return 0;
    }

    private static int deleteAllFromMenuSources(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            Set<String> sourceIds,
            ItemIdentity identity
    ) {
        int deletedCount = 0;
        for (String sourceId : sourceIds) {
            deletedCount += deleteAllFromMenuSlots(player, menu, layout.menuSlotsForSource(sourceId), identity);
        }
        return deletedCount;
    }

    private static int deleteFromInventoryMenu(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ItemIdentity identity
    ) {
        return deleteFromMenuSlots(player, menu, new ActionableSourcePolicy(new MenuSlotResolver(menu, null)).playerInsertTargetSlots(), identity);
    }

    private static int deleteAllFromInventoryMenu(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ItemIdentity identity
    ) {
        return deleteAllFromMenuSlots(player, menu, new ActionableSourcePolicy(new MenuSlotResolver(menu, null)).playerInsertTargetSlots(), identity);
    }

    private static int deleteFromMenuSlots(
            ServerPlayer player,
            AbstractContainerMenu menu,
            Iterable<Integer> menuSlots,
            ItemIdentity identity
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

            ItemStack extracted = sourceSlot.safeTake(1, sourceStack.getCount(), player);
            if (!extracted.isEmpty()) {
                return extracted.getCount();
            }
        }
        return 0;
    }

    private static int deleteAllFromMenuSlots(
            ServerPlayer player,
            AbstractContainerMenu menu,
            Iterable<Integer> menuSlots,
            ItemIdentity identity
    ) {
        int deletedCount = 0;
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
            if (!extracted.isEmpty()) {
                deletedCount += extracted.getCount();
            }
        }
        return deletedCount;
    }

    private static int deleteFromPrimaryCarriedStorage(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            ItemIdentity identity
    ) {
        ItemStack extracted = layout.primaryStorageSession().extractFromPrimary(menu, player, identity, StorageTransferMode.ONE);
        return extracted.getCount();
    }

    private static int deleteAllFromPrimaryCarriedStorage(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            ItemIdentity identity
    ) {
        int deletedCount = 0;
        while (true) {
            ItemStack extracted = layout.primaryStorageSession().extractFromPrimary(menu, player, identity, StorageTransferMode.STACK);
            if (extracted.isEmpty()) {
                return deletedCount;
            }
            deletedCount += extracted.getCount();
        }
    }

    private static int deleteFromBackpacks(
            ServerPlayer player,
            ItemIdentity identity,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int[] deletedCount = new int[]{0};
        boolean moved = SophisticatedBackpackTransferSupport.moveMatchingBackpackStack(
                player,
                identity,
                1,
                stack -> {
                    deletedCount[0] += stack.getCount();
                    return ItemStack.EMPTY;
                },
                syncedContents
        );
        return moved ? deletedCount[0] : 0;
    }

    private static int deleteAllFromBackpacks(
            ServerPlayer player,
            ItemIdentity identity,
            Map<UUID, CompoundTag> syncedContents
    ) {
        int[] deletedCount = new int[]{0};
        SophisticatedBackpackTransferSupport.moveMatchingBackpackStack(
                player,
                identity,
                Integer.MAX_VALUE,
                stack -> {
                    deletedCount[0] += stack.getCount();
                    return ItemStack.EMPTY;
                },
                syncedContents
        );
        return deletedCount[0];
    }
}
