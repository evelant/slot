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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DepositExecutor {
    private DepositExecutor() {
    }

    public static DepositOutcome execute(
            ServerPlayer player,
            DepositPlan plan,
            ClaimedChestMap claimedChestMap
    ) {
        if (player == null || plan == null || claimedChestMap == null) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] deposit execute aborted: player={} plan={} claimedChestMap={}",
                    player, plan, claimedChestMap);
            return DepositOutcome.empty();
        }
        if (plan.isEmpty()) {
            SlotCommon.LOGGER.info(
                    "[SLOT] deposit execute: plan is empty (no carried stack had eligible affinity or matching contents)");
            return DepositOutcome.empty();
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return DepositOutcome.empty();
        }

        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        int deposited = 0;
        int failed = 0;
        LinkedHashSet<String> destinations = new LinkedHashSet<>();
        ArrayList<DepositRecord> records = new ArrayList<>();

        for (DepositPlan.Assignment assignment : plan.assignments()) {
            ItemStack sourceStack = carried.peek(player, assignment.laneId(), assignment.slotIndex());
            if (sourceStack == null || sourceStack.isEmpty()) {
                failed++;
                continue;
            }
            int budget = Math.min(assignment.count(), sourceStack.getCount());
            if (budget <= 0) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(sourceStack);
            List<DepositTargetCandidate> candidates = depositTargetsThatAccept(
                    server,
                    worldStorage,
                    assignment.candidateStorageIds(),
                    claimedChestMap,
                    sourceStack,
                    budget);
            if (candidates.isEmpty()) {
                failed++;
                continue;
            }

            // Extract from carry first so we own a real stack to push into
            // chests. Anything we can't insert later is reinserted into
            // carry — never duplicated, never lost.
            ItemStack inHand = carried.extract(
                    player, assignment.laneId(), assignment.slotIndex(), budget, false);
            if (inHand == null || inHand.isEmpty()) {
                failed++;
                continue;
            }
            ItemStack remaining = inHand;
            int insertedTotal = 0;

            // Walk candidates in rank order; each chest takes what it can,
            // leftover spills to the next. Different from the prior
            // all-or-nothing path: a chest with room for 30 of 54 used to
            // be skipped (and the whole assignment failed if no single
            // chest fit everything); now its 30 land there and the other
            // 24 fall through to the next chest.
            for (DepositTargetCandidate candidate : candidates) {
                if (remaining.isEmpty()) {
                    break;
                }
                String candidateId = candidate.storageId();
                WorldStorageAccess.Target target = candidate.target();
                int beforeCount = remaining.getCount();
                ItemStack leftover = worldStorage.insert(server, target, remaining, false);
                int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
                int insertedHere = beforeCount - leftoverCount;
                if (insertedHere <= 0) {
                    continue;
                }
                destinations.add(candidateId);
                records.add(new DepositRecord(candidateId, identity, insertedHere));
                insertedTotal += insertedHere;
                remaining = leftover == null ? ItemStack.EMPTY : leftover;
            }

            // Put any leftover back into carry. Best-effort; if even carry
            // can't take it (shouldn't happen — we just extracted from
            // there), drop in world to avoid item loss.
            if (!remaining.isEmpty()) {
                ItemStack carryLeftover = carried.insertBestFit(player, remaining, false);
                if (carryLeftover != null && !carryLeftover.isEmpty()) {
                    SlotCommon.LOGGER.warn(
                            "[SLOT] deposit: dropping {} of {} (couldn't reinsert into carry)",
                            carryLeftover.getCount(), identity.itemId());
                    player.drop(carryLeftover, false);
                }
            }

            if (insertedTotal > 0) {
                deposited++;
            } else {
                failed++;
            }
        }

        SlotCommon.LOGGER.info(
                "[SLOT] deposit deposited={} failed={} destinations={}",
                deposited, failed, destinations);
        return new DepositOutcome(deposited, failed, destinations, records);
    }

    private static List<DepositTargetCandidate> depositTargetsThatAccept(
            MinecraftServer server,
            WorldStorageAccess worldStorage,
            List<String> candidateStorageIds,
            ClaimedChestMap claimedChestMap,
            ItemStack sourceStack,
            int budget
    ) {
        if (worldStorage == null || candidateStorageIds == null || candidateStorageIds.isEmpty()
                || sourceStack == null || sourceStack.isEmpty() || budget <= 0) {
            return List.of();
        }
        ArrayList<DepositTargetCandidate> out = new ArrayList<>();
        for (String candidateId : candidateStorageIds) {
            WorldStorageAccess.Target target = depositTarget(candidateId, claimedChestMap);
            if (target == null) {
                continue;
            }
            if (StorageMutationProbe.canInsertAny(server, worldStorage, target, sourceStack, budget)) {
                out.add(new DepositTargetCandidate(candidateId, target));
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static WorldStorageAccess.Target depositTarget(String storageId, ClaimedChestMap claimedChestMap) {
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        try {
            UUID candidateUuid = UUID.fromString(storageId);
            ClaimedChest chest = claimedChestMap == null ? null : claimedChestMap.chest(candidateUuid);
            return chest == null ? null : new WorldStorageAccess.Target.Chest(chest);
        } catch (IllegalArgumentException ignored) {
            return WorldDisplayStorageSource.targetFromStorageId(storageId)
                    .filter(target -> target.kind().depositTarget())
                    .map(target -> (WorldStorageAccess.Target) target)
                    .orElse(null);
        }
    }

    private record DepositTargetCandidate(String storageId, WorldStorageAccess.Target target) {
        private DepositTargetCandidate {
            storageId = storageId == null ? "" : storageId;
        }
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
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        ItemStack sourceStack = carried.peek(player, laneId, slotIndex);
        if (sourceStack == null || sourceStack.isEmpty()) {
            return SingleStackOutcome.failed("source_empty");
        }
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        ItemStack single = sourceStack.copy();
        single.setCount(1);
        ItemStack remaining = worldStorage.insert(server, target, single.copy(), true);
        if (!remaining.isEmpty()) {
            return SingleStackOutcome.failed("destination_full");
        }
        ItemStack committed = worldStorage.insert(server, target, single.copy(), false);
        if (!committed.isEmpty()) {
            return SingleStackOutcome.failed("commit_partial");
        }
        ItemIdentity identity = ItemIdentityMatcher.create(sourceStack);
        ItemStack removed = carried.extract(player, laneId, slotIndex, 1, false);
        if (removed == null || removed.isEmpty()) {
            return SingleStackOutcome.failed("extract_failed");
        }
        SlotCommon.LOGGER.info(
                "[SLOT] deposit-one lane={} slot={} chest={}",
                laneId, slotIndex, chest.storageId()
        );
        return SingleStackOutcome.deposited(new DepositRecord(chest.storageId(), identity, 1));
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
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        ItemStack sourceStack = carried.peek(player, laneId, slotIndex);
        if (sourceStack == null || sourceStack.isEmpty()) {
            return SingleStackOutcome.failed("source_empty");
        }
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        ItemStack remaining = worldStorage.insert(server, target, sourceStack.copy(), true);
        if (!remaining.isEmpty()) {
            return SingleStackOutcome.failed("destination_full");
        }
        ItemStack committed = worldStorage.insert(server, target, sourceStack.copy(), false);
        if (!committed.isEmpty()) {
            return SingleStackOutcome.failed("commit_partial");
        }
        int depositedCount = sourceStack.getCount();
        ItemIdentity identity = ItemIdentityMatcher.create(sourceStack);
        ItemStack removed = carried.extract(player, laneId, slotIndex, depositedCount, false);
        if (removed == null || removed.isEmpty()) {
            return SingleStackOutcome.failed("extract_failed");
        }
        SlotCommon.LOGGER.info(
                "[SLOT] deposit-single lane={} slot={} chest={}",
                laneId, slotIndex, chest.storageId()
        );
        return SingleStackOutcome.deposited(new DepositRecord(chest.storageId(), identity, depositedCount));
    }

    /** One observed deposit: identity, count, target chest. Drives affinity bumps. */
    public record DepositRecord(String storageId, ItemIdentity identity, int count) {
        public DepositRecord {
            storageId = storageId == null ? "" : storageId;
            count = Math.max(1, count);
        }

        public DepositRecord(UUID storageId, ItemIdentity identity, int count) {
            this(storageId == null ? "" : storageId.toString(), identity, count);
        }

        public UUID storageUuid() {
            if (storageId == null || storageId.isBlank()) {
                return null;
            }
            try {
                return UUID.fromString(storageId);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    public record DepositOutcome(int deposited, int failed, Set<String> destinations, List<DepositRecord> records) {
        public DepositOutcome {
            deposited = Math.max(0, deposited);
            failed = Math.max(0, failed);
            destinations = destinations == null ? Set.of() : Set.copyOf(destinations);
            records = records == null ? List.of() : List.copyOf(records);
        }

        public static DepositOutcome empty() {
            return new DepositOutcome(0, 0, Set.of(), List.of());
        }
    }

    public record SingleStackOutcome(boolean success, String diagnostic, DepositRecord record) {
        public SingleStackOutcome {
            diagnostic = diagnostic == null ? "" : diagnostic;
        }

        public static SingleStackOutcome deposited(DepositRecord record) {
            return new SingleStackOutcome(true, "", record);
        }

        public static SingleStackOutcome failed(String reason) {
            return new SingleStackOutcome(false, reason, null);
        }
    }
}
