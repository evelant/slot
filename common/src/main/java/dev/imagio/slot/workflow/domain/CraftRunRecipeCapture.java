package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.List;

public record CraftRunRecipeCapture(
        String sourceKey,
        String recipeId,
        String label,
        ItemIdentity outputIdentity,
        String outputLabel,
        int outputCountPerBatch,
        int remainingOutputCount,
        List<CraftRunIngredientGroup> inputs,
        List<String> diagnostics
) {
    public CraftRunRecipeCapture {
        sourceKey = sourceKey == null ? "" : sourceKey.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
        label = label == null || label.isBlank()
                ? outputLabel == null || outputLabel.isBlank() ? "Craft run" : outputLabel.trim()
                : label.trim();
        outputLabel = outputLabel == null || outputLabel.isBlank()
                ? outputIdentity == null ? "Output" : outputIdentity.itemId()
                : outputLabel.trim();
        outputCountPerBatch = Math.max(1, outputCountPerBatch);
        remainingOutputCount = Math.max(1, remainingOutputCount);
        inputs = CraftRunIngredientGroup.normalize(inputs);
        diagnostics = diagnostics == null
                ? List.of()
                : List.copyOf(diagnostics.stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList());
    }

    public static CraftRunRecipeCapture empty() {
        return new CraftRunRecipeCapture("", "", "", null, "", 1, 1, List.of(), List.of());
    }

    public boolean active() {
        return outputIdentity != null && !inputs.isEmpty();
    }
}
