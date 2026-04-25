package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-item relevance value plus the per-contributor breakdown for
 * debugging. {@link #value} is the {@code max} of {@link #contributions}
 * (the design's combination rule for v1; swap to weighted-sum here if a
 * concrete case demands it). Range: {@code [0, 1]}, where {@code 0} is
 * baseline and {@code 1} is maximum relevance.
 */
public record RelevanceScore(float value, Map<String, Float> contributions) {
    public static final RelevanceScore BASELINE = new RelevanceScore(0f, Map.of());

    public RelevanceScore {
        if (Float.isNaN(value)) {
            value = 0f;
        }
        value = Math.max(0f, Math.min(1f, value));
        contributions = contributions == null ? Map.of() : Map.copyOf(contributions);
    }

    /**
     * Run every contributor against the identity, collect the
     * per-name breakdown, and return the combined score (max). Order
     * of contributors does not matter — combination is commutative.
     */
    public static RelevanceScore compute(
            ItemIdentity identity,
            RelevanceContext context,
            List<RelevanceContributor> contributors
    ) {
        if (identity == null || contributors == null || contributors.isEmpty()) {
            return BASELINE;
        }
        RelevanceContext ctx = context == null ? RelevanceContext.empty() : context;
        LinkedHashMap<String, Float> breakdown = new LinkedHashMap<>(contributors.size());
        float combined = 0f;
        for (RelevanceContributor contributor : contributors) {
            if (contributor == null) {
                continue;
            }
            float raw = contributor.score(identity, ctx);
            if (Float.isNaN(raw)) {
                raw = 0f;
            }
            float clamped = Math.max(0f, Math.min(1f, raw));
            breakdown.put(contributor.name(), clamped);
            if (clamped > combined) {
                combined = clamped;
            }
        }
        return new RelevanceScore(combined, breakdown);
    }
}
