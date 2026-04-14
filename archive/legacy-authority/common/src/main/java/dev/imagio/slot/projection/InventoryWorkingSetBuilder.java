package dev.imagio.slot.projection;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.model.SlotRef;
import dev.imagio.slot.client.model.StackSnapshot;
import dev.imagio.slot.client.model.WorkingSetAggregator;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.client.source.InventorySource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class InventoryWorkingSetBuilder {
    private final WorkingSetAggregator aggregator = new WorkingSetAggregator();

    public Collector collector(Map<String, InventoryViewData.SourceInfo> sources) {
        return new Collector(Map.copyOf(sources));
    }

    public static Map<String, InventoryViewData.SourceInfo> sourceInfos(Iterable<? extends InventorySource> sources) {
        Map<String, InventoryViewData.SourceInfo> sourceInfos = new LinkedHashMap<>();
        for (InventorySource source : sources) {
            sourceInfos.put(
                    source.id(),
                    new InventoryViewData.SourceInfo(
                            source.id(),
                            source.displayName(),
                            source.stableOrder()
                    )
            );
        }
        return Map.copyOf(sourceInfos);
    }

    public final class Collector {
        private final Map<String, InventoryViewData.SourceInfo> sources;
        private final Map<ItemIdentity, SlotCategory> resolvedCategories = new LinkedHashMap<>();
        private final Map<ItemIdentity, ItemStack> displayStacks = new LinkedHashMap<>();
        private final List<StackSnapshot> snapshots = new ArrayList<>();

        private Collector(Map<String, InventoryViewData.SourceInfo> sources) {
            this.sources = sources;
        }

        public void addStack(ItemStack stack, String sourceId, int slotIndex) {
            addStack(stack, sourceId, slotIndex, stack == null ? 0 : stack.getCount());
        }

        public void addStack(ItemStack stack, String sourceId, int slotIndex, int count) {
            if (stack == null || stack.isEmpty() || !sources.containsKey(sourceId)) {
                return;
            }

            ItemIdentity identity = ItemBehaviorPolicy.createIdentity(stack);
            resolvedCategories.putIfAbsent(identity, ItemBehaviorPolicy.resolveCategory(identity, stack));
            displayStacks.putIfAbsent(identity, stack.copy());
            snapshots.add(new StackSnapshot(identity, new SlotRef(sourceId, slotIndex), Math.max(0, count)));
        }

        public int snapshotCount() {
            return snapshots.size();
        }

        public InventoryViewData build(CollectionStore collectionStore) {
            return build(collectionStore, sourceId -> true);
        }

        public InventoryViewData build(CollectionStore collectionStore, Predicate<String> carriedSourceFilter) {
            List<ItemEntry> aggregated = aggregator.aggregate(
                    snapshots,
                    resolvedCategories::get,
                    collectionStore::isFavorite,
                    collectionStore::collectionsFor
            );
            List<ItemEntry> resolvedEntries = new ArrayList<>(aggregated.size());
            for (ItemEntry entry : aggregated) {
                ItemStack displayStack = displayStacks.get(entry.identity());
                if (displayStack == null || displayStack.isEmpty()) {
                    resolvedEntries.add(entry);
                    continue;
                }

                var fallbackGrouping = ItemBehaviorPolicy.resolveFallbackGrouping(entry.identity(), entry.category());
                resolvedEntries.add(new ItemEntry(
                        entry.identity(),
                        entry.totalCount(),
                        entry.perSourceCounts(),
                        entry.backingSlots(),
                        entry.category(),
                        entry.favorite(),
                        InventoryViewDataSupport.resolveCollections(collectionStore, entry, displayStack),
                        fallbackGrouping == null ? null : fallbackGrouping.id(),
                        fallbackGrouping == null ? null : fallbackGrouping.label()
                ));
            }
            return new InventoryViewData(
                    InventoryViewDataSupport.buildEntryViews(resolvedEntries, displayStacks),
                    InventoryViewDataSupport.buildSections(collectionStore, resolvedEntries, carriedSourceFilter),
                    sources,
                    InventoryViewDataSupport.buildCollectionNames(collectionStore)
            );
        }
    }
}
