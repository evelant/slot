package dev.imagio.slot.workflow.domain;

/**
 * Player-authored participation mode for a claimed storage block.
 */
public enum ChestRole {
    STORAGE,
    INPUT,
    OUTPUT,
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

    public int takePriority() {
        return switch (this) {
            case OUTPUT -> 300;
            case STORAGE -> 200;
            case INPUT -> 100;
            case IGNORE -> 0;
        };
    }

    public ChestRole next() {
        return switch (this) {
            case STORAGE -> INPUT;
            case INPUT -> OUTPUT;
            case OUTPUT -> IGNORE;
            case IGNORE -> STORAGE;
        };
    }

    public String displayLabel() {
        return switch (this) {
            case STORAGE -> "Storage";
            case INPUT -> "Input";
            case OUTPUT -> "Output";
            case IGNORE -> "Ignore";
        };
    }

    public static ChestRole parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return IGNORE;
        }
        String normalized = raw.trim();
        if ("BUFFER".equalsIgnoreCase(normalized)) {
            return INPUT;
        }
        for (ChestRole role : values()) {
            if (role.name().equalsIgnoreCase(normalized) || role.displayLabel().equalsIgnoreCase(normalized)) {
                return role;
            }
        }
        return IGNORE;
    }
}
