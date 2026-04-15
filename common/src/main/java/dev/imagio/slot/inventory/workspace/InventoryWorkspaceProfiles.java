package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.session.InventorySessionSnapshot;

import java.util.List;

public final class InventoryWorkspaceProfiles {
    private static final InventoryWorkspaceProfile CARRIED = new InventoryWorkspaceProfile(
            InventoryWorkspaceProfileId.CARRIED,
            List.of(
                    InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
                    InventoryWorkspaceZoneKind.QUICK_ACCESS,
                    InventoryWorkspaceZoneKind.EQUIPMENT,
                    InventoryWorkspaceZoneKind.WORKFLOW_RAIL,
                    InventoryWorkspaceZoneKind.TOOL_DOCK,
                    InventoryWorkspaceZoneKind.STATUS_RAIL
            ),
            InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
            ""
    );
    private static final InventoryWorkspaceProfile DUAL_PANE = new InventoryWorkspaceProfile(
            InventoryWorkspaceProfileId.DUAL_PANE,
            List.of(
                    InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
                    InventoryWorkspaceZoneKind.SECONDARY_BROWSE,
                    InventoryWorkspaceZoneKind.QUICK_ACCESS,
                    InventoryWorkspaceZoneKind.EQUIPMENT,
                    InventoryWorkspaceZoneKind.WORKFLOW_RAIL,
                    InventoryWorkspaceZoneKind.TOOL_DOCK,
                    InventoryWorkspaceZoneKind.STATUS_RAIL
            ),
            InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
            ""
    );
    private static final InventoryWorkspaceProfile TERMINAL_HYBRID = new InventoryWorkspaceProfile(
            InventoryWorkspaceProfileId.TERMINAL_HYBRID,
            List.of(
                    InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
                    InventoryWorkspaceZoneKind.TOOL_DOCK,
                    InventoryWorkspaceZoneKind.SECONDARY_BROWSE,
                    InventoryWorkspaceZoneKind.QUICK_ACCESS,
                    InventoryWorkspaceZoneKind.EQUIPMENT,
                    InventoryWorkspaceZoneKind.WORKFLOW_RAIL,
                    InventoryWorkspaceZoneKind.STATUS_RAIL
            ),
            InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
            ""
    );

    private InventoryWorkspaceProfiles() {
    }

    public static InventoryWorkspaceProfile select(InventorySessionSnapshot snapshot) {
        if (snapshot == null || snapshot.host() == null) {
            return CARRIED;
        }
        InventoryHostDescriptor host = snapshot.host();
        InventoryBrowseDocument browseDocument = snapshot.browseDocument();
        if (host.observationHints().carriedOnly()) {
            return CARRIED;
        }
        if (host.observationHints().hostFamilyHint() == InventoryHostFamilyHint.TERMINAL_HYBRID) {
            return TERMINAL_HYBRID;
        }
        boolean hasCarriedPane = hasVisiblePane(browseDocument, InventoryPaneMembership.CARRIED);
        boolean hasExternalPane = hasVisiblePane(browseDocument, InventoryPaneMembership.EXTERNAL);
        if (host.observationHints().hostFamilyHint() == InventoryHostFamilyHint.DUAL_PANE || (hasCarriedPane && hasExternalPane)) {
            return DUAL_PANE;
        }
        return hasExternalPane ? DUAL_PANE : CARRIED;
    }

    private static boolean hasVisiblePane(InventoryBrowseDocument browseDocument, InventoryPaneMembership paneMembership) {
        return browseDocument != null
                && browseDocument.panes().stream().anyMatch(pane -> pane != null && pane.paneMembership() == paneMembership);
    }
}
