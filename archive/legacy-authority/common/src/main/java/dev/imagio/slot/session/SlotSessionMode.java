package dev.imagio.slot.session;

public enum SlotSessionMode {
    GENERAL,
    PLAYER_INVENTORY,
    SLOT_WORKSPACE,
    SLOT_CARRIED,
    DUAL_PANE,
    CARRIED_ONLY,
    NON_STORAGE,
    UNSUPPORTED;

    public boolean recordsRecentLoot() {
        return switch (this) {
            case GENERAL, SLOT_WORKSPACE, DUAL_PANE, NON_STORAGE -> true;
            default -> false;
        };
    }

    public boolean slotOwned() {
        return this == SLOT_CARRIED || this == SLOT_WORKSPACE;
    }

    public boolean carriedOnly() {
        return this == PLAYER_INVENTORY || this == SLOT_CARRIED || this == CARRIED_ONLY;
    }
}
