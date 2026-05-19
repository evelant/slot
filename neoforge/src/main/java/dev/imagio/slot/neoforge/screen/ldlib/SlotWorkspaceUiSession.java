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
import dev.imagio.slot.inventory.core.ItemStackStructuralKey;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.ActiveChestPanelProjectionSupport;
import dev.imagio.slot.inventory.workspace.ChestContentAffinitySeeder;
import dev.imagio.slot.inventory.workspace.DepositPlanner;
import dev.imagio.slot.inventory.workspace.HotbarSlotRecencyTracker;
import dev.imagio.slot.inventory.workspace.KitGatherService;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.LootChestProjectionSupport;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.TakeAllExecutor;
import dev.imagio.slot.inventory.workspace.WorkspaceBeltCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceChestProjectionSupport;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceCursorCommandService;
import dev.imagio.slot.inventory.workspace.WorkspaceHotbarSlotReverser;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageIndex;
import dev.imagio.slot.inventory.workspace.WorkspaceStorageMemoryStore;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.neoforge.storage.ChestDepositObserver;
import dev.imagio.slot.neoforge.storage.ChestStorageAnchors;
import dev.imagio.slot.neoforge.storage.ChestStorageIds;
import dev.imagio.slot.neoforge.storage.NeoForgeCarriedActivityTracker;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
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

    private final Player player;
    private final LearnedIslandRuleStore learnedRules = new LearnedIslandRuleStore();
    private final HotbarSlotRecencyTracker hotbarRecency = new HotbarSlotRecencyTracker();
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private CompoundTag lastContentTag = new CompoundTag();
    private CompoundTag lastViewTag;
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
        viewModel = SlotWorkspaceViewModelCodec.decode(player.registryAccess(), tag);
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
        String normalized = query == null ? "" : query;
        if (normalized.equals(searchQuery)) {
            return;
        }
        searchQuery = normalized;
        if (player instanceof ServerPlayer serverPlayer) {
            broadcast(serverPlayer);
        }
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
        status = "chest_claimed";
        diagnostics = "";
        broadcast(serverPlayer);
    }

    void forgetChest(String storageId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        // Capture anchors BEFORE the workflow service deletes the chest;
        // afterwards the chest record is gone and we can't find its
        // world positions to clear the stale BE storage-id attachment.
        // Without this clear, a forgotten chest still shows up as
        // "claimed" via {@link ChestStorageIds#read} on the next
        // deposit, blocking re-claim — the bug where forgetting a
        // chest meant losing it permanently until block-break.
        java.util.List<BlockPos> anchorPositions = anchorPositionsForChest(serverPlayer, storageId);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.forgetChest(
                workflowRuntime(serverPlayer),
                storageId
        ));
        ServerLevel level = serverPlayer.serverLevel();
        for (BlockPos pos : anchorPositions) {
            ChestStorageIds.clear(level, pos);
        }
    }

    private java.util.List<BlockPos> anchorPositionsForChest(ServerPlayer serverPlayer, String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return java.util.List.of();
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException ignored) {
            return java.util.List.of();
        }
        ClaimedChest existing = workflowRuntime(serverPlayer).chestClaimWorkflow()
                .claimedChestMap().chest(uuid);
        if (existing == null) {
            return java.util.List.of();
        }
        String currentDimension = serverPlayer.serverLevel().dimension().location().toString();
        java.util.ArrayList<BlockPos> positions = new java.util.ArrayList<>();
        existing.anchors().forEach(anchor -> {
            if (currentDimension.equals(anchor.dimensionId())) {
                positions.add(new BlockPos(anchor.x(), anchor.y(), anchor.z()));
            }
        });
        return positions;
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
            refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.createWorkflowTab(
                workflowRuntime(serverPlayer),
                name
        ));
    }

    void createKitVariant(String parentKitId, String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
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
        // Auto-fetch toward kit-scoped desired counts. Runs after the kit
        // is recorded as active so each identity gets pulled from proximate
        // chests up to (desired - currently_carried). Best-effort: if no
        // chest has the item or carry is full the gap stays.
        autoFetchKitDesiredCounts(serverPlayer, kitId);
    }

    /**
     * Pull items from proximate chests toward the kit-scoped desired
     * counts. Replaces the legacy bring-list fetch path (the bring list
     * itself is now folded into kit-scoped desired counts). Mirrors the
     * chest-walk in {@link #takeByIdentity}: highest-affinity proximate
     * chest first, falls through to the rest. Multi-pass per identity
     * because a chest may only have part of the gap; subsequent chests
     * fill the rest.
     */
    private void autoFetchKitDesiredCounts(ServerPlayer serverPlayer, String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        java.util.Map<ItemIdentity, Integer> wants = runtime.desiredCountWorkflow().forKit(kitId);
        if (wants.isEmpty()) {
            return;
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            return;
        }
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
        // Refresh authority snapshot once before the pull loop so the
        // carried-count check sees the post-apply state.
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        for (java.util.Map.Entry<ItemIdentity, Integer> want : wants.entrySet()) {
            ItemIdentity identity = want.getKey();
            int desired = want.getValue() == null ? 0 : want.getValue();
            if (identity == null || desired <= 0) {
                continue;
            }
            int carried = countCarried(authority, identity);
            int gap = desired - carried;
            if (gap <= 0) {
                continue;
            }
            // Sort chests for THIS identity by affinity score so the most
            // likely home gets walked first.
            java.util.List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                    identity, claimedChestMap, affinityMap, proximate);
            int remaining = gap;
            for (ClaimedChest chest : ranked) {
                if (remaining <= 0) {
                    break;
                }
                TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                        serverPlayer, chest, identity, remaining, "kit-auto-fetch");
                if (outcome.tookAnything()) {
                    remaining -= outcome.moved();
                }
            }
        }
        // Re-apply the kit so the new carry surfaces in any kit-needed slot.
        reapplyActiveKitFromCarry(serverPlayer);
    }

    private static int countCarried(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
        if (authority == null || identity == null) {
            return 0;
        }
        int total = 0;
        for (var source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    total += entry.count();
                }
            }
        }
        return total;
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
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deactivateKit(
                workflowRuntime(serverPlayer)
        ));
    }

    void performUndo() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.performUndo(
                workflowRuntime(serverPlayer)
        ));
    }

    void performRedo() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.performRedo(
                workflowRuntime(serverPlayer)
        ));
    }

    void renameCluster(String clusterId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.duplicateKit(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void deleteKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deleteKit(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void switchKitPage(Integer direction) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.addKitPage(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void removeKitPage(String kitId, Integer pageIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
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
        refreshServerView(serverPlayer);
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
                    false);
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
                recordHotbarPlacementOnSuccess(hotbarIndex, outcome);
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
                recordHotbarPlacementOnSuccess(hotbarIndex, WorkspaceCommandOutcome.accepted("assigned", ""));
                return WorkspaceCommandOutcome.accepted(
                        "assigned_to_hotbar_" + (hotbarIndex + 1),
                        "moved to hotbar " + (hotbarIndex + 1));
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
                    recordHotbarPlacementOnSuccess(hotbarIndex, outcome);
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
            recordHotbarPlacementOnSuccess(hotbarIndex, outcome);
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

        refreshServerView(serverPlayer);
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
                hotbarRecency.placementSequence(),
                targetHotbarIndex -> assignIdentityToHotbarIndex(serverPlayer, identity, targetHotbarIndex)));
    }

    void moveIdentityBetweenBackpackAndMain(
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
        refreshServerView(serverPlayer);
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
        applyOutcome(serverPlayer, WorkspaceBeltCommandService.moveIdentityBetweenBackpackAndMain(
                serverPlayer,
                host,
                authority,
                StorageAccessRegistry.carriedSourceAccess(),
                actionExecutor,
                identity,
                "slot_workspace.ldlib"));
    }

    private void recordHotbarPlacementOnSuccess(int hotbarIndex, WorkspaceCommandOutcome outcome) {
        hotbarRecency.recordPlacementOnSuccess(hotbarIndex, outcome);
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
                () -> activeDepositFallbackChest(serverPlayer)));
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
                () -> activeDepositFallbackChest(serverPlayer)));
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
                () -> activeDepositFallbackChest(serverPlayer)));
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
                identity));
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
                maxCount));
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
        status = outcome.status();
        diagnostics = outcome.diagnostics();
        broadcast(serverPlayer);
    }

    private void applyTakeOutcome(ServerPlayer serverPlayer, WorkspaceCommandOutcome outcome) {
        WorkspaceCommandOutcome resolved = outcome == null
                ? WorkspaceCommandOutcome.rejected("take_command_failed")
                : outcome;
        if (isCarryAcquisition(resolved)) {
            NeoForgeCarriedActivityTracker.suppressNext(serverPlayer);
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
            case "took_one", "took_stack", "took_partial", "took_all", "took_all_partial" -> true;
            default -> false;
        };
    }

    private void reject(String reason) {
        status = "rejected";
        diagnostics = reason == null || reason.isBlank() ? "rejected" : reason;
        if (player instanceof ServerPlayer serverPlayer) {
            broadcast(serverPlayer);
        }
    }

    private void refreshServerView(ServerPlayer serverPlayer) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        String hostDiagnostics = host == null ? "host_resolution_failed" : "";
        String combinedDiagnostics = combineDiagnostics(hostDiagnostics, diagnostics);
        int selected = serverPlayer.getInventory().selected;
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        long gameTime = serverPlayer.serverLevel().getGameTime();
        runtime.contextualSuggestions().observeStationContext(
                host,
                authority,
                gameTime,
                DomainEventMetadata.origin("contextual.neoforge.station_context"));
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        MinecraftServer server = serverPlayer.getServer();
        WorldStorageAccess worldStorage = StorageAccessRegistry.isInstalled()
                ? StorageAccessRegistry.worldStorageAccess()
                : null;
        Set<String> proximateIds = WorkspaceChestProjectionSupport.proximateStorageIds(serverPlayer, claimedChestMap);
        Set<String> contextualSuggestionStorageIds = WorkspaceChestProjectionSupport.proximateStorageIds(
                serverPlayer,
                claimedChestMap,
                WorkspaceChestProjectionSupport.CONTEXTUAL_SUGGESTION_RADIUS_BLOCKS);
        List<WorldDisplayStorageSource> displaySources =
                WorkspaceChestProjectionSupport.proximateDisplaySources(serverPlayer, worldStorage);
        WorkspaceStorageIndex storageIndex = WorkspaceStorageIndex.build(
                server,
                authority,
                runtime.snapshot(),
                worldStorage,
                proximateIds,
                displaySources,
                gameTime);
        Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver =
                storageIndex.contentsResolver();
        displaySources = storageIndex.displaySources();
        Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerInfo =
                SophisticatedBackpackInventoryIntegrationProvider.carriedContainerInfoByIdentity(serverPlayer);
        Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver =
                containerInfo.isEmpty() ? identity -> null : containerInfo::get;
        SlotWorkspaceViewModel.LootChestSource lootChestSource = resolveLootChestSource(serverPlayer, claimedChestMap);
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel = resolveActiveChestPanel(
                serverPlayer, runtime, claimedChestMap);
        clearSatisfiedWantedCounts(authority);
        SlotWorkspaceViewModel projected = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
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
                gameTime,
                activeChestPanel,
                displaySources,
                contextualSuggestionStorageIds,
                displaySources,
                storageIndex.trackedDisplayEntries(),
                storageIndex.liveDepositStorageIds()
        );
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
        if (SlotWorkspaceCommandService.autoHomeTriageItems(runtime, projected, autoHomeAttempted)) {
            activeChestPanel = resolveActiveChestPanel(serverPlayer, runtime, claimedChestMap);
            projected = SlotWorkspaceViewModel.project(
                    authority,
                    runtime.snapshot(),
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
                    gameTime,
                    activeChestPanel,
                    displaySources,
                    contextualSuggestionStorageIds,
                    displaySources,
                    storageIndex.trackedDisplayEntries(),
                    storageIndex.liveDepositStorageIds()
            );
        }
        hotbarRecency.observe(projected);
        CompoundTag nextContent = SlotWorkspaceViewModelCodec.encode(projected, serverPlayer.registryAccess(), false);
        if (!nextContent.equals(lastContentTag)) {
            lastContentTag = nextContent.copy();
            viewModel = projected.withRevision(nextRevision++);
            lastViewTag = SlotWorkspaceViewModelCodec.encode(viewModel, serverPlayer.registryAccess());
        }
        rememberStructuralState(serverPlayer);
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
        return menuId != lastStructuralMenuId
                || !menuKeys.equals(lastStructuralMenuKeys)
                || !cursorKey.equals(lastStructuralCursorKey);
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
            NeoForgeCarriedActivityTracker.suppressNext(serverPlayer);
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
        refreshServerView(serverPlayer);
        if (serverPlayer.containerMenu != null) {
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    private static String combineDiagnostics(String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + "  " + second;
        }
        return hasFirst ? first : hasSecond ? second : "";
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
