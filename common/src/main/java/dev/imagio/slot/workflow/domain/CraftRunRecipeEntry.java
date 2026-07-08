package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;

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
        SlotResourceIdentity outputResourceIdentity,
        long outputAmountPerBatch,
        long remainingOutputAmount,
        List<String> diagnostics
) {
    public CraftRunRecipeEntry(
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
        this(
                entryId,
                sequence,
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

    public CraftRunRecipeEntry {
        entryId = entryId == null || entryId.isBlank() ? "craft-run" : entryId.trim();
        sequence = Math.max(1L, sequence);
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
        remainingOutputCount = Math.max(0, remainingOutputCount);
        outputAmountPerBatch = Math.max(1L, outputAmountPerBatch <= 0L
                ? outputCountPerBatch
                : outputAmountPerBatch);
        remainingOutputAmount = Math.max(0L, remainingOutputAmount < 0L
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
                resolved.outputResourceIdentity(),
                resolved.outputAmountPerBatch(),
                resolved.remainingOutputAmount(),
                resolved.diagnostics());
    }

    public boolean active() {
        return outputResourceIdentity != null && !inputs.isEmpty();
    }

    public boolean pending() {
        return active() && remainingOutputAmount > 0L;
    }

    public boolean complete() {
        return active() && remainingOutputAmount <= 0L;
    }

    public int remainingBatches() {
        if (remainingOutputAmount <= 0L) {
            return 0;
        }
        long batches = (remainingOutputAmount + outputAmountPerBatch - 1L) / outputAmountPerBatch;
        return batches >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(1L, batches);
    }

    public CraftRunRecipeEntry withRemainingOutputCount(int count) {
        long nextAmount = outputResourceIdentity != null && outputResourceIdentity.fluid()
                ? Math.max(0L, count)
                : Math.max(0L, count);
        return withRemainingOutput(count, nextAmount);
    }

    public CraftRunRecipeEntry withRemainingOutputAmount(long amount) {
        return withRemainingOutput(saturatedInt(amount), amount);
    }

    private CraftRunRecipeEntry withRemainingOutput(int count, long amount) {
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
                outputResourceIdentity,
                outputAmountPerBatch,
                amount,
                diagnostics);
    }

    public CraftRunRecipeEntry withSelectedAlternative(String groupId, ItemIdentity identity) {
        return withSelectedAlternative(groupId, SlotResourceIdentity.item(identity));
    }

    public CraftRunRecipeEntry withSelectedAlternative(String groupId, SlotResourceIdentity identity) {
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
                outputResourceIdentity,
                outputAmountPerBatch,
                remainingOutputAmount,
                diagnostics);
    }

    private static int saturatedInt(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
