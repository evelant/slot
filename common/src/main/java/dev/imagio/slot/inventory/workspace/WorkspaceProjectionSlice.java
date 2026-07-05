package dev.imagio.slot.inventory.workspace;

import java.util.EnumSet;

/**
 * Coarse read-model slices used by workspace projection invalidation and
 * diagnostics. These names describe common projection dependencies, not wire
 * message chunks; loader encoders can continue to use their existing format.
 */
public enum WorkspaceProjectionSlice {
    CARD,
    SECTION,
    STORAGE,
    WAYFINDING,
    DEPOSITABILITY,
    HOTBAR,
    WORKFLOW,
    PANEL,
    CONTEXTUAL,
    FRAME,
    REMOTE_SEARCH;

    public static EnumSet<WorkspaceProjectionSlice> all() {
        return EnumSet.allOf(WorkspaceProjectionSlice.class);
    }

    public static EnumSet<WorkspaceProjectionSlice> none() {
        return EnumSet.noneOf(WorkspaceProjectionSlice.class);
    }
}
