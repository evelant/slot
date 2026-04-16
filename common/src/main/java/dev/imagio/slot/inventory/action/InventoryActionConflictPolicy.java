package dev.imagio.slot.inventory.action;

public enum InventoryActionConflictPolicy {
    DEFAULT,
    INSERT_ONLY,
    ASSIGN_WITH_DISPLACE,
    SWAP_EXACT,
    REPLACE_AND_STAGE,
    REJECT_IF_OCCUPIED
}
