package dev.imagio.slot.client.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemHeuristicsTest {
    @Test
    void portableContainerFallbackRecognizesKnownContainerLikeIds() {
        assertTrue(ItemHeuristics.hasPortableContainerFallbackToken("simpletms:tm_case"));
        assertTrue(ItemHeuristics.hasPortableContainerFallbackToken("simpletms:tr_case"));
        assertTrue(ItemHeuristics.hasPortableContainerFallbackToken("sophisticatedbackpacks:backpack"));
        assertTrue(ItemHeuristics.hasPortableContainerFallbackToken("travelgear:miners_satchel"));
        assertFalse(ItemHeuristics.hasPortableContainerFallbackToken("trulymodular:netherite_zweihander"));
    }

    @Test
    void conservativeAutoJunkRemainsVanillaOnlyAndNarrow() {
        assertTrue(ItemHeuristics.isConservativeVanillaAutoJunkItemId("minecraft:stone_bricks"));
        assertTrue(ItemHeuristics.isConservativeVanillaAutoJunkItemId("minecraft:wheat_seeds"));
        assertFalse(ItemHeuristics.isConservativeVanillaAutoJunkItemId("cobblemon:shiny_stone"));
        assertFalse(ItemHeuristics.isConservativeVanillaAutoJunkItemId("megashowdown:mega_stone"));
        assertFalse(ItemHeuristics.isConservativeVanillaAutoJunkItemId("simpletms:tm_case"));
    }
}
