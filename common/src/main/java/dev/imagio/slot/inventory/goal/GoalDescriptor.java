package dev.imagio.slot.inventory.goal;

import java.util.ArrayList;
import java.util.List;

public record GoalDescriptor(
        String goalId,
        String label,
        List<GoalStackDescriptor> targetOutputs,
        int targetCount,
        String focusedRecipeId,
        String focusedCategoryId,
        List<GoalRecipeDescriptor> recipes
) {
    public GoalDescriptor {
        goalId = goalId == null || goalId.isBlank() ? "goal" : goalId.trim();
        label = label == null || label.isBlank() ? goalId : label.trim();
        targetOutputs = copyStacks(targetOutputs);
        targetCount = Math.max(0, targetCount);
        focusedRecipeId = focusedRecipeId == null ? "" : focusedRecipeId.trim();
        focusedCategoryId = focusedCategoryId == null ? "" : focusedCategoryId.trim();
        recipes = copyRecipes(recipes);
    }

    public GoalStackDescriptor primaryTargetOutput() {
        return targetOutputs.isEmpty() ? null : targetOutputs.get(0);
    }

    private static List<GoalStackDescriptor> copyStacks(List<GoalStackDescriptor> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalStackDescriptor> copy = new ArrayList<>(source.size());
        for (GoalStackDescriptor stack : source) {
            if (stack != null) {
                copy.add(stack);
            }
        }
        return List.copyOf(copy);
    }

    private static List<GoalRecipeDescriptor> copyRecipes(List<GoalRecipeDescriptor> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalRecipeDescriptor> copy = new ArrayList<>(source.size());
        for (GoalRecipeDescriptor recipe : source) {
            if (recipe != null) {
                copy.add(recipe);
            }
        }
        return List.copyOf(copy);
    }
}
