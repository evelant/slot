package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Reverses a single deposit record — extracts {@code count} items of
 * {@code identity} from a specific chest and inserts them back into the
 * player's carry. Used by the deposit-undo closure pushed onto
 * {@code UndoStack} after a successful deposit-button click.
 *
 * <p>Best-effort: if the chest no longer holds enough of the identity
 * (someone else took some), only what's available is pulled. If the
 * player's carry can't accept everything, the leftover is reinserted
 * into the chest so items aren't lost. Errors don't propagate — undo
 * should never throw out of the {@code UndoStack}.
 */
public final class DepositReverser {
    private DepositReverser() {
    }

    /** Pull from chest into carry. Used as the undo direction. */
    public static int pullFromChestToCarry(
            ServerPlayer player,
            UUID storageId,
            ItemIdentity identity,
            int count
    ) {
        if (player == null || storageId == null || identity == null || count <= 0) {
            return 0;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        ClaimedChest chest = lookupChest(player, storageId);
        if (chest == null) {
            return 0;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        if (!world.isAccessible(server, target)) {
            return 0;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();

        // Walk chest slots, matching identity, extracting up to remaining
        // demand. Collect the actual ItemStacks first so the chest read
        // cursor isn't invalidated by mid-loop mutation.
        int remaining = count;
        ArrayList<ItemStack> pulled = new ArrayList<>();
        for (WorldStorageAccess.SlotContent entry : world.enumerate(server, target)) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = entry.stack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (!identity.equals(ItemIdentityMatcher.create(stack))) {
                continue;
            }
            int pullCount = Math.min(stack.getCount(), remaining);
            ItemStack taken = world.extract(server, target, entry.slotIndex(), pullCount, false);
            if (taken == null || taken.isEmpty()) {
                continue;
            }
            pulled.add(taken);
            remaining -= taken.getCount();
        }
        int restored = 0;
        for (ItemStack stack : pulled) {
            ItemStack leftover = carried.insertBestFit(player, stack, false);
            int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
            restored += stack.getCount() - leftoverCount;
            if (leftoverCount > 0) {
                // Carry rejected part of the stack — put it back where it
                // came from so the items aren't lost. The chest had room
                // for them moments ago, so this should always succeed; if
                // it doesn't, log and move on (there is no good recovery
                // path inside an undo closure).
                ItemStack reinsertLeftover = world.insert(server, target, leftover, false);
                if (reinsertLeftover != null && !reinsertLeftover.isEmpty()) {
                    SlotCommon.LOGGER.warn(
                            "[SLOT] deposit-undo: lost {} of {} (chest={} carry rejected, chest reinsert rejected)",
                            reinsertLeftover.getCount(), identity.itemId(), storageId);
                }
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] deposit-undo pulled identity={} count={} restored={} chest={}",
                identity.itemId(), count, restored, storageId);
        return restored;
    }

    /** Push from carry into chest. Used as the redo direction. */
    public static int pushFromCarryToChest(
            ServerPlayer player,
            UUID storageId,
            ItemIdentity identity,
            int count
    ) {
        if (player == null || storageId == null || identity == null || count <= 0) {
            return 0;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        ClaimedChest chest = lookupChest(player, storageId);
        if (chest == null) {
            return 0;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        if (!world.isAccessible(server, target)) {
            return 0;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();

        int remaining = count;
        int delivered = 0;
        // Walk carry locations matching the identity until budget exhausted
        // or carry is exhausted. Each iteration extracts a slot's worth and
        // inserts into the chest; chest leftover (full chest) goes back
        // into carry to avoid item loss.
        for (CarriedSourceAccess.CarriedLocation loc : carried.findAllMatching(player, identity)) {
            if (remaining <= 0) {
                break;
            }
            ItemStack peeked = carried.peek(player, loc.sourceId(), loc.slotIndex());
            if (peeked == null || peeked.isEmpty()) {
                continue;
            }
            int pullCount = Math.min(peeked.getCount(), remaining);
            ItemStack taken = carried.extract(player, loc.sourceId(), loc.slotIndex(), pullCount, false);
            if (taken == null || taken.isEmpty()) {
                continue;
            }
            ItemStack chestLeftover = world.insert(server, target, taken, false);
            int leftoverCount = chestLeftover == null || chestLeftover.isEmpty() ? 0 : chestLeftover.getCount();
            delivered += taken.getCount() - leftoverCount;
            remaining -= taken.getCount() - leftoverCount;
            if (leftoverCount > 0) {
                // Chest rejected part of the stack — put it back into the
                // player's carry so items aren't lost.
                ItemStack carryLeftover = carried.insertBestFit(player, chestLeftover, false);
                if (carryLeftover != null && !carryLeftover.isEmpty()) {
                    SlotCommon.LOGGER.warn(
                            "[SLOT] deposit-redo: lost {} of {} (chest={} both rejected the leftover)",
                            carryLeftover.getCount(), identity.itemId(), storageId);
                }
                // Chest is full for this stack; further slots holding the
                // same identity won't fit either.
                break;
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] deposit-redo pushed identity={} count={} delivered={} chest={}",
                identity.itemId(), count, delivered, storageId);
        return delivered;
    }

    private static ClaimedChest lookupChest(ServerPlayer player, UUID storageId) {
        var runtime = dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService.runtime(player);
        if (runtime == null) {
            return null;
        }
        ClaimedChestMap map = runtime.chestClaimWorkflow().claimedChestMap();
        return map == null ? null : map.chest(storageId);
    }
}
