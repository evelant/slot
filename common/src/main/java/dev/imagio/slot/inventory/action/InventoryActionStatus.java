package dev.imagio.slot.inventory.action;

public enum InventoryActionStatus {
    SUCCESS,
    PARTIAL,
    BLOCKED,
    FAILED;

    public boolean successful() {
        return this == SUCCESS || this == PARTIAL;
    }
}
