package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

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
        ItemIdentity key = key(identity);
        ChestAffinity direct = bonds.get(key);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<ItemIdentity, ChestAffinity> entry : bonds.entrySet()) {
            if (ItemIdentityMatcher.matchesMovable(entry.getKey(), key)) {
                return entry.getValue();
            }
        }
        return null;
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

    public ChestAffinityMap recordDeposit(UUID storageId, ItemIdentity identity, long tick) {
        if (storageId == null || identity == null) {
            return this;
        }
        ItemIdentity key = key(identity);
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> next = new LinkedHashMap<>(entries);
        LinkedHashMap<ItemIdentity, ChestAffinity> bonds = new LinkedHashMap<>(
                next.getOrDefault(storageId, Map.of()));
        ChestAffinity existing = affinity(storageId, key);
        if (existing != null) {
            bonds.keySet().removeIf(candidate -> ItemIdentityMatcher.matchesMovable(candidate, key));
        }
        ChestAffinity bumped = existing == null
                ? new ChestAffinity(key, 1, tick)
                : existing.bump(1, tick);
        bonds.put(key, bumped);
        next.put(storageId, Map.copyOf(bonds));
        return new ChestAffinityMap(next);
    }

    public ChestAffinityMap forget(UUID storageId, ItemIdentity identity) {
        if (storageId == null || identity == null) {
            return this;
        }
        Map<ItemIdentity, ChestAffinity> bonds = entries.get(storageId);
        if (bonds == null || bonds.isEmpty()) {
            return this;
        }
        ItemIdentity key = key(identity);
        LinkedHashMap<ItemIdentity, ChestAffinity> nextBonds = new LinkedHashMap<>(bonds);
        boolean removed = nextBonds.keySet().removeIf(candidate -> ItemIdentityMatcher.matchesMovable(candidate, key));
        if (!removed) {
            return this;
        }
        LinkedHashMap<UUID, Map<ItemIdentity, ChestAffinity>> next = new LinkedHashMap<>(entries);
        if (nextBonds.isEmpty()) {
            next.remove(storageId);
        } else {
            next.put(storageId, Map.copyOf(nextBonds));
        }
        return new ChestAffinityMap(next);
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
                    ItemIdentity key = key(bond.getKey());
                    ChestAffinity value = new ChestAffinity(
                            key,
                            bond.getValue().score(),
                            bond.getValue().lastTouchedTick());
                    copied.merge(key, value, ChestAffinityMap::merge);
                }
            }
            if (!copied.isEmpty()) {
                out.put(storageId, Map.copyOf(copied));
            }
        }
        return Map.copyOf(out);
    }

    private static ItemIdentity key(ItemIdentity identity) {
        return ItemIdentityMatcher.normalizeMovable(identity);
    }

    private static ChestAffinity merge(ChestAffinity left, ChestAffinity right) {
        ItemIdentity identity = key(left.identity());
        return new ChestAffinity(
                identity,
                left.score() + right.score(),
                Math.max(left.lastTouchedTick(), right.lastTouchedTick()));
    }
}
