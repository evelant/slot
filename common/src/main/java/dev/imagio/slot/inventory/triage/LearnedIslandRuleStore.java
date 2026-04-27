package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class LearnedIslandRuleStore {
    public static final int DEFAULT_MIN_CONFIRMATIONS = 2;

    // Umbrella/meta namespaces shared across mods or the vanilla game. They are too broad
    // to be a useful "this item is like that item" signal: every vanilla item shares
    // "minecraft", every common tag uses "c", etc. Tag and creative-tab adjacency remain
    // strong enough signals on their own.
    private static final Set<String> OVERLY_BROAD_NAMESPACES = Set.of(
            "minecraft",
            "c",
            "forge",
            "neoforge"
    );

    private final int minConfirmations;
    private final Map<LearnedAdjacencyKey, Map<String, Entry>> byAdjacency = new LinkedHashMap<>();

    public LearnedIslandRuleStore() {
        this(DEFAULT_MIN_CONFIRMATIONS);
    }

    public LearnedIslandRuleStore(int minConfirmations) {
        this.minConfirmations = Math.max(1, minConfirmations);
    }

    public int minConfirmations() {
        return minConfirmations;
    }

    public void recordAssignment(IslandSignalDescriptor descriptor, String islandId, long timestampEpochMillis) {
        Objects.requireNonNull(descriptor, "descriptor");
        if (islandId == null || islandId.isBlank()) {
            throw new IllegalArgumentException("islandId must not be blank");
        }
        for (LearnedAdjacencyKey key : adjacencyKeys(descriptor)) {
            Map<String, Entry> islandsForKey = byAdjacency.computeIfAbsent(key, ignored -> new LinkedHashMap<>());
            Entry entry = islandsForKey.computeIfAbsent(islandId, ignored -> new Entry());
            entry.confirmingIdentities.add(descriptor.identity());
            entry.lastConfirmedAtEpochMillis = Math.max(entry.lastConfirmedAtEpochMillis, timestampEpochMillis);
        }
    }

    public List<LearnedIslandRule> firingRulesFor(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return List.of();
        }
        LinkedHashMap<String, LearnedIslandRule> bestByIsland = new LinkedHashMap<>();
        for (LearnedAdjacencyKey key : adjacencyKeys(descriptor)) {
            Map<String, Entry> islandsForKey = byAdjacency.get(key);
            if (islandsForKey == null) {
                continue;
            }
            for (Map.Entry<String, Entry> islandEntry : islandsForKey.entrySet()) {
                Entry entry = islandEntry.getValue();
                int effective = entry.confirmingIdentities.size()
                        - (entry.confirmingIdentities.contains(descriptor.identity()) ? 1 : 0);
                if (effective < minConfirmations) {
                    continue;
                }
                LearnedIslandRule rule = new LearnedIslandRule(
                        key,
                        islandEntry.getKey(),
                        entry.confirmingIdentities,
                        entry.lastConfirmedAtEpochMillis
                );
                LearnedIslandRule existing = bestByIsland.get(islandEntry.getKey());
                if (existing == null || betterRule(rule, existing)) {
                    bestByIsland.put(islandEntry.getKey(), rule);
                }
            }
        }
        return List.copyOf(bestByIsland.values());
    }

    public List<LearnedIslandRule> allRules() {
        ArrayList<LearnedIslandRule> all = new ArrayList<>();
        for (Map.Entry<LearnedAdjacencyKey, Map<String, Entry>> adjacencyEntry : byAdjacency.entrySet()) {
            LearnedAdjacencyKey key = adjacencyEntry.getKey();
            for (Map.Entry<String, Entry> islandEntry : adjacencyEntry.getValue().entrySet()) {
                Entry entry = islandEntry.getValue();
                all.add(new LearnedIslandRule(
                        key,
                        islandEntry.getKey(),
                        entry.confirmingIdentities,
                        entry.lastConfirmedAtEpochMillis
                ));
            }
        }
        return List.copyOf(all);
    }

    public void clear() {
        byAdjacency.clear();
    }

    static List<LearnedAdjacencyKey> adjacencyKeys(IslandSignalDescriptor descriptor) {
        ArrayList<LearnedAdjacencyKey> keys = new ArrayList<>();
        for (String tag : descriptor.itemTags()) {
            keys.add(LearnedAdjacencyKey.tag(tag));
        }
        // FacetIndex material_family adjacency lets the rule fire across
        // shape variants of the same material — e.g. homing oak_planks +
        // oak_log + oak_stairs to "Wood" suggests the same island for
        // oak_wood, even though their item-tag sets differ. Tag-only
        // adjacency couldn't span that gap.
        String materialFamily = descriptor.materialFamily();
        if (materialFamily != null && !materialFamily.isBlank()) {
            keys.add(LearnedAdjacencyKey.materialFamily(materialFamily));
        }
        String namespace = descriptor.namespace();
        if (!namespace.isBlank() && !OVERLY_BROAD_NAMESPACES.contains(namespace)) {
            keys.add(LearnedAdjacencyKey.namespace(namespace));
        }
        if (!descriptor.creativeTabId().isBlank()) {
            keys.add(LearnedAdjacencyKey.creativeTab(descriptor.creativeTabId()));
        }
        return List.copyOf(keys);
    }

    private static boolean betterRule(LearnedIslandRule candidate, LearnedIslandRule current) {
        int priorityCompare = Integer.compare(candidate.adjacency().priorityRank(), current.adjacency().priorityRank());
        if (priorityCompare != 0) {
            return priorityCompare < 0;
        }
        int confirmationCompare = Integer.compare(candidate.confirmations(), current.confirmations());
        if (confirmationCompare != 0) {
            return confirmationCompare > 0;
        }
        return candidate.lastConfirmedAtEpochMillis() > current.lastConfirmedAtEpochMillis();
    }

    private static final class Entry {
        private final LinkedHashSet<ItemIdentity> confirmingIdentities = new LinkedHashSet<>();
        private long lastConfirmedAtEpochMillis;
    }
}
