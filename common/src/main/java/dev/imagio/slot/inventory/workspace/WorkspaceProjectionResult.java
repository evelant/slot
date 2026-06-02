package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

public record WorkspaceProjectionResult(
        SlotWorkspaceViewModel viewModel,
        String contentFingerprint,
        WorkspaceProjectionSessionCache.Diagnostics diagnostics
) {
    public WorkspaceProjectionResult {
        viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        contentFingerprint = contentFingerprint == null ? "" : contentFingerprint;
        diagnostics = diagnostics == null ? WorkspaceProjectionSessionCache.Diagnostics.empty() : diagnostics;
    }

    public boolean structuralCacheHit() {
        return diagnostics.structuralCacheHit();
    }

    public ItemIdentityMatcher.MemoStats identityMemoStats() {
        return diagnostics.identityMemoStats();
    }
}
