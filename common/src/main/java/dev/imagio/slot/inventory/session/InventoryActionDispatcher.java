package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;

@FunctionalInterface
public interface InventoryActionDispatcher {
    void dispatch(InventoryActionRequest request);
}
