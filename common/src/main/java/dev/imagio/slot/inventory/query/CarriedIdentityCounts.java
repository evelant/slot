package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Canonical, movable-aware counts for every stack in the carried pane.
 * Build this once per authority snapshot and share it across workflow target
 * math, projection, wayfinding, and UI summaries.
 */
public final class CarriedIdentityCounts {
    private static final CarriedIdentityCounts EMPTY = new CarriedIdentityCounts(Map.of());

    private final Map<ItemIdentity, Integer> counts;
    private final Map<String, Integer> itemIdCounts;

    public CarriedIdentityCounts(Map<ItemIdentity, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            this.counts = Map.of();
            this.itemIdCounts = Map.of();
            return;
        }
        LinkedHashMap<ItemIdentity, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : counts.entrySet()) {
            ItemIdentityCollections.mergeCount(
                    normalized,
                    entry.getKey(),
                    entry.getValue() == null ? 0 : entry.getValue());
        }
        if (normalized.isEmpty()) {
            this.counts = Map.of();
            this.itemIdCounts = Map.of();
            return;
        }
        LinkedHashMap<String, Integer> byItemId = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : normalized.entrySet()) {
            ItemIdentity identity = entry.getKey();
            Integer count = entry.getValue();
            if (identity != null && count != null && count > 0) {
                byItemId.merge(identity.itemId(), count, Integer::sum);
            }
        }
        this.counts = Collections.unmodifiableMap(normalized);
        this.itemIdCounts = byItemId.isEmpty() ? Map.of() : Collections.unmodifiableMap(byItemId);
    }

    public static CarriedIdentityCounts empty() {
        return EMPTY;
    }

    public static CarriedIdentityCounts from(InventoryAuthoritySnapshot authority) {
        if (authority == null) {
            return empty();
        }
        LinkedHashMap<ItemIdentity, Integer> out = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present() || entry.stack() == null || entry.stack().isEmpty()) {
                    continue;
                }
                ItemIdentityCollections.mergeCount(out, ItemIdentityMatcher.create(entry.stack()), entry.count());
            }
        }
        return out.isEmpty() ? empty() : new CarriedIdentityCounts(out);
    }

    public Map<ItemIdentity, Integer> counts() {
        return counts;
    }

    public int count(ItemIdentity identity) {
        if (identity == null || counts.isEmpty()) {
            return 0;
        }
        ItemIdentity target = ItemIdentityCollections.key(identity);
        if (target == null) {
            return 0;
        }
        if (target.comparisonMode() == ItemComparisonMode.ITEM_ID) {
            return Math.max(0, itemIdCounts.getOrDefault(target.itemId(), 0));
        }
        int direct = Math.max(0, counts.getOrDefault(target, 0));
        int itemOnly = Math.max(0, counts.getOrDefault(ItemIdentity.of(target.itemId()), 0));
        return direct + itemOnly;
    }

    public boolean contains(ItemIdentity identity) {
        return count(identity) > 0;
    }

    public Set<ItemIdentity> identities() {
        return counts.isEmpty() ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(counts.keySet()));
    }
}
