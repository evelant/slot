package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.workflow.domain.ChestAffinity;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Affinity/content-driven routing. Replaces the link-era tests; the planner
 * now looks up {@code affinity[chest, identity]} and live chest contents for
 * proximate claimed chests.
 */
class DepositPlannerTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CHEST_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CHEST_C = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void emptyProximateSetProducesEmptyPlan() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 16),
                affinity(CHEST_A, "minecraft:redstone", 5),
                claimedMap(CHEST_A),
                Set.of()
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void stackWithoutAffinityIsIgnored() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:bone", 4),
                ChestAffinityMap.empty(),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString())
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void positiveAffinityWithProximateClaimedChestRoutesDeposit() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 5, "minecraft:redstone", 16),
                affinity(CHEST_A, "minecraft:redstone", 5),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString())
        );
        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals(BuiltinInventoryIds.PLAYER_MAIN, assignment.laneId());
        assertEquals(5, assignment.slotIndex());
        assertEquals("minecraft:redstone", assignment.itemId());
        assertEquals(List.of(CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void existingProximateContentsRouteDepositWithoutAffinity() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 5, "minecraft:redstone", 16),
                ChestAffinityMap.empty(),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                null,
                (chest, identity) -> chest.storageId().equals(CHEST_A)
                        && identity.equals(ItemIdentity.of("minecraft:redstone"))
        );

        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals("minecraft:redstone", assignment.itemId());
        assertEquals(List.of(CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void multipleProximateChestsRankedByScore() {
        ItemIdentity redstone = ItemIdentity.of("minecraft:redstone");
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> bonds = new LinkedHashMap<>();
        bonds.put(CHEST_A, Map.of(redstone, new ChestAffinity(redstone, 1, 0L)));
        bonds.put(CHEST_B, Map.of(redstone, new ChestAffinity(redstone, 5, 0L)));
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 16),
                new ChestAffinityMap(bonds),
                claimedMap(CHEST_A, CHEST_B),
                Set.of(CHEST_A.toString(), CHEST_B.toString())
        );
        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        // Highest-score chest first; spill on full goes to next.
        assertEquals(List.of(CHEST_B.toString(), CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void affinityRanksBeforeContentOnlyChest() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 16),
                affinity(CHEST_A, "minecraft:redstone", 1),
                claimedMap(CHEST_A, CHEST_B),
                Set.of(CHEST_A.toString(), CHEST_B.toString()),
                null,
                (chest, identity) -> chest.storageId().equals(CHEST_B)
                        && identity.equals(ItemIdentity.of("minecraft:redstone"))
        );

        assertEquals(1, plan.assignments().size());
        assertEquals(
                List.of(CHEST_A.toString(), CHEST_B.toString()),
                plan.assignments().get(0).candidateStorageIds());
    }

    @Test
    void ineligibleChestsDoNotRouteByAffinityOrContents() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 16),
                affinity(CHEST_A, "minecraft:redstone", 1),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                null,
                (chest, identity) -> chest.storageId().equals(CHEST_A)
                        && identity.equals(ItemIdentity.of("minecraft:redstone")),
                chest -> false
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void similarBondsDoNotRouteWhenNoDirectAffinity() {
        // CHEST_A has strong learned bonds for other ingots, but the
        // carried identity itself has never been deposited there. The
        // planner must leave it carried instead of inferring similarity.
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        ItemIdentity goldIngot = ItemIdentity.of("minecraft:gold_ingot");
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> bonds = new LinkedHashMap<>();
        bonds.put(CHEST_A, Map.of(
                ironIngot, new ChestAffinity(ironIngot, 3, 0L),
                goldIngot, new ChestAffinity(goldIngot, 3, 0L)
        ));

        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:netherite_ingot", 1),
                new ChestAffinityMap(bonds),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString())
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void affinityAndContentAreTheOnlyEligibleTiers() {
        ItemIdentity rawIron = ItemIdentity.of("minecraft:raw_iron");
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        ItemIdentity ironBlock = ItemIdentity.of("minecraft:iron_block");
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> bonds = new LinkedHashMap<>();
        bonds.put(CHEST_A, Map.of(rawIron, new ChestAffinity(rawIron, 1, 0L)));
        bonds.put(CHEST_B, Map.of(
                ironIngot, new ChestAffinity(ironIngot, 5, 0L),
                ironBlock, new ChestAffinity(ironBlock, 5, 0L)
        ));

        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:raw_iron", 4),
                new ChestAffinityMap(bonds),
                claimedMap(CHEST_A, CHEST_B),
                Set.of(CHEST_A.toString(), CHEST_B.toString())
        );

        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals(List.of(CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void explicitDepositUsesDirectAffinity() {
        ItemIdentity target = ItemIdentity.of("minecraft:netherite_ingot");

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B),
                affinity(CHEST_B, "minecraft:netherite_ingot", 1),
                Set.of(CHEST_A.toString(), CHEST_B.toString())
        );

        assertEquals(List.of(CHEST_B), ranked);
    }

    @Test
    void explicitDepositUsesExistingContentsWithoutAffinity() {
        ItemIdentity target = ItemIdentity.of("minecraft:netherite_ingot");

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B),
                ChestAffinityMap.empty(),
                Set.of(CHEST_A.toString(), CHEST_B.toString()),
                (chest, identity) -> chest.storageId().equals(CHEST_A)
                        && identity.equals(target)
        );

        assertEquals(List.of(CHEST_A), ranked);
    }

    @Test
    void explicitDepositRespectsChestEligibility() {
        ItemIdentity target = ItemIdentity.of("minecraft:netherite_ingot");

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A),
                affinity(CHEST_A, "minecraft:netherite_ingot", 1),
                Set.of(CHEST_A.toString()),
                (chest, identity) -> true,
                chest -> false
        );

        assertTrue(ranked.isEmpty());
    }

    @Test
    void explicitDepositDoesNotUseSimilarityWhenNoDirectAffinityExists() {
        ItemIdentity target = ItemIdentity.of("minecraft:netherite_ingot");

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B),
                ChestAffinityMap.empty(),
                Set.of(CHEST_A.toString(), CHEST_B.toString())
        );

        assertTrue(ranked.isEmpty());
    }

    @Test
    void explicitDepositDoesNotUseEmptiestChestFallback() {
        ItemIdentity target = ItemIdentity.of("minecraft:amethyst_shard");

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B, CHEST_C),
                ChestAffinityMap.empty(),
                Set.of(CHEST_A.toString(), CHEST_B.toString(), CHEST_C.toString())
        );

        assertTrue(ranked.isEmpty());
    }

    @Test
    void bulkDepositWithReservationDepositsOnlyExcessToAffinityChest() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 5),
                affinity(CHEST_A, "minecraft:redstone", 4),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                identity -> identity.itemId().equals("minecraft:redstone") ? 3 : 0
        );

        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals("minecraft:redstone", assignment.itemId());
        assertEquals(2, assignment.count());
        assertEquals(List.of(CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void bulkDepositWithMovableToolReservationKeepsDamagedTool() {
        DepositPlan plan = DepositPlanner.plan(
                authority(
                        BuiltinInventoryIds.PLAYER_MAIN,
                        0,
                        new ItemStack("gtceu:steel_mining_hammer", "{Damage:512}", 1, 1)),
                ChestAffinityMap.empty(),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                identity -> ItemIdentity.of("gtceu:steel_mining_hammer").equals(identity) ? 1 : 0,
                (chest, identity) -> true
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void bulkDepositWithReservationDoesNotUseExplicitSimilarityOrEmptyChestFallback() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:amethyst_shard", 5),
                ChestAffinityMap.empty(),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                identity -> identity.itemId().equals("minecraft:amethyst_shard") ? 3 : 0
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void exactIdentityAffinityOnlyIgnoresOtherBonds() {
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:netherite_ingot", 1),
                new ChestAffinityMap(Map.of(CHEST_A,
                        Map.of(ironIngot, new ChestAffinity(ironIngot, 5, 0L)))),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString())
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void onlyProximateChestsCount() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 16),
                affinity(CHEST_A, "minecraft:redstone", 5),
                claimedMap(CHEST_A, CHEST_B),
                Set.of(CHEST_B.toString())
        );
        assertTrue(plan.isEmpty());
    }

    private static ChestAffinityMap affinity(UUID storageId, String itemId, int score) {
        ItemIdentity identity = ItemIdentity.of(itemId);
        return new ChestAffinityMap(Map.of(storageId,
                Map.of(identity, new ChestAffinity(identity, score, 0L))));
    }

    private static ClaimedChestMap claimedMap(UUID... storageIds) {
        java.util.ArrayList<ClaimedChest> chests = new java.util.ArrayList<>();
        for (UUID id : storageIds) {
            chests.add(new ClaimedChest(
                    id,
                    Set.of(new ChestAnchor("minecraft:overworld", 0, 64, 0)),
                    0, 0, ""));
        }
        return new ClaimedChestMap(chests);
    }

    private static InventoryAuthoritySnapshot authority(String sourceId, int slotIndex, String itemId, int count) {
        return authority(sourceId, slotIndex, new ItemStack(itemId, count, 64));
    }

    private static InventoryAuthoritySnapshot authority(String sourceId, int slotIndex, ItemStack stack) {
        int count = stack == null || stack.isEmpty() ? 0 : stack.getCount();
        InventorySourceSnapshot source = new InventorySourceSnapshot(
                sourceId,
                36,
                List.of(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(sourceId, slotIndex),
                        stack,
                        count,
                        ""
                )),
                ""
        );
        return new InventoryAuthoritySnapshot(null, Map.of(sourceId, source), CursorStateSnapshot.empty());
    }
}
