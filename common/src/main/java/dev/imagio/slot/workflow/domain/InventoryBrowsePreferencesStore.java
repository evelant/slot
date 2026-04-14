package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;

public interface InventoryBrowsePreferencesStore {
    InventoryBrowsePreferences current();

    void replaceWith(InventoryBrowsePreferences preferences);

    default void update(java.util.function.UnaryOperator<InventoryBrowsePreferences> updater) {
        java.util.function.UnaryOperator<InventoryBrowsePreferences> resolved = updater == null ? current -> current : updater;
        replaceWith(resolved.apply(current()));
    }
}
