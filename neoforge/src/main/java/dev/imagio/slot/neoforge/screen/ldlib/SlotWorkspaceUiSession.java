package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.SlotDiagnostics;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
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
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
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
        return lastViewTag == null ? viewModel.toTag(player.registryAccess()) : lastViewTag.copy();
    }

    void acceptRemoteView(Tag tag) {
        viewModel = SlotWorkspaceViewModel.fromTag(player.registryAccess(), tag);
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

        TransferExecution execution = executeTransfer(serverPlayer, source, destination, origin);
        status = execution.feedback().status();
        diagnostics = execution.feedback().diagnostics();
        broadcast(serverPlayer);
    }

    void assignHome(String itemId, String comparisonMode, String componentFingerprint, String islandId, Integer worldX, Integer worldY) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null || islandId == null || islandId.isBlank()) {
            reject("invalid_home_assignment");
            return;
        }
        SlotWorkspaceViewModel.AtlasItem item = visibleAtlasItem(serverPlayer, identity);
        if (item == null) {
            reject("selected_item_not_visible");
            return;
        }
        applyHomeDrop(serverPlayer, identity, islandId, worldX, worldY, "slot_workspace.ldlib.home_assign");
        broadcast(serverPlayer);
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
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null || chipIslandId == null || chipIslandId.isBlank()) {
            reject("invalid_chip_accept");
            return;
        }
        SlotWorkspaceViewModel.AtlasItem item = visibleAtlasItem(serverPlayer, identity);
        if (item == null) {
            reject("selected_item_not_visible");
            return;
        }
        IslandSuggestionTemplate template = resolveTemplate(templateName);
        String resolvedIslandId;
        if (template != null) {
            resolvedIslandId = resolveOrMaterializeTemplateIsland(serverPlayer, template, identity, item.name());
            if (resolvedIslandId == null) {
                reject("template_island_creation_failed");
                return;
            }
            refreshServerView(serverPlayer);
        } else {
            WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
            if (runtime.visualAtlasWorkflow().visualHomeMap().island(chipIslandId) == null) {
                reject("unknown_island");
                return;
            }
            resolvedIslandId = chipIslandId;
        }
        applyHomeDrop(serverPlayer, identity, resolvedIslandId, null, null, "slot_workspace.ldlib.chip_accept");
        broadcast(serverPlayer);
    }

    private String resolveOrMaterializeTemplateIsland(
            ServerPlayer serverPlayer,
            IslandSuggestionTemplate template,
            ItemIdentity seedIdentity,
            String seedLabel
    ) {
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        VisualAtlasIsland existing = runtime.visualAtlasWorkflow().visualHomeMap().playerIslands().stream()
                .filter(island -> island != null && template.defaultLabel().equalsIgnoreCase(island.label()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return existing.id();
        }
        SlotWorkspaceAtlasLayout.PlayerIslandDraft draft = SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                template.defaultLabel(),
                seedIdentity,
                runtime.visualAtlasWorkflow().visualHomeMap()
        );
        VisualAtlasIsland created = runtime.visualAtlasWorkflow().createIsland(
                draft.label(),
                draft.x(),
                draft.y(),
                draft.width(),
                draft.height(),
                template.defaultColor(),
                seedIdentity,
                DomainEventMetadata.origin("slot_workspace.ldlib.chip_accept.island_create")
        );
        SlotDebugLog.log(
                "LDLib atlas template island materialized {} ({}) for chip accept seed={}",
                created == null ? "null" : created.id(),
                template.name(),
                seedLabel
        );
        return created == null ? null : created.id();
    }

    private static IslandSuggestionTemplate resolveTemplate(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return null;
        }
        try {
            return IslandSuggestionTemplate.valueOf(templateName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
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
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        SlotWorkspaceViewModel.AtlasItem item = visibleAtlasItem(serverPlayer, identity);
        if (identity == null) {
            reject("invalid_island_seed");
            return;
        }
        if (item == null) {
            reject("selected_item_not_visible");
            return;
        }
        String trimmedLabel = label == null ? "" : label.trim();
        if (trimmedLabel.isBlank()) {
            reject("invalid_island_label");
            return;
        }
        if (color == null || worldX == null || worldY == null) {
            reject("invalid_island_placement");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        try {
            VisualAtlasIsland created = runtime.visualAtlasWorkflow().createIsland(
                    trimmedLabel,
                    worldX,
                    worldY,
                    SlotWorkspaceAtlasLayout.PLAYER_ISLAND_MIN_WIDTH,
                    SlotWorkspaceAtlasLayout.PLAYER_ISLAND_MIN_HEIGHT,
                    color,
                    identity,
                    DomainEventMetadata.origin("slot_workspace.ldlib.island_create")
            );
            SlotWorkspaceAtlasLayout.Placement placement = SlotWorkspaceAtlasLayout.placementForOrdinal(
                    SlotWorkspaceAtlasLayout.baseIslands(runtime.visualAtlasWorkflow().visualHomeMap()),
                    created.id(),
                    0
            );
            runtime.visualAtlasWorkflow().assignHome(
                    identity,
                    created.id(),
                    placement.localX(),
                    placement.localY(),
                    VisualHomeOrigin.PLAYER_PLACED,
                    true,
                    DomainEventMetadata.origin("slot_workspace.ldlib.home_assign")
            );
            status = "island created";
            diagnostics = created.label();
            SlotDebugLog.log("LDLib atlas island created {} for {}", created.id(), identity.itemId());
            broadcast(serverPlayer);
        } catch (IllegalArgumentException exception) {
            reject(exception.getMessage());
        }
    }

    void moveIsland(String islandId, Integer worldX, Integer worldY) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (islandId == null || islandId.isBlank() || worldX == null || worldY == null) {
            reject("invalid_island_move");
            return;
        }
        refreshServerView(serverPlayer);
        SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
        if (island == null || island.kind() != dev.imagio.slot.workflow.domain.VisualAtlasIslandKind.PLAYER) {
            reject("unknown_player_island");
            return;
        }
        SlotWorkspaceAtlasLayout.IslandOrigin origin = SlotWorkspaceAtlasLayout.clampIslandOrigin(
                viewModel.islands(),
                islandId,
                worldX,
                worldY
        );
        VisualAtlasIsland moved = workflowRuntime(serverPlayer).visualAtlasWorkflow().moveIsland(
                islandId,
                origin.x(),
                origin.y(),
                DomainEventMetadata.origin("slot_workspace.ldlib.island_move")
        );
        if (moved == null) {
            reject("island_move_rejected");
            return;
        }
        status = "island moved";
        diagnostics = moved.label();
        SlotDebugLog.log("LDLib atlas island moved {} -> {},{}", islandId, origin.x(), origin.y());
        broadcast(serverPlayer);
    }

    void renameIsland(String islandId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (islandId == null || islandId.isBlank() || label == null || label.isBlank()) {
            reject("invalid_island_rename");
            return;
        }
        try {
            VisualAtlasIsland renamed = workflowRuntime(serverPlayer).visualAtlasWorkflow().renameIsland(
                    islandId,
                    label,
                    DomainEventMetadata.origin("slot_workspace.ldlib.island_rename")
            );
            if (renamed == null) {
                reject("island_rename_rejected");
                return;
            }
            status = "island renamed";
            diagnostics = renamed.label();
            SlotDebugLog.log("LDLib atlas island renamed {} -> {}", islandId, renamed.label());
            broadcast(serverPlayer);
        } catch (IllegalArgumentException exception) {
            reject(exception.getMessage());
        }
    }

    void recolorIsland(String islandId, Integer color) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (islandId == null || islandId.isBlank() || color == null) {
            reject("invalid_island_recolor");
            return;
        }
        VisualAtlasIsland recolored = workflowRuntime(serverPlayer).visualAtlasWorkflow().recolorIsland(
                islandId,
                color,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_recolor")
        );
        if (recolored == null) {
            reject("island_recolor_rejected");
            return;
        }
        status = "island recolored";
        diagnostics = Integer.toHexString(recolored.color());
        SlotDebugLog.log("LDLib atlas island recolored {} -> {}", islandId, Integer.toHexString(recolored.color()));
        broadcast(serverPlayer);
    }

    void setIslandIcon(String islandId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (islandId == null || islandId.isBlank()) {
            reject("invalid_island_icon");
            return;
        }
        ItemIdentity iconIdentity = itemId == null || itemId.isBlank()
                ? null
                : resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (itemId != null && !itemId.isBlank() && iconIdentity == null) {
            reject("invalid_icon_identity");
            return;
        }
        VisualAtlasIsland updated = workflowRuntime(serverPlayer).visualAtlasWorkflow().setIslandIcon(
                islandId,
                iconIdentity,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_icon")
        );
        if (updated == null) {
            reject("island_icon_rejected");
            return;
        }
        status = iconIdentity == null ? "island icon cleared" : "island icon set";
        diagnostics = iconIdentity == null ? "" : iconIdentity.itemId();
        SlotDebugLog.log("LDLib atlas island icon {} -> {}", islandId, iconIdentity == null ? "<none>" : iconIdentity.itemId());
        broadcast(serverPlayer);
    }

    void deleteIsland(String islandId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (islandId == null || islandId.isBlank()) {
            reject("invalid_island_delete");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        VisualAtlasIsland existing = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
        if (existing == null || existing.kind() != dev.imagio.slot.workflow.domain.VisualAtlasIslandKind.PLAYER) {
            reject("unknown_player_island");
            return;
        }
        IslandSuggestionTemplate materializedTemplate = matchingTemplate(existing);
        boolean deleted = runtime.visualAtlasWorkflow().deleteIsland(
                islandId,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_delete")
        );
        if (!deleted) {
            reject("island_not_empty");
            return;
        }
        if (materializedTemplate != null) {
            runtime.visualAtlasWorkflow().dismissTemplate(
                    materializedTemplate.defaultIslandId(),
                    DomainEventMetadata.origin("slot_workspace.ldlib.template_dismiss")
            );
        }
        status = "island deleted";
        diagnostics = existing.label();
        SlotDebugLog.log("LDLib atlas island deleted {}{}", islandId,
                materializedTemplate == null ? "" : " (template " + materializedTemplate.defaultIslandId() + " dismissed)");
        broadcast(serverPlayer);
    }

    private static IslandSuggestionTemplate matchingTemplate(VisualAtlasIsland island) {
        if (island == null) {
            return null;
        }
        for (IslandSuggestionTemplate template : IslandSuggestionTemplate.values()) {
            if (template.defaultLabel().equalsIgnoreCase(island.label())
                    && template.defaultColor() == island.color()) {
                return template;
            }
        }
        return null;
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
        TransferExecution execution = executeTransfer(
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
        applyHomeDrop(serverPlayer, identity, islandId, worldX, worldY, "slot_workspace.ldlib.drag.hotbar_home");
        broadcast(serverPlayer);
    }

    void toggleCollectionMembership(String itemId, String comparisonMode, String componentFingerprint, String collectionId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        SlotWorkspaceViewModel.AtlasItem item = visibleAtlasItem(serverPlayer, identity);
        if (identity == null || collectionId == null || collectionId.isBlank()) {
            reject("invalid_collection_toggle");
            return;
        }
        if (item == null) {
            reject("selected_item_not_visible");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        if (viewModel.collections().stream().noneMatch(collection -> collection.collectionId().equals(collectionId))) {
            reject("unknown_collection");
            return;
        }
        boolean currentlyMember = runtime.snapshot().collections().memberships().getOrDefault(identity, java.util.Set.of()).contains(collectionId);
        boolean changed = runtime.collectionWorkflow().toggleCollectionMembership(
                identity,
                collectionId,
                DomainEventMetadata.origin("slot_workspace.ldlib.collection_toggle")
        );
        if (!changed) {
            reject("collection_toggle_rejected");
            return;
        }
        status = currentlyMember ? "removed from collection" : "added to collection";
        diagnostics = collectionId;
        SlotDebugLog.log("LDLib collection toggle {} {} {}", identity.itemId(), collectionId, currentlyMember ? "removed" : "added");
        broadcast(serverPlayer);
    }

    void createCollection(String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        try {
            var definition = workflowRuntime(serverPlayer).collectionWorkflow().createCollection(
                    name,
                    DomainEventMetadata.origin("slot_workspace.ldlib.collection_create")
            );
            status = "collection created";
            diagnostics = definition.name();
            SlotDebugLog.log("LDLib collection created {}", definition.id());
            broadcast(serverPlayer);
        } catch (IllegalArgumentException exception) {
            reject(exception.getMessage());
        }
    }

    static TransferFeedback feedback(InventoryActionRequest request, InventoryActionOutcome outcome) {
        if (outcome == null) {
            return TransferFeedback.rejected("transfer_failed");
        }
        if (!outcome.successful()) {
            return TransferFeedback.rejected(outcome.diagnostics().isBlank()
                    ? outcome.status().name().toLowerCase(java.util.Locale.ROOT)
                    : outcome.diagnostics());
        }

        int requestedCount = request == null || request.requestedCount() <= 0
                ? outcome.requestedCount()
                : request.requestedCount();
        int movedCount = movedCount(request, outcome.stackRemainder(), requestedCount);
        if (requestedCount > 0 && movedCount <= 0) {
            return TransferFeedback.rejected("destination_full_or_incompatible");
        }
        if (outcome.stackRemainder() != null && !outcome.stackRemainder().isEmpty()) {
            return new TransferFeedback("partial transfer applied", "remainder:" + outcome.stackRemainder().getCount());
        }
        return new TransferFeedback("transfer applied", "");
    }

    private static int movedCount(InventoryActionRequest request, net.minecraft.world.item.ItemStack remainder, int requestedCount) {
        if (requestedCount <= 0 || remainder == null || remainder.isEmpty()) {
            return Math.max(0, requestedCount);
        }
        if (request == null || request.identity() == null || ItemIdentityMatcher.matchesMovable(remainder, request.identity())) {
            return Math.max(0, requestedCount - remainder.getCount());
        }
        return requestedCount;
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
                learnedRules
        );
        CompoundTag nextContent = projected.toTag(serverPlayer.registryAccess(), false);
        if (!nextContent.equals(lastContentTag)) {
            lastContentTag = nextContent.copy();
            viewModel = projected.withRevision(nextRevision++);
            lastViewTag = viewModel.toTag(serverPlayer.registryAccess());
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

    private TransferExecution executeTransfer(
            ServerPlayer serverPlayer,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin
    ) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            SlotDiagnostics.workspaceTransferHostMissing(source, destination, serverPlayer.containerMenu);
            return TransferExecution.rejected("host_resolution_failed");
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
            return TransferExecution.rejected(build.diagnostics());
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
        TransferFeedback feedback = feedback(request, outcome);
        SlotDiagnostics.workspaceTransferExecuted(host, request, outcome, feedback.status(), feedback.diagnostics());
        SlotDebugLog.log(
                "LDLib workspace transfer {} {} -> {} {}",
                outcome == null ? "missing_outcome" : outcome.status(),
                source.stableKey(),
                destination.stableKey(),
                feedback.diagnostics()
        );
        return new TransferExecution(host, request, outcome, feedback);
    }

    private void applyHomeDrop(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            String islandId,
            Integer worldX,
            Integer worldY,
            String origin
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            reject("invalid_home_assignment");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
        if (island == null) {
            reject("unknown_island");
            return;
        }
        if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
            runtime.visualAtlasWorkflow().clearHome(
                    identity,
                    DomainEventMetadata.origin(origin + ".clear")
            );
            status = "returned to inbox";
            diagnostics = island.label();
            SlotDebugLog.log("LDLib atlas home cleared {} -> {}", identity.itemId(), islandId);
            return;
        }

        SlotWorkspaceAtlasLayout.Placement placement = resolvePlacement(islandId, worldX, worldY);
        runtime.visualAtlasWorkflow().assignHome(
                identity,
                islandId,
                placement.localX(),
                placement.localY(),
                VisualHomeOrigin.PLAYER_PLACED,
                true,
                DomainEventMetadata.origin(origin)
        );
        recordLearnedAssignment(identity, islandId);
        status = "home assigned";
        diagnostics = island.label();
        SlotDebugLog.log(
                "LDLib atlas home assigned {} -> {} local={},{} atlas={},{}",
                identity.itemId(),
                islandId,
                placement.localX(),
                placement.localY(),
                placement.x(),
                placement.y()
        );
    }

    private void recordLearnedAssignment(ItemIdentity identity, String islandId) {
        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(identity));
        net.minecraft.world.item.ItemStack displayStack = item == null ? null : item.displayStack();
        IslandSignalDescriptor descriptor = IslandSignalExtractor.extract(displayStack);
        if (descriptor.identity() == null || descriptor.identity().itemId() == null || descriptor.identity().itemId().isBlank()) {
            descriptor = new IslandSignalDescriptor(
                    identity,
                    descriptor.classSignals(),
                    descriptor.itemTags(),
                    namespaceOf(identity.itemId()),
                    ""
            );
        }
        learnedRules.recordAssignment(descriptor, islandId, System.currentTimeMillis());
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }

    private SlotWorkspaceAtlasLayout.Placement resolvePlacement(String islandId, Integer worldX, Integer worldY) {
        if (worldX != null && worldY != null && worldX >= 0 && worldY >= 0) {
            return SlotWorkspaceAtlasLayout.placementForDrop(viewModel.islands(), islandId, worldX, worldY);
        }
        long existingCount = viewModel.atlasItems().stream()
                .filter(candidate -> candidate.islandId().equals(islandId))
                .count();
        return SlotWorkspaceAtlasLayout.placementForOrdinal(
                viewModel.islands(),
                islandId,
                Math.toIntExact(existingCount)
        );
    }

    private SlotWorkspaceViewModel.AtlasItem visibleAtlasItem(ServerPlayer serverPlayer, ItemIdentity identity) {
        if (identity == null) {
            return null;
        }
        refreshServerView(serverPlayer);
        return viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(identity));
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

    private static ItemIdentity resolveIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        ItemComparisonMode mode = ItemComparisonMode.ITEM_ID;
        try {
            if (comparisonMode != null && !comparisonMode.isBlank()) {
                mode = ItemComparisonMode.valueOf(comparisonMode);
            }
        } catch (IllegalArgumentException ignored) {
            mode = ItemComparisonMode.ITEM_ID;
        }
        return new ItemIdentity(itemId, mode, componentFingerprint == null ? "" : componentFingerprint);
    }

    record TransferFeedback(
            String status,
            String diagnostics
    ) {
        TransferFeedback {
            status = status == null || status.isBlank() ? "rejected" : status;
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        static TransferFeedback rejected(String diagnostics) {
            return new TransferFeedback("transfer rejected", diagnostics);
        }
    }

    private record TransferExecution(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            InventoryActionOutcome outcome,
            TransferFeedback feedback
    ) {
        private static TransferExecution rejected(String diagnostics) {
            return new TransferExecution(null, null, null, TransferFeedback.rejected(diagnostics));
        }

        private boolean appliedCompletely() {
            return "transfer applied".equals(feedback.status());
        }
    }
}
