package dev.imagio.slot.inventory.storage;

import java.util.Locale;

/**
 * Small set of non-container world displays that SLOT can browse as storage.
 * Live discovery is proximate; tracked kinds also feed the remembered storage
 * read model so SLOT can display them like chest contents.
 */
public enum WorldDisplayStorageKind {
    TOOL_RACK("tool_rack", true, true),
    PLACED_ITEM("placed_item", false, true),
    AE2_TERMINAL("ae2_terminal", true, false),
    AE2_NETWORK("ae2_network", true, true);

    private final String key;
    private final boolean depositTarget;
    private final boolean trackedStorage;

    WorldDisplayStorageKind(String key, boolean depositTarget, boolean trackedStorage) {
        this.key = key;
        this.depositTarget = depositTarget;
        this.trackedStorage = trackedStorage;
    }

    public String key() {
        return key;
    }

    /**
     * True when normal player deposit routing may choose this display.
     * Non-deposit displays may still accept insert internally for rollback
     * after a failed take.
     */
    public boolean depositTarget() {
        return depositTarget;
    }

    /**
     * True when the display block participates in SLOT's tracked storage
     * read model. Mutation capability stays separate: placed-item blocks can
     * be tracked for display while remaining non-deposit targets.
     */
    public boolean trackedStorage() {
        return trackedStorage;
    }

    public static WorldDisplayStorageKind fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (WorldDisplayStorageKind kind : values()) {
            if (kind.key.equals(normalized)) {
                return kind;
            }
        }
        return null;
    }
}
