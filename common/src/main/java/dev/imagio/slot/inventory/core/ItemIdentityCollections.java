package dev.imagio.slot.inventory.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Movable-aware collection helpers for {@link ItemIdentity}. Any workflow or
 * projection state that represents "this logical thing the player wants to
 * move/carry/find" should use these helpers instead of strict collection
 * membership. Exact component identity still matters where
 * {@link ItemIdentityMatcher#matchesMovable(ItemIdentity, ItemIdentity)} says
 * it matters; durability and other movable condition-only fingerprints do not.
 */
public final class ItemIdentityCollections {
    private ItemIdentityCollections() {
    }

    public static ItemIdentity key(ItemIdentity identity) {
        return ItemIdentityMatcher.normalizeMovable(identity);
    }

    public static Set<ItemIdentity> normalizedSet(Collection<ItemIdentity> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> normalized = new LinkedHashSet<>();
        for (ItemIdentity identity : source) {
            add(normalized, identity);
        }
        return normalized.isEmpty() ? Set.of() : Set.copyOf(normalized);
    }

    public static boolean contains(Collection<ItemIdentity> identities, ItemIdentity identity) {
        if (identities == null || identities.isEmpty() || identity == null) {
            return false;
        }
        ItemIdentity target = key(identity);
        if (identities.contains(target)) {
            return true;
        }
        for (ItemIdentity candidate : identities) {
            if (ItemIdentityMatcher.matchesMovable(candidate, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Direct lookup for collections whose keys were already materialized
     * through {@link #key(ItemIdentity)} / {@link #add(Collection, ItemIdentity)}.
     * Use this in hot projection paths that own the collection shape; use
     * {@link #contains(Collection, ItemIdentity)} for legacy or external maps
     * that may still contain un-normalized exact identities.
     */
    public static boolean containsCanonical(Collection<ItemIdentity> identities, ItemIdentity identity) {
        return identities != null
                && !identities.isEmpty()
                && identity != null
                && identities.contains(key(identity));
    }

    public static int count(Map<ItemIdentity, Integer> counts, ItemIdentity identity) {
        if (counts == null || counts.isEmpty() || identity == null) {
            return 0;
        }
        ItemIdentity target = key(identity);
        int best = Math.max(0, counts.getOrDefault(target, 0));
        for (Map.Entry<ItemIdentity, Integer> entry : counts.entrySet()) {
            Integer value = entry.getValue();
            if (value == null || value <= best) {
                continue;
            }
            if (ItemIdentityMatcher.matchesMovable(entry.getKey(), target)) {
                best = value;
            }
        }
        return best;
    }

    public static <V> V find(Map<ItemIdentity, V> entries, ItemIdentity identity) {
        if (entries == null || entries.isEmpty() || identity == null) {
            return null;
        }
        ItemIdentity target = key(identity);
        V exact = entries.get(target);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<ItemIdentity, V> entry : entries.entrySet()) {
            if (ItemIdentityMatcher.matchesMovable(entry.getKey(), target)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Direct lookup for maps whose keys were already materialized through
     * {@link #key(ItemIdentity)}. Avoids the movable scan used by
     * {@link #find(Map, ItemIdentity)} for legacy/raw maps.
     */
    public static <V> V findCanonical(Map<ItemIdentity, V> entries, ItemIdentity identity) {
        if (entries == null || entries.isEmpty() || identity == null) {
            return null;
        }
        return entries.get(key(identity));
    }

    public static <V> V findOrDefault(Map<ItemIdentity, V> entries, ItemIdentity identity, V defaultValue) {
        V value = find(entries, identity);
        return value == null ? defaultValue : value;
    }

    public static void add(Collection<ItemIdentity> targets, ItemIdentity identity) {
        if (targets != null && identity != null) {
            targets.add(key(identity));
        }
    }

    public static void mergePositive(Map<ItemIdentity, Integer> targets, ItemIdentity identity, Integer count) {
        if (targets == null || identity == null || count == null || count <= 0) {
            return;
        }
        targets.merge(key(identity), count, Math::max);
    }

    public static void mergeCount(Map<ItemIdentity, Integer> targets, ItemIdentity identity, int count) {
        if (targets == null || identity == null || count <= 0) {
            return;
        }
        targets.merge(key(identity), count, Integer::sum);
    }

    public static void putOrClear(Map<ItemIdentity, Integer> targets, ItemIdentity identity, int count) {
        if (targets == null || identity == null) {
            return;
        }
        ItemIdentity target = key(identity);
        removeMatching(targets, target);
        if (count > 0) {
            targets.put(target, count);
        }
    }

    public static boolean removeMatching(Collection<ItemIdentity> targets, ItemIdentity identity) {
        if (targets == null || targets.isEmpty() || identity == null) {
            return false;
        }
        ItemIdentity target = key(identity);
        boolean removed = false;
        Iterator<ItemIdentity> iterator = targets.iterator();
        while (iterator.hasNext()) {
            ItemIdentity candidate = iterator.next();
            if (ItemIdentityMatcher.matchesMovable(candidate, target)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    public static <V> boolean removeMatching(Map<ItemIdentity, V> targets, ItemIdentity identity) {
        if (targets == null || targets.isEmpty() || identity == null) {
            return false;
        }
        ItemIdentity target = key(identity);
        boolean removed = false;
        Iterator<ItemIdentity> iterator = targets.keySet().iterator();
        while (iterator.hasNext()) {
            ItemIdentity candidate = iterator.next();
            if (ItemIdentityMatcher.matchesMovable(candidate, target)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    public static <V> V putIfAbsent(Map<ItemIdentity, V> targets, ItemIdentity identity, V value) {
        if (targets == null || identity == null) {
            return null;
        }
        V existing = find(targets, identity);
        if (existing != null) {
            return existing;
        }
        targets.put(key(identity), value);
        return null;
    }

    public static <V> V computeIfAbsent(
            Map<ItemIdentity, V> targets,
            ItemIdentity identity,
            Function<ItemIdentity, V> valueFactory
    ) {
        if (targets == null || identity == null || valueFactory == null) {
            return null;
        }
        V existing = find(targets, identity);
        if (existing != null) {
            return existing;
        }
        ItemIdentity key = key(identity);
        V created = valueFactory.apply(key);
        targets.put(key, created);
        return created;
    }
}
