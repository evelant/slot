package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Common server-side behavior for workspace chest verbs. Platform sessions
 * handle transport and view broadcast; routing, proximity validation, affinity
 * updates, and undo records live here.
 */
public final class WorkspaceChestCommandService {
    private WorkspaceChestCommandService() {
    }

    public static WorkspaceCommandOutcome deposit(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority
    ) {
        if (player == null || runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_deposit_context");
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        SlotCommon.LOGGER.info(
                "[SLOT] deposit command received: player={} claimedChests={} proximate={}",
                player.getName().getString(), claimedChestMap.chests().size(), proximate.size());
        if (proximate.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("no_proximate_chest");
        }

        long tick = player.serverLevel().getGameTime();
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
        DepositPlan plan = DepositPlanner.plan(
                authority == null ? InventoryAuthoritySnapshot.empty() : authority,
                affinityMap,
                claimedChestMap,
                proximate,
                reservedCountResolver(runtime),
                liveChestContentPresence(player)
        );
        SlotCommon.LOGGER.info(
                "[SLOT] deposit plan: assignments={} (one per stack with learned affinity or matching contents)",
                plan.assignments().size());
        DepositExecutor.DepositOutcome outcome = DepositExecutor.execute(player, plan, claimedChestMap);
        for (DepositExecutor.DepositRecord record : outcome.records()) {
            runtime.chestClaimWorkflow().recordDeposit(
                    record.storageId(), record.identity(), record.count(), tick);
        }
        recordDepositUndo(player, runtime, outcome.records());
        if (outcome.deposited() == 0 && outcome.failed() == 0) {
            return WorkspaceCommandOutcome.accepted(
                    "nothing_to_deposit",
                    plan.assignments().isEmpty()
                            ? "no carried stack has learned affinity or matching contents with a proximate chest"
                            : "all candidate chests rejected the items");
        }
        if (outcome.deposited() > 0 && outcome.failed() == 0) {
            return WorkspaceCommandOutcome.accepted("deposited", "deposited=" + outcome.deposited());
        }
        if (outcome.deposited() == 0) {
            return WorkspaceCommandOutcome.rejected("deposit_failed=" + outcome.failed());
        }
        return WorkspaceCommandOutcome.accepted(
                "deposited_partial",
                "deposited=" + outcome.deposited() + " failed=" + outcome.failed());
    }

    public static WorkspaceCommandOutcome takeAllFromChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            String storageIdRaw
    ) {
        ChestProximityResult resolved = resolveProximateChest(player, runtime, storageIdRaw);
        if (resolved.outcome() != null) {
            return WorkspaceCommandOutcome.rejected(resolved.outcome());
        }
        TakeAllExecutor.TakeAllOutcome outcome = TakeAllExecutor.execute(player, resolved.chest());
        recordTakeRecords(player, runtime, outcome.records(), "take_all_from_chest");
        if (outcome.movedStacks() == 0 && outcome.leftoverSlots() == 0) {
            return WorkspaceCommandOutcome.accepted("nothing_to_take", "");
        }
        if (outcome.leftoverSlots() == 0) {
            return WorkspaceCommandOutcome.accepted("took_all", "moved=" + outcome.movedStacks());
        }
        return WorkspaceCommandOutcome.accepted(
                "took_all_partial",
                "moved=" + outcome.movedStacks() + " leftover_slots=" + outcome.leftoverSlots());
    }

    public static WorkspaceCommandOutcome depositCarriedToChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            String storageIdRaw
    ) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        ChestProximityResult resolved = resolveProximateChest(player, runtime, storageIdRaw);
        if (resolved.outcome() != null) {
            return WorkspaceCommandOutcome.rejected(resolved.outcome());
        }
        Optional<CarriedSourceAccess.CarriedLocation> sourceLocation =
                StorageAccessRegistry.carriedSourceAccess().findIdentity(player, identity);
        if (sourceLocation.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("nothing_to_deposit");
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                player,
                sourceLocation.get().sourceId(),
                sourceLocation.get().slotIndex(),
                resolved.chest());
        return applyChestDepositOutcome(player, runtime, outcome, resolved.chest(), true);
    }

    public static WorkspaceCommandOutcome depositHotbarToChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            Integer hotbarIndex,
            String storageIdRaw
    ) {
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_slot");
        }
        ChestProximityResult resolved = resolveProximateChest(player, runtime, storageIdRaw);
        if (resolved.outcome() != null) {
            return WorkspaceCommandOutcome.rejected(resolved.outcome());
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                player,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                index,
                resolved.chest());
        return applyChestDepositOutcome(player, runtime, outcome, resolved.chest(), true);
    }

    public static WorkspaceCommandOutcome depositIdentityToLinkedChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            DepositQuantity quantity,
            DesiredCountPolicy desiredCountPolicy
    ) {
        return depositIdentityToLinkedChest(
                player,
                runtime,
                identity,
                quantity,
                desiredCountPolicy,
                (Supplier<ClaimedChest>) null);
    }

    public static WorkspaceCommandOutcome depositIdentityToLinkedChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            DepositQuantity quantity,
            DesiredCountPolicy desiredCountPolicy,
            Supplier<ClaimedChest> activeChestFallback
    ) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (player == null || runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_deposit_context");
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        List<CarriedSourceAccess.CarriedLocation> locations = carried.findAllMatching(player, identity);
        if (locations.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("nothing_to_deposit");
        }

        int carriedTotal = 0;
        ItemStack representativeStack = ItemStack.EMPTY;
        for (CarriedSourceAccess.CarriedLocation location : locations) {
            ItemStack stack = carried.peek(player, location.sourceId(), location.slotIndex());
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            carriedTotal += stack.getCount();
            if (representativeStack.isEmpty()) {
                representativeStack = stack.copy();
            }
        }
        if (carriedTotal <= 0 || representativeStack.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("nothing_to_deposit");
        }

        DesiredCountPolicy desiredPolicy = desiredCountPolicy == null
                ? DesiredCountPolicy.RESPECT
                : desiredCountPolicy;
        int reserved = Math.max(0, reservedCountResolver(runtime).applyAsInt(identity));
        int requested = requestedExplicitDepositCount(carriedTotal, reserved, quantity, desiredPolicy);
        if (requested <= 0) {
            return WorkspaceCommandOutcome.rejected("desired_count_reserved");
        }

        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        List<String> candidates = explicitDepositCandidates(
                player,
                runtime,
                identity,
                representativeStack,
                requested);
        if (candidates.isEmpty()) {
            ClaimedChest fallbackChest = activeChestFallback == null ? null : activeChestFallback.get();
            candidates = activeChestFallbackDepositCandidate(
                    player,
                    runtime,
                    fallbackChest,
                    representativeStack,
                    requested);
        }
        if (candidates.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("no_linked_proximate_chest_with_room");
        }

        int remainingBudget = requested;
        ArrayList<DepositPlan.Assignment> assignments = new ArrayList<>();
        for (CarriedSourceAccess.CarriedLocation location : locations) {
            if (remainingBudget <= 0) {
                break;
            }
            ItemStack stack = carried.peek(player, location.sourceId(), location.slotIndex());
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int allocated = Math.min(stack.getCount(), remainingBudget);
            if (allocated <= 0) {
                continue;
            }
            assignments.add(new DepositPlan.Assignment(
                    location.sourceId(),
                    location.slotIndex(),
                    identity.itemId(),
                    allocated,
                    candidates
            ));
            remainingBudget -= allocated;
        }
        if (assignments.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("nothing_to_deposit");
        }

        DepositExecutor.DepositOutcome outcome = DepositExecutor.execute(
                player,
                new DepositPlan(assignments),
                claimedChestMap);
        long tick = player.serverLevel().getGameTime();
        int depositedCount = 0;
        for (DepositExecutor.DepositRecord record : outcome.records()) {
            depositedCount += record.count();
            runtime.chestClaimWorkflow().recordDeposit(
                    record.storageId(), record.identity(), record.count(), tick);
        }
        recordDepositUndo(player, runtime, outcome.records());
        if (depositedCount <= 0) {
            return WorkspaceCommandOutcome.rejected(outcome.failed() > 0
                    ? "deposit_failed=" + outcome.failed()
                    : "no_linked_proximate_chest_with_room");
        }
        if (depositedCount < requested || outcome.failed() > 0) {
            return WorkspaceCommandOutcome.accepted(
                    "deposited_partial",
                    "deposited=" + depositedCount + " requested=" + requested + " failed=" + outcome.failed());
        }
        return WorkspaceCommandOutcome.accepted("deposited_stack", "deposited=" + depositedCount);
    }

    public static WorkspaceCommandOutcome takeFromChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            String storageIdRaw,
            Integer chestSlotIndex,
            TakeQuantity quantity
    ) {
        int slotIndex = chestSlotIndex == null ? -1 : chestSlotIndex;
        if (slotIndex < 0) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_slot");
        }
        ChestProximityResult resolved = resolveProximateChest(player, runtime, storageIdRaw);
        if (resolved.outcome() != null) {
            return WorkspaceCommandOutcome.rejected(resolved.outcome());
        }
        boolean one = quantity == TakeQuantity.ITEM;
        TakeAllExecutor.TakeSingleOutcome outcome = one
                ? TakeAllExecutor.takeSingleItem(player, resolved.chest(), slotIndex)
                : TakeAllExecutor.takeSingleStack(player, resolved.chest(), slotIndex);
        if (!outcome.tookAnything()) {
            return WorkspaceCommandOutcome.accepted("nothing_to_take", "");
        }
        recordTakeRecord(player, runtime, outcome.record(), "take_from_chest");
        if (one) {
            return WorkspaceCommandOutcome.accepted("took_one", "moved=" + outcome.moved());
        }
        if (outcome.partial()) {
            return WorkspaceCommandOutcome.accepted(
                    "took_partial",
                    "moved=" + outcome.moved() + " leftover=" + outcome.leftover());
        }
        return WorkspaceCommandOutcome.accepted("took_stack", "moved=" + outcome.moved());
    }

    public static WorkspaceCommandOutcome takeByIdentity(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int maxCount
    ) {
        return takeByIdentity(player, runtime, identity, maxCount, true);
    }

    public static WorkspaceCommandOutcome takeByIdentity(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int maxCount,
            boolean recordUndo
    ) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (player == null || runtime == null || maxCount <= 0) {
            return WorkspaceCommandOutcome.rejected("invalid_take_context");
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        if (proximate.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("no_proximate_chest");
        }
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
        List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                identity, claimedChestMap, affinityMap, proximate);
        boolean foundMatchButCouldNotInsert = false;
        boolean one = maxCount == 1;
        for (ClaimedChest chest : ranked) {
            TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                    player,
                    chest,
                    identity,
                    maxCount,
                    one ? "take-one-by-identity" : "take-stack-by-identity");
            if (outcome.tookAnything()) {
                if (recordUndo) {
                    recordTakeRecord(player, runtime, outcome.record(), "take_by_identity");
                } else if (outcome.record() != null) {
                    recordAcquisition(
                            runtime,
                            outcome.record().identity(),
                            outcome.record().count(),
                            "take_by_identity");
                }
                return WorkspaceCommandOutcome.accepted(
                        one ? "took_one" : "took_stack",
                        "moved=" + outcome.moved());
            }
            if (outcome.partial()) {
                foundMatchButCouldNotInsert = true;
            }
        }
        return foundMatchButCouldNotInsert
                ? WorkspaceCommandOutcome.rejected("carry_full")
                : WorkspaceCommandOutcome.accepted("nothing_to_take", "no_matching_proximate_chest");
    }

    /**
     * First shift-click take semantics: fill an unmet desired/kit carry
     * reservation when one exists; otherwise take one stack. Follow-up
     * clicks in the same held-Shift sequence use {@link #takeByIdentity}
     * with {@link Integer#MAX_VALUE} directly.
     */
    public static WorkspaceCommandOutcome takeDesiredGapOrStackByIdentity(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity
    ) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (player == null || runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_take_context");
        }
        int reserved = Math.max(0, reservedCountResolver(runtime).applyAsInt(identity));
        int carried = totalCarriedCount(player, identity);
        int gap = Math.max(0, reserved - carried);
        return takeByIdentity(player, runtime, identity, gap > 0 ? gap : Integer.MAX_VALUE);
    }

    public static ClaimedChest resolveProximateLinkedChestForIdentity(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            ItemStack sourceStack
    ) {
        if (player == null || runtime == null || identity == null || sourceStack == null || sourceStack.isEmpty()) {
            return null;
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        if (proximate.isEmpty()) {
            return null;
        }
        MinecraftServer server = player.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return null;
        }
        long tick = player.serverLevel().getGameTime();
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                identity,
                claimedChestMap,
                affinityMap,
                proximate,
                liveChestContentPresence(player));
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        for (UUID storageId : ranked) {
            ClaimedChest chest = claimedChestMap.chest(storageId);
            if (chest == null) {
                continue;
            }
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            if (!world.isAccessible(server, target)) {
                continue;
            }
            ItemStack simulation = world.insert(server, target, sourceStack.copy(), true);
            if (simulation == null || simulation.isEmpty()) {
                return chest;
            }
        }
        return null;
    }

    private static List<String> explicitDepositCandidates(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            ItemStack sourceStack,
            int requestedCount
    ) {
        if (player == null || runtime == null || identity == null
                || sourceStack == null || sourceStack.isEmpty() || requestedCount <= 0) {
            return List.of();
        }
        MinecraftServer server = player.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return List.of();
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        if (proximate.isEmpty()) {
            return List.of();
        }
        long tick = player.serverLevel().getGameTime();
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                identity,
                claimedChestMap,
                affinityMap,
                proximate,
                liveChestContentPresence(player));
        if (ranked.isEmpty()) {
            return List.of();
        }

        int probeCount = Math.max(1, Math.min(sourceStack.getMaxStackSize(),
                Math.min(requestedCount, sourceStack.getCount())));
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        ArrayList<String> candidates = new ArrayList<>();
        for (UUID storageId : ranked) {
            ClaimedChest chest = claimedChestMap.chest(storageId);
            if (chest == null) {
                continue;
            }
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            if (!world.isAccessible(server, target)) {
                continue;
            }
            ItemStack probe = sourceStack.copy();
            probe.setCount(probeCount);
            ItemStack leftover = world.insert(server, target, probe, true);
            int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
            if (leftoverCount < probeCount) {
                candidates.add(storageId.toString());
            }
        }
        return List.copyOf(candidates);
    }

    private static List<String> activeChestFallbackDepositCandidate(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ClaimedChest fallbackChest,
            ItemStack sourceStack,
            int requestedCount
    ) {
        if (player == null || runtime == null || fallbackChest == null
                || sourceStack == null || sourceStack.isEmpty() || requestedCount <= 0) {
            return List.of();
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        if (claimedChestMap.chest(fallbackChest.storageId()) == null) {
            return List.of();
        }
        MinecraftServer server = player.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return List.of();
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(fallbackChest);
        if (!world.isAccessible(server, target)) {
            return List.of();
        }
        int probeCount = Math.max(1, Math.min(sourceStack.getMaxStackSize(),
                Math.min(requestedCount, sourceStack.getCount())));
        ItemStack probe = sourceStack.copy();
        probe.setCount(probeCount);
        ItemStack leftover = world.insert(server, target, probe, true);
        int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
        return leftoverCount < probeCount ? List.of(fallbackChest.storageId().toString()) : List.of();
    }

    static int requestedExplicitDepositCount(
            int carriedTotal,
            int reservedCount,
            DepositQuantity quantity,
            DesiredCountPolicy desiredCountPolicy
    ) {
        int carried = Math.max(0, carriedTotal);
        int reserved = desiredCountPolicy == DesiredCountPolicy.IGNORE ? 0 : Math.max(0, reservedCount);
        int depositable = Math.max(0, carried - reserved);
        return switch (quantity == null ? DepositQuantity.STACK : quantity) {
            case STACK -> depositable;
            case ITEM -> Math.min(1, depositable);
        };
    }

    static DepositPlanner.ChestContentPresence liveChestContentPresence(ServerPlayer player) {
        if (player == null || player.getServer() == null || !StorageAccessRegistry.isInstalled()) {
            return (chest, identity) -> false;
        }
        MinecraftServer server = player.getServer();
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        LinkedHashMap<UUID, Set<ItemIdentity>> identitiesByChest = new LinkedHashMap<>();
        return (chest, identity) -> {
            if (chest == null || identity == null) {
                return false;
            }
            Set<ItemIdentity> identities = identitiesByChest.computeIfAbsent(
                    chest.storageId(),
                    ignored -> liveChestIdentities(server, world, chest));
            return identities.contains(ItemIdentityMatcher.normalizeMovable(identity));
        };
    }

    private static Set<ItemIdentity> liveChestIdentities(
            MinecraftServer server,
            WorldStorageAccess world,
            ClaimedChest chest
    ) {
        if (server == null || world == null || chest == null) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        for (WorldStorageAccess.SlotContent entry : world.enumerate(server, target)) {
            ItemStack stack = entry.stack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            identities.add(ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack)));
        }
        return Set.copyOf(identities);
    }

    public static ChestProximityResult resolveProximateChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            String storageIdRaw
    ) {
        if (player == null || runtime == null) {
            return ChestProximityResult.failed("invalid_chest_context");
        }
        UUID storageId = parseUuid(storageIdRaw);
        if (storageId == null) {
            return ChestProximityResult.failed("invalid_chest_storage_id");
        }
        ClaimedChestMap map = runtime.chestClaimWorkflow().claimedChestMap();
        ClaimedChest chest = map.chest(storageId);
        if (chest == null) {
            return ChestProximityResult.failed("unknown_chest_tile");
        }
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, map);
        if (!proximate.contains(storageIdRaw)) {
            return ChestProximityResult.failed("not_proximate");
        }
        return new ChestProximityResult(chest, null);
    }

    private static WorkspaceCommandOutcome applyChestDepositOutcome(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            DepositExecutor.SingleStackOutcome outcome,
            ClaimedChest chest,
            boolean recordUndo
    ) {
        if (outcome == null || !outcome.success()) {
            return WorkspaceCommandOutcome.rejected(outcome == null ? "deposit_failed" : outcome.diagnostic());
        }
        if (runtime != null && outcome.record() != null) {
            runtime.chestClaimWorkflow().recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    player.serverLevel().getGameTime());
            if (recordUndo) {
                recordChestTransferUndo(
                        player,
                        runtime,
                        outcome.record().storageId(),
                        outcome.record().identity(),
                        outcome.record().count(),
                        ChestTransferDirection.DEPOSIT);
            }
        }
        return WorkspaceCommandOutcome.accepted("deposited_stack", "deposited to " + chestLabel(chest));
    }

    private static void recordDepositUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            List<DepositExecutor.DepositRecord> records
    ) {
        if (player == null || runtime == null || records == null || records.isEmpty()) {
            return;
        }
        List<DepositExecutor.DepositRecord> captured = List.copyOf(records);
        String label = captured.size() == 1
                ? "deposit " + captured.get(0).identity().itemId()
                : "deposit (" + captured.size() + ")";
        runtime.undoStack().record(
                label,
                ctx -> {
                    for (DepositExecutor.DepositRecord record : captured) {
                        WorkspaceChestTransferReverser.pullFromChestToCarry(
                                player, ctx.runtime(), record.storageId(), record.identity(), record.count());
                    }
                },
                ctx -> {
                    for (DepositExecutor.DepositRecord record : captured) {
                        WorkspaceChestTransferReverser.pushFromCarryToChest(
                                player, ctx.runtime(), record.storageId(), record.identity(), record.count());
                    }
                }
        );
    }

    static void recordTakeRecords(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            List<TakeAllExecutor.TakeRecord> records,
            String diagnostics
    ) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (TakeAllExecutor.TakeRecord record : records) {
            recordTakeRecord(player, runtime, record, diagnostics);
        }
    }

    static void recordTakeRecord(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            TakeAllExecutor.TakeRecord record,
            String diagnostics
    ) {
        if (record == null) {
            return;
        }
        recordAcquisition(runtime, record.identity(), record.count(), diagnostics);
        recordChestTransferUndo(
                player, runtime, record.storageId(), record.identity(), record.count(),
                ChestTransferDirection.TAKE);
    }

    private static void recordAcquisition(
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int count,
            String diagnostics
    ) {
        InventoryAcquisitionActivityRecorder.recordIdentityAcquired(
                runtime,
                identity,
                count,
                InventoryActivityProducer.EXTERNAL_WITHDRAWAL,
                InventoryActivityConfidence.AUTHORITATIVE,
                diagnostics);
    }

    private static void recordChestTransferUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            UUID storageId,
            ItemIdentity identity,
            int count,
            ChestTransferDirection direction
    ) {
        if (player == null || runtime == null || storageId == null || identity == null
                || count <= 0 || direction == null) {
            return;
        }
        String verb = direction == ChestTransferDirection.DEPOSIT ? "deposit" : "take";
        String label = count == 1
                ? verb + " " + identity.itemId()
                : verb + " " + identity.itemId() + " x" + count;
        runtime.undoStack().record(
                label,
                ctx -> {
                    if (direction == ChestTransferDirection.DEPOSIT) {
                        WorkspaceChestTransferReverser.pullFromChestToCarry(
                                player, ctx.runtime(), storageId, identity, count);
                    } else {
                        WorkspaceChestTransferReverser.pushFromCarryToChest(
                                player, ctx.runtime(), storageId, identity, count);
                    }
                },
                ctx -> {
                    if (direction == ChestTransferDirection.DEPOSIT) {
                        WorkspaceChestTransferReverser.pushFromCarryToChest(
                                player, ctx.runtime(), storageId, identity, count);
                    } else {
                        WorkspaceChestTransferReverser.pullFromChestToCarry(
                                player, ctx.runtime(), storageId, identity, count);
                    }
                }
        );
    }

    private static ToIntFunction<ItemIdentity> reservedCountResolver(WorkflowDomainRuntime runtime) {
        return identity -> {
            if (runtime == null || identity == null) {
                return 0;
            }
            var snapshot = runtime.snapshot();
            var kitMap = snapshot.kitMap();
            var activation = kitMap == null ? null : kitMap.activation();
            String kitId = activation != null && activation.isActive() ? activation.kitId() : null;
            Map<ItemIdentity, Integer> activeKitDesired = kitId == null
                    ? Map.of()
                    : snapshot.kitDesiredCounts().getOrDefault(kitId, Map.of());
            return SlotWorkspaceViewModel.reservedCarryCount(
                    identity,
                    kitMap,
                    activeKitDesired,
                    snapshot.playerDesiredCounts(),
                    snapshot.playerWantedCounts());
        };
    }

    private static int totalCarriedCount(ServerPlayer player, ItemIdentity identity) {
        if (player == null || identity == null) {
            return 0;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        int total = 0;
        for (CarriedSourceAccess.CarriedLocation location : carried.findAllMatching(player, identity)) {
            ItemStack stack = carried.peek(player, location.sourceId(), location.slotIndex());
            if (stack != null && !stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static String chestLabel(ClaimedChest chest) {
        if (chest == null) {
            return "chest";
        }
        if (chest.label() != null && !chest.label().isBlank()) {
            return chest.label();
        }
        String hex = chest.storageId().toString();
        int dash = hex.indexOf('-');
        String shortId = dash < 0 ? hex : hex.substring(0, dash);
        if (shortId.length() > 4) {
            shortId = shortId.substring(shortId.length() - 4);
        }
        return "Chest #" + shortId;
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private enum ChestTransferDirection { DEPOSIT, TAKE }

    public enum DepositQuantity { STACK, ITEM }

    /**
     * Whether an explicit deposit gesture protects kit / desired-count
     * reservations. Shift-click deposits excess only; shift-scroll is a
     * precise manual move and intentionally ignores reservations.
     */
    public enum DesiredCountPolicy { RESPECT, IGNORE }

    public enum TakeQuantity { STACK, ITEM }

    public record ChestProximityResult(ClaimedChest chest, String outcome) {
        private static ChestProximityResult failed(String reason) {
            return new ChestProximityResult(null, reason);
        }
    }
}
