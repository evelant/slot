package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-local UI memory for ephemeral view state that should survive
 * closing and reopening the workspace within the same Minecraft client.
 */
public final class WorkspaceUiSessionMemory {
    private static final Map<String, State> STATES = new ConcurrentHashMap<>();
    private static final String STORAGE_GHOST_SECTION_KEY = "workspace.storageGhostSections";
    static final long SEARCH_CLOSE_CLEAR_MILLIS = 6_000L;

    private WorkspaceUiSessionMemory() {
    }

    public static String searchQuery(String surfaceKey) {
        return searchQuery(surfaceKey, System.currentTimeMillis());
    }

    static String searchQuery(String surfaceKey, long nowMillis) {
        State state = state(surfaceKey);
        clearExpiredSearchQuery(state, nowMillis);
        return state.searchQuery;
    }

    public static void setSearchQuery(String surfaceKey, String query) {
        State state = state(surfaceKey);
        state.searchQuery = query == null ? "" : WorkspaceSearchQuery.cleanInput(query);
        state.searchClosedAtMillis = 0L;
    }

    public static void markClosed(String surfaceKey) {
        markClosed(surfaceKey, System.currentTimeMillis());
    }

    static void markClosed(String surfaceKey, long nowMillis) {
        state(surfaceKey).searchClosedAtMillis = nowMillis;
    }

    public static float wallScroll(String surfaceKey) {
        return state(surfaceKey).wallScroll;
    }

    public static void setWallScroll(String surfaceKey, float wallScroll) {
        state(surfaceKey).wallScroll = Float.isFinite(wallScroll) ? Math.max(0f, wallScroll) : 0f;
    }

    public static boolean storageGhostSectionExpanded(String surfaceKey, String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return false;
        }
        return state(STORAGE_GHOST_SECTION_KEY).expandedStorageGhostSections.contains(islandId);
    }

    public static boolean toggleStorageGhostSection(String surfaceKey, String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return false;
        }
        State state = state(STORAGE_GHOST_SECTION_KEY);
        if (state.expandedStorageGhostSections.contains(islandId)) {
            state.expandedStorageGhostSections.remove(islandId);
            return false;
        }
        state.expandedStorageGhostSections.add(islandId);
        return true;
    }

    private static State state(String surfaceKey) {
        String key = surfaceKey == null || surfaceKey.isBlank() ? "default" : surfaceKey;
        return STATES.computeIfAbsent(key, ignored -> new State());
    }

    private static void clearExpiredSearchQuery(State state, long nowMillis) {
        if (state == null || state.searchQuery.isBlank() || state.searchClosedAtMillis <= 0L) {
            return;
        }
        if (nowMillis - state.searchClosedAtMillis >= SEARCH_CLOSE_CLEAR_MILLIS) {
            state.searchQuery = "";
            state.searchClosedAtMillis = 0L;
        }
    }

    private static final class State {
        private String searchQuery = "";
        private long searchClosedAtMillis;
        private float wallScroll;
        private final Set<String> expandedStorageGhostSections = new LinkedHashSet<>();
    }
}
