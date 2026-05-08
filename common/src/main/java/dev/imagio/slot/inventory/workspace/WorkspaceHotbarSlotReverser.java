package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackEquivalence;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Best-effort undo/redo primitive for a single hotbar slot.
 */
public final class WorkspaceHotbarSlotReverser {
    private WorkspaceHotbarSlotReverser() {
    }

    public static ItemStack peekSlot(ServerPlayer player, int hotbarIndex) {
        if (player == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = player.getInventory().getItem(hotbarIndex);
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    public static void restoreSlot(ServerPlayer player, int hotbarIndex, ItemStack target) {
        if (player == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        Inventory inventory = player.getInventory();
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();

        ItemStack current = inventory.getItem(hotbarIndex);
        if (current != null && !current.isEmpty()) {
            ItemStack displaced = current.copy();
            inventory.setItem(hotbarIndex, ItemStack.EMPTY);
            ItemStack remainder = carried.insertBestFit(player, displaced, false);
            if (remainder != null && !remainder.isEmpty()) {
                player.drop(remainder, false);
            }
        }
        if (target == null || target.isEmpty()) {
            return;
        }

        ItemIdentity targetIdentity = ItemIdentityMatcher.create(target);
        if (targetIdentity == null) {
            return;
        }
        int remaining = target.getCount();
        for (CarriedSourceAccess.CarriedLocation loc : carried.findAllMatching(player, targetIdentity)) {
            if (remaining <= 0) {
                break;
            }
            if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(loc.sourceId())
                    && loc.slotIndex() == hotbarIndex) {
                continue;
            }
            ItemStack peeked = carried.peek(player, loc.sourceId(), loc.slotIndex());
            if (peeked == null || peeked.isEmpty()) {
                continue;
            }
            int pull = Math.min(peeked.getCount(), remaining);
            ItemStack extracted = carried.extract(player, loc.sourceId(), loc.slotIndex(), pull, false);
            if (extracted == null || extracted.isEmpty()) {
                continue;
            }
            ItemStack hotbar = inventory.getItem(hotbarIndex);
            if (hotbar == null || hotbar.isEmpty()) {
                inventory.setItem(hotbarIndex, extracted);
                remaining -= extracted.getCount();
                continue;
            }
            if (ItemStackEquivalence.sameItemAndData(hotbar, extracted)) {
                int merged = Math.min(hotbar.getMaxStackSize() - hotbar.getCount(), extracted.getCount());
                if (merged > 0) {
                    hotbar.grow(merged);
                    extracted.shrink(merged);
                    remaining -= merged;
                }
            }
            if (!extracted.isEmpty()) {
                ItemStack overflow = carried.insertBestFit(player, extracted, false);
                if (overflow != null && !overflow.isEmpty()) {
                    SlotCommon.LOGGER.warn(
                            "[SLOT] hotbar-undo: dropped {} overflow during restore (identity={} idx={})",
                            overflow.getCount(), targetIdentity.itemId(), hotbarIndex);
                    player.drop(overflow, false);
                }
            }
        }
    }
}
