package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

public final class WorkspaceProjectionEngine {
    private WorkspaceProjectionStore lastStore = WorkspaceProjectionStore.empty();

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
        return ItemIdentityMatcher.withMemo(identityMemo, () -> {
            WorkspaceProjectionStore store = WorkspaceProjectionStore.from(resolved);
            lastStore = store;
            return SlotWorkspaceViewModel.project(resolved, store);
        });
    }

    WorkspaceProjectionStore lastStore() {
        return lastStore;
    }
}
