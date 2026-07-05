package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class WorkspaceCraftRunCommandService {
    private WorkspaceCraftRunCommandService() {
    }

    public static WorkspaceCommandOutcome addRecipe(
            WorkflowDomainRuntime runtime,
            CraftRunRecipeCapture capture
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("runtime_unavailable");
        }
        CraftRunState before = runtime.snapshot().craftRun();
        boolean changed = runtime.craftRunWorkflow().add(capture);
        if (!changed) {
            return WorkspaceCommandOutcome.rejected("craft_run_recipe_not_found");
        }
        return withCraftRunInvalidation(
                WorkspaceCommandOutcome.accepted("craft_run_added", ""),
                before,
                runtime.snapshot().craftRun(),
                "craft_run_recipe_add");
    }

    public static WorkspaceCommandOutcome adjustEntry(
            WorkflowDomainRuntime runtime,
            String entryId,
            int delta
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("runtime_unavailable");
        }
        CraftRunState before = runtime.snapshot().craftRun();
        boolean changed = runtime.craftRunWorkflow().adjustRemainingOutput(entryId, delta);
        if (!changed) {
            return WorkspaceCommandOutcome.rejected("craft_run_entry_not_found");
        }
        return withCraftRunInvalidation(
                WorkspaceCommandOutcome.accepted("craft_run_adjusted", ""),
                before,
                runtime.snapshot().craftRun(),
                "craft_run_entry_adjust");
    }

    public static WorkspaceCommandOutcome selectIngredientAlternative(
            WorkflowDomainRuntime runtime,
            String entryId,
            String groupId,
            ItemIdentity identity
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("runtime_unavailable");
        }
        CraftRunState before = runtime.snapshot().craftRun();
        boolean changed = runtime.craftRunWorkflow().selectIngredientAlternative(entryId, groupId, identity);
        if (!changed) {
            return WorkspaceCommandOutcome.rejected("craft_run_ingredient_not_found");
        }
        return withCraftRunInvalidation(
                WorkspaceCommandOutcome.accepted("craft_run_ingredient_selected", ""),
                before,
                runtime.snapshot().craftRun(),
                "craft_run_ingredient_select");
    }

    public static WorkspaceCommandOutcome removeEntry(
            WorkflowDomainRuntime runtime,
            String entryId
    ) {
        if (runtime == null) {
            return WorkspaceCommandOutcome.rejected("runtime_unavailable");
        }
        CraftRunState before = runtime.snapshot().craftRun();
        boolean changed = runtime.craftRunWorkflow().remove(entryId);
        if (!changed) {
            return WorkspaceCommandOutcome.rejected("craft_run_entry_not_found");
        }
        return withCraftRunInvalidation(
                WorkspaceCommandOutcome.accepted("craft_run_removed", ""),
                before,
                runtime.snapshot().craftRun(),
                "craft_run_entry_remove");
    }

    public static WorkspaceCommandOutcome stageEntry(
            WorkflowDomainRuntime runtime,
            InventoryAuthoritySnapshot authority,
            String entryId,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            String originPrefix
    ) {
        if (runtime == null || authority == null || actionExecutor == null) {
            return WorkspaceCommandOutcome.rejected("invalid_craft_run_stage_context");
        }
        InventoryHostDescriptor host = authority.host();
        if (host == null) {
            return WorkspaceCommandOutcome.rejected("host_resolution_failed");
        }
        CraftRunRecipeEntry entry = runtime.snapshot().craftRun().entry(entryId);
        if (entry == null || !entry.active()) {
            return WorkspaceCommandOutcome.rejected("craft_run_entry_not_found");
        }

        int moved = 0;
        String lastFailure = "";
        for (CraftRunIngredientGroup group : entry.inputs()) {
            StageNeed need = needFor(authority, entry, group);
            if (need.deficit() <= 0 || need.identities().isEmpty()) {
                continue;
            }
            int movedForGroup = stageNeed(host, authority, need, actionExecutor, originPrefix);
            moved += movedForGroup;
            if (movedForGroup < need.deficit()) {
                lastFailure = movedForGroup == 0 ? "matching_ingredient_unavailable" : "partial_stage";
            }
        }

        if (moved > 0) {
            return new WorkspaceCommandOutcome(true, "craft_run_staged", "staged " + moved + " ingredients");
        }
        return new WorkspaceCommandOutcome(
                false,
                "nothing_to_stage",
                lastFailure.isBlank() ? "no missing craft-run ingredient was found outside main inventory" : lastFailure);
    }

    private static int stageNeed(
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority,
            StageNeed need,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor,
            String originPrefix
    ) {
        int remaining = need.deficit();
        int moved = 0;
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (remaining <= 0) {
                break;
            }
            if (source == null || skipAsCraftRunSource(source)) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (remaining <= 0) {
                    break;
                }
                if (entry == null || !entry.present() || !matchesAny(entry.stack(), need.identities())) {
                    continue;
                }
                int count = Math.min(remaining, entry.count());
                InventoryActionRequest request = transferRequest(
                        host,
                        sourceTarget(source, entry),
                        new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                        origin(originPrefix, "craft_run.stage"),
                        ItemIdentityMatcher.create(entry.stack()),
                        entry.stack(),
                        count);
                InventoryActionOutcome outcome = actionExecutor.apply(request);
                WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);
                if (!feedback.appliedCompletely()) {
                    continue;
                }
                moved += count;
                remaining -= count;
            }
        }
        return moved;
    }

    private static StageNeed needFor(
            InventoryAuthoritySnapshot authority,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group
    ) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (CraftRunAlternative alternative : group.selectedOrAllAlternatives()) {
            if (alternative != null && alternative.identity() != null) {
                identities.add(alternative.identity());
            }
        }
        int required = group.requiredForBatches(entry.remainingBatches());
        int staged = 0;
        for (InventoryEntrySnapshot inventoryEntry : authority.entries(BuiltinInventoryIds.PLAYER_MAIN)) {
            if (inventoryEntry != null && inventoryEntry.present()
                    && matchesAny(inventoryEntry.stack(), identities)) {
                staged += inventoryEntry.count();
            }
        }
        return new StageNeed(identities, Math.max(0, required - staged));
    }

    private static boolean skipAsCraftRunSource(InventorySourceDescriptor source) {
        if (source == null) {
            return true;
        }
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(source.id())) {
            return true;
        }
        InventorySourceRole role = source.role();
        return role == InventorySourceRole.QUICK_ACCESS
                || role == InventorySourceRole.EQUIPMENT
                || role == InventorySourceRole.OFFHAND;
    }

    private static boolean matchesAny(ItemStack stack, Set<ItemIdentity> identities) {
        if (stack == null || stack.isEmpty() || identities == null || identities.isEmpty()) {
            return false;
        }
        for (ItemIdentity identity : identities) {
            if (identity != null && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return true;
            }
        }
        return false;
    }

    private static InventoryActionTarget sourceTarget(InventorySourceDescriptor source, InventoryEntrySnapshot entry) {
        if (entry == null) {
            return new InventoryActionTarget.SourceTarget(source == null ? "" : source.id());
        }
        if (entry.entryKey().providerEntry()) {
            return new InventoryActionTarget.SourceEntryTarget(entry.sourceId(), entry.entryId());
        }
        if (source == null) {
            return new InventoryActionTarget.SourceSlotTarget(entry.sourceId(), entry.slotIndex());
        }
        return switch (source.role()) {
            case QUICK_ACCESS -> new InventoryActionTarget.QuickAccessTarget(source.laneId(), entry.slotIndex());
            case EQUIPMENT, OFFHAND -> new InventoryActionTarget.EquipmentTarget(source.groupId(), entry.slotIndex());
            default -> new InventoryActionTarget.SourceSlotTarget(entry.sourceId(), entry.slotIndex());
        };
    }

    private static InventoryActionRequest transferRequest(
            InventoryHostDescriptor host,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin,
            ItemIdentity identity,
            ItemStack stack,
            int requestedCount
    ) {
        ItemStack requestStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!requestStack.isEmpty()) {
            requestStack.setCount(Math.max(1, requestedCount));
        }
        return new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                UUID.randomUUID().toString(),
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                origin,
                source,
                destination,
                Math.max(1, requestedCount),
                identity,
                requestStack,
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                "");
    }

    private static String origin(String prefix, String suffix) {
        return (prefix == null || prefix.isBlank() ? "slot_workspace.common" : prefix) + "." + suffix;
    }

    private static WorkspaceCommandOutcome withCraftRunInvalidation(
            WorkspaceCommandOutcome outcome,
            CraftRunState before,
            CraftRunState after,
            String diagnostics
    ) {
        WorkspaceCommandOutcome resolved = outcome == null
                ? WorkspaceCommandOutcome.rejected("null_craft_run_outcome")
                : outcome;
        if (!resolved.success()) {
            return resolved;
        }
        Set<ItemIdentity> identities = craftRunIdentities(before, after);
        if (identities.isEmpty()) {
            return resolved.withInvalidations(List.of(WorkspaceInvalidation.full(
                    WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                    "craft_run_changed_without_affected_identities")));
        }
        return resolved.withInvalidations(List.of(new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                identities,
                Set.of(),
                Set.of(),
                EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                diagnostics)));
    }

    private static Set<ItemIdentity> craftRunIdentities(CraftRunState before, CraftRunState after) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        addCraftRunIdentities(identities, before);
        addCraftRunIdentities(identities, after);
        return identities.isEmpty() ? Set.of() : Set.copyOf(identities);
    }

    private static void addCraftRunIdentities(Set<ItemIdentity> identities, CraftRunState state) {
        if (identities == null || state == null || state.entries().isEmpty()) {
            return;
        }
        for (CraftRunRecipeEntry entry : state.entries()) {
            if (entry == null) {
                continue;
            }
            if (entry.outputIdentity() != null) {
                identities.add(entry.outputIdentity());
            }
            for (CraftRunIngredientGroup group : entry.inputs()) {
                if (group == null) {
                    continue;
                }
                for (CraftRunAlternative alternative : group.alternatives()) {
                    if (alternative != null && alternative.identity() != null) {
                        identities.add(alternative.identity());
                    }
                }
            }
        }
    }

    private record StageNeed(Set<ItemIdentity> identities, int deficit) {
        private StageNeed {
            identities = identities == null ? Set.of() : Set.copyOf(identities);
            deficit = Math.max(0, deficit);
        }
    }
}
