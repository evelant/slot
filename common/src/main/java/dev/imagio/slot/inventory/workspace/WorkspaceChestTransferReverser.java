package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Best-effort undo/redo primitive for chest <-> carry transfers.
 */
public final class WorkspaceChestTransferReverser {
    private WorkspaceChestTransferReverser() {
    }

    public static int pullFromChestToCarry(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            UUID storageId,
            ItemIdentity identity,
            int count
    ) {
        if (storageId == null) {
            return 0;
        }
        return pullFromStorageToCarry(player, runtime, storageId.toString(), identity, count);
    }

    public static int pullFromStorageToCarry(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            String storageId,
            ItemIdentity identity,
            int count
    ) {
        if (player == null || runtime == null || storageId == null || storageId.isBlank()
                || identity == null || count <= 0) {
            return 0;
        }
        MinecraftServer server = player.getServer();
        WorldStorageAccess.Target target = lookupTarget(runtime, storageId);
        if (server == null || target == null) {
            return 0;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        if (!world.isAccessible(server, target)) {
            return 0;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();

        int remaining = count;
        ArrayList<ItemStack> pulled = new ArrayList<>();
        for (WorldStorageAccess.SlotContent entry : world.enumerate(server, target)) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = entry.stack();
            if (stack == null || stack.isEmpty() || !ItemIdentityMatcher.matchesMovable(stack, identity)) {
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
            if (leftoverCount <= 0) {
                continue;
            }
            ItemStack reinsertLeftover = world.insert(server, target, leftover, false);
            if (reinsertLeftover != null && !reinsertLeftover.isEmpty()) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] chest-transfer-undo: lost {} of {} (storage={} carry rejected, storage reinsert rejected)",
                        reinsertLeftover.getCount(), identity.itemId(), storageId);
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] chest-transfer-undo pulled identity={} requested={} restored={} storage={}",
                identity.itemId(), count, restored, storageId);
        return restored;
    }

    public static int pushFromCarryToChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            UUID storageId,
            ItemIdentity identity,
            int count
    ) {
        if (storageId == null) {
            return 0;
        }
        return pushFromCarryToStorage(player, runtime, storageId.toString(), identity, count);
    }

    public static int pushFromCarryToStorage(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            String storageId,
            ItemIdentity identity,
            int count
    ) {
        if (player == null || runtime == null || storageId == null || storageId.isBlank()
                || identity == null || count <= 0) {
            return 0;
        }
        MinecraftServer server = player.getServer();
        WorldStorageAccess.Target target = lookupTarget(runtime, storageId);
        if (server == null || target == null) {
            return 0;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        if (!world.isAccessible(server, target)) {
            return 0;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();

        int remaining = count;
        int delivered = 0;
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
            int inserted = taken.getCount() - leftoverCount;
            delivered += inserted;
            remaining -= inserted;
            if (leftoverCount <= 0) {
                continue;
            }
            ItemStack carryLeftover = carried.insertBestFit(player, chestLeftover, false);
            if (carryLeftover != null && !carryLeftover.isEmpty()) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] chest-transfer-redo: lost {} of {} (storage={} both rejected leftover)",
                        carryLeftover.getCount(), identity.itemId(), storageId);
            }
            break;
        }
        SlotCommon.LOGGER.info(
                "[SLOT] chest-transfer-redo pushed identity={} requested={} delivered={} storage={}",
                identity.itemId(), count, delivered, storageId);
        return delivered;
    }

    private static WorldStorageAccess.Target lookupTarget(WorkflowDomainRuntime runtime, String storageId) {
        if (runtime == null || storageId == null || storageId.isBlank()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(storageId);
            ClaimedChest chest = lookupChest(runtime, uuid);
            return chest == null ? null : new WorldStorageAccess.Target.Chest(chest);
        } catch (IllegalArgumentException ignored) {
            return WorldDisplayStorageSource.targetFromStorageId(storageId)
                    .map(target -> (WorldStorageAccess.Target) target)
                    .orElse(null);
        }
    }

    private static ClaimedChest lookupChest(WorkflowDomainRuntime runtime, UUID storageId) {
        ClaimedChestMap map = runtime.chestClaimWorkflow().claimedChestMap();
        return map == null ? null : map.chest(storageId);
    }
}
