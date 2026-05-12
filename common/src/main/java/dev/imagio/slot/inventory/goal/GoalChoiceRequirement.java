package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.List;

public record GoalChoiceRequirement(
        String choiceGroupId,
        String recipeId,
        String ingredientId,
        String serializedIngredient,
        String label,
        int requiredCount,
        int unresolvedCount,
        ItemIdentity targetIdentity,
        List<GoalStackDescriptor> alternatives,
        List<GoalResolvedChoice> autoResolved,
        List<String> breadcrumbs,
        List<String> diagnostics
) {
    public GoalChoiceRequirement {
        choiceGroupId = clean(choiceGroupId);
        recipeId = clean(recipeId);
        ingredientId = clean(ingredientId);
        serializedIngredient = clean(serializedIngredient);
        label = label == null || label.isBlank() ? ingredientId : label.trim();
        requiredCount = Math.max(0, requiredCount);
        unresolvedCount = Math.max(0, Math.min(requiredCount, unresolvedCount));
        alternatives = copyStacks(alternatives);
        autoResolved = copyResolved(autoResolved);
        breadcrumbs = copyStrings(breadcrumbs);
        diagnostics = copyStrings(diagnostics);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
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

    private static List<GoalResolvedChoice> copyResolved(List<GoalResolvedChoice> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        ArrayList<GoalResolvedChoice> copy = new ArrayList<>(source.size());
        for (GoalResolvedChoice resolved : source) {
            if (resolved != null) {
                copy.add(resolved);
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
