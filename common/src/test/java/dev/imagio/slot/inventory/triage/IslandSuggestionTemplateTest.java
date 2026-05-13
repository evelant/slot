package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandSuggestionTemplateTest {
    @Test
    void foodMatchesDescriptorWithFoodSignal() {
        assertTrue(IslandSuggestionTemplate.FOOD.matches(
                descriptor("minecraft:apple", Set.of(IslandSignal.FOOD), Set.of())
        ));
    }

    @Test
    void toolsMatchesDigger() {
        assertTrue(IslandSuggestionTemplate.TOOLS.matches(
                descriptor("minecraft:iron_pickaxe", Set.of(IslandSignal.DIGGER_TOOL), Set.of())
        ));
    }

    @Test
    void weaponsMatchesAnyWeaponClass() {
        assertTrue(IslandSuggestionTemplate.WEAPONS.matches(
                descriptor("minecraft:diamond_sword", Set.of(IslandSignal.SWORD), Set.of())
        ));
        assertTrue(IslandSuggestionTemplate.WEAPONS.matches(
                descriptor("minecraft:crossbow", Set.of(IslandSignal.CROSSBOW), Set.of())
        ));
        assertTrue(IslandSuggestionTemplate.WEAPONS.matches(
                descriptor("minecraft:trident", Set.of(IslandSignal.TRIDENT), Set.of())
        ));
    }

    @Test
    void armorMatchesAnyArmorSlot() {
        assertTrue(IslandSuggestionTemplate.ARMOR.matches(
                descriptor("minecraft:iron_helmet", Set.of(IslandSignal.ARMOR_HEAD), Set.of())
        ));
        assertTrue(IslandSuggestionTemplate.ARMOR.matches(
                descriptor("minecraft:diamond_chestplate", Set.of(IslandSignal.ARMOR_CHEST), Set.of())
        ));
    }

    @Test
    void materialsFamilyTagsRouteToDedicatedTemplates() {
        // c:ingots / c:gems / c:raw_materials / c:ores and stock families
        // split out into their own templates so common ingredients (sticks,
        // seeds, clay, mob drops) cluster separately from refined output.
        // Generic role=material remains the MATERIALS catch-all.
        assertTrue(IslandSuggestionTemplate.INGOTS.matches(
                descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots"))
        ));
        assertTrue(IslandSuggestionTemplate.INGOTS.matches(
                descriptor("minecraft:iron_ingot", Set.of(), Set.of("forge:ingots"))
        ));
        assertTrue(IslandSuggestionTemplate.INGOTS.matches(
                descriptor("tfc:metal/ingot/copper", Set.of(), Set.of("forge:ingots/copper"))
        ));
        assertTrue(IslandSuggestionTemplate.GEMS.matches(
                descriptor("minecraft:diamond", Set.of(), Set.of("c:gems"))
        ));
        assertTrue(IslandSuggestionTemplate.GEMS.matches(
                descriptor("minecraft:diamond", Set.of(), Set.of("forge:gems/diamond"))
        ));
        assertTrue(IslandSuggestionTemplate.RAW_MATERIALS.matches(
                descriptor("minecraft:raw_iron", Set.of(), Set.of("c:raw_materials"))
        ));
        assertTrue(IslandSuggestionTemplate.RAW_MATERIALS.matches(
                descriptor("tfc:ore/normal_native_copper", Set.of(), Set.of("forge:raw_materials/copper"))
        ));
        assertFalse(IslandSuggestionTemplate.INGOTS.matches(
                descriptor("modded:compressed_resource", Set.of(), Set.of("balm:ingots"))
        ));
        assertFalse(IslandSuggestionTemplate.MATERIALS.matches(
                descriptor("modded:wire", Set.of(), Set.of("mod:wires"))
        ));
        assertTrue(IslandSuggestionTemplate.WOOD.matches(
                roleDescriptor("minecraft:stick", "material")
        ));
    }

    @Test
    void materialPathSegmentsRouteForgeStyleCommodityItems() {
        assertEquals(IslandSuggestionTemplate.INGOTS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("tfc:metal/ingot/copper", "material")));
        assertEquals(IslandSuggestionTemplate.RAW_MATERIALS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("tfc:ore/normal_native_copper", "material")));
        assertEquals(IslandSuggestionTemplate.RAW_MATERIALS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("gtceu:purified_copper_ore", "material")));

        assertEquals(IslandSuggestionTemplate.UTILITY,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("tfc:ceramic/ingot_mold", "utility")));
    }

    @Test
    void woodStockRoutesToWoodWithoutStealingShapedOrToolItems() {
        assertEquals(IslandSuggestionTemplate.WOOD,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:oak_log", "natural_resource", "log")));
        assertEquals(IslandSuggestionTemplate.WOOD,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:stripped_oak_wood", "natural_resource", "wood")));
        assertEquals(IslandSuggestionTemplate.WOOD,
                IslandSuggestionTemplate.firstMatch(taggedFormDescriptor(
                        "minecraft:oak_planks", "building_block", "whole_block", Set.of("minecraft:planks"))));
        assertEquals(IslandSuggestionTemplate.WOOD,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("tfc:wood/lumber/oak", "material")));
        assertEquals(IslandSuggestionTemplate.WOOD,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("afc:wood/boards/baobab", "material")));

        assertEquals(IslandSuggestionTemplate.STAIRS,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:oak_stairs", "building_block", "stairs")));
        assertEquals(IslandSuggestionTemplate.DOORS,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:oak_door", "building_block", "door")));
        assertEquals(IslandSuggestionTemplate.TOOLS,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:wooden_pickaxe", "tool", "tool")));
        assertEquals(IslandSuggestionTemplate.MATERIALS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("modded:circuit_board", "material")));
    }

    @Test
    void seedCropPlantClayAndMobStockRouteToDedicatedTemplates() {
        assertEquals(IslandSuggestionTemplate.SEEDS,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:wheat_seeds", "natural_resource", "seed")));
        assertEquals(IslandSuggestionTemplate.SEEDS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:pitcher_pod", "natural_resource")));
        assertEquals(IslandSuggestionTemplate.MATERIALS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("modded:seed_oil", "material")));

        assertEquals(IslandSuggestionTemplate.CROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:wheat", "material")));
        assertEquals(IslandSuggestionTemplate.CROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:carrot", "consumable")));
        assertEquals(IslandSuggestionTemplate.FOOD,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:baked_potato", "consumable", "food_cooked")));
        assertEquals(IslandSuggestionTemplate.FOOD,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:pumpkin_pie", "consumable")));

        assertEquals(IslandSuggestionTemplate.PLANTS,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:oak_sapling", "natural_resource", "sapling")));
        assertEquals(IslandSuggestionTemplate.PLANTS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:dandelion", "natural_resource")));
        assertEquals(IslandSuggestionTemplate.NATURAL,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:grass_block", "natural_resource")));

        assertEquals(IslandSuggestionTemplate.CLAY_POTTERY,
                IslandSuggestionTemplate.firstMatch(materialDescriptor("minecraft:clay_ball", "material", null, "clay")));
        assertEquals(IslandSuggestionTemplate.CLAY_POTTERY,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:brick", "material")));
        assertEquals(IslandSuggestionTemplate.CLAY_POTTERY,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:decorated_pot", "decorative_block")));
        assertEquals(IslandSuggestionTemplate.BUILDING,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:stone_bricks", "building_block")));
        assertEquals(IslandSuggestionTemplate.STAIRS,
                IslandSuggestionTemplate.firstMatch(formDescriptor("minecraft:brick_stairs", "building_block", "stairs")));

        assertEquals(IslandSuggestionTemplate.MOB_DROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:string", "material")));
        assertEquals(IslandSuggestionTemplate.MOB_DROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:leather", "material")));
        assertEquals(IslandSuggestionTemplate.MOB_DROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:gunpowder", "material")));
        assertEquals(IslandSuggestionTemplate.MOB_DROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:ender_pearl", "utility")));
        assertEquals(IslandSuggestionTemplate.MOB_DROPS,
                IslandSuggestionTemplate.firstMatch(roleDescriptor("minecraft:rotten_flesh", "consumable")));
        assertFalse(IslandSuggestionTemplate.MOB_DROPS.matches(
                materialDescriptor("minecraft:white_wool", "material", "whole_block", "wool")
        ));
    }

    @Test
    void storageMatchesChestsAndShulkers() {
        assertTrue(IslandSuggestionTemplate.STORAGE.matches(
                descriptor("minecraft:chest", Set.of(), Set.of("c:chests"))
        ));
        assertTrue(IslandSuggestionTemplate.STORAGE.matches(
                descriptor("minecraft:shulker_box", Set.of(), Set.of("c:shulker_boxes"))
        ));
        assertFalse(IslandSuggestionTemplate.STORAGE.matches(
                descriptor("minecraft:chest_minecart", Set.of(), Set.of())
        ));
    }

    @Test
    void templatesDoNotMatchEmptyDescriptor() {
        IslandSignalDescriptor empty = IslandSignalDescriptor.empty(ItemIdentity.of("minecraft:stone"));
        for (IslandSuggestionTemplate template : IslandSuggestionTemplate.values()) {
            assertFalse(template.matches(empty), template + " should not fire for empty descriptor");
        }
    }

    @Test
    void roleFiresMatchingTemplateEvenWithoutClassOrTagSignals() {
        assertTrue(IslandSuggestionTemplate.TOOLS.matches(
                roleDescriptor("modded:laser_drill", "tool")));
        assertTrue(IslandSuggestionTemplate.WEAPONS.matches(
                roleDescriptor("modded:plasma_rifle", "weapon")));
        assertTrue(IslandSuggestionTemplate.WEAPONS.matches(
                roleDescriptor("minecraft:arrow", "ammunition")));
        assertTrue(IslandSuggestionTemplate.ARMOR.matches(
                roleDescriptor("modded:exo_helmet", "armor")));
        assertTrue(IslandSuggestionTemplate.FOOD.matches(
                roleDescriptor("modded:protein_bar", "consumable")));
        assertTrue(IslandSuggestionTemplate.NATURAL.matches(
                roleDescriptor("modded:bauxite_ore", "natural_resource")));
        assertTrue(IslandSuggestionTemplate.STORAGE.matches(
                roleDescriptor("modded:magic_crate", "storage_block")));
        assertTrue(IslandSuggestionTemplate.STORAGE.matches(
                roleDescriptor("modded:travel_bag", "container_portable")));
    }

    @Test
    void allClassifiedRolesNowResolveToATemplate() {
        // Pre-expansion, utility / curiosity / transport / trophy / admin
        // were unmapped — every chip-eligible item with one of these
        // roles got no chip. After Phase 4a's coverage lift, each role
        // routes to a meaningful catch-all template.
        java.util.Map<String, IslandSuggestionTemplate> expected = java.util.Map.of(
                "transport", IslandSuggestionTemplate.TRANSPORT,
                "utility", IslandSuggestionTemplate.UTILITY,
                "curiosity", IslandSuggestionTemplate.CURIOSITY,
                "trophy", IslandSuggestionTemplate.CURIOSITY,
                "admin", IslandSuggestionTemplate.MISC
        );
        for (java.util.Map.Entry<String, IslandSuggestionTemplate> e : expected.entrySet()) {
            IslandSignalDescriptor descriptor = roleDescriptor("modded:" + e.getKey() + "_thing", e.getKey());
            assertEquals(e.getValue(), IslandSuggestionTemplate.firstMatch(descriptor),
                    "role " + e.getKey() + " should map to " + e.getValue());
        }
    }

    @Test
    void roleOnlyTemplatesFireForPureRoleDescriptors() {
        // Vanilla items like cracked_stone_bricks / stone_stairs / torch /
        // furnace / lantern / redstone_torch have no class subclass and no
        // useful c:* tag membership — only the role facet routes them.
        assertTrue(IslandSuggestionTemplate.BUILDING.matches(
                roleDescriptor("minecraft:cracked_stone_bricks", "building_block")));
        assertTrue(IslandSuggestionTemplate.BUILDING.matches(
                roleDescriptor("minecraft:stone_stairs", "building_block")));
        assertTrue(IslandSuggestionTemplate.DECORATION.matches(
                roleDescriptor("minecraft:lantern", "decorative_block")));
        assertTrue(IslandSuggestionTemplate.NATURAL.matches(
                roleDescriptor("minecraft:dirt", "natural_resource")));
        assertTrue(IslandSuggestionTemplate.WORKBENCHES.matches(
                roleDescriptor("minecraft:furnace", "functional_block")));
        assertTrue(IslandSuggestionTemplate.WORKBENCHES.matches(
                roleDescriptor("minecraft:torch", "functional_block")));
        assertTrue(IslandSuggestionTemplate.REDSTONE.matches(
                roleDescriptor("minecraft:redstone_torch", "redstone_component")));
        assertTrue(IslandSuggestionTemplate.MECHANISMS.matches(
                roleDescriptor("modded:cogwheel", "mechanism")));
        assertTrue(IslandSuggestionTemplate.UPGRADES.matches(
                roleDescriptor("modded:tool_battery", "upgrade")));
    }

    @Test
    void formKeyedTemplatesFireFromFormFacetAlone() {
        // form-keyed templates (STAIRS, SLABS, etc.) route on form
        // regardless of role — a stair is a stair. They're declared
        // BEFORE the catch-all BUILDING so the more specific form
        // match wins.
        IslandSignalDescriptor stair = formDescriptor("minecraft:oak_stairs", "building_block", "stairs");
        assertEquals(IslandSuggestionTemplate.STAIRS,
                IslandSuggestionTemplate.firstMatch(stair));

        IslandSignalDescriptor slab = formDescriptor("minecraft:oak_slab", "building_block", "slab");
        assertEquals(IslandSuggestionTemplate.SLABS,
                IslandSuggestionTemplate.firstMatch(slab));

        IslandSignalDescriptor wall = formDescriptor("minecraft:cobblestone_wall", "building_block", "wall");
        assertEquals(IslandSuggestionTemplate.WALLS,
                IslandSuggestionTemplate.firstMatch(wall));

        IslandSignalDescriptor door = formDescriptor("minecraft:oak_door", "building_block", "door");
        assertEquals(IslandSuggestionTemplate.DOORS,
                IslandSuggestionTemplate.firstMatch(door));

        IslandSignalDescriptor pane = formDescriptor("minecraft:glass_pane", "building_block", "pane");
        assertEquals(IslandSuggestionTemplate.WINDOWS,
                IslandSuggestionTemplate.firstMatch(pane));

        // Plain structural blocks (no form facet) still route to BUILDING.
        IslandSignalDescriptor stone = roleDescriptor("minecraft:stone", "building_block");
        assertEquals(IslandSuggestionTemplate.BUILDING,
                IslandSuggestionTemplate.firstMatch(stone));
    }

    private static IslandSignalDescriptor formDescriptor(String itemId, String role, String form) {
        return taggedFormDescriptor(itemId, role, form, Set.of());
    }

    private static IslandSignalDescriptor materialDescriptor(
            String itemId,
            String role,
            String form,
            String materialFamily
    ) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                "",
                role,
                null,
                materialFamily,
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                null, null, null, null, null,
                java.util.List.of(),
                form,
                false
        );
    }

    private static IslandSignalDescriptor taggedFormDescriptor(String itemId, String role, String form, Set<String> tags) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                tags,
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                "",
                role,
                null,
                null,
                java.util.List.of(),
                java.util.List.of(),
                null, null, null, null, null,
                java.util.List.of(),
                form,
                false
        );
    }

    @Test
    void roleAndClassSignalAgreeOnSameTemplate() {
        // Vanilla diamond_pickaxe: DiggerItem class signal + role "tool"
        // — both routes pick the same template.
        IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                ItemIdentity.of("minecraft:diamond_pickaxe"),
                Set.of(IslandSignal.DIGGER_TOOL),
                Set.of(),
                "minecraft",
                "",
                "tool"
        );
        assertTrue(IslandSuggestionTemplate.TOOLS.matches(descriptor));
    }

    @Test
    void roleSurvivesAbsentClassAndTagSignals() {
        // Datapack/KubeJS items typically lack subclass + tag membership;
        // FacetIndex is the only path that can route them.
        IslandSignalDescriptor descriptor = roleDescriptor("kubejs:custom_pickaxe", "tool");
        assertTrue(descriptor.classSignals().isEmpty());
        assertTrue(descriptor.itemTags().isEmpty());
        assertTrue(descriptor.role() != null);
        assertTrue(IslandSuggestionTemplate.TOOLS.matches(descriptor));
    }

    private static IslandSignalDescriptor descriptor(
            String itemId,
            Set<IslandSignal> signals,
            Set<String> tags
    ) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                signals,
                tags,
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                ""
        );
    }

    private static IslandSignalDescriptor roleDescriptor(String itemId, String role) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                "",
                role
        );
    }
}
