package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.query.CarriedIdentityCounts;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public final class CraftRunTargetOverlay {
    private CraftRunTargetOverlay() {
    }

    public static WorkflowTabTargets.Resolution apply(
            WorkflowTabTargets.Resolution base,
            CraftRunState craftRun,
            CarriedIdentityCounts carriedCounts
    ) {
        WorkflowTabTargets.Resolution resolved = base == null ? WorkflowTabTargets.Resolution.empty() : base;
        if (craftRun == null || !craftRun.active()) {
            return resolved;
        }
        CarriedIdentityCounts carried = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        LinkedHashMap<ItemIdentity, Integer> desired = new LinkedHashMap<>(resolved.desiredCounts());
        LinkedHashSet<ItemIdentity> desiredFromWorkflow = new LinkedHashSet<>(resolved.desiredFromWorkflowTab());
        LinkedHashMap<ItemIdentity, Integer> wanted = new LinkedHashMap<>(resolved.wantedCounts());
        LinkedHashSet<ItemIdentity> relevant = new LinkedHashSet<>(resolved.workflowRelevantIdentities());
        LinkedHashSet<ItemIdentity> missing = new LinkedHashSet<>(resolved.missingWorkflowIdentities());
        LinkedHashMap<ItemIdentity, Integer> beltRequirements = new LinkedHashMap<>(resolved.beltPageRequirements());
        LinkedHashMap<ItemIdentity, Integer> consumedRequirements = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Integer> reusableRequirements = new LinkedHashMap<>();
        LinkedHashSet<ItemIdentity> requirementOrder = new LinkedHashSet<>();

        for (CraftRunRecipeEntry entry : craftRun.entries()) {
            if (entry == null || !entry.active()) {
                continue;
            }
            ItemIdentityCollections.add(relevant, entry.outputIdentity());
            if (!entry.pending()) {
                continue;
            }
            int batches = entry.remainingBatches();
            for (CraftRunIngredientGroup group : entry.inputs()) {
                ItemIdentity selected = pressureIdentity(group, carried);
                if (selected == null) {
                    if (group != null) {
                        for (CraftRunAlternative alternative : group.alternatives()) {
                            if (alternative != null) {
                                ItemIdentityCollections.add(relevant, alternative.identity());
                            }
                        }
                    }
                    continue;
                }
                int target = group.requiredForBatches(batches);
                mergeRequirement(
                        requirementOrder,
                        consumedRequirements,
                        reusableRequirements,
                        selected,
                        target,
                        group.consumed());
                ItemIdentityCollections.add(relevant, selected);
            }
        }
        for (ItemIdentity identity : requirementOrder) {
            int target = saturatedAdd(
                    consumedRequirements.getOrDefault(identity, 0),
                    reusableRequirements.getOrDefault(identity, 0));
            if (target <= 0) {
                continue;
            }
            ItemIdentityCollections.mergePositive(wanted, identity, target);
            if (carried.count(identity) < ItemIdentityCollections.count(wanted, identity)) {
                ItemIdentityCollections.add(missing, identity);
            }
        }
        return new WorkflowTabTargets.Resolution(
                desired,
                desiredFromWorkflow,
                wanted,
                relevant,
                resolved.acceptedInputs(),
                missing,
                beltRequirements);
    }

    public static ItemIdentity pressureIdentity(
            CraftRunIngredientGroup group,
            CarriedIdentityCounts carriedCounts
    ) {
        if (group == null || group.alternatives().isEmpty()) {
            return null;
        }
        if (group.selectedAlternativeIdentity() != null) {
            return group.selectedAlternativeIdentity();
        }
        if (group.alternatives().size() == 1) {
            CraftRunAlternative alternative = group.alternatives().get(0);
            return alternative == null ? null : alternative.identity();
        }
        CarriedIdentityCounts carried = carriedCounts == null ? CarriedIdentityCounts.empty() : carriedCounts;
        for (CraftRunAlternative alternative : group.alternatives()) {
            if (alternative != null && carried.count(alternative.identity()) > 0) {
                return alternative.identity();
            }
        }
        return null;
    }

    private static void mergeRequirement(
            LinkedHashSet<ItemIdentity> requirementOrder,
            LinkedHashMap<ItemIdentity, Integer> consumedRequirements,
            LinkedHashMap<ItemIdentity, Integer> reusableRequirements,
            ItemIdentity identity,
            int target,
            boolean consumed
    ) {
        ItemIdentity key = ItemIdentityCollections.key(identity);
        if (key == null || target <= 0) {
            return;
        }
        requirementOrder.add(key);
        if (consumed) {
            ItemIdentityCollections.mergeCount(consumedRequirements, key, target);
            return;
        }
        reusableRequirements.merge(key, target, Math::max);
    }

    private static int saturatedAdd(int left, int right) {
        long sum = (long) Math.max(0, left) + Math.max(0, right);
        return sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }
}
