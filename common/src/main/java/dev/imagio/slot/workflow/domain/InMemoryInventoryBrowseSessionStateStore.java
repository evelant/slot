package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;

public final class InMemoryInventoryBrowseSessionStateStore implements InventoryBrowseSessionStateStore {
    private InventoryBrowseSessionState current = InventoryBrowseSessionState.defaults(InventoryBrowsePreferences.defaults());

    @Override
    public InventoryBrowseSessionState current() {
        return current;
    }

    @Override
    public void replaceWith(InventoryBrowseSessionState state) {
        current = state == null ? InventoryBrowseSessionState.defaults(InventoryBrowsePreferences.defaults()) : state;
    }
}
