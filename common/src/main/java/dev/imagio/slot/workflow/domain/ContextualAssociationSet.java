package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public record ContextualAssociationSet(
        Map<String, ContextualAssociationHint> itemHints
) {
    public static final int MAX_ITEM_HINTS = 24;

    public ContextualAssociationSet {
        itemHints = bound(itemHints);
    }

    public static ContextualAssociationSet empty() {
        return new ContextualAssociationSet(Map.of());
    }

    public ContextualAssociationSet learn(ItemIdentity identity, long sequence, long delta, double weight) {
        if (identity == null || identity.itemId().isBlank() || weight <= 0D || !Double.isFinite(weight)) {
            return this;
        }
        String itemId = identity.itemId();
        LinkedHashMap<String, ContextualAssociationHint> next = new LinkedHashMap<>(itemHints);
        ContextualAssociationHint previous = next.get(itemId);
        next.put(itemId, previous == null
                ? ContextualAssociationHint.first(itemId, sequence, delta, weight)
                : previous.refreshed(sequence, delta, weight));
        return new ContextualAssociationSet(next);
    }

    public long lastSequence() {
        long latest = 0L;
        for (ContextualAssociationHint hint : itemHints.values()) {
            if (hint != null) {
                latest = Math.max(latest, hint.lastSequence());
            }
        }
        return latest;
    }

    private static Map<String, ContextualAssociationHint> bound(Map<String, ContextualAssociationHint> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, ContextualAssociationHint> copy = source.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
                .filter(entry -> entry.getValue() != null && entry.getValue().score() > 0D)
                .sorted(Comparator
                        .<Map.Entry<String, ContextualAssociationHint>>comparingDouble(
                                entry -> entry.getValue().score())
                        .reversed()
                        .thenComparing(entry -> entry.getKey()))
                .limit(MAX_ITEM_HINTS)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey().trim(), entry.getValue()),
                        LinkedHashMap::putAll);
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }
}
