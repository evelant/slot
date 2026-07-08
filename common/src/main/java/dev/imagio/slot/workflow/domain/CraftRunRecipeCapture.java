package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;

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
        SlotResourceIdentity outputResourceIdentity,
        long outputAmountPerBatch,
        long remainingOutputAmount,
        List<String> diagnostics
) {
    public CraftRunRecipeCapture(
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
        this(
                sourceKey,
                recipeId,
                label,
                outputIdentity,
                outputLabel,
                outputCountPerBatch,
                remainingOutputCount,
                inputs,
                SlotResourceIdentity.item(outputIdentity),
                outputCountPerBatch,
                remainingOutputCount,
                diagnostics);
    }

    public CraftRunRecipeCapture {
        sourceKey = sourceKey == null ? "" : sourceKey.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
        outputIdentity = ItemIdentityCollections.key(outputIdentity);
        outputResourceIdentity = SlotResourceCollections.key(outputResourceIdentity != null
                ? outputResourceIdentity
                : SlotResourceIdentity.item(outputIdentity));
        if (outputResourceIdentity != null && outputResourceIdentity.item()) {
            outputIdentity = outputResourceIdentity.toItemIdentity();
        }
        if (outputResourceIdentity != null && outputResourceIdentity.fluid()) {
            outputIdentity = null;
        }
        label = label == null || label.isBlank()
                ? outputLabel == null || outputLabel.isBlank() ? "Craft run" : outputLabel.trim()
                : label.trim();
        outputLabel = outputLabel == null || outputLabel.isBlank()
                ? outputResourceIdentity == null ? "Output" : outputResourceIdentity.id()
                : outputLabel.trim();
        outputCountPerBatch = Math.max(1, outputCountPerBatch);
        remainingOutputCount = Math.max(1, remainingOutputCount);
        outputAmountPerBatch = Math.max(1L, outputAmountPerBatch <= 0L
                ? outputCountPerBatch
                : outputAmountPerBatch);
        remainingOutputAmount = Math.max(1L, remainingOutputAmount <= 0L
                ? remainingOutputCount
                : remainingOutputAmount);
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
        return outputResourceIdentity != null && !inputs.isEmpty();
    }
}
