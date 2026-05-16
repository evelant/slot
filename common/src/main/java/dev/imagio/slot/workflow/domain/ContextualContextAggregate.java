package dev.imagio.slot.workflow.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public record ContextualContextAggregate(
        String contextKey,
        String label,
        int timesSeen,
        long lastSeenSequence,
        Map<String, Double> itemHints,
        Map<String, Double> facetHints
) {
    public ContextualContextAggregate {
        contextKey = contextKey == null ? "" : contextKey.trim();
        label = label == null ? "" : label.trim();
        timesSeen = Math.max(0, timesSeen);
        lastSeenSequence = Math.max(0L, lastSeenSequence);
        itemHints = ContextualItemAggregate.boundedHints(itemHints, 24);
        facetHints = ContextualItemAggregate.boundedHints(facetHints, 24);
    }

    public static ContextualContextAggregate empty(String contextKey, String label) {
        return new ContextualContextAggregate(contextKey, label, 0, 0L, Map.of(), Map.of());
    }

    public ContextualContextAggregate seen(String nextLabel, long sequence, Map<String, Double> nextItemHints) {
        LinkedHashMap<String, Double> mergedHints = new LinkedHashMap<>(itemHints);
        if (nextItemHints != null) {
            nextItemHints.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null && value > 0D && Double.isFinite(value)) {
                    mergedHints.merge(key.trim(), value, Double::sum);
                }
            });
        }
        return new ContextualContextAggregate(
                contextKey,
                nextLabel == null || nextLabel.isBlank() ? label : nextLabel,
                timesSeen + 1,
                sequence,
                mergedHints,
                facetHints);
    }
}
