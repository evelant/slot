package dev.imagio.slot.inventory.workspace;

/**
 * Content fingerprints for independently encoded workspace view slices.
 */
public record WorkspaceViewSliceKeys(
        String frame,
        String wall,
        String storage,
        String hotbar,
        String workflow,
        String panels,
        String contextual
) {
    public WorkspaceViewSliceKeys {
        frame = frame == null ? "" : frame;
        wall = wall == null ? "" : wall;
        storage = storage == null ? "" : storage;
        hotbar = hotbar == null ? "" : hotbar;
        workflow = workflow == null ? "" : workflow;
        panels = panels == null ? "" : panels;
        contextual = contextual == null ? "" : contextual;
    }

    public static WorkspaceViewSliceKeys from(SlotWorkspaceViewModel viewModel) {
        return WorkspaceProjectionFingerprint.sliceKeys(viewModel);
    }
}
