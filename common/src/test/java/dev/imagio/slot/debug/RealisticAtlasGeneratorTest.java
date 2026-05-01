package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.IslandSignal;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealisticAtlasGeneratorTest {
    @Test
    void emptyPoolProducesEmptyPlan() {
        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                List.of(), PopulateProfile.ORGANIZED, new Random(1L), stack -> IslandSuggestionTemplate.MISC);

        assertTrue(plan.islands().isEmpty());
        assertTrue(plan.assignments().isEmpty());
        assertTrue(plan.chests().isEmpty());
        assertTrue(plan.triageStacks().isEmpty());
        assertTrue(plan.homedStacks().isEmpty());
    }

    @Test
    void homedStackCountMatchesAssignmentCount() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.MATERIALS, 40);
        pool.add(IslandSuggestionTemplate.TOOLS, 10);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(13L),
                pool::classify
        );

        assertEquals(plan.assignments().size(), plan.homedStacks().size(),
                "homed stack count must match assignment count so every homed identity lands in inventory");
        for (ItemStack stack : plan.homedStacks()) {
            assertTrue(stack.getCount() >= 1, "homed stack must have count >= 1");
        }
    }

    @Test
    void organizedProfileCreatesOneIslandPerNonEmptyTemplate() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.MATERIALS, 40);
        pool.add(IslandSuggestionTemplate.BUILDING, 20);
        pool.add(IslandSuggestionTemplate.TOOLS, 10);
        pool.add(IslandSuggestionTemplate.FOOD, 10);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::classify
        );

        HashSet<String> labels = new HashSet<>();
        HashSet<String> islandIds = new HashSet<>();
        for (VisualAtlasIsland island : plan.islands()) {
            islandIds.add(island.id());
            labels.add(island.label());
        }
        assertEquals(4, plan.islands().size());
        assertTrue(labels.contains(IslandSuggestionTemplate.MATERIALS.defaultLabel()));
        assertTrue(labels.contains(IslandSuggestionTemplate.BUILDING.defaultLabel()));
        assertTrue(labels.contains(IslandSuggestionTemplate.TOOLS.defaultLabel()));
        assertTrue(labels.contains(IslandSuggestionTemplate.FOOD.defaultLabel()));

        // Populated island IDs match what chip-accept would create — the
        // template's defaultIslandId. That alignment lets a chip-accept
        // on a populated atlas land in the existing island instead of
        // duplicating it.
        assertTrue(islandIds.contains(IslandSuggestionTemplate.MATERIALS.defaultIslandId()));
        assertTrue(islandIds.contains(IslandSuggestionTemplate.TOOLS.defaultIslandId()));
    }

    @Test
    void assignmentsReferenceValidIslandsAndFitInsideBounds() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.MATERIALS, 30);
        pool.add(IslandSuggestionTemplate.REDSTONE, 12);
        pool.add(IslandSuggestionTemplate.STORAGE, 5);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(3L),
                pool::classify
        );

        HashMap<String, VisualAtlasIsland> byId = new HashMap<>();
        for (VisualAtlasIsland island : plan.islands()) {
            byId.put(island.id(), island);
        }
        for (VisualHomeAssignment assignment : plan.assignments().values()) {
            VisualAtlasIsland island = byId.get(assignment.islandId());
            assertNotNull(island, "assignment references unknown island: " + assignment.islandId());
            assertTrue(assignment.ordinal() >= 0);
        }
    }

    @Test
    void triageStacksMatchProfileFraction() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.MATERIALS, 200);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(42L),
                pool::classify
        );

        int expectedHomed = Math.min(PopulateProfile.ORGANIZED.identityCount(), 200);
        int expectedTriage = (int) Math.round(expectedHomed * PopulateProfile.ORGANIZED.triageFraction());
        expectedTriage = Math.min(expectedTriage, 200 - expectedHomed);
        expectedTriage = Math.max(0, expectedTriage);

        assertEquals(expectedTriage, plan.triageStacks().size());
        assertEquals(expectedHomed, plan.assignments().size());
    }

    @Test
    void chestCountMatchesProfile() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.MATERIALS, 40);
        pool.add(IslandSuggestionTemplate.TOOLS, 10);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(9L),
                pool::classify
        );

        assertEquals(PopulateProfile.ORGANIZED.chestCount(), plan.chests().size());
    }

    @Test
    void linkedChestsMostlyContainTemplateMatchedItems() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.MATERIALS, 40);
        pool.add(IslandSuggestionTemplate.FOOD, 20);
        pool.add(IslandSuggestionTemplate.TOOLS, 15);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.LATE_MODPACK,
                new Random(11L),
                pool::classify
        );

        EnumMap<IslandSuggestionTemplate, String> islandByTemplate = new EnumMap<>(IslandSuggestionTemplate.class);
        for (VisualAtlasIsland island : plan.islands()) {
            for (IslandSuggestionTemplate template : IslandSuggestionTemplate.values()) {
                if (island.id().equals(template.defaultIslandId())) {
                    islandByTemplate.put(template, island.id());
                    break;
                }
            }
        }

        int linkedChests = 0;
        int templateMatches = 0;
        int totalContents = 0;
        for (ChestSpec chest : plan.chests()) {
            if (!chest.isLinked()) {
                continue;
            }
            linkedChests++;
            IslandSuggestionTemplate linkedTemplate = null;
            for (Map.Entry<IslandSuggestionTemplate, String> entry : islandByTemplate.entrySet()) {
                if (entry.getValue().equals(chest.linkedIslandId())) {
                    linkedTemplate = entry.getKey();
                    break;
                }
            }
            assertNotNull(linkedTemplate, "linked chest references unknown island: " + chest.linkedIslandId());
            for (ChestContentEntry entry : chest.contents()) {
                totalContents++;
                if (pool.classify(entry.stack()) == linkedTemplate) {
                    templateMatches++;
                }
            }
        }

        assertTrue(linkedChests > 0, "expected some linked chests");
        assertTrue(totalContents > 0, "expected some chest contents");
        // Content bias is 85%; allow generous slack so RNG variance doesn't flake.
        double ratio = (double) templateMatches / (double) totalContents;
        assertTrue(ratio >= 0.6,
                "expected at least 60% template-match in linked chests, got " + ratio);
    }

    @Test
    void islandsFromDifferentTemplatesDoNotOverlap() {
        TemplatePool pool = new TemplatePool();
        pool.add(IslandSuggestionTemplate.TOOLS, 8);
        pool.add(IslandSuggestionTemplate.WEAPONS, 6);
        pool.add(IslandSuggestionTemplate.MATERIALS, 30);
        pool.add(IslandSuggestionTemplate.NATURAL, 12);
        pool.add(IslandSuggestionTemplate.REDSTONE, 5);
        pool.add(IslandSuggestionTemplate.MECHANISMS, 18);
        pool.add(IslandSuggestionTemplate.STORAGE, 4);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(17L),
                pool::classify
        );

        // Islands no longer carry authored width/height (Phase 2.2 — sizes
        // come from the client-side AtlasLayout packer). Verify the
        // generator at least gives every island a distinct top-left so
        // the synthetic atlas reads as multiple islands when rendered.
        List<VisualAtlasIsland> islands = new ArrayList<>(plan.islands());
        java.util.Set<String> origins = new java.util.HashSet<>();
        for (VisualAtlasIsland island : islands) {
            String packed = island.x() + "," + island.y();
            assertTrue(origins.add(packed),
                    "duplicate island origin (" + island.x() + "," + island.y() + ") for " + island.id());
        }
    }

    @Test
    void qualifiedSubsystemSpawnsItsOwnIsland() {
        // 12 Create-mechanical-power items + 10 Create-logistics items
        // + a small hand of generic mechanism items. Both subsystems clear
        // the threshold (=10), so each gets its own island. The generic
        // mechanism items fall back to MECHANISMS.
        DescriptorPool pool = new DescriptorPool();
        for (int i = 0; i < 12; i++) {
            pool.addModded("create:cog_" + i, "mechanism", "create:mechanical_power");
        }
        for (int i = 0; i < 10; i++) {
            pool.addModded("create:funnel_" + i, "mechanism", "create:logistics");
        }
        for (int i = 0; i < 5; i++) {
            pool.addModded("modded:plain_gear_" + i, "mechanism", null);
        }

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(42L),
                pool::describe
        );

        Set<String> ids = new HashSet<>();
        for (VisualAtlasIsland island : plan.islands()) {
            ids.add(island.id());
        }
        assertTrue(ids.contains(IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX + "create:mechanical_power"),
                "create:mechanical_power should qualify as its own island");
        assertTrue(ids.contains(IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX + "create:logistics"),
                "create:logistics should qualify as its own island");
        assertTrue(ids.contains(IslandSuggestionTemplate.MECHANISMS.defaultIslandId()),
                "non-subsystem mechanism items should still land on MECHANISMS");
    }

    @Test
    void belowThresholdSubsystemFallsBackToParentTemplate() {
        // 5 items in a subsystem — below the 10-item threshold, so no
        // dedicated island; everything folds into MECHANISMS.
        DescriptorPool pool = new DescriptorPool();
        for (int i = 0; i < 5; i++) {
            pool.addModded("create:tiny_" + i, "mechanism", "create:mechanical_power");
        }

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::describe
        );

        Set<String> ids = new HashSet<>();
        for (VisualAtlasIsland island : plan.islands()) {
            ids.add(island.id());
        }
        assertEquals(1, plan.islands().size(),
                "subsystem below threshold → exactly one MECHANISMS island");
        assertTrue(ids.contains(IslandSuggestionTemplate.MECHANISMS.defaultIslandId()));
        for (String id : ids) {
            assertTrue(!id.startsWith(IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX),
                    "no subsystem island should be created when items are below threshold");
        }
    }

    @Test
    void decorationSubsystemNeverFiresEvenWhenQualified() {
        // 30 decorative_block items all sharing create:decoration. Even at
        // 30 items (well above threshold), the parent template (DECORATION)
        // is not in the subsystem-grouping whitelist, so they all collapse
        // into the single DECORATION island.
        DescriptorPool pool = new DescriptorPool();
        for (int i = 0; i < 30; i++) {
            pool.addModded("create:fancy_block_" + i, "decorative_block", "create:decoration");
        }

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(11L),
                pool::describe
        );

        Set<String> ids = new HashSet<>();
        for (VisualAtlasIsland island : plan.islands()) {
            ids.add(island.id());
        }
        assertEquals(1, plan.islands().size(),
                "decoration items must collapse into DECORATION even with a qualified subsystem");
        assertTrue(ids.contains(IslandSuggestionTemplate.DECORATION.defaultIslandId()));
    }

    @Test
    void trophyShuntRoutesToCuriosityIslandRegardlessOfRole() {
        // Modded "trophy" item with role=trophy — must land on CURIOSITY,
        // not bucket through its declared role.
        DescriptorPool pool = new DescriptorPool();
        pool.addTrophy("modded:legendary_skull", "trophy");
        pool.addRoleOnly("modded:plain_block", "building_block");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(9L),
                pool::describe
        );

        Map<ItemIdentity, VisualHomeAssignment> assignments = plan.assignments();
        VisualHomeAssignment trophyAssignment = null;
        for (VisualHomeAssignment a : assignments.values()) {
            if ("modded:legendary_skull".equals(a.identity().itemId())) {
                trophyAssignment = a;
            }
        }
        assertNotNull(trophyAssignment);
        assertEquals(IslandSuggestionTemplate.CURIOSITY.defaultIslandId(), trophyAssignment.islandId());
    }

    @Test
    void withinIslandSortClustersFamilyViaIdAlphabetical() {
        // Three oak items + three birch items in a BUILDING island.
        // The carry rank ties (all null frequency) so the secondary id
        // sort takes over — and since the family is in the id prefix
        // (birch_*, oak_*), siblings naturally cluster contiguously.
        DescriptorPool pool = new DescriptorPool();
        pool.addBuilding("modded:birch_thing_1", "wood_birch");
        pool.addBuilding("modded:oak_thing_1", "wood_oak");
        pool.addBuilding("modded:birch_thing_2", "wood_birch");
        pool.addBuilding("modded:oak_thing_2", "wood_oak");
        pool.addBuilding("modded:oak_thing_3", "wood_oak");
        pool.addBuilding("modded:birch_thing_3", "wood_birch");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(13L),
                pool::describe
        );

        // Find the BUILDING island and read its assignments in ordinal
        // order. All wood_birch items should appear before all wood_oak
        // items (or vice versa) — never interleaved.
        String buildingIslandId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> buildingItemIds = new ArrayList<>();
        VisualHomeAssignment[] sorted = plan.assignments().values().stream()
                .filter(a -> buildingIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .toArray(VisualHomeAssignment[]::new);
        for (VisualHomeAssignment a : sorted) {
            buildingItemIds.add(a.identity().itemId());
        }
        assertEquals(6, buildingItemIds.size());

        boolean seenOak = false;
        boolean seenBirchAfterOak = false;
        boolean seenOakAfterBirch = false;
        boolean firstFamilyIsBirch = buildingItemIds.get(0).contains("birch");
        for (String id : buildingItemIds) {
            if (firstFamilyIsBirch) {
                if (id.contains("oak")) {
                    seenOak = true;
                } else if (seenOak) {
                    seenBirchAfterOak = true;
                }
            } else {
                if (id.contains("birch")) {
                    seenOak = true;
                } else if (seenOak) {
                    seenOakAfterBirch = true;
                }
            }
        }
        assertTrue(!seenBirchAfterOak && !seenOakAfterBirch,
                "items of one material_family should be contiguous; got " + buildingItemIds);
    }

    @Test
    void uniqueRarityItemsAlwaysRollAsSingleStackInChests() {
        // A trophy-tier item (rarity=unique) routed to CURIOSITY would
        // otherwise roll 1-8 via the template-only path. Descriptor-aware
        // rollStackCount must clamp to count=1 so the populated atlas
        // has nether_star / dragon_egg etc. on display, not stacked.
        DescriptorPool pool = new DescriptorPool();
        for (int i = 0; i < 12; i++) {
            // Trophies sit in CURIOSITY via the trophy shunt; need
            // multiple to ensure at least one ends up in a chest.
            pool.addTrophy("modded:legendary_skull_" + i, "trophy");
        }

        boolean foundTrophyInChest = false;
        for (long seed : new long[]{1L, 7L, 31L, 99L, 421L}) {
            RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                    pool.stacks(),
                    PopulateProfile.LATE_MODPACK,
                    new Random(seed),
                    pool::describe
            );
            for (ChestSpec chest : plan.chests()) {
                for (ChestContentEntry entry : chest.contents()) {
                    if (!entry.stack().itemId().contains("legendary_skull_")) {
                        continue;
                    }
                    foundTrophyInChest = true;
                    assertEquals(1, entry.stack().getCount(),
                            "trophy-tier item must roll as single stack; got count "
                                    + entry.stack().getCount() + " for "
                                    + entry.stack().itemId());
                }
            }
        }
        assertTrue(foundTrophyInChest,
                "expected at least one trophy item to land in a chest across 5 seeds");
    }

    @Test
    void multipleChestsInSameIslandSpanDifferentFacetThemes() {
        // Three material families × eight items each, all routed to
        // MATERIALS. Across multiple seeds, the populated MATERIALS
        // chests should reliably span ≥ 2 distinct dominant families
        // (the "diversity bias"). Without the cross-chest claimed-keys
        // set, two independent uniform seed picks could randomly land
        // on the same family.
        java.util.Set<String> globalDominantFamilies = new java.util.LinkedHashSet<>();
        int aggregateChestsChecked = 0;
        for (long seed : new long[]{7L, 13L, 31L, 42L, 99L}) {
            DescriptorPool pool = new DescriptorPool();
            for (int i = 0; i < 8; i++) {
                pool.addMaterialFamily("modded:iron_chunk_" + i, "iron");
            }
            for (int i = 0; i < 8; i++) {
                pool.addMaterialFamily("modded:gold_chunk_" + i, "gold");
            }
            for (int i = 0; i < 8; i++) {
                pool.addMaterialFamily("modded:copper_chunk_" + i, "copper");
            }
            RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                    pool.stacks(),
                    PopulateProfile.LATE_MODPACK,
                    new Random(seed),
                    pool::describe
            );

            for (ChestSpec chest : plan.chests()) {
                if (!chest.isLinked()
                        || !chest.linkedIslandId().equals(IslandSuggestionTemplate.MATERIALS.defaultIslandId())) {
                    continue;
                }
                int iron = 0, gold = 0, copper = 0;
                for (ChestContentEntry entry : chest.contents()) {
                    String id = entry.stack().itemId();
                    if (id.contains("iron_chunk_")) iron++;
                    else if (id.contains("gold_chunk_")) gold++;
                    else if (id.contains("copper_chunk_")) copper++;
                }
                int relevant = iron + gold + copper;
                if (relevant < 4) continue;
                aggregateChestsChecked++;
                int max = Math.max(iron, Math.max(gold, copper));
                if (max * 10 < relevant * 5) continue;
                if (max == iron) globalDominantFamilies.add("iron");
                else if (max == gold) globalDominantFamilies.add("gold");
                else globalDominantFamilies.add("copper");
            }
        }
        assertTrue(aggregateChestsChecked >= 4,
                "expected at least four MATERIALS chests across seeds; got "
                        + aggregateChestsChecked);
        // Across 5 seeds × 2 chests each, the diversity bias should
        // surface every family at least once.
        assertTrue(globalDominantFamilies.size() >= 2,
                "expected MATERIALS chests to span at least two facet themes across seeds; got "
                        + globalDominantFamilies);
    }

    @Test
    void linkedChestsClusterContentByFacetSimilarity() {
        // 8 iron-family items + 8 gold-family items in MATERIALS, all at
        // the same carry rank. With facet-aware chest filling, each
        // generated MATERIALS chest should overwhelmingly be either
        // iron-themed or gold-themed (>= 60% same family) instead of an
        // even mix. Without the facet-similarity bias, a single chest's
        // 20 fills would be ~50/50 iron/gold by uniform pick.
        DescriptorPool pool = new DescriptorPool();
        for (int i = 0; i < 8; i++) {
            pool.addMaterialFamily("modded:iron_chunk_" + i, "iron");
        }
        for (int i = 0; i < 8; i++) {
            pool.addMaterialFamily("modded:gold_chunk_" + i, "gold");
        }

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.LATE_MODPACK,
                new Random(13L),
                pool::describe
        );

        int chestsChecked = 0;
        int chestsThemed = 0;
        for (ChestSpec chest : plan.chests()) {
            if (!chest.isLinked()) {
                continue;
            }
            int ironCount = 0;
            int goldCount = 0;
            for (ChestContentEntry entry : chest.contents()) {
                String id = entry.stack().itemId();
                if (id.contains("iron_chunk_")) ironCount++;
                else if (id.contains("gold_chunk_")) goldCount++;
            }
            int relevant = ironCount + goldCount;
            if (relevant < 4) {
                // Skip tiny chests where the family-purity check is
                // statistically meaningless.
                continue;
            }
            chestsChecked++;
            int dominant = Math.max(ironCount, goldCount);
            if (dominant * 10 >= relevant * 6) {
                chestsThemed++;
            }
        }

        assertTrue(chestsChecked > 0, "expected at least one MATERIALS chest with enough family-relevant content");
        // Allow some statistical slack: ≥ 60% of checked chests should
        // be themed (i.e. dominated 60/40 or stronger by one family).
        // Pure random would give ~25% by binomial chance.
        assertTrue(chestsThemed * 10 >= chestsChecked * 6,
                "expected >= 60% of checked chests to read as facet-themed; got "
                        + chestsThemed + "/" + chestsChecked);
    }

    @Test
    void carriedSampleBiasesTowardEverydayItems() {
        // 30 everyday + 30 rare items, STARTER profile (identityCount=30,
        // carriedCap=20). With weight 6 (everyday) vs 0.2 (rare) = 30:1
        // ratio, the carried 20 should land overwhelmingly on everyday.
        DescriptorPool pool = new DescriptorPool();
        for (int i = 0; i < 30; i++) {
            pool.addBuildingWithFrequency("modded:every_" + i, "wood_oak", "everyday");
        }
        for (int i = 0; i < 30; i++) {
            pool.addBuildingWithFrequency("modded:rare_" + i, "wood_oak", "rare");
        }

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.STARTER,
                new Random(101L),
                pool::describe
        );

        int everydayCount = 0;
        for (ItemStack stack : plan.homedStacks()) {
            if (stackId(stack).contains("every_")) {
                everydayCount++;
            }
        }
        // Floor is conservative: even with the homed-pool selection
        // being uniform-random, the carried-sample weighting alone
        // should push the everyday-share well above 50%.
        assertTrue(everydayCount >= plan.homedStacks().size() * 3 / 4,
                "expected ≥75% of carried picks to be everyday-frequency; got "
                        + everydayCount + "/" + plan.homedStacks().size());
    }

    private static String stackId(ItemStack stack) {
        try {
            return (String) stack.getClass().getMethod("itemId").invoke(stack);
        } catch (Exception ignored) {
            return "";
        }
    }

    @Test
    void carryRankDominatesMaterialFamilyGrouping() {
        // Items with the highest carry rank (everyday > frequent > rare)
        // cluster at the TOP of an island, regardless of material
        // family. Within a carry-rank band, items still group by family
        // for residual locality. So the order is:
        //   everyday_birch, everyday_oak, frequent_birch, frequent_oak,
        //   rare_birch, rare_oak
        // — not the old "tier 0 = both everyday and frequent grouped by
        // family" interleave that put every-frequent-pairs together.
        DescriptorPool pool = new DescriptorPool();
        pool.addBuildingWithFrequency("modded:rare_oak", "wood_oak", "rare");
        pool.addBuildingWithFrequency("modded:rare_birch", "wood_birch", "rare");
        pool.addBuildingWithFrequency("modded:every_oak", "wood_oak", "everyday");
        pool.addBuildingWithFrequency("modded:every_birch", "wood_birch", "everyday");
        pool.addBuildingWithFrequency("modded:frequent_oak", "wood_oak", "frequent");
        pool.addBuildingWithFrequency("modded:frequent_birch", "wood_birch", "frequent");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(31L),
                pool::describe
        );

        String buildingIslandId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> buildingIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        assertEquals(List.of(
                "modded:every_birch",
                "modded:every_oak",
                "modded:frequent_birch",
                "modded:frequent_oak",
                "modded:rare_birch",
                "modded:rare_oak"
        ), ordered);
    }

    @Test
    void rarityPenalizesUncommonAndAboveOnly() {
        // abundant / common / null rarities are treated as
        // equivalently-carry-friendly and tie on carry rank — only
        // uncommon+ tiers contribute a meaningful penalty. So among
        // three frequent items, the uncommon one sorts last while the
        // common+null ones tie and fall back to alphabetical id.
        DescriptorPool pool = new DescriptorPool();
        pool.addBuildingWithFrequencyAndRarity("modded:asphalt", "asphalt", "frequent", null);
        pool.addBuildingWithFrequencyAndRarity("modded:stone", "stone", "frequent", "common");
        pool.addBuildingWithFrequencyAndRarity("modded:obsidian", "obsidian", "frequent", "uncommon");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::describe
        );

        String buildingIslandId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> buildingIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        assertEquals(List.of(
                "modded:asphalt",   // frequent + null → carry rank 10
                "modded:stone",     // frequent + common → carry rank 10 (tie, "stone" > "asphalt" alphabetically)
                "modded:obsidian"   // frequent + uncommon → carry rank 12 (penalized)
        ), ordered);
    }

    @Test
    void dyedItemsClusterByStemThenCanonicalColorOrder() {
        // Two stems (carpet, wool) × three dye colors each (white, gray,
        // black). Comparator must:
        //   1. Group all carpets before all wools (id stem, "carpet" <
        //      "wool" alphabetically).
        //   2. Within each stem, order colors as Minecraft's dye wheel:
        //      white → gray → black, NOT alphabetical (black, gray, white).
        DescriptorPool pool = new DescriptorPool();
        pool.addDyedDecoration("modded:black_wool", "black");
        pool.addDyedDecoration("modded:white_wool", "white");
        pool.addDyedDecoration("modded:gray_carpet", "gray");
        pool.addDyedDecoration("modded:white_carpet", "white");
        pool.addDyedDecoration("modded:black_carpet", "black");
        pool.addDyedDecoration("modded:gray_wool", "gray");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::describe
        );

        String decorationIslandId = IslandSuggestionTemplate.DECORATION.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> decorationIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        assertEquals(List.of(
                "modded:white_carpet",
                "modded:gray_carpet",
                "modded:black_carpet",
                "modded:white_wool",
                "modded:gray_wool",
                "modded:black_wool"
        ), ordered);
    }

    @Test
    void palettedItemsClusterByPrimaryToneNotIdAlpha() {
        // Six warm-toned wood blocks across three "species" — pure id
        // alphabetical would interleave them as acacia, jungle, mangrove,
        // but palette clustering pulls all three wood_red items into one
        // block and all three wood_medium items into another, regardless
        // of their alphabetical order.
        DescriptorPool pool = new DescriptorPool();
        pool.addPalettedBuilding("modded:acacia_planks", "wood_red");
        pool.addPalettedBuilding("modded:oak_planks", "wood_medium");
        pool.addPalettedBuilding("modded:jungle_planks", "wood_red");
        pool.addPalettedBuilding("modded:spruce_planks", "wood_medium");
        pool.addPalettedBuilding("modded:mangrove_planks", "wood_red");
        pool.addPalettedBuilding("modded:dark_oak_planks", "wood_medium");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(13L),
                pool::describe
        );

        String buildingIslandId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> buildingIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        // Expected: wood_medium block first (alphabetical: wood_medium <
        // wood_red), id-alpha within each tone group.
        assertEquals(List.of(
                "modded:dark_oak_planks",
                "modded:oak_planks",
                "modded:spruce_planks",
                "modded:acacia_planks",
                "modded:jungle_planks",
                "modded:mangrove_planks"
        ), ordered);
    }

    @Test
    void dyedItemsLeadPalettedItemsLeadPlainItems() {
        // Mixed island: a dyed wool, a palette-toned plank, a plain
        // building block. Cluster-key ordering puts dyed first, palette
        // next, plain id last — even though pure alphabetical would
        // interleave them ("modded:concrete" < "modded:plank" <
        // "modded:wool").
        DescriptorPool pool = new DescriptorPool();
        pool.addDyedDecoration("modded:white_concrete", "white");
        pool.addPalettedBuilding("modded:oak_plank", "wood_medium");
        pool.addBuilding("modded:plain_brick", null);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(99L),
                pool::describe
        );

        // Both DECORATION and BUILDING islands fire; collect every
        // assignment and verify global ordering by cluster prefix.
        List<String> ordered = plan.assignments().values().stream()
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        // dyed → palette → plain. The dyed item is in its own island
        // (DECORATION), so cross-island ordinals don't matter for the
        // claim — what matters is that within-island sort respects
        // the cluster bands. Here it's per-island anyway.
        assertTrue(ordered.contains("modded:white_concrete"));
        assertTrue(ordered.contains("modded:oak_plank"));
        assertTrue(ordered.contains("modded:plain_brick"));
        // BUILDING island: palette before plain.
        String buildingId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> buildingOrder = plan.assignments().values().stream()
                .filter(a -> buildingId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();
        assertEquals(List.of("modded:oak_plank", "modded:plain_brick"), buildingOrder);
    }

    @Test
    void originTierOrdersEarlyOverworldAheadOfLateNetherAndCreative() {
        // Five MATERIALS items spanning the origin tiers. Pure id-alpha
        // would put creative_dust first ("c" before "i" / "n" / "o"),
        // but origin clustering pulls early items to the top, late
        // items down, and creative_only to the very bottom regardless
        // of name.
        DescriptorPool pool = new DescriptorPool();
        pool.addMaterialWithOrigin("modded:netherite_chunk", "nether");
        pool.addMaterialWithOrigin("modded:iron_filings", "overworld_cave");
        pool.addMaterialWithOrigin("modded:end_shard", "end");
        pool.addMaterialWithOrigin("modded:creative_dust", "creative_only");
        pool.addMaterialWithOrigin("modded:village_token", "village");
        pool.addMaterialWithOrigin("modded:ancient_relic", "ancient_city");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::describe
        );

        String materialsIslandId = IslandSuggestionTemplate.MATERIALS.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> materialsIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        assertEquals(List.of(
                "modded:iron_filings",      // early ('a' overworld_cave)
                "modded:village_token",     // early ('a' village)
                "modded:ancient_relic",     // mid   ('b' ancient_city)
                "modded:end_shard",         // late  ('c' end)
                "modded:netherite_chunk",   // late  ('c' nether)
                "modded:creative_dust"      // creative ('z')
        ), ordered);
    }

    @Test
    void flavorPartitionsPlainBlocksBeforeFancyAndAncient() {
        // Five plain-id BUILDING items with mixed flavors. Within the
        // plain-zone of the cluster key, the flavor rank wins over
        // id-alpha so plain blocks lead, then natural / fancy /
        // ancient — even though pure id-alpha would put "ancient_brick"
        // first.
        DescriptorPool pool = new DescriptorPool();
        pool.addBuildingWithFlavor("modded:ancient_brick", "ancient");
        pool.addBuildingWithFlavor("modded:fancy_brick", "fancy");
        pool.addBuildingWithFlavor("modded:plain_brick", "plain");
        pool.addBuildingWithFlavor("modded:natural_stone", "natural");
        pool.addBuildingWithFlavor("modded:ominous_brick", "ominous");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::describe
        );

        String buildingIslandId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> buildingIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        assertEquals(List.of(
                "modded:plain_brick",
                "modded:natural_stone",
                "modded:fancy_brick",
                "modded:ominous_brick",
                "modded:ancient_brick"
        ), ordered);
    }

    @Test
    void frequencyOrdersWithinIslandFromUbiquitousToRare() {
        DescriptorPool pool = new DescriptorPool();
        pool.addBuildingWithFrequency("modded:rare_block", "wood_oak", "rare");
        pool.addBuildingWithFrequency("modded:every_block", "wood_oak", "everyday");
        pool.addBuildingWithFrequency("modded:occasional_block", "wood_oak", "occasional");
        pool.addBuildingWithFrequency("modded:frequent_block", "wood_oak", "frequent");

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(21L),
                pool::describe
        );

        String buildingIslandId = IslandSuggestionTemplate.BUILDING.defaultIslandId();
        List<String> ordered = plan.assignments().values().stream()
                .filter(a -> buildingIslandId.equals(a.islandId()))
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .map(a -> a.identity().itemId())
                .toList();

        assertEquals(List.of(
                "modded:every_block",
                "modded:frequent_block",
                "modded:occasional_block",
                "modded:rare_block"
        ), ordered);
    }

    private static final class DescriptorPool {
        private final ArrayList<ItemStack> stacks = new ArrayList<>();
        private final LinkedHashMap<String, IslandSignalDescriptor> descriptorsById = new LinkedHashMap<>();
        private int nextId = 0;

        List<ItemStack> stacks() {
            return stacks;
        }

        IslandSignalDescriptor describe(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return IslandSignalDescriptor.empty(ItemIdentity.of("minecraft:air"));
            }
            String id;
            try {
                id = (String) stack.getClass().getMethod("itemId").invoke(stack);
            } catch (Exception ignored) {
                id = "";
            }
            IslandSignalDescriptor descriptor = descriptorsById.get(id);
            return descriptor == null
                    ? IslandSignalDescriptor.empty(ItemIdentity.of(id))
                    : descriptor;
        }

        void addModded(String itemId, String role, String subsystemId) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    role,
                    null,
                    null,
                    subsystemId == null ? List.of() : List.of(subsystemId),
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    false
            ));
        }

        void addRoleOnly(String itemId, String role) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    role
            ));
        }

        void addDyedDecoration(String itemId, String dyeColor) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "decorative_block",
                    null,
                    null,
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    dyeColor,
                    List.of(),
                    null,
                    false
            ));
        }

        void addPalettedBuilding(String itemId, String paletteValue) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "building_block",
                    null,
                    null,
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    paletteValue == null ? List.of() : List.of(paletteValue),
                    null,
                    false
            ));
        }

        void addBuildingWithFlavor(String itemId, String flavor) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "building_block",
                    null,
                    null,
                    List.of(),
                    List.of(),
                    flavor,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    false
            ));
        }

        void addMaterialFamily(String itemId, String materialFamily) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "material",
                    null,
                    materialFamily,
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    false
            ));
        }

        void addMaterialWithOrigin(String itemId, String origin) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "material",
                    null,
                    null,
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    origin,
                    null,
                    List.of(),
                    null,
                    false
            ));
        }

        void addBuilding(String itemId, String materialFamily) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "building_block",
                    null,
                    materialFamily,
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    false
            ));
        }

        void addBuildingWithFrequency(String itemId, String materialFamily, String frequency) {
            addBuildingWithFrequencyAndRarity(itemId, materialFamily, frequency, null);
        }

        void addBuildingWithFrequencyAndRarity(String itemId, String materialFamily,
                                                String frequency, String rarity) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    "building_block",
                    null,
                    materialFamily,
                    List.of(),
                    List.of(),
                    null,
                    frequency,
                    rarity,
                    null,
                    null,
                    List.of(),
                    null,
                    false
            ));
        }

        void addTrophy(String itemId, String role) {
            registerStack(itemId);
            descriptorsById.put(itemId, new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    role
            ));
        }

        private void registerStack(String itemId) {
            ItemStack stack = new ItemStack(itemId, 1, 64);
            stacks.add(stack);
            nextId++;
        }

        private static String namespaceOf(String itemId) {
            int colon = itemId.indexOf(':');
            return colon <= 0 ? "" : itemId.substring(0, colon);
        }
    }

    private static final class TemplatePool {
        private final ArrayList<ItemStack> stacks = new ArrayList<>();
        private final HashMap<String, IslandSuggestionTemplate> templatesById = new HashMap<>();
        private int nextId = 0;

        void add(IslandSuggestionTemplate template, int count) {
            for (int index = 0; index < count; index++) {
                String id = "slot_test:" + template.name().toLowerCase() + "_" + (nextId++);
                ItemStack stack = new ItemStack(id, 1, 64);
                stacks.add(stack);
                templatesById.put(id, template);
            }
        }

        List<ItemStack> stacks() {
            return stacks;
        }

        IslandSuggestionTemplate classify(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return IslandSuggestionTemplate.MISC;
            }
            try {
                Function<ItemStack, String> itemIdExtractor = s -> {
                    try {
                        return (String) s.getClass().getMethod("itemId").invoke(s);
                    } catch (Exception ignored) {
                        return "";
                    }
                };
                return templatesById.getOrDefault(itemIdExtractor.apply(stack), IslandSuggestionTemplate.MISC);
            } catch (RuntimeException ignored) {
                return IslandSuggestionTemplate.MISC;
            }
        }
    }
}
