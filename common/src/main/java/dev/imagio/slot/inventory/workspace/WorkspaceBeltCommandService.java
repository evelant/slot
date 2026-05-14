package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Shared belt / hotbar command semantics. Platform sessions provide the live
 * player, authority snapshot, and action executor; this service owns the
 * identity lookup, occupant staging, and transfer request shape.
 */
public final class WorkspaceBeltCommandService {
    public static final String CARRIED_IDENTITY_NOT_FOUND = "identity not found in any carried source";

    private WorkspaceBeltCommandService() {
    }

    @FunctionalInterface
    public interface TransferExecutor {
        WorkspaceTransferExecution execute(
                InventoryActionTarget source,
                InventoryActionTarget destination,
                String origin);
    }

    @FunctionalInterface
    public interface ViewModelRefresher {
        SlotWorkspaceViewModel refresh();
    }

    public static WorkspaceCommandOutcome returnHotbarToHome(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            Integer hotbarIndex,
            TransferExecutor transferExecutor,
            String originPrefix
    ) {
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_slot");
        }
        SlotWorkspaceViewModel.HotbarSlot slot = hotbarSlot(viewModel, index);
        if (slot == null) {
            return WorkspaceCommandOutcome.rejected("hotbar_not_projected");
        }
        if (!slot.occupied()) {
            return WorkspaceCommandOutcome.rejected("hotbar_slot_empty");
        }
        if (player == null || runtime == null || transferExecutor == null) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_return_context");
        }

        ItemIdentity identity = ItemIdentityMatcher.create(slot.displayStack());
        boolean homed = runtime.snapshot().visualHomeMap().assignment(identity) != null;
        ItemStack hotbarBefore = WorkspaceHotbarSlotReverser.peekSlot(player, index);
        ClaimedChest depositTarget = WorkspaceChestCommandService.resolveProximateLinkedChestForIdentity(
                player,
                runtime,
                identity,
                slot.displayStack());
        if (depositTarget != null) {
            return WorkspaceChestCommandService.depositHotbarToChest(
                    player,
                    runtime,
                    index,
                    depositTarget.storageId().toString());
        }

        WorkspaceTransferExecution execution = transferExecutor.execute(
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, index),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                origin(originPrefix, "return_hotbar_to_home"));
        if (execution.appliedCompletely()) {
            ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(player, index);
            recordHotbarSlotUndo(
                    player,
                    runtime,
                    index,
                    hotbarBefore,
                    hotbarAfter,
                    "return hotbar " + (index + 1) + " to inventory");
            return new WorkspaceCommandOutcome(
                    true,
                    homed ? "returned_to_home" : "returned_unhomed",
                    homed ? "returned to its home" : "returned to inventory");
        }
        String feedbackDiagnostics = execution.feedback().diagnostics();
        boolean fullDestination = "destination_full_or_incompatible".equals(feedbackDiagnostics);
        return new WorkspaceCommandOutcome(
                false,
                fullDestination ? "no_free_main_slot" : execution.feedback().status(),
                fullDestination ? "no free main inventory slot" : feedbackDiagnostics);
    }

    public static WorkspaceCommandOutcome assignHomeToFreeHotbar(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            ItemIdentity identity,
            boolean suppressChestPreference,
            IntFunction<WorkspaceCommandOutcome> hotbarAssigner
    ) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (hotbarAssigner == null) {
            return WorkspaceCommandOutcome.rejected("hotbar_assigner_unavailable");
        }
        if (!suppressChestPreference) {
            WorkspaceCommandOutcome depositOutcome = WorkspaceChestCommandService.depositIdentityToLinkedChest(
                    player,
                    runtime,
                    identity,
                    WorkspaceChestCommandService.DepositQuantity.STACK,
                    WorkspaceChestCommandService.DesiredCountPolicy.RESPECT);
            if (depositOutcome.success() && "deposited_stack".equals(depositOutcome.status())) {
                return depositOutcome;
            }
            if (!isHotbarFallbackDepositMiss(depositOutcome)) {
                return depositOutcome;
            }
        }

        int targetHotbarIndex = firstPartialOrFreeHotbarSlot(viewModel, identity);
        if (targetHotbarIndex < 0) {
            return new WorkspaceCommandOutcome(
                    false,
                    "no_free_hotbar_slot",
                    "all hotbar slots are occupied");
        }
        return hotbarAssigner.apply(targetHotbarIndex);
    }

    public static WorkspaceCommandOutcome assignIdentityToAutoHotbar(
            SlotWorkspaceViewModel viewModel,
            ItemIdentity identity,
            Map<Integer, Long> hotbarPlacementSequence,
            IntFunction<WorkspaceCommandOutcome> hotbarAssigner
    ) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (hotbarAssigner == null) {
            return WorkspaceCommandOutcome.rejected("hotbar_assigner_unavailable");
        }
        int targetHotbarIndex = firstPartialFreeOrOldestHotbarSlot(
                viewModel,
                identity,
                hotbarPlacementSequence);
        if (targetHotbarIndex < 0) {
            return new WorkspaceCommandOutcome(
                    false,
                    "hotbar_not_projected",
                    "hotbar slots are unavailable");
        }
        return hotbarAssigner.apply(targetHotbarIndex);
    }

    public static boolean isHotbarFallbackDepositMiss(WorkspaceCommandOutcome outcome) {
        if (outcome == null || outcome.success()) {
            return false;
        }
        return "nothing_to_deposit".equals(outcome.diagnostics())
                || "desired_count_reserved".equals(outcome.diagnostics())
                || "no_linked_proximate_chest_with_room".equals(outcome.diagnostics());
    }

    public static WorkspaceCommandOutcome moveHotbarToAtlas(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            Integer hotbarIndex,
            String islandId,
            Integer ordinal,
            TransferExecutor transferExecutor,
            ViewModelRefresher viewModelRefresher,
            String originPrefix
    ) {
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9 || islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_drop");
        }
        SlotWorkspaceViewModel.HotbarSlot slot = hotbarSlot(viewModel, index);
        if (slot == null) {
            return WorkspaceCommandOutcome.rejected("hotbar_not_projected");
        }
        if (!slot.occupied()) {
            return WorkspaceCommandOutcome.rejected("hotbar_slot_empty");
        }
        if (viewModel == null || viewModel.island(islandId) == null) {
            return WorkspaceCommandOutcome.rejected("unknown_island");
        }
        if (player == null || runtime == null || transferExecutor == null) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_drop_context");
        }

        ItemIdentity identity = ItemIdentityMatcher.create(slot.displayStack());
        ItemStack hotbarBefore = WorkspaceHotbarSlotReverser.peekSlot(player, index);
        VisualHomeAssignment homeBefore = runtime.snapshot().visualHomeMap().assignment(identity);
        WorkspaceTransferExecution execution = transferExecutor.execute(
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, index),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                origin(originPrefix, "drag.hotbar_to_atlas"));
        if (!execution.appliedCompletely()) {
            return new WorkspaceCommandOutcome(
                    false,
                    execution.feedback().status(),
                    execution.feedback().diagnostics());
        }

        SlotWorkspaceViewModel refreshedView = viewModelRefresher == null ? viewModel : viewModelRefresher.refresh();
        WorkspaceCommandOutcome dropOutcome = SlotWorkspaceCommandService.applyHomeDrop(
                runtime,
                refreshedView == null ? viewModel : refreshedView,
                learnedRules,
                signalExtractor,
                identity,
                islandId,
                ordinal,
                origin(originPrefix, "drag.hotbar_home"));
        if (dropOutcome.success()) {
            VisualHomeAssignment homeAfter = runtime.snapshot().visualHomeMap().assignment(identity);
            ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(player, index);
            recordHotbarAndHomeUndo(
                    player,
                    runtime,
                    index,
                    identity,
                    hotbarBefore,
                    hotbarAfter,
                    homeBefore,
                    homeAfter,
                    "drag hotbar " + (index + 1) + " to atlas");
        }
        return dropOutcome;
    }

    public static int firstPartialOrFreeHotbarSlot(
            SlotWorkspaceViewModel viewModel,
            ItemIdentity identity
    ) {
        if (viewModel == null || identity == null) {
            return -1;
        }
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (!slot.occupied()) {
                continue;
            }
            ItemStack stack = slot.displayStack();
            if (stack == null || stack.isEmpty() || slot.count() >= stack.getMaxStackSize()) {
                continue;
            }
            if (ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return slot.hotbarIndex();
            }
        }
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (!slot.occupied()) {
                return slot.hotbarIndex();
            }
        }
        return -1;
    }

    public static int firstPartialFreeOrOldestHotbarSlot(
            SlotWorkspaceViewModel viewModel,
            ItemIdentity identity,
            Map<Integer, Long> hotbarPlacementSequence
    ) {
        int partialOrFree = firstPartialOrFreeHotbarSlot(viewModel, identity);
        if (partialOrFree >= 0) {
            return partialOrFree;
        }
        if (viewModel == null || identity == null) {
            return -1;
        }
        int selectedAnyIndex = -1;
        long selectedAnySequence = Long.MAX_VALUE;
        int selectedEvictionIndex = -1;
        long selectedEvictionSequence = Long.MAX_VALUE;
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (slot == null || !slot.occupied()) {
                continue;
            }
            ItemStack stack = slot.displayStack();
            long sequence = hotbarPlacementSequence == null
                    ? Long.MAX_VALUE
                    : hotbarPlacementSequence.getOrDefault(slot.hotbarIndex(), Long.MAX_VALUE);
            if (selectedAnyIndex < 0
                    || sequence < selectedAnySequence
                    || (sequence == selectedAnySequence && slot.hotbarIndex() < selectedAnyIndex)) {
                selectedAnyIndex = slot.hotbarIndex();
                selectedAnySequence = sequence;
            }
            if (stack != null && !stack.isEmpty() && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                continue;
            }
            if (selectedEvictionIndex < 0
                    || sequence < selectedEvictionSequence
                    || (sequence == selectedEvictionSequence && slot.hotbarIndex() < selectedEvictionIndex)) {
                selectedEvictionIndex = slot.hotbarIndex();
                selectedEvictionSequence = sequence;
            }
        }
        if (selectedEvictionIndex >= 0) {
            return selectedEvictionIndex;
        }
        return selectedAnyIndex;
    }

    public static WorkspaceCommandOutcome assignIdentityToHotbarByTransfer(
            ServerPlayer player,
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority,
            CarriedSourceAccess carried,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            ItemIdentity identity,
            int hotbarIndex,
            String originPrefix
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        if (host == null || authority == null || actionExecutor == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        if (carried == null) {
            return WorkspaceCommandOutcome.rejected("carried_source_access_unavailable");
        }
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_slot");
        }

        LocatedCarriedEntry located = findIdentityInAuthority(authority, identity)
                .orElseGet(() -> carried.findIdentity(player, identity)
                        .map(location -> new LocatedCarriedEntry(location.sourceId(), location.slotIndex(), null))
                        .orElse(null));
        if (located == null) {
            return WorkspaceCommandOutcome.rejected(CARRIED_IDENTITY_NOT_FOUND);
        }
        String sourceId = located.sourceId();
        int slotIndex = located.slotIndex();
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId) && slotIndex == hotbarIndex) {
            return WorkspaceCommandOutcome.accepted(
                    "assigned_to_hotbar_" + (hotbarIndex + 1),
                    "already in hotbar " + (hotbarIndex + 1));
        }

        InventoryActionTarget sourceTarget = sourceTarget(host, sourceId, slotIndex);
        InventoryActionTarget hotbarTarget = new InventoryActionTarget.QuickAccessTarget(
                BuiltinInventoryIds.QUICK_ACCESS_LANE_0,
                hotbarIndex);
        InventoryEntrySnapshot sourceEntry = located.entry();
        if (sourceEntry == null || !sourceEntry.present()) {
            sourceEntry = sourceEntry(authority, carried, player, sourceId, slotIndex, sourceTarget);
        }
        if (sourceEntry == null || !sourceEntry.present()) {
            return WorkspaceCommandOutcome.rejected("source_stack_missing");
        }
        ItemStack sourceStack = sourceEntry.stack().copy();
        sourceStack.setCount(sourceEntry.count());
        ItemIdentity sourceIdentity = ItemIdentityMatcher.create(sourceStack);

        Integer stagingSlot = null;
        InventoryEntrySnapshot targetEntry = InventoryAuthorityReadService.entrySnapshot(authority, hotbarTarget);
        if (targetEntry != null && targetEntry.present()) {
            ItemIdentity targetIdentity = ItemIdentityMatcher.create(targetEntry.stack());
            boolean sameIdentity = ItemIdentityMatcher.matchesMovable(targetIdentity, sourceIdentity);
            boolean canMergeMore = sameIdentity && targetEntry.count() < targetEntry.stack().getMaxStackSize();
            if (!canMergeMore) {
                if (sameIdentity) {
                    return WorkspaceCommandOutcome.accepted(
                            "assigned_to_hotbar_" + (hotbarIndex + 1),
                            "already in hotbar " + (hotbarIndex + 1));
                }
                if (canAssignDirectly(host, sourceId)) {
                    WorkspaceTransferExecution directAssign = executeAssignRequest(
                            host,
                            actionExecutor,
                            sourceTarget,
                            hotbarTarget,
                            origin(originPrefix, "assign_identity_to_hotbar_swap"),
                            sourceIdentity,
                            sourceStack,
                            sourceEntry.count());
                    if (directAssign.appliedCompletely()) {
                        return new WorkspaceCommandOutcome(
                                true,
                                "assigned_to_hotbar_" + (hotbarIndex + 1),
                                "moved to hotbar " + (hotbarIndex + 1));
                    }
                    return WorkspaceCommandOutcome.rejected(directAssign.feedback().diagnostics());
                }
                stagingSlot = firstEmptyMainSlot(authority);
                if (stagingSlot == null) {
                    return WorkspaceCommandOutcome.rejected("no free main inventory slot");
                }
                ItemStack targetStack = targetEntry.stack().copy();
                targetStack.setCount(targetEntry.count());
                WorkspaceTransferExecution stage = executeTransferRequest(
                        host,
                        actionExecutor,
                        hotbarTarget,
                        new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, stagingSlot),
                        origin(originPrefix, "stage_hotbar_assignment"),
                        targetIdentity,
                        targetStack,
                        targetEntry.count());
                if (!stage.appliedCompletely()) {
                    return WorkspaceCommandOutcome.rejected(stage.feedback().diagnostics());
                }
            }
        }

        WorkspaceTransferExecution execution = executeTransferRequest(
                host,
                actionExecutor,
                sourceTarget,
                hotbarTarget,
                origin(originPrefix, "assign_identity_to_hotbar"),
                sourceIdentity,
                sourceStack,
                sourceEntry.count());
        if (execution.appliedCompletely()) {
            return new WorkspaceCommandOutcome(
                    true,
                    "assigned_to_hotbar_" + (hotbarIndex + 1),
                    "moved to hotbar " + (hotbarIndex + 1));
        }
        if (stagingSlot != null) {
            executeTransferRequest(
                    host,
                    actionExecutor,
                    new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, stagingSlot),
                    hotbarTarget,
                    origin(originPrefix, "stage_hotbar_assignment_rollback"),
                    targetEntry == null ? null : ItemIdentityMatcher.create(targetEntry.stack()),
                    targetEntry == null ? ItemStack.EMPTY : targetEntry.stack(),
                    targetEntry == null ? 0 : targetEntry.count());
        }
        return WorkspaceCommandOutcome.rejected(execution.feedback().diagnostics());
    }

    static Optional<LocatedCarriedEntry> findIdentityInAuthority(
            InventoryAuthoritySnapshot authority,
            ItemIdentity identity
    ) {
        if (authority == null || identity == null) {
            return Optional.empty();
        }
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present() || !entry.slotBacked()) {
                    continue;
                }
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    return Optional.of(new LocatedCarriedEntry(source.id(), entry.slotIndex(), entry));
                }
            }
        }
        return findUniqueStackableItemIdInAuthority(authority, identity);
    }

    private static Optional<LocatedCarriedEntry> findUniqueStackableItemIdInAuthority(
            InventoryAuthoritySnapshot authority,
            ItemIdentity identity
    ) {
        if (authority == null || identity == null) {
            return Optional.empty();
        }
        LocatedCarriedEntry selected = null;
        ItemIdentity liveVariant = null;
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present() || !entry.slotBacked()) {
                    continue;
                }
                ItemStack stack = entry.stack();
                if (stack == null || stack.isEmpty() || stack.getMaxStackSize() <= 1) {
                    continue;
                }
                ItemIdentity candidate = ItemIdentityMatcher.create(stack);
                if (!candidate.itemId().equals(identity.itemId())) {
                    continue;
                }
                if (liveVariant == null) {
                    liveVariant = candidate;
                    selected = new LocatedCarriedEntry(source.id(), entry.slotIndex(), entry);
                    continue;
                }
                if (!liveVariant.equals(candidate)) {
                    return Optional.empty();
                }
            }
        }
        return Optional.ofNullable(selected);
    }

    record LocatedCarriedEntry(String sourceId, int slotIndex, InventoryEntrySnapshot entry) {
        LocatedCarriedEntry {
            sourceId = sourceId == null ? "" : sourceId;
        }
    }

    private static InventoryEntrySnapshot sourceEntry(
            InventoryAuthoritySnapshot authority,
            CarriedSourceAccess carried,
            ServerPlayer player,
            String sourceId,
            int slotIndex,
            InventoryActionTarget sourceTarget
    ) {
        InventoryEntrySnapshot entry = InventoryAuthorityReadService.entrySnapshot(authority, sourceTarget);
        if (entry != null && entry.present()) {
            return entry;
        }
        ItemStack peeked = carried.peek(player, sourceId, slotIndex);
        if (peeked == null || peeked.isEmpty()) {
            return null;
        }
        return new InventoryEntrySnapshot(
                InventoryEntryKey.slot(sourceId, slotIndex),
                peeked,
                peeked.getCount(),
                "");
    }

    private static WorkspaceTransferExecution executeTransferRequest(
            InventoryHostDescriptor host,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin,
            ItemIdentity identity,
            ItemStack stack,
            int requestedCount
    ) {
        String requestId = UUID.randomUUID().toString();
        ItemStack requestStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!requestStack.isEmpty() && requestedCount > 0) {
            requestStack.setCount(requestedCount);
        }
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                requestId,
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                origin,
                source,
                destination,
                Math.max(0, requestedCount),
                identity,
                requestStack,
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );
        InventoryActionOutcome outcome = actionExecutor.apply(request);
        WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);
        return new WorkspaceTransferExecution(host, request, outcome, feedback);
    }

    private static WorkspaceTransferExecution executeAssignRequest(
            InventoryHostDescriptor host,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin,
            ItemIdentity identity,
            ItemStack stack,
            int requestedCount
    ) {
        String requestId = UUID.randomUUID().toString();
        ItemStack requestStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!requestStack.isEmpty() && requestedCount > 0) {
            requestStack.setCount(requestedCount);
        }
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                requestId,
                InventoryActionKind.ASSIGN,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE,
                origin,
                source,
                destination,
                Math.max(0, requestedCount),
                identity,
                requestStack,
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );
        InventoryActionOutcome outcome = actionExecutor.apply(request);
        WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);
        return new WorkspaceTransferExecution(host, request, outcome, feedback);
    }

    private static boolean canAssignDirectly(InventoryHostDescriptor host, String sourceId) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return false;
        }
        InventorySourceDescriptor source = host.source(sourceId);
        return source != null && source.bindingRoute() == InventoryBindingRoute.PLAYER;
    }

    private static Integer firstEmptyMainSlot(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return null;
        }
        int capacity = authority.slotCapacity(BuiltinInventoryIds.PLAYER_MAIN);
        for (int index = 0; index < capacity; index++) {
            InventoryEntrySnapshot entry = authority.slotEntry(BuiltinInventoryIds.PLAYER_MAIN, index);
            if (entry == null || !entry.present()) {
                return index;
            }
        }
        return null;
    }

    private static InventoryActionTarget sourceTarget(InventoryHostDescriptor host, String sourceId, int slotIndex) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex);
        }
        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null) {
            return new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex);
        }
        return switch (source.role()) {
            case QUICK_ACCESS -> new InventoryActionTarget.QuickAccessTarget(source.laneId(), slotIndex);
            case EQUIPMENT, OFFHAND -> new InventoryActionTarget.EquipmentTarget(source.groupId(), slotIndex);
            default -> new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex);
        };
    }

    private static String origin(String prefix, String suffix) {
        String resolved = prefix == null || prefix.isBlank() ? "slot_workspace.common" : prefix;
        return resolved + "." + suffix;
    }

    private static SlotWorkspaceViewModel.HotbarSlot hotbarSlot(SlotWorkspaceViewModel viewModel, int hotbarIndex) {
        if (viewModel == null || hotbarIndex < 0 || hotbarIndex >= viewModel.hotbarSlots().size()) {
            return null;
        }
        return viewModel.hotbarSlots().get(hotbarIndex);
    }

    public static void recordHotbarSlotUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            int hotbarIndex,
            ItemStack before,
            ItemStack after,
            String label
    ) {
        if (player == null || runtime == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        ItemStack beforeCopy = before == null ? ItemStack.EMPTY : before.copy();
        ItemStack afterCopy = after == null ? ItemStack.EMPTY : after.copy();
        if (ItemStack.matches(beforeCopy, afterCopy)) {
            return;
        }
        runtime.undoStack().record(
                label,
                ctx -> WorkspaceHotbarSlotReverser.restoreSlot(player, hotbarIndex, beforeCopy),
                ctx -> WorkspaceHotbarSlotReverser.restoreSlot(player, hotbarIndex, afterCopy)
        );
    }

    private static void recordHotbarAndHomeUndo(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            int hotbarIndex,
            ItemIdentity identity,
            ItemStack hotbarBefore,
            ItemStack hotbarAfter,
            VisualHomeAssignment homeBefore,
            VisualHomeAssignment homeAfter,
            String label
    ) {
        if (player == null || runtime == null || identity == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        ItemStack beforeCopy = hotbarBefore == null ? ItemStack.EMPTY : hotbarBefore.copy();
        ItemStack afterCopy = hotbarAfter == null ? ItemStack.EMPTY : hotbarAfter.copy();
        runtime.undoStack().record(
                label,
                ctx -> {
                    SlotWorkspaceCommandService.restoreHomeAssignment(ctx.runtime(), identity, homeBefore);
                    WorkspaceHotbarSlotReverser.restoreSlot(player, hotbarIndex, beforeCopy);
                },
                ctx -> {
                    WorkspaceHotbarSlotReverser.restoreSlot(player, hotbarIndex, afterCopy);
                    SlotWorkspaceCommandService.restoreHomeAssignment(ctx.runtime(), identity, homeAfter);
                }
        );
    }
}
