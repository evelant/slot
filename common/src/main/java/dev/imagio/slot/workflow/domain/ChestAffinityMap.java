package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-(chest, identity) learned affinity. Replaces the explicit
 * ChestLinkMap from the link era — routing reads from here.
 *
 * <p>{@link #score(UUID, ItemIdentity)} returns the raw persisted score.
 * For routing/UI, callers normally want {@link #scoreAt(UUID, ItemIdentity, long)}
 * or {@link #decayed(long)} to apply the per-bond decay rule.
 */
public record ChestAffinityMap(Map<UUID, Map<ItemIdentity, ChestAffinity>> entries) {
    public ChestAffinityMap {
        entries = copy(entries);
    }

    public static ChestAffinityMap empty() {
        return new ChestAffinityMap(Map.of());
    }

    public Map<ItemIdentity, ChestAffinity> forChest(UUID storageId) {
        if (storageId == null) {
            return Map.of();
        }
        Map<ItemIdentity, ChestAffinity> bonds = entries.get(storageId);
        return bonds == null ? Map.of() : bonds;
    }

    public ChestAffinity affinity(UUID storageId, ItemIdentity identity) {
        if (storageId == null || identity == null) {
            return null;
        }
        Map<ItemIdentity, ChestAffinity> bonds = entries.get(storageId);
        if (bonds == null) {
            return null;
        }
        return bonds.get(identity);
    }

    public int score(UUID storageId, ItemIdentity identity) {
        ChestAffinity bond = affinity(storageId, identity);
        return bond == null ? 0 : bond.score();
    }

    /** Decayed score at {@code currentTick}; never below zero. */
    public int scoreAt(UUID storageId, ItemIdentity identity, long currentTick) {
        ChestAffinity bond = affinity(storageId, identity);
        return bond == null ? 0 : bond.effectiveScore(currentTick);
    }

    /**
     * Return a copy of this map with every bond's score replaced by its
     * decayed value at {@code currentTick}. Bonds that decay to zero are
     * dropped entirely; chests that lose every bond are also dropped. Use
     * this once at the top of routing/UI projections so downstream code can
     * keep using {@link #score(UUID, ItemIdentity)} unchanged.
     */
    public ChestAffinityMap decayed(long currentTick) {
        if (entries.isEmpty() || !ChestAffinity.DECAY_ENABLED) {
            return this;
        }
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, Map<ItemIdentity, ChestAffinity>> entry : entries.entrySet()) {
            LinkedHashMap<ItemIdentity, ChestAffinity> bonds = new LinkedHashMap<>();
            for (Map.Entry<ItemIdentity, ChestAffinity> bond : entry.getValue().entrySet()) {
                int effective = bond.getValue().effectiveScore(currentTick);
                if (effective <= 0) {
                    continue;
                }
                if (effective == bond.getValue().score()) {
                    bonds.put(bond.getKey(), bond.getValue());
                } else {
                    bonds.put(bond.getKey(), new ChestAffinity(
                            bond.getValue().identity(),
                            effective,
                            bond.getValue().lastTouchedTick()
                    ));
                }
            }
            if (!bonds.isEmpty()) {
                result.put(entry.getKey(), Map.copyOf(bonds));
            }
        }
        return new ChestAffinityMap(result);
    }

    private static Map<UUID, Map<ItemIdentity, ChestAffinity>> copy(Map<UUID, Map<ItemIdentity, ChestAffinity>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> out = new LinkedHashMap<>(source.size());
        for (Map.Entry<UUID, Map<ItemIdentity, ChestAffinity>> entry : source.entrySet()) {
            UUID storageId = entry.getKey();
            Map<ItemIdentity, ChestAffinity> bonds = entry.getValue();
            if (storageId == null || bonds == null || bonds.isEmpty()) {
                continue;
            }
            LinkedHashMap<ItemIdentity, ChestAffinity> copied = new LinkedHashMap<>(bonds.size());
            for (Map.Entry<ItemIdentity, ChestAffinity> bond : bonds.entrySet()) {
                if (bond.getKey() != null && bond.getValue() != null && bond.getValue().score() > 0) {
                    copied.put(bond.getKey(), bond.getValue());
                }
            }
            if (!copied.isEmpty()) {
                out.put(storageId, Map.copyOf(copied));
            }
        }
        return Map.copyOf(out);
    }
}
