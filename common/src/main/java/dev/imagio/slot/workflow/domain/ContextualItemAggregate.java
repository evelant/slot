package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

public record ContextualItemAggregate(
        ItemIdentity identity,
        int timesAcquired,
        int timesTakenFromStorage,
        int timesDepositedToStorage,
        int timesCraftedOrProduced,
        int timesUsed,
        int timesPlaced,
        int timesConsumed,
        int timesDamaged,
        long lastActiveSequence,
        long lastAcquiredSequence,
        long lastDepositedSequence
) {
    public ContextualItemAggregate {
        identity = ItemIdentityCollections.key(identity);
        timesAcquired = Math.max(0, timesAcquired);
        timesTakenFromStorage = Math.max(0, timesTakenFromStorage);
        timesDepositedToStorage = Math.max(0, timesDepositedToStorage);
        timesCraftedOrProduced = Math.max(0, timesCraftedOrProduced);
        timesUsed = Math.max(0, timesUsed);
        timesPlaced = Math.max(0, timesPlaced);
        timesConsumed = Math.max(0, timesConsumed);
        timesDamaged = Math.max(0, timesDamaged);
        lastActiveSequence = Math.max(0L, lastActiveSequence);
        lastAcquiredSequence = Math.max(0L, lastAcquiredSequence);
        lastDepositedSequence = Math.max(0L, lastDepositedSequence);
    }

    public static ContextualItemAggregate empty(ItemIdentity identity) {
        return new ContextualItemAggregate(identity, 0, 0, 0, 0, 0, 0, 0, 0, 0L, 0L, 0L);
    }

    public ContextualItemAggregate record(ContextualSignalKind kind, long sequence) {
        if (kind == null) {
            return this;
        }
        return switch (kind) {
            case ITEM_ACQUIRED -> acquired(sequence, false, false);
            case ITEM_TAKEN_FROM_STORAGE -> acquired(sequence, true, false);
            case ITEM_CRAFTED_OR_PRODUCED -> acquired(sequence, false, true);
            case ITEM_DEPOSITED_TO_STORAGE -> deposited(sequence);
            case ITEM_USED -> active(
                    timesAcquired, timesTakenFromStorage, timesDepositedToStorage, timesCraftedOrProduced,
                    timesUsed + 1, timesPlaced, timesConsumed, timesDamaged, sequence);
            case ITEM_PLACED -> active(
                    timesAcquired, timesTakenFromStorage, timesDepositedToStorage, timesCraftedOrProduced,
                    timesUsed, timesPlaced + 1, timesConsumed, timesDamaged, sequence);
            case ITEM_CONSUMED -> active(
                    timesAcquired, timesTakenFromStorage, timesDepositedToStorage, timesCraftedOrProduced,
                    timesUsed, timesPlaced, timesConsumed + 1, timesDamaged, sequence);
            case ITEM_DAMAGED -> active(
                    timesAcquired, timesTakenFromStorage, timesDepositedToStorage, timesCraftedOrProduced,
                    timesUsed, timesPlaced, timesConsumed, timesDamaged + 1, sequence);
            case STATION_OPENED, STATION_CONTENTS_CHANGED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> this;
        };
    }

    public ContextualItemAggregate acquired(long sequence, boolean fromStorage, boolean produced) {
        return active(
                timesAcquired + 1,
                timesTakenFromStorage + (fromStorage ? 1 : 0),
                timesDepositedToStorage,
                timesCraftedOrProduced + (produced ? 1 : 0),
                timesUsed,
                timesPlaced,
                timesConsumed,
                timesDamaged,
                sequence);
    }

    public ContextualItemAggregate deposited(long sequence) {
        return new ContextualItemAggregate(
                identity,
                timesAcquired,
                timesTakenFromStorage,
                timesDepositedToStorage + 1,
                timesCraftedOrProduced,
                timesUsed,
                timesPlaced,
                timesConsumed,
                timesDamaged,
                lastActiveSequence,
                lastAcquiredSequence,
                sequence);
    }

    private ContextualItemAggregate active(
            int nextAcquired,
            int nextTaken,
            int nextDeposited,
            int nextCrafted,
            int nextUsed,
            int nextPlaced,
            int nextConsumed,
            int nextDamaged,
            long sequence
    ) {
        return new ContextualItemAggregate(
                identity,
                nextAcquired,
                nextTaken,
                nextDeposited,
                nextCrafted,
                nextUsed,
                nextPlaced,
                nextConsumed,
                nextDamaged,
                sequence,
                nextAcquired > timesAcquired ? sequence : lastAcquiredSequence,
                lastDepositedSequence);
    }
}
