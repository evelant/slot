package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashMap;
import java.util.Map;

public record ContextualItemAggregate(
        ItemIdentity identity,
        int timesObservedCarried,
        int timesAcquired,
        int timesTakenFromStorage,
        int timesDepositedToStorage,
        int timesCraftedOrProduced,
        long totalCarriedTicks,
        double recentCarriedTicksEwma,
        long lastCarriedSequence,
        long lastAcquiredSequence,
        long lastDepositedSequence,
        Map<String, Double> cooccurrenceHints
) {
    private static final double EWMA_ALPHA = 0.35D;

    public ContextualItemAggregate {
        timesObservedCarried = Math.max(0, timesObservedCarried);
        timesAcquired = Math.max(0, timesAcquired);
        timesTakenFromStorage = Math.max(0, timesTakenFromStorage);
        timesDepositedToStorage = Math.max(0, timesDepositedToStorage);
        timesCraftedOrProduced = Math.max(0, timesCraftedOrProduced);
        totalCarriedTicks = Math.max(0L, totalCarriedTicks);
        recentCarriedTicksEwma = Double.isFinite(recentCarriedTicksEwma)
                ? Math.max(0D, recentCarriedTicksEwma)
                : 0D;
        lastCarriedSequence = Math.max(0L, lastCarriedSequence);
        lastAcquiredSequence = Math.max(0L, lastAcquiredSequence);
        lastDepositedSequence = Math.max(0L, lastDepositedSequence);
        cooccurrenceHints = boundedHints(cooccurrenceHints, 16);
    }

    public static ContextualItemAggregate empty(ItemIdentity identity) {
        return new ContextualItemAggregate(identity, 0, 0, 0, 0, 0, 0L, 0D, 0L, 0L, 0L, Map.of());
    }

    public ContextualItemAggregate observedCarried(long sequence) {
        return new ContextualItemAggregate(
                identity, timesObservedCarried + 1, timesAcquired, timesTakenFromStorage,
                timesDepositedToStorage, timesCraftedOrProduced, totalCarriedTicks,
                recentCarriedTicksEwma, sequence, lastAcquiredSequence, lastDepositedSequence, cooccurrenceHints);
    }

    public ContextualItemAggregate carriedFor(long elapsedTicks, long sequence) {
        long elapsed = Math.max(0L, elapsedTicks);
        double nextEwma = recentCarriedTicksEwma <= 0D
                ? elapsed
                : recentCarriedTicksEwma * (1D - EWMA_ALPHA) + elapsed * EWMA_ALPHA;
        return new ContextualItemAggregate(
                identity, timesObservedCarried, timesAcquired, timesTakenFromStorage,
                timesDepositedToStorage, timesCraftedOrProduced, totalCarriedTicks + elapsed,
                nextEwma, sequence, lastAcquiredSequence, lastDepositedSequence, cooccurrenceHints);
    }

    public ContextualItemAggregate acquired(long sequence, boolean fromStorage, boolean produced) {
        return new ContextualItemAggregate(
                identity,
                timesObservedCarried,
                timesAcquired + 1,
                timesTakenFromStorage + (fromStorage ? 1 : 0),
                timesDepositedToStorage,
                timesCraftedOrProduced + (produced ? 1 : 0),
                totalCarriedTicks,
                recentCarriedTicksEwma,
                lastCarriedSequence,
                sequence,
                lastDepositedSequence,
                cooccurrenceHints);
    }

    public ContextualItemAggregate deposited(long sequence) {
        return new ContextualItemAggregate(
                identity,
                timesObservedCarried,
                timesAcquired,
                timesTakenFromStorage,
                timesDepositedToStorage + 1,
                timesCraftedOrProduced,
                totalCarriedTicks,
                recentCarriedTicksEwma,
                lastCarriedSequence,
                lastAcquiredSequence,
                sequence,
                cooccurrenceHints);
    }

    public ContextualItemAggregate withHint(String key, double delta) {
        if (key == null || key.isBlank() || delta <= 0D || !Double.isFinite(delta)) {
            return this;
        }
        LinkedHashMap<String, Double> next = new LinkedHashMap<>(cooccurrenceHints);
        next.merge(key.trim(), delta, Double::sum);
        return new ContextualItemAggregate(
                identity, timesObservedCarried, timesAcquired, timesTakenFromStorage,
                timesDepositedToStorage, timesCraftedOrProduced, totalCarriedTicks,
                recentCarriedTicksEwma, lastCarriedSequence, lastAcquiredSequence,
                lastDepositedSequence, next);
    }

    static Map<String, Double> boundedHints(Map<String, Double> source, int limit) {
        if (source == null || source.isEmpty() || limit <= 0) {
            return Map.of();
        }
        return source.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0D && Double.isFinite(entry.getValue()))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey().trim(), entry.getValue()),
                        LinkedHashMap::putAll);
    }
}
