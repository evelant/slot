package dev.imagio.slot.forge.network;

import dev.imagio.slot.forge.storage.ForgeChestDepositObserver;
import dev.imagio.slot.forge.storage.ForgeChestStorageAnchors;
import dev.imagio.slot.forge.storage.ForgeChestStorageIds;
import dev.imagio.slot.forge.compat.sacks.SacksNSuchCarriedProvider;
import dev.imagio.slot.forge.compat.toolbelt.ToolBeltCarriedProvider;
import dev.imagio.slot.forge.triage.Forge120IslandSignalExtractor;
import dev.imagio.slot.forge.storage.ForgeCarriedActivityTracker;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemStackStructuralKey;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.workspace.ChestContentAffinitySeeder;
import dev.imagio.slot.inventory.workspace.HotbarSlotRecencyRegistry;
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
import dev.imagio.slot.inventory.workspace.WorkspaceCraftRunCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceCursorCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceHotbarSlotReverser;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionRequest;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionResult;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionSessionCache;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageIndex;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageRoutingContext;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageMemoryStore;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionPacket;
import dev.imagio.slot.workflow.domain.ContextualSuggestionFeatureFlags;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.ui.action.WorkspaceActionSessionContext;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

final class ForgeWorkspaceSession {
    private static final int TARGET_MAIN_SOURCE = 1;
    private static final int TARGET_MAIN_SLOT = 2;
    private static final int TARGET_HOTBAR_SLOT = 3;
    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    private final WorkflowDomainRuntime runtime;
    private final LearnedIslandRuleStore learnedRules = new LearnedIslandRuleStore();
    private final Set<ItemIdentity> autoHomeAttempted = new HashSet<>();
    private final WorkspaceProjectionSessionCache projectionCache = new WorkspaceProjectionSessionCache();
    private WorkspaceActionSessionContext context;
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private WorkspaceCursorCommandService.CursorOrigin cursorOrigin;
    private long nextRevision = 1L;
    private String status = "ready";
    private String diagnostics = "";
    private String searchQuery = "";
    private String lastContentFingerprint = "";
    private long lastObservedWorkflowSequence = Long.MIN_VALUE;
    private long lastObservedCarriedRevision = Long.MIN_VALUE;
    private Set<String> lastObservedProximateStorageIds = Set.of();
    private Set<String> lastObservedContextualStorageIds = Set.of();
    private AbstractContainerMenu observedMenu;
    private ContainerListener observedMenuListener;
    private final Map<Integer, ItemStackStructuralKey> observedSlotKeys = new HashMap<>();
    private boolean dirty;

    ForgeWorkspaceSession(WorkspaceActionEnvelope envelope, int menuContainerId, WorkflowDomainRuntime runtime) {
        this.context = new WorkspaceActionSessionContext(envelope.sessionId(), menuContainerId, envelope.viewRevision());
        this.runtime = runtime;
    }

    WorkspaceActionSessionContext context() {
        return context;
    }

    void attachMenuListener(ServerPlayer player) {
        detachMenuListener();
        AbstractContainerMenu menu = player == null ? null : player.containerMenu;
        if (menu == null) {
            return;
        }
        observedMenu = menu;
        seedObservedSlotKeys(menu);
        observedMenuListener = new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu changedMenu, int slotIndex, ItemStack stack) {
                if (changedMenu == observedMenu && updateObservedSlotKey(slotIndex, stack)) {
                    markDirty();
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu changedMenu, int dataIndex, int value) {
                // The workspace projection reads item slots, not menu progress/data
                // values. Marking dirty here would reproject every tick for many
                // furnace/machine menus that update progress through ContainerData.
            }
        };
        menu.addSlotListener(observedMenuListener);
    }

    void detachMenuListener() {
        if (observedMenu != null && observedMenuListener != null) {
            observedMenu.removeSlotListener(observedMenuListener);
        }
        observedMenu = null;
        observedMenuListener = null;
        observedSlotKeys.clear();
        dirty = false;
    }

    boolean observes(AbstractContainerMenu menu) {
        return observedMenu != null && observedMenu == menu;
    }

    void markDirty() {
        dirty = true;
    }

    boolean dirty() {
        return dirty;
    }

    boolean shouldRefresh(ServerPlayer player) {
        return dirty
                || currentWorkflowSequence() != lastObservedWorkflowSequence
                || CarriedInventoryRevisions.revision(player) != lastObservedCarriedRevision
                || storageProximityChanged(player);
    }

    void clearDirty() {
        dirty = false;
    }

    public SlotWorkspaceViewModel currentViewModel() {
        return viewModel;
    }

    private void seedObservedSlotKeys(AbstractContainerMenu menu) {
        observedSlotKeys.clear();
        if (menu == null) {
            return;
        }
        for (int slotIndex = 0; slotIndex < menu.slots.size(); slotIndex++) {
            observedSlotKeys.put(slotIndex, ItemStackStructuralKey.from(menu.slots.get(slotIndex).getItem()));
        }
    }

    private boolean updateObservedSlotKey(int slotIndex, ItemStack stack) {
        if (slotIndex < 0) {
            return true;
        }
        ItemStackStructuralKey next = ItemStackStructuralKey.from(stack);
        ItemStackStructuralKey previous = observedSlotKeys.put(slotIndex, next);
        return previous == null || !previous.equals(next);
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
        if (runtime != null && ContextualSuggestionFeatureFlags.LIVE_OBSERVATION_ENABLED) {
            runtime.contextualSuggestions().observeStationContext(
                    host,
                    authority,
                    gameTime,
                    DomainEventMetadata.origin("contextual.forge.station_context"));
        }

        WorkspaceProjectionResult projection = projectionCache.project(
                projectionRequest(authority, selected, combinedDiagnostics, gameTime, player));
        SlotWorkspaceViewModel projected = projection.viewModel();
        if (SlotWorkspaceCommandService.autoHomeTriageItems(runtime, projected, autoHomeAttempted)) {
            projectionCache.clear();
            projection = projectionCache.project(
                    projectionRequest(authority, selected, combinedDiagnostics, gameTime, player));
            projected = projection.viewModel();
        }
        HotbarSlotRecencyRegistry.observe(player, projected);

        long observedWorkflowSequence = currentWorkflowSequence();
        long observedCarriedRevision = CarriedInventoryRevisions.revision(player);
        lastObservedWorkflowSequence = observedWorkflowSequence;
        lastObservedCarriedRevision = observedCarriedRevision;
        if (!forceRevision && projection.contentFingerprint().equals(lastContentFingerprint)) {
            return viewModel;
        }

        lastContentFingerprint = projection.contentFingerprint();
        long revision = nextRevision++;
        viewModel = projected.withRevision(revision);
        context = new WorkspaceActionSessionContext(context.sessionId(), context.menuContainerId(), revision);
        return viewModel;
    }

    private long currentWorkflowSequence() {
        if (runtime == null) {
            return 0L;
        }
        WorkflowDomainSnapshot snapshot = runtime.snapshot();
        return snapshot == null ? 0L : snapshot.nextGlobalSequence() * 31L + snapshot.craftRun().revision();
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
                        authority);
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
            case TAKE_ITEMS_BY_IDENTITY -> {
                WorkspaceCommandOutcome chestOutcome = WorkspaceChestCommandService.takeByIdentity(
                        player,
                        runtime,
                        identityArg(args, 0),
                        integerArg(args, 3) == null ? 0 : integerArg(args, 3));
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
            case TOGGLE_WANTED_ITEM -> toggleWantedItem(player, identityArg(args, 0));
            case SET_WANTED_COUNT -> setWantedCount(
                    player,
                    identityArg(args, 0),
                    integerArg(args, 3) == null ? 0 : integerArg(args, 3));
            case ADJUST_WANTED_COUNT -> adjustWantedCount(
                    player,
                    identityArg(args, 0),
                    integerArg(args, 3) == null ? 0 : integerArg(args, 3));
            case SET_JUNK -> setJunk(
                    identityArg(args, 0),
                    integerArg(args, 3) != null && integerArg(args, 3) != 0);
            case TRASH_IDENTITY -> SlotWorkspaceCommandService.trashIdentity(
                    player,
                    runtime,
                    identityArg(args, 0));
            case CRAFT_RUN_STAGE_ENTRY -> stageCraftRunEntry(player, stringArg(args, 0));
            case CRAFT_RUN_ADJUST_ENTRY -> adjustCraftRunEntry(
                    stringArg(args, 0),
                    integerArg(args, 1) == null ? 0 : integerArg(args, 1));
            case CRAFT_RUN_SELECT_INGREDIENT -> selectCraftRunIngredient(
                    stringArg(args, 0),
                    stringArg(args, 1),
                    identityArg(args, 2));
            case CRAFT_RUN_REMOVE_ENTRY -> runtime != null && runtime.craftRunWorkflow().remove(stringArg(args, 0))
                    ? WorkspaceCommandOutcome.accepted("craft_run_removed", "")
                    : WorkspaceCommandOutcome.rejected("craft_run_entry_not_found");
            case DEPOSIT_HOME_TO_LINKED_CHEST -> WorkspaceChestCommandService.depositIdentityToLinkedChest(
                    player,
                    runtime,
                    identityArg(args, 0),
                    WorkspaceChestCommandService.DepositQuantity.STACK,
                    WorkspaceChestCommandService.DesiredCountPolicy.RESPECT,
                    () -> activeDepositFallbackChest(player));
            case DEPOSIT_ONE_HOME_TO_LINKED_CHEST -> WorkspaceChestCommandService.depositIdentityToLinkedChest(
                    player,
                    runtime,
                    identityArg(args, 0),
                    WorkspaceChestCommandService.DepositQuantity.ITEM,
                    WorkspaceChestCommandService.DesiredCountPolicy.IGNORE,
                    () -> activeDepositFallbackChest(player));
            case DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST -> WorkspaceChestCommandService.depositIdentityCountToLinkedChest(
                    player,
                    runtime,
                    identityArg(args, 0),
                    integerArg(args, 3) == null ? 0 : integerArg(args, 3),
                    WorkspaceChestCommandService.DesiredCountPolicy.IGNORE,
                    () -> activeDepositFallbackChest(player));
            case PICKUP_TO_CURSOR -> applyCursorOutcome(player, WorkspaceCursorCommandService.pickupToCursor(
                    player,
                    runtime,
                    identityArg(args, 0),
                    integerArg(args, 3)));
            case CURSOR_CANCEL -> applyCursorOutcome(player, WorkspaceCursorCommandService.cursorCancel(
                    player,
                    runtime,
                    cursorOrigin));
            case CURSOR_SMART_DEPOSIT -> applyCursorOutcome(player, WorkspaceCursorCommandService.cursorSmartDeposit(
                    player,
                    runtime,
                    cursorOrigin));
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
            case CROSS_SURFACE_QUICK_MOVE_HOTBAR -> WorkspaceCursorCommandService.crossSurfaceQuickMoveHotbar(
                    player,
                    integerArg(args, 0));
            case ASSIGN_HOME -> {
                InventoryAuthoritySnapshot authority = refreshViewBeforeCommand(player);
                yield SlotWorkspaceCommandService.assignHome(
                        runtime,
                        viewModel,
                        learnedRules,
                        Forge120IslandSignalExtractor::extract,
                        authority,
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
            case CLAIM_CHEST_AT_POS -> claimChestAtPos(
                    player,
                    stringArg(args, 0),
                    integerArg(args, 1),
                    integerArg(args, 2),
                    integerArg(args, 3));
            case SET_CHEST_ROLE_AT_POS -> setChestRoleAtPos(
                    player,
                    stringArg(args, 0),
                    integerArg(args, 1),
                    integerArg(args, 2),
                    integerArg(args, 3),
                    stringArg(args, 4));
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
            case DELETE_ISLAND -> {
                WorkspaceCommandOutcome deleteOutcome = SlotWorkspaceCommandService.deleteIsland(runtime, stringArg(args, 0));
                if (deleteOutcome.success()) {
                    autoHomeAttempted.clear();
                }
                yield deleteOutcome;
            }
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
            case CREATE_WORKFLOW_TAB -> SlotWorkspaceCommandService.createWorkflowTab(
                    runtime,
                    stringArg(args, 0));
            case CREATE_KIT_VARIANT -> SlotWorkspaceCommandService.createKitVariant(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1));
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
            case SET_KIT_MEMBER -> SlotWorkspaceCommandService.setKitMember(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    stringArg(args, 3),
                    integerArg(args, 4) == null ? 0 : integerArg(args, 4));
            case SET_KIT_ACCEPTED_INPUT -> SlotWorkspaceCommandService.setKitAcceptedInput(
                    runtime,
                    stringArg(args, 0),
                    stringArg(args, 1),
                    stringArg(args, 2),
                    stringArg(args, 3),
                    stringArg(args, 4),
                    stringArg(args, 5),
                    integerArg(args, 6) == null ? 0 : integerArg(args, 6));
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
            case REORDER_KIT -> SlotWorkspaceCommandService.reorderKit(
                    runtime,
                    stringArg(args, 0),
                    integerArg(args, 1) == null ? -1 : integerArg(args, 1));
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
            case ASSIGN_IDENTITY_TO_AUTO_HOTBAR -> {
                refreshViewBeforeCommand(player);
                yield assignIdentityToAutoHotbar(
                        player,
                        identityArg(args, 0));
            }
            case ASSIGN_IDENTITY_TO_HOTBAR_SLOT -> {
                refreshViewBeforeCommand(player);
                yield assignIdentityToHotbarSlot(
                        player,
                        identityArg(args, 0),
                        integerArg(args, 3));
            }
            case MOVE_IDENTITY_TO_MAIN_INVENTORY -> {
                refreshViewBeforeCommand(player);
                yield moveIdentityToMainInventory(
                        player,
                        identityArg(args, 0));
            }
            case MOVE_IDENTITY_TO_BACKPACK -> {
                refreshViewBeforeCommand(player);
                yield moveIdentityToBackpack(
                        player,
                        identityArg(args, 0));
            }
            case UNDO -> SlotWorkspaceCommandService.performUndo(runtime);
            case REDO -> SlotWorkspaceCommandService.performRedo(runtime);
            default -> WorkspaceCommandOutcome.rejected("forge_action_not_enabled:" + packet.action().wireId());
        };
        return applyOutcome(outcome);
    }

    private WorkspaceCommandOutcome stageCraftRunEntry(ServerPlayer player, String entryId) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        return WorkspaceCraftRunCommandService.stageEntry(
                runtime,
                authority,
                entryId,
                actionExecutor(runtime, host, player),
                "slot_workspace.forge");
    }

    private WorkspaceCommandOutcome adjustCraftRunEntry(String entryId, int delta) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("runtime_unavailable");
        }
        return runtime.craftRunWorkflow().adjustRemainingOutput(entryId, delta)
                ? WorkspaceCommandOutcome.accepted("craft_run_adjusted", "")
                : WorkspaceCommandOutcome.rejected("craft_run_entry_not_found");
    }

    private WorkspaceCommandOutcome selectCraftRunIngredient(String entryId, String groupId, ItemIdentity identity) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("runtime_unavailable");
        }
        return runtime.craftRunWorkflow().selectIngredientAlternative(entryId, groupId, identity)
                ? WorkspaceCommandOutcome.accepted("craft_run_ingredient_selected", "")
                : WorkspaceCommandOutcome.rejected("craft_run_ingredient_not_found");
    }

    private WorkspaceProjectionRequest projectionRequest(
            InventoryAuthoritySnapshot authority,
            int selected,
            String combinedDiagnostics,
            long gameTime,
            ServerPlayer player
    ) {
        if (runtime != null) {
            runtime.collectionWorkflow().expireJunkTags();
        }
        WorkflowDomainSnapshot snapshot = runtime == null ? null : runtime.snapshot();
        WorkspaceStorageRoutingContext storageContext =
                WorkspaceStorageRoutingContext.build(player, runtime, authority);
        ClaimedChestMap claimedChestMap = storageContext.claimedChestMap();
        SlotWorkspaceViewModel.LootChestSource lootChestSource = resolveLootChestSource(player, claimedChestMap);
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel =
                resolveActiveChestPanel(player, claimedChestMap);
        List<WorldDisplayStorageSource> displaySources = storageContext.displaySources();
        Set<String> proximateIds = storageContext.proximateStorageIds();
        Set<String> contextualSuggestionStorageIds = storageContext.contextualSuggestionStorageIds();
        lastObservedProximateStorageIds = Set.copyOf(proximateIds);
        lastObservedContextualStorageIds = Set.copyOf(contextualSuggestionStorageIds);
        WorkspaceStorageIndex storageIndex = storageContext.storageIndex();
        clearSatisfiedWantedCounts(authority);
        return new WorkspaceProjectionRequest(
                authority,
                snapshot,
                status,
                combinedDiagnostics,
                0,
                selected,
                0,
                learnedRules,
                Forge120IslandSignalExtractor::extract,
                storageIndex.contentsResolver(),
                proximateIds,
                carriedContainerInfoResolver(player),
                lootChestSource,
                searchQuery,
                gameTime,
                activeChestPanel,
                displaySources,
                contextualSuggestionStorageIds,
                displaySources,
                storageIndex.trackedDisplayEntries(),
                storageIndex.liveDepositStorageIds(),
                storageIndex,
                storageContext.liveChestContentPresence(),
                storageContext.liveStorageAffinityEligibility()
        );
    }

    private boolean storageProximityChanged(ServerPlayer player) {
        if (player == null || runtime == null) {
            return false;
        }
        WorkflowDomainSnapshot snapshot = runtime.snapshot();
        ClaimedChestMap claimedChestMap = snapshot == null ? ClaimedChestMap.empty() : snapshot.claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        if (!proximate.equals(lastObservedProximateStorageIds)) {
            return true;
        }
        Set<String> contextual = WorkspaceChestProjectionSupport.proximateStorageIds(
                player,
                claimedChestMap,
                WorkspaceChestProjectionSupport.CONTEXTUAL_SUGGESTION_RADIUS_BLOCKS);
        return !contextual.equals(lastObservedContextualStorageIds);
    }

    private static Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> carriedContainerInfoResolver(
            ServerPlayer player
    ) {
        if (player == null) {
            return ignored -> null;
        }
        LinkedHashMap<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> byIdentity = new LinkedHashMap<>();
        mergeContainerInfo(byIdentity, SophisticatedBackpackInventoryIntegrationProvider.carriedContainerInfoByIdentity(player));
        mergeContainerInfo(byIdentity, SacksNSuchCarriedProvider.carriedContainerInfoByIdentity(player));
        mergeContainerInfo(byIdentity, ToolBeltCarriedProvider.carriedContainerInfoByIdentity(player));
        return byIdentity.isEmpty() ? ignored -> null : byIdentity::get;
    }

    private static void mergeContainerInfo(
            Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> target,
            Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> source
    ) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        source.forEach((identity, info) -> {
            if (identity == null || info == null || info.slotCapacity() <= 0) {
                return;
            }
            target.merge(identity, info, (left, right) -> new SlotWorkspaceViewModel.CarriedContainerInfo(
                    left.freeSlots() + right.freeSlots(),
                    left.slotCapacity() + right.slotCapacity()));
        });
    }

    private WorkspaceCommandOutcome toggleWantedItem(ServerPlayer player, ItemIdentity identity) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        return SlotWorkspaceCommandService.toggleWantedCount(
                runtime,
                authority,
                ref.itemId(),
                ref.comparisonMode(),
                ref.componentFingerprint());
    }

    private WorkspaceCommandOutcome adjustWantedCount(ServerPlayer player, ItemIdentity identity, int delta) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (delta == 0) {
            return WorkspaceCommandOutcome.accepted("wanted unchanged", identity.itemId());
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        return SlotWorkspaceCommandService.adjustWantedCount(
                runtime,
                authority,
                ref.itemId(),
                ref.comparisonMode(),
                ref.componentFingerprint(),
                delta);
    }

    private WorkspaceCommandOutcome setWantedCount(ServerPlayer player, ItemIdentity identity, int count) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        return SlotWorkspaceCommandService.setWantedCount(
                runtime,
                authority,
                ref.itemId(),
                ref.comparisonMode(),
                ref.componentFingerprint(),
                count);
    }

    private WorkspaceCommandOutcome setJunk(ItemIdentity identity, boolean marked) {
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        return SlotWorkspaceCommandService.setJunk(
                runtime,
                ref.itemId(),
                ref.comparisonMode(),
                ref.componentFingerprint(),
                marked);
    }

    private void clearSatisfiedWantedCounts(InventoryAuthoritySnapshot authority) {
        SlotWorkspaceCommandService.clearSatisfiedWantedCounts(runtime, authority);
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
        seedClaimedChestContents(player, storageId);
        return WorkspaceCommandOutcome.accepted("chest_claimed", storageId.toString());
    }

    private WorkspaceCommandOutcome setChestRoleAtPos(
            ServerPlayer player,
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String roleName
    ) {
        if (player == null || runtime == null
                || dimensionId == null || dimensionId.isBlank()
                || x == null || y == null || z == null) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_role_request");
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
        ChestRole role = ChestRole.parse(roleName);
        ClaimedChest existing = runtime.chestClaimWorkflow().chestByAnchor(anchor);
        UUID storageId = existing == null
                ? role == ChestRole.IGNORE ? null : ForgeChestDepositObserver.resolveOrCreateClaim(
                        runtime.chestClaimWorkflow(), level, pos, anchor)
                : existing.storageId();
        if (storageId == null) {
            return WorkspaceCommandOutcome.accepted("chest_role_set", ChestRole.IGNORE.name());
        }
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setChestRole(runtime, storageId, role);
        if (outcome.success() && role == ChestRole.STORAGE) {
            seedClaimedChestContents(player, storageId);
        }
        return outcome;
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

    private ClaimedChest activeDepositFallbackChest(ServerPlayer player) {
        if (player == null || runtime == null) {
            return null;
        }
        BlockPos pos = ForgeChestDepositObserver.activeChestPos(player);
        if (pos == null) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        if (!ForgeChestStorageAnchors.isClaimable(level, pos)) {
            return null;
        }
        ChestAnchor anchor = ForgeChestStorageAnchors.toAnchor(level, pos);
        if (anchor == null) {
            return null;
        }
        if (!WorkspaceChestProjectionSupport.isProximate(player, anchor)) {
            return null;
        }
        UUID storageId = ForgeChestDepositObserver.resolveOrCreateClaim(
                runtime.chestClaimWorkflow(),
                level,
                pos,
                anchor);
        seedClaimedChestContents(player, storageId);
        return storageId == null ? null : runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
    }

    private int seedClaimedChestContents(ServerPlayer player, UUID storageId) {
        if (player == null || storageId == null || runtime == null || !StorageAccessRegistry.isInstalled()) {
            return 0;
        }
        ClaimedChest claim = runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        SlotWorkspaceViewModel.ChestContentsSnapshot contents = WorkspaceChestProjectionSupport.readContents(
                player.getServer(),
                claim,
                worldStorage);
        WorkspaceStorageMemoryStore.observeStorageIds(
                player.getServer(),
                worldStorage,
                runtime.chestClaimWorkflow().claimedChestMap(),
                List.of(storageId.toString()),
                player.serverLevel().getGameTime(),
                "claim_seed");
        return ChestContentAffinitySeeder.seedInitialContents(
                runtime.chestClaimWorkflow(),
                storageId,
                contents,
                player.serverLevel().getGameTime());
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

    private WorkspaceCommandOutcome assignIdentityToAutoHotbar(
            ServerPlayer player,
            ItemIdentity identity
    ) {
        return WorkspaceBeltCommandService.assignIdentityToAutoHotbar(
                viewModel,
                identity,
                HotbarSlotRecencyRegistry.recencySequence(player),
                targetHotbarIndex -> assignIdentityToHotbarIndex(player, identity, targetHotbarIndex));
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarIndex(
            ServerPlayer player,
            ItemIdentity identity,
            int hotbarIndex
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        ItemStack hotbarBefore = WorkspaceHotbarSlotReverser.peekSlot(player, hotbarIndex);
        WorkspaceCommandOutcome outcome = assignIdentityToHotbarFromCarry(player, identity, hotbarIndex);
        if (missingFromCarry(outcome)) {
            WorkspaceCommandOutcome missingOutcome = outcome;
            WorkspaceCommandOutcome takeOutcome = WorkspaceChestCommandService.takeByIdentity(
                    player,
                    runtime,
                    identity,
                    Integer.MAX_VALUE,
                    false);
            if (tookStackForHotbar(takeOutcome)) {
                outcome = assignIdentityToHotbarFromCarry(player, identity, hotbarIndex);
            } else {
                outcome = takeOutcome.success() ? missingOutcome : takeOutcome;
            }
        }
        if (outcome.success()) {
            ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(player, hotbarIndex);
            WorkspaceBeltCommandService.recordHotbarSlotUndo(
                    player,
                    runtime,
                    hotbarIndex,
                    hotbarBefore,
                    hotbarAfter,
                    "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
            recordHotbarPlacementOnSuccess(player, hotbarIndex, outcome);
        }
        return outcome;
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarFromCarry(
            ServerPlayer player,
            ItemIdentity identity,
            int hotbarIndex
    ) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        WorkspaceCommandOutcome outcome = WorkspaceBeltCommandService.assignIdentityToHotbarByTransfer(
                player,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                request -> {
                    InventoryActionOutcome actionOutcome = InventoryActionExecutor.execute(
                            host,
                            player,
                            request,
                            ProtectionPolicy.allowAll()
                    );
                    recordOutcome(player, actionOutcome);
                    return actionOutcome;
                },
                identity,
                hotbarIndex,
                "slot_workspace.forge");
        return outcome;
    }

    private WorkspaceCommandOutcome moveIdentityToMainInventory(
            ServerPlayer player,
            ItemIdentity identity
    ) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        return WorkspaceBeltCommandService.moveIdentityToMainInventory(
                player,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                request -> {
                    InventoryActionOutcome actionOutcome = InventoryActionExecutor.execute(
                            host,
                            player,
                            request,
                            ProtectionPolicy.allowAll()
                    );
                    recordOutcome(player, actionOutcome);
                    return actionOutcome;
                },
                identity,
                "slot_workspace.forge");
    }

    private WorkspaceCommandOutcome moveIdentityToBackpack(
            ServerPlayer player,
            ItemIdentity identity
    ) {
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        return WorkspaceBeltCommandService.moveIdentityToBackpack(
                player,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                request -> {
                    InventoryActionOutcome actionOutcome = InventoryActionExecutor.execute(
                            host,
                            player,
                            request,
                            ProtectionPolicy.allowAll()
                    );
                    recordOutcome(player, actionOutcome);
                    return actionOutcome;
                },
                identity,
                "slot_workspace.forge");
    }

    private static boolean missingFromCarry(WorkspaceCommandOutcome outcome) {
        return outcome != null
                && !outcome.success()
                && WorkspaceBeltCommandService.CARRIED_IDENTITY_NOT_FOUND.equals(outcome.diagnostics());
    }

    private static boolean tookStackForHotbar(WorkspaceCommandOutcome outcome) {
        if (outcome == null || !outcome.success()) {
            return false;
        }
        return "took_one".equals(outcome.status())
                || "took_stack".equals(outcome.status())
                || "took_partial".equals(outcome.status());
    }

    private void recordHotbarPlacementOnSuccess(ServerPlayer player, int hotbarIndex, WorkspaceCommandOutcome outcome) {
        HotbarSlotRecencyRegistry.recordPlacementOnSuccess(
                player,
                hotbarIndex,
                outcome);
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
                        ForgeCarriedActivityTracker.suppressOutcome(player, outcome);
                    }
                });
    }

    private WorkspaceCommandOutcome gatherActiveKit(ServerPlayer player) {
        KitGatherService.Outcome outcome = KitGatherService.gatherActiveKit(player, runtime);
        reapplyActiveKitFromCarry(player, runtime);
        return KitGatherService.toWorkspaceOutcome(outcome);
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
        ForgeCarriedActivityTracker.suppressNext(player);
        ForgeCarriedActivityTracker.markDirty(player, "workspace_take");
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
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);
        SlotWorkspaceCommandService.reapplyActiveKit(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor(runtime, host, player));
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
            ForgeCarriedActivityTracker.suppressOutcome(player, outcome);
        }
    }

    private InventoryAuthoritySnapshot refreshViewBeforeCommand(ServerPlayer player) {
        if (player != null) {
            InventoryHostDescriptor host = resolveHost(player);
            InventoryAuthoritySnapshot authority = host == null
                    ? InventoryAuthoritySnapshot.empty()
                    : InventoryAuthorityReadService.serverAuthority(player, host);
            project(player);
            return authority;
        }
        return InventoryAuthoritySnapshot.empty();
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
