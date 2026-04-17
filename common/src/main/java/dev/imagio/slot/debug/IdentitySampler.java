package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class IdentitySampler {
    private IdentitySampler() {
    }

    public static List<ItemIdentity> sample(List<ItemIdentity> pool, int count, long seed) {
        Objects.requireNonNull(pool, "pool");
        if (count <= 0 || pool.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<ItemIdentity> deduplicated = new LinkedHashSet<>(pool.size());
        for (ItemIdentity identity : pool) {
            if (identity != null) {
                deduplicated.add(identity);
            }
        }
        if (deduplicated.isEmpty()) {
            return List.of();
        }
        ArrayList<ItemIdentity> shuffled = new ArrayList<>(deduplicated);
        Collections.shuffle(shuffled, new Random(seed));
        int limit = Math.min(count, shuffled.size());
        return List.copyOf(shuffled.subList(0, limit));
    }
}
