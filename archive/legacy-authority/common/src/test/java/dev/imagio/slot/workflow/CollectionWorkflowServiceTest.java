package dev.imagio.slot.workflow;

import dev.imagio.slot.client.collection.CollectionDefinition;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryViewData;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionWorkflowServiceTest {
    @Test
    void normalizeViewStateSeedsAndRepairsSelectedLoadout() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition tools = store.createCollection("Tools");
        CollectionWorkflowService workflow = new CollectionWorkflowService(store, CollectionViewStateController.NOOP);

        HotbarLoadoutDefinition first = workflow.createLoadout(
                tools.id(),
                "First",
                List.of(new HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                null
        );
        HotbarLoadoutDefinition second = workflow.createLoadout(
                tools.id(),
                "Second",
                List.of(new HotbarLoadoutSlot(1, ItemIdentity.of("minecraft:dirt"))),
                null
        );

        assertEquals(second.id(), workflow.selectedLoadout(tools.id()).id());

        store.deleteHotbarLoadout(tools.id(), second.id());
        workflow.normalizeViewState(inventoryViewData(tools.id(), "Tools"));

        assertEquals(first.id(), workflow.selectedLoadout(tools.id()).id());
    }

    @Test
    void cycleSelectedLoadoutWrapsAcrossAvailableLoadouts() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition tools = store.createCollection("Tools");
        CollectionWorkflowService workflow = new CollectionWorkflowService(store, CollectionViewStateController.NOOP);

        HotbarLoadoutDefinition first = workflow.createLoadout(
                tools.id(),
                "First",
                List.of(new HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                null
        );
        HotbarLoadoutDefinition second = workflow.createLoadout(
                tools.id(),
                "Second",
                List.of(new HotbarLoadoutSlot(1, ItemIdentity.of("minecraft:dirt"))),
                null
        );

        assertTrue(workflow.cycleSelectedLoadout(tools.id(), 1));
        assertEquals(first.id(), workflow.selectedLoadout(tools.id()).id());
        assertTrue(workflow.cycleSelectedLoadout(tools.id(), -1));
        assertEquals(second.id(), workflow.selectedLoadout(tools.id()).id());
    }

    @Test
    void collectionMembershipAndFavoritesAreWorkflowOwnedMutations() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition tools = store.createCollection("Tools");
        CollectionWorkflowService workflow = new CollectionWorkflowService(store, CollectionViewStateController.NOOP);
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        assertTrue(workflow.toggleCollectionMembership(identity, store.collectionsFor(identity), tools.id()));
        assertTrue(store.collectionsFor(identity).contains(tools.id()));
        assertTrue(workflow.toggleFavorite(identity, store.isFavorite(identity)));
        assertTrue(store.isFavorite(identity));

        assertTrue(workflow.toggleCollectionMembership(identity, store.collectionsFor(identity), tools.id()));
        assertFalse(store.collectionsFor(identity).contains(tools.id()));
        assertTrue(workflow.toggleFavorite(identity, store.isFavorite(identity)));
        assertFalse(store.isFavorite(identity));
    }

    @Test
    void viewStateControllerIsUsedForCollapsedCollectionWorkflow() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition tools = store.createCollection("Tools");
        FakeCollectionViewStateController controller = new FakeCollectionViewStateController();
        CollectionWorkflowService workflow = new CollectionWorkflowService(store, controller);

        assertFalse(workflow.isCollectionCollapsed(tools.id()));
        workflow.toggleCollectionCollapsed(tools.id());
        assertTrue(workflow.isCollectionCollapsed(tools.id()));
        assertTrue(workflow.pinLoadoutsWhenCollectionCollapsed(tools.id()));
        workflow.togglePinLoadoutsWhenCollectionCollapsed(tools.id());
        assertFalse(workflow.pinLoadoutsWhenCollectionCollapsed(tools.id()));
    }

    @Test
    void updateSelectedLoadoutKeepsSelectionAnchored() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition tools = store.createCollection("Tools");
        CollectionWorkflowService workflow = new CollectionWorkflowService(store, CollectionViewStateController.NOOP);

        HotbarLoadoutDefinition created = workflow.createLoadout(
                tools.id(),
                "Builder",
                List.of(new HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                null
        );

        HotbarLoadoutDefinition updated = workflow.updateSelectedLoadout(
                tools.id(),
                List.of(new HotbarLoadoutSlot(2, ItemIdentity.of("minecraft:glass"))),
                ItemIdentity.of("minecraft:torch")
        );

        assertNotNull(updated);
        assertEquals(created.id(), updated.id());
        assertEquals(ItemIdentity.of("minecraft:glass"), updated.identityForSlot(2));
        assertEquals(ItemIdentity.of("minecraft:torch"), updated.offhandIdentity());
        assertEquals(created.id(), workflow.selectedLoadout(tools.id()).id());
    }

    @Test
    void renameLoadoutReturnsFalseWhenLoadoutIdIsUnknown() {
        CollectionStore store = new CollectionStore();
        CollectionDefinition tools = store.createCollection("Tools");
        CollectionWorkflowService workflow = new CollectionWorkflowService(store, CollectionViewStateController.NOOP);

        workflow.createLoadout(
                tools.id(),
                "Builder",
                List.of(new HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                null
        );

        assertFalse(workflow.renameLoadout(tools.id(), "missing-loadout", "Renamed"));
    }

    private static InventoryViewData inventoryViewData(String collectionId, String collectionName) {
        return new InventoryViewData(
                List.of(),
                List.of(InventoryViewData.Section.collection(collectionId, collectionName, 0)),
                Map.of(),
                Map.of(collectionId, collectionName)
        );
    }

    private static final class FakeCollectionViewStateController implements CollectionViewStateController {
        private final Map<String, Boolean> collapsed = new LinkedHashMap<>();
        private final Map<String, Boolean> pinnedLoadouts = new LinkedHashMap<>();

        @Override
        public boolean isCollectionCollapsed(String collectionId) {
            return collapsed.getOrDefault(collectionId, false);
        }

        @Override
        public void setCollectionCollapsed(String collectionId, boolean collapsed) {
            this.collapsed.put(collectionId, collapsed);
        }

        @Override
        public boolean pinLoadoutsWhenCollectionCollapsed(String collectionId) {
            return pinnedLoadouts.getOrDefault(collectionId, true);
        }

        @Override
        public void setPinLoadoutsWhenCollectionCollapsed(String collectionId, boolean pinned) {
            pinnedLoadouts.put(collectionId, pinned);
        }
    }
}
