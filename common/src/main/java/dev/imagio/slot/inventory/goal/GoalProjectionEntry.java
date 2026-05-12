package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.List;

public record GoalProjectionEntry(
        GoalProjectionEntryKind kind,
        ItemIdentity identity,
        String label,
        String recipeId,
        String ingredientId,
        String serializedIngredient,
        String producerRecipeId,
        int requiredCount,
        int carriedCount,
        int storageCount,
        int missingCount,
        int wantedCount,
        boolean choiceIndicator,
        String choiceGroupId,
        List<GoalStackDescriptor> alternatives,
        List<String> breadcrumbs,
        List<String> diagnostics
) {
    public GoalProjectionEntry {
        kind = kind == null ? GoalProjectionEntryKind.MISSING_GHOST : kind;
        label = label == null || label.isBlank()
                ? (identity == null ? "" : identity.itemId())
                : label.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
        ingredientId = ingredientId == null ? "" : ingredientId.trim();
        serializedIngredient = serializedIngredient == null ? "" : serializedIngredient.trim();
        producerRecipeId = producerRecipeId == null ? "" : producerRecipeId.trim();
        requiredCount = Math.max(0, requiredCount);
        carriedCount = Math.max(0, carriedCount);
        storageCount = Math.max(0, storageCount);
        missingCount = Math.max(0, missingCount);
        wantedCount = Math.max(0, wantedCount);
        choiceGroupId = choiceGroupId == null ? "" : choiceGroupId.trim();
        alternatives = copyStacks(alternatives);
        breadcrumbs = copyStrings(breadcrumbs);
        diagnostics = copyStrings(diagnostics);
        choiceIndicator = choiceIndicator && !choiceGroupId.isBlank();
    }

    public static GoalProjectionEntry fromRequirement(GoalRequirement requirement) {
        GoalProjectionEntryKind kind = requirement.carriedCount() > 0
                ? GoalProjectionEntryKind.REAL_CARD
                : requirement.storageCount() > 0
                ? GoalProjectionEntryKind.STORAGE_GHOST
                : GoalProjectionEntryKind.MISSING_GHOST;
        return new GoalProjectionEntry(
                kind,
                requirement.identity(),
                requirement.label(),
                requirement.recipeId(),
                requirement.ingredientId(),
                "",
                requirement.producerRecipeId(),
                requirement.requiredCount(),
                requirement.carriedCount(),
                requirement.storageCount(),
                requirement.missingCount(),
                requirement.wantedCount(),
                requirement.choiceInvolved(),
                requirement.choiceGroupId(),
                List.of(),
                requirement.breadcrumbs(),
                requirement.diagnostics()
        );
    }

    public static GoalProjectionEntry fromChoice(GoalChoiceRequirement choice) {
        return new GoalProjectionEntry(
                GoalProjectionEntryKind.CHOICE_CARD,
                choice.targetIdentity(),
                choice.label(),
                choice.recipeId(),
                choice.ingredientId(),
                choice.serializedIngredient(),
                "",
                choice.requiredCount(),
                0,
                0,
                choice.unresolvedCount(),
                0,
                false,
                choice.choiceGroupId(),
                choice.alternatives(),
                choice.breadcrumbs(),
                choice.diagnostics()
        );
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
