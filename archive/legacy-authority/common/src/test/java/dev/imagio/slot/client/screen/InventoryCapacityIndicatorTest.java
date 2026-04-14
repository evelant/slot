package dev.imagio.slot.client.screen;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryCapacityIndicatorTest {
    @Test
    void carriedCapacitySourcesOnlyIncludeGenericInsertionTargets() {
        Set<String> resolved = InventoryCapacityIndicator.carriedCapacitySourceIds(Set.of(
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN,
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
                ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK,
                ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR,
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND,
                ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE
        ));

        assertEquals(Set.of(
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN,
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
                ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK,
                ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE
        ), resolved);
    }
}
