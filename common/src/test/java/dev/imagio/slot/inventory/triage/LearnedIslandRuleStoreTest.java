package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LearnedIslandRuleStoreTest {
    @Test
    void singleConfirmationDoesNotFire() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(
                descriptor("minecraft:iron_ingot", Set.of("c:ingots")),
                "island.mining",
                10L
        );

        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("minecraft:gold_ingot", Set.of("c:ingots"))
        );

        assertTrue(firing.isEmpty());
    }

    @Test
    void twoDistinctIdentitiesFireLearnedRule() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(
                descriptor("minecraft:iron_ingot", Set.of("c:ingots")),
                "island.mining",
                10L
        );
        store.recordAssignment(
                descriptor("minecraft:gold_ingot", Set.of("c:ingots")),
                "island.mining",
                20L
        );

        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("minecraft:copper_ingot", Set.of("c:ingots"))
        );

        assertEquals(1, firing.size());
        assertEquals("island.mining", firing.get(0).islandId());
        assertEquals(LearnedAdjacencyKey.Kind.TAG, firing.get(0).adjacency().kind());
        assertEquals("c:ingots", firing.get(0).adjacency().value());
    }

    @Test
    void sameItemRecordedTwiceDoesNotFireForThatIdentity() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(
                descriptor("minecraft:iron_ingot", Set.of("c:ingots")),
                "island.mining",
                10L
        );
        store.recordAssignment(
                descriptor("minecraft:iron_ingot", Set.of("c:ingots")),
                "island.mining",
                20L
        );

        assertTrue(store.firingRulesFor(descriptor("minecraft:iron_ingot", Set.of("c:ingots"))).isEmpty());
    }

    @Test
    void learnedRuleFiresOnceTwoOthersConfirmEvenIfCurrentItemAlsoPresent() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(descriptor("minecraft:iron_ingot", Set.of("c:ingots")), "island.mining", 10L);
        store.recordAssignment(descriptor("minecraft:gold_ingot", Set.of("c:ingots")), "island.mining", 20L);
        store.recordAssignment(descriptor("minecraft:copper_ingot", Set.of("c:ingots")), "island.mining", 30L);

        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("minecraft:copper_ingot", Set.of("c:ingots"))
        );
        assertEquals(1, firing.size());
    }

    @Test
    void tagRuleWinsOverNamespaceWhenSameIsland() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(descriptor("modded:iron_ingot", Set.of("c:ingots")), "island.mining", 10L);
        store.recordAssignment(descriptor("modded:gold_ingot", Set.of("c:ingots")), "island.mining", 20L);
        store.recordAssignment(descriptor("modded:stone", Set.of()), "island.mining", 5L);
        store.recordAssignment(descriptor("modded:dirt", Set.of()), "island.mining", 7L);

        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("modded:copper_ingot", Set.of("c:ingots"))
        );

        assertEquals(1, firing.size());
        assertEquals(LearnedAdjacencyKey.Kind.TAG, firing.get(0).adjacency().kind());
    }

    @Test
    void minecraftNamespaceDoesNotFireAcrossUnrelatedItems() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(descriptor("minecraft:oak_planks", Set.of("minecraft:planks")), "island.wood", 1L);
        store.recordAssignment(descriptor("minecraft:birch_planks", Set.of("minecraft:planks")), "island.wood", 2L);

        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("minecraft:azure_bluet", Set.of("minecraft:small_flowers"))
        );

        assertTrue(firing.isEmpty(), "broad minecraft namespace must not match flowers to a wood island");
    }

    @Test
    void separateIslandsEachReturnOwnRule() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(descriptor("minecraft:iron_ingot", Set.of("c:ingots")), "island.mining", 10L);
        store.recordAssignment(descriptor("minecraft:gold_ingot", Set.of("c:ingots")), "island.mining", 20L);
        store.recordAssignment(descriptor("modded:steel_ingot", Set.of("c:ingots")), "island.industry", 30L);
        store.recordAssignment(descriptor("modded:copper_plate", Set.of("c:ingots")), "island.industry", 40L);

        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("modded:tin_ingot", Set.of("c:ingots"))
        );

        assertEquals(2, firing.size());
    }

    @Test
    void customThresholdHonored() {
        LearnedIslandRuleStore store = new LearnedIslandRuleStore(3);
        store.recordAssignment(descriptor("minecraft:iron_ingot", Set.of("c:ingots")), "island.mining", 10L);
        store.recordAssignment(descriptor("minecraft:gold_ingot", Set.of("c:ingots")), "island.mining", 20L);

        assertTrue(store.firingRulesFor(descriptor("minecraft:copper_ingot", Set.of("c:ingots"))).isEmpty());

        store.recordAssignment(descriptor("minecraft:copper_ingot", Set.of("c:ingots")), "island.mining", 30L);
        List<LearnedIslandRule> firing = store.firingRulesFor(
                descriptor("minecraft:netherite_ingot", Set.of("c:ingots"))
        );
        assertEquals(1, firing.size());
    }

    @Test
    void materialFamilyAdjacencyFiresAcrossShapeVariants() {
        // The motivating case: player has homed wood-family items into a
        // custom "Wood" island. Item tags differ across shape variants
        // (planks/log/stairs each carry distinct tag sets), but they all
        // share material_family=wood_birch via FacetIndex. Two
        // confirmations on wood_birch are enough to fire a learned rule
        // for a third wood_birch item — even if its item-tag set has
        // nothing in common with the first two.
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(woodDescriptor("minecraft:birch_planks", "wood_birch", Set.of("minecraft:planks")), "island.wood", 10L);
        store.recordAssignment(woodDescriptor("minecraft:birch_stairs", "wood_birch", Set.of("minecraft:wooden_stairs")), "island.wood", 20L);

        List<LearnedIslandRule> firing = store.firingRulesFor(
                woodDescriptor("minecraft:birch_wood", "wood_birch", Set.of("minecraft:logs"))
        );

        assertEquals(1, firing.size());
        assertEquals("island.wood", firing.get(0).islandId());
        assertEquals(LearnedAdjacencyKey.Kind.MATERIAL_FAMILY, firing.get(0).adjacency().kind());
    }

    @Test
    void materialFamilyKeyIsDistinctFromTagOfTheSameValue() {
        // Defensive: if a tag id ever literally equals a material_family
        // value (e.g. "iron"), the two should still partition cleanly.
        LearnedIslandRuleStore store = new LearnedIslandRuleStore();
        store.recordAssignment(
                new IslandSignalDescriptor(
                        ItemIdentity.of("minecraft:iron_ingot"),
                        Set.of(),
                        Set.of("iron"),
                        "minecraft", "", null, null),
                "island.tag-only", 10L);
        store.recordAssignment(
                new IslandSignalDescriptor(
                        ItemIdentity.of("minecraft:iron_block"),
                        Set.of(),
                        Set.of("iron"),
                        "minecraft", "", null, null),
                "island.tag-only", 20L);

        // A descriptor that only carries material_family=iron (no tag)
        // must NOT collide with the tag-only learnings above.
        List<LearnedIslandRule> firing = store.firingRulesFor(
                new IslandSignalDescriptor(
                        ItemIdentity.of("minecraft:raw_iron"),
                        Set.of(),
                        Set.of(),
                        "minecraft", "", null, "iron"));
        assertTrue(firing.isEmpty(),
                "MATERIAL_FAMILY:iron should not match against learnings recorded under TAG:iron");
    }

    private static IslandSignalDescriptor descriptor(String itemId, Set<String> tags) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                tags,
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                ""
        );
    }

    private static IslandSignalDescriptor woodDescriptor(String itemId, String materialFamily, Set<String> tags) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                tags,
                itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : "",
                "",
                "building_block",
                materialFamily
        );
    }
}
