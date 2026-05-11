package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class GoalWorkspaceClientState {
    private static final LinkedHashMap<String, MutableGoalTab> GOALS = new LinkedHashMap<>();
    private static String activeGoalId = "";
    private static int revision;

    private GoalWorkspaceClientState() {
    }

    public static synchronized int revision() {
        return revision;
    }

    public static synchronized List<GoalTab> goalTabs() {
        ArrayList<GoalTab> tabs = new ArrayList<>(GOALS.size());
        for (MutableGoalTab tab : GOALS.values()) {
            tabs.add(tab.snapshot(tab.goalId.equals(activeGoalId)));
        }
        return List.copyOf(tabs);
    }

    public static synchronized GoalTab activeGoal() {
        MutableGoalTab tab = GOALS.get(activeGoalId);
        return tab == null ? null : tab.snapshot(true);
    }

    public static synchronized boolean hasActiveGoal() {
        return GOALS.containsKey(activeGoalId);
    }

    public static synchronized GoalTab addOrActivate(GoalDescriptor descriptor) {
        if (descriptor == null || descriptor.primaryTargetOutput() == null) {
            return null;
        }
        int targetCount = resolvedTargetCount(descriptor);
        GoalDescriptor normalized = withTargetCount(descriptor, targetCount);
        MutableGoalTab existing = GOALS.get(normalized.goalId());
        if (existing == null) {
            existing = new MutableGoalTab(normalized.goalId(), normalized.label(), targetCount, normalized);
            GOALS.put(existing.goalId, existing);
        } else {
            existing.label = normalized.label();
            existing.targetCount = targetCount;
            existing.descriptor = normalized;
        }
        activeGoalId = existing.goalId;
        revision++;
        return existing.snapshot(true);
    }

    public static synchronized void selectAll() {
        if (activeGoalId.isEmpty()) {
            return;
        }
        activeGoalId = "";
        revision++;
    }

    public static synchronized boolean selectGoal(String goalId) {
        String id = clean(goalId);
        if (id.isEmpty() || !GOALS.containsKey(id)) {
            return false;
        }
        if (id.equals(activeGoalId)) {
            return true;
        }
        activeGoalId = id;
        revision++;
        return true;
    }

    public static synchronized boolean removeGoal(String goalId) {
        String id = clean(goalId);
        if (id.isEmpty()) {
            return false;
        }
        MutableGoalTab removed = GOALS.remove(id);
        if (removed == null) {
            return false;
        }
        if (id.equals(activeGoalId)) {
            activeGoalId = GOALS.isEmpty() ? "" : GOALS.keySet().iterator().next();
        }
        revision++;
        return true;
    }

    public static synchronized boolean adjustTargetCount(String goalId, int delta) {
        String id = clean(goalId);
        if (id.isEmpty()) {
            GoalTab active = activeGoal();
            id = active == null ? "" : active.goalId();
        }
        MutableGoalTab tab = GOALS.get(id);
        if (tab == null || delta == 0) {
            return false;
        }
        int next = Math.max(1, tab.targetCount + delta);
        if (next == tab.targetCount) {
            return false;
        }
        tab.targetCount = next;
        tab.descriptor = withTargetCount(tab.descriptor, next);
        activeGoalId = tab.goalId;
        revision++;
        return true;
    }

    public static synchronized void clear() {
        if (GOALS.isEmpty() && activeGoalId.isEmpty()) {
            return;
        }
        GOALS.clear();
        activeGoalId = "";
        revision++;
    }

    public static GoalDescriptor withTargetCount(GoalDescriptor descriptor, int targetCount) {
        if (descriptor == null) {
            return null;
        }
        return new GoalDescriptor(
                descriptor.goalId(),
                descriptor.label(),
                descriptor.targetOutputs(),
                Math.max(1, targetCount),
                descriptor.focusedRecipeId(),
                descriptor.focusedCategoryId(),
                descriptor.recipes()
        );
    }

    private static int resolvedTargetCount(GoalDescriptor descriptor) {
        if (descriptor.targetCount() > 0) {
            return descriptor.targetCount();
        }
        GoalStackDescriptor output = descriptor.primaryTargetOutput();
        return output == null ? 1 : Math.max(1, output.count());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record GoalTab(
            String goalId,
            String label,
            int targetCount,
            GoalDescriptor descriptor,
            boolean active
    ) {
        public GoalTab {
            goalId = goalId == null || goalId.isBlank() ? "goal" : goalId.trim();
            label = label == null || label.isBlank() ? goalId : label.trim();
            targetCount = Math.max(1, targetCount);
        }
    }

    private static final class MutableGoalTab {
        private final String goalId;
        private String label;
        private int targetCount;
        private GoalDescriptor descriptor;

        private MutableGoalTab(String goalId, String label, int targetCount, GoalDescriptor descriptor) {
            this.goalId = goalId;
            this.label = label;
            this.targetCount = targetCount;
            this.descriptor = descriptor;
        }

        private GoalTab snapshot(boolean active) {
            return new GoalTab(goalId, label, targetCount, descriptor, active);
        }
    }
}
