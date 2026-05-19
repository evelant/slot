package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared target math for workflow tabs. The durable rule is:
 * {@code All + parent tab + active tab}, with every layer acting as a floor.
 */
public final class WorkflowTabTargets {
    private WorkflowTabTargets() {
    }

    public record Resolution(
            Map<ItemIdentity, Integer> desiredCounts,
            Set<ItemIdentity> desiredFromWorkflowTab,
            Map<ItemIdentity, Integer> wantedCounts,
            Set<ItemIdentity> workflowRelevantIdentities,
            Set<WorkflowAcceptedInputRule> acceptedInputs,
            Set<ItemIdentity> missingWorkflowIdentities,
            Map<ItemIdentity, Integer> beltPageRequirements
    ) {
        public Resolution {
            desiredCounts = desiredCounts == null ? Map.of() : Map.copyOf(desiredCounts);
            desiredFromWorkflowTab = desiredFromWorkflowTab == null
                    ? Set.of()
                    : Set.copyOf(new LinkedHashSet<>(desiredFromWorkflowTab));
            wantedCounts = wantedCounts == null ? Map.of() : Map.copyOf(wantedCounts);
            workflowRelevantIdentities = workflowRelevantIdentities == null
                    ? Set.of()
                    : Set.copyOf(new LinkedHashSet<>(workflowRelevantIdentities));
            acceptedInputs = acceptedInputs == null
                    ? Set.of()
                    : Set.copyOf(new LinkedHashSet<>(acceptedInputs));
            missingWorkflowIdentities = missingWorkflowIdentities == null
                    ? Set.of()
                    : Set.copyOf(new LinkedHashSet<>(missingWorkflowIdentities));
            beltPageRequirements = beltPageRequirements == null ? Map.of() : Map.copyOf(beltPageRequirements);
        }

        public static Resolution empty() {
            return new Resolution(Map.of(), Set.of(), Map.of(), Set.of(), Set.of(), Set.of(), Map.of());
        }

        public int desiredCount(ItemIdentity identity) {
            return countFor(desiredCounts, identity);
        }

        public boolean desiredFromWorkflowTab(ItemIdentity identity) {
            return containsMovable(desiredFromWorkflowTab, identity);
        }

        public int wantedCount(ItemIdentity identity) {
            return countFor(wantedCounts, identity);
        }

        public boolean workflowRelevant(ItemIdentity identity) {
            return workflowRelevant(identity, Set.of());
        }

        public boolean workflowRelevant(ItemIdentity identity, Set<String> itemTags) {
            return containsMovable(workflowRelevantIdentities, identity) || acceptedInput(identity, itemTags);
        }

        public boolean acceptedInput(ItemIdentity identity, Set<String> itemTags) {
            if (acceptedInputs.isEmpty()) {
                return false;
            }
            for (WorkflowAcceptedInputRule rule : acceptedInputs) {
                if (rule != null && rule.matches(identity, itemTags)) {
                    return true;
                }
            }
            return false;
        }

        public int reservedCarryCount(ItemIdentity identity) {
            if (identity == null) {
                return 0;
            }
            return Math.max(
                    Math.max(desiredCount(identity), wantedCount(identity)),
                    countFor(beltPageRequirements, identity));
        }
    }

    private static int countFor(Map<ItemIdentity, Integer> counts, ItemIdentity identity) {
        return WorkflowTargetCounts.count(counts, identity);
    }

    private static boolean containsMovable(Set<ItemIdentity> identities, ItemIdentity identity) {
        return WorkflowTargetCounts.contains(identities, identity);
    }

    public static Resolution resolve(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot snapshot
    ) {
        if (snapshot == null) {
            return Resolution.empty();
        }
        return resolve(
                authority,
                snapshot.kitMap(),
                snapshot.playerDesiredCounts(),
                snapshot.kitDesiredCounts(),
                snapshot.playerWantedCounts(),
                snapshot.kitWantedCounts());
    }

    public static Resolution resolve(
            InventoryAuthoritySnapshot authority,
            KitMap kitMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts,
            Map<String, Map<ItemIdentity, Integer>> kitWantedCounts
    ) {
        LinkedHashMap<ItemIdentity, Integer> desired = positiveCopy(playerDesiredCounts);
        LinkedHashSet<ItemIdentity> desiredFromWorkflow = new LinkedHashSet<>();
        LinkedHashMap<ItemIdentity, Integer> wanted = activePlayerWantedCounts(authority, playerWantedCounts);
        LinkedHashSet<ItemIdentity> relevant = new LinkedHashSet<>();
        relevant.addAll(desired.keySet());
        relevant.addAll(wanted.keySet());
        LinkedHashSet<WorkflowAcceptedInputRule> acceptedInputs = new LinkedHashSet<>();
        LinkedHashSet<ItemIdentity> workflowOwned = new LinkedHashSet<>();
        LinkedHashMap<ItemIdentity, Integer> beltRequirements = new LinkedHashMap<>();

        List<KitDefinition> lineage = kitMap == null ? List.of() : kitMap.activeLineage();
        KitDefinition beltOwner = beltOwner(lineage);
        mergeBeltRequirements(relevant, beltRequirements, beltOwner, kitMap == null ? null : kitMap.activation());
        workflowOwned.addAll(beltRequirements.keySet());

        for (KitDefinition kit : lineage) {
            if (kit == null) {
                continue;
            }
            for (ItemIdentity member : kit.members()) {
                mergePositive(wanted, member, 1);
                mergePositive(relevant, member);
                mergePositive(workflowOwned, member);
            }
            for (WorkflowAcceptedInputRule rule : kit.acceptedInputs()) {
                if (rule == null) {
                    continue;
                }
                acceptedInputs.add(rule);
                if (rule.exactItem()) {
                    mergePositive(relevant, rule.identity());
                }
            }
            Map<ItemIdentity, Integer> scopedDesired = kitDesiredCounts == null
                    ? Map.of()
                    : kitDesiredCounts.getOrDefault(kit.id(), Map.of());
            for (Map.Entry<ItemIdentity, Integer> entry : scopedDesired.entrySet()) {
                mergePositive(desired, entry.getKey(), entry.getValue());
                mergePositive(workflowOwned, entry.getKey());
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0
                        && entry.getValue() >= countFor(desired, entry.getKey())) {
                    mergePositive(desiredFromWorkflow, entry.getKey());
                    mergePositive(relevant, entry.getKey());
                }
            }
            Map<ItemIdentity, Integer> scopedWanted = kitWantedCounts == null
                    ? Map.of()
                    : kitWantedCounts.getOrDefault(kit.id(), Map.of());
            for (Map.Entry<ItemIdentity, Integer> entry : scopedWanted.entrySet()) {
                mergePositive(wanted, entry.getKey(), entry.getValue());
                mergePositive(relevant, entry.getKey());
                mergePositive(workflowOwned, entry.getKey());
            }
        }

        for (Map.Entry<ItemIdentity, Integer> entry : beltRequirements.entrySet()) {
            mergePositive(wanted, entry.getKey(), entry.getValue());
            mergePositive(relevant, entry.getKey());
        }

        LinkedHashSet<ItemIdentity> missing = new LinkedHashSet<>();
        for (ItemIdentity identity : relevant) {
            if (identity != null && containsMovable(workflowOwned, identity)
                    && carriedMovableCount(authority, identity) < Math.max(1, Math.max(
                    countFor(wanted, identity),
                    Math.max(countFor(desired, identity), countFor(beltRequirements, identity))))) {
                mergePositive(missing, identity);
            }
        }
        return new Resolution(desired, desiredFromWorkflow, wanted, relevant, acceptedInputs, missing, beltRequirements);
    }

    public static Set<ItemIdentity> protectedIdentities(
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitWantedCounts
    ) {
        Resolution resolution = resolve(
                InventoryAuthoritySnapshot.empty(),
                kitMap,
                Map.of(),
                kitDesiredCounts,
                Map.of(),
                kitWantedCounts);
        return resolution.workflowRelevantIdentities();
    }

    private static KitDefinition beltOwner(List<KitDefinition> lineage) {
        if (lineage == null || lineage.isEmpty()) {
            return null;
        }
        KitDefinition active = lineage.get(lineage.size() - 1);
        if (active == null) {
            return null;
        }
        if (!active.variant() || hasExplicitBeltPage(active)) {
            return active;
        }
        return lineage.size() > 1 ? lineage.get(0) : active;
    }

    private static boolean hasExplicitBeltPage(KitDefinition kit) {
        if (kit == null) {
            return false;
        }
        if (kit.offhand() != null) {
            return true;
        }
        for (KitPage page : kit.pages()) {
            if (page != null && page.filledSlotCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private static void mergeBeltRequirements(
            Set<ItemIdentity> relevant,
            Map<ItemIdentity, Integer> requirements,
            KitDefinition kit,
            KitActivation activation
    ) {
        if (kit == null || requirements == null) {
            return;
        }
        int pageIndex = activation == null ? 0 : Math.max(0, Math.min(activation.pageIndex(), kit.pageCount() - 1));
        KitPage page = kit.page(pageIndex);
        if (page != null) {
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                ItemIdentity identity = page.slot(slotIndex);
                mergePositive(requirements, identity, 1);
                mergePositive(relevant, identity);
            }
        }
        mergePositive(requirements, kit.offhand(), 1);
        mergePositive(relevant, kit.offhand());
    }

    private static LinkedHashMap<ItemIdentity, Integer> positiveCopy(Map<ItemIdentity, Integer> source) {
        LinkedHashMap<ItemIdentity, Integer> out = new LinkedHashMap<>();
        if (source == null || source.isEmpty()) {
            return out;
        }
        for (Map.Entry<ItemIdentity, Integer> entry : source.entrySet()) {
            mergePositive(out, entry.getKey(), entry.getValue());
        }
        return out;
    }

    private static LinkedHashMap<ItemIdentity, Integer> activePlayerWantedCounts(
            InventoryAuthoritySnapshot authority,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        LinkedHashMap<ItemIdentity, Integer> out = new LinkedHashMap<>();
        if (playerWantedCounts == null || playerWantedCounts.isEmpty()) {
            return out;
        }
        for (Map.Entry<ItemIdentity, Integer> entry : playerWantedCounts.entrySet()) {
            ItemIdentity identity = entry.getKey();
            Integer target = entry.getValue();
            if (identity != null && target != null && target > 0
                    && carriedMovableCount(authority, identity) < target) {
                mergePositive(out, identity, target);
            }
        }
        return out;
    }

    private static void mergePositive(Map<ItemIdentity, Integer> targets, ItemIdentity identity, Integer count) {
        WorkflowTargetCounts.mergePositive(targets, identity, count);
    }

    private static void mergePositive(Set<ItemIdentity> targets, ItemIdentity identity) {
        WorkflowTargetCounts.add(targets, identity);
    }

    private static int carriedMovableCount(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
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
}
