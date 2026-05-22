package dev.imagio.slot.workflow.domain;

/**
 * Player-authored participation mode for a claimed storage block.
 */
public enum ChestRole {
    STORAGE,
    BUFFER,
    IGNORE;

    public boolean visibleToWorkspace() {
        return this != IGNORE;
    }

    public boolean learnsAffinity() {
        return this == STORAGE;
    }

    public boolean quickDepositTarget() {
        return this == STORAGE;
    }

    public ChestRole next() {
        return switch (this) {
            case STORAGE -> BUFFER;
            case BUFFER -> IGNORE;
            case IGNORE -> STORAGE;
        };
    }

    public String displayLabel() {
        return switch (this) {
            case STORAGE -> "Storage";
            case BUFFER -> "Buffer";
            case IGNORE -> "Ignore";
        };
    }

    public static ChestRole parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return STORAGE;
        }
        for (ChestRole role : values()) {
            if (role.name().equalsIgnoreCase(raw) || role.displayLabel().equalsIgnoreCase(raw)) {
                return role;
            }
        }
        return STORAGE;
    }
}
