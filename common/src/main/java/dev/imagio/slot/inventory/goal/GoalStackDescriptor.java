package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record GoalStackDescriptor(
        ItemIdentity identity,
        String displayName,
        int count
) {
    public GoalStackDescriptor {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        displayName = displayName == null || displayName.isBlank() ? identity.itemId() : displayName.trim();
        count = Math.max(0, count);
    }

    public static GoalStackDescriptor of(String itemId, String displayName, int count) {
        return new GoalStackDescriptor(ItemIdentity.of(itemId), displayName, count);
    }

    public static GoalStackDescriptor of(String itemId, int count) {
        return of(itemId, itemId, count);
    }

    public GoalStackDescriptor withCount(int newCount) {
        return new GoalStackDescriptor(identity, displayName, newCount);
    }
}
