package dev.imagio.slot.forge.network;

import dev.imagio.slot.forge.Forge120GhostStackFactory;
import dev.imagio.slot.forge.storage.ForgeChestDepositObserver;
import dev.imagio.slot.forge.storage.ForgeChestStorageAnchors;
import dev.imagio.slot.forge.storage.ForgeChestStorageIds;
import dev.imagio.slot.forge.triage.Forge120IslandSignalExtractor;
import dev.imagio.slot.forge.storage.ForgeCarriedActivityTracker;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.ActiveChestPanelProjectionSupport;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.KitGatherService;
import dev.imagio.slot.inventory.workspace.KitPageCycleService;
import dev.imagio.slot.inventory.workspace.LootChestProjectionSupport;
import dev.imagio.slot.inventory.workspace.WorkspaceBeltCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestProjectionSupport;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceCursorCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionPacket;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.ui.action.WorkspaceActionSessionContext;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

final class ForgeWorkspaceSession {
    private static final int TARGET_MAIN_SOURCE = 1;
    private static final int TARGET_MAIN_SLOT = 2;
    private static final int TARGET_HOTBAR_SLOT = 3;
    private static final int AUTO_HOME_MAX_PER_PROJECTION = 64;
    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    private final WorkflowDomainRuntime runtime;
    private final LearnedIslandRuleStore learnedRules = new LearnedIslandRuleStore();
    private final Set<ItemIdentity> autoHomeAttempted = new HashSet<>();
    private WorkspaceActionSessionContext context;
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private WorkspaceCursorCommandService.CursorOrigin cursorOrigin;
    private long nextRevision = 1L;
    private String status = "ready";
    private String diagnostics = "";
    private String searchQuery = "";

    ForgeWorkspaceSession(WorkspaceActionEnvelope envelope, int menuContainerId, WorkflowDomainRuntime runtime) {
        this.context = new WorkspaceActionSessionContext(envelope.sessionId(), menuContainerId, envelope.viewRevision());
        this.runtime = runtime;
    }

    WorkspaceActionSessionContext context() {
        return context;
    }

    SlotWorkspaceViewModel project(ServerPlayer player) {
        return project(player, true);
    }

    SlotWorkspaceViewModel project(ServerPlayer player, boolean forceRevision) {
        InventoryHostDescriptor host = resolveHost(player);
        InventoryAuthoritySnapshot authority = host == null || player == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(player, host);
        String hostDiagnostics = host == null ? "host_resolution_failed" : "";
        String combinedDiagnostics = combineDiagnostics(hostDiagnostics, diagnostics);
        int selected = player == null ? -1 : player.getInventory().selected;
        long gameTime = player == null ? 0L : player.serverLevel().getGameTime();

        SlotWorkspaceViewModel projected = project(authority, selected, combinedDiagnostics, gameTime, player);
        int autoHomeCount = 0;
        while (autoHomeCount < AUTO_HOME_MAX_PER_PROJECTION
                && SlotWorkspaceCommandService.autoHomeTriageItems(runtime, projected, autoHomeAttempted)) {
            autoHomeCount++;
            projected = project(authority, selected, combinedDiagnostics, gameTime, player);
        }
        if (autoHomeCount >= AUTO_HOME_MAX_PER_PROJECTION && !projected.triageItems().isEmpty()) {
            combinedDiagnostics = combineDiagnostics(combinedDiagnostics, "auto_home_deferred");
            projected = project(authority, selected, combinedDiagnostics, gameTime, player);
        }

        if (!forceRevision && sameViewIgnoringRevision(viewModel, projected)) {
            return viewModel;
        }

        long revision = nextRevision++;
        viewModel = projected.withRevision(revision);
        context = new WorkspaceActionSessionContext(context.sessionId(), context.menuContainerId(), revision);
        return viewModel;
    }

    private static boolean sameViewIgnoringRevision(
            SlotWorkspaceViewModel current,
            SlotWorkspaceViewModel projected
    ) {
        if (current == null || projected == null) {
            return current == projected;
        }
        return current.withRevision(projected.revision()).equals(projected);
    }

    WorkspaceCommandOutcome handleAction(ServerPlayer player, WorkspaceActionPacket packet) {
        if (packet == null) {
            return applyOutcome(WorkspaceCommandOutcome.rejected("missing_action_packet"));
        }
        Object[] args = packet.toObjects();
        WorkspaceCommandOutcome outcome = switch (packet.action()) {
            case TRANSFER -> {
                InventoryActionTarget source = target(integerArg(args, 0), integerArg(args, 1), true);
                InventoryActionTarget destination = target(integerArg(args, 2), integerArg(args, 3), false);
                if (source == null || destination == null) {
                    yield WorkspaceCommandOutcome.rejected("invalid_transfer_target");
                }
                WorkspaceTransferExecution execution = executeTransfer(
                        player,
                        source,
                        destination,
                        stringArg(args, 4)
                );
                WorkspaceTransferFeedback feedback = execution.feedback();
                boolean success = execution.outcome() != null && execution.outcome().successful();
                yield new WorkspaceCommandOutcome(success, feedback.status(), feedback.diagnostics());
            }
            case SET_SEARCH_QUERY -> {
                String query = WorkspaceSearchQuery.cleanInput(stringArg(args, 0));
                if (query.equals(searchQuery)) {
                    yield WorkspaceCommandOutcome.accepted("ready", diagnostics);
                }
                searchQuery = query;
                yield WorkspaceCommandOutcome.accepted("search updated", searchQuery.isBlank() ? "" : "query=" + searchQuery);
            }
            case DEPOSIT -> {
                InventoryHostDescriptor host = resolveHost(player);
                InventoryAuthoritySnapshot authority = host == null
                        ? InventoryAuthoritySnapshot.empty()
                        : InventoryAuthorityReadService.serverAuthority(player, host);
                yield WorkspaceChestCommandService.deposit(
                        player,
                        runtime,
                        authority,
                        this::descriptorForIdentity);
            }
            case GATHER_ACTIVE_KIT -> gatherActiveKit(player);
            case TAKE_ALL_FROM_CHEST -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeAllFromChest(
                        player,
                        runtime,
                        stringArg(args, 0));
                reapplyActiveKitAfterCarryAcquisition(player, chestOutcome);
                yield chestOutcome;
            }
            case DEPOSIT_CARRIED_TO_CHEST -> WorkspaceChestCommandService.depositCarriedToChest(
                    player,
                    runtime,
                    identityArg(args, 0),
                    stringArg(args, 3));
            case DEPOSIT_HOTBAR_TO_CHEST -> WorkspaceChestCommandService.depositHotbarToChest(
                    player,
                    runtime,
                    integerArg(args, 0),
                    stringArg(args, 1));
            case TAKE_FROM_CHEST -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeFromChest(
                        player,
                        runtime,
                        stringArg(args, 0),
                        integerArg(args, 1),
                        WorkspaceChestCommandService.TakeQuantity.STACK);
                reapplyActiveKitAfterCarryAcquisition(player, chestOutcome);
                yield chestOutcome;
            }
            case TAKE_ONE_FROM_CHEST -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeFromChest(
                        player,
                        runtime,
                        stringArg(args, 0),
                        integerArg(args, 1),
                        WorkspaceChestCommandService.TakeQuantity.ITEM);
                reapplyActiveKitAfterCarryAcquisition(player, chestOutcome);
                yield chestOutcome;
            }
            case TAKE_ONE_BY_IDENTITY -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeByIdentity(
                        player,
                        runtime,
                        identityArg(args, 0),
                        1);
                reapplyActiveKitAfterCarryAcquisition(player, chestOutcome);
                yield chestOutcome;
            }
            case TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeDesiredGapOrStackByIdentity(
                        player,
                        runtime,
                        identityArg(args, 0));
                reapplyActiveKitAfterCarryAcquisition(player, chestOutcome);
                yield chestOutcome;
            }
            case TAKE_STACK_BY_IDENTITY -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeByIdentity(
                        player,
                        runtime,
                        identityArg(args, 0),
                        Integer.MAX_VALUE);
                reapplyActiveKitAfterCarryAcquisition(player, chestOutcome);
                yield chestOutcome;
            }
            case DEPOSIT_HOME_TO_LINKED_CHEST -> WorkspaceChestCommandService.depositIdentityToLinkedChest(
                    player,
                    runtime,
                    identityArg(args, 0),
                    WorkspaceChestCommandService.DepositQuantity.STACK,
                    WorkspaceChestCommandService.DesiredCountPolicy.RESPECT,
                    this::descriptorForIdentity);
            case DEPOSIT_ONE_HOME_TO_LINKED_CHEST -> WorkspaceChestCommandService.depositIdentityToLinkedChest(
                    player,
                    runtime,
                    identityArg(args, 0),
                    WorkspaceChestCommandService.DepositQuantity.ITEM,
                    WorkspaceChestCommandService.DesiredCountPolicy.IGNORE,
                    this::descriptorForIdentity);
            case PICKUP_TO_CURSOR -> applyCursorOutcome(player, WorkspaceCursorCommandService.pickupToCursor(
                    player,
                    runtime,
                    identityArg(args, 0),
                    integerArg(args, 3)));
            case CURSOR_CANCEL -> applyCursorOutcome(player, WorkspaceCursorCommandService.cursorCancel(
                    player,
                    runtime,
                    cursorOrigin,
                    this::descriptorForIdentity));
            case CURSOR_SMART_DEPOSIT -> applyCursorOutcome(player, WorkspaceCursorCommandService.cursorSmartDeposit(
                    player,
                    runtime,
                    cursorOrigin,
                    this::descriptorForIdentity));
            case DROP_CURSOR_INTO_CHEST -> applyCursorOutcome(player, WorkspaceCursorCommandService.dropCursorIntoChest(
                    player,
                    runtime,
                    cursorOrigin,
                    stringArg(args, 0)));
            case DROP_CURSOR_AT_HOTBAR -> applyCursorOutcome(player, WorkspaceCursorCommandService.dropCursorAtHotbar(
                    player,
                    cursorOrigin,
                    integerArg(args, 0),
                    integerArg(args, 1)));
            case CROSS_SURFACE_DROP_ON_HOST_SLOT -> WorkspaceCursorCommandService.crossSurfaceDropOnHostSlot(
                    player,
                    identityArg(args, 0),
                    integerArg(args, 3));
            case CROSS_SURFACE_QUICK_MOVE_ATLAS -> WorkspaceCursorCommandService.crossSurfaceQuickMoveAtlas(
                    player,
                    identityArg(args, 0),
                    integerArg(args, 3));
            case ASSIGN_HOME -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.assignHome(
                        runtime,
                        viewModel,
                        learnedRules,
                        Forge120IslandSignalExtractor::extract,
                        stringArg(args, 0),
                        stringArg(args, 1),
                        stringArg(args, 2),
                        stringArg(args, 3),
                        integerArg(args, 4)
                );
            }
            case ACCEPT_CHIP -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.acceptChip(
                        runtime,
                        viewModel,
                        learnedRules,
                        Forge120IslandSignalExtractor::extract,
                        stringArg(args, 0),
                        stringArg(args, 1),
                        stringArg(args, 2),
                        stringArg(args, 3),
                        stringArg(args, 4)
                );
            }
            case CREATE_NAMED_ISLAND -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.createNamedIslandForItem(
                        runtime,
                        viewModel,
                        learnedRules,
                        Forge120IslandSignalExtractor::extract,
                        stringArg(args, 0),
                        stringArg(args, 1),
                        stringArg(args, 2),
                        stringArg(args, 3),
                        integerArg(args, 4),
                        integerArg(args, 5),
                        integerArg(args, 6)
                );
            }
            case MOVE_ISLAND -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.moveIsland(
                        runtime,
                        viewModel,
                        stringArg(args, 0),
                        doubleArg(args, 1),
                        doubleArg(args, 2)
                );
            }
            case REORDER_ISLAND -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.reorderIsland(
                        runtime,
                        viewModel,
                        stringArg(args, 0),
                        integerArg(args, 1)
                );
            }
            case MOVE_CHEST -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.moveChest(
                        runtime,
                        viewModel,
                        stringArg(args, 0),
                        integerArg(args, 1),
                        integerArg(args, 2));
            }
            case RELABEL_CHEST -> {
                refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.relabelChest(
                        runtime,
                        viewModel,
                        stringArg(args, 0),
                        stringArg(args, 1));
            }
            case FORGET_CHEST -> forgetChest(player, stringArg(args, 0));
            case CLAIM_CHEST_AT_POS -> claimChestAtPos(
                    player,
                    stringArg(args, 0),
                    integerArg(args, 1),
                    integerArg(args, 2),
                    integerArg(args, 3));
            case RENAME_CLUSTER -> SlotWorkspaceCommandService.relabelCluster(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1));
            case FORGET_ITEM_AFFINITY -> SlotWorkspaceCommandService.forgetItemAffinity(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    stringArg(args, 3));
            case RENAME_ISLAND -> SlotWorkspaceCommandService.renameIsland(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1)
            );
            case RECOLOR_ISLAND -> SlotWorkspaceCommandService.recolorIsland(
                    runtime,
                    stringArg(args, 0),
                    integerArg(args, 1)
            );
            case SET_ISLAND_ICON -> SlotWorkspaceCommandService.setIslandIcon(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    stringArg(args, 3)
            );
            case DELETE_ISLAND -> SlotWorkspaceCommandService.deleteIsland(runtime, stringArg(args, 0));
            case MOVE_HOTBAR_TO_ATLAS -> {
                refreshViewBeforeCommand(player);
                yield moveHotbarToAtlas(
                        player,
                        integerArg(args, 0),
                        stringArg(args, 1),
                        integerArg(args, 2));
            }
            case SAVE_KIT -> {
                refreshViewBeforeCommand(player);
                yield saveBeltAsKit(player, stringArg(args, 0));
            }
            case ACTIVATE_KIT -> {
                refreshViewBeforeCommand(player);
                yield activateKit(player, stringArg(args, 0));
            }
            case DEACTIVATE_KIT -> SlotWorkspaceCommandService.deactivateKit(runtime);
            case DELETE_KIT -> SlotWorkspaceCommandService.deleteKit(runtime, stringArg(args, 0));
            case SWITCH_KIT_PAGE -> {
                refreshViewBeforeCommand(player);
                yield switchKitPage(player, integerArg(args, 0));
            }
            case ADD_KIT_PAGE -> SlotWorkspaceCommandService.addKitPage(runtime, stringArg(args, 0));
            case REMOVE_KIT_PAGE -> SlotWorkspaceCommandService.removeKitPage(
                    runtime,
                    stringArg(args, 0),
                    integerArg(args, 1) == null ? -1 : integerArg(args, 1));
            case SET_KIT_SLOT_IDENTITY -> {
                refreshViewBeforeCommand(player);
                yield setKitSlotIdentity(
                        player,
                        stringArg(args, 0),
                        integerArg(args, 1),
                        integerArg(args, 2),
                        stringArg(args, 3),
                        stringArg(args, 4),
                        stringArg(args, 5));
            }
            case RENAME_KIT -> SlotWorkspaceCommandService.renameKit(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1));
            case DUPLICATE_KIT -> SlotWorkspaceCommandService.duplicateKit(runtime, stringArg(args, 0));
            case SWAP_KIT_SLOTS -> SlotWorkspaceCommandService.swapKitSlots(
                    runtime,
                    stringArg(args, 0),
                    integerArg(args, 1) == null ? -1 : integerArg(args, 1),
                    integerArg(args, 2) == null ? -1 : integerArg(args, 2),
                    integerArg(args, 3) == null ? -1 : integerArg(args, 3));
            case SET_KIT_SCOPED_DESIRED_COUNT -> SlotWorkspaceCommandService.setKitScopedDesiredCount(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    stringArg(args, 3),
                    integerArg(args, 4) == null ? 0 : integerArg(args, 4));
            case SET_PLAYER_DESIRED_COUNT -> SlotWorkspaceCommandService.setPlayerDesiredCount(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    integerArg(args, 3) == null ? 0 : integerArg(args, 3));
            case ADJUST_PLAYER_DESIRED_COUNT -> SlotWorkspaceCommandService.adjustPlayerDesiredCount(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    integerArg(args, 3) == null ? 0 : integerArg(args, 3));
            case RETURN_HOTBAR_TO_HOME -> {
                refreshViewBeforeCommand(player);
                yield returnHotbarToHome(player, integerArg(args, 0));
            }
            case ASSIGN_HOME_TO_FREE_HOTBAR -> {
                refreshViewBeforeCommand(player);
                yield assignHomeToFreeHotbar(
                        player,
                        identityArg(args, 0),
                        false);
            }
            case ASSIGN_HOME_TO_HOTBAR_ONLY -> {
                refreshViewBeforeCommand(player);
                yield assignHomeToFreeHotbar(
                        player,
                        identityArg(args, 0),
                        true);
            }
            case ASSIGN_IDENTITY_TO_HOTBAR_SLOT -> {
                refreshViewBeforeCommand(player);
                yield assignIdentityToHotbarSlot(
                        player,
                        identityArg(args, 0),
                        integerArg(args, 3));
            }
            case UNDO -> SlotWorkspaceCommandService.performUndo(runtime);
            case REDO -> SlotWorkspaceCommandService.performRedo(runtime);
            default -> WorkspaceCommandOutcome.rejected("forge_action_not_enabled:" + packet.action().wireId());
        };
        return applyOutcome(outcome);
    }

    private SlotWorkspaceViewModel project(
            InventoryAuthoritySnapshot authority,
            int selected,
            String combinedDiagnostics,
            long gameTime,
            ServerPlayer player
    ) {
        WorkflowDomainSnapshot snapshot = runtime == null ? null : runtime.snapshot();
        ClaimedChestMap claimedChestMap = snapshot == null
                ? ClaimedChestMap.empty()
                : snapshot.claimedChestMap();
        MinecraftServer server = player == null ? null : player.getServer();
        WorldStorageAccess worldStorage = StorageAccessRegistry.isInstalled()
                ? StorageAccessRegistry.worldStorageAccess()
                : null;
        SlotWorkspaceViewModel.LootChestSource lootChestSource = resolveLootChestSource(player, claimedChestMap);
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel =
                resolveActiveChestPanel(player, claimedChestMap);
        return SlotWorkspaceViewModel.project(
                authority,
                snapshot,
                status,
                combinedDiagnostics,
                0,
                selected,
                0,
                learnedRules,
                Forge120IslandSignalExtractor::extract,
                WorkspaceChestProjectionSupport.contentsResolver(server, claimedChestMap, worldStorage),
                WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap),
                ignored -> null,
                lootChestSource,
                searchQuery,
                gameTime,
                activeChestPanel
        );
    }

    private WorkspaceCommandOutcome claimChestAtPos(
            ServerPlayer player,
            String dimensionId,
            Integer x,
            Integer y,
            Integer z
    ) {
        if (player == null || runtime == null
                || dimensionId == null || dimensionId.isBlank()
                || x == null || y == null || z == null) {
            return WorkspaceCommandOutcome.rejected("invalid_claim_request");
        }
        ServerLevel level = player.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            return WorkspaceCommandOutcome.rejected("dimension_mismatch");
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!ForgeChestStorageAnchors.isClaimable(level, pos)) {
            return WorkspaceCommandOutcome.rejected("not_claimable");
        }
        ChestAnchor anchor = ForgeChestStorageAnchors.toAnchor(level, pos);
        if (anchor == null) {
            return WorkspaceCommandOutcome.rejected("anchor_resolution_failed");
        }
        UUID storageId = ForgeChestDepositObserver.resolveOrCreateClaim(
                runtime.chestClaimWorkflow(), level, pos, anchor);
        if (storageId == null) {
            return WorkspaceCommandOutcome.rejected("claim_failed");
        }
        return WorkspaceCommandOutcome.accepted("chest_claimed", storageId.toString());
    }

    private WorkspaceCommandOutcome forgetChest(ServerPlayer player, String storageId) {
        List<BlockPos> anchorPositions = anchorPositionsForChest(player, storageId);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.forgetChest(runtime, storageId);
        if (!outcome.success()) {
            return outcome;
        }
        if (player != null) {
            ServerLevel level = player.serverLevel();
            for (BlockPos pos : anchorPositions) {
                ForgeChestStorageIds.clear(level, pos);
            }
        }
        return outcome;
    }

    private List<BlockPos> anchorPositionsForChest(ServerPlayer player, String storageId) {
        if (player == null || runtime == null || storageId == null || storageId.isBlank()) {
            return List.of();
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException ignored) {
            return List.of();
        }
        ClaimedChest existing = runtime.chestClaimWorkflow().claimedChestMap().chest(uuid);
        if (existing == null) {
            return List.of();
        }
        String dimensionId = player.serverLevel().dimension().location().toString();
        ArrayList<BlockPos> positions = new ArrayList<>(existing.anchors().size());
        for (ChestAnchor anchor : existing.anchors()) {
            if (anchor != null && dimensionId.equals(anchor.dimensionId())) {
                positions.add(new BlockPos(anchor.x(), anchor.y(), anchor.z()));
            }
        }
        return List.copyOf(positions);
    }

    private SlotWorkspaceViewModel.ActiveChestPanel resolveActiveChestPanel(
            ServerPlayer player,
            ClaimedChestMap claimedChestMap
    ) {
        return ActiveChestPanelProjectionSupport.resolve(
                player,
                runtime,
                claimedChestMap,
                ForgeChestDepositObserver.activeChestPos(player),
                ForgeChestStorageAnchors::toAnchor);
    }

    private SlotWorkspaceViewModel.LootChestSource resolveLootChestSource(
            ServerPlayer player,
            ClaimedChestMap claimedChestMap
    ) {
        return LootChestProjectionSupport.closest(
                        player,
                        claimedChestMap,
                        ForgeChestStorageAnchors::isClaimable,
                        ForgeChestStorageIds::read)
                .map(pos -> {
                    ServerLevel level = player.serverLevel();
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (!(blockEntity instanceof Container container)) {
                        return null;
                    }
                    int size = container.getContainerSize();
                    ArrayList<ItemStack> contents = new ArrayList<>(size);
                    for (int index = 0; index < size; index++) {
                        contents.add(container.getItem(index).copy());
                    }
                    String dimensionId = level.dimension().location().toString();
                    String label = "Loot chest at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
                    return new SlotWorkspaceViewModel.LootChestSource(
                            pos.getX(), pos.getY(), pos.getZ(), dimensionId, label, contents
                    );
                })
                .orElse(null);
    }

    private WorkspaceTransferExecution executeTransfer(
            ServerPlayer player,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin
    ) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceTransferExecution.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                source,
                destination,
                origin
        );
        if (!build.dispatchable()) {
            return WorkspaceTransferExecution.rejected(build.diagnostics());
        }
        InventoryActionRequest request = build.request();
        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                player,
                request,
                ProtectionPolicy.allowAll()
        );
        recordOutcome(player, outcome);
        WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);
        return new WorkspaceTransferExecution(host, request, outcome, feedback);
    }

    private WorkspaceCommandOutcome returnHotbarToHome(ServerPlayer player, Integer hotbarIndex) {
        return WorkspaceBeltCommandService.returnHotbarToHome(
                player,
                runtime,
                viewModel,
                hotbarIndex,
                this::descriptorForIdentity,
                (source, destination, origin) -> executeTransfer(player, source, destination, origin),
                "slot_workspace.forge");
    }

    private WorkspaceCommandOutcome assignHomeToFreeHotbar(
            ServerPlayer player,
            ItemIdentity identity,
            boolean suppressChestPreference
    ) {
        return WorkspaceBeltCommandService.assignHomeToFreeHotbar(
                player,
                runtime,
                viewModel,
                identity,
                suppressChestPreference,
                this::descriptorForIdentity,
                targetHotbarIndex -> assignIdentityToHotbarIndex(player, identity, targetHotbarIndex));
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarSlot(
            ServerPlayer player,
            ItemIdentity identity,
            Integer hotbarIndex
    ) {
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (index < 0 || index >= 9) {
            return WorkspaceCommandOutcome.rejected("invalid_hotbar_slot");
        }
        return assignIdentityToHotbarIndex(player, identity, index);
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarIndex(
            ServerPlayer player,
            ItemIdentity identity,
            int hotbarIndex
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        return WorkspaceBeltCommandService.assignIdentityToHotbarByTransfer(
                player,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                request -> {
                    InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                            host,
                            player,
                            request,
                            ProtectionPolicy.allowAll()
                    );
                    recordOutcome(player, outcome);
                    return outcome;
                },
                identity,
                hotbarIndex,
                "slot_workspace.forge");
    }

    private WorkspaceCommandOutcome moveHotbarToAtlas(
            ServerPlayer player,
            Integer hotbarIndex,
            String islandId,
            Integer ordinal
    ) {
        return WorkspaceBeltCommandService.moveHotbarToAtlas(
                player,
                runtime,
                viewModel,
                learnedRules,
                Forge120IslandSignalExtractor::extract,
                hotbarIndex,
                islandId,
                ordinal,
                (source, destination, origin) -> executeTransfer(player, source, destination, origin),
                () -> project(player),
                "slot_workspace.forge");
    }

    private WorkspaceCommandOutcome saveBeltAsKit(ServerPlayer player, String name) {
        InventoryHostDescriptor host = resolveHost(player);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(player, host);
        return SlotWorkspaceCommandService.saveBeltAsKit(
                runtime,
                authority,
                KIT_IDENTITY_RESOLVER,
                name);
    }

    private WorkspaceCommandOutcome activateKit(ServerPlayer player, String kitId) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        return SlotWorkspaceCommandService.activateKit(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor(host, player),
                kitId);
    }

    private WorkspaceCommandOutcome switchKitPage(ServerPlayer player, Integer direction) {
        return KitPageCycleService.switchActivePage(
                player,
                runtime,
                direction == null ? 1 : direction,
                "forge_session",
                outcome -> {
                    if (outcome != null && outcome.successful()) {
                        ForgeCarriedActivityTracker.suppressNext(player);
                    }
                });
    }

    private WorkspaceCommandOutcome gatherActiveKit(ServerPlayer player) {
        return KitGatherService.toWorkspaceOutcome(gatherActiveKitAndReapply(player, runtime));
    }

    private WorkspaceCommandOutcome setKitSlotIdentity(
            ServerPlayer player,
            String kitId,
            Integer pageIndex,
            Integer slotIndex,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        InventoryHostDescriptor host = resolveHost(player);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(player, host);
        Function<InventoryActionRequest, InventoryActionOutcome> executor = host == null
                ? null
                : actionExecutor(host, player);
        return SlotWorkspaceCommandService.setKitSlotIdentity(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                executor,
                kitId,
                pageIndex == null ? -1 : pageIndex,
                slotIndex == null ? -1 : slotIndex,
                itemId,
                comparisonMode,
                componentFingerprint);
    }

    private Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor(
            InventoryHostDescriptor host,
            ServerPlayer player
    ) {
        return actionExecutor(runtime, host, player);
    }

    static KitGatherService.Outcome gatherActiveKitAndReapply(
            ServerPlayer player,
            WorkflowDomainRuntime runtime
    ) {
        KitGatherService.Outcome outcome = KitGatherService.gatherActiveKit(player, runtime);
        reapplyActiveKitFromCarry(player, runtime);
        return outcome;
    }

    private static Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor(
            WorkflowDomainRuntime runtime,
            InventoryHostDescriptor host,
            ServerPlayer player
    ) {
        return request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    player,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(runtime, player, outcome);
            return outcome;
        };
    }

    private WorkspaceCommandOutcome applyCursorOutcome(
            ServerPlayer player,
            WorkspaceCursorCommandService.CursorCommandOutcome cursorOutcome
    ) {
        if (cursorOutcome == null) {
            return WorkspaceCommandOutcome.rejected("cursor_command_failed");
        }
        cursorOrigin = cursorOutcome.cursorOrigin();
        WorkspaceCommandOutcome outcome = cursorOutcome.outcome();
        if (outcome.success()) {
            ForgeCarriedActivityTracker.suppressNext(player);
        }
        return outcome;
    }

    private void reapplyActiveKitAfterCarryAcquisition(
            ServerPlayer player,
            WorkspaceCommandOutcome outcome
    ) {
        if (player == null || runtime == null || outcome == null || !outcome.success()) {
            return;
        }
        String status = outcome.status();
        if (!("took_one".equals(status)
                || "took_stack".equals(status)
                || "took_partial".equals(status)
                || "took_all".equals(status)
                || "took_all_partial".equals(status))) {
            return;
        }
        reapplyActiveKitFromCarry(player, runtime);
    }

    private static void reapplyActiveKitFromCarry(ServerPlayer player, WorkflowDomainRuntime runtime) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return;
        }
        if (runtime == null || !runtime.kitWorkflow().activation().isActive()) {
            return;
        }
        ForgeCarriedActivityTracker.suppressNext(player);
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceCommandService.reapplyActiveKit(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor(runtime, host, player));
    }

    private dev.imagio.slot.inventory.triage.IslandSignalDescriptor descriptorForIdentity(ItemIdentity identity) {
        if (identity == null || identity.itemId() == null || identity.itemId().isBlank()) {
            return null;
        }
        ItemStack stack = resolveGhostStack(identity.itemId());
        if (stack.isEmpty()) {
            return null;
        }
        try {
            return Forge120IslandSignalExtractor.extract(stack);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static ItemStack resolveGhostStack(String itemId) {
        return Forge120GhostStackFactory.resolve(itemId);
    }

    private void recordOutcome(ServerPlayer player, InventoryActionOutcome outcome) {
        recordOutcome(runtime, player, outcome);
    }

    private static void recordOutcome(
            WorkflowDomainRuntime runtime,
            ServerPlayer player,
            InventoryActionOutcome outcome
    ) {
        if (runtime != null) {
            runtime.recordOutcome(outcome);
        }
        if (outcome != null && outcome.successful()) {
            ForgeCarriedActivityTracker.suppressNext(player);
        }
    }

    private void refreshViewBeforeCommand(ServerPlayer player) {
        if (player != null) {
            project(player);
        }
    }

    private WorkspaceCommandOutcome applyOutcome(WorkspaceCommandOutcome outcome) {
        WorkspaceCommandOutcome resolved = outcome == null
                ? WorkspaceCommandOutcome.rejected("null_command_outcome")
                : outcome;
        status = resolved.status();
        diagnostics = resolved.diagnostics();
        return resolved;
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT Workspace"),
                ForgeWorkspaceSession.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotWorkspace", "forge")
                )
        ));
    }

    private static String combineDiagnostics(String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + "; " + second;
        }
        if (hasFirst) {
            return first;
        }
        return hasSecond ? second : "";
    }

    private static String stringArg(Object[] args, int index) {
        Object value = arg(args, index);
        return value instanceof String string ? string : "";
    }

    private static Integer integerArg(Object[] args, int index) {
        Object value = arg(args, index);
        return value instanceof Integer integer ? integer : null;
    }

    private static Double doubleArg(Object[] args, int index) {
        Object value = arg(args, index);
        return value instanceof Double doubleValue ? doubleValue : null;
    }

    private static ItemIdentity identityArg(Object[] args, int startIndex) {
        return new SlotWorkspaceViewModel.IdentityRef(
                stringArg(args, startIndex),
                stringArg(args, startIndex + 1),
                stringArg(args, startIndex + 2)
        ).toIdentity();
    }

    private static Object arg(Object[] args, int index) {
        return args == null || index < 0 || index >= args.length ? null : args[index];
    }

    private static InventoryActionTarget target(Integer kind, Integer index, boolean source) {
        int resolvedKind = kind == null ? -1 : kind;
        int resolvedIndex = index == null ? -1 : index;
        return switch (resolvedKind) {
            case TARGET_MAIN_SOURCE -> source
                    ? null
                    : new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN);
            case TARGET_MAIN_SLOT -> resolvedIndex >= 0 && resolvedIndex < 27
                    ? new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, resolvedIndex)
                    : null;
            case TARGET_HOTBAR_SLOT -> resolvedIndex >= 0 && resolvedIndex < 9
                    ? new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, resolvedIndex)
                    : null;
            default -> null;
        };
    }
}
