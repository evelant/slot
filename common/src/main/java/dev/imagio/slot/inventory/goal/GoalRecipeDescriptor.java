package dev.imagio.slot.inventory.goal;

import java.util.ArrayList;
import java.util.List;

public record GoalRecipeDescriptor(
        String recipeId,
        String categoryId,
        boolean supportsTree,
        List<GoalStackDescriptor> outputs,
        List<GoalIngredientDescriptor> inputs,
        List<GoalIngredientDescriptor> catalysts,
        List<String> diagnostics
) {
    public GoalRecipeDescriptor {
        recipeId = recipeId == null || recipeId.isBlank() ? "recipe" : recipeId.trim();
        categoryId = categoryId == null ? "" : categoryId.trim();
        outputs = copyStacks(outputs);
        inputs = copyIngredients(inputs);
        catalysts = copyIngredients(catalysts);
        diagnostics = copyStrings(diagnostics);
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

    private static List<GoalIngredientDescriptor> copyIngredients(List<GoalIngredientDescriptor> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalIngredientDescriptor> copy = new ArrayList<>(source.size());
        for (GoalIngredientDescriptor ingredient : source) {
            if (ingredient != null) {
                copy.add(ingredient);
            }
        }
        return List.copyOf(copy);
    }

    private static List<String> copyStrings(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<String> copy = new ArrayList<>(source.size());
        for (String value : source) {
            if (value != null && !value.isBlank()) {
                copy.add(value.trim());
            }
        }
        return List.copyOf(copy);
    }
}
