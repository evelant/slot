package dev.imagio.slot.inventory.storage;

import java.util.Locale;

/**
 * Small set of non-container world displays that SLOT can browse as storage.
 * These are discovered live from nearby blocks and are never persisted as
 * claimed storage.
 */
public enum WorldDisplayStorageKind {
    TOOL_RACK("tool_rack", true),
    PLACED_ITEM("placed_item", false);

    private final String key;
    private final boolean depositTarget;

    WorldDisplayStorageKind(String key, boolean depositTarget) {
        this.key = key;
        this.depositTarget = depositTarget;
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
