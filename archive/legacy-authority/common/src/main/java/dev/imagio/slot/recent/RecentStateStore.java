package dev.imagio.slot.recent;

import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class RecentStateStore {
    private final int maxTrackedIdentities;
    private final Map<ItemIdentity, Long> recencyByIdentity = new LinkedHashMap<>();
    private long nextSequence = 1L;

    public RecentStateStore(int maxTrackedIdentities) {
        this.maxTrackedIdentities = Math.max(1, maxTrackedIdentities);
    }

    public void reset() {
        recencyByIdentity.clear();
        nextSequence = 1L;
    }

    public void record(ItemIdentity identity) {
        ItemIdentity normalizedIdentity = ItemBehaviorPolicy.normalizeTrackedIdentity(identity);
        if (normalizedIdentity == null) {
            return;
        }
        recencyByIdentity.put(normalizedIdentity, nextSequence++);
        prune();
    }

    public boolean contains(ItemIdentity identity) {
        ItemIdentity normalizedIdentity = ItemBehaviorPolicy.normalizeTrackedIdentity(identity);
        return normalizedIdentity != null && recencyByIdentity.containsKey(normalizedIdentity);
    }

    public void dismiss(ItemIdentity identity) {
        ItemIdentity normalizedIdentity = ItemBehaviorPolicy.normalizeTrackedIdentity(identity);
        if (normalizedIdentity != null) {
            recencyByIdentity.remove(normalizedIdentity);
        }
    }

    public void dismissAll(Collection<ItemIdentity> identities) {
        if (identities == null || identities.isEmpty()) {
            return;
        }
        for (ItemIdentity identity : identities) {
            dismiss(identity);
        }
    }

    public List<ItemIdentity> visibleRecentIdentities(
            List<ItemEntry> entries,
            Predicate<String> carriedSourceFilter,
            int visibleLimit
    ) {
        if (entries == null || carriedSourceFilter == null || recencyByIdentity.isEmpty()) {
            return List.of();
        }

        Set<ItemIdentity> visibleCarriedIdentities = carriedCounts(entries, carriedSourceFilter).keySet();
        if (visibleCarriedIdentities.isEmpty()) {
            return List.of();
        }

        List<Map.Entry<ItemIdentity, Long>> visibleRecentEntries = new ArrayList<>();
        for (Map.Entry<ItemIdentity, Long> recentEntry : recencyByIdentity.entrySet()) {
            if (visibleCarriedIdentities.contains(recentEntry.getKey())) {
                visibleRecentEntries.add(recentEntry);
            }
        }

        visibleRecentEntries.sort(Map.Entry.<ItemIdentity, Long>comparingByValue(Comparator.reverseOrder()));
        List<ItemIdentity> identities = new ArrayList<>(Math.min(Math.max(0, visibleLimit), visibleRecentEntries.size()));
        for (Map.Entry<ItemIdentity, Long> recentEntry : visibleRecentEntries) {
            identities.add(recentEntry.getKey());
            if (identities.size() >= visibleLimit) {
                break;
            }
        }
        return List.copyOf(identities);
    }

    private Map<ItemIdentity, Integer> carriedCounts(List<ItemEntry> entries, Predicate<String> carriedSourceFilter) {
        Map<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (ItemEntry entry : entries) {
            int count = carriedCount(entry, carriedSourceFilter);
            if (count <= 0) {
                continue;
            }
            ItemIdentity normalizedIdentity = ItemBehaviorPolicy.normalizeTrackedIdentity(entry.identity());
            if (normalizedIdentity != null) {
                counts.merge(normalizedIdentity, count, Integer::sum);
            }
        }
        return counts;
    }

    private int carriedCount(ItemEntry entry, Predicate<String> carriedSourceFilter) {
        int count = 0;
        for (Map.Entry<String, Integer> sourceCount : entry.perSourceCounts().entrySet()) {
            if (carriedSourceFilter.test(sourceCount.getKey())) {
                count += sourceCount.getValue();
            }
        }
        return count;
    }

    private void prune() {
        if (recencyByIdentity.size() <= maxTrackedIdentities) {
            return;
        }

        List<Map.Entry<ItemIdentity, Long>> ordered = new ArrayList<>(recencyByIdentity.entrySet());
        ordered.sort(Map.Entry.comparingByValue());
        int removeCount = recencyByIdentity.size() - maxTrackedIdentities;
        for (int index = 0; index < removeCount; index++) {
            recencyByIdentity.remove(ordered.get(index).getKey());
        }
    }
}
