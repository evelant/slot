package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.session.InventorySessionToken;

import java.util.List;
import java.util.Objects;

public record InventoryWorkspaceModel(
        InventorySessionToken sessionToken,
        HostInstanceKey hostId,
        InventoryWorkspaceProfile profile,
        List<InventoryWorkspaceZone> zones,
        InventoryWorkspaceSubjectRef defaultFocusSubject,
        InventoryWorkspaceStatus status,
        String diagnostics
) {
    public InventoryWorkspaceModel {
        sessionToken = sessionToken == null ? new InventorySessionToken("", 0L) : sessionToken;
        hostId = hostId == null ? HostInstanceKey.empty() : hostId;
        profile = profile == null
                ? new InventoryWorkspaceProfile(
                        InventoryWorkspaceProfileId.CARRIED,
                        List.of(InventoryWorkspaceZoneKind.STATUS_RAIL),
                        InventoryWorkspaceZoneKind.STATUS_RAIL,
                        ""
                )
                : profile;
        zones = zones == null ? List.of() : List.copyOf(zones.stream().filter(Objects::nonNull).toList());
        status = status == null
                ? new InventoryWorkspaceStatus(null, false, 0, false, false, List.of())
                : status;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static InventoryWorkspaceModel empty(InventorySessionToken sessionToken, HostInstanceKey hostId) {
        return new InventoryWorkspaceModel(
                sessionToken,
                hostId,
                new InventoryWorkspaceProfile(
                        InventoryWorkspaceProfileId.CARRIED,
                        List.of(InventoryWorkspaceZoneKind.STATUS_RAIL),
                        InventoryWorkspaceZoneKind.STATUS_RAIL,
                        ""
                ),
                List.of(),
                null,
                new InventoryWorkspaceStatus(null, false, 0, false, false, List.of()),
                ""
        );
    }

    public InventoryWorkspaceProfileId profileId() {
        return profile.id();
    }

    public InventoryWorkspaceZone zone(InventoryWorkspaceZoneKind kind) {
        if (kind == null) {
            return null;
        }
        return zones.stream()
                .filter(zone -> zone.kind() == kind)
                .findFirst()
                .orElse(null);
    }
}
