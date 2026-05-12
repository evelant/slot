package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceItemTargets;

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
        int carried = item.carried() ? Math.max(0, item.totalCount()) : 0;
        return WorkspaceItemTargets.from(item).hasAnyGap(carried);
    }
}
