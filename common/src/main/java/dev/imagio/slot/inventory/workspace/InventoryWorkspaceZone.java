package dev.imagio.slot.inventory.workspace;

import java.util.List;
import java.util.Objects;

public record InventoryWorkspaceZone(
        String id,
        InventoryWorkspaceZoneKind kind,
        boolean primary,
        List<InventoryWorkspaceSurface> surfaces,
        String diagnostics
) {
    public InventoryWorkspaceZone {
        id = id == null ? "" : id;
        kind = kind == null ? InventoryWorkspaceZoneKind.STATUS_RAIL : kind;
        surfaces = surfaces == null ? List.of() : List.copyOf(surfaces.stream().filter(Objects::nonNull).toList());
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
