package dev.imagio.slot.workflow.domain;

public record ContextualAssociationHint(
        String itemId,
        double score,
        int count,
        long lastSequence,
        double averageDelta
) {
    private static final double REFRESH_DECAY = 0.82D;

    public ContextualAssociationHint {
        itemId = itemId == null ? "" : itemId.trim();
        score = Double.isFinite(score) ? Math.max(0D, score) : 0D;
        count = Math.max(0, count);
        lastSequence = Math.max(0L, lastSequence);
        averageDelta = Double.isFinite(averageDelta) ? Math.max(0D, averageDelta) : 0D;
    }

    public static ContextualAssociationHint first(String itemId, long sequence, long delta, double weight) {
        return new ContextualAssociationHint(itemId, weight, 1, sequence, delta);
    }

    public ContextualAssociationHint refreshed(long sequence, long delta, double weight) {
        int nextCount = Math.max(1, count + 1);
        double nextAverage = averageDelta <= 0D
                ? Math.max(0L, delta)
                : averageDelta + (Math.max(0L, delta) - averageDelta) / nextCount;
        return new ContextualAssociationHint(
                itemId,
                score * REFRESH_DECAY + Math.max(0D, weight),
                nextCount,
                sequence,
                nextAverage);
    }
}
