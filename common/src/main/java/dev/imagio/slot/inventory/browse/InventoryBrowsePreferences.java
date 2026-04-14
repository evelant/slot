package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryActionScope;

public record InventoryBrowsePreferences(
        InventoryBrowseSortMode defaultSortMode,
        InventoryBrowseGroupingMode defaultGroupingMode,
        InventoryBrowsePaneMode defaultPaneMode,
        InventoryActionScope defaultBulkActionScope
) {
    public InventoryBrowsePreferences {
        defaultSortMode = defaultSortMode == null ? InventoryBrowseSortMode.NAME : defaultSortMode;
        defaultGroupingMode = defaultGroupingMode == null ? InventoryBrowseGroupingMode.CATEGORY : defaultGroupingMode;
        defaultPaneMode = defaultPaneMode == null ? InventoryBrowsePaneMode.CARRIED_ONLY : defaultPaneMode;
        defaultBulkActionScope = defaultBulkActionScope == null ? InventoryActionScope.VISIBLE_MATCHES : defaultBulkActionScope;
    }

    public static InventoryBrowsePreferences defaults() {
        return new InventoryBrowsePreferences(
                InventoryBrowseSortMode.NAME,
                InventoryBrowseGroupingMode.CATEGORY,
                InventoryBrowsePaneMode.CARRIED_ONLY,
                InventoryActionScope.VISIBLE_MATCHES
        );
    }
}
