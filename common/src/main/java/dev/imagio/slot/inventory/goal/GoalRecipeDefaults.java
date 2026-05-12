package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record GoalRecipeDefaults(Map<String, String> recipeChoicesByOutputItemId) {
    public GoalRecipeDefaults {
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        if (recipeChoicesByOutputItemId != null) {
            for (Map.Entry<String, String> entry : recipeChoicesByOutputItemId.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null || entry.getValue().isBlank()) {
                    continue;
                }
                copy.put(entry.getKey().trim(), entry.getValue().trim());
            }
        }
        recipeChoicesByOutputItemId = Collections.unmodifiableMap(copy);
    }

    public static GoalRecipeDefaults empty() {
        return new GoalRecipeDefaults(Map.of());
    }

    public GoalRecipeDefaults withRecipeChoice(ItemIdentity outputIdentity, String recipeId) {
        String key = outputKey(outputIdentity);
        if (key.isBlank() || recipeId == null || recipeId.isBlank()) {
            return this;
        }
        LinkedHashMap<String, String> copy = new LinkedHashMap<>(recipeChoicesByOutputItemId);
        copy.put(key, recipeId.trim());
        return new GoalRecipeDefaults(copy);
    }

    public GoalRecipeDefaults mergedWith(GoalRecipeDefaults overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return this;
        }
        LinkedHashMap<String, String> copy = new LinkedHashMap<>(recipeChoicesByOutputItemId);
        copy.putAll(overrides.recipeChoicesByOutputItemId());
        return new GoalRecipeDefaults(copy);
    }

    public String recipeChoiceFor(ItemIdentity outputIdentity) {
        String key = outputKey(outputIdentity);
        return key.isBlank() ? "" : recipeChoicesByOutputItemId.getOrDefault(key, "");
    }

    public boolean hasRecipeChoice(ItemIdentity outputIdentity) {
        return !recipeChoiceFor(outputIdentity).isBlank();
    }

    public boolean isEmpty() {
        return recipeChoicesByOutputItemId.isEmpty();
    }

    private static String outputKey(ItemIdentity outputIdentity) {
        return outputIdentity == null || outputIdentity.itemId() == null ? "" : outputIdentity.itemId().trim();
    }
}
