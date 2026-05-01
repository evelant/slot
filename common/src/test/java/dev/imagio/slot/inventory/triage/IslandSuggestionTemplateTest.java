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
        // c:ingots / c:gems / c:raw_materials / c:ores split out into
        // their own templates (INGOTS / GEMS / RAW_MATERIALS) so common
        // ingredients (sticks, leather) cluster separately from refined
        // output. Generic role=material is the MATERIALS catch-all.
        assertTrue(IslandSuggestionTemplate.INGOTS.matches(
                descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots"))
        ));
        assertTrue(IslandSuggestionTemplate.GEMS.matches(
                descriptor("minecraft:diamond", Set.of(), Set.of("c:gems"))
        ));
        assertTrue(IslandSuggestionTemplate.RAW_MATERIALS.matches(
                descriptor("minecraft:raw_iron", Set.of(), Set.of("c:raw_materials"))
        ));
        assertFalse(IslandSuggestionTemplate.MATERIALS.matches(
                descriptor("modded:wire", Set.of(), Set.of("mod:wires"))
        ));
        assertTrue(IslandSuggestionTemplate.MATERIALS.matches(
                roleDescriptor("minecraft:stick", "material")
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
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
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
