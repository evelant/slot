package dev.imagio.slot.inventory.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void itemOnlyIdentityMatchesExactMovableIdentity() {
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("mod:water_flask", "{Water:100}"),
                ItemIdentity.of("mod:water_flask")));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("mod:building_gadget", "{Mode:\"wall\"}"),
                ItemIdentity.of("mod:building_gadget")));
    }

    @Test
    void exactComponentIdentitiesRemainExactWithoutAStackPolicySignal() {
        assertFalse(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("grapplemod:grapplinghook", "{Damage:7,hookState:\"attached\"}"),
                ItemIdentity.exact("grapplemod:grapplinghook", "{Damage:1,hookState:\"idle\"}")));
        assertFalse(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("mod:configurable_gadget", "{Mode:\"wall\"}"),
                ItemIdentity.exact("mod:configurable_gadget", "{Mode:\"surface\"}")));
    }

    @Test
    void damageOnlyExactIdentitiesNormalizeAsMovableCondition() {
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("grapplemod:longfallboots", "{Damage:12}"),
                ItemIdentity.exact("grapplemod:longfallboots", "{Damage:44}")));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("minecraft:diamond_pickaxe", "{minecraft:damage=>12}"),
                ItemIdentity.of("minecraft:diamond_pickaxe")));
    }

    @Test
    void damageableStacksCreateItemOnlyMovableIdentities() {
        assertEquals(
                ItemIdentity.of("gtceu:steel_mining_hammer"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "gtceu:steel_mining_hammer",
                        "{Damage:512}",
                        1,
                        1).damageable()));
        assertEquals(
                ItemIdentity.of("tfc:metal/hammer/steel"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/hammer/steel",
                        "{Damage:45}",
                        1,
                        1).damageable()));
        assertEquals(
                ItemIdentity.of("tfc:metal/tongs/steel"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "tfc:metal/tongs/steel",
                        "{Damage:45}",
                        1,
                        1).damageable()));
    }

    @Test
    void containerOnlyFingerprintsCreateItemOnlyMovableIdentities() {
        assertEquals(
                ItemIdentity.of("sns:straw_basket"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "sns:straw_basket",
                        "{Inventory:[{Slot:0b,id:\"minecraft:torch\",Count:8b}]}",
                        1,
                        1)));
    }

    @Test
    void registeredPortableContainerStacksCreateItemOnlyMovableIdentities() {
        PortableContainerClassifiers.register(stack -> "mod:portable_case".equals(stack.itemId()));

        assertEquals(
                ItemIdentity.of("mod:portable_case"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "mod:portable_case",
                        "{CustomState:1}",
                        1,
                        1)));
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
