package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public final class TakeAllExecutor {
    private TakeAllExecutor() {
    }

    public static TakeAllOutcome execute(ServerPlayer player, ClaimedChest chest) {
        if (player == null || chest == null) {
            return TakeAllOutcome.empty();
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return TakeAllOutcome.empty();
        }
        IItemHandler handler = resolveHandler(server, chest);
        if (handler == null) {
            return TakeAllOutcome.empty();
        }

        int movedStacks = 0;
        int movedItems = 0;
        int leftoverSlots = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack contents = handler.getStackInSlot(slot);
            if (contents == null || contents.isEmpty()) {
                continue;
            }
            int originalCount = contents.getCount();
            ItemStack working = handler.extractItem(slot, originalCount, false);
            if (working.isEmpty()) {
                continue;
            }
            int beforeTransfer = working.getCount();
            boolean fullyInserted = player.getInventory().add(working);
            int remaining = working.getCount();
            int movedFromThisSlot = beforeTransfer - remaining;
            if (movedFromThisSlot > 0) {
                movedStacks++;
                movedItems += movedFromThisSlot;
            }
            if (!fullyInserted && remaining > 0) {
                leftoverSlots++;
                ItemStack leftover = handler.insertItem(slot, working, false);
                if (!leftover.isEmpty()) {
                    player.drop(leftover, false);
                }
            }
        }
        player.getInventory().setChanged();
        if (movedStacks > 0 || leftoverSlots > 0) {
            SlotCommon.LOGGER.info(
                    "[SLOT] take-all chest={} moved_stacks={} moved_items={} leftover_slots={}",
                    chest.storageId(), movedStacks, movedItems, leftoverSlots
            );
        }
        return new TakeAllOutcome(movedStacks, movedItems, leftoverSlots);
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

    public record TakeAllOutcome(int movedStacks, int movedItems, int leftoverSlots) {
        public TakeAllOutcome {
            movedStacks = Math.max(0, movedStacks);
            movedItems = Math.max(0, movedItems);
            leftoverSlots = Math.max(0, leftoverSlots);
        }

        public static TakeAllOutcome empty() {
            return new TakeAllOutcome(0, 0, 0);
        }
    }
}
