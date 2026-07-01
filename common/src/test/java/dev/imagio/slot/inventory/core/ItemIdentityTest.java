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
    void memoEvictsEldestEntriesInsteadOfClearingAllCachedIdentities() {
        ItemIdentityMatcher.Memo memo = new ItemIdentityMatcher.Memo();

        for (int i = 0; i < 4_097; i++) {
            int index = i;
            ItemIdentityMatcher.withMemo(memo, () -> ItemIdentityMatcher.create(
                    new net.minecraft.world.item.ItemStack(
                            "mod:component_sensitive_item",
                            "{Unique:" + index + "}",
                            1,
                            64)));
        }
        for (int i = 0; i < 4_097; i++) {
            int index = i;
            ItemIdentityMatcher.withMemo(memo, () -> ItemIdentityMatcher.normalizeMovable(
                    ItemIdentity.exact("minecraft:diamond_pickaxe", "{Damage:" + index + "}")));
        }

        ItemIdentityMatcher.MemoStats stats = memo.stats();
        assertEquals(4_097, stats.createMisses());
        assertEquals(4_096, stats.createCacheSize());
        assertEquals(1, stats.createEvictions());
        assertEquals(4_097, stats.normalizeMisses());
        assertEquals(4_096, stats.normalizeCacheSize());
        assertEquals(1, stats.normalizeEvictions());

        ItemIdentityMatcher.withMemo(memo, () -> ItemIdentityMatcher.create(
                new net.minecraft.world.item.ItemStack(
                        "mod:component_sensitive_item",
                        "{Unique:4096}",
                        1,
                        64)));
        ItemIdentityMatcher.withMemo(memo, () -> ItemIdentityMatcher.normalizeMovable(
                ItemIdentity.exact("minecraft:diamond_pickaxe", "{Damage:4096}")));

        ItemIdentityMatcher.MemoStats afterHit = memo.stats();
        assertEquals(1, afterHit.createHits());
        assertEquals(1, afterHit.normalizeHits());
        assertEquals(4_096, afterHit.createCacheSize());
        assertEquals(4_096, afterHit.normalizeCacheSize());
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
    void gregTechElectricChargeStateCreatesItemOnlyMovableIdentities() {
        ItemIdentity lowCharge = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:lithium_battery",
                "{Charge:400L}",
                1,
                1));
        ItemIdentity highCharge = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:lithium_battery",
                "{Charge:1200L,MaxCharge:10000L}",
                1,
                1));
        ItemIdentity dischargeMode = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:lithium_battery",
                "{Charge:1200L,DischargeMode:1b}",
                1,
                1));
        ItemIdentity infinite = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:zero_point_module",
                "{Infinite:1b}",
                1,
                1));

        assertEquals(ItemIdentity.of("gtceu:lithium_battery"), lowCharge);
        assertEquals(lowCharge, highCharge);
        assertEquals(lowCharge, dischargeMode);
        assertEquals(ItemIdentity.of("gtceu:zero_point_module"), infinite);
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact("gtceu:lithium_battery", "{Charge:200L}"),
                ItemIdentity.exact("gtceu:lithium_battery", "{Charge:1800L,MaxCharge:10000L}")));
    }

    @Test
    void gregTechElectricChargeInsideCustomDataWrapperNormalizesAsConditionOnly() {
        assertEquals(
                ItemIdentity.of("gtceu:lithium_battery"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "gtceu:lithium_battery",
                        "{minecraft:custom_data=>CustomData[{Charge:400L,MaxCharge:10000L}]}",
                        1,
                        1)));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact(
                        "gtceu:lithium_battery",
                        "{minecraft:custom_data=>CustomData[{Charge:400L}]}"),
                ItemIdentity.exact(
                        "gtceu:lithium_battery",
                        "{minecraft:custom_data=>CustomData[{Charge:1200L}]}")));
    }

    @Test
    void gregTechFluidContainersPreserveFluidContentsBeforePortableContainerNormalization() {
        PortableContainerClassifiers.register(stack -> "gtceu:steel_drum".equals(stack.itemId())
                || "gtceu:basic_super_tank".equals(stack.itemId()));

        ItemIdentity emptyDrum = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:steel_drum",
                1,
                1));
        ItemIdentity waterDrum = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:steel_drum",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:16000}}",
                1,
                1));
        ItemIdentity lessWaterDrum = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:steel_drum",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:1000}}",
                1,
                1));
        ItemIdentity lavaDrum = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:steel_drum",
                "{Fluid:{FluidName:\"minecraft:lava\",Amount:16000}}",
                1,
                1));
        ItemIdentity oilTank = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "gtceu:basic_super_tank",
                "{stored:{FluidName:\"gtceu:raw_oil\",Amount:2147483647},storedAmount:5000000000L}",
                1,
                1));

        assertEquals(ItemIdentity.of("gtceu:steel_drum"), emptyDrum);
        assertEquals(ItemIdentity.exact("gtceu:steel_drum", "fluid=minecraft:water"), waterDrum);
        assertEquals(waterDrum, lessWaterDrum);
        assertEquals(ItemIdentity.exact("gtceu:steel_drum", "fluid=minecraft:lava"), lavaDrum);
        assertEquals(ItemIdentity.exact("gtceu:basic_super_tank", "fluid=gtceu:raw_oil"), oilTank);
        assertFalse(ItemIdentityMatcher.matchesMovable(waterDrum, lavaDrum));
        assertFalse(ItemIdentityMatcher.usesItemOnlyMovableIdentity(new net.minecraft.world.item.ItemStack(
                "gtceu:steel_drum",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:16000}}",
                1,
                1)));
    }

    @Test
    void forgeFluidHandlerItemStackContainersPreserveFluidContentsBeforePortableContainerNormalization() {
        PortableContainerClassifiers.register(stack -> "waterflasks:iron_flask".equals(stack.itemId()));

        ItemIdentity emptyFlask = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "waterflasks:iron_flask",
                1,
                1));
        ItemIdentity waterFlask = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "waterflasks:iron_flask",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:2000}}",
                1,
                1));
        ItemIdentity lessWaterFlask = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "waterflasks:iron_flask",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:100}}",
                1,
                1));
        ItemIdentity lavaFlask = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "waterflasks:iron_flask",
                "{Fluid:{FluidName:\"minecraft:lava\",Amount:2000}}",
                1,
                1));

        assertEquals(ItemIdentity.of("waterflasks:iron_flask"), emptyFlask);
        assertEquals(ItemIdentity.exact("waterflasks:iron_flask", "fluid=minecraft:water"), waterFlask);
        assertEquals(waterFlask, lessWaterFlask);
        assertEquals(ItemIdentity.exact("waterflasks:iron_flask", "fluid=minecraft:lava"), lavaFlask);
        assertFalse(ItemIdentityMatcher.matchesMovable(waterFlask, lavaFlask));
        assertFalse(ItemIdentityMatcher.usesItemOnlyMovableIdentity(new net.minecraft.world.item.ItemStack(
                "waterflasks:iron_flask",
                "{Fluid:{FluidName:\"minecraft:water\",Amount:2000}}",
                1,
                1)));
    }

    @Test
    void gregTechFluidContainersInsideCustomDataWrapperPreserveFluidContents() {
        PortableContainerClassifiers.register(stack -> "gtceu:basic_super_tank".equals(stack.itemId()));

        assertEquals(
                ItemIdentity.exact("gtceu:basic_super_tank", "fluid=minecraft:water"),
                ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                        "gtceu:basic_super_tank",
                        "{minecraft:custom_data=>CustomData[{Fluid:{FluidName:\"minecraft:water\",Amount:16000}}]}",
                        1,
                        1)));
        assertTrue(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact(
                        "gtceu:basic_super_tank",
                        "{minecraft:custom_data=>CustomData[{Fluid:{FluidName:\"minecraft:water\",Amount:16000}}]}"),
                ItemIdentity.exact(
                        "gtceu:basic_super_tank",
                        "{minecraft:custom_data=>CustomData[{Fluid:{FluidName:\"minecraft:water\",Amount:1000}}]}")));
        assertFalse(ItemIdentityMatcher.matchesMovable(
                ItemIdentity.exact(
                        "gtceu:basic_super_tank",
                        "{minecraft:custom_data=>CustomData[{Fluid:{FluidName:\"minecraft:water\",Amount:16000}}]}"),
                ItemIdentity.exact(
                        "gtceu:basic_super_tank",
                        "{minecraft:custom_data=>CustomData[{Fluid:{FluidName:\"minecraft:lava\",Amount:16000}}]}")));
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
    void registeredPortableContainerIgnoresLunchboxEnergyAndContentsState() {
        PortableContainerClassifiers.register(stack -> "tfclunchbox:electric_lunchbox".equals(stack.itemId()));

        ItemIdentity charged = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "tfclunchbox:electric_lunchbox",
                "{Energy:1200,LunchboxUUID:\"first\",Items:[{Slot:0b,id:\"minecraft:apple\",Count:2b}],IsOpen:1b,ForgeCaps:{}}",
                1,
                1));
        ItemIdentity drained = ItemIdentityMatcher.create(new net.minecraft.world.item.ItemStack(
                "tfclunchbox:electric_lunchbox",
                "{Energy:400,LunchboxUUID:\"second\",Items:[{Slot:0b,id:\"minecraft:carrot\",Count:1b}],IsOpen:0b,ForgeCaps:{}}",
                1,
                1));

        assertEquals(ItemIdentity.of("tfclunchbox:electric_lunchbox"), charged);
        assertEquals(charged, drained);
        assertTrue(ItemIdentityMatcher.matchesMovable(charged, drained));
    }

    @Test
    void persistedPortableContainerStateNormalizesWithoutLiveStackCapability() {
        ItemIdentity first = ItemIdentity.exact(
                "tfclunchbox:electric_lunchbox",
                "{Energy:1200,LunchboxUUID:\"first\",Items:[{Slot:0b,id:\"minecraft:apple\",Count:2b}],IsOpen:1b,ForgeCaps:{}}");
        ItemIdentity second = ItemIdentity.exact(
                "tfclunchbox:electric_lunchbox",
                "{Energy:400,LunchboxUUID:\"second\",Items:[{Slot:0b,id:\"minecraft:carrot\",Count:1b}],IsOpen:0b,ForgeCaps:{}}");

        assertEquals(ItemIdentity.of("tfclunchbox:electric_lunchbox"), ItemIdentityMatcher.normalizeMovable(first));
        assertEquals(ItemIdentityMatcher.normalizeMovable(first), ItemIdentityMatcher.normalizeMovable(second));
    }

    @Test
    void poweredModeDataDoesNotNormalizeAsPortableContainerState() {
        ItemIdentity configured = ItemIdentity.exact(
                "mod:configurable_gadget",
                "{Energy:1200,Mode:\"wide\"}");
        ItemIdentity chargedMode = ItemIdentity.exact(
                "mod:configurable_gadget",
                "{Charge:1200,Mode:\"wide\"}");
        ItemIdentity customDataMode = ItemIdentity.exact(
                "mod:configurable_gadget",
                "{minecraft:custom_data=>CustomData[{Charge:1200,Mode:\"wide\"}]}");

        assertEquals(configured, ItemIdentityMatcher.normalizeMovable(configured));
        assertEquals(chargedMode, ItemIdentityMatcher.normalizeMovable(chargedMode));
        assertEquals(customDataMode, ItemIdentityMatcher.normalizeMovable(customDataMode));
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
