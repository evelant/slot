package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSortMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowDomainRuntimeTest {
    @Test
    void runtimePersistsCollectionProtectionQueryAndRecentMutationsSynchronously() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        RecordingPort port = new RecordingPort();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                repository,
                new WorkflowDomainPersistenceService(port)
        );
        CollectionDefinition collection = runtime.collectionWorkflow().createCollection("Collection");
        QuickAccessLoadoutDefinition loadout = runtime.collectionWorkflow().createLoadout(
                collection.id(),
                "Builder",
                java.util.Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.QuickAccessLaneTarget("player.quick_access.lane_0", 0),
                        ItemIdentity.of("minecraft:stone")
                ))
        );

        runtime.setProtectedIdentity(ItemIdentity.of("minecraft:shield"), true);
        runtime.setProtectedTarget(new InventoryActionTarget.EquipmentTarget("equipment.offhand", 0), true);
        runtime.browseSessionState().replaceWith(new InventoryBrowseSessionState(
                new InventoryBrowseFilter("stone", InventoryBrowseFilterScope.ALL),
                InventoryBrowseSortMode.COUNT_DESC,
                InventoryBrowseGroupingMode.CATEGORY,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryPaneMembership.EXTERNAL,
                collection.id(),
                loadout.id(),
                "tool:craft",
                InventoryActionScope.VISIBLE_ROWS,
                new InventoryBrowseSubjectRef.LoadoutRef(collection.id(), loadout.id()),
                java.util.Set.of("collapsed")
        ));
        runtime.recordActivityEvent(new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.OBSERVED,
                ItemIdentity.of("minecraft:diamond"),
                2,
                null,
                null,
                "",
                "",
                java.util.List.of(),
                ""
        ));

        assertEquals(repository.snapshot(), port.saved);
        assertEquals("stone", port.saved.browseSessionState().filter().searchText());
        assertEquals(1, port.saved.collections().loadoutsByCollection().get(collection.id()).size());
        assertEquals(2, port.saved.recents().countsByIdentity().get(ItemIdentity.of("minecraft:diamond")));
    }

    private static final class RecordingPort implements WorkflowDomainPersistencePort {
        private WorkflowDomainSnapshot saved = WorkflowDomainSnapshot.empty();

        @Override
        public WorkflowDomainSnapshot load() {
            return saved;
        }

        @Override
        public void save(WorkflowDomainSnapshot snapshot) {
            saved = snapshot == null ? WorkflowDomainSnapshot.empty() : snapshot;
        }
    }
}
