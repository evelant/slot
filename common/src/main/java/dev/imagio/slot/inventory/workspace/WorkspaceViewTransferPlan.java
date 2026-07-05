package dev.imagio.slot.inventory.workspace;

import java.util.EnumSet;

public record WorkspaceViewTransferPlan(
        WorkspaceViewTransferMode mode,
        long baseRevision,
        long revision,
        EnumSet<WorkspaceViewTransferSlice> slices
) {
    public WorkspaceViewTransferPlan {
        mode = mode == null ? WorkspaceViewTransferMode.FULL_SNAPSHOT : mode;
        baseRevision = Math.max(0L, baseRevision);
        revision = Math.max(0L, revision);
        slices = slices == null || slices.isEmpty()
                ? EnumSet.noneOf(WorkspaceViewTransferSlice.class)
                : EnumSet.copyOf(slices);
        if (mode == WorkspaceViewTransferMode.FULL_SNAPSHOT) {
            slices = EnumSet.allOf(WorkspaceViewTransferSlice.class);
            baseRevision = 0L;
        }
    }

    public static WorkspaceViewTransferPlan full(long revision) {
        return new WorkspaceViewTransferPlan(
                WorkspaceViewTransferMode.FULL_SNAPSHOT,
                0L,
                revision,
                EnumSet.allOf(WorkspaceViewTransferSlice.class));
    }

    public static WorkspaceViewTransferPlan from(
            long baseRevision,
            WorkspaceViewSliceKeys previous,
            long revision,
            WorkspaceViewSliceKeys current,
            boolean forceFull
    ) {
        if (forceFull || previous == null || current == null || baseRevision <= 0L) {
            return full(revision);
        }
        EnumSet<WorkspaceViewTransferSlice> changed = EnumSet.noneOf(WorkspaceViewTransferSlice.class);
        addIfChanged(changed, WorkspaceViewTransferSlice.FRAME, previous.frame(), current.frame());
        addIfChanged(changed, WorkspaceViewTransferSlice.WALL, previous.wall(), current.wall());
        addIfChanged(changed, WorkspaceViewTransferSlice.STORAGE, previous.storage(), current.storage());
        addIfChanged(changed, WorkspaceViewTransferSlice.HOTBAR, previous.hotbar(), current.hotbar());
        addIfChanged(changed, WorkspaceViewTransferSlice.WORKFLOW, previous.workflow(), current.workflow());
        addIfChanged(changed, WorkspaceViewTransferSlice.PANELS, previous.panels(), current.panels());
        addIfChanged(changed, WorkspaceViewTransferSlice.CONTEXTUAL, previous.contextual(), current.contextual());
        return new WorkspaceViewTransferPlan(
                WorkspaceViewTransferMode.DELTA,
                baseRevision,
                revision,
                changed);
    }

    private static void addIfChanged(
            EnumSet<WorkspaceViewTransferSlice> slices,
            WorkspaceViewTransferSlice slice,
            String previous,
            String current
    ) {
        if (slices != null && slice != null && !safe(previous).equals(safe(current))) {
            slices.add(slice);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
