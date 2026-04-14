package dev.imagio.slot.client.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdentitySupportTest {
    @Test
    void normalizesStableMovableIdentityForKnownModdedGearAndContainers() {
        assertStableMovableIdentity("onlypaxels:netherite_paxel");
        assertStableMovableIdentity("trulymodular:netherite_zweihander");
        assertStableMovableIdentity("simpletms:tm_case");
        assertStableMovableIdentity("simpletms:tr_case");
        assertStableMovableIdentity("sophisticatedbackpacks:backpack");
        assertStableMovableIdentity("relics:golden_ring");
    }

    @Test
    void leavesOrdinaryMaterialsAsExactComponentAwareIdentity() {
        ItemIdentity exactStone = ItemIdentity.exact("minecraft:stone", "custom");

        assertFalse(ItemIdentitySupport.usesStableMovableIdentity("minecraft:stone"));
        assertEquals(exactStone, ItemIdentitySupport.normalizeMovableIdentity(exactStone));
        assertFalse(ItemIdentitySupport.matchesMovableIdentity(exactStone, ItemIdentity.of("minecraft:stone")));
    }

    private static void assertStableMovableIdentity(String itemId) {
        ItemIdentity exactIdentity = ItemIdentity.exact(itemId, "custom");

        assertTrue(ItemIdentitySupport.usesStableMovableIdentity(itemId));
        assertEquals(ItemIdentity.of(itemId), ItemIdentitySupport.normalizeMovableIdentity(exactIdentity));
        assertTrue(ItemIdentitySupport.matchesMovableIdentity(exactIdentity, ItemIdentity.of(itemId)));
    }
}
