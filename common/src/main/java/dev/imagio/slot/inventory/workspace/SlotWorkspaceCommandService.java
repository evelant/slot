package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.LoadoutApplyExecutor;
import dev.imagio.slot.workflow.domain.LoadoutApplyResult;
import dev.imagio.slot.workflow.domain.LoadoutApplyService;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Workspace command domain surface. Every chip-accept, home-assign, island-mgmt, and
 * collection-mgmt decision lives here as a pure static method over a workflow runtime
 * and view-model snapshot. The platform adapter (UI session) handles Minecraft-side
 * concerns (ServerPlayer resolution, live authority refresh, NBT broadcast) but must
 * not re-implement any of these rules.
 */
public final class SlotWorkspaceCommandService {
    private SlotWorkspaceCommandService() {
    }

    public static WorkspaceCommandOutcome assignHome(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String islandId,
            Integer worldX,
            Integer worldY
    ) {
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null || islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_home_assignment");
        }
        if (!visibleInAtlas(viewModel, identity)) {
            return WorkspaceCommandOutcome.rejected("selected_item_not_visible");
        }
        return applyHomeDrop(
                runtime,
                viewModel,
                learnedRules,
                signalExtractor,
                identity,
                islandId,
                worldX,
                worldY,
                "slot_workspace.ldlib.home_assign"
        );
    }

    public static WorkspaceCommandOutcome acceptChip(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String chipIslandId,
            String templateName
    ) {
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null || chipIslandId == null || chipIslandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_chip_accept");
        }
        SlotWorkspaceViewModel.AtlasItem item = viewModel == null ? null
                : viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(identity));
        if (item == null) {
            return WorkspaceCommandOutcome.rejected("selected_item_not_visible");
        }
        IslandSuggestionTemplate template = resolveTemplate(templateName);
        String resolvedIslandId;
        if (template != null) {
            resolvedIslandId = resolveOrMaterializeTemplateIsland(runtime, template, identity, item.name());
            if (resolvedIslandId == null) {
                return WorkspaceCommandOutcome.rejected("template_island_creation_failed");
            }
        } else {
            if (runtime.visualAtlasWorkflow().visualHomeMap().island(chipIslandId) == null) {
                return WorkspaceCommandOutcome.rejected("unknown_island");
            }
            resolvedIslandId = chipIslandId;
        }
        return applyHomeDrop(
                runtime,
                viewModel,
                learnedRules,
                signalExtractor,
                identity,
                resolvedIslandId,
                null,
                null,
                "slot_workspace.ldlib.chip_accept"
        );
    }

    public static WorkspaceCommandOutcome createNamedIslandForItem(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String label,
            Integer color,
            Integer worldX,
            Integer worldY
    ) {
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_island_seed");
        }
        if (!visibleInAtlas(viewModel, identity)) {
            return WorkspaceCommandOutcome.rejected("selected_item_not_visible");
        }
        String trimmedLabel = label == null ? "" : label.trim();
        if (trimmedLabel.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_island_label");
        }
        if (color == null || worldX == null || worldY == null) {
            return WorkspaceCommandOutcome.rejected("invalid_island_placement");
        }
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
            SlotDebugLog.log("LDLib atlas island created {} for {}", created.id(), identity.itemId());
            recordLearnedAssignment(viewModel, learnedRules, signalExtractor, identity, created.id());
            return WorkspaceCommandOutcome.accepted("island created", created.label());
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected(exception.getMessage());
        }
    }

    public static WorkspaceCommandOutcome moveIsland(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String islandId,
            Integer worldX,
            Integer worldY
    ) {
        if (islandId == null || islandId.isBlank() || worldX == null || worldY == null) {
            return WorkspaceCommandOutcome.rejected("invalid_island_move");
        }
        SlotWorkspaceViewModel.AtlasIsland island = viewModel == null ? null : viewModel.island(islandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            return WorkspaceCommandOutcome.rejected("unknown_player_island");
        }
        VisualAtlasIsland moved = runtime.visualAtlasWorkflow().moveIsland(
                islandId,
                worldX,
                worldY,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_move")
        );
        if (moved == null) {
            return WorkspaceCommandOutcome.rejected("island_move_rejected");
        }
        SlotDebugLog.log("LDLib atlas island moved {} -> {},{}", islandId, worldX, worldY);
        return WorkspaceCommandOutcome.accepted("island moved", moved.label());
    }

    public static WorkspaceCommandOutcome linkIslandToChest(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String islandId,
            String storageId
    ) {
        if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_link");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_storage_id");
        }
        SlotWorkspaceViewModel.AtlasIsland island = viewModel == null ? null : viewModel.island(islandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            return WorkspaceCommandOutcome.rejected("unknown_player_island");
        }
        SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel.claimedChestTile(storageId);
        if (tile == null) {
            return WorkspaceCommandOutcome.rejected("unknown_chest_tile");
        }
        boolean linked = runtime.chestLinkWorkflow().linkIslandToChest(
                islandId,
                uuid,
                DomainEventMetadata.origin("slot_workspace.ldlib.chest_link")
        );
        if (!linked) {
            return WorkspaceCommandOutcome.rejected("chest_link_rejected");
        }
        SlotDebugLog.log("LDLib chest link created {} <-> {}", islandId, storageId);
        return WorkspaceCommandOutcome.accepted("chest linked", island.label());
    }

    public static WorkspaceCommandOutcome unlinkIslandFromChest(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String islandId,
            String storageId
    ) {
        if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_unlink");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_storage_id");
        }
        boolean unlinked = runtime.chestLinkWorkflow().unlinkIslandFromChest(
                islandId,
                uuid,
                DomainEventMetadata.origin("slot_workspace.ldlib.chest_unlink")
        );
        if (!unlinked) {
            return WorkspaceCommandOutcome.rejected("chest_unlink_rejected");
        }
        String label = viewModel == null || viewModel.island(islandId) == null
                ? islandId
                : viewModel.island(islandId).label();
        SlotDebugLog.log("LDLib chest link removed {} <-> {}", islandId, storageId);
        return WorkspaceCommandOutcome.accepted("chest unlinked", label);
    }

    public static WorkspaceCommandOutcome relabelChest(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String storageId,
            String label
    ) {
        if (storageId == null || storageId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_relabel");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_storage_id");
        }
        SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel == null ? null : viewModel.claimedChestTile(storageId);
        if (tile == null) {
            return WorkspaceCommandOutcome.rejected("unknown_chest_tile");
        }
        String normalized = label == null ? "" : label.trim();
        ClaimedChest relabeled = runtime.chestClaimWorkflow().relabelChest(
                uuid,
                normalized,
                DomainEventMetadata.origin("slot_workspace.ldlib.chest_relabel")
        );
        if (relabeled == null) {
            return WorkspaceCommandOutcome.rejected("chest_relabel_rejected");
        }
        SlotDebugLog.log("LDLib chest relabeled {} -> '{}'", storageId, normalized);
        return WorkspaceCommandOutcome.accepted("chest renamed", normalized.isBlank() ? storageId : normalized);
    }

    public static WorkspaceCommandOutcome moveChest(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String storageId,
            Integer atlasX,
            Integer atlasY
    ) {
        if (storageId == null || storageId.isBlank() || atlasX == null || atlasY == null) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_move");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_storage_id");
        }
        SlotWorkspaceViewModel.ClaimedChestTile tile = viewModel == null ? null : viewModel.claimedChestTile(storageId);
        if (tile == null) {
            return WorkspaceCommandOutcome.rejected("unknown_chest_tile");
        }
        ClaimedChest moved = runtime.chestClaimWorkflow().moveChest(
                uuid,
                atlasX,
                atlasY,
                DomainEventMetadata.origin("slot_workspace.ldlib.chest_move")
        );
        if (moved == null) {
            return WorkspaceCommandOutcome.rejected("chest_move_rejected");
        }
        SlotDebugLog.log("LDLib atlas chest moved {} -> {},{}", storageId, atlasX, atlasY);
        return WorkspaceCommandOutcome.accepted("chest moved", storageId);
    }

    public static WorkspaceCommandOutcome renameIsland(
            WorkflowDomainRuntime runtime,
            String islandId,
            String label
    ) {
        if (islandId == null || islandId.isBlank() || label == null || label.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_island_rename");
        }
        try {
            VisualAtlasIsland renamed = runtime.visualAtlasWorkflow().renameIsland(
                    islandId,
                    label,
                    DomainEventMetadata.origin("slot_workspace.ldlib.island_rename")
            );
            if (renamed == null) {
                return WorkspaceCommandOutcome.rejected("island_rename_rejected");
            }
            SlotDebugLog.log("LDLib atlas island renamed {} -> {}", islandId, renamed.label());
            return WorkspaceCommandOutcome.accepted("island renamed", renamed.label());
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected(exception.getMessage());
        }
    }

    public static WorkspaceCommandOutcome recolorIsland(
            WorkflowDomainRuntime runtime,
            String islandId,
            Integer color
    ) {
        if (islandId == null || islandId.isBlank() || color == null) {
            return WorkspaceCommandOutcome.rejected("invalid_island_recolor");
        }
        VisualAtlasIsland recolored = runtime.visualAtlasWorkflow().recolorIsland(
                islandId,
                color,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_recolor")
        );
        if (recolored == null) {
            return WorkspaceCommandOutcome.rejected("island_recolor_rejected");
        }
        String hex = Integer.toHexString(recolored.color());
        SlotDebugLog.log("LDLib atlas island recolored {} -> {}", islandId, hex);
        return WorkspaceCommandOutcome.accepted("island recolored", hex);
    }

    public static WorkspaceCommandOutcome setIslandIcon(
            WorkflowDomainRuntime runtime,
            String islandId,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_island_icon");
        }
        ItemIdentity iconIdentity = itemId == null || itemId.isBlank()
                ? null
                : resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (itemId != null && !itemId.isBlank() && iconIdentity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_icon_identity");
        }
        VisualAtlasIsland updated = runtime.visualAtlasWorkflow().setIslandIcon(
                islandId,
                iconIdentity,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_icon")
        );
        if (updated == null) {
            return WorkspaceCommandOutcome.rejected("island_icon_rejected");
        }
        SlotDebugLog.log("LDLib atlas island icon {} -> {}", islandId, iconIdentity == null ? "<none>" : iconIdentity.itemId());
        return WorkspaceCommandOutcome.accepted(
                iconIdentity == null ? "island icon cleared" : "island icon set",
                iconIdentity == null ? "" : iconIdentity.itemId()
        );
    }

    public static WorkspaceCommandOutcome saveBeltAsKit(
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            String name
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_runtime");
        }
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank()) {
            trimmed = defaultKitName(runtime);
        }
        try {
            KitDefinition created = runtime.kitWorkflow().snapshotFromAuthority(
                    trimmed,
                    authority == null ? InventoryAuthoritySnapshot.empty() : authority,
                    identityResolver,
                    DomainEventMetadata.origin("slot_workspace.ldlib.kit_snapshot")
            );
            if (created == null) {
                return WorkspaceCommandOutcome.rejected("kit_snapshot_rejected");
            }
            SlotDebugLog.log("LDLib kit snapshot created {} ({} slots filled)",
                    created.id(), created.pages().get(0).filledSlotCount());
            return WorkspaceCommandOutcome.accepted("kit saved", created.name());
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected(exception.getMessage());
        }
    }

    public static WorkspaceCommandOutcome activateKit(
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            String kitId
    ) {
        if (runtime == null || actionExecutor == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_activation");
        }
        if (kitId == null || kitId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_id");
        }
        KitDefinition kit = runtime.kitWorkflow().kit(kitId);
        if (kit == null) {
            return WorkspaceCommandOutcome.rejected("unknown_kit");
        }
        LoadoutApplyService.LoadoutApplyPlan plan = runtime.kitWorkflow().planActivate(
                kitId,
                0,
                authority == null ? InventoryAuthoritySnapshot.empty() : authority,
                protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy,
                identityResolver
        );
        LoadoutApplyResult result = new LoadoutApplyExecutor(actionExecutor).execute(plan);
        runtime.kitWorkflow().activate(
                kitId,
                0,
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_activate")
        );
        int satisfied = result.satisfiedTargets().size();
        int missing = result.missingTargets().size();
        String planReasons = result.diagnostics().isEmpty() ? "" : String.join(",", result.diagnostics());
        SlotDebugLog.log("LDLib kit activated {} satisfied={} missing={} reasons={}",
                kitId, satisfied, missing, planReasons);
        StringBuilder diagnostics = new StringBuilder()
                .append("satisfied=").append(satisfied)
                .append(" missing=").append(missing);
        if (!planReasons.isBlank()) {
            diagnostics.append(" reasons=").append(planReasons);
        }
        String status = missing == 0 ? "kit activated" : "kit activated (missing " + missing + ")";
        return WorkspaceCommandOutcome.accepted(status, diagnostics.toString());
    }

    public static WorkspaceCommandOutcome deactivateKit(WorkflowDomainRuntime runtime) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_runtime");
        }
        if (!runtime.kitWorkflow().activation().isActive()) {
            return WorkspaceCommandOutcome.rejected("no_active_kit");
        }
        runtime.kitWorkflow().deactivate(
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_deactivate")
        );
        SlotDebugLog.log("LDLib kit deactivated");
        return WorkspaceCommandOutcome.accepted("kit deactivated", "");
    }

    public static WorkspaceCommandOutcome deleteKit(WorkflowDomainRuntime runtime, String kitId) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_runtime");
        }
        if (kitId == null || kitId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_id");
        }
        KitDefinition existing = runtime.kitWorkflow().kit(kitId);
        if (existing == null) {
            return WorkspaceCommandOutcome.rejected("unknown_kit");
        }
        runtime.kitWorkflow().delete(
                kitId,
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_delete")
        );
        SlotDebugLog.log("LDLib kit deleted {}", kitId);
        return WorkspaceCommandOutcome.accepted("kit deleted", existing.name());
    }

    private static String defaultKitName(WorkflowDomainRuntime runtime) {
        int count = runtime.kitWorkflow().kits().size();
        return "Kit " + (count + 1);
    }

    public static WorkspaceCommandOutcome deleteIsland(
            WorkflowDomainRuntime runtime,
            String islandId
    ) {
        if (islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_island_delete");
        }
        VisualAtlasIsland existing = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
        if (existing == null || existing.kind() != VisualAtlasIslandKind.PLAYER) {
            return WorkspaceCommandOutcome.rejected("unknown_player_island");
        }
        IslandSuggestionTemplate materializedTemplate = matchingTemplate(existing);
        boolean deleted = runtime.visualAtlasWorkflow().deleteIsland(
                islandId,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_delete")
        );
        if (!deleted) {
            return WorkspaceCommandOutcome.rejected("island_not_empty");
        }
        if (materializedTemplate != null) {
            runtime.visualAtlasWorkflow().dismissTemplate(
                    materializedTemplate.defaultIslandId(),
                    DomainEventMetadata.origin("slot_workspace.ldlib.template_dismiss")
            );
        }
        SlotDebugLog.log("LDLib atlas island deleted {}{}", islandId,
                materializedTemplate == null ? "" : " (template " + materializedTemplate.defaultIslandId() + " dismissed)");
        return WorkspaceCommandOutcome.accepted("island deleted", existing.label());
    }


    /**
     * Apply the "drop identity onto island" rule: triage target clears the home;
     * anything else assigns at the resolved placement and records a learned rule.
     * Exposed publicly so the hotbar-to-atlas flow (which first runs a transfer)
     * can re-use the same rule after the stack arrives in the main inventory.
     */
    public static WorkspaceCommandOutcome applyHomeDrop(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            ItemIdentity identity,
            String islandId,
            Integer worldX,
            Integer worldY,
            String origin
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_home_assignment");
        }
        SlotWorkspaceViewModel.AtlasIsland island = viewModel == null ? null : viewModel.island(islandId);
        if (island == null) {
            return WorkspaceCommandOutcome.rejected("unknown_island");
        }
        if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
            runtime.visualAtlasWorkflow().clearHome(
                    identity,
                    DomainEventMetadata.origin(origin + ".clear")
            );
            SlotDebugLog.log("LDLib atlas home cleared {} -> {}", identity.itemId(), islandId);
            return WorkspaceCommandOutcome.accepted("returned to inbox", island.label());
        }

        SlotWorkspaceAtlasLayout.Placement placement = resolvePlacement(viewModel, islandId, worldX, worldY);
        runtime.visualAtlasWorkflow().assignHome(
                identity,
                islandId,
                placement.localX(),
                placement.localY(),
                VisualHomeOrigin.PLAYER_PLACED,
                true,
                DomainEventMetadata.origin(origin)
        );
        recordLearnedAssignment(viewModel, learnedRules, signalExtractor, identity, islandId);
        SlotDebugLog.log(
                "LDLib atlas home assigned {} -> {} local={},{} atlas={},{}",
                identity.itemId(),
                islandId,
                placement.localX(),
                placement.localY(),
                placement.x(),
                placement.y()
        );
        return WorkspaceCommandOutcome.accepted("home assigned", island.label());
    }

    static ItemIdentity resolveIdentity(String itemId, String comparisonMode, String componentFingerprint) {
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

    static IslandSuggestionTemplate resolveTemplate(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return null;
        }
        try {
            return IslandSuggestionTemplate.valueOf(templateName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static IslandSuggestionTemplate matchingTemplate(VisualAtlasIsland island) {
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

    private static String resolveOrMaterializeTemplateIsland(
            WorkflowDomainRuntime runtime,
            IslandSuggestionTemplate template,
            ItemIdentity seedIdentity,
            String seedLabel
    ) {
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

    private static void recordLearnedAssignment(
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            ItemIdentity identity,
            String islandId
    ) {
        if (learnedRules == null || signalExtractor == null) {
            return;
        }
        SlotWorkspaceViewModel.AtlasItem item = viewModel == null ? null
                : viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(identity));
        ItemStack displayStack = item == null ? null : item.displayStack();
        IslandSignalDescriptor descriptor = signalExtractor.apply(displayStack);
        if (descriptor == null) {
            return;
        }
        if (descriptor.identity() == null
                || descriptor.identity().itemId() == null
                || descriptor.identity().itemId().isBlank()) {
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

    private static boolean visibleInAtlas(SlotWorkspaceViewModel viewModel, ItemIdentity identity) {
        if (identity == null || viewModel == null) {
            return false;
        }
        return viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(identity)) != null;
    }

    private static SlotWorkspaceAtlasLayout.Placement resolvePlacement(
            SlotWorkspaceViewModel viewModel,
            String islandId,
            Integer worldX,
            Integer worldY
    ) {
        if (viewModel == null) {
            return new SlotWorkspaceAtlasLayout.Placement(
                    islandId,
                    SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X,
                    SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP,
                    0,
                    0
            );
        }
        if (worldX != null && worldY != null) {
            return SlotWorkspaceAtlasLayout.placementForDrop(viewModel.islands(), islandId, worldX, worldY);
        }
        return SlotWorkspaceAtlasLayout.placementForOrdinal(
                viewModel.islands(),
                islandId,
                0
        );
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
