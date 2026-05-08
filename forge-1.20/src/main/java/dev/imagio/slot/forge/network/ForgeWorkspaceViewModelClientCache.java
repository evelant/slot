package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;

public final class ForgeWorkspaceViewModelClientCache {
    private static volatile WorkspaceActionEnvelope envelope;
    private static volatile SlotWorkspaceViewModel latest = SlotWorkspaceViewModel.empty();

    private ForgeWorkspaceViewModelClientCache() {
    }

    static void update(WorkspaceActionEnvelope nextEnvelope, SlotWorkspaceViewModel viewModel) {
        envelope = nextEnvelope;
        latest = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
    }

    public static SlotWorkspaceViewModel latestFor(String sessionId) {
        WorkspaceActionEnvelope currentEnvelope = envelope;
        if (currentEnvelope == null || sessionId == null || !sessionId.equals(currentEnvelope.sessionId())) {
            return null;
        }
        return latest;
    }

    public static SlotWorkspaceViewModel latest() {
        return latest;
    }

    public static void clear() {
        envelope = null;
        latest = SlotWorkspaceViewModel.empty();
    }
}
