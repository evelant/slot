package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickAccessServiceTest {
    @Test
    void backpackFallbackAllowedForEmptyAndCarriedSourceSets() {
        assertTrue(QuickAccessPolicy.allowsBackpackFallback(null));
        assertTrue(QuickAccessPolicy.allowsBackpackFallback(Set.of()));
        assertTrue(QuickAccessPolicy.allowsBackpackFallback(Set.of(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK)));
        assertTrue(QuickAccessPolicy.allowsBackpackFallback(Set.of(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE)));
    }

    @Test
    void backpackFallbackRejectedForExternalOnlySourceSets() {
        assertFalse(QuickAccessPolicy.allowsBackpackFallback(Set.of("open_container")));
    }

    @Test
    void quickAccessIndexHelpersTrackHotbarAndOffhandRange() {
        assertTrue(QuickAccessPolicy.isQuickAccessIndex(0));
        assertTrue(QuickAccessPolicy.isQuickAccessIndex(HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX));
        assertFalse(QuickAccessPolicy.isQuickAccessIndex(-1));
        assertFalse(QuickAccessPolicy.isQuickAccessIndex(HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT));
        assertTrue(QuickAccessPolicy.isOffhandIndex(HotbarLoadoutDefinition.OFFHAND_SLOT_INDEX));
        assertFalse(QuickAccessPolicy.isOffhandIndex(0));
    }
}
