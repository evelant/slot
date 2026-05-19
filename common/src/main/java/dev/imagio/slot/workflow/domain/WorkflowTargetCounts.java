package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Canonical target-count identity handling for desired / wanted / workflow-tab
 * math. Stable movable tools are stored and compared by item id so durability
 * or transient component churn does not split one player intent into multiple
 * map keys.
 */
final class WorkflowTargetCounts {
    private WorkflowTargetCounts() {
    }

    static ItemIdentity key(ItemIdentity identity) {
        return ItemIdentityMatcher.normalizeMovable(identity);
    }

    static int count(Map<ItemIdentity, Integer> counts, ItemIdentity identity) {
        if (counts == null || counts.isEmpty() || identity == null) {
            return 0;
        }
        ItemIdentity target = key(identity);
        int best = Math.max(0, counts.getOrDefault(target, 0));
        for (Map.Entry<ItemIdentity, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= best) {
                continue;
            }
            if (ItemIdentityMatcher.matchesMovable(entry.getKey(), target)) {
                best = entry.getValue();
            }
        }
        return best;
    }

    static boolean contains(Set<ItemIdentity> identities, ItemIdentity identity) {
        if (identities == null || identities.isEmpty() || identity == null) {
            return false;
        }
        ItemIdentity target = key(identity);
        if (identities.contains(target)) {
            return true;
        }
        for (ItemIdentity candidate : identities) {
            if (ItemIdentityMatcher.matchesMovable(candidate, target)) {
                return true;
            }
        }
        return false;
    }

    static void mergePositive(Map<ItemIdentity, Integer> targets, ItemIdentity identity, Integer count) {
        if (targets == null || identity == null || count == null || count <= 0) {
            return;
        }
        targets.merge(key(identity), count, Math::max);
    }

    static void add(Set<ItemIdentity> targets, ItemIdentity identity) {
        if (targets != null && identity != null) {
            targets.add(key(identity));
        }
    }

    static void putOrClear(Map<ItemIdentity, Integer> targets, ItemIdentity identity, int count) {
        if (targets == null || identity == null) {
            return;
        }
        ItemIdentity target = key(identity);
        removeMatching(targets, target);
        if (count > 0) {
            targets.put(target, count);
        }
    }

    private static void removeMatching(Map<ItemIdentity, Integer> targets, ItemIdentity identity) {
        Iterator<ItemIdentity> iterator = targets.keySet().iterator();
        while (iterator.hasNext()) {
            ItemIdentity candidate = iterator.next();
            if (ItemIdentityMatcher.matchesMovable(candidate, identity)) {
                iterator.remove();
            }
        }
    }
}
