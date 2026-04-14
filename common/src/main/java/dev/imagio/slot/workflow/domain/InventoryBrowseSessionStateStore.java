package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;

public interface InventoryBrowseSessionStateStore {
    InventoryBrowseSessionState current();

    void replaceWith(InventoryBrowseSessionState state);

    default void update(java.util.function.UnaryOperator<InventoryBrowseSessionState> updater) {
        java.util.function.UnaryOperator<InventoryBrowseSessionState> resolved = updater == null ? current -> current : updater;
        replaceWith(resolved.apply(current()));
    }
}
