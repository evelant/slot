package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Snapshot + restore primitive for a single hotbar slot, used by undo
 * closures of the assign-to-hotbar and return-from-hotbar flows.
 *
 * <p>{@link #peekSlot} copies the current stack out so the closure can
 * compare later. {@link #restoreSlot} replaces the slot's contents with
 * a target stack (or empty), displacing the current occupant into
 * carry via {@code insertBestFit}; whatever can't be carried is dropped
 * into the world. The replacement count is best-effort: it pulls
 * matching identity from any other carry source up to the target's
 * count, but if the player has since used items, the slot may end up
 * with less than the snapshot. Errors are logged, not thrown — undo
 * closures shouldn't propagate failures to the {@code UndoStack}.
 */
public final class HotbarSlotReverser {
    private HotbarSlotReverser() {
    }

    /** Read-only snapshot (a copy) of hotbar slot {@code idx}. */
    public static ItemStack peekSlot(ServerPlayer player, int idx) {
        if (player == null || idx < 0 || idx >= 9) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = player.getInventory().getItem(idx);
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    /**
     * Drive hotbar slot {@code idx} toward {@code target} (empty = clear
     * the slot). Whatever currently occupies the slot is pushed back
     * into the player's carry; if the slot is being filled, matching
     * identity is pulled from elsewhere in carry to seed the new stack.
     */
    public static void restoreSlot(ServerPlayer player, int idx, ItemStack target) {
        if (player == null || idx < 0 || idx >= 9) {
            return;
        }
        Inventory inv = player.getInventory();
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();

        // Step 1: clear the slot. Push existing contents (if any) back
        // into carry; world-drop anything that doesn't fit.
        ItemStack current = inv.getItem(idx);
        if (current != null && !current.isEmpty()) {
            ItemStack displaced = current.copy();
            inv.setItem(idx, ItemStack.EMPTY);
            ItemStack remainder = carried.insertBestFit(player, displaced, false);
            if (remainder != null && !remainder.isEmpty()) {
                player.drop(remainder, false);
            }
        }
        if (target == null || target.isEmpty()) {
            return;
        }

        // Step 2: pull `target.count` of `target.identity` from anywhere
        // in carry into the slot. Skip the hotbar slot we're filling so
        // we don't extract from our own destination.
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
                    && loc.slotIndex() == idx) {
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
            ItemStack hotbar = inv.getItem(idx);
            if (hotbar == null || hotbar.isEmpty()) {
                inv.setItem(idx, extracted);
                remaining -= extracted.getCount();
                continue;
            }
            if (ItemStack.isSameItemSameComponents(hotbar, extracted)) {
                int max = hotbar.getMaxStackSize();
                int merged = Math.min(max - hotbar.getCount(), extracted.getCount());
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
                            overflow.getCount(), targetIdentity.itemId(), idx);
                    player.drop(overflow, false);
                }
            }
        }
    }
}
