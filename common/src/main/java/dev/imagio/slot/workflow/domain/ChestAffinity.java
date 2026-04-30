package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Objects;

/**
 * One stored affinity bond between a claimed chest and an item identity.
 *
 * <p>{@code score} is the persisted bump count (each observed deposit adds
 * one point). {@code lastTouchedTick} timestamps the most recent bump; it
 * drives decay — affinity decreases by 1 per
 * {@link #DEFAULT_DECAY_TICKS_PER_POINT} game ticks of inactivity.
 *
 * <p>Decay is "lazy": readers call {@link #effectiveScore(long)} to get the
 * decayed score; the persisted {@code score} is only updated when the player
 * performs another deposit (see {@link #bump(int, long)}). Game time on the
 * server is play-time-based, so a player who shelves the mod for a month
 * doesn't lose all state — it only decays while the world is running.
 */
public record ChestAffinity(
        ItemIdentity identity,
        int score,
        long lastTouchedTick
) {
    /** One point of affinity decays after this many game ticks of inactivity. ~1 in-game day. */
    public static final long DEFAULT_DECAY_TICKS_PER_POINT = 24000L;

    public ChestAffinity {
        Objects.requireNonNull(identity, "identity");
        score = Math.max(0, score);
        lastTouchedTick = Math.max(0L, lastTouchedTick);
    }

    /**
     * Compute the decayed score at {@code currentTick}, never below zero.
     * Persisted state is unchanged.
     */
    public int effectiveScore(long currentTick) {
        return effectiveScore(currentTick, DEFAULT_DECAY_TICKS_PER_POINT);
    }

    public int effectiveScore(long currentTick, long decayTicksPerPoint) {
        if (decayTicksPerPoint <= 0L || score <= 0) {
            return score;
        }
        long elapsed = Math.max(0L, currentTick - lastTouchedTick);
        long decay = elapsed / decayTicksPerPoint;
        if (decay <= 0L) {
            return score;
        }
        if (decay >= score) {
            return 0;
        }
        return score - (int) decay;
    }

    /**
     * Apply decay first, then bump by {@code delta} (minimum 1). Refreshes
     * {@code lastTouchedTick} to {@code currentTick} so subsequent decay
     * starts from now.
     */
    public ChestAffinity bump(int delta, long currentTick) {
        return bump(delta, currentTick, DEFAULT_DECAY_TICKS_PER_POINT);
    }

    public ChestAffinity bump(int delta, long currentTick, long decayTicksPerPoint) {
        int decayed = effectiveScore(currentTick, decayTicksPerPoint);
        int next = decayed + Math.max(1, delta);
        long touchedAt = Math.max(lastTouchedTick, currentTick);
        return new ChestAffinity(identity, next, touchedAt);
    }
}
