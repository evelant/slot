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
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.DepositPlan;
import dev.imagio.slot.inventory.workspace.DepositPlanner;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.neoforge.storage.ChestContentsReader;
import dev.imagio.slot.neoforge.storage.ChestDepositObserver;
import dev.imagio.slot.neoforge.storage.ChestProximityResolver;
import dev.imagio.slot.neoforge.storage.ChestStorageAnchors;
import dev.imagio.slot.neoforge.storage.ChestStorageIds;
import dev.imagio.slot.neoforge.storage.DepositExecutor;
import dev.imagio.slot.neoforge.storage.LootChestProximityResolver;
import dev.imagio.slot.neoforge.storage.TakeAllExecutor;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private CompoundTag lastContentTag = new CompoundTag();
    private CompoundTag lastViewTag;
    private long nextRevision = 1L;
    private String status = "ready";
    private String diagnostics = "";
    /**
     * Mirror of the client-side search query, pushed via
     * {@link #setSearchQuery}. Drives server-side gating of the remote-only
     * ghost synthesis: those ghosts pollute the atlas full-time if always on,
     * but disappearing them under no search blocks search-as-find. Storing
     * the active query lets the projection conditionally synthesize matching
     * ghosts only when the player is searching.
     */
    private String searchQuery = "";

    SlotWorkspaceUiSession(Player player) {
        this.player = player;
    }

    SlotWorkspaceViewModel viewModel() {
        return viewModel;
    }

    Tag viewTag() {
        if (player instanceof ServerPlayer serverPlayer) {
            refreshServerView(serverPlayer);
        }
        return lastViewTag == null ? SlotWorkspaceViewModelCodec.encode(viewModel, player.registryAccess()) : lastViewTag.copy();
    }

    void acceptRemoteView(Tag tag) {
        viewModel = SlotWorkspaceViewModelCodec.decode(player.registryAccess(), tag);
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
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.assignHome(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
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

    void deposit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            status = "rejected";
            diagnostics = "no_proximate_chest";
            broadcast(serverPlayer);
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        long tick = serverPlayer.serverLevel().getGameTime();
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
        DepositPlan plan = DepositPlanner.plan(
                authority,
                affinityMap,
                claimedChestMap,
                proximate,
                this::descriptorForIdentity
        );
        DepositExecutor.DepositOutcome outcome = DepositExecutor.execute(serverPlayer, plan, claimedChestMap);
        for (DepositExecutor.DepositRecord record : outcome.records()) {
            runtime.chestClaimWorkflow().recordDeposit(
                    record.storageId(), record.identity(), record.count(), tick);
        }
        if (outcome.deposited() == 0 && outcome.failed() == 0) {
            status = "nothing_to_deposit";
            diagnostics = "";
        } else if (outcome.deposited() > 0 && outcome.failed() == 0) {
            status = "deposited";
            diagnostics = "deposited=" + outcome.deposited();
        } else if (outcome.deposited() == 0) {
            status = "rejected";
            diagnostics = "deposit_failed=" + outcome.failed();
        } else {
            status = "deposited_partial";
            diagnostics = "deposited=" + outcome.deposited() + " failed=" + outcome.failed();
        }
        broadcast(serverPlayer);
    }

    /**
     * Resolve an {@link ItemIdentity} back to an
     * {@link dev.imagio.slot.inventory.triage.IslandSignalDescriptor}
     * for the deposit-planner facet-affinity fallback. Builds a
     * synthetic default {@link ItemStack} via the registry, which is
     * enough to recover tags, role, material_family, etc. — chest
     * affinity bonds don't carry component fingerprints, so the
     * default stack is good enough for the adjacency-key lookup.
     * Returns {@code null} when the registry doesn't have the item
     * (datapack-only or removed mods); the caller treats null as "no
     * facet match" and falls through to direct affinity only.
     */
    private dev.imagio.slot.inventory.triage.IslandSignalDescriptor descriptorForIdentity(ItemIdentity identity) {
        if (identity == null || identity.itemId() == null || identity.itemId().isBlank()) {
            return null;
        }
        ItemStack stack = GhostAtlasStackFactory.resolve(identity.itemId());
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        try {
            return IslandSignalExtractor.extract(stack);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
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
        long radiusSq = (long) LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS
                * LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS;
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
        ClaimedChest claim = chestService.claimedChestMap().chest(storageId);
        if (claim == null) {
            reject("loot_chest_claim_missing");
            return;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> sourceLocation = carried.findIdentity(serverPlayer, identity);
        if (sourceLocation.isEmpty()) {
            // Claim still stands — the player has now made this chest
            // a storage chest even if they had nothing in carry to deposit.
            // Surface the partial success so the panel refreshes.
            refreshServerView(serverPlayer);
            status = "claimed";
            diagnostics = "claimed_no_deposit_source";
            broadcast(serverPlayer);
            return;
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                serverPlayer,
                sourceLocation.get().sourceId(),
                sourceLocation.get().slotIndex(),
                claim);
        if (outcome.success() && outcome.record() != null) {
            chestService.recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    level.getGameTime());
        }
        applyChestDepositOutcome(serverPlayer, outcome, claim);
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
        long radiusSq = (long) LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS
                * LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS;
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
        long radiusSq = (long) LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS
                * LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS;
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
        refreshServerView(serverPlayer);
        if (storageIdRaw == null || storageIdRaw.isBlank()) {
            status = "rejected";
            diagnostics = "invalid_chest_storage_id";
            broadcast(serverPlayer);
            return;
        }
        UUID storageId;
        try {
            storageId = UUID.fromString(storageIdRaw);
        } catch (IllegalArgumentException ignored) {
            status = "rejected";
            diagnostics = "invalid_chest_storage_id";
            broadcast(serverPlayer);
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChest chest = runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
        if (chest == null) {
            status = "rejected";
            diagnostics = "unknown_chest_tile";
            broadcast(serverPlayer);
            return;
        }
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(
                serverPlayer, runtime.chestClaimWorkflow().claimedChestMap()
        );
        if (!proximate.contains(storageIdRaw)) {
            status = "rejected";
            diagnostics = "chest_not_proximate";
            broadcast(serverPlayer);
            return;
        }
        TakeAllExecutor.TakeAllOutcome outcome = TakeAllExecutor.execute(serverPlayer, chest);
        if (outcome.movedStacks() == 0 && outcome.leftoverSlots() == 0) {
            status = "nothing_to_take";
            diagnostics = "";
        } else if (outcome.leftoverSlots() == 0) {
            status = "took_all";
            diagnostics = "moved=" + outcome.movedStacks();
        } else {
            status = "took_all_partial";
            diagnostics = "moved=" + outcome.movedStacks() + " leftover_slots=" + outcome.leftoverSlots();
        }
        if (outcome.movedStacks() > 0) {
            reapplyActiveKitFromCarry(serverPlayer);
        }
        broadcast(serverPlayer);
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
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deleteIsland(
                workflowRuntime(serverPlayer),
                islandId
        ));
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
            workflowRuntime(serverPlayer).recordOutcome(outcome);
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
            workflowRuntime(serverPlayer).recordOutcome(outcome);
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
            workflowRuntime(serverPlayer).recordOutcome(outcome);
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

    void addKitBring(String kitId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.addKitBring(
                workflowRuntime(serverPlayer),
                kitId,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    void removeKitBring(String kitId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.removeKitBring(
                workflowRuntime(serverPlayer),
                kitId,
                itemId,
                comparisonMode,
                componentFingerprint
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
                    workflowRuntime(serverPlayer).recordOutcome(outcome);
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
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }
        refreshServerView(serverPlayer);
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(index);
        if (!slot.occupied()) {
            status = "nothing_to_return";
            diagnostics = "hotbar slot " + (index + 1) + " is empty";
            broadcast(serverPlayer);
            return;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(slot.displayStack());
        boolean homed = workflowRuntime(serverPlayer).snapshot().visualHomeMap().assignment(identity) != null;
        ClaimedChest depositTarget = resolveProximateLinkedChestForIdentity(
                serverPlayer, identity, slot.displayStack());
        if (depositTarget != null) {
            DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                    serverPlayer,
                    BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                    index,
                    depositTarget
            );
            applyChestDepositOutcome(serverPlayer, outcome, depositTarget);
            return;
        }
        WorkspaceTransferExecution execution = executeTransfer(
                serverPlayer,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, index),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                "slot_workspace.ldlib.return_hotbar_to_home"
        );
        if (execution.appliedCompletely()) {
            status = homed ? "returned_to_home" : "returned_unhomed";
            diagnostics = homed ? "returned to its home" : "returned to inventory";
        } else {
            String feedbackDiagnostics = execution.feedback().diagnostics();
            boolean fullDestination = "destination_full_or_incompatible".equals(feedbackDiagnostics);
            status = fullDestination ? "no_free_main_slot" : execution.feedback().status();
            diagnostics = fullDestination ? "no free main inventory slot" : feedbackDiagnostics;
        }
        broadcast(serverPlayer);
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
        if (!suppressChestPreference
                && tryDepositIdentityToLinkedChest(serverPlayer, identity, DepositQuantity.STACK)
                        == DepositAttempt.DISPATCHED) {
            return;
        }
        int freeHotbarIndex = -1;
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (!slot.occupied()) {
                freeHotbarIndex = slot.hotbarIndex();
                break;
            }
        }
        if (freeHotbarIndex < 0) {
            status = "no_free_hotbar_slot";
            diagnostics = "all hotbar slots are occupied";
            broadcast(serverPlayer);
            return;
        }
        // Route through the same identity-based hotbar assignment that digit-press
        // uses so items living in backpacks / other non-player carried sources can
        // reach the hotbar — CarriedSourceAccess.findIdentity walks every carried
        // source so the target doesn't need to live in PLAYER_MAIN.
        assignIdentityToHotbarIndex(serverPlayer, identity, freeHotbarIndex);
    }

    private void assignIdentityToHotbarIndex(ServerPlayer serverPlayer, ItemIdentity identity, int hotbarIndex) {
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
        if (located.isEmpty()) {
            status = "nothing_to_assign";
            diagnostics = "identity not found in any carried source";
            broadcast(serverPlayer);
            return;
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
                status = "assigned_to_hotbar_" + (hotbarIndex + 1);
                diagnostics = "moved to hotbar " + (hotbarIndex + 1);
            } else {
                status = execution.feedback().status();
                diagnostics = execution.feedback().diagnostics();
            }
            broadcast(serverPlayer);
            return;
        }
        applyLoadoutSingleTarget(serverPlayer, hotbarIndex, identity);
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
        // (or any other non-player carried source) the in-place swap path rejects with
        // `assign_requires_player_bound_targets`, so assignIdentityToHotbarIndex routes
        // through LoadoutApplyService's staging flow — TRANSFER source→hotbar, with an
        // automatic staging step that moves any current hotbar occupant into a free main slot.
        assignIdentityToHotbarIndex(serverPlayer, identity, hotbarIndex);
    }

    private void applyLoadoutSingleTarget(ServerPlayer serverPlayer, int hotbarIndex, ItemIdentity identity) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        var entry = new dev.imagio.slot.workflow.domain.QuickAccessLoadoutEntry(
                new dev.imagio.slot.workflow.domain.LoadoutTarget.QuickAccessLaneTarget(
                        BuiltinInventoryIds.QUICK_ACCESS_LANE_0, hotbarIndex),
                identity
        );
        var loadout = new dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition(
                "assign_identity_" + hotbarIndex,
                identity.itemId() + " -> hotbar " + (hotbarIndex + 1),
                java.util.Set.of(entry)
        );
        var plan = dev.imagio.slot.workflow.domain.LoadoutApplyService.plan(
                loadout,
                authority,
                ProtectionPolicy.allowAll(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                e -> ItemIdentityMatcher.create(e.stack())
        );
        java.util.function.Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            workflowRuntime(serverPlayer).recordOutcome(outcome);
            return outcome;
        };
        var result = new dev.imagio.slot.workflow.domain.LoadoutApplyExecutor(actionExecutor).execute(plan);
        if (result.satisfiedTargets().stream().anyMatch(t ->
                t instanceof dev.imagio.slot.workflow.domain.LoadoutTarget.QuickAccessLaneTarget q
                        && q.slotIndex() == hotbarIndex)) {
            status = "assigned_to_hotbar_" + (hotbarIndex + 1);
            diagnostics = "moved to hotbar " + (hotbarIndex + 1);
        } else {
            status = "rejected";
            diagnostics = result.diagnostics().isEmpty()
                    ? "assign failed"
                    : String.join(",", result.diagnostics());
        }
        broadcast(serverPlayer);
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
        switch (tryDepositIdentityToLinkedChest(serverPlayer, identity, DepositQuantity.STACK)) {
            case DISPATCHED -> { /* applyChestDepositOutcome already broadcast */ }
            case SOURCE_MISSING -> {
                status = "rejected";
                diagnostics = "nothing_to_deposit";
                broadcast(serverPlayer);
            }
            case NO_CHEST -> {
                status = "rejected";
                diagnostics = "no_linked_proximate_chest_with_room";
                broadcast(serverPlayer);
            }
        }
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
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> sourceLocation = carried.findIdentity(serverPlayer, identity);
        if (sourceLocation.isEmpty()) {
            status = "rejected";
            diagnostics = "nothing_to_deposit";
            broadcast(serverPlayer);
            return;
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                serverPlayer,
                sourceLocation.get().sourceId(),
                sourceLocation.get().slotIndex(),
                resolved.chest);
        // Bump affinity so future routing prefers this chest for the
        // identity. The omnibus deposit() method already does this loop
        // for its multi-stack outcomes; the per-chip path was missing
        // it, leaving drag-onto-chip deposits invisible to the
        // affinity learner.
        if (outcome.success() && outcome.record() != null) {
            workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    serverPlayer.serverLevel().getGameTime());
        }
        applyChestDepositOutcome(serverPlayer, outcome, resolved.chest);
    }

    void depositHotbarToChest(Integer hotbarIndex, String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                serverPlayer,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                index,
                resolved.chest
        );
        if (outcome.success() && outcome.record() != null) {
            workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    serverPlayer.serverLevel().getGameTime());
        }
        applyChestDepositOutcome(serverPlayer, outcome, resolved.chest);
    }

    void takeOneFromChest(String storageIdRaw, Integer chestSlotIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int slotIndex = chestSlotIndex == null ? -1 : chestSlotIndex;
        if (slotIndex < 0) {
            reject("invalid_chest_slot");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeSingleItem(
                serverPlayer, resolved.chest, slotIndex);
        if (!outcome.tookAnything()) {
            status = "nothing_to_take";
            diagnostics = "";
        } else {
            status = "took_one";
            diagnostics = "moved=" + outcome.moved();
        }
        if (outcome.tookAnything()) {
            reapplyActiveKitFromCarry(serverPlayer);
        }
        broadcast(serverPlayer);
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
        switch (tryDepositIdentityToLinkedChest(serverPlayer, identity, DepositQuantity.ITEM)) {
            case DISPATCHED -> { /* applyChestDepositOutcome already broadcast */ }
            case SOURCE_MISSING -> {
                status = "rejected";
                diagnostics = "nothing_to_deposit";
                broadcast(serverPlayer);
            }
            case NO_CHEST -> {
                status = "rejected";
                diagnostics = "no_linked_proximate_chest_with_room";
                broadcast(serverPlayer);
            }
        }
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
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            status = "rejected";
            diagnostics = "no_proximate_chest";
            broadcast(serverPlayer);
            return;
        }
        // Walk proximate chests in affinity-score order; first one with a
        // matching stack wins. If none have it, walk all proximate chests
        // (a chest may carry the item without the player having deposited
        // it yet — affinity is monotonically learned, not authoritative).
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
        java.util.ArrayList<ClaimedChest> ranked = new java.util.ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (proximate.contains(chest.storageId().toString())) {
                ranked.add(chest);
            }
        }
        ranked.sort((a, b) -> Integer.compare(
                affinityMap.score(b.storageId(), identity),
                affinityMap.score(a.storageId(), identity)
        ));
        boolean foundMatchButCouldNotInsert = false;
        for (ClaimedChest chest : ranked) {
            TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                    serverPlayer, chest, identity, maxCount,
                    maxCount == 1 ? "take-one-by-identity" : "take-stack-by-identity");
            if (outcome.tookAnything()) {
                status = maxCount == 1 ? "took_one" : "took_stack";
                diagnostics = "moved=" + outcome.moved();
                reapplyActiveKitFromCarry(serverPlayer);
                broadcast(serverPlayer);
                return;
            }
            if (outcome.partial()) {
                // Chest had the matching item but inventory rejected the
                // insert — almost always a full-carry problem.
                foundMatchButCouldNotInsert = true;
            }
        }
        if (foundMatchButCouldNotInsert) {
            status = "rejected";
            diagnostics = "carry_full";
        } else {
            status = "nothing_to_take";
            diagnostics = "no_matching_proximate_chest";
        }
        broadcast(serverPlayer);
    }

    void takeFromChest(String storageIdRaw, Integer chestSlotIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int slotIndex = chestSlotIndex == null ? -1 : chestSlotIndex;
        if (slotIndex < 0) {
            reject("invalid_chest_slot");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeSingleStack(
                serverPlayer, resolved.chest, slotIndex);
        if (!outcome.tookAnything()) {
            status = "nothing_to_take";
            diagnostics = "";
        } else if (outcome.partial()) {
            status = "took_partial";
            diagnostics = "moved=" + outcome.moved() + " leftover=" + outcome.leftover();
        } else {
            status = "took_stack";
            diagnostics = "moved=" + outcome.moved();
        }
        if (outcome.tookAnything()) {
            reapplyActiveKitFromCarry(serverPlayer);
        }
        broadcast(serverPlayer);
    }

    private ChestProximityResult resolveProximateChest(ServerPlayer serverPlayer, String storageIdRaw) {
        if (storageIdRaw == null || storageIdRaw.isBlank()) {
            return ChestProximityResult.failed("invalid_chest_storage_id");
        }
        UUID storageId;
        try {
            storageId = UUID.fromString(storageIdRaw);
        } catch (IllegalArgumentException ignored) {
            return ChestProximityResult.failed("invalid_chest_storage_id");
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChest chest = runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
        if (chest == null) {
            return ChestProximityResult.failed("unknown_chest_tile");
        }
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(
                serverPlayer, runtime.chestClaimWorkflow().claimedChestMap());
        if (!proximate.contains(storageIdRaw)) {
            return ChestProximityResult.failed("not_proximate");
        }
        return new ChestProximityResult(chest, null);
    }

    private void applyChestDepositRejection(ServerPlayer serverPlayer, String reason) {
        status = "rejected";
        diagnostics = reason;
        broadcast(serverPlayer);
    }

    private void applyChestDepositOutcome(
            ServerPlayer serverPlayer,
            DepositExecutor.SingleStackOutcome outcome,
            ClaimedChest chest
    ) {
        if (outcome.success()) {
            status = "deposited_stack";
            diagnostics = "deposited to " + chestLabel(chest);
        } else {
            status = "rejected";
            diagnostics = outcome.diagnostic();
        }
        broadcast(serverPlayer);
    }

    /** Quantity variant for {@link #tryDepositIdentityToLinkedChest}: whole stack vs single item. */
    private enum DepositQuantity { STACK, ITEM }

    /** Outcome of a deposit attempt. DISPATCHED means the outcome was already applied + broadcast. */
    private enum DepositAttempt { DISPATCHED, SOURCE_MISSING, NO_CHEST }

    /**
     * Find the first carried slot matching {@code identity}, resolve a
     * proximate linked chest that accepts it, and deposit via
     * {@link DepositExecutor}. Applies the outcome (status + broadcast) only
     * when {@link DepositAttempt#DISPATCHED} is returned — callers translate
     * SOURCE_MISSING / NO_CHEST into their own rejection diagnostics (the
     * two modes differ: opportunistic fall-through for assign-to-hotbar,
     * hard rejection for explicit deposit verbs).
     */
    private DepositAttempt tryDepositIdentityToLinkedChest(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            DepositQuantity quantity
    ) {
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
        if (located.isEmpty()) {
            return DepositAttempt.SOURCE_MISSING;
        }
        ItemStack sourceStack = carried.peek(serverPlayer,
                located.get().sourceId(), located.get().slotIndex());
        if (sourceStack.isEmpty()) {
            return DepositAttempt.SOURCE_MISSING;
        }
        ClaimedChest chest = resolveProximateLinkedChestForIdentity(serverPlayer, identity, sourceStack);
        if (chest == null) {
            return DepositAttempt.NO_CHEST;
        }
        DepositExecutor.SingleStackOutcome outcome = switch (quantity) {
            case STACK -> DepositExecutor.depositSingleStack(
                    serverPlayer, located.get().sourceId(), located.get().slotIndex(), chest);
            case ITEM -> DepositExecutor.depositSingleItem(
                    serverPlayer, located.get().sourceId(), located.get().slotIndex(), chest);
        };
        applyChestDepositOutcome(serverPlayer, outcome, chest);
        return DepositAttempt.DISPATCHED;
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

    /**
     * Pick a proximate claimed chest for {@code identity} based on stored
     * affinity (highest score first; ties broken by chest order). Falls
     * back to "any proximate chest with capacity for the stack" so the
     * cold-start case still works before affinity has been observed.
     */
    private ClaimedChest resolveProximateLinkedChestForIdentity(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            ItemStack sourceStack
    ) {
        if (serverPlayer == null || identity == null || sourceStack == null || sourceStack.isEmpty()) {
            return null;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            return null;
        }
        MinecraftServer server = serverPlayer.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return null;
        }
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        record Candidate(ClaimedChest chest, int score, int freeSlots) {
        }
        java.util.ArrayList<Candidate> candidates = new java.util.ArrayList<>();
        for (ClaimedChest chest : claimedChestMap.chests()) {
            if (!proximate.contains(chest.storageId().toString())) {
                continue;
            }
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            if (!world.isAccessible(server, target)) {
                continue;
            }
            ItemStack simulation = world.insert(server, target, sourceStack.copy(), true);
            if (!simulation.isEmpty()) {
                continue;
            }
            int score = affinityMap.score(chest.storageId(), identity);
            int totalSlots = world.slotCount(server, target);
            int occupied = 0;
            for (WorldStorageAccess.SlotContent content : world.enumerate(server, target)) {
                if (!content.stack().isEmpty()) {
                    occupied++;
                }
            }
            int free = Math.max(0, totalSlots - occupied);
            candidates.add(new Candidate(chest, score, free));
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort((a, b) -> {
            int cmp = Integer.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.freeSlots(), b.freeSlots());
            if (cmp != 0) return cmp;
            return a.chest().storageId().compareTo(b.chest().storageId());
        });
        return candidates.get(0).chest();
    }

    private static final class ChestProximityResult {
        private final ClaimedChest chest;
        private final String outcome;

        private ChestProximityResult(ClaimedChest chest, String outcome) {
            this.chest = chest;
            this.outcome = outcome;
        }

        private static ChestProximityResult failed(String reason) {
            return new ChestProximityResult(null, reason);
        }
    }

    private static ItemIdentity resolveIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return new SlotWorkspaceViewModel.IdentityRef(itemId, comparisonMode, componentFingerprint).toIdentity();
    }

    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    void moveHotbarToAtlas(Integer hotbarIndex, String islandId, Integer ordinal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int resolvedHotbarIndex = hotbarIndex == null ? -1 : hotbarIndex;
        if (resolvedHotbarIndex < 0 || resolvedHotbarIndex >= 9 || islandId == null || islandId.isBlank()) {
            reject("invalid_hotbar_drop");
            return;
        }
        SlotWorkspaceViewModel.HotbarSlot hotbarSlot = visibleHotbarSlot(serverPlayer, resolvedHotbarIndex);
        if (hotbarSlot == null) {
            reject("selected_hotbar_not_visible");
            return;
        }
        if (!SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)
                && viewModel.island(islandId) == null) {
            reject("unknown_island");
            return;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(hotbarSlot.displayStack());
        WorkspaceTransferExecution execution = executeTransfer(
                serverPlayer,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, resolvedHotbarIndex),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                "slot_workspace.ldlib.drag.hotbar_to_atlas"
        );
        if (!execution.appliedCompletely()) {
            status = execution.feedback().status();
            diagnostics = execution.feedback().diagnostics();
            broadcast(serverPlayer);
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.applyHomeDrop(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                identity,
                islandId,
                ordinal,
                "slot_workspace.ldlib.drag.hotbar_home"
        ));
    }

    private void applyOutcome(ServerPlayer serverPlayer, WorkspaceCommandOutcome outcome) {
        status = outcome.status();
        diagnostics = outcome.diagnostics();
        broadcast(serverPlayer);
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
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        MinecraftServer server = serverPlayer.getServer();
        Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver = storageId -> {
            if (server == null) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(storageId);
            } catch (IllegalArgumentException ignored) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            ClaimedChest chest = claimedChestMap.chest(uuid);
            if (chest == null) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            return ChestContentsReader.read(server, chest);
        };
        Set<String> proximateIds = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerInfo =
                SophisticatedBackpackInventoryIntegrationProvider.carriedContainerInfoByIdentity(serverPlayer);
        Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver =
                containerInfo.isEmpty() ? identity -> null : containerInfo::get;
        SlotWorkspaceViewModel.LootChestSource lootChestSource = resolveLootChestSource(serverPlayer, claimedChestMap);
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
                searchQuery
        );
        CompoundTag nextContent = SlotWorkspaceViewModelCodec.encode(projected, serverPlayer.registryAccess(), false);
        if (!nextContent.equals(lastContentTag)) {
            lastContentTag = nextContent.copy();
            viewModel = projected.withRevision(nextRevision++);
            lastViewTag = SlotWorkspaceViewModelCodec.encode(viewModel, serverPlayer.registryAccess());
        }
    }

    private static SlotWorkspaceViewModel.LootChestSource resolveLootChestSource(
            ServerPlayer serverPlayer, ClaimedChestMap claimedChestMap
    ) {
        return LootChestProximityResolver.closest(serverPlayer, claimedChestMap)
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
        workflowRuntime(serverPlayer).recordOutcome(outcome);
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

    private SlotWorkspaceViewModel.HotbarSlot visibleHotbarSlot(ServerPlayer serverPlayer, int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            return null;
        }
        refreshServerView(serverPlayer);
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hotbarIndex);
        return slot.occupied() ? slot : null;
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
