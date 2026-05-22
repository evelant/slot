package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
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
        Map<ItemIdentity, Integer> beforeCarried = sourceCounts(previous, resolvedIdentityResolver, true);
        Map<ItemIdentity, Integer> afterCarried = sourceCounts(current, resolvedIdentityResolver, true);
        Map<ItemIdentity, Integer> beforeNonCarried = sourceCounts(previous, resolvedIdentityResolver, false);
        Map<ItemIdentity, Integer> afterNonCarried = sourceCounts(current, resolvedIdentityResolver, false);
        Map<ItemIdentity, Integer> beforeCursor = cursorCounts(previous);
        Map<ItemIdentity, Integer> afterCursor = cursorCounts(current);
        LinkedHashMap<ItemIdentity, Integer> acquisitions = new LinkedHashMap<>();
        for (ItemIdentity identity : afterCarried.keySet()) {
            int carriedGain = positiveDelta(beforeCarried, afterCarried, identity);
            int cursorLoss = positiveDelta(afterCursor, beforeCursor, identity);
            int count = Math.max(0, carriedGain - cursorLoss);
            if (count > 0) {
                acquisitions.merge(identity, count, Integer::sum);
            }
        }
        for (ItemIdentity identity : afterCursor.keySet()) {
            int cursorGain = positiveDelta(beforeCursor, afterCursor, identity);
            int nonCarriedLoss = positiveDelta(afterNonCarried, beforeNonCarried, identity);
            int count = Math.min(cursorGain, nonCarriedLoss);
            if (count > 0) {
                acquisitions.merge(identity, count, Integer::sum);
            }
        }
        return acquisitions.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new InventoryActivityEvent(
                        InventoryActivityKind.ACQUIRED,
                        InventoryActivityProducer.AUTHORITY_DIFF,
                        InventoryActivityConfidence.OBSERVED,
                        entry.getKey(),
                        entry.getValue(),
                        null,
                        null,
                        "",
                        "",
                        List.of(),
                        "authority_diff_gain"
                ))
                .toList();
    }

    private static Map<ItemIdentity, Integer> sourceCounts(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            boolean carried
    ) {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        if (authority == null || authority.host() == null || identityResolver == null) {
            return counts;
        }
        for (InventorySourceDescriptor source : authority.sourceDescriptors()) {
            if (source == null || (source.paneMembership() == InventoryPaneMembership.CARRIED) != carried) {
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

    private static Map<ItemIdentity, Integer> cursorCounts(InventoryAuthoritySnapshot authority) {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        if (authority == null) {
            return counts;
        }
        CursorStateSnapshot cursor = authority.cursorState();
        if (cursor != null && cursor.present()) {
            ItemIdentity identity = ItemIdentityMatcher.create(cursor.stack());
            if (identity != null) {
                counts.merge(identity, Math.max(0, cursor.stack().getCount()), Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    private static int positiveDelta(
            Map<ItemIdentity, Integer> before,
            Map<ItemIdentity, Integer> after,
            ItemIdentity identity
    ) {
        if (identity == null) {
            return 0;
        }
        return Math.max(0, after.getOrDefault(identity, 0) - before.getOrDefault(identity, 0));
    }
}
