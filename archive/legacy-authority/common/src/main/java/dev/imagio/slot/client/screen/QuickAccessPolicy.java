package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.session.ChestLikeMenuLayout;

import java.util.Set;

final class QuickAccessPolicy {
    private QuickAccessPolicy() {
    }

    static boolean allowsBackpackFallback(Set<String> preferredSourceIds) {
        return preferredSourceIds == null
                || preferredSourceIds.isEmpty()
                || preferredSourceIds.contains(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK)
                || preferredSourceIds.contains(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE);
    }

    static boolean isQuickAccessIndex(int quickAccessIndex) {
        return quickAccessIndex >= 0 && quickAccessIndex < HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT;
    }

    static boolean isOffhandIndex(int quickAccessIndex) {
        return quickAccessIndex == HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX;
    }
}
