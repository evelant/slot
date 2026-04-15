package dev.imagio.slot.inventory.integration;

public enum InventorySlotOwnershipPosture {
    UNKNOWN,
    SLOT_OWNED,
    HYBRID,
    PROVIDER_BACKED;

    public boolean slotOwned() {
        return this == SLOT_OWNED;
    }
}
