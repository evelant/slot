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
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.neoforge.storage.ChestContentsReader;
import dev.imagio.slot.neoforge.storage.ChestProximityResolver;
import dev.imagio.slot.neoforge.storage.DepositExecutor;
import dev.imagio.slot.neoforge.storage.TakeAllExecutor;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestLinkMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

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

    void assignHome(String itemId, String comparisonMode, String componentFingerprint, String islandId, Integer worldX, Integer worldY) {
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
                worldX,
                worldY
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

    void moveIsland(String islandId, Integer worldX, Integer worldY) {
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
        DepositPlan plan = DepositPlanner.plan(
                authority,
                runtime.snapshot().visualHomeMap(),
                runtime.snapshot().chestLinkMap(),
                proximate
        );
        DepositExecutor.DepositOutcome outcome = DepositExecutor.execute(serverPlayer, plan, claimedChestMap);
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

    void linkIslandToChest(String islandId, String storageId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.linkIslandToChest(
                workflowRuntime(serverPlayer),
                viewModel,
                islandId,
                storageId
        ));
    }

    void unlinkIslandFromChest(String islandId, String storageId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.unlinkIslandFromChest(
                workflowRuntime(serverPlayer),
                viewModel,
                islandId,
                storageId
        ));
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

    void moveStorageZone(Integer deltaX, Integer deltaY) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int dx = deltaX == null ? 0 : deltaX;
        int dy = deltaY == null ? 0 : deltaY;
        if (dx == 0 && dy == 0) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap map = runtime.chestClaimWorkflow().claimedChestMap();
        int moved = 0;
        for (ClaimedChest chest : map.chests()) {
            WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.moveChest(
                    runtime,
                    viewModel,
                    chest.storageId().toString(),
                    chest.atlasX() + dx,
                    chest.atlasY() + dy
            );
            if ("rejected".equals(outcome.status())) {
                continue;
            }
            moved++;
        }
        status = moved > 0 ? "storage_zone_moved" : "rejected";
        diagnostics = "moved=" + moved;
        broadcast(serverPlayer);
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

    private ClaimedChest resolveProximateLinkedChestForIdentity(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            ItemStack sourceStack
    ) {
        if (serverPlayer == null || identity == null || sourceStack == null || sourceStack.isEmpty()) {
            return null;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        VisualHomeAssignment assignment = runtime.snapshot().visualHomeMap().assignment(identity);
        if (assignment == null) {
            return null;
        }
        String islandId = assignment.islandId();
        if (islandId == null || islandId.isBlank()
                || SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
            return null;
        }
        ChestLinkMap linkMap = runtime.snapshot().chestLinkMap();
        java.util.Set<java.util.UUID> linkedStorageIds = linkMap.chestsLinkedFrom(islandId);
        if (linkedStorageIds == null || linkedStorageIds.isEmpty()) {
            return null;
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            return null;
        }
        record Candidate(ClaimedChest chest, int freeSlots, int matchingCount, java.util.UUID storageId) {
        }
        MinecraftServer server = serverPlayer.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return null;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        java.util.List<Candidate> candidates = new java.util.ArrayList<>();
        for (java.util.UUID storageId : linkedStorageIds) {
            if (!proximate.contains(storageId.toString())) {
                continue;
            }
            ClaimedChest chest = claimedChestMap.chest(storageId);
            if (chest == null) {
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
            int totalSlots = world.slotCount(server, target);
            int matchingCount = 0;
            int occupiedSlots = 0;
            for (WorldStorageAccess.SlotContent content : world.enumerate(server, target)) {
                ItemStack here = content.stack();
                if (here.isEmpty()) {
                    continue;
                }
                occupiedSlots++;
                if (ItemIdentityMatcher.create(here).equals(identity)) {
                    matchingCount += here.getCount();
                }
            }
            int freeSlots = Math.max(0, totalSlots - occupiedSlots);
            candidates.add(new Candidate(chest, freeSlots, matchingCount, storageId));
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort((a, b) -> {
            int cmp = Integer.compare(a.freeSlots(), b.freeSlots());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.matchingCount(), a.matchingCount());
            if (cmp != 0) return cmp;
            return a.storageId().compareTo(b.storageId());
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

    void moveHotbarToAtlas(Integer hotbarIndex, String islandId, Integer worldX, Integer worldY) {
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
                worldX,
                worldY,
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
                containerResolver
        );
        CompoundTag nextContent = SlotWorkspaceViewModelCodec.encode(projected, serverPlayer.registryAccess(), false);
        if (!nextContent.equals(lastContentTag)) {
            lastContentTag = nextContent.copy();
            viewModel = projected.withRevision(nextRevision++);
            lastViewTag = SlotWorkspaceViewModelCodec.encode(viewModel, serverPlayer.registryAccess());
        }
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
