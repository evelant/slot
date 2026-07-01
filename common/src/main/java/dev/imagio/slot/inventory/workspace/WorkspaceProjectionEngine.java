package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

public final class WorkspaceProjectionEngine {
    public SlotWorkspaceViewModel project(
            WorkspaceProjectionRequest request,
            ItemIdentityMatcher.Memo identityMemo
    ) {
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        return ItemIdentityMatcher.withMemo(identityMemo, () -> SlotWorkspaceViewModel.project(
                resolved.authority(),
                resolved.workflow(),
                resolved.status(),
                resolved.diagnostics(),
                resolved.pendingCount(),
                resolved.selectedQuickAccessSlot(),
                resolved.revision(),
                resolved.learnedRules(),
                resolved.signalExtractor(),
                resolved.chestContentsResolver(),
                resolved.proximateStorageIds(),
                resolved.carriedContainerInfoResolver(),
                resolved.lootChestSource(),
                resolved.searchQuery(),
                resolved.currentTick(),
                resolved.activeChestPanel(),
                resolved.worldDisplaySources(),
                resolved.contextualSuggestionStorageIds(),
                resolved.contextualSuggestionDisplaySources(),
                resolved.trackedDisplayStorageEntries(),
                resolved.depositEligibleStorageIds(),
                resolved.liveChestContentPresence(),
                resolved.liveStorageAffinityEligibility(),
                resolved.remoteStorageDetailIntent()));
    }
}
