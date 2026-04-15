package dev.imagio.slot.inventory.integration;

public enum InventoryHostFamilyHint {
    UNKNOWN,
    CARRIED_ONLY,
    DUAL_PANE,
    TERMINAL_HYBRID;

    public boolean terminalLike() {
        return this == TERMINAL_HYBRID;
    }
}
