package dev.imagio.slot.atlas;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.StorageAreaMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageZoneAutoPlacementTest {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final StorageZoneAutoPlacement.Config CONFIG = new StorageZoneAutoPlacement.Config(
            48, 160, 160, 2400, 0, 4.0
    );

    @Test
    void firstClaimLandsOnDefaultSeed() {
        ChestAnchor anchor = new ChestAnchor(OVERWORLD, 100, 64, -200);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(List.of(), anchor, CONFIG);

        assertEquals(2400, result.atlasX());
        assertEquals(0, result.atlasY());
        assertFalse(result.usedNeighbor());
    }

    @Test
    void nearbyClaimSeedsPlacementFromNeighborAtlasPosition() {
        ChestAnchor existingAnchor = new ChestAnchor(OVERWORLD, 100, 64, 100);
        ClaimedChest existing = new ClaimedChest(
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        // New anchor 10 blocks east of the existing one (within the 48-block radius)
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 110, 64, 100);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(
                List.of(existing), newAnchor, CONFIG
        );

        assertTrue(result.usedNeighbor());
        // Scaled world delta (10 * 4.0 = 40) should place new tile within one grid step
        int deltaX = Math.abs(result.atlasX() - existing.atlasX());
        int deltaY = Math.abs(result.atlasY() - existing.atlasY());
        assertTrue(deltaX <= CONFIG.atlasStepX() * 2, "delta " + deltaX + " should be near neighbor");
        assertTrue(deltaY <= CONFIG.atlasStepY() * 2, "delta " + deltaY + " should be near neighbor");
        // Must not land exactly on the neighbor
        assertFalse(result.atlasX() == existing.atlasX() && result.atlasY() == existing.atlasY());
    }

    @Test
    void distantClaimDoesNotPickNeighbor() {
        ChestAnchor existingAnchor = new ChestAnchor(OVERWORLD, 0, 64, 0);
        ClaimedChest existing = new ClaimedChest(
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        // 500 blocks away — well outside the 48-block radius
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 500, 64, 500);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(
                List.of(existing), newAnchor, CONFIG
        );

        assertFalse(result.usedNeighbor());
        // Would seed to default, but default cell is occupied, so should bump to a free cell
        assertFalse(result.atlasX() == existing.atlasX() && result.atlasY() == existing.atlasY());
    }

    @Test
    void claimsInDifferentDimensionsAreIgnored() {
        ChestAnchor existingAnchor = new ChestAnchor("minecraft:the_nether", 100, 64, 100);
        ClaimedChest existing = new ClaimedChest(
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 100, 64, 100);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(
                List.of(existing), newAnchor, CONFIG
        );

        assertFalse(result.usedNeighbor(), "cross-dimension neighbor must not count");
    }

    @Test
    void collisionAtDefaultSeedBumpsToFreeCell() {
        ChestAnchor existingAnchor = new ChestAnchor(OVERWORLD, 0, 64, 0);
        // Existing claim parked at the exact default seed
        ClaimedChest existing = new ClaimedChest(
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        // New claim unrelated to it in world space (> radius)
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 1000, 64, 1000);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(
                List.of(existing), newAnchor, CONFIG
        );

        assertNotEquals(existing.atlasX(), result.atlasX(),
                "must not overlap the existing tile at default seed");
    }

    @Test
    void placementOutputIsSnappedToGrid() {
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 7, 64, 3);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(
                List.of(), newAnchor, CONFIG
        );

        assertEquals(0, result.atlasX() % CONFIG.atlasStepX());
        assertEquals(0, result.atlasY() % CONFIG.atlasStepY());
    }

    @Test
    void areaRelativePlacementIgnoresChestsInOtherAreas() {
        java.util.UUID mountainArea = java.util.UUID.randomUUID();
        java.util.UUID derrickArea = java.util.UUID.randomUUID();
        ChestAnchor mountainAnchor = new ChestAnchor(OVERWORLD, 0, 64, 0);
        ChestAnchor derrickAnchor = new ChestAnchor(OVERWORLD, 5, 64, 0);
        ClaimedChest mountainChest = new ClaimedChest(
                java.util.UUID.randomUUID(), Set.of(mountainAnchor), 2400, 0, "", mountainArea
        );
        ClaimedChest derrickChest = new ClaimedChest(
                java.util.UUID.randomUUID(), Set.of(derrickAnchor), 8000, 8000, "", derrickArea
        );
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 6, 64, 0);

        StorageZoneAutoPlacement.Result result = StorageZoneAutoPlacement.compute(
                List.of(mountainChest, derrickChest), newAnchor, mountainArea, CONFIG
        );

        // The derrick chest is closer in world but in a different area; the
        // area-filtered call must seed off mountainChest only.
        assertTrue(result.usedNeighbor());
        assertTrue(Math.abs(result.atlasX() - mountainChest.atlasX()) <= CONFIG.atlasStepX() * 2,
                "should seed near mountain area chest, not derrick");
    }

    @Test
    void inferProximityAreaPicksClosestChestArea() {
        java.util.UUID mountainArea = java.util.UUID.randomUUID();
        ClaimedChest mountainChest = new ClaimedChest(
                java.util.UUID.randomUUID(),
                Set.of(new ChestAnchor(OVERWORLD, 0, 64, 0)),
                2400, 0, "", mountainArea
        );
        ChestAnchor newAnchor = new ChestAnchor(OVERWORLD, 5, 64, 5);

        java.util.UUID inferred = StorageZoneAutoPlacement.inferProximityArea(
                List.of(mountainChest), newAnchor, CONFIG.worldRadius()
        );

        assertEquals(mountainArea, inferred);
    }

    @Test
    void inferProximityAreaReturnsNullWhenNoNeighborInRange() {
        ClaimedChest distantChest = new ClaimedChest(
                java.util.UUID.randomUUID(),
                Set.of(new ChestAnchor(OVERWORLD, 0, 64, 0)),
                2400, 0, "", StorageAreaMap.DEFAULT_AREA_ID
        );
        ChestAnchor farAnchor = new ChestAnchor(OVERWORLD, 1000, 64, 1000);

        java.util.UUID inferred = StorageZoneAutoPlacement.inferProximityArea(
                List.of(distantChest), farAnchor, CONFIG.worldRadius()
        );

        assertEquals(null, inferred);
    }
}
