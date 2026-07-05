package dev.imagio.slot.inventory.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceViewTransferPlanTest {
    @Test
    void missingBaseForcesFullSnapshot() {
        WorkspaceViewTransferPlan plan = WorkspaceViewTransferPlan.from(
                0L,
                null,
                1L,
                keys("a", "b", "c", "d", "e", "f", "g"),
                false);

        assertEquals(WorkspaceViewTransferMode.FULL_SNAPSHOT, plan.mode());
        assertEquals(7, plan.slices().size());
    }

    @Test
    void changedKeysSelectOnlyChangedSlices() {
        WorkspaceViewSliceKeys previous = keys("frame-a", "wall-a", "storage-a", "hotbar-a", "workflow-a", "panels-a", "context-a");
        WorkspaceViewSliceKeys current = keys("frame-b", "wall-a", "storage-b", "hotbar-a", "workflow-a", "panels-a", "context-a");

        WorkspaceViewTransferPlan plan = WorkspaceViewTransferPlan.from(4L, previous, 5L, current, false);

        assertEquals(WorkspaceViewTransferMode.DELTA, plan.mode());
        assertEquals(4L, plan.baseRevision());
        assertEquals(5L, plan.revision());
        assertEquals(2, plan.slices().size());
        assertTrue(plan.slices().contains(WorkspaceViewTransferSlice.FRAME));
        assertTrue(plan.slices().contains(WorkspaceViewTransferSlice.STORAGE));
    }

    private static WorkspaceViewSliceKeys keys(
            String frame,
            String wall,
            String storage,
            String hotbar,
            String workflow,
            String panels,
            String contextual
    ) {
        return new WorkspaceViewSliceKeys(frame, wall, storage, hotbar, workflow, panels, contextual);
    }
}
