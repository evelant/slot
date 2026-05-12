package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

public final class GoalChoiceKeys {
    private static final String PRODUCER_MARKER = "producer";

    private GoalChoiceKeys() {
    }

    public static String ingredientChoiceGroupId(String recipeId, String ingredientId) {
        String recipe = clean(recipeId);
        String ingredient = clean(ingredientId);
        if (recipe.isBlank() || ingredient.isBlank()) {
            return "";
        }
        return recipe + "#" + ingredient;
    }

    public static String producerChoiceGroupId(String recipeId, String ingredientId, ItemIdentity outputIdentity) {
        String ingredientGroup = ingredientChoiceGroupId(recipeId, ingredientId);
        if (ingredientGroup.isBlank()) {
            return "";
        }
        String output = outputIdentity == null ? "" : clean(outputIdentity.itemId());
        return output.isBlank()
                ? ingredientGroup + "#" + PRODUCER_MARKER
                : ingredientGroup + "#" + PRODUCER_MARKER + "#" + output;
    }

    public static boolean isProducerChoiceGroup(String choiceGroupId) {
        String[] parts = parts(choiceGroupId);
        return parts.length >= 3 && PRODUCER_MARKER.equals(parts[2]);
    }

    public static String recipeIdFromChoiceGroup(String choiceGroupId) {
        String[] parts = parts(choiceGroupId);
        return parts.length == 0 ? "" : parts[0];
    }

    public static String ingredientIdFromChoiceGroup(String choiceGroupId) {
        String[] parts = parts(choiceGroupId);
        return parts.length < 2 ? "" : parts[1];
    }

    public static ItemIdentity producerTargetIdentity(String choiceGroupId) {
        String[] parts = parts(choiceGroupId);
        if (parts.length < 4 || parts[3].isBlank()) {
            return null;
        }
        return ItemIdentity.of(parts[3]);
    }

    private static String[] parts(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        String[] raw = value.trim().split("#", -1);
        for (int index = 0; index < raw.length; index++) {
            raw[index] = clean(raw[index]);
        }
        return raw;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
