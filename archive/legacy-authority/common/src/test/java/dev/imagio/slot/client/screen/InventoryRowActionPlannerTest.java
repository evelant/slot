package dev.imagio.slot.client.screen;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryRowActionPlannerTest {
    @Test
    void equippedSourcesIncludeArmor() {
        assertTrue(InventoryRowActionPlanner.hasEquippedOnlySources(Map.of(dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, 1)));
    }

    @Test
    void equippedSourcesIncludeOffhand() {
        assertTrue(InventoryRowActionPlanner.hasEquippedOnlySources(Map.of(dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, 1)));
    }

    @Test
    void nonEquippedSourcesAreIgnored() {
        assertFalse(InventoryRowActionPlanner.hasEquippedOnlySources(Map.of(
                dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 1,
                dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, 2
        )));
    }

    @Test
    void equippedSourcesAreNotOnlyWhenOtherRelevantSourcesExist() {
        assertFalse(InventoryRowActionPlanner.hasEquippedOnlySources(Map.of(
                dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, 1,
                dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 1
        )));
    }

    @Test
    void equippedSourcesRespectRelevantScope() {
        assertTrue(InventoryRowActionPlanner.hasEquippedOnlySources(
                Map.of(
                        dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, 1,
                        dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_OPEN_CONTAINER, 4
                ),
                Set.of(
                        dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR,
                        dev.imagio.slot.session.ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND
                )
        ));
    }
}
