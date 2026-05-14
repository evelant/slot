package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChestAffinityDecayTest {
    private static final ItemIdentity REDSTONE = ItemIdentity.of("minecraft:redstone");
    private static final UUID CHEST = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final long DAY = ChestAffinity.DEFAULT_DECAY_TICKS_PER_POINT;

    @Test
    void noDecayBeforeOneIntervalElapsed() {
        ChestAffinity bond = new ChestAffinity(REDSTONE, 5, 0L);
        assertEquals(5, bond.effectiveScore(DAY - 1, DAY));
    }

    @Test
    void linearDecayPerInterval() {
        ChestAffinity bond = new ChestAffinity(REDSTONE, 5, 0L);
        assertEquals(4, bond.effectiveScore(DAY, DAY));
        assertEquals(3, bond.effectiveScore(2 * DAY, DAY));
        assertEquals(0, bond.effectiveScore(5 * DAY, DAY));
        assertEquals(0, bond.effectiveScore(100 * DAY, DAY));
    }

    @Test
    void bumpAfterDecayStartsFromDecayedScore() {
        ChestAffinity bond = new ChestAffinity(REDSTONE, 10, 0L);
        ChestAffinity bumped = bond.bump(1, 4 * DAY, DAY);
        // 10 - 4 (decay) + 1 (bump) = 7
        assertEquals(7, bumped.score());
        assertEquals(4 * DAY, bumped.lastTouchedTick());
    }

    @Test
    void bumpRefreshesLastTouchedTick() {
        ChestAffinity bond = new ChestAffinity(REDSTONE, 1, 100L);
        ChestAffinity bumped = bond.bump(1, 200L);
        assertEquals(200L, bumped.lastTouchedTick());
    }

    @Test
    void defaultEffectiveScoreDoesNotDecayDuringPlaytest() {
        ChestAffinity bond = new ChestAffinity(REDSTONE, 5, 0L);
        assertEquals(5, bond.effectiveScore(100 * DAY));
    }

    @Test
    void defaultBumpAddsToRawScoreDuringPlaytest() {
        ChestAffinity bond = new ChestAffinity(REDSTONE, 10, 0L);
        ChestAffinity bumped = bond.bump(1, 4 * DAY);
        assertEquals(11, bumped.score());
        assertEquals(4 * DAY, bumped.lastTouchedTick());
    }

    @Test
    void mapDecayedKeepsBondsWhileDefaultDecayDisabled() {
        LinkedHashMap<ItemIdentity, ChestAffinity> bonds = new LinkedHashMap<>();
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        bonds.put(REDSTONE, new ChestAffinity(REDSTONE, 1, 0L));
        bonds.put(stone, new ChestAffinity(stone, 5, 0L));
        ChestAffinityMap map = new ChestAffinityMap(Map.of(CHEST, bonds));

        ChestAffinityMap decayed = map.decayed(2 * DAY);
        assertSame(map, decayed);
        assertEquals(5, decayed.score(CHEST, stone));
        assertEquals(1, decayed.score(CHEST, REDSTONE));
        assertEquals(2, decayed.forChest(CHEST).size());
    }

    @Test
    void mapDecayedKeepsChestsWhileDefaultDecayDisabled() {
        ChestAffinityMap map = new ChestAffinityMap(Map.of(CHEST,
                Map.of(REDSTONE, new ChestAffinity(REDSTONE, 1, 0L))));
        ChestAffinityMap decayed = map.decayed(5 * DAY);
        assertEquals(1, decayed.score(CHEST, REDSTONE));
    }

    @Test
    void mapDecayedReturnsSelfWhenNoChange() {
        ChestAffinityMap map = new ChestAffinityMap(Map.of(CHEST,
                Map.of(REDSTONE, new ChestAffinity(REDSTONE, 5, 1000L))));
        ChestAffinityMap decayed = map.decayed(1000L + DAY - 1);
        assertEquals(5, decayed.score(CHEST, REDSTONE));
    }

    @Test
    void mapDecayedHandlesEmptyInputCheaply() {
        ChestAffinityMap empty = ChestAffinityMap.empty();
        assertSame(empty, empty.decayed(123456L));
    }
}
