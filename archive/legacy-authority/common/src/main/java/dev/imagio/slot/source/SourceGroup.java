package dev.imagio.slot.source;

public enum SourceGroup {
    PLAYER_MAIN,
    PLAYER_HOTBAR,
    CARRIED,
    EXTERNAL,
    EQUIPMENT,
    TOOL,
    VIRTUAL;

    public boolean carried() {
        return this == PLAYER_MAIN || this == PLAYER_HOTBAR || this == CARRIED || this == EQUIPMENT;
    }

    public boolean external() {
        return this == EXTERNAL;
    }
}
