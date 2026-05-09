package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

public final class WorkspaceGatherUiSupport {
    private WorkspaceGatherUiSupport() {
    }

    public static boolean isGatherableItem(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.presence().isEmpty()) {
            return false;
        }
        if (item.kitNeeded()) {
            return true;
        }
        return item.desiredCount() > 0 && item.totalCount() < item.desiredCount();
    }
}
