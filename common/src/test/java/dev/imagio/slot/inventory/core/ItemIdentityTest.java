package dev.imagio.slot.inventory.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdentityTest {

    /**
     * Regression: a non-stackable item with no component data (e.g. vanilla
     * water_bucket) was being created two ways — {@code ItemIdentity.of(id)}
     * by the populate command / kit bring list and
     * {@code ItemIdentity.exact(id, "")} by ItemIdentityMatcher.create() on
     * the chest stack — splitting the atlas card into two and breaking
     * carriedHasMovable. Constructor must collapse the empty-fingerprint
     * exact form to id form so they hash + equal the same.
     */
    @Test
    void exactWithBlankFingerprintEqualsItemIdForm() {
        ItemIdentity ofForm = ItemIdentity.of("minecraft:water_bucket");
        ItemIdentity exactBlank = ItemIdentity.exact("minecraft:water_bucket", "");
        ItemIdentity exactNull = ItemIdentity.exact("minecraft:water_bucket", null);

        assertEquals(ItemComparisonMode.ITEM_ID, ofForm.comparisonMode());
        assertEquals(ItemComparisonMode.ITEM_ID, exactBlank.comparisonMode());
        assertEquals(ItemComparisonMode.ITEM_ID, exactNull.comparisonMode());
        assertEquals(ofForm, exactBlank);
        assertEquals(ofForm.hashCode(), exactBlank.hashCode());
        assertEquals(ofForm, exactNull);
    }

    @Test
    void exactWithRealFingerprintStaysExact() {
        ItemIdentity exact = ItemIdentity.exact("minecraft:diamond_sword", "{Damage:42}");
        assertEquals(ItemComparisonMode.ITEM_ID_AND_COMPONENTS, exact.comparisonMode());
        assertNotEquals(ItemIdentity.of("minecraft:diamond_sword"), exact);
    }

    @Test
    void mutableUtilityItemsMatchMovableByItemId() {
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("mod:water_flask", "{Water:100}"),
                ItemIdentity.exact("mod:water_flask", "{Water:20}")));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("mod:building_gadget", "{Mode:\"wall\"}"),
                ItemIdentity.exact("mod:building_gadget", "{Mode:\"surface\"}")));
    }

    @Test
    void gluedToolNamesMatchMovableByItemId() {
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("grapplemod:grapplinghook", "{Damage:7,hookState:\"attached\"}"),
                ItemIdentity.exact("grapplemod:grapplinghook", "{Damage:1,hookState:\"idle\"}")));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("grapplemod:longfallboots", "{Damage:12}"),
                ItemIdentity.exact("grapplemod:longfallboots", "{Damage:44}")));
    }

    @Test
    void itemOnlyIgnoresVolatileComponentData() {
        assertEquals(
                ItemIdentity.of("tfc:metal/ingot/brass"),
                ItemIdentityMatcher.itemOnly(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/ingot/brass",
                        "{heat:704.2}",
                        1,
                        64)));
    }

    @Test
    void structuralKeysIgnoreComponentOnlyChurn() {
        assertEquals(
                ItemStackStructuralKey.from(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/ingot/brass",
                        "{heat:100.0}",
                        1,
                        64)),
                ItemStackStructuralKey.from(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/ingot/brass",
                        "{heat:725.0}",
                        1,
                        64)));
        assertNotEquals(
                ItemStackStructuralKey.from(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/ingot/brass",
                        "{heat:725.0}",
                        1,
                        64)),
                ItemStackStructuralKey.from(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/ingot/brass",
                        "{heat:725.0}",
                        2,
                        64)));
    }

    @Test
    void blanksOnlyComparisonModeDowngradesByDirectConstruction() {
        ItemIdentity raw = new ItemIdentity(
                "minecraft:water_bucket",
                ItemComparisonMode.ITEM_ID_AND_COMPONENTS,
                ""
        );
        assertEquals(ItemComparisonMode.ITEM_ID, raw.comparisonMode());
        assertEquals(ItemIdentity.of("minecraft:water_bucket"), raw);
    }
}
