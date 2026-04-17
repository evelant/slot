package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.SlotDiagnostics;
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
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;

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
        if (viewModel.island(islandId) == null) {
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

    void toggleCollectionMembership(String itemId, String comparisonMode, String componentFingerprint, String collectionId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.toggleCollectionMembership(
                workflowRuntime(serverPlayer),
                viewModel,
                itemId,
                comparisonMode,
                componentFingerprint,
                collectionId
        ));
    }

    void createCollection(String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.createCollection(
                workflowRuntime(serverPlayer),
                name
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
        SlotWorkspaceViewModel projected = SlotWorkspaceViewModel.project(
                authority,
                workflowRuntime(serverPlayer).snapshot(),
                status,
                combinedDiagnostics,
                0,
                selected,
                0,
                learnedRules,
                IslandSignalExtractor::extract
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
