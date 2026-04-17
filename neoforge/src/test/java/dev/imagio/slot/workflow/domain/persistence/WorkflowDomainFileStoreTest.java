package dev.imagio.slot.workflow.domain.persistence;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSortMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.LoadoutTarget;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutEntry;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowDomainFileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void fileStoreRoundTripsWorkflowDomainSnapshot() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);
        CollectionDefinition exploration = runtime.collectionWorkflow().createCollection("Exploration");
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:torch"), exploration.id());
        runtime.collectionWorkflow().setDesiredCount(exploration.id(), ItemIdentity.of("minecraft:torch"), 48);
        QuickAccessLoadoutDefinition loadout = runtime.collectionWorkflow().createLoadout(
                exploration.id(),
                "Caving",
                Set.of(
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                                ItemIdentity.of("minecraft:torch")
                        ),
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                                ItemIdentity.of("minecraft:shield")
                        )
                )
        );
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
        runtime.setProtectedIdentity(ItemIdentity.of("minecraft:shield"), true);
        runtime.setProtectedTarget(
                new InventoryActionTarget.EquipmentTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                true
        );
        runtime.setProtectPortableContainers(true);
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                320,
                196,
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:torch")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:torch"), island.id(), 16, 60);
        runtime.visualAtlasWorkflow().moveIsland(island.id(), 912, 236);
        source.browseSessionState().replaceWith(new InventoryBrowseSessionState(
                new InventoryBrowseFilter("torch", InventoryBrowseFilterScope.SELECTED_COLLECTION),
                InventoryBrowseSortMode.COUNT_DESC,
                InventoryBrowseGroupingMode.SOURCE,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryPaneMembership.EXTERNAL,
                exploration.id(),
                loadout.id(),
                "tool:craft",
                InventoryActionScope.VISIBLE_ROWS,
                new InventoryBrowseSubjectRef.LoadoutRef(exploration.id(), loadout.id()),
                Set.of("exploration=collapsed")
        ));

        WorkflowDomainFileStore fileStore = new WorkflowDomainFileStore(tempDir.resolve("slot-workflow-state.json"));
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(fileStore);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());
        assertEquals("torch", restored.browseSessionState().current().filter().searchText());
        assertEquals(
                new InventoryBrowseSubjectRef.LoadoutRef(exploration.id(), loadout.id()),
                restored.browseSessionState().current().selectedSubject()
        );
        assertTrue(restored.workflowProjection().protection().protects(ItemIdentity.of("minecraft:shield"), null));
        assertTrue(restored.workflowProjection().protection().protectsPortableContainers());
    }

    @Test
    void fileStoreRoundTripsIslandManagementEvents() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);

        VisualAtlasIsland keeper = runtime.visualAtlasWorkflow().createIsland(
                "Machines", 10, 20, 320, 196, 0xCC5A4A6E, ItemIdentity.of("minecraft:torch")
        );
        runtime.visualAtlasWorkflow().renameIsland(keeper.id(), "Workshop");
        runtime.visualAtlasWorkflow().recolorIsland(keeper.id(), 0xFF112233);
        runtime.visualAtlasWorkflow().setIslandIcon(keeper.id(), ItemIdentity.of("minecraft:anvil"));

        VisualAtlasIsland doomed = runtime.visualAtlasWorkflow().createIsland(
                "Scraps", 40, 60, 240, 120, 0xFF222222, null
        );
        runtime.visualAtlasWorkflow().deleteIsland(doomed.id());

        runtime.visualAtlasWorkflow().dismissTemplate("template.food");

        WorkflowDomainFileStore fileStore = new WorkflowDomainFileStore(tempDir.resolve("slot-island-mgmt.json"));
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(fileStore);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());
        VisualAtlasIsland restoredKeeper = restored.workflowProjection().visualHomeMap().island(keeper.id());
        assertEquals("Workshop", restoredKeeper.label());
        assertEquals(0xFF112233, restoredKeeper.color());
        assertEquals(ItemIdentity.of("minecraft:anvil"), restoredKeeper.iconIdentity());
        assertTrue(restored.workflowProjection().visualHomeMap().island(doomed.id()) == null);
        assertTrue(restored.workflowProjection().visualHomeMap().templateDismissed("template.food"));
    }
}
