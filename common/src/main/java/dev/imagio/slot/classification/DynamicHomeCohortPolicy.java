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
 * Cohort-sized gate for dynamic classification islands. Counts come from the
 * loaded classification dataset, not the player's current carried items, so a
 * large modpack storage group can earn a stable island before the player has
 * picked up every item in that group. {@code mod_subsystem} currently stays
 * semantic/query evidence because templates do not opt into subsystem wall
 * sections.
 */
public final class DynamicHomeCohortPolicy {
    public static final int DEFAULT_MIN_SUBSYSTEM_ITEMS = 10;
    // Temporary safety valve while the next vocabulary refresh is validated.
    // Keep counting groups for inspect/audit, but don't let stale generated
    // organization_group values materialize main-wall homes during rehome.
    public static final boolean ORGANIZATION_GROUP_HOMING_ENABLED = false;

    private static final Object CACHE_LOCK = new Object();
    private static volatile FacetIndex cachedIndex;
    private static volatile DynamicHomeCohortPolicy cachedPolicy;

    private final Map<String, Integer> subsystemCounts;
    private final Map<String, Integer> organizationGroupCounts;
    private final int minSubsystemItems;

    private DynamicHomeCohortPolicy(
            Map<String, Integer> subsystemCounts,
            Map<String, Integer> organizationGroupCounts,
            int minSubsystemItems
    ) {
        this.subsystemCounts = Collections.unmodifiableMap(new LinkedHashMap<>(subsystemCounts));
        this.organizationGroupCounts = Collections.unmodifiableMap(new LinkedHashMap<>(organizationGroupCounts));
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
        LinkedHashMap<String, Integer> groupCounts = new LinkedHashMap<>();
        if (index != null && !index.isEmpty()) {
            for (String itemId : index.itemIds()) {
                if (itemId == null || itemId.isBlank()) {
                    continue;
                }
                IslandSignalDescriptor descriptor = descriptorFrom(index, itemId);
                if (IslandSuggestionTemplate.isTrophy(descriptor)) {
                    continue;
                }
                IslandSuggestionTemplate parent = IslandSuggestionTemplate.firstMatchOrMisc(descriptor);
                if (parent.allowsSubsystemGrouping()) {
                    addCounts(counts, descriptor.subsystems());
                }
                if (parent.allowsOrganizationGrouping()) {
                    addCounts(groupCounts, descriptor.organizationGroups());
                }
            }
        }
        return new DynamicHomeCohortPolicy(counts, groupCounts, minSubsystemItems);
    }

    private static void addCounts(Map<String, Integer> counts, Iterable<String> ids) {
        if (ids == null) {
            return;
        }
        Set<String> seenForItem = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank() || !seenForItem.add(id)) {
                continue;
            }
            counts.merge(id, 1, Integer::sum);
        }
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

    public boolean organizationGroupQualifies(String groupId) {
        if (!ORGANIZATION_GROUP_HOMING_ENABLED) {
            return false;
        }
        return organizationGroupCount(groupId) >= minSubsystemItems;
    }

    public int organizationGroupCount(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return 0;
        }
        return organizationGroupCounts.getOrDefault(groupId, 0);
    }

    public int minSubsystemItems() {
        return minSubsystemItems;
    }

    public Map<String, Integer> counts() {
        return subsystemCounts;
    }

    public Map<String, Integer> organizationGroupCounts() {
        return organizationGroupCounts;
    }

    public Predicate<String> qualifier() {
        return this::qualifies;
    }

    public Predicate<String> organizationGroupQualifier() {
        return this::organizationGroupQualifies;
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
                index.organizationGroups(itemId),
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
