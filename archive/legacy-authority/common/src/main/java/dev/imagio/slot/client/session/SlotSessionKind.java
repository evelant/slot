package dev.imagio.slot.client.session;

public enum SlotSessionKind {
    GENERAL(true),
    PLAYER_INVENTORY(false),
    SLOT_WORKSPACE(true),
    SLOT_CARRIED(false),
    EXTERNAL_CONTAINER(true),
    CARRIED_CONTAINER(false),
    NON_STORAGE_CONTAINER(true);

    private final boolean recordsRecentLoot;

    SlotSessionKind(boolean recordsRecentLoot) {
        this.recordsRecentLoot = recordsRecentLoot;
    }

    public boolean recordsRecentLoot() {
        return recordsRecentLoot;
    }
}
