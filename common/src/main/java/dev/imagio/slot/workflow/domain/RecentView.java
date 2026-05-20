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
    public RecentView {
        countsByIdentity = sanitizeCounts(countsByIdentity);
        latestSequenceByIdentity = sanitizeSequences(latestSequenceByIdentity);
    }

    public static RecentView empty() {
        return new RecentView(Map.of(), Map.of());
    }

    public List<ItemIdentity> visibleItems() {
        ArrayList<ItemIdentity> identities = new ArrayList<>(countsByIdentity.keySet());
        java.util.Collections.reverse(identities);
        return List.copyOf(identities);
    }

    private static Map<ItemIdentity, Integer> sanitizeCounts(Map<ItemIdentity, Integer> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Integer> copied = new LinkedHashMap<>();
        source.forEach((identity, count) -> {
            if (identity != null && count != null && count > 0) {
                ItemIdentityCollections.mergePositive(copied, identity, count);
            }
        });
        return java.util.Collections.unmodifiableMap(copied);
    }

    private static Map<ItemIdentity, Long> sanitizeSequences(Map<ItemIdentity, Long> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, Long> copied = new LinkedHashMap<>();
        source.forEach((identity, sequence) -> {
            if (identity != null && sequence != null && sequence >= 0L) {
                ItemIdentity key = ItemIdentityCollections.key(identity);
                copied.merge(key, sequence, Math::max);
            }
        });
        return java.util.Collections.unmodifiableMap(copied);
    }
}
