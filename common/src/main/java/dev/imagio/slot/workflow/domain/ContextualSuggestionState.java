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
        Map<ItemIdentity, ContextualCarriedObservation> activeCarried,
        List<ContextualSignalRecord> recentSignals,
        String activeContextKey
) {
    public static final int MAX_ITEM_AGGREGATES = 512;
    public static final int MAX_CONTEXT_AGGREGATES = 64;
    public static final int MAX_RECENT_SIGNALS = 128;
    public static final String STATION_COOCCURRENCE_HINT_PREFIX = "station_item:";

    public ContextualSuggestionState {
        nextStreamSequence = Math.max(1L, nextStreamSequence);
        itemAggregates = itemAggregates == null ? Map.of() : copyItems(itemAggregates);
        contextAggregates = contextAggregates == null ? Map.of() : copyContexts(contextAggregates);
        activeCarried = activeCarried == null ? Map.of() : Map.copyOf(activeCarried);
        recentSignals = recentSignals == null ? List.of() : List.copyOf(recentSignals.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingLong(record -> record.envelope().globalSequence()))
                .skip(Math.max(0, recentSignals.size() - MAX_RECENT_SIGNALS))
                .toList());
        activeContextKey = activeContextKey == null ? "" : activeContextKey.trim();
    }

    public static ContextualSuggestionState empty() {
        return new ContextualSuggestionState(1L, Map.of(), Map.of(), Map.of(), List.of(), "");
    }

    public ContextualSuggestionState record(ContextualSignalRecord record) {
        if (record == null || record.event() == null) {
            return this;
        }
        ContextualSignalEvent event = record.event();
        long sequence = record.envelope().globalSequence();
        LinkedHashMap<ItemIdentity, ContextualItemAggregate> nextItems = new LinkedHashMap<>(itemAggregates);
        LinkedHashMap<String, ContextualContextAggregate> nextContexts = new LinkedHashMap<>(contextAggregates);
        LinkedHashMap<ItemIdentity, ContextualCarriedObservation> nextActive = new LinkedHashMap<>(activeCarried);
        String nextActiveContextKey = activeContextKey;

        ItemIdentity identity = event.identity();
        if (identity != null) {
            ContextualItemAggregate aggregate = nextItems.getOrDefault(identity, ContextualItemAggregate.empty(identity));
            aggregate = switch (event.kind()) {
                case CARRIED_SET_CHANGED -> applyCarriedChange(aggregate, nextActive, identity, event, sequence);
                case ITEM_TAKEN_FROM_STORAGE -> aggregate.acquired(sequence, true, false);
                case ITEM_CRAFTED_OR_PRODUCED -> aggregate.acquired(sequence, false, true);
                case ITEM_ACQUIRED -> aggregate.acquired(sequence, false, false);
                case ITEM_DEPOSITED_TO_STORAGE -> aggregate.deposited(sequence);
                default -> aggregate;
            };
            nextItems.put(identity, aggregate);
        }

        if (!event.contextKey().isBlank()) {
            LinkedHashMap<String, Double> itemHints = new LinkedHashMap<>();
            if (identity != null) {
                itemHints.merge(identity.itemId(), 1.0D, Double::sum);
            }
            ContextualContextAggregate aggregate = nextContexts.getOrDefault(
                    event.contextKey(),
                    ContextualContextAggregate.empty(event.contextKey(), event.contextLabel()));
            nextContexts.put(event.contextKey(), aggregate.seen(event.contextLabel(), sequence, itemHints));
            if (event.kind() == ContextualSignalKind.STATION_OPENED) {
                nextActiveContextKey = event.contextKey();
            }
            nextItems = learnStationCooccurrence(nextItems, identity, event, sequence);
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
                nextActive,
                nextSignals,
                nextActiveContextKey);
    }

    private static String itemHintKey(ItemIdentity identity) {
        return identity == null || identity.itemId().isBlank()
                ? ""
                : STATION_COOCCURRENCE_HINT_PREFIX + identity.itemId();
    }

    private LinkedHashMap<ItemIdentity, ContextualItemAggregate> learnStationCooccurrence(
            LinkedHashMap<ItemIdentity, ContextualItemAggregate> items,
            ItemIdentity identity,
            ContextualSignalEvent event,
            long sequence
    ) {
        if (identity == null || event.kind() != ContextualSignalKind.STATION_CONTENTS_CHANGED) {
            return items;
        }
        LinkedHashMap<ItemIdentity, ContextualItemAggregate> next = items;
        for (ContextualSignalRecord recent : recentSignals) {
            if (recent == null || recent.event() == null || recent.envelope() == null) {
                continue;
            }
            ContextualSignalEvent previous = recent.event();
            ItemIdentity previousIdentity = previous.identity();
            if (previousIdentity == null || previousIdentity.equals(identity)) {
                continue;
            }
            if (previous.kind() != ContextualSignalKind.STATION_CONTENTS_CHANGED) {
                continue;
            }
            if (!event.contextKey().equals(previous.contextKey())) {
                continue;
            }
            if (sequence - recent.envelope().globalSequence() > 8L) {
                continue;
            }
            ContextualItemAggregate currentAggregate =
                    next.getOrDefault(identity, ContextualItemAggregate.empty(identity))
                            .withHint(itemHintKey(previousIdentity), 1.0D);
            ContextualItemAggregate previousAggregate =
                    next.getOrDefault(previousIdentity, ContextualItemAggregate.empty(previousIdentity))
                            .withHint(itemHintKey(identity), 1.0D);
            next.put(identity, currentAggregate);
            next.put(previousIdentity, previousAggregate);
        }
        return next;
    }

    public ContextualSuggestionState withActiveCarried(
            Map<ItemIdentity, ContextualCarriedObservation> nextActiveCarried
    ) {
        return new ContextualSuggestionState(
                nextStreamSequence,
                itemAggregates,
                contextAggregates,
                nextActiveCarried,
                recentSignals,
                activeContextKey);
    }

    public ContextualSuggestionState withActiveContextKey(String nextActiveContextKey) {
        return new ContextualSuggestionState(
                nextStreamSequence,
                itemAggregates,
                contextAggregates,
                activeCarried,
                recentSignals,
                nextActiveContextKey);
    }

    private static ContextualItemAggregate applyCarriedChange(
            ContextualItemAggregate aggregate,
            Map<ItemIdentity, ContextualCarriedObservation> active,
            ItemIdentity identity,
            ContextualSignalEvent event,
            long sequence
    ) {
        String phase = event.metadataValue("phase");
        if ("end".equals(phase)) {
            ContextualCarriedObservation previous = active.remove(identity);
            long elapsed = Math.max(0L, event.count());
            if (elapsed <= 0L && previous != null && event.observedTick() > 0L) {
                elapsed = Math.max(0L, event.observedTick() - previous.firstSeenTick());
            }
            return aggregate.carriedFor(elapsed, sequence);
        }
        active.put(identity, new ContextualCarriedObservation(
                identity,
                sequence,
                event.observedTick(),
                event.observedTick(),
                event.count(),
                event.sourceKey()));
        return aggregate.observedCarried(sequence);
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
                                        entry.getValue().lastCarriedSequence(),
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
                        (map, entry) -> map.put(entry.getKey().trim(), entry.getValue()),
                        LinkedHashMap::putAll);
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }
}
