package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ProjectedInventoryRow(
        ItemIdentity identity,
        InventoryPaneMembership paneMembership,
        int visibleTotalCount,
        List<ProjectedEntryRef> backingEntries,
        List<String> backingSources,
        String diagnostics
) {
    public ProjectedInventoryRow {
        paneMembership = paneMembership == null ? InventoryPaneMembership.HIDDEN : paneMembership;
        visibleTotalCount = Math.max(0, visibleTotalCount);
        backingEntries = backingEntries == null ? List.of() : List.copyOf(backingEntries);
        backingSources = backingSources == null ? List.of() : List.copyOf(backingSources);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static ProjectedInventoryRow empty(ItemIdentity identity, InventoryPaneMembership paneMembership) {
        return new ProjectedInventoryRow(identity, paneMembership, 0, List.of(), List.of(), "");
    }

    public Set<InventoryEntryKey> backingEntryKeys() {
        LinkedHashSet<InventoryEntryKey> keys = new LinkedHashSet<>();
        for (ProjectedEntryRef entry : backingEntries) {
            if (entry != null) {
                keys.add(entry.entryKey());
            }
        }
        return Set.copyOf(keys);
    }
}
