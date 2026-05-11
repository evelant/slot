package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.List;

public record GoalRequirement(
        String requirementId,
        String recipeId,
        String ingredientId,
        GoalRequirementKind kind,
        ItemIdentity identity,
        String label,
        int requiredCount,
        int carriedCount,
        int proximateStorageCount,
        int elsewhereStorageCount,
        int missingCount,
        int desiredCount,
        boolean choiceInvolved,
        String choiceGroupId,
        List<String> breadcrumbs,
        List<String> diagnostics
) {
    public GoalRequirement {
        requirementId = requirementId == null || requirementId.isBlank() ? "requirement" : requirementId.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
        ingredientId = ingredientId == null ? "" : ingredientId.trim();
        kind = kind == null ? GoalRequirementKind.CONCRETE : kind;
        label = label == null || label.isBlank()
                ? (identity == null ? ingredientId : identity.itemId())
                : label.trim();
        requiredCount = Math.max(0, requiredCount);
        carriedCount = Math.max(0, carriedCount);
        proximateStorageCount = Math.max(0, proximateStorageCount);
        elsewhereStorageCount = Math.max(0, elsewhereStorageCount);
        missingCount = Math.max(0, missingCount);
        desiredCount = Math.max(0, desiredCount);
        choiceGroupId = choiceGroupId == null ? "" : choiceGroupId.trim();
        choiceInvolved = choiceInvolved && !choiceGroupId.isBlank();
        breadcrumbs = copyStrings(breadcrumbs);
        diagnostics = copyStrings(diagnostics);
    }

    public int storageCount() {
        return proximateStorageCount + elsewhereStorageCount;
    }

    public int visibleCount() {
        return carriedCount + storageCount();
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
