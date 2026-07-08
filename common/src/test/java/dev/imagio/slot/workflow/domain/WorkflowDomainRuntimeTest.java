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
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowDomainRuntimeTest {
    @Test
    void runtimeCoalescesPersistenceUntilPendingSaveIsFlushed() {
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
                InventoryBrowseGroupingMode.FLAT,
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

        assertEquals(WorkflowDomainSnapshot.empty(), port.saved);
        assertTrue(runtime.savePending());
        runtime.flushPendingSave();

        assertEquals(repository.snapshot(), port.saved);
        assertEquals("stone", port.saved.browseSessionState().filter().searchText());
        assertEquals(1, port.saved.collections().loadoutsByCollection().get(collection.id()).size());
        assertEquals(2, port.saved.recents().countsByIdentity().get(ItemIdentity.of("minecraft:diamond")));
    }

    @Test
    void fluidObservationBaselinesAndIgnoresSameTotalTransfers() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null
        );
        SlotResourceIdentity oxygen = SlotResourceIdentity.fluid("gtceu:oxygen");
        runtime.craftRunWorkflow().add(fluidCapture(oxygen, 1000L, 1000L));

        assertTrue(runtime.craftRunWorkflow().state().active());
        runtime.observeFluidResourceCounts(Map.of(oxygen, 1000L), "initial_tank_scan");
        assertEquals(1000L, runtime.craftRunWorkflow().state().selectedEntry().remainingOutputAmount());

        runtime.observeFluidResourceCounts(Map.of(oxygen, 1000L), "tank_to_tank_transfer_same_total");
        assertEquals(1000L, runtime.craftRunWorkflow().state().selectedEntry().remainingOutputAmount());

        runtime.observeFluidResourceCounts(Map.of(oxygen, 1500L), "reactor_output_increased_total");
        assertEquals(500L, runtime.craftRunWorkflow().state().selectedEntry().remainingOutputAmount());
    }

    private static CraftRunRecipeCapture fluidCapture(
            SlotResourceIdentity output,
            long outputAmountPerBatch,
            long remainingOutputAmount
    ) {
        return new CraftRunRecipeCapture(
                "emi:test/fluid",
                "gtceu:test/fluid",
                "Oxygen",
                null,
                "Oxygen",
                (int) outputAmountPerBatch,
                (int) remainingOutputAmount,
                List.of(new CraftRunIngredientGroup(
                        "dust",
                        "Dust",
                        1,
                        List.of(new CraftRunAlternative(ItemIdentity.of("gtceu:dust"), "Dust")),
                        List.of())),
                output,
                outputAmountPerBatch,
                remainingOutputAmount,
                List.of("gregtech_fluid_recipe"));
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
