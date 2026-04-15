package dev.imagio.slot.inventory.workspace;

import java.util.List;
import java.util.Objects;

public record InventoryWorkspaceProfile(
        InventoryWorkspaceProfileId id,
        List<InventoryWorkspaceZoneKind> zoneOrder,
        InventoryWorkspaceZoneKind defaultFocusZone,
        String diagnostics
) {
    public InventoryWorkspaceProfile {
        id = id == null ? InventoryWorkspaceProfileId.CARRIED : id;
        zoneOrder = zoneOrder == null ? List.of() : List.copyOf(zoneOrder.stream().filter(Objects::nonNull).toList());
        defaultFocusZone = defaultFocusZone == null ? InventoryWorkspaceZoneKind.PRIMARY_BROWSE : defaultFocusZone;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
