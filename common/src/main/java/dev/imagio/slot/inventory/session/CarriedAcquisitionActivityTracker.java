package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class CarriedAcquisitionActivityTracker {
    private final Map<String, InventoryAuthoritySnapshot> baselinesByKey = new HashMap<>();
    private final Set<String> suppressNextByKey = new HashSet<>();
    private final Map<String, Map<ItemIdentity, Integer>> suppressedAcquisitionsByKey = new HashMap<>();

    public int observe(
            String key,
            InventoryAuthoritySnapshot authority,
            WorkflowDomainRuntime runtime,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            String sessionId
    ) {
        if (key == null || key.isBlank()) {
            return 0;
        }
        InventoryAuthoritySnapshot resolvedAuthority = authority == null
                ? InventoryAuthoritySnapshot.empty()
                : authority;
        InventoryAuthoritySnapshot previous = baselinesByKey.put(key, resolvedAuthority);
        if (runtime == null || previous == null) {
            suppressNextByKey.remove(key);
            return 0;
        }
        if (suppressNextByKey.remove(key)) {
            suppressedAcquisitionsByKey.remove(key);
            return 0;
        }
        Map<ItemIdentity, Integer> suppressed = suppressedAcquisitionsByKey.remove(key);
        int recorded = 0;
        for (InventoryActivityEvent activityEvent : InventoryAuthorityDiffClassifier.classifyCarriedAcquisitions(
                previous,
                resolvedAuthority,
                identityResolver
        )) {
            InventoryActivityEvent resolvedActivityEvent = suppress(activityEvent, suppressed);
            if (resolvedActivityEvent == null) {
                continue;
            }
            if (runtime.recordActivityEvent(
                    resolvedActivityEvent,
                    new DomainEventMetadata("activity.authority_diff", "", "", sessionId == null ? "" : sessionId)
            )) {
                recorded++;
            }
        }
        return recorded;
    }

    public void suppressNext(String key) {
        if (key != null && !key.isBlank()) {
            suppressNextByKey.add(key);
            suppressedAcquisitionsByKey.remove(key);
        }
    }

    public void suppressAcquired(String key, ItemIdentity identity, int count) {
        if (key == null || key.isBlank() || identity == null || count <= 0) {
            return;
        }
        suppressedAcquisitionsByKey
                .computeIfAbsent(key, ignored -> new HashMap<>())
                .merge(ItemIdentityCollections.key(identity), count, Integer::sum);
    }

    public void suppressAcquired(String key, Collection<InventoryActivityEvent> events) {
        if (key == null || key.isBlank() || events == null || events.isEmpty()) {
            return;
        }
        for (InventoryActivityEvent event : events) {
            if (event == null || !event.present()) {
                continue;
            }
            if (event.kind() == InventoryActivityKind.ACQUIRED || event.kind() == InventoryActivityKind.CRAFTED) {
                suppressAcquired(key, event.identity(), event.count());
            }
        }
    }

    public void forget(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        baselinesByKey.remove(key);
        suppressNextByKey.remove(key);
        suppressedAcquisitionsByKey.remove(key);
    }

    public void clear() {
        baselinesByKey.clear();
        suppressNextByKey.clear();
        suppressedAcquisitionsByKey.clear();
    }

    private static InventoryActivityEvent suppress(
            InventoryActivityEvent event,
            Map<ItemIdentity, Integer> suppressed
    ) {
        if (event == null || !event.present() || suppressed == null || suppressed.isEmpty()) {
            return event;
        }
        int suppressedCount = ItemIdentityCollections.count(suppressed, event.identity());
        if (suppressedCount <= 0) {
            return event;
        }
        int remainingSuppressed = suppressedCount - event.count();
        ItemIdentityCollections.putOrClear(suppressed, event.identity(), remainingSuppressed);
        int remainingEventCount = event.count() - suppressedCount;
        if (remainingEventCount <= 0) {
            return null;
        }
        return new InventoryActivityEvent(
                event.kind(),
                event.producer(),
                event.confidence(),
                event.identity(),
                remainingEventCount,
                event.fromTarget(),
                event.toTarget(),
                event.requestId(),
                event.recoveryToken(),
                event.reasonCodes(),
                event.diagnostics()
        );
    }
}
