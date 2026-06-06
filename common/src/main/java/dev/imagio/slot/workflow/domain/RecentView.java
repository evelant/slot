package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RecentView(
        Map<ItemIdentity, Integer> countsByIdentity,
        Map<ItemIdentity, Long> latestSequenceByIdentity
) {
    public static final int MAX_IDENTITIES = 24;

    public RecentView {
        LinkedHashMap<ItemIdentity, Integer> counts = sanitizeCounts(countsByIdentity);
        LinkedHashMap<ItemIdentity, Long> sequences = sanitizeSequences(latestSequenceByIdentity);
        while (counts.size() > MAX_IDENTITIES) {
            ItemIdentity oldest = counts.keySet().iterator().next();
            counts.remove(oldest);
            sequences.remove(oldest);
        }
        sequences.keySet().removeIf(identity -> !counts.containsKey(identity));
        countsByIdentity = counts.isEmpty() ? Map.of() : java.util.Collections.unmodifiableMap(counts);
        latestSequenceByIdentity = sequences.isEmpty() ? Map.of() : java.util.Collections.unmodifiableMap(sequences);
    }

    public static RecentView empty() {
        return new RecentView(Map.of(), Map.of());
    }

    public List<ItemIdentity> visibleItems() {
        ArrayList<ItemIdentity> identities = new ArrayList<>(countsByIdentity.keySet());
        java.util.Collections.reverse(identities);
        return List.copyOf(identities);
    }

    private static LinkedHashMap<ItemIdentity, Integer> sanitizeCounts(Map<ItemIdentity, Integer> source) {
        if (source == null || source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<ItemIdentity, Integer> copied = new LinkedHashMap<>();
        source.forEach((identity, count) -> {
            if (identity != null && count != null && count > 0) {
                ItemIdentityCollections.mergePositive(copied, identity, count);
            }
        });
        return copied;
    }

    private static LinkedHashMap<ItemIdentity, Long> sanitizeSequences(Map<ItemIdentity, Long> source) {
        if (source == null || source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<ItemIdentity, Long> copied = new LinkedHashMap<>();
        source.forEach((identity, sequence) -> {
            if (identity != null && sequence != null && sequence >= 0L) {
                ItemIdentity key = ItemIdentityCollections.key(identity);
                copied.merge(key, sequence, Math::max);
            }
        });
        return copied;
    }
}
