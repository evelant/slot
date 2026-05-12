package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

/**
 * UI-local cache for goal projection. A projection walks the captured EMI
 * recipe graph and current authority, so render/tooltip paths must not rebuild
 * it repeatedly while the underlying view state is unchanged.
 */
public final class GoalWorkspaceProjectionCache {
    private long viewRevision = Long.MIN_VALUE;
    private int goalRevision = Integer.MIN_VALUE;
    private String goalId = "";
    private GoalWorkspaceProjection projection;

    public GoalWorkspaceProjection get(SlotWorkspaceViewModel viewModel) {
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        if (active == null) {
            clear();
            return null;
        }
        SlotWorkspaceViewModel source = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        long nextViewRevision = source.revision();
        int nextGoalRevision = GoalWorkspaceClientState.revision();
        String nextGoalId = active.goalId();
        if (projection != null
                && viewRevision == nextViewRevision
                && goalRevision == nextGoalRevision
                && goalId.equals(nextGoalId)) {
            return projection;
        }
        GoalWorkspaceProjection next = GoalWorkspaceProjection.fromGoal(source, active);
        viewRevision = nextViewRevision;
        goalRevision = nextGoalRevision;
        goalId = nextGoalId;
        projection = next;
        return projection;
    }

    public void invalidate() {
        clear();
    }

    private void clear() {
        viewRevision = Long.MIN_VALUE;
        goalRevision = Integer.MIN_VALUE;
        goalId = "";
        projection = null;
    }
}
