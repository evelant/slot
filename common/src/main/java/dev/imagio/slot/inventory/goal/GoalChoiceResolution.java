package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record GoalChoiceResolution(
        Map<String, ItemIdentity> choicesByKey,
        Map<String, String> recipeChoicesByKey
) {
    public GoalChoiceResolution(Map<String, ItemIdentity> choicesByKey) {
        this(choicesByKey, Map.of());
    }

    public GoalChoiceResolution {
        LinkedHashMap<String, ItemIdentity> copy = new LinkedHashMap<>();
        if (choicesByKey != null) {
            for (Map.Entry<String, ItemIdentity> entry : choicesByKey.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                    continue;
                }
                copy.put(entry.getKey().trim(), entry.getValue());
            }
        }
        choicesByKey = Collections.unmodifiableMap(copy);

        LinkedHashMap<String, String> recipeCopy = new LinkedHashMap<>();
        if (recipeChoicesByKey != null) {
            for (Map.Entry<String, String> entry : recipeChoicesByKey.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null || entry.getValue().isBlank()) {
                    continue;
                }
                recipeCopy.put(entry.getKey().trim(), entry.getValue().trim());
            }
        }
        recipeChoicesByKey = Collections.unmodifiableMap(recipeCopy);
    }

    public static GoalChoiceResolution empty() {
        return new GoalChoiceResolution(Map.of(), Map.of());
    }

    public GoalChoiceResolution withChoice(String choiceGroupId, ItemIdentity identity) {
        if (choiceGroupId == null || choiceGroupId.isBlank() || identity == null) {
            return this;
        }
        LinkedHashMap<String, ItemIdentity> copy = new LinkedHashMap<>(choicesByKey);
        copy.put(choiceGroupId.trim(), identity);
        return new GoalChoiceResolution(copy, recipeChoicesByKey);
    }

    public GoalChoiceResolution withRecipeChoice(String choiceGroupId, String recipeId) {
        if (choiceGroupId == null || choiceGroupId.isBlank() || recipeId == null || recipeId.isBlank()) {
            return this;
        }
        LinkedHashMap<String, String> copy = new LinkedHashMap<>(recipeChoicesByKey);
        copy.put(choiceGroupId.trim(), recipeId.trim());
        return new GoalChoiceResolution(choicesByKey, copy);
    }

    public GoalChoiceResolution withoutChoice(String choiceGroupId) {
        if (choiceGroupId == null || choiceGroupId.isBlank()) {
            return this;
        }
        String key = choiceGroupId.trim();
        if (!choicesByKey.containsKey(key) && !recipeChoicesByKey.containsKey(key)) {
            return this;
        }
        LinkedHashMap<String, ItemIdentity> copy = new LinkedHashMap<>(choicesByKey);
        copy.remove(key);
        LinkedHashMap<String, String> recipeCopy = new LinkedHashMap<>(recipeChoicesByKey);
        recipeCopy.remove(key);
        return new GoalChoiceResolution(copy, recipeCopy);
    }

    public boolean hasChoice(String choiceGroupId) {
        return choiceFor("", choiceGroupId) != null || !recipeChoiceFor(choiceGroupId).isBlank();
    }

    public ItemIdentity choiceFor(String ingredientId, String choiceGroupId) {
        if (choiceGroupId != null && !choiceGroupId.isBlank()) {
            ItemIdentity byGroup = choicesByKey.get(choiceGroupId);
            if (byGroup != null) {
                return byGroup;
            }
        }
        if (ingredientId != null && !ingredientId.isBlank()) {
            return choicesByKey.get(ingredientId);
        }
        return null;
    }

    public String recipeChoiceFor(String choiceGroupId) {
        if (choiceGroupId == null || choiceGroupId.isBlank()) {
            return "";
        }
        return recipeChoicesByKey.getOrDefault(choiceGroupId.trim(), "");
    }
}
