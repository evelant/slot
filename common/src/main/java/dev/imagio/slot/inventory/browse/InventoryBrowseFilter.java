package dev.imagio.slot.inventory.browse;

public record InventoryBrowseFilter(
        String searchText,
        InventoryBrowseFilterScope scope
) {
    public InventoryBrowseFilter {
        searchText = searchText == null ? "" : searchText;
        scope = scope == null ? InventoryBrowseFilterScope.ALL : scope;
    }

    public static InventoryBrowseFilter empty() {
        return new InventoryBrowseFilter("", InventoryBrowseFilterScope.ALL);
    }
}
