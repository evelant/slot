package dev.imagio.slot.classification;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Cohort-sized gate for dynamic subsystem islands. Counts come from the
 * loaded classification dataset, not the player's current carried items,
 * so a large modpack mechanic can earn a stable island before the player
 * has picked up every item in that mechanic.
 */
public final class DynamicHomeCohortPolicy {
    public static final int DEFAULT_MIN_SUBSYSTEM_ITEMS = 10;

    private static final Object CACHE_LOCK = new Object();
    private static volatile FacetIndex cachedIndex;
    private static volatile DynamicHomeCohortPolicy cachedPolicy;

    private final Map<String, Integer> subsystemCounts;
    private final int minSubsystemItems;

    private DynamicHomeCohortPolicy(Map<String, Integer> subsystemCounts, int minSubsystemItems) {
        this.subsystemCounts = Collections.unmodifiableMap(new LinkedHashMap<>(subsystemCounts));
        this.minSubsystemItems = Math.max(1, minSubsystemItems);
    }

    public static DynamicHomeCohortPolicy current() {
        FacetIndex index = FacetIndexHolder.get();
        DynamicHomeCohortPolicy policy = cachedPolicy;
        if (policy != null && cachedIndex == index) {
            return policy;
        }
        synchronized (CACHE_LOCK) {
            if (cachedPolicy == null || cachedIndex != index) {
                cachedIndex = index;
                cachedPolicy = from(index);
            }
            return cachedPolicy;
        }
    }

    public static DynamicHomeCohortPolicy from(FacetIndex index) {
        return from(index, DEFAULT_MIN_SUBSYSTEM_ITEMS);
    }

    public static DynamicHomeCohortPolicy from(FacetIndex index, int minSubsystemItems) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        if (index != null && !index.isEmpty()) {
            for (String itemId : index.itemIds()) {
                if (itemId == null || itemId.isBlank()) {
                    continue;
                }
                IslandSignalDescriptor descriptor = descriptorFrom(index, itemId);
                if (descriptor.subsystems().isEmpty()
                        || IslandSuggestionTemplate.isTrophy(descriptor)
                        || !IslandSuggestionTemplate.firstMatchOrMisc(descriptor).allowsSubsystemGrouping()) {
                    continue;
                }
                Set<String> seenForItem = new LinkedHashSet<>();
                for (String subsystemId : descriptor.subsystems()) {
                    if (subsystemId == null || subsystemId.isBlank() || !seenForItem.add(subsystemId)) {
                        continue;
                    }
                    counts.merge(subsystemId, 1, Integer::sum);
                }
            }
        }
        return new DynamicHomeCohortPolicy(counts, minSubsystemItems);
    }

    public boolean qualifies(String subsystemId) {
        return count(subsystemId) >= minSubsystemItems;
    }

    public int count(String subsystemId) {
        if (subsystemId == null || subsystemId.isBlank()) {
            return 0;
        }
        return subsystemCounts.getOrDefault(subsystemId, 0);
    }

    public int minSubsystemItems() {
        return minSubsystemItems;
    }

    public Map<String, Integer> counts() {
        return subsystemCounts;
    }

    public Predicate<String> qualifier() {
        return this::qualifies;
    }

    private static IslandSignalDescriptor descriptorFrom(FacetIndex index, String itemId) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                namespaceOf(itemId),
                "",
                index.role(itemId).orElse(null),
                index.roleAlternatives(itemId),
                index.materialFamily(itemId).orElse(null),
                index.subsystems(itemId),
                index.activities(itemId),
                index.flavor(itemId).orElse(null),
                index.carryFrequency(itemId).orElse(null),
                index.rarity(itemId).orElse(null),
                index.origin(itemId).orElse(null),
                index.dyeColor(itemId).orElse(null),
                index.palette(itemId),
                index.form(itemId).orElse(null),
                index.emitsLight(itemId)
        );
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
