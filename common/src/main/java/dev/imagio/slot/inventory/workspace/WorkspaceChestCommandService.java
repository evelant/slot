package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackTags;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowTabTargets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
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
        InventoryAuthoritySnapshot resolvedAuthority = depositAuthority(player, authority);
        WorkspaceStorageRoutingContext routing =
                WorkspaceStorageRoutingContext.build(player, runtime, resolvedAuthority);
        ClaimedChestMap claimedChestMap = routing.claimedChestMap();
        Set<String> proximate = routing.proximateStorageIds();
        List<WorldDisplayStorageSource> displaySources = routing.displaySources();
        SlotCommon.LOGGER.info(
                "[SLOT] deposit command received: player={} claimedChests={} proximate={} displays={}",
                player.getName().getString(), claimedChestMap.chests().size(), proximate.size(), displaySources.size());
        if (proximate.isEmpty() && !routing.hasDisplayDepositTarget()) {
            return WorkspaceCommandOutcome.rejected("no_proximate_chest");
        }

        ToIntFunction<ItemIdentity> reservedCounts = reservedCountResolver(runtime);
        DepositPlanner.StackProtection acceptedInputProtection = acceptedInputProtection(runtime);
        DepositPlanner.StackProtection workflowPutAwayProtection = activeWorkflowPutAwayProtection(runtime);
        DepositPlanner.StackProtection stackProtection = combineStackProtection(
                acceptedInputProtection,
                workflowPutAwayProtection);
        DepositPlan plan = DepositPlanner.plan(
                resolvedAuthority,
                routing.affinityMap(),
                claimedChestMap,
                proximate,
                reservedCounts,
                routing.liveChestContentPresence(),
                routing.liveStorageAffinityEligibility(),
                stackProtection
        );
        plan = withDisplayDepositAssignments(
                player,
                resolvedAuthority,
                plan,
                displaySources,
                reservedCounts,
                stackProtection);
        SlotCommon.LOGGER.info(
                "[SLOT] deposit plan: assignments={} (one per stack with eligible learned affinity or matching contents)",
                plan.assignments().size());
        DepositExecutor.DepositOutcome outcome =
                DepositExecutor.execute(player, plan, claimedChestMap, displaySources);
        observeStorageIds(player, claimedChestMap, outcome.destinations(), "slot.deposit");
        for (DepositExecutor.DepositRecord record : outcome.records()) {
            UUID storageUuid = record.storageUuid();
            if (storageUuid != null) {
                runtime.chestClaimWorkflow().recordDeposit(
                        storageUuid, record.identity(), record.count(), routing.tick());
            }
        }
        recordDepositUndo(player, runtime, outcome.records());
        if (outcome.deposited() == 0 && outcome.failed() == 0) {
            return WorkspaceCommandOutcome.accepted(
                    "nothing_to_deposit",
                    plan.assignments().isEmpty()
                            ? "no carried stack has eligible learned affinity or matching contents with a proximate chest"
                            : "all candidate chests rejected the items")
                    .withInvalidations(List.of(WorkspaceInvalidation.frame(
                            WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                            "nothing_to_deposit")));
        }
        if (outcome.deposited() > 0 && outcome.failed() == 0) {
            return WorkspaceCommandOutcome.accepted("deposited", "deposited=" + outcome.deposited())
                    .withInvalidations(depositRecordInvalidations(outcome.records(), "deposit_records"));
        }
        if (outcome.deposited() == 0) {
            return WorkspaceCommandOutcome.rejected("deposit_failed=" + outcome.failed());
        }
        return WorkspaceCommandOutcome.accepted(
                "deposited_partial",
                "deposited=" + outcome.deposited() + " failed=" + outcome.failed())
                .withInvalidations(depositRecordInvalidations(outcome.records(), "deposit_records_partial"));
    }

    private static InventoryAuthoritySnapshot depositAuthority(
            ServerPlayer player,
            InventoryAuthoritySnapshot fallback
    ) {
        InventoryAuthoritySnapshot resolved = fallback == null
                ? InventoryAuthoritySnapshot.empty()
                : fallback;
        if (player == null || !StorageAccessRegistry.isInstalled()) {
            return resolved;
        }
        InventoryAuthoritySnapshot carriedAuthority =
                StorageAccessRegistry.carriedSourceAccess().currentAuthority(player);
        return hasDeclaredCarriedSources(carriedAuthority) ? carriedAuthority : resolved;
    }

    private static boolean hasDeclaredCarriedSources(InventoryAuthoritySnapshot authority) {
        return authority != null && !authority.carriedSources().isEmpty();
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
        List<InventoryActivityEvent> activityEvents =
                recordTakeRecords(player, runtime, outcome.records(), "take_all_from_chest");
        observeTakeRecords(player, runtime.chestClaimWorkflow().claimedChestMap(), outcome.records(), "slot.take_all");
        if (outcome.movedStacks() == 0 && outcome.leftoverSlots() == 0) {
            return WorkspaceCommandOutcome.accepted("nothing_to_take", "")
                    .withInvalidations(List.of(WorkspaceInvalidation.frame(
                            WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                            "nothing_to_take")));
        }
        if (outcome.leftoverSlots() == 0) {
            return WorkspaceCommandOutcome.accepted("took_all", "moved=" + outcome.movedStacks())
                    .withActivityEvents(activityEvents)
                    .withInvalidations(takeRecordInvalidations(outcome.records(), "take_all_records"));
        }
        return WorkspaceCommandOutcome.accepted(
                "took_all_partial",
                "moved=" + outcome.movedStacks() + " leftover_slots=" + outcome.leftoverSlots())
                .withActivityEvents(activityEvents)
                .withInvalidations(takeRecordInvalidations(outcome.records(), "take_all_records_partial"));
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
        return depositIdentityToLinkedChest(
                player,
                runtime,
                identity,
                quantity,
                desiredCountPolicy,
                activeChestFallback,
                null);
    }

    public static WorkspaceCommandOutcome depositIdentityCountToLinkedChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int requestedCount,
            DesiredCountPolicy desiredCountPolicy,
            Supplier<ClaimedChest> activeChestFallback
    ) {
        return depositIdentityToLinkedChest(
                player,
                runtime,
                identity,
                DepositQuantity.ITEM,
                desiredCountPolicy,
                activeChestFallback,
                Math.max(0, requestedCount));
    }

    private static WorkspaceCommandOutcome depositIdentityToLinkedChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            DepositQuantity quantity,
            DesiredCountPolicy desiredCountPolicy,
            Supplier<ClaimedChest> activeChestFallback,
            Integer requestedCountOverride
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
        int requested = requestedExplicitDepositCount(
                carriedTotal,
                reserved,
                quantity,
                desiredPolicy,
                requestedCountOverride);
        if (requested <= 0) {
            return WorkspaceCommandOutcome.rejected("desired_count_reserved");
        }

        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        ExplicitDepositCandidates candidateResult = explicitDepositCandidates(
                player,
                runtime,
                identity,
                representativeStack,
                requested);
        List<String> candidates = candidateResult.storageIds();
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
                claimedChestMap,
                candidateResult.displaySources());
        observeStorageIds(player, claimedChestMap, outcome.destinations(), "slot.deposit.identity");
        long tick = player.serverLevel().getGameTime();
        int depositedCount = 0;
        for (DepositExecutor.DepositRecord record : outcome.records()) {
            depositedCount += record.count();
            UUID storageUuid = record.storageUuid();
            if (storageUuid != null) {
                runtime.chestClaimWorkflow().recordDeposit(
                        storageUuid, record.identity(), record.count(), tick);
            }
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
                    "deposited=" + depositedCount + " requested=" + requested + " failed=" + outcome.failed())
                    .withInvalidations(depositRecordInvalidations(outcome.records(), "deposit_identity_records_partial"));
        }
        return WorkspaceCommandOutcome.accepted("deposited_stack", "deposited=" + depositedCount)
                .withInvalidations(depositRecordInvalidations(outcome.records(), "deposit_identity_records"));
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
            return WorkspaceCommandOutcome.accepted("nothing_to_take", "")
                    .withInvalidations(List.of(WorkspaceInvalidation.frame(
                            WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                            "nothing_to_take")));
        }
        InventoryActivityEvent activityEvent = recordTakeRecord(player, runtime, outcome.record(), "take_from_chest");
        observeTakeRecord(player, runtime.chestClaimWorkflow().claimedChestMap(), outcome.record(), "slot.take");
        if (one) {
            return WorkspaceCommandOutcome.accepted("took_one", "moved=" + outcome.moved())
                    .withActivityEvents(activityEvents(activityEvent))
                    .withInvalidations(takeRecordInvalidations(takeRecordList(outcome.record()), "take_one_record"));
        }
        if (outcome.partial()) {
            return WorkspaceCommandOutcome.accepted(
                    "took_partial",
                    "moved=" + outcome.moved() + " leftover=" + outcome.leftover())
                    .withActivityEvents(activityEvents(activityEvent))
                    .withInvalidations(takeRecordInvalidations(
                            takeRecordList(outcome.record()),
                            "take_stack_record_partial"));
        }
        return WorkspaceCommandOutcome.accepted("took_stack", "moved=" + outcome.moved())
                .withActivityEvents(activityEvents(activityEvent))
                .withInvalidations(takeRecordInvalidations(takeRecordList(outcome.record()), "take_stack_record"));
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
        WorkspaceStorageRoutingContext routing =
                WorkspaceStorageRoutingContext.build(player, runtime, InventoryAuthoritySnapshot.empty());
        ClaimedChestMap claimedChestMap = routing.claimedChestMap();
        Set<String> proximate = routing.proximateStorageIds();
        List<WorldDisplayStorageSource> displaySources = routing.displaySources();
        if (!routing.hasNearbyClaimedOrDisplayStorage()) {
            return WorkspaceCommandOutcome.rejected("no_proximate_chest");
        }
        List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                identity, claimedChestMap, routing.affinityMap(), proximate);
        boolean foundMatchButCouldNotInsert = false;
        boolean one = maxCount == 1;
        boolean singleSlotOnly = one || maxCount == Integer.MAX_VALUE;
        int requested = singleSlotOnly ? maxCount : Math.max(1, maxCount);
        int moved = 0;
        boolean stoppedByPartial = false;
        List<TakeAllExecutor.TakeRecord> records = new ArrayList<>();
        while (moved < requested) {
            boolean progressed = false;
            int request = singleSlotOnly ? maxCount : requested - moved;
            for (ClaimedChest chest : ranked) {
                TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                        player,
                        chest,
                        identity,
                        request,
                        one ? "take-one-by-identity"
                                : singleSlotOnly ? "take-stack-by-identity" : "take-items-by-identity");
                if (outcome.tookAnything()) {
                    moved += outcome.moved();
                    if (outcome.record() != null) {
                        records.add(outcome.record());
                    }
                    stoppedByPartial = outcome.partial();
                    progressed = true;
                    break;
                }
                if (outcome.partial()) {
                    foundMatchButCouldNotInsert = true;
                }
            }
            if (!progressed) {
                for (WorldDisplayStorageSource source : displaySources) {
                    if (source == null || source.contents().isEmpty()) {
                        continue;
                    }
                    TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                            player,
                            source.target(),
                            source.storageId(),
                            identity,
                            request,
                            one ? "take-one-by-display"
                                    : singleSlotOnly ? "take-stack-by-display" : "take-items-by-display");
                    if (outcome.tookAnything()) {
                        moved += outcome.moved();
                        if (outcome.record() != null) {
                            records.add(outcome.record());
                        }
                        stoppedByPartial = outcome.partial();
                        progressed = true;
                        break;
                    }
                    if (outcome.partial()) {
                        foundMatchButCouldNotInsert = true;
                    }
                }
            }
            if (!progressed || singleSlotOnly || stoppedByPartial) {
                break;
            }
        }
        if (moved > 0) {
            List<InventoryActivityEvent> activityEvents;
            if (recordUndo) {
                activityEvents = recordTakeRecords(player, runtime, records, "take_by_identity");
            } else {
                ArrayList<InventoryActivityEvent> recorded = new ArrayList<>();
                for (TakeAllExecutor.TakeRecord record : records) {
                    InventoryActivityEvent activityEvent =
                            recordAcquisition(runtime, record.identity(), record.count(), "take_by_identity");
                    if (activityEvent != null) {
                        recorded.add(activityEvent);
                    }
                }
                activityEvents = List.copyOf(recorded);
            }
            observeTakeRecords(player, claimedChestMap, records, "slot.take_by_identity");
            String status = one
                    ? "took_one"
                    : singleSlotOnly ? "took_stack" : moved < requested ? "took_partial" : "took_items";
            String diagnostics = "moved=" + moved
                    + (singleSlotOnly ? "" : " requested=" + requested)
                    + (stoppedByPartial ? " carry_full=true" : "");
            return WorkspaceCommandOutcome.accepted(status, diagnostics)
                    .withActivityEvents(activityEvents)
                    .withInvalidations(takeRecordInvalidations(records, "take_by_identity_records"));
        }
        return foundMatchButCouldNotInsert
                ? WorkspaceCommandOutcome.rejected("carry_full")
                : WorkspaceCommandOutcome.accepted("nothing_to_take", "no_matching_proximate_chest")
                        .withInvalidations(List.of(WorkspaceInvalidation.frame(
                                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                                "no_matching_proximate_chest")));
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
        WorkspaceStorageRoutingContext routing =
                WorkspaceStorageRoutingContext.build(player, runtime, InventoryAuthoritySnapshot.empty());
        ClaimedChestMap claimedChestMap = routing.claimedChestMap();
        Set<String> proximate = routing.proximateStorageIds();
        if (proximate.isEmpty()) {
            return null;
        }
        MinecraftServer server = player.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return null;
        }
        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                identity,
                claimedChestMap,
                routing.affinityMap(),
                proximate,
                routing.liveChestContentPresence(),
                routing.liveStorageAffinityEligibility());
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
            ItemStack simulation = world.insert(player, server, target, sourceStack.copy(), true);
            if (simulation == null || simulation.isEmpty()) {
                return chest;
            }
        }
        return null;
    }

    private static ExplicitDepositCandidates explicitDepositCandidates(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            ItemStack sourceStack,
            int requestedCount
    ) {
        if (player == null || runtime == null || identity == null
                || sourceStack == null || sourceStack.isEmpty() || requestedCount <= 0) {
            return ExplicitDepositCandidates.empty();
        }
        MinecraftServer server = player.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return ExplicitDepositCandidates.empty();
        }
        WorkspaceStorageRoutingContext routing =
                WorkspaceStorageRoutingContext.build(player, runtime, InventoryAuthoritySnapshot.empty());
        ClaimedChestMap claimedChestMap = routing.claimedChestMap();
        Set<String> proximate = routing.proximateStorageIds();
        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                identity,
                claimedChestMap,
                routing.affinityMap(),
                proximate,
                routing.liveChestContentPresence(),
                routing.liveStorageAffinityEligibility());

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
            ItemStack leftover = world.insert(player, server, target, probe, true);
            int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
            if (leftoverCount < probeCount) {
                candidates.add(storageId.toString());
            }
        }
        for (WorldDisplayStorageSource source : routing.displaySources()) {
            if (source == null || !source.depositTarget()) {
                continue;
            }
            ItemStack probe = sourceStack.copy();
            probe.setCount(probeCount);
            ItemStack leftover = world.insert(player, server, source.target(), probe, true);
            int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
            if (leftoverCount < probeCount) {
                candidates.add(source.storageId());
            }
        }
        return new ExplicitDepositCandidates(List.copyOf(candidates), routing.displaySources());
    }

    private record ExplicitDepositCandidates(
            List<String> storageIds,
            List<WorldDisplayStorageSource> displaySources
    ) {
        private ExplicitDepositCandidates {
            storageIds = storageIds == null ? List.of() : List.copyOf(storageIds);
            displaySources = displaySources == null ? List.of() : List.copyOf(displaySources);
        }

        private static ExplicitDepositCandidates empty() {
            return new ExplicitDepositCandidates(List.of(), List.of());
        }
    }

    private static DepositPlan withDisplayDepositAssignments(
            ServerPlayer player,
            InventoryAuthoritySnapshot authority,
            DepositPlan basePlan,
            List<WorldDisplayStorageSource> displaySources,
            ToIntFunction<ItemIdentity> reservedCountResolver,
            DepositPlanner.StackProtection stackProtection
    ) {
        DepositPlan resolvedBase = basePlan == null ? DepositPlan.empty() : basePlan;
        if (player == null || player.getServer() == null || authority == null
                || !hasDisplayDepositTarget(displaySources)
                || !StorageAccessRegistry.isInstalled()) {
            return resolvedBase;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        List<InventorySourceDescriptor> declaredCarried = authority.carriedSources();
        List<String> sourceIds = declaredCarried.isEmpty()
                ? List.copyOf(authority.sourcesById().keySet())
                : declaredCarried.stream().map(InventorySourceDescriptor::id).toList();
        LinkedHashMap<ItemIdentity, Integer> budgetByIdentity = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Boolean> fullyProtected = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> assignedBySlot = new LinkedHashMap<>();
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                ItemIdentity budgetIdentity = displayDepositBudgetIdentity(entry.stack());
                budgetByIdentity.merge(budgetIdentity, entry.count(), Integer::sum);
                if (stackProtection != null && stackProtection.protects(entry.stack(), identity)) {
                    fullyProtected.put(budgetIdentity, true);
                }
            }
        }
        for (Map.Entry<ItemIdentity, Integer> entry : new ArrayList<>(budgetByIdentity.entrySet())) {
            int reserved = reservedCountResolver == null
                    ? 0
                    : Math.max(0, reservedCountResolver.applyAsInt(entry.getKey()));
            if (fullyProtected.getOrDefault(entry.getKey(), false)) {
                reserved = Math.max(reserved, entry.getValue());
            }
            budgetByIdentity.put(entry.getKey(), Math.max(0, entry.getValue() - reserved));
        }
        for (DepositPlan.Assignment assignment : resolvedBase.assignments()) {
            InventoryEntrySnapshot entry = authority.slotEntry(assignment.laneId(), assignment.slotIndex());
            if (entry == null || !entry.present()) {
                continue;
            }
            ItemIdentity identity = displayDepositBudgetIdentity(entry.stack());
            int allocated = Math.min(entry.count(), Math.max(0, assignment.count()));
            if (allocated <= 0) {
                continue;
            }
            assignedBySlot.merge(slotKey(assignment.laneId(), assignment.slotIndex()), allocated, Integer::sum);
            budgetByIdentity.computeIfPresent(identity, (ignored, current) -> Math.max(0, current - allocated));
        }
        ArrayList<DepositPlan.Assignment> assignments = new ArrayList<>(resolvedBase.assignments());
        for (String sourceId : sourceIds) {
            for (InventoryEntrySnapshot entry : authority.entries(sourceId)) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                ItemIdentity budgetIdentity = displayDepositBudgetIdentity(entry.stack());
                int remainingBudget = budgetByIdentity.getOrDefault(budgetIdentity, 0);
                if (remainingBudget <= 0) {
                    continue;
                }
                int alreadyAssigned = assignedBySlot.getOrDefault(slotKey(sourceId, entry.slotIndex()), 0);
                int available = Math.max(0, entry.count() - alreadyAssigned);
                int allocated = Math.min(available, remainingBudget);
                if (allocated <= 0) {
                    continue;
                }
                List<String> candidates = displayDepositCandidates(player, world, displaySources, entry.stack(), allocated);
                if (candidates.isEmpty()) {
                    continue;
                }
                assignments.add(new DepositPlan.Assignment(
                        sourceId,
                        entry.slotIndex(),
                        identity.itemId(),
                        allocated,
                        candidates));
                budgetByIdentity.put(budgetIdentity, remainingBudget - allocated);
            }
        }
        return assignments.size() == resolvedBase.assignments().size()
                ? resolvedBase
                : new DepositPlan(assignments);
    }

    private static ItemIdentity displayDepositBudgetIdentity(ItemStack stack) {
        return ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
    }

    private static List<String> displayDepositCandidates(
            ServerPlayer player,
            WorldStorageAccess world,
            List<WorldDisplayStorageSource> displaySources,
            ItemStack sourceStack,
            int requestedCount
    ) {
        if (player == null || player.getServer() == null || world == null
                || displaySources == null || displaySources.isEmpty()
                || sourceStack == null || sourceStack.isEmpty() || requestedCount <= 0) {
            return List.of();
        }
        int probeCount = Math.max(1, Math.min(sourceStack.getMaxStackSize(),
                Math.min(requestedCount, sourceStack.getCount())));
        ArrayList<String> candidates = new ArrayList<>();
        ItemIdentity sourceIdentity = displayDepositBudgetIdentity(sourceStack);
        for (WorldDisplayStorageSource source : displaySources) {
            if (source == null || !source.depositTarget()) {
                continue;
            }
            if (!WorldDisplayDepositRouting.containsMatchingContent(source, sourceIdentity)) {
                continue;
            }
            ItemStack probe = sourceStack.copy();
            probe.setCount(probeCount);
            ItemStack leftover = world.insert(player, player.getServer(), source.target(), probe, true);
            int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
            if (leftoverCount < probeCount) {
                candidates.add(source.storageId());
            }
        }
        return candidates.isEmpty() ? List.of() : List.copyOf(candidates);
    }

    private static boolean hasDisplayDepositTarget(List<WorldDisplayStorageSource> displaySources) {
        if (displaySources == null || displaySources.isEmpty()) {
            return false;
        }
        for (WorldDisplayStorageSource source : displaySources) {
            if (source != null && source.depositTarget()) {
                return true;
            }
        }
        return false;
    }

    private static void observeTakeRecords(
            ServerPlayer player,
            ClaimedChestMap claimedChestMap,
            List<TakeAllExecutor.TakeRecord> records,
            String source
    ) {
        if (records == null || records.isEmpty()) {
            return;
        }
        LinkedHashSet<String> storageIds = new LinkedHashSet<>();
        for (TakeAllExecutor.TakeRecord record : records) {
            if (record != null && record.count() > 0 && !record.storageId().isBlank()) {
                storageIds.add(record.storageId());
            }
        }
        observeStorageIds(player, claimedChestMap, storageIds, source);
    }

    private static void observeTakeRecord(
            ServerPlayer player,
            ClaimedChestMap claimedChestMap,
            TakeAllExecutor.TakeRecord record,
            String source
    ) {
        if (record == null || record.count() <= 0 || record.storageId().isBlank()) {
            return;
        }
        observeStorageIds(player, claimedChestMap, List.of(record.storageId()), source);
    }

    static void observeStorageIds(
            ServerPlayer player,
            ClaimedChestMap claimedChestMap,
            Collection<String> storageIds,
            String source
    ) {
        if (player == null || player.getServer() == null || !StorageAccessRegistry.isInstalled()
                || storageIds == null || storageIds.isEmpty()) {
            return;
        }
        WorkspaceStorageMemoryStore.observeStorageIds(
                player.getServer(),
                StorageAccessRegistry.worldStorageAccess(),
                claimedChestMap,
                storageIds,
                player.serverLevel().getGameTime(),
                source);
    }

    private static String slotKey(String sourceId, int slotIndex) {
        return (sourceId == null ? "" : sourceId) + "\u0000" + slotIndex;
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
        if (!fallbackChest.role().visibleToWorkspace()) {
            return List.of();
        }
        if (!WorkspaceChestProjectionSupport.isProximate(player, fallbackChest)) {
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
        ItemStack leftover = world.insert(player, server, target, probe, true);
        int leftoverCount = leftover == null || leftover.isEmpty() ? 0 : leftover.getCount();
        return leftoverCount < probeCount ? List.of(fallbackChest.storageId().toString()) : List.of();
    }

    static int requestedExplicitDepositCount(
            int carriedTotal,
            int reservedCount,
            DepositQuantity quantity,
            DesiredCountPolicy desiredCountPolicy
    ) {
        return requestedExplicitDepositCount(carriedTotal, reservedCount, quantity, desiredCountPolicy, null);
    }

    static int requestedExplicitDepositCount(
            int carriedTotal,
            int reservedCount,
            DepositQuantity quantity,
            DesiredCountPolicy desiredCountPolicy,
            Integer requestedCountOverride
    ) {
        int carried = Math.max(0, carriedTotal);
        int reserved = desiredCountPolicy == DesiredCountPolicy.IGNORE ? 0 : Math.max(0, reservedCount);
        int depositable = Math.max(0, carried - reserved);
        if (requestedCountOverride != null) {
            return Math.min(Math.max(0, requestedCountOverride), depositable);
        }
        return switch (quantity == null ? DepositQuantity.STACK : quantity) {
            case STACK -> depositable;
            case ITEM -> Math.min(1, depositable);
        };
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
            UUID storageUuid = outcome.record().storageUuid();
            if (storageUuid != null) {
                runtime.chestClaimWorkflow().recordDeposit(
                        storageUuid,
                        outcome.record().identity(),
                        outcome.record().count(),
                        player.serverLevel().getGameTime());
                observeStorageIds(
                        player,
                        runtime.chestClaimWorkflow().claimedChestMap(),
                        List.of(storageUuid.toString()),
                        "slot.deposit.single");
            }
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
        return WorkspaceCommandOutcome.accepted("deposited_stack", "deposited to " + chestLabel(chest))
                .withInvalidations(depositRecordInvalidations(
                        depositRecordList(outcome.record()),
                        "deposit_single_record"));
    }

    static List<WorkspaceInvalidation> depositRecordInvalidations(
            List<DepositExecutor.DepositRecord> records,
            String diagnostics
    ) {
        if (records == null || records.isEmpty()) {
            return List.of(chestTransferInvalidation(Set.of(), Set.of(), missingRecordDiagnostics(diagnostics)));
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        LinkedHashSet<String> storageIds = new LinkedHashSet<>();
        for (DepositExecutor.DepositRecord record : records) {
            if (record == null || record.count() <= 0) {
                continue;
            }
            ItemIdentityCollections.add(identities, record.identity());
            if (!record.storageId().isBlank()) {
                storageIds.add(record.storageId());
            }
        }
        return List.of(chestTransferInvalidation(identities, storageIds, diagnostics));
    }

    static List<WorkspaceInvalidation> takeRecordInvalidations(
            List<TakeAllExecutor.TakeRecord> records,
            String diagnostics
    ) {
        if (records == null || records.isEmpty()) {
            return List.of(chestTransferInvalidation(Set.of(), Set.of(), missingRecordDiagnostics(diagnostics)));
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        LinkedHashSet<String> storageIds = new LinkedHashSet<>();
        for (TakeAllExecutor.TakeRecord record : records) {
            if (record == null || record.count() <= 0) {
                continue;
            }
            ItemIdentityCollections.add(identities, record.identity());
            if (!record.storageId().isBlank()) {
                storageIds.add(record.storageId());
            }
        }
        return List.of(chestTransferInvalidation(identities, storageIds, diagnostics));
    }

    private static WorkspaceInvalidation chestTransferInvalidation(
            Collection<ItemIdentity> identities,
            Collection<String> storageIds,
            String diagnostics
    ) {
        Set<ItemIdentity> resolvedIdentities = identities == null || identities.isEmpty()
                ? Set.of()
                : Set.copyOf(identities);
        Set<String> resolvedStorageIds = storageIds == null || storageIds.isEmpty()
                ? Set.of()
                : Set.copyOf(storageIds);
        boolean hasRecords = !resolvedIdentities.isEmpty() && !resolvedStorageIds.isEmpty();
        return new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                resolvedIdentities,
                resolvedStorageIds,
                Set.of(),
                hasRecords ? chestTransferSlices() : WorkspaceProjectionSlice.all(),
                !hasRecords,
                hasRecords ? diagnostics : missingRecordDiagnostics(diagnostics));
    }

    private static EnumSet<WorkspaceProjectionSlice> chestTransferSlices() {
        return EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.STORAGE,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.DEPOSITABILITY,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.HOTBAR,
                WorkspaceProjectionSlice.FRAME,
                WorkspaceProjectionSlice.REMOTE_SEARCH);
    }

    private static String missingRecordDiagnostics(String diagnostics) {
        if (diagnostics == null || diagnostics.isBlank()) {
            return "missing_chest_transfer_records";
        }
        return diagnostics.endsWith("_missing_chest_transfer_records")
                ? diagnostics
                : diagnostics + "_missing_chest_transfer_records";
    }

    private static List<DepositExecutor.DepositRecord> depositRecordList(DepositExecutor.DepositRecord record) {
        return record == null ? List.of() : List.of(record);
    }

    private static List<TakeAllExecutor.TakeRecord> takeRecordList(TakeAllExecutor.TakeRecord record) {
        return record == null ? List.of() : List.of(record);
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
                        String storageId = record.storageId();
                        if (storageId == null || storageId.isBlank()) {
                            continue;
                        }
                        WorkspaceChestTransferReverser.pullFromStorageToCarry(
                                player, ctx.runtime(), storageId, record.identity(), record.count());
                    }
                },
                ctx -> {
                    for (DepositExecutor.DepositRecord record : captured) {
                        String storageId = record.storageId();
                        if (storageId == null || storageId.isBlank()) {
                            continue;
                        }
                        WorkspaceChestTransferReverser.pushFromCarryToStorage(
                                player, ctx.runtime(), storageId, record.identity(), record.count());
                    }
                }
        );
    }

    static List<InventoryActivityEvent> recordTakeRecords(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            List<TakeAllExecutor.TakeRecord> records,
            String diagnostics
    ) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        ArrayList<InventoryActivityEvent> activityEvents = new ArrayList<>();
        for (TakeAllExecutor.TakeRecord record : records) {
            InventoryActivityEvent activityEvent = recordTakeRecord(player, runtime, record, diagnostics);
            if (activityEvent != null) {
                activityEvents.add(activityEvent);
            }
        }
        return List.copyOf(activityEvents);
    }

    static InventoryActivityEvent recordTakeRecord(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            TakeAllExecutor.TakeRecord record,
            String diagnostics
    ) {
        if (record == null) {
            return null;
        }
        InventoryActivityEvent activityEvent = recordAcquisition(runtime, record.identity(), record.count(), diagnostics);
        recordChestTransferUndo(
                player, runtime, record.storageId(), record.identity(), record.count(),
                ChestTransferDirection.TAKE);
        return activityEvent;
    }

    private static InventoryActivityEvent recordAcquisition(
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int count,
            String diagnostics
    ) {
        InventoryActivityEvent event = InventoryAcquisitionActivityRecorder.acquiredEvent(
                identity,
                count,
                InventoryActivityProducer.EXTERNAL_WITHDRAWAL,
                InventoryActivityConfidence.AUTHORITATIVE,
                diagnostics);
        if (runtime == null || event == null) {
            return null;
        }
        return runtime.recordActivityEvent(event) ? event : null;
    }

    private static List<InventoryActivityEvent> activityEvents(InventoryActivityEvent event) {
        return event == null ? List.of() : List.of(event);
    }

    private static void recordChestTransferUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            String storageId,
            ItemIdentity identity,
            int count,
            ChestTransferDirection direction
    ) {
        if (player == null || runtime == null || storageId == null || identity == null
                || count <= 0 || direction == null) {
            return;
        }
        UUID storageUuid = parseUuid(storageId);
        if (storageUuid == null) {
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
                                player, ctx.runtime(), storageUuid, identity, count);
                    } else {
                        WorkspaceChestTransferReverser.pushFromCarryToChest(
                                player, ctx.runtime(), storageUuid, identity, count);
                    }
                },
                ctx -> {
                    if (direction == ChestTransferDirection.DEPOSIT) {
                        WorkspaceChestTransferReverser.pushFromCarryToChest(
                                player, ctx.runtime(), storageUuid, identity, count);
                    } else {
                        WorkspaceChestTransferReverser.pullFromChestToCarry(
                                player, ctx.runtime(), storageUuid, identity, count);
                    }
                }
        );
    }

    private static ToIntFunction<ItemIdentity> reservedCountResolver(WorkflowDomainRuntime runtime) {
        WorkflowTabTargets.Resolution targets = runtime == null
                ? WorkflowTabTargets.Resolution.empty()
                : WorkflowTabTargets.resolve(InventoryAuthoritySnapshot.empty(), runtime.snapshot());
        return identity -> {
            if (identity == null) {
                return 0;
            }
            return SlotWorkspaceViewModel.reservedCarryCount(identity, targets);
        };
    }

    private static DepositPlanner.StackProtection acceptedInputProtection(WorkflowDomainRuntime runtime) {
        if (runtime == null) {
            return null;
        }
        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());
        if (targets.acceptedInputs().isEmpty()) {
            return null;
        }
        return (stack, identity) -> targets.acceptedInput(identity, ItemStackTags.itemTagIds(stack));
    }

    private static DepositPlanner.StackProtection activeWorkflowPutAwayProtection(WorkflowDomainRuntime runtime) {
        WorkflowDomainSnapshot snapshot = runtime == null ? null : runtime.snapshot();
        if (snapshot == null || snapshot.kitMap() == null) {
            return null;
        }
        KitActivation activation = snapshot.kitMap().activation();
        if (activation == null || !activation.isActive()) {
            return null;
        }
        Set<ItemIdentity> putAwayIdentities = activation.putAwayIdentities();
        return (stack, identity) -> !ItemIdentityCollections.contains(putAwayIdentities, identity);
    }

    private static DepositPlanner.StackProtection combineStackProtection(
            DepositPlanner.StackProtection first,
            DepositPlanner.StackProtection second
    ) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return (stack, identity) -> first.protects(stack, identity) || second.protects(stack, identity);
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
