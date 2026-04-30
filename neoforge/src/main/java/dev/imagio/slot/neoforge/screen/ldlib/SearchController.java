package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.CARRIED_FIT_MAX_SCALE;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.CARRIED_FIT_MIN_SCALE;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.CARRIED_FIT_PADDING_PX;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.atlas.FitCarriedCamera;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class SearchController {
    private static final long SEARCH_PREVIEW_DELAY_MS = 220L;
    private static final long SEARCH_COMMIT_DELAY_MS = 3500L;

    private final SlotWorkspaceUiController host;

    private String searchQuery = "";
    private boolean searchModalActive;
    private String searchBuffer = "";
    private AtlasCamera searchOrigin;
    private long searchLastKeystrokeMs;
    private boolean searchPreviewPanned;
    private boolean searchInteractionDisablesAutoDismiss;
    private boolean searchCommitted;
    private List<AtlasSearchIndex.SearchRow> searchMatches = List.of();
    private int searchMatchIndex;

    SearchController(SlotWorkspaceUiController host) {
        this.host = host;
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
        return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
    }

    boolean matchesItem(SlotWorkspaceViewModel.AtlasItem item) {
        String query = normalizedQuery();
        if (query.isBlank()) {
            return true;
        }
        StringBuilder searchable = new StringBuilder();
        searchable.append(item.name().toLowerCase(Locale.ROOT)).append(' ')
                .append(item.identity().itemId().toLowerCase(Locale.ROOT)).append(' ');
        SlotWorkspaceViewModel.AtlasIsland island = host.viewModel.island(item.islandId());
        if (island != null) {
            searchable.append(island.label().toLowerCase(Locale.ROOT)).append(' ');
            searchable.append(island.kind().name().toLowerCase(Locale.ROOT)).append(' ');
        }
        return searchable.toString().contains(query);
    }

    /**
     * True when {@code summary} matches the active search query. Used by
     * the search-results panel to filter per-chest content listings.
     */
    boolean matchesContentSummary(SlotWorkspaceViewModel.ChestContentSummary summary) {
        String query = normalizedQuery();
        if (query.isBlank() || summary == null) {
            return false;
        }
        return (summary.name().toLowerCase(Locale.ROOT) + ' '
                + summary.itemId().toLowerCase(Locale.ROOT))
                .contains(query);
    }

    void handleCharTyped(UIEvent event) {
        char codePoint = event.codePoint;
        if (codePoint == '/' && !searchModalActive && !host.peekActive && !host.hotkeys.isTextInputFocused()) {
            event.stopPropagation();
            openModal();
            return;
        }
        if (!searchModalActive) {
            return;
        }
        if (codePoint == '/') {
            event.stopPropagation();
            if (searchInteractionDisablesAutoDismiss || !searchBuffer.isEmpty()) {
                closeModal();
                openModal();
            }
            return;
        }
        if (searchInteractionDisablesAutoDismiss) {
            // Locked in via Tab/Enter — further typing is ignored so the user
            // can browse results without clobbering their query.
            if (codePoint >= 0x20 && codePoint < 0x7F) {
                event.stopPropagation();
            }
            return;
        }
        if (codePoint >= '0' && codePoint <= '9') {
            return;
        }
        if (codePoint >= 0x20 && codePoint < 0x7F) {
            event.stopPropagation();
            appendBuffer(codePoint);
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
                commit(AtlasCameraController.CommitSource.SEARCH_ENTER, false);
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

    private void openModal() {
        if (!host.cameraController.hasGraphView()) {
            return;
        }
        searchModalActive = true;
        searchBuffer = "";
        searchOrigin = host.cameraController.currentCamera();
        searchLastKeystrokeMs = System.currentTimeMillis();
        searchPreviewPanned = false;
        searchInteractionDisablesAutoDismiss = false;
        searchCommitted = false;
        searchMatches = List.of();
        searchMatchIndex = 0;
        setSearchQuery("");
        setScreenClosesOnEsc(false);
        host.rebuild();
    }

    private void appendBuffer(char codePoint) {
        searchBuffer += codePoint;
        searchLastKeystrokeMs = System.currentTimeMillis();
        searchPreviewPanned = false;
        recomputeMatches();
        syncQuery();
        host.rebuild();
    }

    private void popBuffer() {
        if (searchBuffer.isEmpty()) {
            return;
        }
        searchBuffer = searchBuffer.substring(0, searchBuffer.length() - 1);
        searchLastKeystrokeMs = System.currentTimeMillis();
        searchPreviewPanned = false;
        recomputeMatches();
        syncQuery();
        host.rebuild();
    }

    private void cycleMatch() {
        if (searchMatches.isEmpty()) {
            return;
        }
        searchMatchIndex = (searchMatchIndex + 1) % searchMatches.size();
        searchPreviewPanned = true;
        searchLastKeystrokeMs = System.currentTimeMillis();
        searchInteractionDisablesAutoDismiss = true;
        easeToFitAllMatches();
        host.rebuild();
    }

    private void commit(AtlasCameraController.CommitSource source, boolean closeAfter) {
        if (searchMatches.isEmpty()) {
            if (closeAfter) {
                abort();
            }
            return;
        }
        // Search is a find-where tool: commit just freezes the current
        // fit-all view, leaves the active query in place so cards stay
        // highlighted, and closes the modal. No pan-to-current-match
        // zoom — players step through matches via Tab + visual scan.
        AtlasCamera origin = searchCommitted ? host.cameraController.currentCamera() : searchOrigin;
        AtlasCamera target = cameraForAllMatches();
        if (target != null) {
            host.cameraController.commitFrom(
                    origin,
                    target,
                    source,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.COMMIT_DURATION_MS);
            searchCommitted = true;
        }
        searchLastKeystrokeMs = System.currentTimeMillis();
        if (closeAfter) {
            closeModal();
            // Keep searchQuery set so cards stay highlighted; the player
            // dismisses the highlight by typing "/" again or Esc-ing
            // back from a fresh modal.
        }
        host.rebuild();
    }

    private void abort() {
        AtlasCamera origin = searchOrigin;
        boolean wasCommitted = searchCommitted;
        closeModal();
        if (!wasCommitted && origin != null) {
            host.cameraController.snap(origin);
        }
        setSearchQuery("");
        host.rebuild();
    }

    private void closeModal() {
        searchModalActive = false;
        searchBuffer = "";
        searchOrigin = null;
        searchMatches = List.of();
        searchMatchIndex = 0;
        searchInteractionDisablesAutoDismiss = false;
        searchCommitted = false;
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
        for (SlotWorkspaceViewModel.AtlasItem item : host.viewModel.atlasItems()) {
            dev.imagio.slot.atlas.lod.AtlasLayoutResult.ItemPlacement place = host.placementFor(item);
            rows.add(new AtlasSearchIndex.SearchRow(
                    item.name(),
                    item.identity().itemId(),
                    AtlasSearchIndex.Pool.PRIMARY,
                    item.carried(),
                    place.x(),
                    place.y(),
                    place.width(),
                    place.height()
            ));
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : host.viewModel.islands()) {
            dev.imagio.slot.atlas.lod.AtlasLayoutResult.IslandPlacement placement = host.islandPlacementFor(island);
            rows.add(new AtlasSearchIndex.SearchRow(
                    island.label(),
                    island.islandId(),
                    AtlasSearchIndex.Pool.SECONDARY,
                    island.carriedCount() > 0,
                    placement.x(),
                    placement.y(),
                    placement.width(),
                    placement.height()
            ));
        }
        return rows;
    }

    /**
     * Zoom the camera out to a single frame that fits every match's
     * placement rect. Replaces the old "pan to current match" behaviour:
     * search is now a "find-where" tool that reveals all matches at once
     * (highlighted via the existing per-card searchMatch overlay).
     */
    private void easeToFitAllMatches() {
        if (searchMatches.isEmpty()) {
            return;
        }
        AtlasCamera target = cameraForAllMatches();
        if (target != null) {
            host.cameraController.ease(
                    target,
                    AtlasCameraController.CUBIC_IN_OUT,
                    AtlasCameraController.SEARCH_PREVIEW_DURATION_MS);
        }
    }

    private AtlasCamera cameraForAllMatches() {
        SlotAtlasGraphView atlas = host.cameraController.graphView();
        if (atlas == null || searchMatches.isEmpty()) {
            return null;
        }
        float viewportWidth = atlas.getContentWidth();
        float viewportHeight = atlas.getContentHeight();
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            return null;
        }
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (AtlasSearchIndex.SearchRow row : searchMatches) {
            float x = row.targetX();
            float y = row.targetY();
            float w = row.targetWidth();
            float h = row.targetHeight();
            if (w <= 0f || h <= 0f) {
                continue;
            }
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x + w > maxX) maxX = x + w;
            if (y + h > maxY) maxY = y + h;
        }
        if (!Float.isFinite(minX) || !Float.isFinite(maxX) || maxX <= minX || maxY <= minY) {
            return null;
        }
        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                FitCarriedCamera.Rect.of(minX, minY, maxX - minX, maxY - minY),
                viewportWidth,
                viewportHeight,
                CARRIED_FIT_MIN_SCALE,
                CARRIED_FIT_MAX_SCALE,
                CARRIED_FIT_PADDING_PX
        );
        return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
    }

    private AtlasCamera cameraForMatch(AtlasSearchIndex.SearchRow row) {
        SlotAtlasGraphView atlas = host.cameraController.graphView();
        if (atlas == null || row == null) {
            return null;
        }
        float viewportWidth = atlas.getContentWidth();
        float viewportHeight = atlas.getContentHeight();
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            return null;
        }
        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                FitCarriedCamera.Rect.of(row.targetX(), row.targetY(), row.targetWidth(), row.targetHeight()),
                viewportWidth,
                viewportHeight,
                CARRIED_FIT_MIN_SCALE,
                CARRIED_FIT_MAX_SCALE,
                CARRIED_FIT_PADDING_PX
        );
        return camera == null ? null : new AtlasCamera(camera.offsetX(), camera.offsetY(), camera.scale());
    }

    private void syncQuery() {
        setSearchQuery(searchBuffer);
    }

    /**
     * Mirror the local query into the server session so its next view
     * projection can synthesize remote-only ghosts for the matching
     * identities. The dispatcher short-circuits when the value is
     * unchanged, so calling this on every keystroke is cheap.
     */
    private void setSearchQuery(String value) {
        String next = value == null ? "" : value;
        if (next.equals(searchQuery)) {
            return;
        }
        searchQuery = next;
        host.rpc.sendSearchQuery(searchQuery);
    }

    void tickIdleTimer() {
        if (!searchModalActive || searchMatches.isEmpty()) {
            return;
        }
        long idleMs = System.currentTimeMillis() - searchLastKeystrokeMs;
        if (!searchPreviewPanned && idleMs >= SEARCH_PREVIEW_DELAY_MS) {
            searchPreviewPanned = true;
            easeToFitAllMatches();
        }
        if (!searchInteractionDisablesAutoDismiss && idleMs >= SEARCH_COMMIT_DELAY_MS) {
            commit(AtlasCameraController.CommitSource.SEARCH_COMMIT, true);
        }
    }
}
