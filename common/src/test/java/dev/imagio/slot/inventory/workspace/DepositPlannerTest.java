package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
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
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Affinity-driven routing. Replaces the link-era tests; the planner now
 * looks up {@code affinity[chest, identity]} for proximate claimed chests.
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
    void facetSimilarBondsRouteWhenNoDirectAffinity() {
        // CHEST_A holds iron_ingot (affinity=3) and gold_ingot (affinity=3).
        // The carried identity is netherite_ingot — never deposited there
        // before, so direct affinity is 0. With the descriptorLookup
        // wired, the planner falls back to facet-similar bonds: all
        // three ingots share material_family=ingot and the c:ingots
        // tag, so CHEST_A becomes a candidate via facet affinity.
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        ItemIdentity goldIngot = ItemIdentity.of("minecraft:gold_ingot");
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> bonds = new LinkedHashMap<>();
        bonds.put(CHEST_A, Map.of(
                ironIngot, new ChestAffinity(ironIngot, 3, 0L),
                goldIngot, new ChestAffinity(goldIngot, 3, 0L)
        ));

        Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup = identity -> {
            String id = identity.itemId();
            return switch (id) {
                case "minecraft:iron_ingot" -> ingotDescriptor(id, "iron");
                case "minecraft:gold_ingot" -> ingotDescriptor(id, "gold");
                case "minecraft:netherite_ingot" -> ingotDescriptor(id, "netherite");
                default -> null;
            };
        };

        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:netherite_ingot", 1),
                new ChestAffinityMap(bonds),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                descriptorLookup
        );

        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals("minecraft:netherite_ingot", assignment.itemId());
        assertEquals(List.of(CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void directAffinityOutranksFacetAffinity() {
        // CHEST_A has direct redstone affinity (score 1). CHEST_B has no
        // direct redstone but its other bonds (iron_ingot) share the
        // c:ingots tag with redstone — wait, actually redstone doesn't
        // share that tag. Use a more controlled scenario: target identity
        // shares material_family=iron with CHEST_B's existing bonds, and
        // CHEST_A has a low direct affinity for the target. Direct must
        // still win even if facet aggregate is higher.
        ItemIdentity rawIron = ItemIdentity.of("minecraft:raw_iron");
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        ItemIdentity ironBlock = ItemIdentity.of("minecraft:iron_block");
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> bonds = new LinkedHashMap<>();
        bonds.put(CHEST_A, Map.of(rawIron, new ChestAffinity(rawIron, 1, 0L)));
        bonds.put(CHEST_B, Map.of(
                ironIngot, new ChestAffinity(ironIngot, 5, 0L),
                ironBlock, new ChestAffinity(ironBlock, 5, 0L)
        ));

        Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup = identity -> {
            String id = identity.itemId();
            return switch (id) {
                case "minecraft:raw_iron" -> ingotDescriptor(id, "iron");
                case "minecraft:iron_ingot" -> ingotDescriptor(id, "iron");
                case "minecraft:iron_block" -> ingotDescriptor(id, "iron");
                default -> null;
            };
        };

        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:raw_iron", 4),
                new ChestAffinityMap(bonds),
                claimedMap(CHEST_A, CHEST_B),
                Set.of(CHEST_A.toString(), CHEST_B.toString()),
                descriptorLookup
        );

        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        // CHEST_A has direct affinity=1, CHEST_B has facet-aggregate
        // affinity=10. Direct still wins → CHEST_A first, CHEST_B second.
        assertEquals(List.of(CHEST_A.toString(), CHEST_B.toString()), assignment.candidateStorageIds());
    }

    @Test
    void explicitDepositKeepsLinkedRankingAheadOfSimilarityFallback() {
        ItemIdentity target = ItemIdentity.of("minecraft:netherite_ingot");
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup = identity -> switch (identity.itemId()) {
            case "minecraft:netherite_ingot" -> ingotDescriptor(identity.itemId(), "netherite");
            case "minecraft:iron_ingot" -> ingotDescriptor(identity.itemId(), "iron");
            default -> null;
        };

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B),
                affinity(CHEST_B, "minecraft:netherite_ingot", 1),
                Set.of(CHEST_A.toString(), CHEST_B.toString()),
                descriptorLookup,
                storageId -> storageId.equals(CHEST_A) ? Set.of(ironIngot) : Set.of(),
                storageId -> new DepositPlanner.ChestSpace(27, 1)
        );

        assertEquals(List.of(CHEST_B), ranked);
    }

    @Test
    void explicitDepositUsesFacetSimilarityWhenNoLinkedChestMatches() {
        ItemIdentity target = ItemIdentity.of("minecraft:netherite_ingot");
        ItemIdentity ironIngot = ItemIdentity.of("minecraft:iron_ingot");
        ItemIdentity oakLog = ItemIdentity.of("minecraft:oak_log");
        Function<ItemIdentity, IslandSignalDescriptor> descriptorLookup = identity -> switch (identity.itemId()) {
            case "minecraft:netherite_ingot" -> ingotDescriptor(identity.itemId(), "netherite");
            case "minecraft:iron_ingot" -> ingotDescriptor(identity.itemId(), "iron");
            case "minecraft:oak_log" -> woodDescriptor(identity.itemId());
            default -> null;
        };

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B),
                ChestAffinityMap.empty(),
                Set.of(CHEST_A.toString(), CHEST_B.toString()),
                descriptorLookup,
                storageId -> storageId.equals(CHEST_A) ? Set.of(ironIngot) : Set.of(oakLog),
                storageId -> new DepositPlanner.ChestSpace(27, 1)
        );

        assertEquals(List.of(CHEST_A, CHEST_B), ranked);
    }

    @Test
    void explicitDepositUsesEmptiestChestWhenNoSimilarityExists() {
        ItemIdentity target = ItemIdentity.of("minecraft:amethyst_shard");

        List<UUID> ranked = DepositPlanner.rankChestsForExplicitDeposit(
                target,
                claimedMap(CHEST_A, CHEST_B, CHEST_C),
                ChestAffinityMap.empty(),
                Set.of(CHEST_A.toString(), CHEST_B.toString(), CHEST_C.toString()),
                identity -> null,
                storageId -> Set.of(),
                storageId -> {
                    if (storageId.equals(CHEST_A)) {
                        return new DepositPlanner.ChestSpace(27, 20);
                    }
                    if (storageId.equals(CHEST_B)) {
                        return new DepositPlanner.ChestSpace(27, 1);
                    }
                    return new DepositPlanner.ChestSpace(27, 12);
                }
        );

        assertEquals(List.of(CHEST_B, CHEST_C, CHEST_A), ranked);
    }

    @Test
    void bulkDepositWithReservationDepositsOnlyExcessToPresenceChest() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:redstone", 5),
                ChestAffinityMap.empty(),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                null,
                storageId -> storageId.equals(CHEST_A)
                        ? Set.of(ItemIdentity.of("minecraft:redstone"))
                        : Set.of(),
                identity -> identity.itemId().equals("minecraft:redstone") ? 3 : 0
        );

        assertEquals(1, plan.assignments().size());
        DepositPlan.Assignment assignment = plan.assignments().get(0);
        assertEquals("minecraft:redstone", assignment.itemId());
        assertEquals(2, assignment.count());
        assertEquals(List.of(CHEST_A.toString()), assignment.candidateStorageIds());
    }

    @Test
    void bulkDepositWithReservationDoesNotUseExplicitSimilarityOrEmptyChestFallback() {
        DepositPlan plan = DepositPlanner.plan(
                authority(BuiltinInventoryIds.PLAYER_MAIN, 0, "minecraft:amethyst_shard", 5),
                ChestAffinityMap.empty(),
                claimedMap(CHEST_A),
                Set.of(CHEST_A.toString()),
                identity -> null,
                storageId -> Set.of(),
                identity -> identity.itemId().equals("minecraft:amethyst_shard") ? 3 : 0
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void facetFallbackSkippedWhenLookupReturnsNull() {
        // No descriptorLookup → exact-match identity affinity only.
        // (This is the legacy 4-arg overload.)
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

    private static IslandSignalDescriptor ingotDescriptor(String itemId, String materialFamily) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of("c:ingots"),
                "minecraft",
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
        );
    }

    private static IslandSignalDescriptor woodDescriptor(String itemId) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of("minecraft:logs"),
                "minecraft",
                "",
                "building",
                null,
                "wood_oak",
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
        );
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
