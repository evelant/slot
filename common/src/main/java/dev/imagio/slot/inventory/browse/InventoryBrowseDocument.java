package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;

import java.util.List;
import java.util.Objects;

public record InventoryBrowseDocument(
        InventoryBrowsePaneMode paneMode,
        InventoryPaneMembership activePane,
        List<InventoryBrowsePane> panes,
        InventoryBrowseSessionState sessionState,
        String diagnostics
) {
    public InventoryBrowseDocument {
        paneMode = paneMode == null ? InventoryBrowsePaneMode.CARRIED_ONLY : paneMode;
        activePane = activePane == null ? InventoryPaneMembership.CARRIED : activePane;
        panes = panes == null ? List.of() : List.copyOf(panes.stream().filter(Objects::nonNull).toList());
        sessionState = sessionState == null ? InventoryBrowseSessionState.defaults(InventoryBrowsePreferences.defaults()) : sessionState;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
