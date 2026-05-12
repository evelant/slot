package dev.imagio.slot.inventory.goal;

public record GoalPlanState(
        String goalId,
        String label,
        int targetCount,
        GoalDescriptor descriptor,
        GoalChoiceResolution choiceResolution
) {
    public GoalPlanState {
        goalId = goalId == null || goalId.isBlank()
                ? descriptor == null ? "goal" : descriptor.goalId()
                : goalId.trim();
        label = label == null || label.isBlank()
                ? descriptor == null ? goalId : descriptor.label()
                : label.trim();
        targetCount = Math.max(1, targetCount);
        choiceResolution = choiceResolution == null ? GoalChoiceResolution.empty() : choiceResolution;
    }
}
