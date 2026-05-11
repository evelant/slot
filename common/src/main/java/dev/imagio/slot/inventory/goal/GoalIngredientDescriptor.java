package dev.imagio.slot.inventory.goal;

import java.util.ArrayList;
import java.util.List;

public record GoalIngredientDescriptor(
        String ingredientId,
        String label,
        int quantity,
        double chance,
        String serializedIngredient,
        List<GoalStackDescriptor> alternatives,
        boolean choiceRequired,
        String tagOrListLabel,
        List<String> diagnostics
) {
    public GoalIngredientDescriptor {
        ingredientId = ingredientId == null || ingredientId.isBlank() ? "ingredient" : ingredientId.trim();
        tagOrListLabel = tagOrListLabel == null ? "" : tagOrListLabel.trim();
        label = label == null || label.isBlank()
                ? (!tagOrListLabel.isBlank() ? tagOrListLabel : ingredientId)
                : label.trim();
        quantity = Math.max(0, quantity);
        chance = Double.isFinite(chance) ? Math.max(0.0D, chance) : 1.0D;
        serializedIngredient = serializedIngredient == null ? "" : serializedIngredient.trim();
        alternatives = copyStacks(alternatives);
        choiceRequired = choiceRequired || alternatives.size() > 1;
        diagnostics = copyStrings(diagnostics);
    }

    public static GoalIngredientDescriptor concrete(
            String ingredientId,
            GoalStackDescriptor alternative,
            int quantity
    ) {
        return new GoalIngredientDescriptor(
                ingredientId,
                alternative == null ? ingredientId : alternative.displayName(),
                quantity,
                1.0D,
                "",
                alternative == null ? List.of() : List.of(alternative),
                false,
                "",
                List.of()
        );
    }

    public static GoalIngredientDescriptor choice(
            String ingredientId,
            String label,
            int quantity,
            String tagOrListLabel,
            List<GoalStackDescriptor> alternatives
    ) {
        return new GoalIngredientDescriptor(
                ingredientId,
                label,
                quantity,
                1.0D,
                "",
                alternatives,
                true,
                tagOrListLabel,
                List.of()
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
