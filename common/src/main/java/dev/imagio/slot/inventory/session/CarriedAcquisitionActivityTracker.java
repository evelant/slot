package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class CarriedAcquisitionActivityTracker {
    private final Map<String, InventoryAuthoritySnapshot> baselinesByKey = new HashMap<>();
    private final Set<String> suppressNextByKey = new HashSet<>();

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
            return 0;
        }
        int recorded = 0;
        for (InventoryActivityEvent activityEvent : InventoryAuthorityDiffClassifier.classifyCarriedAcquisitions(
                previous,
                resolvedAuthority,
                identityResolver
        )) {
            if (runtime.recordActivityEvent(
                    activityEvent,
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
        }
    }

    public void forget(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        baselinesByKey.remove(key);
        suppressNextByKey.remove(key);
    }

    public void clear() {
        baselinesByKey.clear();
        suppressNextByKey.clear();
    }
}
