package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

/**
 * Client-side cache of the latest pushed workspace view-model. Lets
 * client UI not currently rendering the SLOT atlas (e.g. the claim
 * button on a vanilla chest GUI) read the current storage-area state
 * without round-tripping a request to the server.
 *
 * <p>Updated by {@code SlotWorkspaceUiController} every time the
 * server pushes a new view tag. Empty for new players who have never
 * opened the SLOT workspace; consumers must handle that gracefully
 * (typically by offering to create a fresh area).
 */
public final class SlotClientWorkspaceCache {
    private static volatile SlotWorkspaceViewModel latest = SlotWorkspaceViewModel.empty();

    private SlotClientWorkspaceCache() {
    }

    public static void update(SlotWorkspaceViewModel viewModel) {
        if (viewModel != null) {
            latest = viewModel;
        }
    }

    public static SlotWorkspaceViewModel latest() {
        return latest;
    }

    public static void clear() {
        latest = SlotWorkspaceViewModel.empty();
    }
}
