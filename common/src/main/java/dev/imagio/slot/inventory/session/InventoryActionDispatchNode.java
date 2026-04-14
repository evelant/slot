package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;

public record InventoryActionDispatchNode(
        InventoryActionRequest request,
        InventoryActionDispatchNode onSuccess,
        InventoryActionDispatchNode onFailure
) {
    public static InventoryActionDispatchNode of(InventoryActionRequest request) {
        return new InventoryActionDispatchNode(request, null, null);
    }

    public static InventoryActionDispatchNode chain(
            InventoryActionRequest request,
            InventoryActionDispatchNode onSuccess,
            InventoryActionDispatchNode onFailure
    ) {
        return new InventoryActionDispatchNode(request, onSuccess, onFailure);
    }
}
