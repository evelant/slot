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
    void patchouliGuideBookIdentityUsesBookSelectorNotIncidentalData() {
        ItemIdentity guide = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"tfc:field_guide\"}",
                1,
                1));
        ItemIdentity guideWithDisplayData = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{display:{Name:\"TerraFirmaGreg Guide\"},\"patchouli:book\":\"tfc:field_guide\"}",
                1,
                1));
        ItemIdentity otherGuide = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"ae2:guide\"}",
                1,
                1));

        assertEquals(ItemIdentity.exact("patchouli:guide_book", "patchouli:book=tfc:field_guide"), guide);
        assertEquals(guide, guideWithDisplayData);
        assertTrue(ItemIdentityMatcher.matchesMovable(guide, guideWithDisplayData));
        assertFalse(ItemIdentityMatcher.matchesMovable(guide, otherGuide));
    }

    @Test
    void patchouliGuideBookSelectorCanLiveInsideCustomData() {
        ItemIdentity guide = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{minecraft:custom_data=>{display:{Name:\"TerraFirmaGreg Guide\"},\"patchouli:book\":\"tfc:field_guide\"}}",
                1,
                1));
        ItemIdentity otherGuide = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{minecraft:custom_data=>{\"patchouli:book\":\"ae2:guide\"}}",
                1,
                1));

        assertEquals(ItemIdentity.exact("patchouli:guide_book", "patchouli:book=tfc:field_guide"), guide);
        assertFalse(ItemIdentityMatcher.matchesMovable(guide, otherGuide));
    }

    @Test
    void patchouliGuideBookSelectorCanLiveInsideComponentValueWrappers() {
        ItemIdentity guide = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{minecraft:custom_data=>CustomData[{display:{Name:\"TerraFirmaGreg Guide\"},\"patchouli:book\":\"tfc:field_guide\"}]}",
                1,
                1));
        ItemIdentity desired = ItemIdentity.exact(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"tfc:field_guide\"}");
        ItemIdentity otherGuide = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "patchouli:guide_book",
                "{minecraft:custom_data=>Optional[{\"patchouli:book\":\"ae2:guide\"}]}",
                1,
                1));

        assertEquals(ItemIdentity.exact("patchouli:guide_book", "patchouli:book=tfc:field_guide"), guide);
        assertTrue(ItemIdentityMatcher.matchesMovable(guide, desired));
        assertFalse(ItemIdentityMatcher.matchesMovable(guide, otherGuide));
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
    void tfcFoodStateCreatesItemOnlyMovableIdentities() {
        assertEquals(
                ItemIdentity.of("tfc:food/banana"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "tfc:food/banana",
                        "{tfc:food=>Food[creationDate=100,rotten=false,traits=[]]}",
                        4,
                        32)));
        assertEquals(
                ItemIdentity.of("tfc:food/banana"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "tfc:food/banana",
                        "{tfc:food=>Food[creationDate=200,rotten=false,traits=[preserved]]}",
                        4,
                        32)));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact(
                        "tfc:food/banana",
                        "{ForgeCaps:{\"tfc:food\":{creationDate:200,traits:[\"tfc:preserved\"]}}}"),
                ItemIdentity.exact(
                        "tfc:food/banana",
                        "{ForgeCaps:{\"tfc:food\":{creationDate:100,traits:[]}}}")));
    }

    @Test
    void mixedToolConditionFingerprintsNormalizeAsMovableCondition() {
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact(
                        "gtceu:steel_mining_hammer",
                        "{Damage:12,HideFlags:2,\"GT.Tool\":{MaxDamage:960}}"),
                ItemIdentity.exact(
                        "gtceu:steel_mining_hammer",
                        "{Damage:512,HideFlags:2,\"GT.Tool\":{MaxDamage:960}}")));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact(
                        "mod:component_tool",
                        "{minecraft:damage=>12,minecraft:tool=>{rules:[]}}"),
                ItemIdentity.of("mod:component_tool")));
    }

    @Test
    void toolTaggedStacksCreateItemOnlyMovableIdentities() {
        assertEquals(
                ItemIdentity.of("mod:odd_hammer"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "mod:odd_hammer",
                        "{Mode:\"dig\",Energy:400}",
                        1,
                        1).withTags("c:tools/hammer")));
        assertEquals(
                ItemIdentity.of("mod:vanilla_tagged_tool"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "mod:vanilla_tagged_tool",
                        "{Mode:\"dig\",Energy:400}",
                        1,
                        1).withTags("minecraft:pickaxes")));
    }

    @Test
    void toolStateFingerprintsCreateItemOnlyMovableIdentities() {
        assertEquals(
                ItemIdentity.of("gtceu:steel_mining_hammer"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "gtceu:steel_mining_hammer",
                        "{Damage:512,HideFlags:2,\"GT.Tool\":{MaxDamage:960}}",
                        1,
                        1)));
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
    void maxDamageOnlyStacksCreateItemOnlyMovableIdentities() {
        assertEquals(
                ItemIdentity.of("mod:stack_data_tool"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "mod:stack_data_tool",
                        "{Damage:512,Mode:\"wide\"}",
                        1,
                        1).maxDamage(960)));
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
