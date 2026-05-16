package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ContextualSuggestionState(
        long nextStreamSequence,
        Map<ItemIdentity, ContextualItemAggregate> itemAggregates,
        Map<String, ContextualContextAggregate> contextAggregates,
        ContextualAssociationIndex associationIndex,
        List<ContextualSignalRecord> recentSignals,
        String activeContextKey
) {
    public static final int MAX_ITEM_AGGREGATES = 512;
    public static final int MAX_CONTEXT_AGGREGATES = 64;
    public static final int MAX_RECENT_SIGNALS = 160;
    private static final long ASSOCIATION_WINDOW_SEQUENCES = 24L;
    private static final long STATION_COUSE_WINDOW_SEQUENCES = 8L;

    public ContextualSuggestionState {
        nextStreamSequence = Math.max(1L, nextStreamSequence);
        itemAggregates = itemAggregates == null ? Map.of() : copyItems(itemAggregates);
        contextAggregates = contextAggregates == null ? Map.of() : copyContexts(contextAggregates);
        associationIndex = associationIndex == null ? ContextualAssociationIndex.empty() : associationIndex;
        recentSignals = recentSignals == null ? List.of() : List.copyOf(recentSignals.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingLong(record -> record.envelope().globalSequence()))
                .skip(Math.max(0, recentSignals.size() - MAX_RECENT_SIGNALS))
                .toList());
        activeContextKey = activeContextKey == null ? "" : activeContextKey.trim();
    }

    public static ContextualSuggestionState empty() {
        return new ContextualSuggestionState(1L, Map.of(), Map.of(), ContextualAssociationIndex.empty(), List.of(), "");
    }

    public ContextualSuggestionState record(ContextualSignalRecord record) {
        if (record == null || record.event() == null) {
            return this;
        }
        ContextualSignalEvent event = record.event();
        long sequence = record.envelope().globalSequence();
        LinkedHashMap<ItemIdentity, ContextualItemAggregate> nextItems = new LinkedHashMap<>(itemAggregates);
        LinkedHashMap<String, ContextualContextAggregate> nextContexts = new LinkedHashMap<>(contextAggregates);
        ContextualAssociationIndex nextAssociations = associationIndex;
        String nextActiveContextKey = activeContextKey;

        ItemIdentity identity = event.identity();
        if (identity != null) {
            ContextualItemAggregate aggregate = nextItems.getOrDefault(identity, ContextualItemAggregate.empty(identity))
                    .record(event.kind(), sequence);
            nextItems.put(identity, aggregate);
        }

        if (!event.contextKey().isBlank()) {
            ContextualContextAggregate aggregate = nextContexts.getOrDefault(
                    event.contextKey(),
                    ContextualContextAggregate.empty(event.contextKey(), event.contextLabel()));
            nextContexts.put(event.contextKey(), aggregate.seen(event.contextLabel(), sequence, Map.of()));
            if (event.kind() == ContextualSignalKind.STATION_OPENED) {
                nextActiveContextKey = event.contextKey();
            }
        }

        if (ContextualEventSignature.trainsItemCandidate(event)) {
            nextAssociations = learnAssociations(nextAssociations, event, sequence);
        }

        ArrayList<ContextualSignalRecord> nextSignals = new ArrayList<>(recentSignals);
        nextSignals.add(record);
        if (nextSignals.size() > MAX_RECENT_SIGNALS) {
            nextSignals = new ArrayList<>(nextSignals.subList(nextSignals.size() - MAX_RECENT_SIGNALS, nextSignals.size()));
        }
        return new ContextualSuggestionState(
                Math.max(nextStreamSequence, record.envelope().streamSequence() + 1L),
                nextItems,
                nextContexts,
                nextAssociations,
                nextSignals,
                nextActiveContextKey);
    }

    public ContextualSuggestionState withActiveContextKey(String nextActiveContextKey) {
        return new ContextualSuggestionState(
                nextStreamSequence,
                itemAggregates,
                contextAggregates,
                associationIndex,
                recentSignals,
                nextActiveContextKey);
    }

    private ContextualAssociationIndex learnAssociations(
            ContextualAssociationIndex index,
            ContextualSignalEvent event,
            long sequence
    ) {
        ContextualAssociationIndex next = index == null ? ContextualAssociationIndex.empty() : index;
        ItemIdentity identity = event.identity();
        String currentSignature = ContextualEventSignature.key(event);
        for (ContextualSignalRecord recent : recentSignals) {
            if (recent == null || recent.event() == null || recent.envelope() == null) {
                continue;
            }
            long delta = sequence - recent.envelope().globalSequence();
            if (delta <= 0L || delta > ASSOCIATION_WINDOW_SEQUENCES) {
                continue;
            }
            ContextualSignalEvent previous = recent.event();
            if (!ContextualEventSignature.trainsAssociations(previous)) {
                continue;
            }
            String previousSignature = ContextualEventSignature.key(previous);
            if (!previousSignature.isBlank() && shouldLearnForwardAssociation(previous, event, delta)) {
                next = next.learnNextItem(previousSignature, identity, sequence, delta, associationWeight(previous, event, delta));
            }
            if (shouldLearnStationCouse(previous, event, delta)) {
                next = next.learnNextItem(currentSignature, previous.identity(), sequence, delta,
                        associationWeight(event, previous, delta) * 0.75D);
            }
        }
        return next;
    }

    private static boolean shouldLearnForwardAssociation(
            ContextualSignalEvent previous,
            ContextualSignalEvent event,
            long delta
    ) {
        if (previous == null
                || event == null
                || previous.identity() == null
                || event.identity() == null
                || previous.identity().equals(event.identity())
                || delta <= 0L
                || delta > ASSOCIATION_WINDOW_SEQUENCES) {
            return false;
        }
        if (previous.kind() == ContextualSignalKind.STATION_CONTENTS_CHANGED
                && event.kind() == ContextualSignalKind.STATION_CONTENTS_CHANGED) {
            return !event.contextKey().isBlank() && event.contextKey().equals(previous.contextKey());
        }
        if (previous.kind() == ContextualSignalKind.ITEM_TAKEN_FROM_STORAGE
                && event.kind() == ContextualSignalKind.STATION_CONTENTS_CHANGED) {
            return true;
        }
        return false;
    }

    private static boolean shouldLearnStationCouse(
            ContextualSignalEvent previous,
            ContextualSignalEvent event,
            long delta
    ) {
        return previous != null
                && event != null
                && previous.identity() != null
                && event.identity() != null
                && !previous.identity().equals(event.identity())
                && delta <= STATION_COUSE_WINDOW_SEQUENCES
                && previous.kind() == ContextualSignalKind.STATION_CONTENTS_CHANGED
                && event.kind() == ContextualSignalKind.STATION_CONTENTS_CHANGED
                && !event.contextKey().isBlank()
                && event.contextKey().equals(previous.contextKey());
    }

    private static double associationWeight(ContextualSignalEvent from, ContextualSignalEvent to, long delta) {
        double base = switch (to.kind()) {
            case STATION_CONTENTS_CHANGED -> 1.15D;
            case ITEM_TAKEN_FROM_STORAGE, ITEM_CRAFTED_OR_PRODUCED -> 1.1D;
            case ITEM_ACQUIRED -> 0.85D;
            case ITEM_USED, ITEM_PLACED, ITEM_CONSUMED, ITEM_DAMAGED,
                    STATION_OPENED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED,
                    ITEM_DEPOSITED_TO_STORAGE -> 0D;
        };
        return base / (1D + Math.max(0L, delta) / 12D);
    }

    private static Map<ItemIdentity, ContextualItemAggregate> copyItems(
            Map<ItemIdentity, ContextualItemAggregate> source
    ) {
        if (source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<ItemIdentity, ContextualItemAggregate> copy = source.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .sorted(Comparator
                        .<Map.Entry<ItemIdentity, ContextualItemAggregate>>comparingLong(
                                entry -> Math.max(
                                        entry.getValue().lastActiveSequence(),
                                        Math.max(entry.getValue().lastAcquiredSequence(), entry.getValue().lastDepositedSequence())))
                        .reversed())
                .limit(MAX_ITEM_AGGREGATES)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }

    private static Map<String, ContextualContextAggregate> copyContexts(
            Map<String, ContextualContextAggregate> source
    ) {
        if (source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, ContextualContextAggregate> copy = source.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null)
                .sorted(Comparator
                        .<Map.Entry<String, ContextualContextAggregate>>comparingLong(
                                entry -> entry.getValue().lastSeenSequence())
                        .reversed())
                .limit(MAX_CONTEXT_AGGREGATES)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey().trim(), withoutItemHints(entry.getValue())),
                        LinkedHashMap::putAll);
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }

    private static ContextualContextAggregate withoutItemHints(ContextualContextAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        return new ContextualContextAggregate(
                aggregate.contextKey(),
                aggregate.label(),
                aggregate.timesSeen(),
                aggregate.lastSeenSequence(),
                Map.of(),
                aggregate.facetHints());
    }
}
