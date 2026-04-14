package dev.imagio.slot.client.collection;

import dev.imagio.slot.client.model.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionStoreTest {
    @Test
    void collectionsAreCreatedSortedAndMutable() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition trains = store.createCollection("Create Trains");
        store.createCollection("Bee Setup");

        List<CollectionDefinition> collections = store.userCollections();
        assertEquals(List.of("Bee Setup", "Create Trains"), collections.stream().map(CollectionDefinition::name).toList());

        store.renameCollection(trains.id(), "Train Logistics");
        assertEquals("Train Logistics", store.userCollections().get(1).name());

        store.deleteCollection(trains.id());
        assertEquals(List.of("Bee Setup"), store.userCollections().stream().map(CollectionDefinition::name).toList());
    }

    @Test
    void favoritesAreBackedByCollectionMembership() {
        CollectionStore store = new CollectionStore();
        ItemIdentity wrench = ItemIdentity.of("create:wrench");

        assertFalse(store.isFavorite(wrench));
        store.setFavorite(wrench, true);
        assertTrue(store.isFavorite(wrench));
        assertTrue(store.collectionsFor(wrench).contains(CollectionStore.FAVORITES_ID));
        store.setFavorite(wrench, false);
        assertFalse(store.isFavorite(wrench));
    }

    @Test
    void snapshotRoundTripRestoresCollectionsAndMemberships() {
        CollectionStore original = new CollectionStore();
        CollectionDefinition trains = original.createCollection("Create Trains");
        CollectionDefinition bees = original.createCollection("Bee Setup");
        ItemIdentity wrench = ItemIdentity.of("create:wrench");
        ItemIdentity rail = ItemIdentity.of("minecraft:rail");

        original.setFavorite(wrench, true);
        original.addToCollection(trains.id(), wrench);
        original.addToCollection(bees.id(), rail);

        CollectionStore restored = new CollectionStore();
        restored.replaceWith(original.snapshot());

        assertEquals(
                List.of("Bee Setup", "Create Trains"),
                restored.userCollections().stream().map(CollectionDefinition::name).toList()
        );
        assertTrue(restored.isFavorite(wrench));
        assertEquals(Set.of(CollectionStore.FAVORITES_ID, trains.id()), restored.collectionsFor(wrench));
        assertEquals(Set.of(bees.id()), restored.collectionsFor(rail));
    }
}
