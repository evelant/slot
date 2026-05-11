package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record GoalVisibleAuthority(
        Map<ItemIdentity, GoalAuthorityCount> countsByIdentity
) {
    public GoalVisibleAuthority {
        LinkedHashMap<ItemIdentity, GoalAuthorityCount> copy = new LinkedHashMap<>();
        if (countsByIdentity != null) {
            for (Map.Entry<ItemIdentity, GoalAuthorityCount> entry : countsByIdentity.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null || entry.getValue().totalCount() <= 0) {
                    continue;
                }
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        countsByIdentity = Collections.unmodifiableMap(copy);
    }

    public static GoalVisibleAuthority empty() {
        return new GoalVisibleAuthority(Map.of());
    }

    public static GoalVisibleAuthority fromCounts(
            Map<ItemIdentity, Integer> carriedCounts,
            Map<ItemIdentity, Integer> proximateStorageCounts,
            Map<ItemIdentity, Integer> elsewhereStorageCounts
    ) {
        LinkedHashMap<ItemIdentity, MutableCount> counts = new LinkedHashMap<>();
        mergeCarried(counts, carriedCounts);
        mergeProximate(counts, proximateStorageCounts);
        mergeElsewhere(counts, elsewhereStorageCounts);
        LinkedHashMap<ItemIdentity, GoalAuthorityCount> resolved = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, MutableCount> entry : counts.entrySet()) {
            MutableCount count = entry.getValue();
            resolved.put(entry.getKey(), new GoalAuthorityCount(count.carried, count.proximate, count.elsewhere));
        }
        return new GoalVisibleAuthority(resolved);
    }

    public static GoalVisibleAuthority fromAuthority(
            InventoryAuthoritySnapshot authority,
            Map<ItemIdentity, Integer> proximateStorageCounts,
            Map<ItemIdentity, Integer> elsewhereStorageCounts
    ) {
        LinkedHashMap<ItemIdentity, Integer> carried = new LinkedHashMap<>();
        if (authority != null) {
            for (var source : authority.carriedSources()) {
                for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                    if (entry == null || !entry.present()) {
                        continue;
                    }
                    ItemIdentity identity = ItemIdentityMatcher.create(entry.stack());
                    carried.merge(identity, entry.count(), Integer::sum);
                }
            }
        }
        return fromCounts(carried, proximateStorageCounts, elsewhereStorageCounts);
    }

    public GoalAuthorityCount count(ItemIdentity identity) {
        if (identity == null) {
            return new GoalAuthorityCount(0, 0, 0);
        }
        GoalAuthorityCount exact = countsByIdentity.get(identity);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<ItemIdentity, GoalAuthorityCount> entry : countsByIdentity.entrySet()) {
            if (ItemIdentityMatcher.matchesMovable(entry.getKey(), identity)) {
                return entry.getValue();
            }
        }
        return new GoalAuthorityCount(0, 0, 0);
    }

    public List<GoalStackDescriptor> visibleAlternativesInAuthorityOrder(List<GoalStackDescriptor> alternatives) {
        if (alternatives == null || alternatives.isEmpty() || countsByIdentity.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalStackDescriptor> ordered = new ArrayList<>();
        for (ItemIdentity visible : countsByIdentity.keySet()) {
            for (GoalStackDescriptor alternative : alternatives) {
                if (alternative != null
                        && !ordered.contains(alternative)
                        && count(alternative.identity()).totalCount() > 0
                        && ItemIdentityMatcher.matchesMovable(visible, alternative.identity())) {
                    ordered.add(alternative);
                }
            }
        }
        return List.copyOf(ordered);
    }

    private static void mergeCarried(Map<ItemIdentity, MutableCount> counts, Map<ItemIdentity, Integer> source) {
        merge(counts, source, (count, value) -> count.carried += value);
    }

    private static void mergeProximate(Map<ItemIdentity, MutableCount> counts, Map<ItemIdentity, Integer> source) {
        merge(counts, source, (count, value) -> count.proximate += value);
    }

    private static void mergeElsewhere(Map<ItemIdentity, MutableCount> counts, Map<ItemIdentity, Integer> source) {
        merge(counts, source, (count, value) -> count.elsewhere += value);
    }

    private static void merge(
            Map<ItemIdentity, MutableCount> counts,
            Map<ItemIdentity, Integer> source,
            CountMutator mutator
    ) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<ItemIdentity, Integer> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            MutableCount count = counts.computeIfAbsent(entry.getKey(), ignored -> new MutableCount());
            mutator.apply(count, entry.getValue());
        }
    }

    private interface CountMutator {
        void apply(MutableCount count, int value);
    }

    private static final class MutableCount {
        private int carried;
        private int proximate;
        private int elsewhere;
    }
}
