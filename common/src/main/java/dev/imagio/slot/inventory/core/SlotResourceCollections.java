package dev.imagio.slot.inventory.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SlotResourceCollections {
    private SlotResourceCollections() {
    }

    public static SlotResourceIdentity key(SlotResourceIdentity identity) {
        if (identity == null) {
            return null;
        }
        return new SlotResourceIdentity(identity.kind(), identity.id(), identity.fingerprint());
    }

    public static void mergeAmount(
            Map<SlotResourceIdentity, Long> target,
            SlotResourceIdentity identity,
            long amount
    ) {
        if (target == null || identity == null || amount <= 0L) {
            return;
        }
        target.merge(key(identity), amount, SlotResourceCollections::saturatedAdd);
    }

    public static long count(Map<SlotResourceIdentity, Long> source, SlotResourceIdentity identity) {
        if (source == null || source.isEmpty() || identity == null) {
            return 0L;
        }
        SlotResourceIdentity key = key(identity);
        return Math.max(0L, source.getOrDefault(key, 0L));
    }

    public static Map<SlotResourceIdentity, Long> normalizeAmounts(Map<SlotResourceIdentity, Long> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<SlotResourceIdentity, Long> normalized = new LinkedHashMap<>();
        for (Map.Entry<SlotResourceIdentity, Long> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0L) {
                mergeAmount(normalized, entry.getKey(), entry.getValue());
            }
        }
        return normalized.isEmpty() ? Map.of() : Map.copyOf(normalized);
    }

    private static long saturatedAdd(long left, long right) {
        if (left >= Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
