package dev.imagio.slot.session;

public enum InventoryPaneMembership {
    CARRIED,
    EXTERNAL,
    HIDDEN,
    TOOL_ONLY;

    public boolean visibleToUser() {
        return this != HIDDEN && this != TOOL_ONLY;
    }

    public boolean carried() {
        return this == CARRIED;
    }

    public boolean external() {
        return this == EXTERNAL;
    }
}
