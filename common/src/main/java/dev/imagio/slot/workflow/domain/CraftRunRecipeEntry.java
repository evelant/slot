package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;

import java.util.ArrayList;
import java.util.List;

public record CraftRunRecipeEntry(
        String entryId,
        long sequence,
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
    public CraftRunRecipeEntry {
        entryId = entryId == null || entryId.isBlank() ? "craft-run" : entryId.trim();
        sequence = Math.max(1L, sequence);
        sourceKey = sourceKey == null ? "" : sourceKey.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
        label = label == null || label.isBlank()
                ? outputLabel == null || outputLabel.isBlank() ? "Craft run" : outputLabel.trim()
                : label.trim();
        outputIdentity = ItemIdentityCollections.key(outputIdentity);
        outputLabel = outputLabel == null || outputLabel.isBlank()
                ? outputIdentity == null ? "Output" : outputIdentity.itemId()
                : outputLabel.trim();
        outputCountPerBatch = Math.max(1, outputCountPerBatch);
        remainingOutputCount = Math.max(0, remainingOutputCount);
        inputs = CraftRunIngredientGroup.normalize(inputs);
        diagnostics = diagnostics == null
                ? List.of()
                : List.copyOf(diagnostics.stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList());
    }

    public static CraftRunRecipeEntry fromCapture(String entryId, long sequence, CraftRunRecipeCapture capture) {
        CraftRunRecipeCapture resolved = capture == null ? CraftRunRecipeCapture.empty() : capture;
        return new CraftRunRecipeEntry(
                entryId,
                sequence,
                resolved.sourceKey(),
                resolved.recipeId(),
                resolved.label(),
                resolved.outputIdentity(),
                resolved.outputLabel(),
                resolved.outputCountPerBatch(),
                resolved.remainingOutputCount(),
                resolved.inputs(),
                resolved.diagnostics());
    }

    public boolean active() {
        return outputIdentity != null && !inputs.isEmpty();
    }

    public boolean pending() {
        return active() && remainingOutputCount > 0;
    }

    public boolean complete() {
        return active() && remainingOutputCount <= 0;
    }

    public int remainingBatches() {
        if (remainingOutputCount <= 0) {
            return 0;
        }
        return Math.max(1, (remainingOutputCount + outputCountPerBatch - 1) / outputCountPerBatch);
    }

    public CraftRunRecipeEntry withRemainingOutputCount(int count) {
        return new CraftRunRecipeEntry(
                entryId,
                sequence,
                sourceKey,
                recipeId,
                label,
                outputIdentity,
                outputLabel,
                outputCountPerBatch,
                count,
                inputs,
                diagnostics);
    }

    public CraftRunRecipeEntry withSelectedAlternative(String groupId, ItemIdentity identity) {
        if (groupId == null || groupId.isBlank() || inputs.isEmpty()) {
            return this;
        }
        boolean changed = false;
        ArrayList<CraftRunIngredientGroup> nextInputs = new ArrayList<>(inputs.size());
        for (CraftRunIngredientGroup group : inputs) {
            if (group == null || !group.groupId().equals(groupId)) {
                nextInputs.add(group);
                continue;
            }
            CraftRunIngredientGroup nextGroup = group.withSelectedAlternative(identity);
            changed = changed || nextGroup != group;
            nextInputs.add(nextGroup);
        }
        if (!changed) {
            return this;
        }
        return new CraftRunRecipeEntry(
                entryId,
                sequence,
                sourceKey,
                recipeId,
                label,
                outputIdentity,
                outputLabel,
                outputCountPerBatch,
                remainingOutputCount,
                nextInputs,
                diagnostics);
    }
}
