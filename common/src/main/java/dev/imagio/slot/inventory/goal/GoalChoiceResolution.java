package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record GoalChoiceResolution(
        Map<String, ItemIdentity> choicesByKey
) {
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
    }

    public static GoalChoiceResolution empty() {
        return new GoalChoiceResolution(Map.of());
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
}
