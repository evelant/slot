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
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class WorkspaceCraftRunCommandService {
    private WorkspaceCraftRunCommandService() {
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

    private record StageNeed(Set<ItemIdentity> identities, int deficit) {
        private StageNeed {
            identities = identities == null ? Set.of() : Set.copyOf(identities);
            deficit = Math.max(0, deficit);
        }
    }
}
