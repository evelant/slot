package dev.imagio.slot.atlas;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
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
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, ""
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
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, ""
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
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, ""
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
                UUID.randomUUID(), Set.of(existingAnchor), 2400, 0, ""
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
}
