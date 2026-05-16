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
import dev.imagio.slot.inventory.goal.GoalChoiceResolution;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
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
        runtime.goalRecipeDefaultWorkflow().set("tfc:glue", "toomanyrecipeviewers:/tfc/barrel/glue");
        runtime.goalPlanWorkflow().save(goalPlan());
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
        assertEquals(
                "toomanyrecipeviewers:/tfc/barrel/glue",
                restored.workflowProjection().goalRecipeDefaults().get("tfc:glue")
        );
        assertEquals(1, restored.workflowProjection().goalPlans().size());
        assertEquals("emi:slot:recipe/press", restored.workflowProjection().goalPlans().get(0).goalId());
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

    private static GoalPlanState goalPlan() {
        ItemIdentity output = ItemIdentity.of("tfc:glue");
        GoalStackDescriptor outputStack = new GoalStackDescriptor(output, "Glue", 1);
        GoalDescriptor descriptor = new GoalDescriptor(
                "emi:slot:recipe/press",
                "Glue",
                List.of(outputStack),
                1,
                "slot:recipe/press",
                "slot:test",
                List.of(new GoalRecipeDescriptor(
                        "slot:recipe/press",
                        "slot:test",
                        true,
                        List.of(outputStack),
                        List.of(GoalIngredientDescriptor.choice(
                                "input",
                                "#item:forge:tools/wire_cutters",
                                1,
                                "#item:forge:tools/wire_cutters",
                                List.of(GoalStackDescriptor.of("tfc:metal/tool/copper_wire_cutter", "Copper Wire Cutter", 1))
                        )),
                        List.of(),
                        List.of()
                ))
        );
        return new GoalPlanState(
                descriptor.goalId(),
                descriptor.label(),
                descriptor.targetCount(),
                descriptor,
                GoalChoiceResolution.empty()
        );
    }
}
