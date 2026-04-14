package dev.imagio.slot.client.model;

import dev.imagio.slot.client.category.SlotCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class WorkingSetAggregator {
    public List<ItemEntry> aggregate(
            Collection<StackSnapshot> snapshots,
            Function<ItemIdentity, SlotCategory> categoryResolver,
            Predicate<ItemIdentity> favoriteLookup,
            Function<ItemIdentity, Set<String>> collectionLookup
    ) {
        Map<ItemIdentity, MutableItemEntry> grouped = new LinkedHashMap<>();

        for (StackSnapshot snapshot : snapshots) {
            MutableItemEntry entry = grouped.computeIfAbsent(
                    snapshot.identity(),
                    identity -> new MutableItemEntry(
                            identity,
                            defaultCategory(categoryResolver.apply(identity)),
                            favoriteLookup.test(identity),
                            defaultCollections(collectionLookup.apply(identity))
                    )
            );
            entry.totalCount += snapshot.count();
            entry.backingSlots.add(snapshot.slotRef());
            entry.perSourceCounts.merge(snapshot.slotRef().sourceId(), snapshot.count(), Integer::sum);
        }

        List<ItemEntry> entries = new ArrayList<>(grouped.size());
        for (MutableItemEntry entry : grouped.values()) {
            entries.add(entry.freeze());
        }

        return List.copyOf(entries);
    }

    private static SlotCategory defaultCategory(SlotCategory category) {
        return category == null ? SlotCategory.MISC : category;
    }

    private static Set<String> defaultCollections(Set<String> collectionIds) {
        return collectionIds == null ? Set.of() : Set.copyOf(collectionIds);
    }

    private static final class MutableItemEntry {
        private final ItemIdentity identity;
        private final SlotCategory category;
        private final boolean favorite;
        private final Set<String> collectionIds;
        private final Map<String, Integer> perSourceCounts = new LinkedHashMap<>();
        private final List<SlotRef> backingSlots = new ArrayList<>();
        private int totalCount;

        private MutableItemEntry(ItemIdentity identity, SlotCategory category, boolean favorite, Set<String> collectionIds) {
            this.identity = identity;
            this.category = category;
            this.favorite = favorite;
            this.collectionIds = collectionIds;
        }

        private ItemEntry freeze() {
            return new ItemEntry(identity, totalCount, perSourceCounts, backingSlots, category, favorite, collectionIds, null, null);
        }
    }
}
