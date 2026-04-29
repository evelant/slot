package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Objects;

/**
 * One stored affinity bond between a claimed chest and an item identity.
 *
 * <p>Score is a coarse "how strongly does this chest claim this item"
 * counter; {@code lastTouchedTick} timestamps the most recent bump for
 * future decay (not yet applied — see docs/plans/learned-storage.md).
 */
public record ChestAffinity(
        ItemIdentity identity,
        int score,
        long lastTouchedTick
) {
    public ChestAffinity {
        Objects.requireNonNull(identity, "identity");
        score = Math.max(0, score);
        lastTouchedTick = Math.max(0L, lastTouchedTick);
    }

    public ChestAffinity bump(int delta, long tick) {
        return new ChestAffinity(identity, score + Math.max(1, delta), Math.max(lastTouchedTick, tick));
    }
}
