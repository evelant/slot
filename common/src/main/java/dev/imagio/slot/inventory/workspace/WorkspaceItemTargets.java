package dev.imagio.slot.inventory.workspace;

/**
 * The target counts attached to one atlas item, kept as separate concepts.
 *
 * <p>Desired counts and wanted counts often produce the same guidance
 * behavior, but they do not mean the same thing. UI code should use this type
 * when it intentionally needs a combined display target; domain logic should
 * continue reading the specific desired or wanted source it owns.
 */
public record WorkspaceItemTargets(
        int desiredCount,
        boolean desiredCountFromKit,
        int wantedCount
) {
    public WorkspaceItemTargets {
        desiredCount = Math.max(0, desiredCount);
        desiredCountFromKit = desiredCount > 0 && desiredCountFromKit;
        wantedCount = Math.max(0, wantedCount);
    }

    public static WorkspaceItemTargets from(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return empty();
        }
        return new WorkspaceItemTargets(
                item.desiredCount(),
                item.desiredCountFromKit(),
                item.wantedCount()
        );
    }

    public static WorkspaceItemTargets empty() {
        return new WorkspaceItemTargets(0, false, 0);
    }

    public int displayTargetCount() {
        return Math.max(desiredCount, wantedCount);
    }

    public boolean hasDesiredGap(int carriedCount) {
        return desiredCount > 0 && Math.max(0, carriedCount) < desiredCount;
    }

    public boolean hasWantedGap(int carriedCount) {
        return wantedCount > 0 && Math.max(0, carriedCount) < wantedCount;
    }

    public boolean hasAnyGap(int carriedCount) {
        return hasDesiredGap(carriedCount) || hasWantedGap(carriedCount);
    }
}
