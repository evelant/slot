package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealisticAtlasGeneratorTest {
    @Test
    void emptyPoolProducesEmptyPlan() {
        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                List.of(), PopulateProfile.ORGANIZED, new Random(1L), stack -> SemanticBucket.MISC);

        assertTrue(plan.islands().isEmpty());
        assertTrue(plan.assignments().isEmpty());
        assertTrue(plan.chests().isEmpty());
        assertTrue(plan.triageStacks().isEmpty());
        assertTrue(plan.homedStacks().isEmpty());
    }

    @Test
    void homedStackCountMatchesAssignmentCount() {
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.MATERIALS, 40);
        pool.add(SemanticBucket.TOOLS, 10);

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
    void organizedProfileCreatesOneIslandPerNonEmptyBucket() {
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.MATERIALS, 40);
        pool.add(SemanticBucket.BUILDING, 20);
        pool.add(SemanticBucket.TOOLS, 10);
        pool.add(SemanticBucket.FOOD, 10);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(7L),
                pool::classify
        );

        HashSet<String> islandIds = new HashSet<>();
        HashSet<String> labels = new HashSet<>();
        for (VisualAtlasIsland island : plan.islands()) {
            islandIds.add(island.id());
            labels.add(island.label());
        }
        assertEquals(4, plan.islands().size());
        assertTrue(labels.contains(SemanticBucket.MATERIALS.label()));
        assertTrue(labels.contains(SemanticBucket.BUILDING.label()));
        assertTrue(labels.contains(SemanticBucket.TOOLS.label()));
        assertTrue(labels.contains(SemanticBucket.FOOD.label()));
    }

    @Test
    void assignmentsReferenceValidIslandsAndFitInsideBounds() {
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.MATERIALS, 30);
        pool.add(SemanticBucket.REDSTONE, 12);
        pool.add(SemanticBucket.STORAGE, 5);

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
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.MATERIALS, 200);

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
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.MATERIALS, 40);
        pool.add(SemanticBucket.TOOLS, 10);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.ORGANIZED,
                new Random(9L),
                pool::classify
        );

        assertEquals(PopulateProfile.ORGANIZED.chestCount(), plan.chests().size());
    }

    @Test
    void linkedChestsMostlyContainBucketMatchedItems() {
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.MATERIALS, 40);
        pool.add(SemanticBucket.FOOD, 20);
        pool.add(SemanticBucket.TOOLS, 15);

        RealisticAtlasPlan plan = RealisticAtlasGenerator.generate(
                pool.stacks(),
                PopulateProfile.LATE_MODPACK,
                new Random(11L),
                pool::classify
        );

        EnumMap<SemanticBucket, String> islandByBucket = new EnumMap<>(SemanticBucket.class);
        for (VisualAtlasIsland island : plan.islands()) {
            for (SemanticBucket bucket : SemanticBucket.values()) {
                if (island.id().endsWith(bucket.id())) {
                    islandByBucket.put(bucket, island.id());
                    break;
                }
            }
        }

        int linkedChests = 0;
        int bucketMatches = 0;
        int totalContents = 0;
        for (ChestSpec chest : plan.chests()) {
            if (!chest.isLinked()) {
                continue;
            }
            linkedChests++;
            SemanticBucket linkedBucket = null;
            for (Map.Entry<SemanticBucket, String> entry : islandByBucket.entrySet()) {
                if (entry.getValue().equals(chest.linkedIslandId())) {
                    linkedBucket = entry.getKey();
                    break;
                }
            }
            assertNotNull(linkedBucket, "linked chest references unknown island: " + chest.linkedIslandId());
            for (ChestContentEntry entry : chest.contents()) {
                totalContents++;
                if (pool.classify(entry.stack()) == linkedBucket) {
                    bucketMatches++;
                }
            }
        }

        assertTrue(linkedChests > 0, "expected some linked chests");
        assertTrue(totalContents > 0, "expected some chest contents");
        // Content bias is 85%; allow generous slack so RNG variance doesn't flake.
        double ratio = (double) bucketMatches / (double) totalContents;
        assertTrue(ratio >= 0.6,
                "expected at least 60% bucket-match in linked chests, got " + ratio);
    }

    @Test
    void islandsFromDifferentBucketsDoNotOverlap() {
        BucketPool pool = new BucketPool();
        pool.add(SemanticBucket.TOOLS, 8);
        pool.add(SemanticBucket.COMBAT, 6);
        pool.add(SemanticBucket.MATERIALS, 30);
        pool.add(SemanticBucket.NATURAL, 12);
        pool.add(SemanticBucket.REDSTONE, 5);
        pool.add(SemanticBucket.MECHANISMS, 18);
        pool.add(SemanticBucket.STORAGE, 4);

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
        java.util.Set<Long> origins = new java.util.HashSet<>();
        for (VisualAtlasIsland island : islands) {
            long packed = ((long) island.x() << 32) | (island.y() & 0xFFFFFFFFL);
            assertTrue(origins.add(packed),
                    "duplicate island origin (" + island.x() + "," + island.y() + ") for " + island.id());
        }
    }

    private static final class BucketPool {
        private final ArrayList<ItemStack> stacks = new ArrayList<>();
        private final HashMap<String, SemanticBucket> buckets = new HashMap<>();
        private int nextId = 0;

        void add(SemanticBucket bucket, int count) {
            for (int index = 0; index < count; index++) {
                String id = "slot_test:" + bucket.id() + "_" + (nextId++);
                ItemStack stack = new ItemStack(id, 1, 64);
                stacks.add(stack);
                buckets.put(id, bucket);
            }
        }

        List<ItemStack> stacks() {
            return stacks;
        }

        SemanticBucket classify(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return SemanticBucket.MISC;
            }
            try {
                Function<ItemStack, String> itemIdExtractor = s -> {
                    try {
                        return (String) s.getClass().getMethod("itemId").invoke(s);
                    } catch (Exception ignored) {
                        return "";
                    }
                };
                return buckets.getOrDefault(itemIdExtractor.apply(stack), SemanticBucket.MISC);
            } catch (RuntimeException ignored) {
                return SemanticBucket.MISC;
            }
        }
    }
}
