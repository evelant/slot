package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentitySamplerTest {
    @Test
    void sameSeedProducesSameSample() {
        List<ItemIdentity> pool = pool(50);

        List<ItemIdentity> first = IdentitySampler.sample(pool, 10, 42L);
        List<ItemIdentity> second = IdentitySampler.sample(pool, 10, 42L);

        assertEquals(first, second);
    }

    @Test
    void differentSeedsProduceDifferentSample() {
        List<ItemIdentity> pool = pool(50);

        List<ItemIdentity> first = IdentitySampler.sample(pool, 10, 1L);
        List<ItemIdentity> second = IdentitySampler.sample(pool, 10, 2L);

        assertNotEquals(first, second);
    }

    @Test
    void sampleContainsNoDuplicates() {
        List<ItemIdentity> pool = pool(100);

        List<ItemIdentity> sample = IdentitySampler.sample(pool, 50, 7L);

        assertEquals(50, sample.size());
        assertEquals(sample.size(), new HashSet<>(sample).size());
    }

    @Test
    void sampleClampsCountToPoolSize() {
        List<ItemIdentity> pool = pool(5);

        List<ItemIdentity> sample = IdentitySampler.sample(pool, 100, 7L);

        assertEquals(pool.size(), sample.size());
        assertEquals(new HashSet<>(pool), new HashSet<>(sample));
    }

    @Test
    void emptyPoolYieldsEmptySample() {
        assertTrue(IdentitySampler.sample(List.of(), 10, 1L).isEmpty());
    }

    @Test
    void zeroCountYieldsEmptySample() {
        assertTrue(IdentitySampler.sample(pool(10), 0, 1L).isEmpty());
    }

    @Test
    void duplicatesInPoolAreCollapsedBeforeSampling() {
        ArrayList<ItemIdentity> pool = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            pool.add(ItemIdentity.of("mod:same"));
        }

        List<ItemIdentity> sample = IdentitySampler.sample(pool, 10, 1L);

        assertEquals(1, sample.size());
    }

    private static List<ItemIdentity> pool(int size) {
        ArrayList<ItemIdentity> pool = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            pool.add(ItemIdentity.of("test:item_" + index));
        }
        return pool;
    }
}
