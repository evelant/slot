package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ContextualSuggestionDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;
    private String stationSnapshotContextKey = "";
    private Map<StationSampleKey, StationSample> stationSnapshot = Map.of();

    public ContextualSuggestionDomainService(WorkflowDomainStateRepository repository, Runnable mutationObserver) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public ContextualSuggestionState state() {
        return repository.contextualSuggestionState();
    }

    public boolean recordSignal(ContextualSignalEvent event, DomainEventMetadata metadata) {
        if (event == null) {
            return false;
        }
        repository.appendContextualSignal(
                event,
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("contextual.signal")
        );
        mutationObserver.run();
        return true;
    }

    public boolean observeActivityRecord(InventoryActivityRecord record) {
        if (record == null || record.event() == null || !record.event().present()) {
            return false;
        }
        InventoryActivityEvent activity = record.event();
        ContextualSignalKind kind = switch (activity.kind()) {
            case ACQUIRED, TRANSFERRED, RESTORED -> activity.producer() == InventoryActivityProducer.EXTERNAL_WITHDRAWAL
                    ? ContextualSignalKind.ITEM_TAKEN_FROM_STORAGE
                    : ContextualSignalKind.ITEM_ACQUIRED;
            case CRAFTED, SMELTED -> ContextualSignalKind.ITEM_CRAFTED_OR_PRODUCED;
            case TRASHED, VOIDED, OVERFLOW_STAGED -> null;
        };
        if (kind == null) {
            return false;
        }
        repository.appendContextualSignal(
                ContextualSignalEvent.item(kind, activity.identity(), activity.count(), sourceKey(activity)),
                new DomainEventMetadata(
                        "contextual.activity",
                        record.envelope().correlationId(),
                        record.envelope().causationId(),
                        record.envelope().sessionId())
        );
        return true;
    }

    public boolean observeCarriedSnapshot(
            InventoryAuthoritySnapshot authority,
            long currentTick,
            DomainEventMetadata metadata
    ) {
        InventoryAuthoritySnapshot resolved = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        LinkedHashMap<ItemIdentity, CarriedSample> current = carriedSamples(resolved);
        ContextualSuggestionState state = repository.contextualSuggestionState();
        boolean changed = false;

        for (Map.Entry<ItemIdentity, CarriedSample> entry : current.entrySet()) {
            if (state.activeCarried().containsKey(entry.getKey())) {
                continue;
            }
            ContextualSignalEvent event = new ContextualSignalEvent(
                    ContextualSignalKind.CARRIED_SET_CHANGED,
                    entry.getKey(),
                    entry.getValue().count(),
                    currentTick,
                    "",
                    "",
                    entry.getValue().sourceKey(),
                    Map.of("phase", "start"));
            repository.appendContextualSignal(event, resolveMetadata(metadata, "contextual.carried.start"));
            state = repository.contextualSuggestionState();
            changed = true;
        }

        for (ContextualCarriedObservation observation : state.activeCarried().values()) {
            if (observation == null || observation.identity() == null || current.containsKey(observation.identity())) {
                continue;
            }
            long elapsed = Math.max(0L, currentTick - observation.firstSeenTick());
            ContextualSignalEvent event = new ContextualSignalEvent(
                    ContextualSignalKind.CARRIED_SET_CHANGED,
                    observation.identity(),
                    (int) Math.min(Integer.MAX_VALUE, elapsed),
                    currentTick,
                    "",
                    "",
                    observation.sourceKey(),
                    Map.of("phase", "end"));
            repository.appendContextualSignal(event, resolveMetadata(metadata, "contextual.carried.end"));
            changed = true;
        }

        if (changed) {
            mutationObserver.run();
        }
        return changed;
    }

    public boolean observeStationOpened(
            InventoryHostDescriptor host,
            DomainEventMetadata metadata
    ) {
        String contextKey = contextKey(host);
        if (contextKey.isBlank()) {
            ContextualSuggestionState state = repository.contextualSuggestionState();
            if (!state.activeContextKey().isBlank()) {
                repository.replaceContextualSuggestionState(state.withActiveContextKey(""));
                mutationObserver.run();
                return true;
            }
            return false;
        }
        ContextualSuggestionState state = repository.contextualSuggestionState();
        if (contextKey.equals(state.activeContextKey()) && !hasFreshContextHints(state, contextKey)) {
            return false;
        }
        ContextualSignalEvent event = new ContextualSignalEvent(
                ContextualSignalKind.STATION_OPENED,
                null,
                1,
                0L,
                contextKey,
                contextLabel(host),
                "",
                Map.of());
        repository.appendContextualSignal(event, resolveMetadata(metadata, "contextual.station.opened"));
        mutationObserver.run();
        return true;
    }

    public boolean observeStationContext(
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority,
            long currentTick,
            DomainEventMetadata metadata
    ) {
        boolean changed = observeStationOpened(host, metadata);
        return observeStationContents(host, authority, currentTick, metadata) || changed;
    }

    public boolean observeStationContents(
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority,
            long currentTick,
            DomainEventMetadata metadata
    ) {
        String contextKey = contextKey(host);
        if (contextKey.isBlank() || host == null || authority == null) {
            stationSnapshotContextKey = "";
            stationSnapshot = Map.of();
            return false;
        }
        Map<StationSampleKey, StationSample> current = stationSamples(host, authority);
        if (current.isEmpty()) {
            stationSnapshotContextKey = contextKey;
            stationSnapshot = current;
            return false;
        }
        if (!contextKey.equals(stationSnapshotContextKey)) {
            stationSnapshotContextKey = contextKey;
            stationSnapshot = current;
            return false;
        }

        ArrayList<ContextualSignalEvent> events = new ArrayList<>();
        for (Map.Entry<StationSampleKey, StationSample> entry : current.entrySet()) {
            StationSample previous = stationSnapshot.get(entry.getKey());
            StationSample next = entry.getValue();
            if (previous == null) {
                events.add(stationChange(next, currentTick, contextKey, contextLabel(host), "increase"));
                continue;
            }
            if (!previous.identity().equals(next.identity())) {
                events.add(stationChange(previous, currentTick, contextKey, contextLabel(host), "decrease"));
                events.add(stationChange(next, currentTick, contextKey, contextLabel(host), "increase"));
                continue;
            }
            int delta = next.count() - previous.count();
            if (delta != 0) {
                events.add(stationChange(
                        new StationSample(next.identity(), Math.abs(delta), next.sourceKey()),
                        currentTick,
                        contextKey,
                        contextLabel(host),
                        delta > 0 ? "increase" : "decrease"));
            }
        }
        for (Map.Entry<StationSampleKey, StationSample> entry : stationSnapshot.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                events.add(stationChange(entry.getValue(), currentTick, contextKey, contextLabel(host), "decrease"));
            }
        }

        stationSnapshot = current;
        if (events.isEmpty()) {
            return false;
        }
        DomainEventMetadata resolvedMetadata = resolveMetadata(metadata, "contextual.station.contents_changed");
        for (ContextualSignalEvent event : events) {
            repository.appendContextualSignal(event, resolvedMetadata);
        }
        mutationObserver.run();
        return true;
    }

    public boolean observeItemUse(
            ItemIdentity identity,
            long currentTick,
            String action,
            String targetKey,
            String sourceKey,
            DomainEventMetadata metadata
    ) {
        return observeItemSignal(
                ContextualSignalKind.ITEM_USED,
                identity,
                1,
                currentTick,
                action,
                targetKey,
                sourceKey,
                metadata,
                "contextual.item.used");
    }

    public boolean observeItemPlaced(
            ItemIdentity identity,
            int count,
            long currentTick,
            String targetKey,
            String sourceKey,
            DomainEventMetadata metadata
    ) {
        return observeItemSignal(
                ContextualSignalKind.ITEM_PLACED,
                identity,
                count,
                currentTick,
                "place_block",
                targetKey,
                sourceKey,
                metadata,
                "contextual.item.placed");
    }

    public boolean observeItemConsumed(
            ItemIdentity identity,
            int count,
            long currentTick,
            String sourceKey,
            DomainEventMetadata metadata
    ) {
        return observeItemSignal(
                ContextualSignalKind.ITEM_CONSUMED,
                identity,
                count,
                currentTick,
                "consume_finish",
                "",
                sourceKey,
                metadata,
                "contextual.item.consumed");
    }

    public boolean observeItemDamaged(
            ItemIdentity identity,
            long currentTick,
            String action,
            String sourceKey,
            DomainEventMetadata metadata
    ) {
        return observeItemSignal(
                ContextualSignalKind.ITEM_DAMAGED,
                identity,
                1,
                currentTick,
                action,
                "",
                sourceKey,
                metadata,
                "contextual.item.damaged");
    }

    private static boolean hasFreshContextHints(ContextualSuggestionState state, String contextKey) {
        if (state == null || contextKey == null || contextKey.isBlank()) {
            return false;
        }
        ContextualContextAggregate aggregate = state.contextAggregates().get(contextKey);
        return aggregate == null;
    }

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }

    private boolean observeItemSignal(
            ContextualSignalKind kind,
            ItemIdentity identity,
            int count,
            long currentTick,
            String action,
            String targetKey,
            String sourceKey,
            DomainEventMetadata metadata,
            String origin
    ) {
        if (identity == null) {
            return false;
        }
        String resolvedAction = action == null ? "" : action.trim();
        String resolvedTarget = targetKey == null ? "" : targetKey.trim();
        LinkedHashMap<String, String> eventMetadata = new LinkedHashMap<>();
        if (!resolvedAction.isBlank()) {
            eventMetadata.put("action", resolvedAction);
        }
        if (!resolvedTarget.isBlank()) {
            eventMetadata.put("target", resolvedTarget);
        }
        repository.appendContextualSignal(
                new ContextualSignalEvent(
                        kind,
                        identity,
                        Math.max(1, count),
                        currentTick,
                        resolvedAction.isBlank() ? "" : "world:" + resolvedAction,
                        resolvedTarget,
                        sourceKey,
                        eventMetadata),
                resolveMetadata(metadata, origin)
        );
        mutationObserver.run();
        return true;
    }

    private static LinkedHashMap<ItemIdentity, CarriedSample> carriedSamples(InventoryAuthoritySnapshot authority) {
        LinkedHashMap<ItemIdentity, CarriedSample> samples = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(entry.stack()));
                samples.compute(identity, (ignored, previous) -> previous == null
                        ? new CarriedSample(entry.count(), source.id())
                        : new CarriedSample(previous.count() + entry.count(), previous.sourceKey()));
            }
        }
        return samples;
    }

    private static Map<StationSampleKey, StationSample> stationSamples(
            InventoryHostDescriptor host,
            InventoryAuthoritySnapshot authority
    ) {
        if (host == null || authority == null || host.toolDescriptors().isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<StationSampleKey, StationSample> samples = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : authority.sourceDescriptors()) {
            if (!stationObservedSource(source)) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.itemOnly(entry.stack());
                if (identity == null) {
                    continue;
                }
                samples.put(
                        new StationSampleKey(entry.entryKey().stableKey()),
                        new StationSample(identity, entry.count(), source.id()));
            }
        }
        return samples.isEmpty() ? Map.of() : Map.copyOf(samples);
    }

    private static boolean stationObservedSource(InventorySourceDescriptor source) {
        return source != null
                && source.domain() == InventorySourceDomain.TOOL_REGION
                && source.paneMembership() != InventoryPaneMembership.CARRIED;
    }

    private static ContextualSignalEvent stationChange(
            StationSample sample,
            long currentTick,
            String contextKey,
            String contextLabel,
            String change
    ) {
        StationSample resolved = sample == null ? new StationSample(null, 0, "") : sample;
        return new ContextualSignalEvent(
                ContextualSignalKind.STATION_CONTENTS_CHANGED,
                resolved.identity(),
                resolved.count(),
                currentTick,
                contextKey,
                contextLabel,
                resolved.sourceKey(),
                Map.of("change", change == null ? "" : change));
    }

    private static String sourceKey(InventoryActivityEvent activity) {
        if (activity == null || activity.fromTarget() == null) {
            return "";
        }
        return activity.fromTarget().toString();
    }

    private static String contextKey(InventoryHostDescriptor host) {
        if (host == null) {
            return "";
        }
        if (host.serverMenuRef() != null && !host.serverMenuRef().menuClassName().isBlank()) {
            return "menu:" + host.serverMenuRef().menuClassName();
        }
        if (!host.screenClassName().isBlank()) {
            return "screen:" + host.screenClassName();
        }
        if (host.hostId() != null && !host.hostId().menuClass().isBlank()) {
            return "host:" + host.hostId().menuClass();
        }
        return "";
    }

    private static String contextLabel(InventoryHostDescriptor host) {
        if (host == null || host.title() == null) {
            return "";
        }
        String label = host.title().getString();
        return label == null ? "" : label;
    }

    private record CarriedSample(int count, String sourceKey) {
        private CarriedSample {
            count = Math.max(0, count);
            sourceKey = sourceKey == null ? "" : sourceKey;
        }
    }

    private record StationSampleKey(String value) {
        private StationSampleKey {
            value = value == null ? "" : value;
        }
    }

    private record StationSample(ItemIdentity identity, int count, String sourceKey) {
        private StationSample {
            count = Math.max(0, count);
            sourceKey = sourceKey == null ? "" : sourceKey;
        }
    }
}
