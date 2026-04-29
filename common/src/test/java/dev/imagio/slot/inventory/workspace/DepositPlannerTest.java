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
 * Affinity-driven routing. Replaces the link-era tests; the planner now
 * looks up {@code affinity[chest, identity]} for proximate claimed chests.
 */
class DepositPlannerTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CHEST_B = UUID.fromString("00000000-0000-0000-0000-000000000002");

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
        InventorySourceSnapshot source = new InventorySourceSnapshot(
                sourceId,
                36,
                List.of(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(sourceId, slotIndex),
                        new ItemStack(itemId, count, 64),
                        count,
                        ""
                )),
                ""
        );
        return new InventoryAuthoritySnapshot(null, Map.of(sourceId, source), CursorStateSnapshot.empty());
    }
}
