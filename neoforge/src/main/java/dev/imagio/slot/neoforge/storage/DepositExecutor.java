package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.workspace.DepositPlan;
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
        if (player == null || plan == null || plan.isEmpty() || claimedChestMap == null) {
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
        LinkedHashSet<UUID> destinations = new LinkedHashSet<>();
        ArrayList<DepositRecord> records = new ArrayList<>();

        for (DepositPlan.Assignment assignment : plan.assignments()) {
            ItemStack sourceStack = carried.peek(player, assignment.laneId(), assignment.slotIndex());
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
                WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
                ItemStack simulated = worldStorage.insert(server, target, sourceStack.copy(), true);
                if (!simulated.isEmpty()) {
                    continue;
                }
                ItemStack committed = worldStorage.insert(server, target, sourceStack.copy(), false);
                if (!committed.isEmpty()) {
                    continue;
                }
                chosen = candidateUuid;
                break;
            }

            if (chosen == null) {
                failed++;
                continue;
            }

            int depositedCount = sourceStack.getCount();
            ItemIdentity identity = ItemIdentityMatcher.create(sourceStack);
            ItemStack removed = carried.extract(
                    player, assignment.laneId(), assignment.slotIndex(), depositedCount, false);
            if (removed == null || removed.isEmpty()) {
                failed++;
                continue;
            }
            deposited++;
            destinations.add(chosen);
            records.add(new DepositRecord(chosen, identity, depositedCount));
        }

        if (deposited > 0 || failed > 0) {
            SlotCommon.LOGGER.info(
                    "[SLOT] deposit deposited={} failed={} destinations={}",
                    deposited, failed, destinations
            );
        }
        return new DepositOutcome(deposited, failed, destinations, records);
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
    public record DepositRecord(UUID storageId, ItemIdentity identity, int count) {
        public DepositRecord {
            count = Math.max(1, count);
        }
    }

    public record DepositOutcome(int deposited, int failed, Set<UUID> destinations, List<DepositRecord> records) {
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
