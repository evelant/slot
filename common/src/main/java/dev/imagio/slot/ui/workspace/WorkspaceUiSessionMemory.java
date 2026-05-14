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

    private static final class State {
        private String searchQuery = "";
        private float wallScroll;
        private final Set<String> expandedStorageGhostSections = new LinkedHashSet<>();
    }
}
