package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import net.minecraft.nbt.CompoundTag;

public final class ForgeWorkspaceViewModelClientCache {
    private static volatile WorkspaceActionEnvelope envelope;
    private static volatile SlotWorkspaceViewModel latest = SlotWorkspaceViewModel.empty();
    private static volatile CompoundTag latestTag;

    private ForgeWorkspaceViewModelClientCache() {
    }

    static void update(WorkspaceActionEnvelope nextEnvelope, SlotWorkspaceViewModel viewModel) {
        envelope = nextEnvelope;
        latest = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        latestTag = Forge120WorkspaceViewModelCodec.encode(latest);
    }

    static UpdateResult update(WorkspaceActionEnvelope nextEnvelope, CompoundTag transferTag) {
        Forge120WorkspaceViewModelCodec.TransferApplyResult applied =
                Forge120WorkspaceViewModelCodec.applyTransfer(latestTag, transferTag);
        if (!applied.applied()) {
            return new UpdateResult(false, applied.requiresFullSnapshot(), applied.diagnostics());
        }
        envelope = nextEnvelope;
        latest = applied.viewModel();
        latestTag = applied.fullTag();
        return new UpdateResult(true, false, "");
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
        latestTag = null;
    }

    public record UpdateResult(
            boolean applied,
            boolean requiresFullSnapshot,
            String diagnostics
    ) {
        public UpdateResult {
            diagnostics = diagnostics == null ? "" : diagnostics;
        }
    }
}
