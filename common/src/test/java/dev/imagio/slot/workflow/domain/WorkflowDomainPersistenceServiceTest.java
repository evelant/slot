package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSortMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.workflow.domain.persistence.WorkflowDomainFileStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowDomainPersistenceServiceTest {
    @Test
    void persistenceServiceRoundTripsWorkflowAndBrowseState() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);
        CollectionDefinition exploration = runtime.collectionWorkflow().createCollection("Exploration");
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:torch"), exploration.id());
        // Player-scoped desired counts (collection-scoped variant retired).
        runtime.desiredCountWorkflow().setPlayer(ItemIdentity.of("minecraft:torch"), 32);
        QuickAccessLoadoutDefinition loadout = runtime.collectionWorkflow().createLoadout(
                exploration.id(),
                "Mining",
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
                3,
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
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:torch")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:torch"), island.id(), 0);
        runtime.visualAtlasWorkflow().moveIsland(island.id(), 912, 236);
        source.browsePreferences().replaceWith(new InventoryBrowsePreferences(
                InventoryBrowseSortMode.COUNT_DESC,
                InventoryBrowseGroupingMode.SOURCE,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryActionScope.VISIBLE_ROWS
        ));
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
                Set.of("collections:exploration=collapsed")
        ));

        RecordingPort port = new RecordingPort();
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(port);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());
        assertEquals("torch", restored.browseSessionState().current().filter().searchText());
        assertEquals(InventoryBrowseGroupingMode.SOURCE, restored.browseSessionState().current().groupingMode());
        assertEquals(
                new InventoryBrowseSubjectRef.LoadoutRef(exploration.id(), loadout.id()),
                restored.browseSessionState().current().selectedSubject()
        );
        assertTrue(restored.workflowProjection().protection().protects(ItemIdentity.of("minecraft:shield"), null));
        assertTrue(restored.workflowProjection().protection().protectsPortableContainers());
    }

    @Test
    void filePersistenceRoundTripsCraftRunState(@TempDir Path tempDir) throws Exception {
        Path statePath = tempDir.resolve("workflow-craft-run.json");
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(
                new WorkflowDomainFileStore(statePath)
        );
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, service);

        assertTrue(runtime.craftRunWorkflow().add(craftRunRecipe(
                "minecraft:torch",
                4,
                4,
                "minecraft:coal",
                1
        )));
        String entryId = runtime.snapshot().craftRun().entries().get(0).entryId();
        assertTrue(runtime.savePending());

        runtime.recordActivityEvent(new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.OBSERVED,
                ItemIdentity.of("minecraft:torch"),
                1,
                null,
                null,
                "",
                "",
                List.of(),
                ""
        ));
        assertEquals(3, runtime.snapshot().craftRun().entry(entryId).remainingOutputCount());
        assertTrue(runtime.flushPendingSave());

        String saved = Files.readString(statePath);
        assertTrue(saved.contains("\"craftRun\""));

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);
        WorkflowDomainRuntime restoredRuntime = new WorkflowDomainRuntime(restored, service);

        CraftRunState restoredRun = restoredRuntime.snapshot().craftRun();
        assertEquals(1, restoredRun.entries().size());
        CraftRunRecipeEntry restoredEntry = restoredRun.entries().get(0);
        assertEquals(entryId, restoredEntry.entryId());
        assertEquals(3, restoredEntry.remainingOutputCount());
        assertEquals(ItemIdentity.of("minecraft:torch"), restoredEntry.outputIdentity());
        assertEquals(1, restoredEntry.inputs().size());

        assertTrue(restoredRuntime.craftRunWorkflow().add(craftRunRecipe(
                "minecraft:stick",
                4,
                4,
                "minecraft:planks",
                2
        )));
        assertEquals("craft-run-2", restoredRuntime.snapshot().craftRun().entries().get(1).entryId());
    }

    @Test
    void filePersistenceCompactsWorkflowEventHistory(@TempDir Path tempDir) throws Exception {
        Path statePath = tempDir.resolve("workflow.json");
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(
                new WorkflowDomainFileStore(statePath)
        );
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, service);

        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Metals",
                10,
                20,
                0xFFAA8844,
                ItemIdentity.of("minecraft:iron_ingot")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:iron_ingot"), island.id(), 0);
        runtime.flushPendingSave();

        assertEquals(0, repository.workflowEvents().snapshot().records().size());
        String saved = Files.readString(statePath);
        assertTrue(saved.contains("\"workflowEvents\": []"));

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);
        assertEquals(0, restored.workflowEvents().snapshot().records().size());
        assertNotNull(restored.workflowProjection().visualHomeMap().assignment(ItemIdentity.of("minecraft:iron_ingot")));
    }

    @Test
    void filePersistenceRoundTripsContextualSuggestionAggregates(@TempDir Path tempDir) throws Exception {
        Path statePath = tempDir.resolve("workflow-contextual.json");
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(
                new WorkflowDomainFileStore(statePath)
        );
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, service);

        runtime.recordActivityEvent(new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.EXTERNAL_WITHDRAWAL,
                InventoryActivityConfidence.AUTHORITATIVE,
                ItemIdentity.of("minecraft:iron_ore"),
                4,
                null,
                null,
                "",
                "",
                List.of(),
                "take_from_storage"
        ));
        runtime.contextualSuggestions().recordSignal(new ContextualSignalEvent(
                ContextualSignalKind.STATION_OPENED,
                null,
                1,
                400L,
                "menu:test_forge",
                "Test Forge",
                "",
                java.util.Map.of()),
                DomainEventMetadata.origin("test.station"));
        runtime.flushPendingSave();

        String saved = Files.readString(statePath);
        assertTrue(saved.contains("\"contextualSuggestions\""));

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        ContextualSuggestionState contextual = restored.snapshot().contextualSuggestions();
        ContextualItemAggregate iron = contextual.itemAggregates().get(ItemIdentity.of("minecraft:iron_ore"));
        assertNotNull(iron);
        assertEquals(1, iron.timesTakenFromStorage());
        assertTrue(contextual.contextAggregates().containsKey("menu:test_forge"));
        assertFalse(contextual.recentSignals().isEmpty());
    }

    private static final class RecordingPort implements WorkflowDomainPersistencePort {
        private WorkflowDomainSnapshot snapshot = WorkflowDomainSnapshot.empty();

        @Override
        public WorkflowDomainSnapshot load() {
            return snapshot;
        }

        @Override
        public void save(WorkflowDomainSnapshot snapshot) {
            this.snapshot = snapshot == null ? WorkflowDomainSnapshot.empty() : snapshot;
        }
    }

    private static CraftRunRecipeCapture craftRunRecipe(
            String outputId,
            int outputCountPerBatch,
            int remainingOutputCount,
            String inputId,
            int requiredCountPerBatch
    ) {
        return new CraftRunRecipeCapture(
                "emi:test/" + outputId,
                "test:" + outputId.replace(':', '/'),
                outputId,
                ItemIdentity.of(outputId),
                outputId,
                outputCountPerBatch,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        "input:0",
                        inputId,
                        requiredCountPerBatch,
                        List.of(new CraftRunAlternative(ItemIdentity.of(inputId), inputId)),
                        List.of()
                )),
                List.of()
        );
    }

}
