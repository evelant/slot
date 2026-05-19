package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceSearchQuery;
import dev.imagio.slot.ui.workspace.WorkspaceSearchInputPolicy;
import dev.imagio.slot.ui.workspace.WorkspaceUiSessionMemory;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Search controller for the sectioned list-view wall. In list-view
 * mode, search is a <em>filter</em>: the wall hides non-matching cards
 * during an active query. There is no camera to pan, so the prior
 * "preview pan / commit zoom / idle dismiss" logic is gone. The
 * controller exposes the buffer, query, and current match list to the
 * wall and the chest-locator panel.
 */
final class SearchController {
    private static final long AUTO_COMMIT_MILLIS = 2_000L;

    private final SlotWorkspaceUiController host;

    private String searchQuery = "";
    private boolean searchModalActive;
    private String searchBuffer = "";
    private boolean searchInteractionDisablesAutoDismiss;
    private List<AtlasSearchIndex.SearchRow> searchMatches = List.of();
    private int searchMatchIndex;
    private long lastSearchInputMillis;

    SearchController(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void restoreRememberedQuery() {
        this.searchQuery = WorkspaceUiSessionMemory.searchQuery(host.surfaceMemoryKey());
    }

    boolean modalActive() {
        return searchModalActive;
    }

    String buffer() {
        return searchBuffer;
    }

    List<AtlasSearchIndex.SearchRow> matches() {
        return searchMatches;
    }

    int matchIndex() {
        return searchMatchIndex;
    }

    boolean interactionDisablesAutoDismiss() {
        return searchInteractionDisablesAutoDismiss;
    }

    String normalizedQuery() {
        if (host.recipeSidebarActive()) {
            return "";
        }
        return WorkspaceSearchQuery.normalized(searchQuery);
    }

    boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
        if (host.recipeSidebarActive()) {
            return true;
        }
        return WorkspaceSearchQuery.matchesItem(
                searchQuery,
                item,
                item == null ? null : host.currentIsland(item.islandId()));
    }

    boolean matchesContentSummary(SlotWorkspaceViewModel.ChestContentSummary summary) {
        return WorkspaceSearchQuery.matchesContentSummary(searchQuery, summary);
    }

    void handleCharTyped(UIEvent event) {
        char codePoint = event.codePoint;
        WorkspaceSearchInputPolicy.Decision decision = WorkspaceSearchInputPolicy.charTyped(
                searchModalActive,
                searchBuffer,
                codePoint,
                host.hotkeys.isTextInputFocused());
        if (decision.handled()
                && decision.action() == WorkspaceSearchInputPolicy.Action.OPEN
                && !searchModalActive) {
            event.stopPropagation();
            openModal();
            return;
        }
        if (!searchModalActive) {
            return;
        }
        if (decision.handled() && decision.action() == WorkspaceSearchInputPolicy.Action.OPEN) {
            event.stopPropagation();
            if (searchInteractionDisablesAutoDismiss || !searchBuffer.isEmpty()) {
                closeModal();
                openModal();
            }
            return;
        }
        if (searchInteractionDisablesAutoDismiss) {
            if (codePoint >= 0x20 && codePoint < 0x7F) {
                event.stopPropagation();
            }
            return;
        }
        if (decision.handled() && decision.action() == WorkspaceSearchInputPolicy.Action.IGNORE_DIGIT) {
            event.stopPropagation();
            return;
        }
        if (decision.handled() && decision.action() == WorkspaceSearchInputPolicy.Action.APPEND) {
            event.stopPropagation();
            setBuffer(decision.query());
        }
    }

    void handleKeyDown(UIEvent event) {
        if (event.keyCode == GLFW.GLFW_KEY_BACKSLASH) {
            WorkspaceSearchInputPolicy.Decision decision = WorkspaceSearchInputPolicy.keyPressed(
                    searchModalActive,
                    searchModalActive ? searchBuffer : searchQuery,
                    WorkspaceSearchInputPolicy.ControlKey.CLEAR);
            if (decision.handled()) {
                event.stopPropagation();
                clearSearch();
            }
            return;
        }
        if (!searchModalActive) {
            return;
        }
        switch (event.keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                event.stopPropagation();
                searchInteractionDisablesAutoDismiss = true;
                closeModal();
                host.rebuild();
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                event.stopPropagation();
                if (!searchInteractionDisablesAutoDismiss) {
                    popBuffer();
                }
            }
            case GLFW.GLFW_KEY_TAB -> {
                event.stopPropagation();
                cycleMatch();
            }
            default -> {
                if (isSearchTypingKey(event.keyCode)) {
                    event.stopPropagation();
                }
            }
        }
    }

    void confirmForHotbar() {
        if (!searchModalActive) {
            return;
        }
        searchInteractionDisablesAutoDismiss = true;
        closeModal();
        host.rebuild();
    }

    void syncRememberedQuery() {
        if (!searchQuery.isBlank()) {
            host.rpc.sendSearchQuery(searchQuery);
        }
    }

    private void openModal() {
        searchModalActive = true;
        searchBuffer = "";
        searchInteractionDisablesAutoDismiss = false;
        searchMatches = List.of();
        searchMatchIndex = 0;
        touchSearchInput();
        setSearchQuery("");
        setScreenClosesOnEsc(true);
        host.rebuild();
    }

    private void appendBuffer(char codePoint) {
        searchBuffer += codePoint;
        recomputeMatches();
        syncQuery();
        host.rebuild();
    }

    private void setBuffer(String value) {
        String next = WorkspaceSearchQuery.cleanInput(value);
        if (next.equals(searchBuffer)) {
            return;
        }
        searchBuffer = next;
        touchSearchInput();
        recomputeMatches();
        syncQuery();
        host.rebuild();
    }

    private void popBuffer() {
        if (searchBuffer.isEmpty()) {
            return;
        }
        searchBuffer = searchBuffer.substring(0, searchBuffer.length() - 1);
        touchSearchInput();
        recomputeMatches();
        syncQuery();
        host.rebuild();
    }

    private void cycleMatch() {
        if (searchMatches.isEmpty()) {
            return;
        }
        searchMatchIndex = (searchMatchIndex + 1) % searchMatches.size();
        searchInteractionDisablesAutoDismiss = true;
        touchSearchInput();
        host.rebuild();
    }

    void clearSearch() {
        closeModal();
        setSearchQuery("");
        host.rebuild();
    }

    private void closeModal() {
        searchModalActive = false;
        searchBuffer = "";
        searchMatches = List.of();
        searchMatchIndex = 0;
        searchInteractionDisablesAutoDismiss = false;
        setScreenClosesOnEsc(true);
    }

    private void setScreenClosesOnEsc(boolean enabled) {
        ModularUI mui = host.root.getModularUI();
        if (mui != null) {
            mui.shouldCloseOnEsc(enabled);
        }
    }

    private void recomputeMatches() {
        if (searchBuffer.isEmpty()) {
            searchMatches = List.of();
            searchMatchIndex = 0;
            return;
        }
        searchMatches = AtlasSearchIndex.search(collectRows(), searchBuffer);
        searchMatchIndex = 0;
    }

    private List<AtlasSearchIndex.SearchRow> collectRows() {
        ArrayList<AtlasSearchIndex.SearchRow> rows = new ArrayList<>();
        for (SlotWorkspaceViewModel.AtlasItem item : host.currentAtlasItems()) {
            rows.add(new AtlasSearchIndex.SearchRow(
                    searchLabel(item),
                    item.identity().itemId(),
                    AtlasSearchIndex.Pool.PRIMARY,
                    item.carried(),
                    0, 0, 0, 0
            ));
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : host.currentIslands()) {
            rows.add(new AtlasSearchIndex.SearchRow(
                    island.label(),
                    island.islandId(),
                    AtlasSearchIndex.Pool.SECONDARY,
                    island.carriedCount() > 0,
                    0, 0, 0, 0
            ));
        }
        return rows;
    }

    private static String searchLabel(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return "";
        }
        String displayName = item.displayStack().isEmpty() ? "" : item.displayStack().getHoverName().getString();
        return displayName == null || displayName.isBlank() ? item.name() : displayName;
    }

    private void syncQuery() {
        setSearchQuery(searchBuffer);
    }

    /**
     * Mirror the local query into the server session so its next view
     * projection can synthesize remote-only ghosts for the matching
     * identities.
     */
    private void setSearchQuery(String value) {
        String next = WorkspaceSearchQuery.cleanInput(value == null ? "" : value);
        if (next.equals(searchQuery)) {
            WorkspaceUiSessionMemory.setSearchQuery(host.surfaceMemoryKey(), next);
            return;
        }
        searchQuery = next;
        WorkspaceUiSessionMemory.setSearchQuery(host.surfaceMemoryKey(), searchQuery);
        host.rpc.sendSearchQuery(searchQuery);
    }

    private static boolean isSearchTypingKey(int keyCode) {
        if ((keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9)
                || (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)) {
            return false;
        }
        return keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_WORLD_2;
    }

    void tickIdleTimer() {
        if (!searchModalActive || System.currentTimeMillis() - lastSearchInputMillis < AUTO_COMMIT_MILLIS) {
            return;
        }
        closeModal();
        host.rebuild();
    }

    private void touchSearchInput() {
        lastSearchInputMillis = System.currentTimeMillis();
    }
}
