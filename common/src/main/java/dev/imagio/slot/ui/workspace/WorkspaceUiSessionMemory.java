package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-local UI memory for ephemeral view state that should survive
 * closing and reopening the workspace within the same Minecraft client.
 */
public final class WorkspaceUiSessionMemory {
    private static final Map<String, State> STATES = new ConcurrentHashMap<>();

    private WorkspaceUiSessionMemory() {
    }

    public static String searchQuery(String surfaceKey) {
        return state(surfaceKey).searchQuery;
    }

    public static void setSearchQuery(String surfaceKey, String query) {
        state(surfaceKey).searchQuery = query == null ? "" : WorkspaceSearchQuery.cleanInput(query);
    }

    public static float wallScroll(String surfaceKey) {
        return state(surfaceKey).wallScroll;
    }

    public static void setWallScroll(String surfaceKey, float wallScroll) {
        state(surfaceKey).wallScroll = Float.isFinite(wallScroll) ? Math.max(0f, wallScroll) : 0f;
    }

    private static State state(String surfaceKey) {
        String key = surfaceKey == null || surfaceKey.isBlank() ? "default" : surfaceKey;
        return STATES.computeIfAbsent(key, ignored -> new State());
    }

    private static final class State {
        private String searchQuery = "";
        private float wallScroll;
    }
}
