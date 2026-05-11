package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record GoalResolvedChoice(
        String choiceGroupId,
        ItemIdentity identity,
        String label,
        int count,
        boolean manual
) {
    public GoalResolvedChoice {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        choiceGroupId = choiceGroupId == null ? "" : choiceGroupId.trim();
        label = label == null || label.isBlank() ? identity.itemId() : label.trim();
        count = Math.max(0, count);
    }
}
