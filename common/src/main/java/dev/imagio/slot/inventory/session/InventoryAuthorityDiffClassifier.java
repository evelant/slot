package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class InventoryAuthorityDiffClassifier {
    private InventoryAuthorityDiffClassifier() {
    }

    public static List<InventoryActivityEvent> classifyCarriedAcquisitions(
            InventoryAuthoritySnapshot previous,
            InventoryAuthoritySnapshot current,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (previous == null
                || current == null
                || previous.host() == null
                || current.host() == null
                || !previous.host().hostId().equals(current.host().hostId())) {
            return List.of();
        }

        Function<InventoryEntrySnapshot, ItemIdentity> resolvedIdentityResolver = identityResolver == null
                ? entry -> entry == null || !entry.present() ? null : ItemIdentityMatcher.create(entry.stack())
                : identityResolver;
        Map<ItemIdentity, Integer> before = carriedCounts(previous, resolvedIdentityResolver);
        Map<ItemIdentity, Integer> after = carriedCounts(current, resolvedIdentityResolver);
        return after.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> {
                    int delta = entry.getValue() - before.getOrDefault(entry.getKey(), 0);
                    if (delta <= 0) {
                        return null;
                    }
                    return new InventoryActivityEvent(
                            InventoryActivityKind.ACQUIRED,
                            InventoryActivityProducer.AUTHORITY_DIFF,
                            InventoryActivityConfidence.OBSERVED,
                            entry.getKey(),
                            delta,
                            null,
                            null,
                            "",
                            "",
                            List.of(),
                            "authority_diff_gain"
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static Map<ItemIdentity, Integer> carriedCounts(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        if (authority == null || authority.host() == null || identityResolver == null) {
            return counts;
        }
        for (InventorySourceDescriptor source : authority.sourcesInPane(InventoryPaneMembership.CARRIED)) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemIdentity identity = identityResolver.apply(entry);
                if (identity != null) {
                    counts.merge(identity, Math.max(0, entry.count()), Integer::sum);
                }
            }
        }
        return Map.copyOf(counts);
    }
}
