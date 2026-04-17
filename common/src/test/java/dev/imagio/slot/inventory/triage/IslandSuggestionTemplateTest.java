package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
    void materialsMatchesKnownTagsOnly() {
        assertTrue(IslandSuggestionTemplate.MATERIALS.matches(
                descriptor("minecraft:iron_ingot", Set.of(), Set.of("c:ingots"))
        ));
        assertTrue(IslandSuggestionTemplate.MATERIALS.matches(
                descriptor("minecraft:diamond", Set.of(), Set.of("c:gems"))
        ));
        assertFalse(IslandSuggestionTemplate.MATERIALS.matches(
                descriptor("modded:wire", Set.of(), Set.of("mod:wires"))
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
}
