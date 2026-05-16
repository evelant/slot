package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record ContextualCarriedObservation(
        ItemIdentity identity,
        long firstSeenSequence,
        long firstSeenTick,
        long lastSeenTick,
        int count,
        String sourceKey
) {
    public ContextualCarriedObservation {
        firstSeenSequence = Math.max(0L, firstSeenSequence);
        firstSeenTick = Math.max(0L, firstSeenTick);
        lastSeenTick = Math.max(firstSeenTick, lastSeenTick);
        count = Math.max(0, count);
        sourceKey = sourceKey == null ? "" : sourceKey;
    }

    public ContextualCarriedObservation withLastSeen(long tick, int nextCount, String nextSourceKey) {
        return new ContextualCarriedObservation(
                identity,
                firstSeenSequence,
                firstSeenTick,
                Math.max(lastSeenTick, tick),
                nextCount,
                nextSourceKey == null || nextSourceKey.isBlank() ? sourceKey : nextSourceKey);
    }
}
