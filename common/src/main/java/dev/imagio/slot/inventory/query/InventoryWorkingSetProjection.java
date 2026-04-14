package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;

import java.util.List;

public record InventoryWorkingSetProjection(
        InventoryAuthoritySnapshot authority,
        InventoryPaneMembership paneMembership,
        List<ProjectedInventoryRow> rows
) {
    public InventoryWorkingSetProjection {
        authority = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        paneMembership = paneMembership == null ? InventoryPaneMembership.HIDDEN : paneMembership;
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    public static InventoryWorkingSetProjection empty(InventoryPaneMembership paneMembership) {
        return new InventoryWorkingSetProjection(InventoryAuthoritySnapshot.empty(), paneMembership, List.of());
    }
}
