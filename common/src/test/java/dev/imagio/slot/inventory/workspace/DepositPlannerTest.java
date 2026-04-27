package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestLink;
import dev.imagio.slot.workflow.domain.ChestLinkMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.StorageAreaMap;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepositPlannerTest {
    private static final String MACHINES_ID = "island.machines";
    private static final String FOOD_ID = "island.food";
    private static final UUID CHEST_MACHINES = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CHEST_MACHINES_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CHEST_FOOD = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void emptyProximateSetProducesEmptyPlan() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                Set.of()
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void stackWithoutHomeIsIgnored() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:bone", 4))))),
                VisualHomeMap.empty(),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                Set.of(CHEST_MACHINES.toString())
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void stackHomedInTriageIsIgnored() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"),
                        assignment(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE))),
                linkMap(List.of()),
                Set.of()
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void homedStackWithoutLinkIsIgnored() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                ChestLinkMap.empty(),
                Set.of(CHEST_MACHINES.toString())
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void linkedButDistantChestIsExcluded() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 5, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                Set.of(CHEST_FOOD.toString())
        );
        assertTrue(plan.isEmpty());
    }

    @Test
    void homedStackWithProximateLinkedChestIsAssigned() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 5, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                Set.of(CHEST_MACHINES.toString())
        );
        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals(BuiltinInventoryIds.PLAYER_MAIN, assignment.laneId());
        assertEquals(5, assignment.slotIndex());
        assertEquals("minecraft:redstone", assignment.itemId());
        assertEquals(List.of(CHEST_MACHINES.toString()), assignment.candidateStorageIds());
    }

    @Test
    void multipleLinkedChestsProduceOrderedCandidates() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        List.of(entry(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 3, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(
                        new ChestLink(MACHINES_ID, CHEST_MACHINES),
                        new ChestLink(MACHINES_ID, CHEST_MACHINES_B)
                )),
                new LinkedHashSet<>(Set.of(CHEST_MACHINES.toString(), CHEST_MACHINES_B.toString()))
        );
        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, assignment.laneId());
        assertEquals(3, assignment.slotIndex());
        assertEquals(2, assignment.candidateStorageIds().size());
        assertTrue(assignment.candidateStorageIds().contains(CHEST_MACHINES.toString()));
        assertTrue(assignment.candidateStorageIds().contains(CHEST_MACHINES_B.toString()));
    }

    @Test
    void plansIncludeBackpackCarriedSources() {
        // Regression guard against the "hardcoded vanilla-lane scan" anti-pattern
        // that caused Deposit to silently ignore items living in backpacks. A
        // provider-registered carried source (namespaced like SB does its
        // backpacks) must be walked by the planner just like main/hotbar/offhand.
        String backpackSourceId = "sophisticatedbackpacks:carried/test-backpack-uuid";
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(backpackSourceId,
                        List.of(entry(backpackSourceId, 7, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                Set.of(CHEST_MACHINES.toString())
        );
        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals(backpackSourceId, assignment.laneId(),
                "assignment.laneId must be the backpack source id, not a builtin lane");
        assertEquals(7, assignment.slotIndex());
        assertEquals("minecraft:redstone", assignment.itemId());
    }

    @Test
    void plansSpanMainHotbarAndOffhandLanes() {
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:redstone", 16))),
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        List.of(entry(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 2, stack("minecraft:redstone", 8))),
                        BuiltinInventoryIds.PLAYER_OFFHAND,
                        List.of(entry(BuiltinInventoryIds.PLAYER_OFFHAND, 0, stack("minecraft:bread", 1)))
                )),
                homeMap(Map.of(
                        ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID),
                        ItemIdentity.of("minecraft:bread"), assignment(FOOD_ID)
                )),
                linkMap(List.of(
                        new ChestLink(MACHINES_ID, CHEST_MACHINES),
                        new ChestLink(FOOD_ID, CHEST_FOOD)
                )),
                new LinkedHashSet<>(Set.of(CHEST_MACHINES.toString(), CHEST_FOOD.toString()))
        );
        assertEquals(3, plan.assignments().size());
        assertTrue(plan.assignments().stream().anyMatch(a -> a.laneId().equals(BuiltinInventoryIds.PLAYER_MAIN)));
        assertTrue(plan.assignments().stream().anyMatch(a -> a.laneId().equals(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0)));
        assertTrue(plan.assignments().stream().anyMatch(a -> a.laneId().equals(BuiltinInventoryIds.PLAYER_OFFHAND)));
    }

    private static InventoryAuthoritySnapshot authority(Map<String, List<InventoryEntrySnapshot>> entriesBySource) {
        LinkedHashMap<String, InventorySourceSnapshot> sources = new LinkedHashMap<>();
        for (Map.Entry<String, List<InventoryEntrySnapshot>> entry : entriesBySource.entrySet()) {
            sources.put(entry.getKey(), new InventorySourceSnapshot(entry.getKey(), Math.max(36, entry.getValue().size()), entry.getValue(), ""));
        }
        return new InventoryAuthoritySnapshot(null, sources, CursorStateSnapshot.empty());
    }

    private static InventoryEntrySnapshot entry(String sourceId, int slotIndex, ItemStack stack) {
        return new InventoryEntrySnapshot(InventoryEntryKey.slot(sourceId, slotIndex), stack, stack.getCount(), "");
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static VisualHomeMap homeMap(Map<ItemIdentity, VisualHomeAssignment> assignments) {
        List<VisualAtlasIsland> islands = List.of(
                new VisualAtlasIsland(MACHINES_ID, "Machines", VisualAtlasIslandKind.PLAYER,
                        0, 0, 0xCC5A4A6E, null),
                new VisualAtlasIsland(FOOD_ID, "Food", VisualAtlasIslandKind.PLAYER,
                        0, 0, 0xCC5A4A6E, null)
        );
        return new VisualHomeMap(islands, assignments, Set.of());
    }

    private static VisualHomeAssignment assignment(String islandId) {
        return new VisualHomeAssignment(
                ItemIdentity.of("minecraft:placeholder"),
                islandId,
                0,
                VisualHomeOrigin.PLAYER_PLACED,
                true
        );
    }

    private static ChestLinkMap linkMap(List<ChestLink> links) {
        return new ChestLinkMap(new LinkedHashSet<>(links));
    }

    @Test
    void chestInProximateAreaIsAcceptedEvenWhenNotInProximateStorageSet() {
        UUID mountainArea = UUID.randomUUID();
        ClaimedChestMap chestMap = new ClaimedChestMap(List.of(
                new ClaimedChest(
                        CHEST_MACHINES,
                        Set.of(new ChestAnchor("minecraft:overworld", 1, 64, 1)),
                        2400, 0, "Iron Chest", mountainArea
                )
        ));
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                chestMap,
                Set.of(),                       // no per-storage proximity
                Set.of(mountainArea)            // but the area is proximate
        );
        assertEquals(1, plan.assignments().size());
        assertTrue(plan.assignments().get(0).candidateStorageIds()
                .contains(CHEST_MACHINES.toString()));
    }

    @Test
    void chestOutsideProximateAreaAndStorageIsRejected() {
        UUID derrickArea = UUID.randomUUID();
        UUID mountainArea = UUID.randomUUID();
        ClaimedChestMap chestMap = new ClaimedChestMap(List.of(
                new ClaimedChest(
                        CHEST_MACHINES,
                        Set.of(new ChestAnchor("minecraft:overworld", 1, 64, 1)),
                        2400, 0, "Iron", derrickArea
                )
        ));
        DepositPlan plan = DepositPlanner.plan(
                authority(Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(entry(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:redstone", 16))))),
                homeMap(Map.of(ItemIdentity.of("minecraft:redstone"), assignment(MACHINES_ID))),
                linkMap(List.of(new ChestLink(MACHINES_ID, CHEST_MACHINES))),
                chestMap,
                Set.of(),
                Set.of(mountainArea)
        );
        assertTrue(plan.isEmpty(),
                "chest in derrick area must be rejected when only mountain is proximate");
    }
}
