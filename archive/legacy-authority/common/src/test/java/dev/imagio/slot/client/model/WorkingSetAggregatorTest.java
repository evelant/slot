package dev.imagio.slot.client.model;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.collection.CollectionDefinition;
import dev.imagio.slot.client.collection.CollectionStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkingSetAggregatorTest {
    @Test
    void aggregatesCountsByIdentityAndSource() {
        CollectionStore collections = new CollectionStore();
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        ItemIdentity iron = ItemIdentity.of("minecraft:iron_ingot");
        CollectionDefinition trains = collections.createCollection("Create Trains");
        collections.setFavorite(stone, true);
        collections.addToCollection(trains.id(), iron);

        WorkingSetAggregator aggregator = new WorkingSetAggregator();
        List<ItemEntry> entries = aggregator.aggregate(
                List.of(
                        new StackSnapshot(stone, new SlotRef("main", 9), 16),
                        new StackSnapshot(stone, new SlotRef("main", 10), 8),
                        new StackSnapshot(stone, new SlotRef("bag", 0), 32),
                        new StackSnapshot(iron, new SlotRef("main", 11), 4)
                ),
                identity -> identity.equals(stone) ? SlotCategory.BUILDING : SlotCategory.MATERIALS,
                collections::isFavorite,
                collections::collectionsFor
        );

        ItemEntry stoneEntry = entries.stream().filter(entry -> entry.identity().equals(stone)).findFirst().orElseThrow();
        ItemEntry ironEntry = entries.stream().filter(entry -> entry.identity().equals(iron)).findFirst().orElseThrow();

        assertEquals(56, stoneEntry.totalCount());
        assertEquals(Map.of("main", 24, "bag", 32), stoneEntry.perSourceCounts());
        assertTrue(stoneEntry.favorite());
        assertEquals(SlotCategory.BUILDING, stoneEntry.category());

        assertEquals(4, ironEntry.totalCount());
        assertEquals(Set.of(trains.id()), ironEntry.collectionIds());
        assertEquals(SlotCategory.MATERIALS, ironEntry.category());
    }
}
