package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.SlotDiagnostics;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.core.ItemStackStructuralKey;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.storage.CarriedInventoryRevisions;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.ActiveChestPanelProjectionSupport;
import dev.imagio.slot.inventory.workspace.ChestContentAffinitySeeder;
import dev.imagio.slot.inventory.workspace.HotbarSlotRecencyRegistry;
import dev.imagio.slot.inventory.workspace.KitGatherService;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.LootChestProjectionSupport;
import dev.imagio.slot.inventory.workspace.QuickHotbarSwapHistory;
import dev.imagio.slot.inventory.workspace.RemoteDetailIdentityPayload;
import dev.imagio.slot.inventory.workspace.RemoteStorageDetailIntent;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.FluidResourceObservationService;
import dev.imagio.slot.inventory.workspace.WorkspaceBeltCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestProjectionSupport;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceCraftRunCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceCursorCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceHotbarSlotReverser;
import dev.imagio.slot.inventory.workspace.WorkspaceAuthorityInvalidations;
import dev.imagio.slot.inventory.workspace.WorkspaceInvalidation;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionRequest;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionResult;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionSessionCache;
import dev.imagio.slot.inventory.workspace.WorkspaceProjectionTiming;
import dev.imagio.slot.inventory.workspace.WorkspaceProximityInvalidations;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageIndex;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageIndexCache;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageRoutingContext;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageMemoryStore;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.inventory.workspace.WorkspaceWorkflowInvalidations;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.neoforge.storage.ChestDepositObserver;
import dev.imagio.slot.neoforge.storage.ChestStorageAnchors;
import dev.imagio.slot.neoforge.storage.ChestStorageIds;
import dev.imagio.slot.neoforge.storage.NeoForgeCarriedActivityTracker;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.ContextualSuggestionFeatureFlags;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

final class SlotWorkspaceUiSession {
    static final int TARGET_MAIN_SOURCE = 1;
    static final int TARGET_MAIN_SLOT = 2;
    static final int TARGET_HOTBAR_SLOT = 3;
    private static final long SLOW_REFRESH_NANOS = 75_000_000L;

    private final Player player;
    private final LearnedIslandRuleStore learnedRules = new LearnedIslandRuleStore();
    private final WorkspaceProjectionSessionCache projectionCache = new WorkspaceProjectionSessionCache();
    private final WorkspaceStorageIndexCache storageIndexCache = new WorkspaceStorageIndexCache();
    private final SlotWorkspaceViewModelCodec.EncodedSliceCache encodedSliceCache =
            new SlotWorkspaceViewModelCodec.EncodedSliceCache();
    private final java.util.List<WorkspaceInvalidation> pendingInvalidations = new java.util.ArrayList<>();
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private String lastContentFingerprint = "";
    private CompoundTag lastViewTag;
    private long lastObservedWorkflowSequence = Long.MIN_VALUE;
    private long lastObservedCarriedRevision = Long.MIN_VALUE;
    private InventoryAuthoritySnapshot lastProjectedAuthority;
    private WorkflowDomainSnapshot lastProjectedWorkflowSnapshot;
    private Set<String> lastObservedProximateStorageIds = Set.of();
    private Set<String> lastObservedContextualStorageIds = Set.of();
    private int lastStructuralMenuId = Integer.MIN_VALUE;
    private List<ItemStackStructuralKey> lastStructuralMenuKeys = List.of();
    private ItemStackStructuralKey lastStructuralCursorKey = ItemStackStructuralKey.EMPTY;
    private long nextRevision = 1L;
    private String status = "ready";
    private String diagnostics = "";
    /**
     * Identities the auto-home pipeline has already attempted in this
     * session, so we don't replay the same suggest-and-assign work on
     * every refresh. Each {@code assignHome} hits the persistence
     * service synchronously; without this gate the first refresh after
     * opening a screen with a full inventory would hang the server tick
     * long enough that the player's close-screen packet never gets
     * processed. The set lives for the lifetime of the session — once
     * persistence is in place, future sessions inherit the assignments
     * via the workflow snapshot.
     */
    private final java.util.Set<ItemIdentity> autoHomeAttempted = new java.util.HashSet<>();
    /**
     * Mirror of the client-side search query, pushed via
     * {@link #setSearchQuery}. Drives server-side gating of the remote-only
     * ghost synthesis: those ghosts pollute the atlas full-time if always on,
     * but disappearing them under no search blocks search-as-find. Storing
     * the active query lets the projection conditionally synthesize matching
     * ghosts only when the player is searching.
     */
    private String searchQuery = "";
    private RemoteStorageDetailIntent remoteStorageDetailIntent = RemoteStorageDetailIntent.INTENT_ONLY;
    private Set<ItemIdentity> remoteRecipeIngredientIdentities = Set.of();

    /**
     * Origin stamp for the current cursor stack. Set whenever a SLOT-
     * initiated pickup writes to {@code menu.setCarried(...)}; cleared on
     * any drop / cancel that empties the cursor. Phase B's right-click
     * cancel reads this to route the cursor stack back to its source
     * (so eager-from-chest pickups reverse cleanly into the chest rather
     * than dump into player inventory). When {@code menu.getCarried()} is
     * non-empty but origin is null, vanilla put the cursor there directly
     * (player clicked a vanilla slot without going through SLOT) — Phase B
     * routes that case through smart-deposit.
     */
    private WorkspaceCursorCommandService.CursorOrigin cursorOrigin = null;

    SlotWorkspaceUiSession(Player player) {
        this.player = player;
    }

    SlotWorkspaceViewModel viewModel() {
        return viewModel;
    }

    public SlotWorkspaceViewModel currentViewModel() {
        return viewModel;
    }

    Tag viewTag() {
        if (player instanceof ServerPlayer serverPlayer) {
            if (shouldRefreshServerView(serverPlayer)) {
                refreshServerView(serverPlayer);
            }
        }
        return lastViewTag == null ? SlotWorkspaceViewModelCodec.encode(viewModel, player.registryAccess()) : lastViewTag.copy();
    }

    void acceptRemoteView(Tag tag) {
        SlotWorkspaceViewModelCodec.TransferApplyResult applied =
                SlotWorkspaceViewModelCodec.applyTransfer(player.registryAccess(), lastViewTag, tag);
        if (!applied.applied()) {
            dev.imagio.slot.SlotCommon.LOGGER.warn(
                    "Rejected NeoForge workspace view transfer: diagnostics={}",
                    applied.diagnostics());
            if (applied.requiresFullSnapshot()) {
                lastViewTag = null;
            }
            return;
        }
        lastViewTag = applied.fullTag();
        viewModel = applied.viewModel();
    }

    void pickupToCursor(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer count
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        applyCursorOutcome(serverPlayer, WorkspaceCursorCommandService.pickupToCursor(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                count));
    }

    void cursorCancel() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyCursorOutcome(serverPlayer, WorkspaceCursorCommandService.cursorCancel(
                serverPlayer,
                workflowRuntime(serverPlayer),
                cursorOrigin));
    }

    void cursorSmartDeposit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyCursorOutcome(serverPlayer, WorkspaceCursorCommandService.cursorSmartDeposit(
                serverPlayer,
                workflowRuntime(serverPlayer),
                cursorOrigin));
    }

    void dropCursorAtHotbar(Integer hotbarIndex, Integer button) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyCursorOutcome(serverPlayer, WorkspaceCursorCommandService.dropCursorAtHotbar(
                serverPlayer,
                cursorOrigin,
                hotbarIndex,
                button));
    }

    void dropCursorIntoChest(String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyCursorOutcome(serverPlayer, WorkspaceCursorCommandService.dropCursorIntoChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                cursorOrigin,
                storageIdRaw));
    }

    /**
     * Cross-surface: a wall card was drag-released over a vanilla slot
     * in the host menu (chest, crafting input, machine input). Find a
     * player-inventory slot inside the host menu carrying the same
     * identity, then synthesize the vanilla two-click sequence
     * {@code PICKUP source → PICKUP target} so the host menu's own
     * {@code Slot.mayPlace} / {@code Slot.safeInsert} logic governs
     * the move (so e.g. crafting input slot's max-stack-size-1 still
     * applies and machine input filters reject what they reject).
     *
     * <p>If the target rejects part of the stack the leftover stays on
     * the menu's cursor; we PICKUP the source again to put it back,
     * and as a final fallback drop any irreducible leftover into the
     * player inventory.
     */
    void crossSurfaceDropOnHostSlot(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer hostSlotIndex
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceCursorCommandService.crossSurfaceDropOnHostSlot(
                serverPlayer,
                resolveIdentity(itemId, comparisonMode, componentFingerprint),
                hostSlotIndex));
    }

    /**
     * Cross-surface host quick-move for atlas cards. Moves carried
     * stacks matching the identity into non-player slots exposed by the
     * current host menu (crafting matrix, machine inputs, chest slots,
     * etc.), repeated up to {@code count} stacks.
     *
     * <p>Bails when no further player slot carries the identity, or
     * when a quickMove call results in no change — the latter prevents
     * an infinite loop if the host's {@code quickMoveStack} returns
     * the source unchanged (e.g. nothing accepts it).
     */
    void crossSurfaceQuickMoveAtlas(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer count
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceCursorCommandService.crossSurfaceQuickMoveAtlas(
                serverPlayer,
                resolveIdentity(itemId, comparisonMode, componentFingerprint),
                count));
    }

    void crossSurfaceQuickMoveHotbar(Integer hotbarIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceCursorCommandService.crossSurfaceQuickMoveHotbar(
                serverPlayer,
                hotbarIndex));
    }

    void transfer(Integer sourceKind, Integer sourceIndex, Integer destinationKind, Integer destinationIndex, String origin) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryActionTarget source = target(sourceKind, sourceIndex, true);
        InventoryActionTarget destination = target(destinationKind, destinationIndex, false);
        if (source == null || destination == null) {
            SlotDiagnostics.workspaceTransferInvalid(sourceKind, sourceIndex, destinationKind, destinationIndex, origin);
            reject("invalid_transfer_target");
            return;
        }
        WorkspaceTransferExecution execution = executeTransfer(serverPlayer, source, destination, origin);
        status = execution.feedback().status();
        diagnostics = execution.feedback().diagnostics();
        broadcast(serverPlayer);
    }

    void assignHome(String itemId, String comparisonMode, String componentFingerprint, String islandId, Integer ordinal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.assignHome(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                authority,
                itemId,
                comparisonMode,
                componentFingerprint,
                islandId,
                ordinal
        );
        applyOutcome(serverPlayer, outcome);
    }

    void acceptChip(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String chipIslandId,
            String templateName
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.acceptChip(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                itemId,
                comparisonMode,
                componentFingerprint,
                chipIslandId,
                templateName
        );
        applyOutcome(serverPlayer, outcome);
    }

    void createNamedIslandForItem(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String label,
            Integer color,
            Integer worldX,
            Integer worldY
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.createNamedIslandForItem(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                itemId,
                comparisonMode,
                componentFingerprint,
                label,
                color,
                worldX,
                worldY
        );
        applyOutcome(serverPlayer, outcome);
    }

    void moveIsland(String islandId, Double worldX, Double worldY) {
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] session.moveIsland received id={} requestedX={} requestedY={}",
                islandId, worldX, worldY);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.moveIsland(
                workflowRuntime(serverPlayer),
                viewModel,
                islandId,
                worldX,
                worldY
        );
        if (outcome.success()) {
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] session.moveIsland applied id={} newPos=({},{})",
                    islandId, worldX, worldY);
        }
        applyOutcome(serverPlayer, outcome);
    }

    void reorderIsland(String islandId, Integer targetIndex) {
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] session.reorderIsland received id={} targetIndex={}",
                islandId, targetIndex);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.reorderIsland(
                workflowRuntime(serverPlayer),
                viewModel,
                islandId,
                targetIndex
        );
        applyOutcome(serverPlayer, outcome);
    }

    void deposit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            dev.imagio.slot.SlotCommon.LOGGER.warn(
                    "[SLOT] deposit RPC received but player is not a ServerPlayer (player={})", player);
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, WorkspaceChestCommandService.deposit(
                serverPlayer,
                workflowRuntime(serverPlayer),
                authority));
    }

    void gatherActiveKit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        KitGatherService.Outcome gatherOutcome = KitGatherService.gatherActiveKit(
                serverPlayer,
                workflowRuntime(serverPlayer));
        reapplyActiveKitFromCarry(serverPlayer);
        applyOutcome(serverPlayer, KitGatherService.toWorkspaceOutcome(gatherOutcome));
    }

    /**
     * Push the active client-side search query into the session so the
     * next view-model projection can gate remote-only ghost synthesis on
     * it. Without this hook, server has no way to know whether the
     * player is currently searching, and would either always or never
     * synthesize the ghosts.
     */
    void setSearchQuery(String query) {
        String normalized = WorkspaceSearchQuery.cleanInput(query);
        if (normalized.equals(searchQuery)) {
            return;
        }
        String previous = searchQuery;
        searchQuery = normalized;
        if (player instanceof ServerPlayer serverPlayer) {
            queueInvalidation(searchQueryInvalidation(previous, searchQuery));
            broadcast(serverPlayer);
        }
    }

    private WorkspaceInvalidation searchQueryInvalidation(String previousQuery, String nextQuery) {
        RemoteStorageDetailIntent previousIntent =
                RemoteStorageDetailIntent.effective(remoteStorageDetailIntent, previousQuery);
        RemoteStorageDetailIntent nextIntent =
                RemoteStorageDetailIntent.effective(remoteStorageDetailIntent, nextQuery);
        if (previousIntent == RemoteStorageDetailIntent.SEARCH || nextIntent == RemoteStorageDetailIntent.SEARCH) {
            return new WorkspaceInvalidation(
                    WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED,
                    java.util.Set.of(),
                    java.util.Set.of(),
                    java.util.Set.of(),
                    java.util.EnumSet.of(
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.CARD,
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.SECTION,
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.FRAME),
                    false,
                    WorkspaceSearchQuery.normalized(nextQuery).isBlank()
                            ? "remote_search_query_cleared"
                            : "remote_search_query_changed");
        }
        return WorkspaceInvalidation.frame(
                WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED,
                "local_search_query_changed");
    }

    void setRemoteStorageDetailIntent(String value) {
        RemoteStorageDetailIntent intent = RemoteStorageDetailIntent.parse(value);
        if (intent == remoteStorageDetailIntent) {
            return;
        }
        remoteStorageDetailIntent = intent;
        if (player instanceof ServerPlayer serverPlayer) {
            queueInvalidation(new WorkspaceInvalidation(
                    WorkspaceInvalidation.Reason.REMOTE_STORAGE_DETAIL_CHANGED,
                    java.util.Set.of(),
                    java.util.Set.of(),
                    java.util.Set.of(),
                    java.util.EnumSet.of(
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.CARD,
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.SECTION,
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.FRAME),
                    false,
                    "remote_detail_changed"));
            broadcast(serverPlayer);
        }
    }

    void setRecipeIngredientFilter(String payload) {
        Set<ItemIdentity> identities = RemoteDetailIdentityPayload.decode(payload);
        if (identities.equals(remoteRecipeIngredientIdentities)) {
            return;
        }
        remoteRecipeIngredientIdentities = identities;
        if (player instanceof ServerPlayer serverPlayer) {
            queueInvalidation(new WorkspaceInvalidation(
                    WorkspaceInvalidation.Reason.REMOTE_STORAGE_DETAIL_CHANGED,
                    identities,
                    java.util.Set.of(),
                    java.util.Set.of(),
                    java.util.EnumSet.of(
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.CARD,
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.SECTION,
                            dev.imagio.slot.inventory.workspace.WorkspaceProjectionSlice.FRAME),
                    true,
                    identities.isEmpty()
                            ? "recipe_ingredient_detail_cleared"
                            : "recipe_ingredient_detail_changed"));
            broadcast(serverPlayer);
        }
    }

    void stageCraftRunEntry(String entryId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, WorkspaceCraftRunCommandService.stageEntry(
                workflowRuntime(serverPlayer),
                authority,
                entryId,
                request -> {
                    InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                            host,
                            serverPlayer,
                            request,
                            ProtectionPolicy.allowAll());
                    recordOutcome(serverPlayer, outcome);
                    return outcome;
                },
                "slot_workspace.neoforge"));
    }

    void adjustCraftRunEntry(String entryId, Integer delta) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceCraftRunCommandService.adjustEntry(
                workflowRuntime(serverPlayer),
                entryId,
                delta == null ? 0 : delta));
    }

    void selectCraftRunIngredient(
            String entryId,
            String groupId,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceCraftRunCommandService.selectIngredientAlternative(
                workflowRuntime(serverPlayer),
                entryId,
                groupId,
                identity(itemId, comparisonMode, componentFingerprint)));
    }

    void removeCraftRunEntry(String entryId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceCraftRunCommandService.removeEntry(
                workflowRuntime(serverPlayer),
                entryId));
    }

    /**
     * Claim the chest at {@code (x, y, z)} in {@code dimensionId}. Driven
     * by the active-chest strip's "Claim" button when the player is
     * looking at an unclaimed chest. Routes through
     * {@link ChestDepositObserver#resolveOrCreateClaim} so a double chest
     * folds both halves into one storage UUID and stamps the
     * {@code slot:storage_id} attachment on the partner — matching what
     * the deposit observer produces on a real deposit.
     */
    void claimChestAtPos(String dimensionId, Integer x, Integer y, Integer z) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || dimensionId == null || dimensionId.isBlank()
                || x == null || y == null || z == null) {
            reject("invalid_claim_request");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("dimension_mismatch");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("not_claimable");
            return;
        }
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(level, pos);
        if (anchor == null) {
            reject("anchor_resolution_failed");
            return;
        }
        UUID storageId = ChestDepositObserver.resolveOrCreateClaim(
                workflowRuntime(serverPlayer).chestClaimWorkflow(), level, pos, anchor);
        if (storageId == null) {
            reject("claim_failed");
            return;
        }
        seedClaimedChestContents(serverPlayer, storageId);
        queueInvalidation(SlotWorkspaceCommandService.localizedChestClaimInvalidation(storageId.toString()));
        status = "chest_claimed";
        diagnostics = "";
        broadcast(serverPlayer);
    }

    void setChestRoleAtPos(String dimensionId, Integer x, Integer y, Integer z, String roleName) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || dimensionId == null || dimensionId.isBlank()
                || x == null || y == null || z == null) {
            reject("invalid_chest_role_request");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("dimension_mismatch");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("not_claimable");
            return;
        }
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(level, pos);
        if (anchor == null) {
            reject("anchor_resolution_failed");
            return;
        }
        ChestRole role = ChestRole.parse(roleName);
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChest existing = runtime.chestClaimWorkflow().chestByAnchor(anchor);
        UUID storageId = existing == null
                ? role == ChestRole.IGNORE ? null : ChestDepositObserver.resolveOrCreateClaim(
                        runtime.chestClaimWorkflow(), level, pos, anchor)
                : existing.storageId();
        if (storageId == null) {
            queueInvalidation(WorkspaceInvalidation.frame(
                    WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                    "unclaimed_chest_ignore_noop"));
            status = "chest_role_set";
            diagnostics = ChestRole.IGNORE.name();
            broadcast(serverPlayer);
            return;
        }
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setChestRole(runtime, storageId, role);
        if (outcome.success() && role == ChestRole.STORAGE) {
            seedClaimedChestContents(serverPlayer, storageId);
        }
        applyOutcome(serverPlayer, outcome);
    }

    void forgetItemAffinity(String storageId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.forgetItemAffinity(
                workflowRuntime(serverPlayer),
                storageId,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    /**
     * Take every stack from a loot (unclaimed) chest at the supplied
     * world position into the player's inventory. Auto-accepts the top
     * chip suggestion for any unhomed identity in the chest before
     * taking, so items land at their suggested home instead of in
     * Triage. Used by the atlas-side loot panel's "Take all" button.
     */
    void takeAllFromLootChest(String dimensionId, Integer x, Integer y, Integer z) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // Refresh so viewModel.lootChestPanel().items() reflects the
        // current chip suggestions for this chest.
        refreshServerView(serverPlayer);
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.lootChestPanel().items()) {
            if (item.playerPlaced()) {
                continue;
            }
            // Skip items already at a non-Triage home — homed items will
            // be routed by deposit flow naturally.
            if (!item.islandId().isBlank()
                    && !item.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE)) {
                continue;
            }
            if (item.chipSuggestions().isEmpty()) {
                continue;
            }
            ChipSuggestion top = item.chipSuggestions().get(0);
            acceptChip(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint(),
                    top.islandId(),
                    top.template() == null ? "" : top.template().name()
            );
        }
        takeFromLootChest(dimensionId, x, y, z, null, null, null);
    }

    /**
     * Take every stack matching {@code identity} from a loot chest into
     * the player's inventory. Combined with a chip-accept (sent first
     * by the client), this performs the "shift+click row → home + take"
     * gesture.
     */
    void takeIdentityFromLootChest(
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        takeFromLootChest(dimensionId, x, y, z, itemId, comparisonMode, componentFingerprint);
    }

    /**
     * Drag-carried-onto-loot-panel claim+deposit: auto-claims the
     * unclaimed chest at the loot-panel position and immediately
     * deposits the carried identity into it. Lets the player bootstrap
     * a claim from inside the SLOT workspace without going through the
     * vanilla chest GUI — answers the "I right-clicked an unclaimed
     * chest, the loot panel is showing, but I can't deposit anything
     * into it without opening vanilla" failure mode.
     */
    void claimAndDepositCarriedToLootChest(
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (dimensionId == null || x == null || y == null || z == null) {
            reject("invalid_loot_chest_pos");
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("loot_chest_other_dimension");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        long radiusSq = (long) LootChestProjectionSupport.DEFAULT_RADIUS_BLOCKS
                * LootChestProjectionSupport.DEFAULT_RADIUS_BLOCKS;
        BlockPos playerPos = serverPlayer.blockPosition();
        long dx = (long) playerPos.getX() - pos.getX();
        long dy = (long) playerPos.getY() - pos.getY();
        long dz = (long) playerPos.getZ() - pos.getZ();
        if (dx * dx + dy * dy + dz * dz > radiusSq) {
            reject("loot_chest_out_of_range");
            return;
        }
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("loot_chest_not_claimable");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        var chestService = runtime.chestClaimWorkflow();
        var anchor = ChestStorageAnchors.toAnchor(level, pos);
        UUID storageId = ChestDepositObserver.resolveOrCreateClaim(chestService, level, pos, anchor);
        if (storageId == null) {
            reject("loot_chest_claim_failed");
            return;
        }
        seedClaimedChestContents(serverPlayer, storageId);
        ClaimedChest claim = chestService.claimedChestMap().chest(storageId);
        if (claim == null) {
            reject("loot_chest_claim_missing");
            return;
        }
        WorkspaceCommandOutcome depositOutcome = WorkspaceChestCommandService.depositCarriedToChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                claim.storageId().toString());
        if (!depositOutcome.success() && "nothing_to_deposit".equals(depositOutcome.diagnostics())) {
            // Claim still stands — the player has now made this chest
            // a storage chest even if they had nothing in carry to deposit.
            // Surface the partial success so the panel refreshes.
            queueInvalidation(SlotWorkspaceCommandService.localizedChestClaimInvalidation(storageId.toString()));
            status = "claimed";
            diagnostics = "claimed_no_deposit_source";
            broadcast(serverPlayer);
            return;
        }
        applyOutcome(serverPlayer, depositOutcome);
    }

    /**
     * "Open vanilla chest GUI here" escape hatch from the loot panel:
     * the right-click intercept redirects every unclaimed-chest open into
     * the SLOT atlas, leaving no path to actually deposit into a loot
     * chest. Wiring the loot panel's button to this RPC opens the
     * vanilla chest UI server-side so the player can drop items in;
     * {@link ChestDepositObserver} then auto-claims the chest and bumps
     * affinity on close. Validates the chest is still unclaimed and in
     * range before opening; rejects otherwise so a stale view-model row
     * can't be used to teleport the open into the wrong target.
     */
    void openVanillaForLootChest(String dimensionId, Integer x, Integer y, Integer z) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (dimensionId == null || x == null || y == null || z == null) {
            reject("invalid_loot_chest_pos");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("loot_chest_other_dimension");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        long radiusSq = (long) LootChestProjectionSupport.DEFAULT_RADIUS_BLOCKS
                * LootChestProjectionSupport.DEFAULT_RADIUS_BLOCKS;
        BlockPos playerPos = serverPlayer.blockPosition();
        long dx = (long) playerPos.getX() - pos.getX();
        long dy = (long) playerPos.getY() - pos.getY();
        long dz = (long) playerPos.getZ() - pos.getZ();
        if (dx * dx + dy * dy + dz * dz > radiusSq) {
            reject("loot_chest_out_of_range");
            return;
        }
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("loot_chest_not_claimable");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedMap = runtime.chestClaimWorkflow().claimedChestMap();
        if (ChestStorageIds.read(level, pos).isPresent()
                || claimedMap.chestByAnchor(ChestStorageAnchors.toAnchor(level, pos)) != null) {
            reject("loot_chest_already_claimed");
            return;
        }
        BlockState state = level.getBlockState(pos);
        MenuProvider provider = state.getMenuProvider(level, pos);
        if (provider == null) {
            reject("loot_chest_no_menu");
            return;
        }
        // Mark the synthetic open as authorized so the existing observer
        // snapshots the chest's contents and runs its auto-claim path on
        // close — without this, openMenu fires onContainerOpen with no
        // PENDING entry and the deposit is silently ignored.
        ChestDepositObserver.expectOpen(serverPlayer, pos);
        serverPlayer.openMenu(provider);
    }

    private void takeFromLootChest(
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (dimensionId == null || x == null || y == null || z == null) {
            reject("invalid_loot_chest_pos");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("loot_chest_other_dimension");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        long radiusSq = (long) LootChestProjectionSupport.DEFAULT_RADIUS_BLOCKS
                * LootChestProjectionSupport.DEFAULT_RADIUS_BLOCKS;
        BlockPos playerPos = serverPlayer.blockPosition();
        long dx = (long) playerPos.getX() - pos.getX();
        long dy = (long) playerPos.getY() - pos.getY();
        long dz = (long) playerPos.getZ() - pos.getZ();
        if (dx * dx + dy * dy + dz * dz > radiusSq) {
            reject("loot_chest_out_of_range");
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Container container) || !ChestStorageAnchors.isClaimable(level, pos)) {
            reject("loot_chest_not_a_chest");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedMap = runtime.chestClaimWorkflow().claimedChestMap();
        if (ChestStorageIds.read(be).isPresent() || claimedMap.chestByAnchor(
                ChestStorageAnchors.toAnchor(level, pos)) != null) {
            reject("loot_chest_already_claimed");
            return;
        }
        ItemIdentity targetIdentity = null;
        if (itemId != null && !itemId.isBlank()) {
            ItemComparisonMode mode = ItemComparisonMode.ITEM_ID;
            if (comparisonMode != null) {
                try {
                    mode = ItemComparisonMode.valueOf(comparisonMode);
                } catch (IllegalArgumentException ignored) {
                }
            }
            targetIdentity = new ItemIdentity(
                    itemId,
                    mode,
                    componentFingerprint == null ? "" : componentFingerprint
            );
        }
        int containerSize = container.getContainerSize();
        int taken = 0;
        for (int slotIndex = 0; slotIndex < containerSize; slotIndex++) {
            ItemStack stack = container.getItem(slotIndex);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (targetIdentity != null) {
                ItemIdentity stackIdentity = ItemIdentityMatcher.create(stack);
                if (!stackIdentity.equals(targetIdentity)) {
                    continue;
                }
            }
            ItemStack copy = stack.copy();
            ItemStack removed = container.removeItem(slotIndex, copy.getCount());
            if (removed == null || removed.isEmpty()) {
                continue;
            }
            ItemStack remaining = StorageAccessRegistry.carriedSourceAccess()
                    .insertBestFit(serverPlayer, removed, false);
            int placed = (removed.getCount() - (remaining == null || remaining.isEmpty() ? 0 : remaining.getCount()));
            if (placed <= 0) {
                // Carry full — put back what wasn't placed.
                container.setItem(slotIndex, removed);
                break;
            }
            taken += placed;
            if (remaining != null && !remaining.isEmpty()) {
                container.setItem(slotIndex, remaining);
                break;
            }
        }
        if (taken > 0) {
            container.setChanged();
            status = "looted";
            diagnostics = "taken=" + taken;
            reapplyActiveKitFromCarry(serverPlayer);
        } else {
            status = "nothing_to_take";
            diagnostics = "";
        }
        broadcast(serverPlayer);
    }

    void takeAllFromChest(String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyTakeOutcome(serverPlayer, WorkspaceChestCommandService.takeAllFromChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                storageIdRaw));
    }

    void relabelChest(String storageId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.relabelChest(
                workflowRuntime(serverPlayer),
                viewModel,
                storageId,
                label
        ));
    }

    void moveChest(String storageId, Integer atlasX, Integer atlasY) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.moveChest(
                workflowRuntime(serverPlayer),
                viewModel,
                storageId,
                atlasX,
                atlasY
        ));
    }

    void renameIsland(String islandId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.renameIsland(
                workflowRuntime(serverPlayer),
                islandId,
                label
        ));
    }

    void recolorIsland(String islandId, Integer color) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.recolorIsland(
                workflowRuntime(serverPlayer),
                islandId,
                color
        ));
    }

    void setIslandIcon(String islandId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setIslandIcon(
                workflowRuntime(serverPlayer),
                islandId,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    void deleteIsland(String islandId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deleteIsland(
                workflowRuntime(serverPlayer),
                islandId
        );
        if (outcome.success()) {
            autoHomeAttempted.clear();
        }
        applyOutcome(serverPlayer, outcome);
    }

    void saveBeltAsKit(String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.saveBeltAsKit(
                workflowRuntime(serverPlayer),
                authority,
                KIT_IDENTITY_RESOLVER,
                name
        ));
    }

    void createWorkflowTab(String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.createWorkflowTab(
                workflowRuntime(serverPlayer),
                name
        ));
    }

    void createKitVariant(String parentKitId, String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.createKitVariant(
                workflowRuntime(serverPlayer),
                parentKitId,
                name
        ));
    }

    void activateKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(serverPlayer, outcome);
            return outcome;
        };
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.activateKit(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                kitId
        ));
    }

    /**
     * Re-run the active kit's plan against the current inventory.
     * Called after any path that adds items into carry (chest take,
     * deposit, ground pickup), so a freshly-acquired item that matches
     * a still-empty kit slot snaps into that slot instead of waiting
     * for the next manual activation.
     */
    private void reapplyActiveKitFromCarry(ServerPlayer serverPlayer) {
        if (!workflowRuntime(serverPlayer).kitWorkflow().activation().isActive()) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(serverPlayer, outcome);
            return outcome;
        };
        SlotWorkspaceCommandService.reapplyActiveKit(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor
        );
    }

    void deactivateKit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deactivateKit(
                workflowRuntime(serverPlayer)
        ));
    }

    void performUndo() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.performUndo(
                workflowRuntime(serverPlayer)
        ));
    }

    void performRedo() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.performRedo(
                workflowRuntime(serverPlayer)
        ));
    }

    void renameCluster(String clusterId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.relabelCluster(
                workflowRuntime(serverPlayer),
                clusterId,
                label
        ));
    }

    void renameKit(String kitId, String newName) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.renameKit(
                workflowRuntime(serverPlayer),
                kitId,
                newName
        ));
    }

    void duplicateKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.duplicateKit(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void reorderKit(String kitId, Integer targetIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.reorderKit(
                workflowRuntime(serverPlayer),
                kitId,
                targetIndex == null ? -1 : targetIndex
        ));
    }

    void deleteKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deleteKit(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void switchKitPage(Integer direction) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(serverPlayer, outcome);
            return outcome;
        };
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.switchKitPage(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                direction == null ? 1 : direction
        ));
    }

    void addKitPage(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.addKitPage(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void removeKitPage(String kitId, Integer pageIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.removeKitPage(
                workflowRuntime(serverPlayer),
                kitId,
                pageIndex == null ? -1 : pageIndex
        ));
    }

    void setKitMember(
            String kitId,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer member
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setKitMember(
                workflowRuntime(serverPlayer),
                kitId,
                itemId,
                comparisonMode,
                componentFingerprint,
                member == null ? 0 : member
        ));
    }

    void setKitAcceptedInput(
            String kitId,
            String kind,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String tagId,
            Integer accepted
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setKitAcceptedInput(
                workflowRuntime(serverPlayer),
                kitId,
                kind,
                itemId,
                comparisonMode,
                componentFingerprint,
                tagId,
                accepted == null ? 0 : accepted
        ));
    }

    /**
     * Set or clear a kit-scoped desired count for an explicit kitId
     * (which may not be the active kit). Replaces the legacy
     * addKitBring/removeKitBring pair — count=1 seeds the standing
     * order, count=0 clears it.
     */
    void setKitScopedDesiredCount(
            String kitId,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer countBoxed
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setKitScopedDesiredCount(
                workflowRuntime(serverPlayer),
                kitId,
                itemId,
                comparisonMode,
                componentFingerprint,
                countBoxed == null ? 0 : countBoxed
        ));
    }

    void swapKitSlots(String kitId, Integer pageIndex, Integer fromIndex, Integer toIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.swapKitSlots(
                workflowRuntime(serverPlayer),
                kitId,
                pageIndex == null ? -1 : pageIndex,
                fromIndex == null ? -1 : fromIndex,
                toIndex == null ? -1 : toIndex
        ));
    }

    void setKitSlotIdentity(
            String kitId,
            Integer pageIndex,
            Integer slotIndex,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // Belt sync only fires when the edit lands on the active kit's active page, and
        // only when we can resolve an inventory host to execute live mutations through.
        // On host-resolution failure the command falls back to definition-only update.
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = host == null
                ? null
                : request -> {
                    InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                            host,
                            serverPlayer,
                            request,
                            ProtectionPolicy.allowAll()
                    );
                    recordOutcome(serverPlayer, outcome);
                    return outcome;
                };
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setKitSlotIdentity(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                kitId,
                pageIndex == null ? -1 : pageIndex,
                slotIndex == null ? -1 : slotIndex,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    void returnHotbarToHome(Integer hotbarIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.returnHotbarToHome(
                serverPlayer,
                workflowRuntime(serverPlayer),
                viewModel,
                hotbarIndex,
                (source, destination, origin) -> executeTransfer(serverPlayer, source, destination, origin),
                "slot_workspace.ldlib"));
    }

    void assignHomeToFreeHotbar(String itemId, String comparisonMode, String componentFingerprint) {
        assignHomeToFreeHotbar(itemId, comparisonMode, componentFingerprint, false);
    }

    void assignHomeToHotbarOnly(String itemId, String comparisonMode, String componentFingerprint) {
        assignHomeToFreeHotbar(itemId, comparisonMode, componentFingerprint, true);
    }

    private void assignHomeToFreeHotbar(String itemId, String comparisonMode, String componentFingerprint, boolean suppressChestPreference) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.assignHomeToFreeHotbar(
                serverPlayer,
                workflowRuntime(serverPlayer),
                viewModel,
                identity,
                suppressChestPreference,
                resolveHost(serverPlayer),
                targetHotbarIndex -> assignIdentityToHotbarIndex(serverPlayer, identity, targetHotbarIndex)));
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarIndex(ServerPlayer serverPlayer, ItemIdentity identity, int hotbarIndex) {
        ItemStack hotbarBefore = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
        if (located.isEmpty()) {
            WorkspaceCommandOutcome takeOutcome = WorkspaceChestCommandService.takeByIdentity(
                    serverPlayer,
                    workflowRuntime(serverPlayer),
                    identity,
                    Integer.MAX_VALUE,
                    false,
                    resolveHost(serverPlayer));
            if (!tookStackForHotbar(takeOutcome)) {
                return takeOutcome.success()
                        ? WorkspaceCommandOutcome.rejected(WorkspaceBeltCommandService.CARRIED_IDENTITY_NOT_FOUND)
                        : takeOutcome;
            }
            WorkspaceCommandOutcome outcome = assignIdentityToHotbarByTransfer(serverPlayer, hotbarIndex, identity);
            if (outcome.success()) {
                ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
                WorkspaceBeltCommandService.recordHotbarSlotUndo(
                        serverPlayer,
                        workflowRuntime(serverPlayer),
                        hotbarIndex,
                        hotbarBefore,
                        hotbarAfter,
                        "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
                recordHotbarPlacementOnSuccess(serverPlayer, hotbarIndex, outcome);
            }
            return outcome;
        }
        String sourceId = located.get().sourceId();
        int slotIndex = located.get().slotIndex();
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            WorkspaceTransferExecution execution = executeTransfer(
                    serverPlayer,
                    new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex),
                    new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, hotbarIndex),
                    "slot_workspace.ldlib.assign_identity_to_hotbar"
            );
            if (execution.appliedCompletely()) {
                ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
                WorkspaceBeltCommandService.recordHotbarSlotUndo(
                        serverPlayer,
                        workflowRuntime(serverPlayer),
                        hotbarIndex,
                        hotbarBefore,
                        hotbarAfter,
                        "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
                recordHotbarPlacementOnSuccess(serverPlayer, hotbarIndex, WorkspaceCommandOutcome.accepted("assigned", ""));
                return WorkspaceBeltCommandService.withCarriedIdentityInvalidation(
                        WorkspaceCommandOutcome.accepted(
                                "assigned_to_hotbar_" + (hotbarIndex + 1),
                                "moved to hotbar " + (hotbarIndex + 1)),
                        "assigned_to_hotbar",
                        identity,
                        hotbarBefore.isEmpty() ? null : ItemIdentityMatcher.create(hotbarBefore));
            } else if ("assign_requires_player_bound_targets".equals(execution.feedback().diagnostics())) {
                WorkspaceCommandOutcome outcome = assignIdentityToHotbarByTransfer(serverPlayer, hotbarIndex, identity);
                if (outcome.success()) {
                    ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
                    WorkspaceBeltCommandService.recordHotbarSlotUndo(
                            serverPlayer,
                            workflowRuntime(serverPlayer),
                            hotbarIndex,
                            hotbarBefore,
                            hotbarAfter,
                            "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
                    recordHotbarPlacementOnSuccess(serverPlayer, hotbarIndex, outcome);
                }
                return outcome;
            }
            return new WorkspaceCommandOutcome(
                    false,
                    execution.feedback().status(),
                    execution.feedback().diagnostics());
        }
        WorkspaceCommandOutcome outcome = assignIdentityToHotbarByTransfer(serverPlayer, hotbarIndex, identity);
        if (outcome.success()) {
            ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
            WorkspaceBeltCommandService.recordHotbarSlotUndo(
                    serverPlayer,
                    workflowRuntime(serverPlayer),
                    hotbarIndex,
                    hotbarBefore,
                    hotbarAfter,
                    "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
            recordHotbarPlacementOnSuccess(serverPlayer, hotbarIndex, outcome);
        }
        return outcome;
    }

    // Identity-based hotbar slot assignment. Unlike the slot-index-based
    // transfer path (TARGET_MAIN_SLOT + firstSlotIndex), this resolves the
    // source on the server by scanning every source tagged as CARRIED — so
    // an item that only lives in a Sophisticated Backpack is still reachable
    // from digit-press / drag-to-hotbar / click-hotbar-while-selected.
    void assignIdentityToHotbarSlot(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer hotbarIndexBoxed
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        int hotbarIndex = hotbarIndexBoxed == null ? -1 : hotbarIndexBoxed;
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }

        // If the source is a player slot (main or hotbar) the factory builds a single ASSIGN
        // request and the in-place swap path handles displacement. If it lives in a backpack
        // or any other non-player carried source, assignIdentityToHotbarIndex routes through
        // WorkspaceBeltCommandService's transfer/staging flow.
        applyOutcome(serverPlayer, assignIdentityToHotbarIndex(serverPlayer, identity, hotbarIndex));
    }

    void assignIdentityToAutoHotbar(
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.assignIdentityToAutoHotbar(
                viewModel,
                identity,
                HotbarSlotRecencyRegistry.recencySequence(serverPlayer),
                targetHotbarIndex -> assignIdentityToHotbarIndexAndRecordQuickSwap(serverPlayer, identity, targetHotbarIndex)));
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarIndexAndRecordQuickSwap(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            int hotbarIndex
    ) {
        ItemStack hotbarBefore = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
        WorkspaceCommandOutcome outcome = assignIdentityToHotbarIndex(serverPlayer, identity, hotbarIndex);
        if (outcome.success()) {
            ItemStack hotbarAfter = WorkspaceHotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
            QuickHotbarSwapHistory.recordSwap(
                    serverPlayer,
                    hotbarIndex,
                    hotbarBefore,
                    hotbarAfter,
                    "quick hotbar " + (hotbarIndex + 1));
        }
        return outcome;
    }

    void moveIdentityToMainInventory(
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(serverPlayer, outcome);
            return outcome;
        };
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.moveIdentityToMainInventory(
                serverPlayer,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                actionExecutor,
                identity,
                "slot_workspace.ldlib"));
    }

    void moveIdentityToBackpack(
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(serverPlayer, outcome);
            return outcome;
        };
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.moveIdentityToBackpack(
                serverPlayer,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                actionExecutor,
                identity,
                "slot_workspace.ldlib"));
    }

    private void recordHotbarPlacementOnSuccess(ServerPlayer serverPlayer, int hotbarIndex, WorkspaceCommandOutcome outcome) {
        HotbarSlotRecencyRegistry.recordPlacementOnSuccess(serverPlayer, hotbarIndex, outcome);
    }

    private static boolean tookStackForHotbar(WorkspaceCommandOutcome outcome) {
        if (outcome == null || !outcome.success()) {
            return false;
        }
        return "took_one".equals(outcome.status())
                || "took_stack".equals(outcome.status())
                || "took_partial".equals(outcome.status());
    }

    private WorkspaceCommandOutcome assignIdentityToHotbarByTransfer(
            ServerPlayer serverPlayer,
            int hotbarIndex,
            ItemIdentity identity
    ) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            recordOutcome(serverPlayer, outcome);
            return outcome;
        };
        return WorkspaceBeltCommandService.assignIdentityToHotbarByTransfer(
                serverPlayer,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                actionExecutor,
                identity,
                hotbarIndex,
                "slot_workspace.ldlib");
    }

    void depositHomeToLinkedChest(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceChestCommandService.depositIdentityToLinkedChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                WorkspaceChestCommandService.DepositQuantity.STACK,
                WorkspaceChestCommandService.DesiredCountPolicy.RESPECT,
                () -> activeDepositFallbackChest(serverPlayer),
                resolveHost(serverPlayer)));
    }

    void depositCarriedToChest(String itemId, String comparisonMode, String componentFingerprint, String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        applyOutcome(serverPlayer, WorkspaceChestCommandService.depositCarriedToChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                storageIdRaw));
    }

    void depositHotbarToChest(Integer hotbarIndex, String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, WorkspaceChestCommandService.depositHotbarToChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                hotbarIndex,
                storageIdRaw));
    }

    void takeOneFromChest(String storageIdRaw, Integer chestSlotIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyTakeOutcome(serverPlayer, WorkspaceChestCommandService.takeFromChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                storageIdRaw,
                chestSlotIndex,
                WorkspaceChestCommandService.TakeQuantity.ITEM));
    }

    void depositOneHomeToLinkedChest(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceChestCommandService.depositIdentityToLinkedChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                WorkspaceChestCommandService.DepositQuantity.ITEM,
                WorkspaceChestCommandService.DesiredCountPolicy.IGNORE,
                () -> activeDepositFallbackChest(serverPlayer),
                resolveHost(serverPlayer)));
    }

    void depositItemsHomeToLinkedChest(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer count
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceChestCommandService.depositIdentityCountToLinkedChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                count == null ? 0 : count,
                WorkspaceChestCommandService.DesiredCountPolicy.IGNORE,
                () -> activeDepositFallbackChest(serverPlayer),
                resolveHost(serverPlayer)));
    }

    /**
     * Set the active-scope desired count "I want N of this carried at all
     * times." Resolves scope server-side: kit-scoped if a kit is active,
     * else player-global. The client doesn't pick the scope so right-click
     * "Set desired count…" always edits whatever scope the pip is showing.
     * Count {@code 0} clears the entry.
     */
    void setPlayerDesiredCount(String itemId, String comparisonMode, String componentFingerprint, Integer countBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setPlayerDesiredCount(
                workflowRuntime(serverPlayer),
                itemId,
                comparisonMode,
                componentFingerprint,
                countBoxed == null ? 0 : countBoxed
        ));
    }

    /**
     * Bump the active-scope desired count by {@code delta}. Wired to
     * ctrl+scrollwheel on atlas cards. Routes to kit-scope when a kit is
     * active so the same gesture edits whatever the player sees on the
     * card. Negative deltas decrement; clamps to zero.
     */
    void adjustPlayerDesiredCount(String itemId, String comparisonMode, String componentFingerprint, Integer deltaBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.adjustPlayerDesiredCount(
                workflowRuntime(serverPlayer),
                itemId,
                comparisonMode,
                componentFingerprint,
                deltaBoxed == null ? 0 : deltaBoxed
        ));
    }

    void toggleWantedItem(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.toggleWantedCount(
                workflowRuntime(serverPlayer),
                authority,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    void setWantedCount(String itemId, String comparisonMode, String componentFingerprint, Integer countBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setWantedCount(
                workflowRuntime(serverPlayer),
                authority,
                itemId,
                comparisonMode,
                componentFingerprint,
                countBoxed == null ? 0 : countBoxed
        ));
    }

    void adjustWantedCount(String itemId, String comparisonMode, String componentFingerprint, Integer deltaBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.adjustWantedCount(
                workflowRuntime(serverPlayer),
                authority,
                itemId,
                comparisonMode,
                componentFingerprint,
                deltaBoxed == null ? 0 : deltaBoxed
        ));
    }

    void setJunk(String itemId, String comparisonMode, String componentFingerprint, Integer markedBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setJunk(
                workflowRuntime(serverPlayer),
                itemId,
                comparisonMode,
                componentFingerprint,
                markedBoxed != null && markedBoxed != 0
        ));
    }

    void trashIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.trashIdentity(
                serverPlayer,
                workflowRuntime(serverPlayer),
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    /**
     * Take one item of {@code identity} from the highest-affinity proximate
     * chest that contains it. Replaces slot-precise client-side take so
     * the player can act on a ghost card without knowing which chest /
     * slot the matching item happens to live in.
     */
    void takeOneByIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        takeByIdentity(itemId, comparisonMode, componentFingerprint, 1);
    }

    void takeItemsByIdentity(String itemId, String comparisonMode, String componentFingerprint, Integer count) {
        takeByIdentity(itemId, comparisonMode, componentFingerprint, count == null ? 0 : count);
    }

    void takeDesiredGapOrStackByIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        applyTakeOutcome(serverPlayer, WorkspaceChestCommandService.takeDesiredGapOrStackByIdentity(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                resolveHost(serverPlayer)));
    }

    void takeStackByIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        takeByIdentity(itemId, comparisonMode, componentFingerprint, Integer.MAX_VALUE);
    }

    private void takeByIdentity(String itemId, String comparisonMode, String componentFingerprint, int maxCount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        applyTakeOutcome(serverPlayer, WorkspaceChestCommandService.takeByIdentity(
                serverPlayer,
                workflowRuntime(serverPlayer),
                identity,
                maxCount,
                true,
                resolveHost(serverPlayer)));
    }

    void takeFromChest(String storageIdRaw, Integer chestSlotIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyTakeOutcome(serverPlayer, WorkspaceChestCommandService.takeFromChest(
                serverPlayer,
                workflowRuntime(serverPlayer),
                storageIdRaw,
                chestSlotIndex,
                WorkspaceChestCommandService.TakeQuantity.STACK));
    }

    private static ItemIdentity resolveIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return new SlotWorkspaceViewModel.IdentityRef(itemId, comparisonMode, componentFingerprint).toIdentity();
    }

    private void clearSatisfiedWantedCounts(InventoryAuthoritySnapshot authority) {
        if (player instanceof ServerPlayer serverPlayer) {
            SlotWorkspaceCommandService.clearSatisfiedWantedCounts(
                    workflowRuntime(serverPlayer),
                    authority);
        }
    }

    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    void moveHotbarToAtlas(Integer hotbarIndex, String islandId, Integer ordinal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.moveHotbarToAtlas(
                serverPlayer,
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                hotbarIndex,
                islandId,
                ordinal,
                (source, destination, origin) -> executeTransfer(serverPlayer, source, destination, origin),
                () -> {
                    refreshServerView(serverPlayer);
                    return viewModel;
                },
                "slot_workspace.ldlib"));
    }

    private void applyOutcome(ServerPlayer serverPlayer, WorkspaceCommandOutcome outcome) {
        WorkspaceCommandOutcome resolved = outcome == null
                ? WorkspaceCommandOutcome.rejected("null_command_outcome")
                : outcome;
        queueOutcomeInvalidations(resolved);
        status = resolved.status();
        diagnostics = resolved.diagnostics();
        broadcast(serverPlayer);
    }

    void applyExternalOutcome(ServerPlayer serverPlayer, WorkspaceCommandOutcome outcome) {
        applyOutcome(serverPlayer, outcome);
    }

    private void applyTakeOutcome(ServerPlayer serverPlayer, WorkspaceCommandOutcome outcome) {
        WorkspaceCommandOutcome resolved = outcome == null
                ? WorkspaceCommandOutcome.rejected("take_command_failed")
                : outcome;
        if (isCarryAcquisition(resolved)) {
            if (resolved.activityEvents().isEmpty()) {
                NeoForgeCarriedActivityTracker.markDirty(serverPlayer, "workspace_take");
            } else {
                NeoForgeCarriedActivityTracker.suppressAcquired(serverPlayer, resolved.activityEvents());
            }
            reapplyActiveKitFromCarry(serverPlayer);
        }
        applyOutcome(serverPlayer, resolved);
    }

    private void applyCursorOutcome(
            ServerPlayer serverPlayer,
            WorkspaceCursorCommandService.CursorCommandOutcome cursorOutcome
    ) {
        WorkspaceCursorCommandService.CursorCommandOutcome resolved = cursorOutcome == null
                ? new WorkspaceCursorCommandService.CursorCommandOutcome(
                        WorkspaceCommandOutcome.rejected("cursor_command_failed"),
                        cursorOrigin)
                : cursorOutcome;
        cursorOrigin = resolved.cursorOrigin();
        WorkspaceCommandOutcome outcome = resolved.outcome();
        if (outcome.success()) {
            NeoForgeCarriedActivityTracker.suppressNext(serverPlayer);
        }
        applyOutcome(serverPlayer, outcome);
    }

    private static boolean isCarryAcquisition(WorkspaceCommandOutcome outcome) {
        if (outcome == null || !outcome.success()) {
            return false;
        }
        return switch (outcome.status()) {
            case "took_one", "took_stack", "took_partial", "took_items", "took_all", "took_all_partial" -> true;
            default -> false;
        };
    }

    private void reject(String reason) {
        status = "rejected";
        diagnostics = reason == null || reason.isBlank() ? "rejected" : reason;
        if (player instanceof ServerPlayer serverPlayer) {
            queueInvalidation(WorkspaceInvalidation.frame(
                    WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                    diagnostics));
            broadcast(serverPlayer);
        }
    }

    private void refreshServerView(ServerPlayer serverPlayer) {
        long totalStart = System.nanoTime();
        long authorityStart = System.nanoTime();
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        long authorityNanos = System.nanoTime() - authorityStart;
        String hostDiagnostics = host == null ? "host_resolution_failed" : "";
        String combinedDiagnostics = combineDiagnostics(hostDiagnostics, diagnostics);
        int selected = serverPlayer.getInventory().selected;
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        runtime.collectionWorkflow().expireJunkTags();
        long gameTime = serverPlayer.serverLevel().getGameTime();
        if (ContextualSuggestionFeatureFlags.LIVE_OBSERVATION_ENABLED) {
            runtime.contextualSuggestions().observeStationContext(
                    host,
                    authority,
                    gameTime,
                    DomainEventMetadata.origin("contextual.neoforge.station_context"));
        }
        long setupStart = System.nanoTime();
        long storageStart = System.nanoTime();
        WorkspaceStorageRoutingContext storageContext =
                WorkspaceStorageRoutingContext.build(serverPlayer, runtime, authority, storageIndexCache, host);
        long storageIndexNanos = System.nanoTime() - storageStart;
        ClaimedChestMap claimedChestMap = storageContext.claimedChestMap();
        Set<String> proximateIds = storageContext.proximateStorageIds();
        Set<String> contextualSuggestionStorageIds = storageContext.contextualSuggestionStorageIds();
        lastObservedProximateStorageIds = Set.copyOf(proximateIds);
        lastObservedContextualStorageIds = Set.copyOf(contextualSuggestionStorageIds);
        List<WorldDisplayStorageSource> displaySources = storageContext.displaySources();
        WorkspaceStorageIndex storageIndex = storageContext.storageIndex();
        Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver =
                storageIndex.contentsResolver();
        Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerInfo =
                SophisticatedBackpackInventoryIntegrationProvider.carriedContainerInfoByIdentity(serverPlayer);
        Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver =
                containerInfo.isEmpty() ? identity -> null : containerInfo::get;
        SlotWorkspaceViewModel.LootChestSource lootChestSource = resolveLootChestSource(serverPlayer, claimedChestMap);
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel = resolveActiveChestPanel(
                serverPlayer, runtime, claimedChestMap);
        clearSatisfiedWantedCounts(authority);
        List<WorkspaceStorageIndex.StorageEntry> projectableDisplayEntries =
                storageIndex.projectableTrackedDisplayEntries();
        Set<String> liveDepositStorageIds = storageIndex.liveDepositStorageIds();
        Map<SlotResourceIdentity, Long> carriedFluidCounts = carriedFluidCounts(serverPlayer);
        FluidResourceObservationService.observe(
                runtime,
                storageIndex,
                carriedFluidCounts,
                "workspace_projection_fluid_observation");
        WorkflowDomainSnapshot snapshot = runtime.snapshot();
        WorkspaceProjectionRequest request = new WorkspaceProjectionRequest(
                authority,
                snapshot,
                status,
                combinedDiagnostics,
                0,
                selected,
                0,
                learnedRules,
                IslandSignalExtractor::extract,
                contentsResolver,
                proximateIds,
                containerResolver,
                lootChestSource,
                searchQuery,
                remoteStorageDetailIntent,
                remoteRecipeIngredientIdentities,
                gameTime,
                activeChestPanel,
                displaySources,
                contextualSuggestionStorageIds,
                displaySources,
                projectableDisplayEntries,
                liveDepositStorageIds,
                storageIndex,
                carriedFluidCounts,
                storageContext.liveChestContentPresence(),
                storageContext.liveStorageAffinityEligibility()
        );
        List<WorkspaceInvalidation> projectionInvalidations = prepareProjectionInvalidations(
                drainInvalidations(),
                authority,
                request.workflow());
        if (lastViewTag == null && projectionInvalidations.isEmpty()) {
            projectionInvalidations = List.of(WorkspaceInvalidation.full(
                    WorkspaceInvalidation.Reason.SESSION_OPEN,
                    "neoforge_session_open"));
        }
        long requestSetupNanos = System.nanoTime() - setupStart;
        long projectionStart = System.nanoTime();
        WorkspaceProjectionResult projection = projectionCache.project(request, projectionInvalidations);
        long projectionCallNanos = System.nanoTime() - projectionStart;
        SlotWorkspaceViewModel projected = projection.viewModel();
        // Pickup-time auto-home: at most one carried-but-unassigned
        // identity per refresh gets routed via its top chip suggestion
        // (or Misc when none fires). Throttled to one per refresh
        // because each {@code assignHome} writes through the persistence
        // service synchronously; without throttling, opening a screen
        // with a full inventory froze the server tick long enough that
        // the close-screen packet never landed. Subsequent refreshes
        // chip away at the remaining triage list. Re-projects against
        // the freshly-mutated snapshot so the broadcast reflects the
        // new assignment.
        boolean autoHomeReprojected = false;
        if (SlotWorkspaceCommandService.autoHomeTriageItems(runtime, projected, autoHomeAttempted)) {
            autoHomeReprojected = true;
            projectionCache.clear();
            activeChestPanel = resolveActiveChestPanel(serverPlayer, runtime, claimedChestMap);
            snapshot = runtime.snapshot();
            long reprojectSetupStart = System.nanoTime();
            WorkspaceProjectionRequest reprojectRequest = new WorkspaceProjectionRequest(
                    authority,
                    snapshot,
                    status,
                    combinedDiagnostics,
                    0,
                    selected,
                    0,
                    learnedRules,
                    IslandSignalExtractor::extract,
                    contentsResolver,
                    proximateIds,
                    containerResolver,
                    lootChestSource,
                    searchQuery,
                    remoteStorageDetailIntent,
                    remoteRecipeIngredientIdentities,
                    gameTime,
                    activeChestPanel,
                    displaySources,
                    contextualSuggestionStorageIds,
                    displaySources,
                    projectableDisplayEntries,
                    liveDepositStorageIds,
                    storageIndex,
                    carriedFluidCounts,
                    storageContext.liveChestContentPresence(),
                    storageContext.liveStorageAffinityEligibility()
            );
            requestSetupNanos += System.nanoTime() - reprojectSetupStart;
            projectionStart = System.nanoTime();
            projection = projectionCache.project(
                    reprojectRequest,
                    WorkspaceInvalidation.full(
                            WorkspaceInvalidation.Reason.AUTO_HOME_REPROJECTED,
                            "auto_home_mutated_workflow"));
            projectionCallNanos += System.nanoTime() - projectionStart;
            projected = projection.viewModel();
        }
        long hotbarStart = System.nanoTime();
        HotbarSlotRecencyRegistry.observe(serverPlayer, projected);
        long hotbarObserveNanos = System.nanoTime() - hotbarStart;
        long observedWorkflowSequence = currentWorkflowSequence(runtime);
        long observedCarriedRevision = CarriedInventoryRevisions.revision(serverPlayer);
        lastObservedWorkflowSequence = observedWorkflowSequence;
        lastObservedCarriedRevision = observedCarriedRevision;
        lastProjectedAuthority = authority;
        lastProjectedWorkflowSnapshot = snapshot;
        long encodeNanos = 0L;
        int payloadBytes = lastViewTag == null ? 0 : lastViewTag.sizeInBytes();
        SlotWorkspaceViewModelCodec.SliceStats sliceStats = encodedSliceCache.lastStats();
        boolean contentChanged = !projection.contentFingerprint().equals(lastContentFingerprint);
        if (!projection.contentFingerprint().equals(lastContentFingerprint)) {
            lastContentFingerprint = projection.contentFingerprint();
            viewModel = projected.withRevision(nextRevision++);
            long encodeStart = System.nanoTime();
            lastViewTag = SlotWorkspaceViewModelCodec.encodeTransfer(
                    viewModel,
                    serverPlayer.registryAccess(),
                    encodedSliceCache,
                    false);
            encodeNanos = System.nanoTime() - encodeStart;
            sliceStats = encodedSliceCache.lastStats();
            payloadBytes = lastViewTag.sizeInBytes();
        }
        long totalNanos = System.nanoTime() - totalStart;
        if (SlotDebugLog.enabled() || totalNanos >= SLOW_REFRESH_NANOS) {
            logRefreshTiming(
                    serverPlayer,
                    projection,
                    authorityNanos,
                    requestSetupNanos,
                    storageIndexNanos,
                    projectionCallNanos,
                    hotbarObserveNanos,
                    encodeNanos,
                    totalNanos,
                    contentChanged,
                    autoHomeReprojected,
                    authority,
                    storageIndex.entries().size(),
                    projectableDisplayEntries.size(),
                    liveDepositStorageIds.size(),
                    storageContext.indexDiagnostics(),
                    payloadBytes,
                    sliceStats);
        }
        rememberStructuralState(serverPlayer);
    }

    private static void logRefreshTiming(
            ServerPlayer serverPlayer,
            WorkspaceProjectionResult projection,
            long authorityNanos,
            long requestSetupNanos,
            long storageIndexNanos,
            long projectionCallNanos,
            long hotbarObserveNanos,
            long encodeNanos,
            long totalNanos,
            boolean contentChanged,
            boolean autoHomeReprojected,
            InventoryAuthoritySnapshot authority,
            int storageEntryCount,
            int trackedDisplayEntryCount,
            int liveDepositStorageCount,
            WorkspaceStorageIndexCache.Diagnostics storageIndexDiagnostics,
            int payloadBytes,
            SlotWorkspaceViewModelCodec.SliceStats sliceStats
    ) {
        SlotWorkspaceViewModel viewModel = projection == null ? SlotWorkspaceViewModel.empty() : projection.viewModel();
        WorkspaceProjectionSessionCache.Diagnostics diagnostics = projection == null ? null : projection.diagnostics();
        WorkspaceProjectionTiming projectionTiming = diagnostics == null
                ? WorkspaceProjectionTiming.empty()
                : diagnostics.timing();
        ItemIdentityMatcher.MemoStats memo = diagnostics == null
                ? ItemIdentityMatcher.MemoStats.empty()
                : diagnostics.identityMemoStats();
        SlotWorkspaceViewModelCodec.SliceStats resolvedSliceStats =
                sliceStats == null ? SlotWorkspaceViewModelCodec.SliceStats.empty() : sliceStats;
        WorkspaceStorageIndexCache.PollDiagnostics poll = storageIndexDiagnostics == null
                ? WorkspaceStorageIndexCache.PollDiagnostics.empty()
                : storageIndexDiagnostics.trackedStoragePoll();
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] NeoForge workspace refresh player={} rev={} changed={} autoHome={} "
                        + "ms[authority={},request={},storageIndex={},inputKey={},projectMiss={},contentKey={},encode={},send={},total={}] "
                        + "counts[carriedEntries={},atlasItems={},triageItems={},storageEntries={},trackedDisplay={},liveDeposit={},wayfinding={},chestChips={},contentSummaries={},payloadBytes={}] "
                        + "cache[hit={},hits={},misses={},memoCreate={}/{}/size={},memoNormalize={}/{}/size={},memoEvictions={}/{},slices={}/{},projectionSlices={}/{},cards={}/{}/removed={},storageChips={}/{}/removed={},edges={}/{}/removed={},depositability={}/{}] "
                        + "poll[trackedCandidates={},checked={},changed={},failed={}] "
                        + "invalidation[{},fallback={},facts={}/{}]",
                serverPlayer == null ? "<unknown>" : serverPlayer.getGameProfile().getName(),
                viewModel.revision(),
                contentChanged,
                autoHomeReprojected,
                ms(authorityNanos),
                ms(requestSetupNanos),
                ms(storageIndexNanos),
                ms(projectionTiming.inputKeyNanos()),
                ms(projectionTiming.projectNanos()),
                ms(projectionTiming.contentKeyNanos()),
                ms(encodeNanos),
                0.0D,
                ms(totalNanos),
                carriedEntryCount(authority),
                viewModel.atlasItems().size(),
                viewModel.triageItems().size(),
                Math.max(0, storageEntryCount),
                Math.max(0, trackedDisplayEntryCount),
                Math.max(0, liveDepositStorageCount),
                viewModel.wayfindingTargets().size(),
                viewModel.chestChips().size(),
                contentSummaryCount(viewModel),
                Math.max(0, payloadBytes),
                diagnostics != null && diagnostics.structuralCacheHit(),
                diagnostics == null ? 0L : diagnostics.structuralHits(),
                diagnostics == null ? 0L : diagnostics.structuralMisses(),
                memo.createHits(),
                memo.createMisses(),
                memo.createCacheSize(),
                memo.normalizeHits(),
                memo.normalizeMisses(),
                memo.normalizeCacheSize(),
                memo.createEvictions(),
                memo.normalizeEvictions(),
                resolvedSliceStats.encodedSlices(),
                resolvedSliceStats.reusedSlices(),
                diagnostics == null ? 0 : diagnostics.projectionSliceStats().rebuiltSlices(),
                diagnostics == null ? 0 : diagnostics.projectionSliceStats().reusedSlices(),
                diagnostics == null ? 0 : diagnostics.cardProjectionStats().rebuiltCards(),
                diagnostics == null ? 0 : diagnostics.cardProjectionStats().reusedCards(),
                diagnostics == null ? 0 : diagnostics.cardProjectionStats().removedCards(),
                diagnostics == null ? 0 : diagnostics.storageProjectionStats().rebuiltStorageChips(),
                diagnostics == null ? 0 : diagnostics.storageProjectionStats().reusedStorageChips(),
                diagnostics == null ? 0 : diagnostics.storageProjectionStats().removedStorageChips(),
                diagnostics == null ? 0 : diagnostics.edgeProjectionStats().rebuiltWayfindingTargets(),
                diagnostics == null ? 0 : diagnostics.edgeProjectionStats().reusedWayfindingTargets(),
                diagnostics == null ? 0 : diagnostics.edgeProjectionStats().removedWayfindingTargets(),
                diagnostics == null ? 0 : diagnostics.edgeProjectionStats().rebuiltDepositabilitySets(),
                diagnostics == null ? 0 : diagnostics.edgeProjectionStats().reusedDepositabilitySets(),
                poll.candidates(),
                poll.checked(),
                poll.changed(),
                poll.failed(),
                diagnostics == null ? "none" : diagnostics.invalidations().compactSummary(),
                diagnostics == null ? "" : diagnostics.fullProjectionReason(),
                diagnostics == null ? 0L : diagnostics.projectionFactsUpdated(),
                diagnostics == null ? 0L : diagnostics.projectionFactsReused());
    }

    private static double ms(long nanos) {
        return WorkspaceProjectionTiming.millis(nanos);
    }

    private static int carriedEntryCount(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return 0;
        }
        int count = 0;
        for (dev.imagio.slot.inventory.core.InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry != null && entry.present()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int contentSummaryCount(SlotWorkspaceViewModel viewModel) {
        if (viewModel == null || viewModel.chestChips().isEmpty()) {
            return 0;
        }
        int count = 0;
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            if (chip != null) {
                count += chip.contents().size();
            }
        }
        return count;
    }

    private boolean shouldRefreshServerView(ServerPlayer serverPlayer) {
        if (lastViewTag == null) {
            return true;
        }
        AbstractContainerMenu menu = serverPlayer == null ? null : serverPlayer.containerMenu;
        int menuId = menu == null ? -1 : menu.containerId;
        List<ItemStackStructuralKey> menuKeys = structuralMenuKeys(menu);
        ItemStackStructuralKey cursorKey = menu == null
                ? ItemStackStructuralKey.EMPTY
                : ItemStackStructuralKey.from(menu.getCarried());
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        boolean refresh = false;
        if (currentWorkflowSequence(runtime) != lastObservedWorkflowSequence) {
            if (!hasPendingLocalizedWorkflowInvalidation()) {
                queueInvalidation(WorkspaceInvalidation.full(
                        WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                        "workflow_sequence_changed_not_localized"));
            }
            refresh = true;
        }
        if (CarriedInventoryRevisions.revision(serverPlayer) != lastObservedCarriedRevision) {
            queueInvalidation(WorkspaceInvalidation.full(
                    WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED,
                    "carried_revision_changed_not_localized"));
            refresh = true;
        }
        if (menuId != lastStructuralMenuId || !menuKeys.equals(lastStructuralMenuKeys)) {
            queueInvalidation(WorkspaceInvalidation.full(
                    WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                    "menu_structure_changed_not_localized"));
            refresh = true;
        }
        if (!cursorKey.equals(lastStructuralCursorKey)) {
            queueInvalidation(WorkspaceInvalidation.hotbarFrame(
                    WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                    "cursor_stack_changed"));
            refresh = true;
        }
        WorkspaceInvalidation proximityInvalidation = storageProximityInvalidation(serverPlayer, runtime);
        if (proximityInvalidation != null) {
            queueInvalidation(proximityInvalidation);
            refresh = true;
        }
        return refresh;
    }

    private static long currentWorkflowSequence(WorkflowDomainRuntime runtime) {
        if (runtime == null) {
            return 0L;
        }
        WorkflowDomainSnapshot snapshot = runtime.snapshot();
        return snapshot == null ? 0L : snapshot.nextGlobalSequence() * 31L + snapshot.craftRun().revision();
    }

    private boolean hasPendingLocalizedWorkflowInvalidation() {
        for (WorkspaceInvalidation invalidation : pendingInvalidations) {
            if (invalidation != null
                    && invalidation.reason() == WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED
                    && !invalidation.requiresFullProjection()) {
                return true;
            }
        }
        return false;
    }

    private WorkspaceInvalidation storageProximityInvalidation(ServerPlayer serverPlayer, WorkflowDomainRuntime runtime) {
        if (serverPlayer == null || runtime == null) {
            return null;
        }
        WorkflowDomainSnapshot snapshot = runtime.snapshot();
        ClaimedChestMap claimedChestMap = snapshot == null ? ClaimedChestMap.empty() : snapshot.claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(serverPlayer, claimedChestMap);
        Set<String> contextual = WorkspaceChestProjectionSupport.proximateStorageIds(
                serverPlayer,
                claimedChestMap,
                WorkspaceChestProjectionSupport.CONTEXTUAL_SUGGESTION_RADIUS_BLOCKS);
        return WorkspaceProximityInvalidations.storageProximityChange(
                lastObservedProximateStorageIds,
                proximate,
                lastObservedContextualStorageIds,
                contextual);
    }

    private void rememberStructuralState(ServerPlayer serverPlayer) {
        AbstractContainerMenu menu = serverPlayer == null ? null : serverPlayer.containerMenu;
        lastStructuralMenuId = menu == null ? -1 : menu.containerId;
        lastStructuralMenuKeys = structuralMenuKeys(menu);
        lastStructuralCursorKey = menu == null
                ? ItemStackStructuralKey.EMPTY
                : ItemStackStructuralKey.from(menu.getCarried());
    }

    private static List<ItemStackStructuralKey> structuralMenuKeys(AbstractContainerMenu menu) {
        if (menu == null || menu.slots.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<ItemStackStructuralKey> keys = new java.util.ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            keys.add(ItemStackStructuralKey.from(slot == null ? ItemStack.EMPTY : slot.getItem()));
        }
        return List.copyOf(keys);
    }

    /**
     * Build the active-chest panel snapshot for the player's currently
     * open chest screen. Empty when the player isn't viewing a chest
     * (host = crafting / inventory / etc.) or when the chest's BlockPos
     * isn't tracked by the deposit observer's session table. Surfaces
     * the chest's claim state + cluster info so the sidebar's chest
     * control strip can render rename / forget / claim affordances
     * scoped to the chest the player is actively interacting with.
     */
    private static SlotWorkspaceViewModel.ActiveChestPanel resolveActiveChestPanel(
            ServerPlayer serverPlayer,
            WorkflowDomainRuntime runtime,
            ClaimedChestMap claimedChestMap
    ) {
        return ActiveChestPanelProjectionSupport.resolve(
                serverPlayer,
                runtime,
                claimedChestMap,
                ChestDepositObserver.activeChestPos(serverPlayer),
                ChestStorageAnchors::toAnchor);
    }

    private ClaimedChest activeDepositFallbackChest(ServerPlayer serverPlayer) {
        if (serverPlayer == null) {
            return null;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        BlockPos pos = ChestDepositObserver.activeChestPos(serverPlayer);
        if (pos == null) {
            return null;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            return null;
        }
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(level, pos);
        if (anchor == null) {
            return null;
        }
        if (!WorkspaceChestProjectionSupport.isProximate(serverPlayer, anchor)) {
            return null;
        }
        UUID storageId = ChestDepositObserver.resolveOrCreateClaim(
                runtime.chestClaimWorkflow(),
                level,
                pos,
                anchor);
        seedClaimedChestContents(serverPlayer, storageId);
        return storageId == null ? null : runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
    }

    private int seedClaimedChestContents(ServerPlayer serverPlayer, UUID storageId) {
        if (serverPlayer == null || storageId == null || !StorageAccessRegistry.isInstalled()) {
            return 0;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChest claim = runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        SlotWorkspaceViewModel.ChestContentsSnapshot contents = WorkspaceChestProjectionSupport.readContents(
                serverPlayer.getServer(),
                claim,
                worldStorage);
        WorkspaceStorageMemoryStore.observeStorageIds(
                serverPlayer.getServer(),
                worldStorage,
                runtime.chestClaimWorkflow().claimedChestMap(),
                java.util.List.of(storageId.toString()),
                serverPlayer.serverLevel().getGameTime(),
                "claim_seed");
        return ChestContentAffinitySeeder.seedInitialContents(
                runtime.chestClaimWorkflow(),
                storageId,
                contents,
                serverPlayer.serverLevel().getGameTime());
    }

    private static SlotWorkspaceViewModel.LootChestSource resolveLootChestSource(
            ServerPlayer serverPlayer, ClaimedChestMap claimedChestMap
    ) {
        return LootChestProjectionSupport.closest(
                        serverPlayer,
                        claimedChestMap,
                        ChestStorageAnchors::isClaimable,
                        ChestStorageIds::read)
                .map(pos -> {
                    ServerLevel level = serverPlayer.serverLevel();
                    BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof ChestBlockEntity)) {
                        return null;
                    }
                    Container container = (Container) be;
                    int size = container.getContainerSize();
                    java.util.ArrayList<ItemStack> contents = new java.util.ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        contents.add(container.getItem(i).copy());
                    }
                    String dimensionId = level.dimension().location().toString();
                    String label = "Loot chest at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
                    return new SlotWorkspaceViewModel.LootChestSource(
                            pos.getX(), pos.getY(), pos.getZ(), dimensionId, label, contents
                    );
                })
                .orElse(null);
    }

    private InventoryHostDescriptor resolveHost(ServerPlayer serverPlayer) {
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                serverPlayer.getInventory(),
                Component.literal("SLOT Workspace"),
                SlotWorkspaceUiSession.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotWorkspace", "ldlib")
                )
        ));
    }

    private WorkflowDomainRuntime workflowRuntime(ServerPlayer serverPlayer) {
        return SlotPlayerWorkflowRuntimeService.runtime(serverPlayer);
    }

    private void recordOutcome(ServerPlayer serverPlayer, InventoryActionOutcome outcome) {
        workflowRuntime(serverPlayer).recordOutcome(outcome);
        if (outcome != null && outcome.successful()) {
            NeoForgeCarriedActivityTracker.suppressOutcome(serverPlayer, outcome);
        }
    }

    private static Map<SlotResourceIdentity, Long> carriedFluidCounts(ServerPlayer player) {
        if (player == null || !StorageAccessRegistry.isInstalled()) {
            return Map.of();
        }
        try {
            return CarriedSourceAccess.fluidCounts(StorageAccessRegistry.carriedSourceAccess().enumerateFluids(player));
        } catch (RuntimeException | LinkageError ignored) {
            return Map.of();
        }
    }

    private WorkspaceTransferExecution executeTransfer(
            ServerPlayer serverPlayer,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin
    ) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            SlotDiagnostics.workspaceTransferHostMissing(source, destination, serverPlayer.containerMenu);
            return WorkspaceTransferExecution.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        var sourceEntry = InventoryAuthorityReadService.entrySnapshot(authority, source);
        SlotDiagnostics.workspaceTransferRequested(origin, host, source, destination, sourceEntry);
        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                source,
                destination,
                origin
        );
        if (!build.dispatchable()) {
            SlotDiagnostics.workspaceTransferBuildRejected(build.diagnostics(), host, source, destination, sourceEntry);
            return WorkspaceTransferExecution.rejected(build.diagnostics());
        }

        InventoryActionRequest request = build.request();
        SlotDiagnostics.workspaceTransferRequestBuilt(host, request);
        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                serverPlayer,
                request,
                ProtectionPolicy.allowAll()
        );
        recordOutcome(serverPlayer, outcome);
        WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);
        SlotDiagnostics.workspaceTransferExecuted(host, request, outcome, feedback.status(), feedback.diagnostics());
        SlotDebugLog.log(
                "LDLib workspace transfer {} {} -> {} {}",
                outcome == null ? "missing_outcome" : outcome.status(),
                source.stableKey(),
                destination.stableKey(),
                feedback.diagnostics()
        );
        return new WorkspaceTransferExecution(host, request, outcome, feedback);
    }

    private void broadcast(ServerPlayer serverPlayer) {
        if (pendingInvalidations.isEmpty()) {
            queueInvalidation(WorkspaceInvalidation.full(
                    WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                    "direct_broadcast_not_localized"));
        }
        refreshServerView(serverPlayer);
        if (serverPlayer.containerMenu != null) {
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    private void queueOutcomeInvalidations(WorkspaceCommandOutcome outcome) {
        WorkspaceCommandOutcome resolved = outcome == null
                ? WorkspaceCommandOutcome.rejected("null_command_outcome")
                : outcome;
        if (!resolved.invalidations().isEmpty()) {
            pendingInvalidations.addAll(resolved.invalidations());
            return;
        }
        if (!resolved.success()) {
            queueInvalidation(WorkspaceInvalidation.frame(
                    WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                    resolved.diagnostics()));
            return;
        }
        queueInvalidation(WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                resolved.status().isBlank() ? "command_outcome_not_localized" : resolved.status()));
    }

    private void queueInvalidation(WorkspaceInvalidation invalidation) {
        if (invalidation != null) {
            pendingInvalidations.add(invalidation);
        }
    }

    private List<WorkspaceInvalidation> drainInvalidations() {
        if (pendingInvalidations.isEmpty()) {
            return List.of();
        }
        List<WorkspaceInvalidation> copy = List.copyOf(pendingInvalidations);
        pendingInvalidations.clear();
        return copy;
    }

    private List<WorkspaceInvalidation> prepareProjectionInvalidations(
            List<WorkspaceInvalidation> invalidations,
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow
    ) {
        List<WorkspaceInvalidation> resolved = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                lastProjectedAuthority,
                authority,
                invalidations);
        return WorkspaceWorkflowInvalidations.localizeSequenceOnlyInvalidations(
                lastProjectedWorkflowSnapshot,
                workflow,
                resolved);
    }

    private static String combineDiagnostics(String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + "  " + second;
        }
        return hasFirst ? first : hasSecond ? second : "";
    }

    private static ItemIdentity identity(String itemId, String comparisonMode, String componentFingerprint) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        ItemComparisonMode mode = ItemComparisonMode.ITEM_ID;
        if (comparisonMode != null) {
            try {
                mode = ItemComparisonMode.valueOf(comparisonMode);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new ItemIdentity(itemId, mode, componentFingerprint == null ? "" : componentFingerprint);
    }

    /**
     * Maps an RPC transfer-target kind (an int the client sends) to an
     * {@link InventoryActionTarget}. The set of valid kinds is protocol-bound —
     * the client must know the integer constants at compile time — so this
     * switch intentionally enumerates exactly what the UI emits over RPC, not
     * every possible carried source in the world.
     *
     * <p>Identity-based flows (drag-to-hotbar, drag-to-chest) don't come
     * through here; they go through
     * {@code CarriedSourceAccess.findIdentity} which is source-agnostic.
     *
     * <p>Adding a new transfer-target kind means: (1) add a TARGET_* constant,
     * (2) update the client-side emitter, (3) add a case here. The scope is
     * the RPC protocol, not the storage layer — bona fide registry-style
     * extensibility lives in {@link CarriedSourceAccess}.
     */
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
