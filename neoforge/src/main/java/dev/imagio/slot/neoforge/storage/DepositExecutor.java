package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.workspace.DepositPlan;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class DepositExecutor {
    private static final int OFFHAND_SENTINEL = -2;

    private DepositExecutor() {
    }

    public static DepositOutcome execute(
            ServerPlayer player,
            DepositPlan plan,
            ClaimedChestMap claimedChestMap
    ) {
        if (player == null || plan == null || plan.isEmpty() || claimedChestMap == null) {
            return DepositOutcome.empty();
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return DepositOutcome.empty();
        }

        int deposited = 0;
        int failed = 0;
        LinkedHashSet<UUID> destinations = new LinkedHashSet<>();

        for (DepositPlan.Assignment assignment : plan.assignments()) {
            int rawSlot = translateToRawSlot(assignment.laneId(), assignment.slotIndex());
            if (rawSlot == Integer.MIN_VALUE) {
                failed++;
                continue;
            }
            ItemStack sourceStack = readStack(player, rawSlot);
            if (sourceStack == null || sourceStack.isEmpty()) {
                failed++;
                continue;
            }

            UUID chosen = null;
            for (String candidateId : assignment.candidateStorageIds()) {
                UUID candidateUuid;
                try {
                    candidateUuid = UUID.fromString(candidateId);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                ClaimedChest chest = claimedChestMap.chest(candidateUuid);
                if (chest == null) {
                    continue;
                }
                IItemHandler handler = resolveHandler(server, chest);
                if (handler == null) {
                    continue;
                }
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, sourceStack.copy(), true);
                if (!remaining.isEmpty()) {
                    continue;
                }
                ItemStack postInsert = ItemHandlerHelper.insertItemStacked(handler, sourceStack.copy(), false);
                if (!postInsert.isEmpty()) {
                    continue;
                }
                chosen = candidateUuid;
                break;
            }

            if (chosen == null) {
                failed++;
                continue;
            }

            writeStack(player, rawSlot, ItemStack.EMPTY);
            deposited++;
            destinations.add(chosen);
        }

        player.getInventory().setChanged();
        if (deposited > 0 || failed > 0) {
            SlotCommon.LOGGER.info(
                    "[SLOT] deposit deposited={} failed={} destinations={}",
                    deposited, failed, destinations
            );
        }
        return new DepositOutcome(deposited, failed, destinations);
    }

    private static int translateToRawSlot(String laneId, int slotIndex) {
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(laneId)) {
            if (slotIndex < 0 || slotIndex >= 27) {
                return Integer.MIN_VALUE;
            }
            return slotIndex + 9;
        }
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(laneId)) {
            if (slotIndex < 0 || slotIndex >= 9) {
                return Integer.MIN_VALUE;
            }
            return slotIndex;
        }
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(laneId)) {
            if (slotIndex != 0) {
                return Integer.MIN_VALUE;
            }
            return OFFHAND_SENTINEL;
        }
        return Integer.MIN_VALUE;
    }

    private static ItemStack readStack(ServerPlayer player, int rawSlot) {
        Inventory inv = player.getInventory();
        if (rawSlot == OFFHAND_SENTINEL) {
            return inv.offhand.isEmpty() ? ItemStack.EMPTY : inv.offhand.get(0);
        }
        if (rawSlot < 0 || rawSlot >= inv.items.size()) {
            return ItemStack.EMPTY;
        }
        return inv.getItem(rawSlot);
    }

    private static void writeStack(ServerPlayer player, int rawSlot, ItemStack stack) {
        Inventory inv = player.getInventory();
        if (rawSlot == OFFHAND_SENTINEL) {
            if (!inv.offhand.isEmpty()) {
                inv.offhand.set(0, stack);
            }
            return;
        }
        if (rawSlot >= 0 && rawSlot < inv.items.size()) {
            inv.setItem(rawSlot, stack);
        }
    }

    private static IItemHandler resolveHandler(MinecraftServer server, ClaimedChest chest) {
        for (ChestAnchor anchor : chest.anchors()) {
            ServerLevel level = resolveLevel(server, anchor);
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    private static ServerLevel resolveLevel(MinecraftServer server, ChestAnchor anchor) {
        if (anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(anchor.dimensionId())) {
                return level;
            }
        }
        return null;
    }

    public static SingleStackOutcome depositSingleItem(
            ServerPlayer player,
            String laneId,
            int slotIndex,
            ClaimedChest chest
    ) {
        if (player == null || chest == null) {
            return SingleStackOutcome.failed("invalid_args");
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return SingleStackOutcome.failed("server_unavailable");
        }
        int rawSlot = translateToRawSlot(laneId, slotIndex);
        if (rawSlot == Integer.MIN_VALUE) {
            return SingleStackOutcome.failed("invalid_source");
        }
        ItemStack sourceStack = readStack(player, rawSlot);
        if (sourceStack == null || sourceStack.isEmpty()) {
            return SingleStackOutcome.failed("source_empty");
        }
        IItemHandler handler = resolveHandler(server, chest);
        if (handler == null) {
            return SingleStackOutcome.failed("chest_unloaded");
        }
        ItemStack single = sourceStack.copy();
        single.setCount(1);
        ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, single.copy(), true);
        if (!remaining.isEmpty()) {
            return SingleStackOutcome.failed("destination_full");
        }
        ItemStack postInsert = ItemHandlerHelper.insertItemStacked(handler, single.copy(), false);
        if (!postInsert.isEmpty()) {
            return SingleStackOutcome.failed("commit_partial");
        }
        ItemStack newSource = sourceStack.copy();
        newSource.shrink(1);
        writeStack(player, rawSlot, newSource);
        player.getInventory().setChanged();
        SlotCommon.LOGGER.info(
                "[SLOT] deposit-one lane={} slot={} chest={}",
                laneId, slotIndex, chest.storageId()
        );
        return SingleStackOutcome.deposited();
    }

    public static SingleStackOutcome depositSingleStack(
            ServerPlayer player,
            String laneId,
            int slotIndex,
            ClaimedChest chest
    ) {
        if (player == null || chest == null) {
            return SingleStackOutcome.failed("invalid_args");
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return SingleStackOutcome.failed("server_unavailable");
        }
        int rawSlot = translateToRawSlot(laneId, slotIndex);
        if (rawSlot == Integer.MIN_VALUE) {
            return SingleStackOutcome.failed("invalid_source");
        }
        ItemStack sourceStack = readStack(player, rawSlot);
        if (sourceStack == null || sourceStack.isEmpty()) {
            return SingleStackOutcome.failed("source_empty");
        }
        IItemHandler handler = resolveHandler(server, chest);
        if (handler == null) {
            return SingleStackOutcome.failed("chest_unloaded");
        }
        ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, sourceStack.copy(), true);
        if (!remaining.isEmpty()) {
            return SingleStackOutcome.failed("destination_full");
        }
        ItemStack postInsert = ItemHandlerHelper.insertItemStacked(handler, sourceStack.copy(), false);
        if (!postInsert.isEmpty()) {
            return SingleStackOutcome.failed("commit_partial");
        }
        writeStack(player, rawSlot, ItemStack.EMPTY);
        player.getInventory().setChanged();
        SlotCommon.LOGGER.info(
                "[SLOT] deposit-single lane={} slot={} chest={}",
                laneId, slotIndex, chest.storageId()
        );
        return SingleStackOutcome.deposited();
    }

    public record DepositOutcome(int deposited, int failed, Set<UUID> destinations) {
        public DepositOutcome {
            deposited = Math.max(0, deposited);
            failed = Math.max(0, failed);
            destinations = destinations == null ? Set.of() : Set.copyOf(destinations);
        }

        public static DepositOutcome empty() {
            return new DepositOutcome(0, 0, Set.of());
        }
    }

    public record SingleStackOutcome(boolean success, String diagnostic) {
        public SingleStackOutcome {
            diagnostic = diagnostic == null ? "" : diagnostic;
        }

        public static SingleStackOutcome deposited() {
            return new SingleStackOutcome(true, "");
        }

        public static SingleStackOutcome failed(String reason) {
            return new SingleStackOutcome(false, reason);
        }
    }
}
