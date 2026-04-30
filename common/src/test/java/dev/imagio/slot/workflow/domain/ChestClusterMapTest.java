package dev.imagio.slot.workflow.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestClusterMapTest {
    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID C = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void emptyMapEmptyClusters() {
        ChestClusterMap map = ChestClusterMap.derive(ClaimedChestMap.empty());
        assertTrue(map.clusters().isEmpty());
        assertNull(map.clusterId(A));
    }

    @Test
    void chestsWithinThresholdShareCluster() {
        ClaimedChestMap chests = chestMap(
                chest(A, "minecraft:overworld", 0, 64, 0),
                chest(B, "minecraft:overworld", 5, 64, 5),
                chest(C, "minecraft:overworld", 10, 64, 0)
        );
        ChestClusterMap map = ChestClusterMap.derive(chests);
        assertEquals(1, map.clusters().size());
        assertEquals(map.clusterId(A), map.clusterId(B));
        assertEquals(map.clusterId(A), map.clusterId(C));
        assertEquals("Storage Area 1", map.clusters().get(0).defaultLabel());
    }

    @Test
    void chestsBeyondThresholdSplitIntoClusters() {
        ClaimedChestMap chests = chestMap(
                chest(A, "minecraft:overworld", 0, 64, 0),
                chest(B, "minecraft:overworld", 200, 64, 0)
        );
        ChestClusterMap map = ChestClusterMap.derive(chests);
        assertEquals(2, map.clusters().size());
        assertNotEquals(map.clusterId(A), map.clusterId(B));
    }

    @Test
    void chainedChestsAreUnioned() {
        // A — B — C is one cluster even though A and C are 24 blocks apart.
        ClaimedChestMap chests = chestMap(
                chest(A, "minecraft:overworld", 0, 64, 0),
                chest(B, "minecraft:overworld", 12, 64, 0),
                chest(C, "minecraft:overworld", 24, 64, 0)
        );
        ChestClusterMap map = ChestClusterMap.derive(chests);
        assertEquals(1, map.clusters().size());
    }

    @Test
    void differentDimensionsAreSeparate() {
        ClaimedChestMap chests = chestMap(
                chest(A, "minecraft:overworld", 0, 64, 0),
                chest(B, "minecraft:the_nether", 0, 64, 0)
        );
        ChestClusterMap map = ChestClusterMap.derive(chests);
        assertEquals(2, map.clusters().size());
        assertNotEquals(map.clusterId(A), map.clusterId(B));
    }

    @Test
    void clusterOrdinalsAreOneIndexed() {
        ClaimedChestMap chests = chestMap(
                chest(A, "minecraft:overworld", 0, 64, 0),
                chest(B, "minecraft:overworld", 200, 64, 0),
                chest(C, "minecraft:overworld", 400, 64, 0)
        );
        ChestClusterMap map = ChestClusterMap.derive(chests);
        List<ChestClusterMap.Cluster> clusters = map.clusters();
        assertEquals(3, clusters.size());
        assertEquals(1, clusters.get(0).ordinal());
        assertEquals(2, clusters.get(1).ordinal());
        assertEquals(3, clusters.get(2).ordinal());
    }

    @Test
    void clusterIdStableAcrossRedrives() {
        ClaimedChestMap chests = chestMap(
                chest(A, "minecraft:overworld", 0, 64, 0),
                chest(B, "minecraft:overworld", 200, 64, 0)
        );
        ChestClusterMap a = ChestClusterMap.derive(chests);
        ChestClusterMap b = ChestClusterMap.derive(chests);
        assertEquals(a.clusterId(A), b.clusterId(A));
        assertEquals(a.clusterId(B), b.clusterId(B));
    }

    private static ClaimedChestMap chestMap(ClaimedChest... chests) {
        return new ClaimedChestMap(List.of(chests));
    }

    private static ClaimedChest chest(UUID id, String dim, int x, int y, int z) {
        return new ClaimedChest(id, Set.of(new ChestAnchor(dim, x, y, z)), 0, 0, "");
    }
}
