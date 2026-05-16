package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public record ContextualAssociationIndex(
        Map<String, ContextualAssociationSet> nextItemsBySignature
) {
    public static final int MAX_SIGNATURES = 256;

    public ContextualAssociationIndex {
        nextItemsBySignature = bound(nextItemsBySignature);
    }

    public static ContextualAssociationIndex empty() {
        return new ContextualAssociationIndex(Map.of());
    }

    public ContextualAssociationIndex learnNextItem(
            String signature,
            ItemIdentity identity,
            long sequence,
            long delta,
            double weight
    ) {
        if (signature == null || signature.isBlank()
                || !ContextualEventSignature.replayableSignature(signature)
                || identity == null || identity.itemId().isBlank()
                || weight <= 0D || !Double.isFinite(weight)) {
            return this;
        }
        LinkedHashMap<String, ContextualAssociationSet> next = new LinkedHashMap<>(nextItemsBySignature);
        ContextualAssociationSet set = next.getOrDefault(signature.trim(), ContextualAssociationSet.empty())
                .learn(identity, sequence, delta, weight);
        if (!set.itemHints().isEmpty()) {
            next.put(signature.trim(), set);
        }
        return new ContextualAssociationIndex(next);
    }

    private static Map<String, ContextualAssociationSet> bound(Map<String, ContextualAssociationSet> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, ContextualAssociationSet> copy = source.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
                .filter(entry -> ContextualEventSignature.replayableSignature(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().itemHints().isEmpty())
                .sorted(Comparator
                        .<Map.Entry<String, ContextualAssociationSet>>comparingLong(
                                entry -> entry.getValue().lastSequence())
                        .reversed()
                        .thenComparing(entry -> entry.getKey()))
                .limit(MAX_SIGNATURES)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey().trim(), entry.getValue()),
                        LinkedHashMap::putAll);
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }
}
