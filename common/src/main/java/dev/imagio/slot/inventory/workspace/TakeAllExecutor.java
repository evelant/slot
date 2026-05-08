package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        List<WorldStorageAccess.SlotContent> contents = worldStorage.enumerate(server, target);
        if (contents.isEmpty()) {
            return TakeAllOutcome.empty();
        }

        int movedStacks = 0;
        int movedItems = 0;
        int leftoverSlots = 0;
        ArrayList<TakeRecord> records = new ArrayList<>();
        for (WorldStorageAccess.SlotContent entry : contents) {
            int slot = entry.slotIndex();
            ItemStack stack = entry.stack();
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            int desired = stack.getCount();
            ItemStack extracted = worldStorage.extract(server, target, slot, desired, false);
            if (extracted == null || extracted.isEmpty()) {
                continue;
            }
            int beforeInsert = extracted.getCount();
            ItemStack remaining = carried.insertBestFit(player, extracted, false);
            int remainingCount = remaining == null || remaining.isEmpty() ? 0 : remaining.getCount();
            int movedFromThisSlot = beforeInsert - remainingCount;
            if (movedFromThisSlot > 0) {
                movedStacks++;
                movedItems += movedFromThisSlot;
                records.add(new TakeRecord(chest.storageId(), identity, movedFromThisSlot));
            }
            if (remainingCount > 0) {
                leftoverSlots++;
                // Put what didn't fit back into the same chest slot; fall back to
                // dropping only if the chest can't re-accept (e.g., slot filters).
                ItemStack putBack = worldStorage.insert(server, target, remaining, false);
                if (putBack != null && !putBack.isEmpty()) {
                    player.drop(putBack, false);
                }
            }
        }
        if (movedStacks > 0 || leftoverSlots > 0) {
            SlotCommon.LOGGER.info(
                    "[SLOT] take-all chest={} moved_stacks={} moved_items={} leftover_slots={}",
                    chest.storageId(), movedStacks, movedItems, leftoverSlots
            );
        }
        return new TakeAllOutcome(movedStacks, movedItems, leftoverSlots, records);
    }

    public static TakeSingleOutcome takeSingleItem(
            ServerPlayer player,
            ClaimedChest chest,
            int chestSlotIndex
    ) {
        return takeFromChestSlot(player, chest, chestSlotIndex, 1, "take-one");
    }

    public static TakeSingleOutcome takeSingleStack(
            ServerPlayer player,
            ClaimedChest chest,
            int chestSlotIndex
    ) {
        return takeFromChestSlot(player, chest, chestSlotIndex, Integer.MAX_VALUE, "take-single");
    }

    /**
     * Walk a chest's contents and take {@code maxCount} of the first
     * slot whose stack matches {@code identity}. Returns the moved/
     * leftover counts. {@code maxCount = 1} for "take one"; pass
     * {@link Integer#MAX_VALUE} for "take whole stack".
     */
    public static TakeSingleOutcome takeByIdentity(
            ServerPlayer player,
            ClaimedChest chest,
            dev.imagio.slot.inventory.core.ItemIdentity identity,
            int maxCount,
            String logLabel
    ) {
        if (player == null || chest == null || identity == null || maxCount <= 0) {
            return TakeSingleOutcome.empty();
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return TakeSingleOutcome.empty();
        }
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        for (WorldStorageAccess.SlotContent entry : worldStorage.enumerate(server, target)) {
            ItemStack stack = entry.stack();
            if (stack.isEmpty()) {
                continue;
            }
            if (!identity.equals(dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(stack))) {
                continue;
            }
            return takeFromChestSlot(player, chest, entry.slotIndex(), maxCount, logLabel);
        }
        return TakeSingleOutcome.empty();
    }

    private static TakeSingleOutcome takeFromChestSlot(
            ServerPlayer player,
            ClaimedChest chest,
            int chestSlotIndex,
            int amount,
            String logLabel
    ) {
        if (player == null || chest == null || amount <= 0) {
            return TakeSingleOutcome.empty();
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return TakeSingleOutcome.empty();
        }
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        if (chestSlotIndex < 0 || chestSlotIndex >= worldStorage.slotCount(server, target)) {
            return TakeSingleOutcome.empty();
        }
        // For "take a stack" the caller passes Integer.MAX_VALUE so the
        // delegate caps at its own stack-size. For "take one" it's literally
        // 1. Simulate first to read what we'd actually get; commit at the
        // end so we know how many to ask back for on leftover.
        ItemStack preview = worldStorage.extract(server, target, chestSlotIndex, amount, true);
        if (preview == null || preview.isEmpty()) {
            return TakeSingleOutcome.empty();
        }
        ItemStack extracted = worldStorage.extract(server, target, chestSlotIndex, preview.getCount(), false);
        if (extracted == null || extracted.isEmpty()) {
            return TakeSingleOutcome.empty();
        }
        int beforeInsert = extracted.getCount();
        ItemStack remaining = carried.insertBestFit(player, extracted, false);
        int remainingCount = remaining == null || remaining.isEmpty() ? 0 : remaining.getCount();
        int moved = beforeInsert - remainingCount;
        if (remainingCount > 0) {
            ItemStack putBack = worldStorage.insert(server, target, remaining, false);
            if (putBack != null && !putBack.isEmpty()) {
                player.drop(putBack, false);
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] {} chest={} slot={} moved={} leftover={}",
                logLabel, chest.storageId(), chestSlotIndex, moved, remainingCount
        );
        ItemIdentity identity = ItemIdentityMatcher.create(preview);
        TakeRecord record = moved > 0 ? new TakeRecord(chest.storageId(), identity, moved) : null;
        return new TakeSingleOutcome(moved, remainingCount, record);
    }

    public record TakeAllOutcome(int movedStacks, int movedItems, int leftoverSlots, List<TakeRecord> records) {
        public TakeAllOutcome {
            movedStacks = Math.max(0, movedStacks);
            movedItems = Math.max(0, movedItems);
            leftoverSlots = Math.max(0, leftoverSlots);
            records = records == null ? List.of() : List.copyOf(records);
        }

        public TakeAllOutcome(int movedStacks, int movedItems, int leftoverSlots) {
            this(movedStacks, movedItems, leftoverSlots, List.of());
        }

        public static TakeAllOutcome empty() {
            return new TakeAllOutcome(0, 0, 0, List.of());
        }
    }

    public record TakeSingleOutcome(int moved, int leftover, TakeRecord record) {
        public TakeSingleOutcome {
            moved = Math.max(0, moved);
            leftover = Math.max(0, leftover);
        }

        public TakeSingleOutcome(int moved, int leftover) {
            this(moved, leftover, null);
        }

        public static TakeSingleOutcome empty() {
            return new TakeSingleOutcome(0, 0, null);
        }

        public boolean tookAnything() {
            return moved > 0;
        }

        public boolean partial() {
            return leftover > 0;
        }
    }

    public record TakeRecord(UUID storageId, ItemIdentity identity, int count) {
        public TakeRecord {
            count = Math.max(0, count);
        }
    }
}
