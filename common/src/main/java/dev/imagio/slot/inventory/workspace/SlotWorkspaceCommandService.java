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
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.triage.WithinIslandOrdering;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.DesiredCountWorkflowDomainService;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.KitSnapshotSupport;
import dev.imagio.slot.workflow.domain.LoadoutApplyExecutor;
import dev.imagio.slot.workflow.domain.LoadoutApplyResult;
import dev.imagio.slot.workflow.domain.LoadoutApplyService;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.undo.UndoContext;
import dev.imagio.slot.workflow.domain.undo.UndoRecord;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
            Integer ordinal
    ) {
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null || islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_home_assignment");
        }
        if (!visibleInAtlas(viewModel, identity)) {
            return WorkspaceCommandOutcome.rejected("selected_item_not_visible");
        }
        final ItemIdentity targetIdentity = identity;
        VisualHomeAssignment before = runtime.visualAtlasWorkflow().visualHomeMap().assignment(targetIdentity);
        WorkspaceCommandOutcome outcome = applyHomeDrop(
                runtime,
                viewModel,
                learnedRules,
                signalExtractor,
                identity,
                islandId,
                ordinal,
                "slot_workspace.ldlib.home_assign"
        );
        if (outcome.success()) {
            VisualHomeAssignment after = runtime.visualAtlasWorkflow().visualHomeMap().assignment(targetIdentity);
            String label = after == null ? "return to inbox" : "move home";
            runtime.undoStack().record(
                    label,
                    ctx -> restoreHomeAssignment(ctx.runtime(), targetIdentity, before),
                    ctx -> restoreHomeAssignment(ctx.runtime(), targetIdentity, after)
            );
        }
        return outcome;
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
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        SlotWorkspaceViewModel.AtlasItem item = viewModel == null ? null : viewModel.atlasItem(ref);
        if (item == null && viewModel != null) {
            // Allow loot-chest-panel identities — they're Triage-equivalent
            // for chip acceptance even though they aren't yet in the carry
            // pane's atlasItems.
            for (SlotWorkspaceViewModel.AtlasItem candidate : viewModel.lootChestPanel().items()) {
                if (ref.equals(candidate.identity())) {
                    item = candidate;
                    break;
                }
            }
        }
        if (item == null) {
            return WorkspaceCommandOutcome.rejected("selected_item_not_visible");
        }
        IslandSuggestionTemplate template = resolveTemplate(templateName);
        String resolvedIslandId;
        boolean materializedNewIsland = false;
        if (template != null) {
            VisualAtlasIsland templateIslandBefore = runtime.visualAtlasWorkflow().visualHomeMap()
                    .playerIslands().stream()
                    .filter(isl -> isl != null && template.defaultLabel().equalsIgnoreCase(isl.label()))
                    .findFirst()
                    .orElse(null);
            resolvedIslandId = resolveOrMaterializeTemplateIsland(runtime, template, identity, item.name());
            if (resolvedIslandId == null) {
                return WorkspaceCommandOutcome.rejected("template_island_creation_failed");
            }
            materializedNewIsland = (templateIslandBefore == null);
        } else {
            if (runtime.visualAtlasWorkflow().visualHomeMap().island(chipIslandId) == null) {
                return WorkspaceCommandOutcome.rejected("unknown_island");
            }
            resolvedIslandId = chipIslandId;
        }
        final ItemIdentity targetIdentity = identity;
        final String targetIslandId = resolvedIslandId;
        VisualHomeAssignment before = runtime.visualAtlasWorkflow().visualHomeMap().assignment(targetIdentity);
        WorkspaceCommandOutcome outcome = applyHomeDrop(
                runtime,
                viewModel,
                learnedRules,
                signalExtractor,
                identity,
                resolvedIslandId,
                null,
                "slot_workspace.ldlib.chip_accept"
        );
        if (outcome.success()) {
            VisualHomeAssignment after = runtime.visualAtlasWorkflow().visualHomeMap().assignment(targetIdentity);
            VisualAtlasIsland createdIslandSnapshot = materializedNewIsland
                    ? runtime.visualAtlasWorkflow().visualHomeMap().island(targetIslandId)
                    : null;
            String label = materializedNewIsland ? "accept suggestion" : "accept chip";
            runtime.undoStack().record(
                    label,
                    ctx -> {
                        restoreHomeAssignment(ctx.runtime(), targetIdentity, before);
                        if (createdIslandSnapshot != null) {
                            ctx.runtime().visualAtlasWorkflow().deleteIsland(
                                    targetIslandId,
                                    DomainEventMetadata.origin("workflow.undo.chip_accept.delete_island")
                            );
                        }
                    },
                    ctx -> {
                        if (createdIslandSnapshot != null) {
                            recreateIslandFromSnapshot(ctx.runtime(), createdIslandSnapshot);
                        }
                        restoreHomeAssignment(ctx.runtime(), targetIdentity, after);
                    }
            );
        }
        return outcome;
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
        final ItemIdentity targetIdentity = identity;
        VisualHomeAssignment before = runtime.visualAtlasWorkflow().visualHomeMap().assignment(targetIdentity);
        try {
            VisualAtlasIsland created = runtime.visualAtlasWorkflow().createIsland(
                    trimmedLabel,
                    worldX,
                    worldY,
                    color,
                    identity,
                    DomainEventMetadata.origin("slot_workspace.ldlib.island_create")
            );
            runtime.visualAtlasWorkflow().assignHome(
                    identity,
                    created.id(),
                    0,
                    VisualHomeOrigin.PLAYER_PLACED,
                    true,
                    DomainEventMetadata.origin("slot_workspace.ldlib.home_assign")
            );
            SlotDebugLog.log("LDLib atlas island created {} for {}", created.id(), identity.itemId());
            recordLearnedAssignment(viewModel, learnedRules, signalExtractor, identity, created.id());
            VisualAtlasIsland createdSnapshot = runtime.visualAtlasWorkflow().visualHomeMap().island(created.id());
            VisualHomeAssignment after = runtime.visualAtlasWorkflow().visualHomeMap().assignment(targetIdentity);
            final String createdId = created.id();
            runtime.undoStack().record(
                    "create island",
                    ctx -> {
                        restoreHomeAssignment(ctx.runtime(), targetIdentity, before);
                        ctx.runtime().visualAtlasWorkflow().deleteIsland(
                                createdId,
                                DomainEventMetadata.origin("workflow.undo.island_create.delete")
                        );
                    },
                    ctx -> {
                        recreateIslandFromSnapshot(ctx.runtime(), createdSnapshot);
                        restoreHomeAssignment(ctx.runtime(), targetIdentity, after);
                    }
            );
            return WorkspaceCommandOutcome.accepted("island created", created.label());
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected(exception.getMessage());
        }
    }

    public static WorkspaceCommandOutcome moveIsland(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String islandId,
            Double worldX,
            Double worldY
    ) {
        if (islandId == null || islandId.isBlank() || worldX == null || worldY == null) {
            return WorkspaceCommandOutcome.rejected("invalid_island_move");
        }
        SlotWorkspaceViewModel.AtlasIsland island = viewModel == null ? null : viewModel.island(islandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            return WorkspaceCommandOutcome.rejected("unknown_player_island");
        }
        VisualAtlasIsland before = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
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
        if (before != null && (before.x() != moved.x() || before.y() != moved.y())) {
            final String targetId = islandId;
            final double beforeX = before.x();
            final double beforeY = before.y();
            final double afterX = moved.x();
            final double afterY = moved.y();
            runtime.undoStack().record(
                    "move island",
                    ctx -> ctx.runtime().visualAtlasWorkflow().moveIsland(
                            targetId, beforeX, beforeY,
                            DomainEventMetadata.origin("workflow.undo.island_move.restore")
                    ),
                    ctx -> ctx.runtime().visualAtlasWorkflow().moveIsland(
                            targetId, afterX, afterY,
                            DomainEventMetadata.origin("workflow.undo.island_move.replay")
                    )
            );
        }
        return WorkspaceCommandOutcome.accepted("island moved", moved.label());
    }

    public static WorkspaceCommandOutcome reorderIsland(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            String islandId,
            Integer targetIndex
    ) {
        if (runtime == null || islandId == null || islandId.isBlank() || targetIndex == null) {
            return WorkspaceCommandOutcome.rejected("invalid_island_reorder");
        }
        SlotWorkspaceViewModel.AtlasIsland island = viewModel == null ? null : viewModel.island(islandId);
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            return WorkspaceCommandOutcome.rejected("unknown_player_island");
        }
        int beforeIndex = runtime.visualAtlasWorkflow().playerIslandIndex(islandId);
        if (beforeIndex < 0) {
            return WorkspaceCommandOutcome.rejected("unknown_player_island");
        }
        VisualAtlasIsland reordered = runtime.visualAtlasWorkflow().reorderIsland(
                islandId,
                targetIndex,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_reorder")
        );
        if (reordered == null) {
            return WorkspaceCommandOutcome.rejected("island_reorder_rejected");
        }
        int afterIndex = runtime.visualAtlasWorkflow().playerIslandIndex(islandId);
        SlotDebugLog.log("LDLib atlas island reordered {} -> {}", islandId, afterIndex);
        if (afterIndex != beforeIndex) {
            final String targetId = islandId;
            final int previousIndex = beforeIndex;
            final int nextIndex = afterIndex;
            runtime.undoStack().record(
                    "reorder section",
                    ctx -> ctx.runtime().visualAtlasWorkflow().reorderIsland(
                            targetId, previousIndex,
                            DomainEventMetadata.origin("workflow.undo.island_reorder.restore")
                    ),
                    ctx -> ctx.runtime().visualAtlasWorkflow().reorderIsland(
                            targetId, nextIndex,
                            DomainEventMetadata.origin("workflow.undo.island_reorder.replay")
                    )
            );
        }
        return WorkspaceCommandOutcome.accepted("section reordered", reordered.label());
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

    public static WorkspaceCommandOutcome relabelCluster(
            WorkflowDomainRuntime runtime,
            String clusterId,
            String label
    ) {
        if (runtime == null || clusterId == null || clusterId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_cluster_relabel");
        }
        boolean ok = runtime.chestClaimWorkflow().relabelCluster(clusterId, label);
        if (!ok) {
            return WorkspaceCommandOutcome.rejected("cluster_relabel_rejected");
        }
        String normalized = label == null ? "" : label.trim();
        SlotDebugLog.log("LDLib cluster relabeled {} -> '{}'", clusterId, normalized);
        return WorkspaceCommandOutcome.accepted("cluster renamed", normalized.isBlank() ? clusterId : normalized);
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
        SlotWorkspaceViewModel.ChestChip chip = viewModel == null ? null : viewModel.chestChip(storageId);
        if (chip == null) {
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

    /** Forget every affinity bond for this chest (player "Forget chest" gesture). */
    public static WorkspaceCommandOutcome forgetChest(
            WorkflowDomainRuntime runtime,
            String storageId
    ) {
        if (storageId == null || storageId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_forget");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_storage_id");
        }
        // Snapshot the chest record + every affinity bond BEFORE we delete
        // them, so the undo lambda can rebuild both. Without this the
        // forget gesture is a one-way trapdoor — discoverable accidents
        // (right-click a chip) would lose the player's accumulated
        // affinity history with no recovery short of redepositing every
        // identity.
        dev.imagio.slot.workflow.domain.ClaimedChest claimBefore =
                runtime.chestClaimWorkflow().claimedChestMap().chest(uuid);
        java.util.Map<dev.imagio.slot.inventory.core.ItemIdentity,
                dev.imagio.slot.workflow.domain.ChestAffinity> affinityBefore = new java.util.LinkedHashMap<>(
                runtime.chestClaimWorkflow().chestAffinityMap().forChest(uuid));
        boolean cleared = runtime.chestClaimWorkflow().forgetChestAffinity(uuid);
        boolean deleted = runtime.chestClaimWorkflow().deleteChest(uuid);
        if (!cleared && !deleted) {
            return WorkspaceCommandOutcome.rejected("chest_forget_rejected");
        }
        runtime.undoStack().record(
                "forget chest",
                ctx -> reapplyForgetChest(ctx.runtime(), uuid),
                ctx -> reinstateChest(ctx.runtime(), claimBefore, affinityBefore)
        );
        SlotDebugLog.log("LDLib chest forgotten {}", storageId);
        return WorkspaceCommandOutcome.accepted("chest forgotten", storageId);
    }

    private static void reapplyForgetChest(WorkflowDomainRuntime runtime, UUID storageId) {
        runtime.chestClaimWorkflow().forgetChestAffinity(storageId);
        runtime.chestClaimWorkflow().deleteChest(storageId);
    }

    private static void reinstateChest(
            WorkflowDomainRuntime runtime,
            dev.imagio.slot.workflow.domain.ClaimedChest claim,
            java.util.Map<dev.imagio.slot.inventory.core.ItemIdentity,
                    dev.imagio.slot.workflow.domain.ChestAffinity> affinity
    ) {
        if (claim == null) {
            return;
        }
        runtime.chestClaimWorkflow().claimWithId(
                claim.storageId(),
                claim.anchors(),
                claim.atlasX(),
                claim.atlasY(),
                claim.label()
        );
        for (java.util.Map.Entry<dev.imagio.slot.inventory.core.ItemIdentity,
                dev.imagio.slot.workflow.domain.ChestAffinity> entry : affinity.entrySet()) {
            dev.imagio.slot.workflow.domain.ChestAffinity bond = entry.getValue();
            runtime.chestClaimWorkflow().recordDeposit(
                    claim.storageId(),
                    entry.getKey(),
                    bond.score(),
                    bond.lastTouchedTick()
            );
        }
    }

    /** Forget affinity[storageId, identity]. Targeted "this chest doesn't hold X anymore". */
    public static WorkspaceCommandOutcome forgetItemAffinity(
            WorkflowDomainRuntime runtime,
            String storageId,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (storageId == null || storageId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_item_forget");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("invalid_chest_storage_id");
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        boolean forgotten = runtime.chestClaimWorkflow().forgetIdentity(uuid, identity);
        if (!forgotten) {
            return WorkspaceCommandOutcome.rejected("item_forget_rejected");
        }
        return WorkspaceCommandOutcome.accepted("affinity forgotten", identity.itemId());
    }

    public static WorkspaceCommandOutcome renameIsland(
            WorkflowDomainRuntime runtime,
            String islandId,
            String label
    ) {
        if (islandId == null || islandId.isBlank() || label == null || label.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_island_rename");
        }
        VisualAtlasIsland before = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
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
            if (before != null && !before.label().equals(renamed.label())) {
                final String targetId = islandId;
                final String beforeLabel = before.label();
                final String afterLabel = renamed.label();
                runtime.undoStack().record(
                        "rename island",
                        ctx -> ctx.runtime().visualAtlasWorkflow().renameIsland(
                                targetId, beforeLabel,
                                DomainEventMetadata.origin("workflow.undo.island_rename.restore")
                        ),
                        ctx -> ctx.runtime().visualAtlasWorkflow().renameIsland(
                                targetId, afterLabel,
                                DomainEventMetadata.origin("workflow.undo.island_rename.replay")
                        )
                );
            }
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
        VisualAtlasIsland before = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
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
        if (before != null && before.color() != recolored.color()) {
            final String targetId = islandId;
            final int beforeColor = before.color();
            final int afterColor = recolored.color();
            runtime.undoStack().record(
                    "recolor island",
                    ctx -> ctx.runtime().visualAtlasWorkflow().recolorIsland(
                            targetId, beforeColor,
                            DomainEventMetadata.origin("workflow.undo.island_recolor.restore")
                    ),
                    ctx -> ctx.runtime().visualAtlasWorkflow().recolorIsland(
                            targetId, afterColor,
                            DomainEventMetadata.origin("workflow.undo.island_recolor.replay")
                    )
            );
        }
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
        VisualAtlasIsland before = runtime.visualAtlasWorkflow().visualHomeMap().island(islandId);
        VisualAtlasIsland updated = runtime.visualAtlasWorkflow().setIslandIcon(
                islandId,
                iconIdentity,
                DomainEventMetadata.origin("slot_workspace.ldlib.island_icon")
        );
        if (updated == null) {
            return WorkspaceCommandOutcome.rejected("island_icon_rejected");
        }
        SlotDebugLog.log("LDLib atlas island icon {} -> {}", islandId, iconIdentity == null ? "<none>" : iconIdentity.itemId());
        if (before != null && !Objects.equals(before.iconIdentity(), updated.iconIdentity())) {
            final String targetId = islandId;
            final ItemIdentity beforeIcon = before.iconIdentity();
            final ItemIdentity afterIcon = updated.iconIdentity();
            runtime.undoStack().record(
                    iconIdentity == null ? "clear island icon" : "set island icon",
                    ctx -> ctx.runtime().visualAtlasWorkflow().setIslandIcon(
                            targetId, beforeIcon,
                            DomainEventMetadata.origin("workflow.undo.island_icon.restore")
                    ),
                    ctx -> ctx.runtime().visualAtlasWorkflow().setIslandIcon(
                            targetId, afterIcon,
                            DomainEventMetadata.origin("workflow.undo.island_icon.replay")
                    )
            );
        }
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
        InventoryAuthoritySnapshot resolvedAuthority = authority == null
                ? InventoryAuthoritySnapshot.empty() : authority;
        // If a kit is active, treat "Save Current Belt" as "update active kit's current page"
        // so the button stays in-place instead of silently forking a new kit.
        var activation = runtime.kitWorkflow().activation();
        if (activation.isActive()) {
            KitDefinition activeKit = runtime.kitWorkflow().kit(activation.kitId());
            if (activeKit != null) {
                try {
                    int pageIndex = Math.max(0, Math.min(activation.pageIndex(), activeKit.pageCount() - 1));
                    KitPage capturedPage = KitSnapshotSupport.capturePageFromAuthority(
                            resolvedAuthority, identityResolver);
                    ItemIdentity offhand = KitSnapshotSupport.captureOffhandIdentity(
                            resolvedAuthority, identityResolver);
                    KitDefinition next = activeKit
                            .withPageReplaced(pageIndex, capturedPage)
                            .withOffhand(offhand);
                    if (!runtime.kitWorkflow().update(next,
                            DomainEventMetadata.origin("slot_workspace.ldlib.kit_page_update"))) {
                        return WorkspaceCommandOutcome.accepted("kit page unchanged", activeKit.name());
                    }
                    SlotDebugLog.log("LDLib kit page updated {} page={}", activeKit.id(), pageIndex);
                    return WorkspaceCommandOutcome.accepted(
                            "kit page updated",
                            activeKit.name() + " (page " + (pageIndex + 1) + ")");
                } catch (IllegalArgumentException exception) {
                    return WorkspaceCommandOutcome.rejected(exception.getMessage());
                }
            }
        }
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank()) {
            trimmed = defaultKitName(runtime);
        }
        try {
            KitDefinition created = runtime.kitWorkflow().snapshotFromAuthority(
                    trimmed,
                    resolvedAuthority,
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

    /**
     * Re-run the active kit's loadout plan against the current authority
     * snapshot WITHOUT recording a new activation. Drives the
     * "shift+click pickup fills the kit-needed slot" experience: when
     * the player pulls the missing item out of a chest while a kit is
     * active, this routes it from main / backpack into the empty
     * hotbar slot the kit declared, instead of leaving the slot a
     * ghost until the next deactivate / reactivate.
     *
     * <p>No-op when no kit is active or when the loadout plan has no
     * pending operations (kit already fully satisfied).
     */
    public static void reapplyActiveKit(
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor
    ) {
        if (runtime == null || actionExecutor == null) {
            return;
        }
        var activation = runtime.kitWorkflow().activation();
        if (!activation.isActive()) {
            return;
        }
        KitDefinition kit = runtime.kitWorkflow().kit(activation.kitId());
        if (kit == null) {
            return;
        }
        LoadoutApplyService.LoadoutApplyPlan plan = runtime.kitWorkflow().planActivate(
                kit.id(),
                activation.pageIndex(),
                authority == null ? InventoryAuthoritySnapshot.empty() : authority,
                protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy,
                identityResolver
        );
        if (plan.operations().isEmpty()) {
            return;
        }
        new LoadoutApplyExecutor(actionExecutor).execute(plan);
    }

    public static WorkspaceCommandOutcome switchKitPage(
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            int direction
    ) {
        if (runtime == null || actionExecutor == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_page_switch");
        }
        var activation = runtime.kitWorkflow().activation();
        if (!activation.isActive()) {
            return WorkspaceCommandOutcome.rejected("no_active_kit");
        }
        KitDefinition kit = runtime.kitWorkflow().kit(activation.kitId());
        if (kit == null) {
            return WorkspaceCommandOutcome.rejected("unknown_kit");
        }
        if (kit.pageCount() <= 1) {
            return WorkspaceCommandOutcome.rejected("kit_has_one_page");
        }
        int step = Integer.signum(direction);
        if (step == 0) {
            step = 1;
        }
        int pageCount = kit.pageCount();
        int nextPage = Math.floorMod(activation.pageIndex() + step, pageCount);
        LoadoutApplyService.LoadoutApplyPlan plan = runtime.kitWorkflow().planActivate(
                kit.id(),
                nextPage,
                authority == null ? InventoryAuthoritySnapshot.empty() : authority,
                protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy,
                identityResolver
        );
        LoadoutApplyResult result = new LoadoutApplyExecutor(actionExecutor).execute(plan);
        runtime.kitWorkflow().switchPage(
                nextPage,
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_switch_page")
        );
        int satisfied = result.satisfiedTargets().size();
        int missing = result.missingTargets().size();
        String planReasons = result.diagnostics().isEmpty() ? "" : String.join(",", result.diagnostics());
        SlotDebugLog.log("LDLib kit page switched {} page={} satisfied={} missing={} reasons={}",
                kit.id(), nextPage, satisfied, missing, planReasons);
        String status = "kit page " + (nextPage + 1) + "/" + pageCount;
        StringBuilder diagnostics = new StringBuilder()
                .append("satisfied=").append(satisfied)
                .append(" missing=").append(missing);
        if (!planReasons.isBlank()) {
            diagnostics.append(" reasons=").append(planReasons);
        }
        return WorkspaceCommandOutcome.accepted(status, diagnostics.toString());
    }

    public static WorkspaceCommandOutcome addKitPage(WorkflowDomainRuntime runtime, String kitId) {
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
        try {
            runtime.kitWorkflow().addPage(
                    kitId,
                    DomainEventMetadata.origin("slot_workspace.ldlib.kit_add_page")
            );
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected("kit_capacity_exceeded");
        }
        KitDefinition updated = runtime.kitWorkflow().kit(kitId);
        SlotDebugLog.log("LDLib kit page added {} pages={}", kitId, updated.pageCount());
        return WorkspaceCommandOutcome.accepted("kit page added", existing.name());
    }

    public static WorkspaceCommandOutcome setKitSlotIdentity(
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            String kitId,
            int pageIndex,
            int slotIndex,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_runtime");
        }
        if (kitId == null || kitId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_id");
        }
        if (runtime.kitWorkflow().kit(kitId) == null) {
            return WorkspaceCommandOutcome.rejected("unknown_kit");
        }
        // identity may be null to clear the slot
        ItemIdentity identity = (itemId == null || itemId.isBlank())
                ? null
                : resolveIdentity(itemId, comparisonMode, componentFingerprint);
        boolean changed = runtime.kitWorkflow().setSlotIdentity(
                kitId, pageIndex, slotIndex, identity,
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_set_slot")
        );
        if (!changed) {
            return WorkspaceCommandOutcome.rejected("kit_slot_unchanged");
        }
        SlotDebugLog.log("LDLib kit slot updated {} page={} slot={} identity={}", kitId,
                pageIndex, slotIndex, identity == null ? "" : identity.itemId());

        // If this edit landed on the active kit's active page, re-plan + apply so the
        // live belt mirrors the definition change. Only possible when the platform
        // adapter supplies an actionExecutor (i.e. a server context with host access);
        // without it we fall back to definition-only update.
        boolean beltSynced = false;
        int beltMissing = 0;
        if (actionExecutor != null) {
            var activation = runtime.kitWorkflow().activation();
            if (activation.isActive()
                    && kitId.equals(activation.kitId())
                    && activation.pageIndex() == pageIndex) {
                LoadoutApplyService.LoadoutApplyPlan plan = runtime.kitWorkflow().planActivate(
                        kitId,
                        pageIndex,
                        authority == null ? InventoryAuthoritySnapshot.empty() : authority,
                        protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy,
                        identityResolver
                );
                LoadoutApplyResult result = new LoadoutApplyExecutor(actionExecutor).execute(plan);
                beltSynced = true;
                beltMissing = result.missingTargets().size();
                SlotDebugLog.log("LDLib kit active-page belt sync {} page={} satisfied={} missing={}",
                        kitId, pageIndex, result.satisfiedTargets().size(), beltMissing);
            }
        }

        String status;
        if (beltSynced && beltMissing == 0) {
            status = "kit slot updated (belt synced)";
        } else if (beltSynced) {
            status = "kit slot updated (belt synced, missing " + beltMissing + ")";
        } else {
            status = "kit slot updated";
        }
        String detail = (identity == null ? "cleared" : identity.itemId());
        return WorkspaceCommandOutcome.accepted(status, detail);
    }

    public static WorkspaceCommandOutcome setKitScopedDesiredCount(
            WorkflowDomainRuntime runtime,
            String kitId,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            int count
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_runtime");
        }
        if (kitId == null || kitId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_id");
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (runtime.kitWorkflow().kit(kitId) == null) {
            return WorkspaceCommandOutcome.rejected("unknown_kit");
        }
        boolean changed = runtime.desiredCountWorkflow().setForKit(kitId, identity, count);
        if (!changed) {
            return WorkspaceCommandOutcome.accepted("noop", "");
        }
        return WorkspaceCommandOutcome.accepted(
                count > 0 ? "kit_desired_set_" + count : "kit_desired_cleared",
                "");
    }

    /**
     * Set the active-scope desired count. If a kit is active the write lands
     * in that kit's desired-count scope; otherwise it lands in the player
     * global scope. The platform adapter must not choose the scope.
     */
    public static WorkspaceCommandOutcome setPlayerDesiredCount(
            WorkflowDomainRuntime runtime,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            int count
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_desired_count_runtime");
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        DesiredCountWorkflowDomainService desired = runtime.desiredCountWorkflow();
        String activeKit = desired.activeScope(runtime.snapshot().kitMap());
        boolean changed = activeKit != null
                ? desired.setForKit(activeKit, identity, count)
                : desired.setPlayer(identity, count);
        if (!changed) {
            return WorkspaceCommandOutcome.accepted("noop", "");
        }
        String scopeTag = activeKit != null ? "kit" : "global";
        return WorkspaceCommandOutcome.accepted(
                count > 0 ? "desired_count_" + scopeTag + "_" + count : "desired_count_cleared",
                "");
    }

    /**
     * Adjust the active-scope desired count. Mirrors
     * {@link #setPlayerDesiredCount}: kit scope wins while a kit is active,
     * otherwise the player-global standing order is changed.
     */
    public static WorkspaceCommandOutcome adjustPlayerDesiredCount(
            WorkflowDomainRuntime runtime,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            int delta
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_desired_count_runtime");
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        if (delta == 0) {
            return WorkspaceCommandOutcome.accepted("noop", "");
        }
        DesiredCountWorkflowDomainService desired = runtime.desiredCountWorkflow();
        String activeKit = desired.activeScope(runtime.snapshot().kitMap());
        boolean changed = activeKit != null
                ? desired.adjustForKit(activeKit, identity, delta)
                : desired.adjustPlayer(identity, delta);
        if (!changed) {
            return WorkspaceCommandOutcome.accepted("noop", "");
        }
        int now = activeKit != null
                ? desired.getForKit(activeKit, identity)
                : desired.getPlayer(identity);
        String scopeTag = activeKit != null ? "kit" : "global";
        return WorkspaceCommandOutcome.accepted("desired_count_" + scopeTag + "_" + now, "");
    }

    public static WorkspaceCommandOutcome swapKitSlots(
            WorkflowDomainRuntime runtime,
            String kitId,
            int pageIndex,
            int fromIndex,
            int toIndex
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_runtime");
        }
        if (kitId == null || kitId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_kit_id");
        }
        if (runtime.kitWorkflow().kit(kitId) == null) {
            return WorkspaceCommandOutcome.rejected("unknown_kit");
        }
        boolean swapped = runtime.kitWorkflow().swapSlots(
                kitId, pageIndex, fromIndex, toIndex,
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_swap_slots")
        );
        if (!swapped) {
            return WorkspaceCommandOutcome.rejected("kit_slot_swap_noop");
        }
        SlotDebugLog.log("LDLib kit slots swapped {} page={} from={} to={}",
                kitId, pageIndex, fromIndex, toIndex);
        return WorkspaceCommandOutcome.accepted("kit slots swapped", kitId);
    }

    public static WorkspaceCommandOutcome removeKitPage(WorkflowDomainRuntime runtime, String kitId, int pageIndex) {
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
        if (existing.pageCount() <= 1) {
            return WorkspaceCommandOutcome.rejected("kit_last_page");
        }
        boolean removed = runtime.kitWorkflow().removePage(
                kitId,
                pageIndex,
                DomainEventMetadata.origin("slot_workspace.ldlib.kit_remove_page")
        );
        if (!removed) {
            return WorkspaceCommandOutcome.rejected("invalid_page_index");
        }
        SlotDebugLog.log("LDLib kit page removed {} page={}", kitId, pageIndex);
        return WorkspaceCommandOutcome.accepted("kit page removed", existing.name());
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

    public static WorkspaceCommandOutcome renameKit(WorkflowDomainRuntime runtime, String kitId, String newName) {
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
        String trimmed = newName == null ? "" : newName.trim();
        if (trimmed.isBlank()) {
            return WorkspaceCommandOutcome.rejected("kit_name_blank");
        }
        try {
            boolean renamed = runtime.kitWorkflow().rename(
                    kitId,
                    trimmed,
                    DomainEventMetadata.origin("slot_workspace.ldlib.kit_rename")
            );
            if (!renamed) {
                return WorkspaceCommandOutcome.accepted("kit name unchanged", existing.name());
            }
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected(exception.getMessage());
        }
        SlotDebugLog.log("LDLib kit renamed {} -> {}", kitId, trimmed);
        return WorkspaceCommandOutcome.accepted("kit renamed", trimmed);
    }

    public static WorkspaceCommandOutcome duplicateKit(WorkflowDomainRuntime runtime, String kitId) {
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
        try {
            KitDefinition copy = runtime.kitWorkflow().duplicate(
                    kitId,
                    DomainEventMetadata.origin("slot_workspace.ldlib.kit_duplicate")
            );
            if (copy == null) {
                return WorkspaceCommandOutcome.rejected("kit_duplicate_rejected");
            }
            SlotDebugLog.log("LDLib kit duplicated {} -> {}", kitId, copy.id());
            return WorkspaceCommandOutcome.accepted("kit duplicated", copy.name());
        } catch (IllegalArgumentException exception) {
            return WorkspaceCommandOutcome.rejected(exception.getMessage());
        }
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
        // Delete only succeeds when the island has no assignments — so undo just
        // recreates the exact pre-delete island snapshot. Template dismissal is not
        // separately reversible; we accept that minor wrinkle.
        final VisualAtlasIsland preDeleteSnapshot = existing;
        final String targetId = islandId;
        runtime.undoStack().record(
                "delete island",
                ctx -> recreateIslandFromSnapshot(ctx.runtime(), preDeleteSnapshot),
                ctx -> ctx.runtime().visualAtlasWorkflow().deleteIsland(
                        targetId,
                        DomainEventMetadata.origin("workflow.undo.island_delete.replay")
                )
        );
        return WorkspaceCommandOutcome.accepted("island deleted", existing.label());
    }


    /**
     * Apply the "drop identity onto island" rule: triage target clears the home;
     * anything else assigns at the requested ordinal (null = append) and
     * records a learned rule. Exposed publicly so the hotbar-to-atlas flow
     * (which first runs a transfer) can re-use the same rule after the stack
     * arrives in the main inventory.
     */
    public static WorkspaceCommandOutcome applyHomeDrop(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            LearnedIslandRuleStore learnedRules,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            ItemIdentity identity,
            String islandId,
            Integer ordinal,
            String origin
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            return WorkspaceCommandOutcome.rejected("invalid_home_assignment");
        }
        if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
            runtime.visualAtlasWorkflow().clearHome(
                    identity,
                    DomainEventMetadata.origin(origin + ".clear")
            );
            SlotDebugLog.log("LDLib atlas home cleared {} -> {}", identity.itemId(), islandId);
            return WorkspaceCommandOutcome.accepted("returned to inbox", "Triage");
        }
        SlotWorkspaceViewModel.AtlasIsland island = viewModel == null ? null : viewModel.island(islandId);
        if (island == null) {
            return WorkspaceCommandOutcome.rejected("unknown_island");
        }

        int resolvedOrdinal = resolveOrdinal(
                runtime, viewModel, signalExtractor, identity, islandId, ordinal);
        runtime.visualAtlasWorkflow().assignHome(
                identity,
                islandId,
                resolvedOrdinal,
                VisualHomeOrigin.PLAYER_PLACED,
                true,
                DomainEventMetadata.origin(origin)
        );
        recordLearnedAssignment(viewModel, learnedRules, signalExtractor, identity, islandId);
        SlotDebugLog.log(
                "LDLib atlas home assigned {} -> {} ordinal={}",
                identity.itemId(),
                islandId,
                resolvedOrdinal
        );
        return WorkspaceCommandOutcome.accepted("home assigned", island.label());
    }

    /**
     * Resolve the ordinal a drop targets in {@code islandId}. A non-null
     * ordinal is taken as-is (drag-drop with explicit position); a null
     * ordinal kicks in cluster-aware insertion via
     * {@link WithinIslandOrdering#WITHIN_ISLAND_COMPARATOR} — the new
     * identity slots in next to its same-cluster neighbors instead of
     * blindly appending. Falls back to plain append when descriptors
     * aren't available (no signalExtractor, no view-model entry, etc.)
     * so the projection's bounds-clamp still does the right thing on
     * a cold start.
     */
    private static int resolveOrdinal(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            ItemIdentity identity,
            String islandId,
            Integer ordinal
    ) {
        if (ordinal != null && ordinal >= 0) {
            return ordinal;
        }
        List<VisualHomeAssignment> sameIsland = new ArrayList<>();
        for (VisualHomeAssignment assignment :
                runtime.visualAtlasWorkflow().visualHomeMap().assignments().values()) {
            if (assignment != null && islandId.equals(assignment.islandId())) {
                sameIsland.add(assignment);
            }
        }
        int currentSize = sameIsland.size();
        if (signalExtractor == null || viewModel == null || currentSize == 0) {
            return currentSize;
        }

        WithinIslandOrdering.DescribedStack newStack =
                describedStackFor(viewModel, signalExtractor, identity);
        if (newStack == null) {
            return currentSize;
        }
        sameIsland.sort(Comparator.comparingInt(VisualHomeAssignment::ordinal));
        for (VisualHomeAssignment existing : sameIsland) {
            if (identity.equals(existing.identity())) {
                // The same identity is being re-homed to the island it
                // already lives in. Skip — projection clears the old
                // slot first via compactOrdinalsAfterRemove.
                continue;
            }
            WithinIslandOrdering.DescribedStack existingStack =
                    describedStackFor(viewModel, signalExtractor, existing.identity());
            if (existingStack == null) {
                continue;
            }
            if (WithinIslandOrdering.WITHIN_ISLAND_COMPARATOR
                    .compare(newStack, existingStack) < 0) {
                return existing.ordinal();
            }
        }
        return currentSize;
    }

    /**
     * Build a {@link WithinIslandOrdering.DescribedStack} for an
     * identity by looking its display stack up in the view-model's
     * atlas item map and running it through {@code signalExtractor}.
     * Returns {@code null} when any required piece is missing — the
     * caller treats null as "fall through to append behavior".
     */
    private static WithinIslandOrdering.DescribedStack describedStackFor(
            SlotWorkspaceViewModel viewModel,
            Function<ItemStack, IslandSignalDescriptor> signalExtractor,
            ItemIdentity identity
    ) {
        if (viewModel == null || signalExtractor == null || identity == null) {
            return null;
        }
        SlotWorkspaceViewModel.AtlasItem item =
                viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(identity));
        if (item == null) {
            // Identity isn't in the atlas item map (could be a loot-chest
            // panel item that hasn't shown up in carry yet). Fall back
            // to a synthetic descriptor so it still gets ordered.
            IslandSignalDescriptor descriptor = IslandSignalDescriptor.empty(identity);
            return new WithinIslandOrdering.DescribedStack(null, descriptor);
        }
        ItemStack stack = item.displayStack();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        IslandSignalDescriptor descriptor = signalExtractor.apply(stack);
        if (descriptor == null) {
            return null;
        }
        return new WithinIslandOrdering.DescribedStack(stack, descriptor);
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
        // Don't record when the assigned island is what the template /
        // subsystem chip already would have suggested for this descriptor —
        // that's a confirmation, not a learned divergence. Recording it
        // still captures the descriptor's broad adjacency keys (TAG /
        // MATERIAL_FAMILY / NAMESPACE / CREATIVE_TAB) against the
        // template island, then those keys spuriously fire for unrelated
        // items that happen to share one. Concrete repro: 3 backpacks
        // accepted into the STORAGE template chip recorded
        // {tag, material_family, namespace} → template.storage rules; the
        // next triage item (oak sapling) shared one of those keys and
        // surfaced "Storage" as a top suggestion.
        if (isTemplateDefaultAssignment(descriptor, islandId)) {
            return;
        }
        learnedRules.recordAssignment(descriptor, islandId, System.currentTimeMillis());
    }

    private static boolean isTemplateDefaultAssignment(IslandSignalDescriptor descriptor, String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return false;
        }
        IslandTemplateMatch templateMatch = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, null);
        if (templateMatch != null && islandId.equals(templateMatch.islandId())) {
            return true;
        }
        // Subsystem-id islands are the chip the suggestion service offers
        // when descriptor.subsystems() contains a matching key — accepting
        // that chip is also a confirmation, not a learned override.
        if (islandId.startsWith(IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX)) {
            String subsystem = islandId.substring(IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX.length());
            for (String descriptorSubsystem : descriptor.subsystems()) {
                if (subsystem.equals(descriptorSubsystem)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean visibleInAtlas(SlotWorkspaceViewModel viewModel, ItemIdentity identity) {
        if (identity == null || viewModel == null) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        if (viewModel.atlasItem(ref) != null) {
            return true;
        }
        // The loot-chest panel is a Triage-equivalent surface; identities
        // visible there should also be acceptable for chip-accept (used by
        // shift+click and "Take all → auto-accept top chip").
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.lootChestPanel().items()) {
            if (ref.equals(item.identity())) {
                return true;
            }
        }
        return false;
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }

    // --- Undo support ---
    //
    // Workspace commands that mutate snapshot "before" and "after" states and push a
    // matched pair of closures onto the runtime's UndoStack. Workflow-only mutations
    // (home moves, island CRUD) only need the runtime; inventory-affecting ops (Kit
    // activation, chest deposit/take, hotbar transfers) will layer on top in phase 2
    // with a full UndoContext (authority + action executor).

    public static void restoreHomeAssignment(
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            VisualHomeAssignment target
    ) {
        if (target == null) {
            runtime.visualAtlasWorkflow().clearHome(
                    identity,
                    DomainEventMetadata.origin("workflow.undo.home.restore")
            );
        } else {
            runtime.visualAtlasWorkflow().assignHome(
                    target.identity(),
                    target.islandId(),
                    target.ordinal(),
                    target.origin(),
                    target.locked(),
                    DomainEventMetadata.origin("workflow.undo.home.restore")
            );
        }
    }

    static void recreateIslandFromSnapshot(
            WorkflowDomainRuntime runtime,
            VisualAtlasIsland snapshot
    ) {
        if (snapshot == null) {
            return;
        }
        runtime.visualAtlasWorkflow().createIslandWithId(
                snapshot.id(),
                snapshot.label(),
                snapshot.x(),
                snapshot.y(),
                snapshot.color(),
                snapshot.iconIdentity(),
                DomainEventMetadata.origin("workflow.undo.island.recreate")
        );
    }

    public static WorkspaceCommandOutcome performUndo(WorkflowDomainRuntime runtime) {
        return performUndo(runtime, UndoContext.workflowOnly(runtime));
    }

    public static WorkspaceCommandOutcome performUndo(WorkflowDomainRuntime runtime, UndoContext context) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_undo_runtime");
        }
        UndoContext resolved = context == null ? UndoContext.workflowOnly(runtime) : context;
        return runtime.undoStack().undo(resolved)
                .map(UndoRecord::label)
                .map(label -> {
                    SlotDebugLog.log("LDLib undo {}", label);
                    return WorkspaceCommandOutcome.accepted("undid: " + label, label);
                })
                .orElseGet(() -> WorkspaceCommandOutcome.rejected("nothing_to_undo"));
    }

    public static WorkspaceCommandOutcome performRedo(WorkflowDomainRuntime runtime) {
        return performRedo(runtime, UndoContext.workflowOnly(runtime));
    }

    public static WorkspaceCommandOutcome performRedo(WorkflowDomainRuntime runtime, UndoContext context) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("invalid_redo_runtime");
        }
        UndoContext resolved = context == null ? UndoContext.workflowOnly(runtime) : context;
        return runtime.undoStack().redo(resolved)
                .map(UndoRecord::label)
                .map(label -> {
                    SlotDebugLog.log("LDLib redo {}", label);
                    return WorkspaceCommandOutcome.accepted("redid: " + label, label);
                })
                .orElseGet(() -> WorkspaceCommandOutcome.rejected("nothing_to_redo"));
    }

    /**
     * Pickup-time auto-home pass: a triage candidate (carried but
     * unassigned) gets routed via its top chip suggestion or to the
     * Misc fallback. Skips undo + learned-rule recording — the player
     * can drag-to-rehome to override; auto-placements aren't a sanctioned
     * adjacency signal.
     *
     * <p>Processes at most one identity per call. Each
     * {@code assignHome}/{@code createIsland} fires
     * {@link WorkflowDomainRuntime#saveNow()} synchronously, so doing
     * a whole-inventory pass in a single tick blocked the server thread
     * long enough that crafting-table screens couldn't close.
     * Per-tick throttling spreads the work and lets the input loop keep
     * pumping; the projection's triage list only ever shrinks, so the
     * pass converges.
     *
     * <p>Returns true iff one assignment was written, so the caller
     * knows to re-project.
     */
    public static boolean autoHomeTriageItems(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel viewModel,
            java.util.Set<ItemIdentity> alreadyAttempted
    ) {
        if (runtime == null || viewModel == null) {
            return false;
        }
        List<SlotWorkspaceViewModel.AtlasItem> triage = viewModel.triageItems();
        if (triage.isEmpty()) {
            return false;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : triage) {
            boolean proximateClaimedChestGhost = item.ghost() && item.proximateCount() > 0;
            if (!item.carried() && !proximateClaimedChestGhost) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            if (alreadyAttempted != null && alreadyAttempted.contains(identity)) {
                continue;
            }
            if (runtime.visualAtlasWorkflow().visualHomeMap().assignment(identity) != null) {
                if (alreadyAttempted != null) {
                    alreadyAttempted.add(identity);
                }
                continue;
            }
            if (alreadyAttempted != null) {
                alreadyAttempted.add(identity);
            }
            String targetIslandId = resolveAutoHomeIsland(runtime, item);
            if (targetIslandId == null || targetIslandId.isBlank()) {
                continue;
            }
            int ordinal = appendOrdinal(runtime, targetIslandId);
            runtime.visualAtlasWorkflow().assignHome(
                    identity,
                    targetIslandId,
                    ordinal,
                    VisualHomeOrigin.AUTO_HOMED,
                    true,
                    DomainEventMetadata.origin("slot_workspace.ldlib.auto_home")
            );
            SlotDebugLog.log(
                    "LDLib atlas auto-homed {} -> {} ordinal={}",
                    identity.itemId(),
                    targetIslandId,
                    ordinal
            );
            return true;
        }
        return false;
    }

    private static String resolveAutoHomeIsland(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        for (dev.imagio.slot.inventory.triage.ChipSuggestion chip : item.chipSuggestions()) {
            if (chip == null) {
                continue;
            }
            if (chip.kind() == dev.imagio.slot.inventory.triage.ChipSuggestion.ChipKind.TEMPLATE) {
                IslandSuggestionTemplate template = chip.template();
                if (template == null) {
                    continue;
                }
                ItemIdentity seed = item.identity().toIdentity();
                return resolveOrMaterializeTemplateIsland(runtime, template, seed, item.name());
            }
            if (runtime.visualAtlasWorkflow().visualHomeMap().island(chip.islandId()) != null) {
                return chip.islandId();
            }
        }
        return resolveOrMaterializeMiscIsland(runtime, item);
    }

    private static String resolveOrMaterializeMiscIsland(
            WorkflowDomainRuntime runtime,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        VisualAtlasIsland existing = runtime.visualAtlasWorkflow().visualHomeMap()
                .island(SlotWorkspaceAtlasLayout.ISLAND_MISC);
        if (existing != null) {
            return existing.id();
        }
        SlotWorkspaceAtlasLayout.PlayerIslandDraft draft = SlotWorkspaceAtlasLayout
                .createNextPlayerIslandDraft(
                        SlotWorkspaceAtlasLayout.ISLAND_MISC_LABEL,
                        item == null ? null : item.identity().toIdentity(),
                        runtime.visualAtlasWorkflow().visualHomeMap()
                );
        VisualAtlasIsland created = runtime.visualAtlasWorkflow().createIslandWithId(
                SlotWorkspaceAtlasLayout.ISLAND_MISC,
                draft.label(),
                draft.x(),
                draft.y(),
                SlotWorkspaceAtlasLayout.ISLAND_MISC_COLOR,
                item == null ? null : item.identity().toIdentity(),
                DomainEventMetadata.origin("slot_workspace.ldlib.auto_home.misc_create")
        );
        return created == null ? null : created.id();
    }

    private static int appendOrdinal(WorkflowDomainRuntime runtime, String islandId) {
        int count = 0;
        for (VisualHomeAssignment assignment :
                runtime.visualAtlasWorkflow().visualHomeMap().assignments().values()) {
            if (assignment != null && islandId.equals(assignment.islandId())) {
                count++;
            }
        }
        return count;
    }
}
