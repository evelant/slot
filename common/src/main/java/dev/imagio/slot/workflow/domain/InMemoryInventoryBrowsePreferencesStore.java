package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;

public final class InMemoryInventoryBrowsePreferencesStore implements InventoryBrowsePreferencesStore {
    private InventoryBrowsePreferences current = InventoryBrowsePreferences.defaults();

    @Override
    public InventoryBrowsePreferences current() {
        return current;
    }

    @Override
    public void replaceWith(InventoryBrowsePreferences preferences) {
        current = preferences == null ? InventoryBrowsePreferences.defaults() : preferences;
    }
}
