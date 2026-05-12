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
    private final SlotWorkspaceUiController host;

    private String searchQuery = "";
    private boolean searchModalActive;
    private String searchBuffer = "";
    private boolean searchInteractionDisablesAutoDismiss;
    private List<AtlasSearchIndex.SearchRow> searchMatches = List.of();
    private int searchMatchIndex;

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
        return WorkspaceSearchQuery.normalized(searchQuery);
    }

    boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
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
        if (!searchModalActive) {
            return;
        }
        switch (event.keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                event.stopPropagation();
                abort();
            }
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
        setSearchQuery("");
        setScreenClosesOnEsc(false);
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
        recomputeMatches();
        syncQuery();
        host.rebuild();
    }

    private void popBuffer() {
        if (searchBuffer.isEmpty()) {
            return;
        }
        searchBuffer = searchBuffer.substring(0, searchBuffer.length() - 1);
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
        host.rebuild();
    }

    private void abort() {
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
                    item.name(),
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

    /**
     * No-op kept for caller compatibility; the prior implementation
     * panned the camera on idle. List-view mode has no camera.
     */
    void tickIdleTimer() {
    }
}
