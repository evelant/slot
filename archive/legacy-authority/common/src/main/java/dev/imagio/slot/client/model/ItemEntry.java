package dev.imagio.slot.client.model;

import dev.imagio.slot.client.category.SlotCategory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ItemEntry {
    private final ItemIdentity identity;
    private final int totalCount;
    private final Map<String, Integer> perSourceCounts;
    private final List<SlotRef> backingSlots;
    private final SlotCategory category;
    private final boolean favorite;
    private final Set<String> collectionIds;
    private final String fallbackGroupId;
    private final String fallbackGroupLabel;

    public ItemEntry(
            ItemIdentity identity,
            int totalCount,
            Map<String, Integer> perSourceCounts,
            List<SlotRef> backingSlots,
            SlotCategory category,
            boolean favorite,
            Set<String> collectionIds,
            String fallbackGroupId,
            String fallbackGroupLabel
    ) {
        this.identity = identity;
        this.totalCount = totalCount;
        this.perSourceCounts = Map.copyOf(perSourceCounts);
        this.backingSlots = List.copyOf(backingSlots);
        this.category = category;
        this.favorite = favorite;
        this.collectionIds = Set.copyOf(collectionIds);
        this.fallbackGroupId = fallbackGroupId == null || fallbackGroupId.isBlank() ? null : fallbackGroupId;
        this.fallbackGroupLabel = fallbackGroupLabel == null || fallbackGroupLabel.isBlank() ? null : fallbackGroupLabel;
    }

    public ItemEntry(
            ItemIdentity identity,
            int totalCount,
            Map<String, Integer> perSourceCounts,
            List<SlotRef> backingSlots,
            SlotCategory category,
            boolean favorite,
            Set<String> collectionIds
    ) {
        this(identity, totalCount, perSourceCounts, backingSlots, category, favorite, collectionIds, null, null);
    }

    public ItemIdentity identity() {
        return identity;
    }

    public int totalCount() {
        return totalCount;
    }

    public Map<String, Integer> perSourceCounts() {
        return perSourceCounts;
    }

    public List<SlotRef> backingSlots() {
        return backingSlots;
    }

    public SlotCategory category() {
        return category;
    }

    public boolean favorite() {
        return favorite;
    }

    public Set<String> collectionIds() {
        return collectionIds;
    }

    public String fallbackGroupId() {
        return fallbackGroupId;
    }

    public String fallbackGroupLabel() {
        return fallbackGroupLabel;
    }
}
