package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.CarriedIdentityCounts;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftRunDomainServiceTest {
    @Test
    void addAdjustAndRemoveManageTrackedRecipes() {
        CraftRunDomainService service = new CraftRunDomainService();

        assertTrue(service.add(capture("slot:recipe/torch", "minecraft:torch", "minecraft:coal", 4)));
        assertTrue(service.state().active());
        assertEquals(1, service.state().entries().size());
        assertEquals("minecraft:torch", service.state().selectedEntry().outputIdentity().itemId());
        assertEquals(4, service.state().selectedEntry().remainingOutputCount());

        assertTrue(service.add(capture("slot:recipe/ladder", "minecraft:ladder", "minecraft:stick", 3)));
        assertEquals(2, service.state().entries().size());
        assertEquals("minecraft:ladder", service.state().selectedEntry().outputIdentity().itemId());

        String ladderEntry = service.state().selectedEntryId();
        assertTrue(service.adjustRemainingOutput(ladderEntry, 2));
        assertEquals(5, service.state().entry(ladderEntry).remainingOutputCount());

        assertTrue(service.remove(ladderEntry));
        assertEquals(1, service.state().entries().size());
        assertEquals("minecraft:torch", service.state().selectedEntry().outputIdentity().itemId());

        assertTrue(service.remove(service.state().selectedEntryId()));
        assertFalse(service.state().active());
    }

    @Test
    void acquiredOutputDecrementsRemainingCount() {
        CraftRunDomainService service = new CraftRunDomainService();
        service.add(capture("slot:recipe/torch", "minecraft:torch", "minecraft:coal", 4));

        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(1),
                activity(InventoryActivityKind.ACQUIRED, "minecraft:torch", 2)
        ));

        assertEquals(2, service.state().selectedEntry().remainingOutputCount());

        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(2),
                activity(InventoryActivityKind.ACQUIRED, "minecraft:torch", 8)
        ));

        assertTrue(service.state().active());
        assertEquals(1, service.state().entries().size());
        assertEquals(0, service.state().selectedEntry().remainingOutputCount());
        assertTrue(service.state().selectedEntry().complete());
    }

    @Test
    void completedOutputCanBeRaisedAgainFromHeaderControls() {
        CraftRunDomainService service = new CraftRunDomainService();
        service.add(capture("slot:recipe/torch", "minecraft:torch", "minecraft:coal", 4));
        String entryId = service.state().selectedEntryId();

        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(1),
                activity(InventoryActivityKind.ACQUIRED, "minecraft:torch", 4)
        ));

        assertFalse(service.adjustRemainingOutput(entryId, -1));
        assertEquals(0, service.state().entry(entryId).remainingOutputCount());

        assertTrue(service.adjustRemainingOutput(entryId, 4));
        assertEquals(4, service.state().entry(entryId).remainingOutputCount());
    }

    @Test
    void unrelatedActivityDoesNotChangeRun() {
        CraftRunDomainService service = new CraftRunDomainService();
        service.add(capture("slot:recipe/torch", "minecraft:torch", "minecraft:coal", 4));
        int revision = service.state().revision();

        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(1),
                activity(InventoryActivityKind.TRANSFERRED, "minecraft:torch", 1)
        ));
        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(2),
                activity(InventoryActivityKind.ACQUIRED, "minecraft:stick", 1)
        ));

        assertEquals(revision, service.state().revision());
        assertEquals(4, service.state().selectedEntry().remainingOutputCount());
    }

    @Test
    void addedProducerCountFloorsToDownstreamRecipeInputs() {
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/bronze_firebox_casing",
                "mod:bronze_firebox_casing",
                1,
                1,
                ingredient("slot:recipe/bronze_firebox_casing/plate", "mod:bronze_plate", 2)));
        service.add(capture(
                "slot:recipe/steam_machine_casing",
                "mod:steam_machine_casing",
                1,
                1,
                ingredient("slot:recipe/steam_machine_casing/plate", "mod:bronze_plate", 2)));

        service.add(capture(
                "slot:recipe/bronze_plate",
                "mod:bronze_plate",
                2,
                2,
                ingredient("slot:recipe/bronze_plate/ingot", "mod:bronze_ingot", 1)));

        assertEquals("mod:bronze_plate", service.state().selectedEntry().outputIdentity().itemId());
        assertEquals(4, service.state().selectedEntry().remainingOutputCount());
    }

    @Test
    void addingConsumerRaisesExistingProducerCount() {
        CraftRunDomainService service = new CraftRunDomainService();

        assertTrue(service.add(capture(
                "slot:recipe/bronze_plate",
                "mod:bronze_plate",
                2,
                2,
                ingredient("slot:recipe/bronze_plate/ingot", "mod:bronze_ingot", 1))));
        String plateEntryId = service.state().selectedEntryId();

        service.add(capture(
                "slot:recipe/bronze_firebox_casing",
                "mod:bronze_firebox_casing",
                1,
                1,
                ingredient("slot:recipe/bronze_firebox_casing/plate", "mod:bronze_plate", 2)));
        assertEquals(2, service.state().entry(plateEntryId).remainingOutputCount());

        service.add(capture(
                "slot:recipe/steam_machine_casing",
                "mod:steam_machine_casing",
                1,
                1,
                ingredient("slot:recipe/steam_machine_casing/plate", "mod:bronze_plate", 2)));

        assertEquals(4, service.state().entry(plateEntryId).remainingOutputCount());
    }

    @Test
    void adjustingConsumerCountRecomputesProducerFloor() {
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/bronze_plate",
                "mod:bronze_plate",
                2,
                2,
                ingredient("slot:recipe/bronze_plate/ingot", "mod:bronze_ingot", 1)));
        String plateEntryId = service.state().selectedEntryId();

        service.add(capture(
                "slot:recipe/bronze_firebox_casing",
                "mod:bronze_firebox_casing",
                1,
                1,
                ingredient("slot:recipe/bronze_firebox_casing/plate", "mod:bronze_plate", 2)));
        String casingEntryId = service.state().selectedEntryId();

        assertTrue(service.adjustRemainingOutput(casingEntryId, 1));

        assertEquals(2, service.state().entry(casingEntryId).remainingOutputCount());
        assertEquals(4, service.state().entry(plateEntryId).remainingOutputCount());
    }

    @Test
    void batchAdjustmentCannotDropBelowOneRecipeOutputBatch() {
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/bronze_plate",
                "mod:bronze_plate",
                2,
                2,
                ingredient("slot:recipe/bronze_plate/ingot", "mod:bronze_ingot", 1)));
        String plateEntryId = service.state().selectedEntryId();

        assertFalse(service.adjustRemainingOutput(plateEntryId, -2));
        assertEquals(2, service.state().entry(plateEntryId).remainingOutputCount());
    }

    @Test
    void dependencyFloorBlocksProducerBatchReduction() {
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/bronze_firebox_casing",
                "mod:bronze_firebox_casing",
                1,
                1,
                ingredient("slot:recipe/bronze_firebox_casing/plate", "mod:bronze_plate", 2)));
        service.add(capture(
                "slot:recipe/steam_machine_casing",
                "mod:steam_machine_casing",
                1,
                1,
                ingredient("slot:recipe/steam_machine_casing/plate", "mod:bronze_plate", 2)));
        service.add(capture(
                "slot:recipe/bronze_plate",
                "mod:bronze_plate",
                2,
                2,
                ingredient("slot:recipe/bronze_plate/ingot", "mod:bronze_ingot", 1)));
        String plateEntryId = service.state().selectedEntryId();

        assertFalse(service.adjustRemainingOutput(plateEntryId, -2));
        assertEquals(4, service.state().entry(plateEntryId).remainingOutputCount());
    }

    @Test
    void acquiredProducerOutputStillDecrementsFlooredEntry() {
        CraftRunDomainService service = new CraftRunDomainService();
        service.add(capture(
                "slot:recipe/bronze_firebox_casing",
                "mod:bronze_firebox_casing",
                1,
                1,
                ingredient("slot:recipe/bronze_firebox_casing/plate", "mod:bronze_plate", 2)));
        service.add(capture(
                "slot:recipe/steam_machine_casing",
                "mod:steam_machine_casing",
                1,
                1,
                ingredient("slot:recipe/steam_machine_casing/plate", "mod:bronze_plate", 2)));
        service.add(capture(
                "slot:recipe/bronze_plate",
                "mod:bronze_plate",
                2,
                2,
                ingredient("slot:recipe/bronze_plate/ingot", "mod:bronze_ingot", 1)));

        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(1),
                activity(InventoryActivityKind.ACQUIRED, "mod:bronze_plate", 2)
        ));

        assertEquals(2, service.state().selectedEntry().remainingOutputCount());
    }

    @Test
    void duplicateConsumedSlotsAggregateIntoOneIngredientNeed() {
        ItemIdentity rod = ItemIdentity.of("mod:black_steel_rod");
        CraftRunRecipeCapture capture = capture(
                "slot:recipe/steam_bloomery",
                "mod:steam_bloomery",
                1,
                1,
                ingredient("slot:recipe/steam_bloomery/rod_0", rod.itemId(), 1),
                ingredient("slot:recipe/steam_bloomery/rod_1", rod.itemId(), 1),
                ingredient("slot:recipe/steam_bloomery/rod_2", rod.itemId(), 1),
                ingredient("slot:recipe/steam_bloomery/rod_3", rod.itemId(), 1));

        assertEquals(1, capture.inputs().size());
        assertEquals(4, capture.inputs().get(0).requiredCountPerBatch());

        CraftRunDomainService service = new CraftRunDomainService();
        assertTrue(service.add(capture));

        WorkflowTabTargets.Resolution targets = CraftRunTargetOverlay.apply(
                WorkflowTabTargets.Resolution.empty(),
                service.state(),
                new CarriedIdentityCounts(Map.of(rod, 1)));

        assertEquals(4, targets.wantedCount(rod));
        assertTrue(targets.missingWorkflowIdentities().contains(rod));
    }

    @Test
    void consumedInputsAggregateAcrossTrackedRecipes() {
        ItemIdentity coal = ItemIdentity.of("minecraft:coal");
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/torch",
                "minecraft:torch",
                4,
                4,
                ingredient("slot:recipe/torch/coal", coal.itemId(), 1)));
        service.add(capture(
                "slot:recipe/campfire",
                "minecraft:campfire",
                1,
                1,
                ingredient("slot:recipe/campfire/coal", coal.itemId(), 2)));

        WorkflowTabTargets.Resolution targets = CraftRunTargetOverlay.apply(
                WorkflowTabTargets.Resolution.empty(),
                service.state(),
                new CarriedIdentityCounts(Map.of(coal, 2)));

        assertEquals(3, targets.wantedCount(coal));
        assertTrue(targets.missingWorkflowIdentities().contains(coal));
    }

    @Test
    void reusableInputsDoNotScaleWantedPressureAcrossRecipes() {
        ItemIdentity hammer = ItemIdentity.of("mod:hammer");
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/plate",
                "mod:plate",
                1,
                1,
                reusableIngredient("slot:recipe/plate/hammer", hammer.itemId(), 1)));
        service.add(capture(
                "slot:recipe/rod",
                "mod:rod",
                1,
                1,
                reusableIngredient("slot:recipe/rod/hammer", hammer.itemId(), 1)));

        WorkflowTabTargets.Resolution targets = CraftRunTargetOverlay.apply(
                WorkflowTabTargets.Resolution.empty(),
                service.state(),
                CarriedIdentityCounts.empty());

        assertEquals(1, targets.wantedCount(hammer));
    }

    @Test
    void completedRecipesStayRelevantButStopWantedPressure() {
        ItemIdentity coal = ItemIdentity.of("minecraft:coal");
        CraftRunDomainService service = new CraftRunDomainService();
        service.add(capture("slot:recipe/torch", "minecraft:torch", coal.itemId(), 3));

        service.observeActivityRecord(new InventoryActivityRecord(
                envelope(1),
                activity(InventoryActivityKind.ACQUIRED, "minecraft:torch", 3)
        ));

        WorkflowTabTargets.Resolution targets = CraftRunTargetOverlay.apply(
                WorkflowTabTargets.Resolution.empty(),
                service.state(),
                CarriedIdentityCounts.empty());

        assertTrue(service.state().active());
        assertEquals(0, targets.wantedCount(coal));
        assertFalse(targets.missingWorkflowIdentities().contains(coal));
        assertTrue(targets.workflowRelevant(ItemIdentity.of("minecraft:torch")));
    }

    @Test
    void reusableDependencyFloorsProducerToOneAcrossConsumers() {
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/plate",
                "mod:plate",
                1,
                1,
                reusableIngredient("slot:recipe/plate/hammer", "mod:hammer", 1)));
        service.add(capture(
                "slot:recipe/rod",
                "mod:rod",
                1,
                1,
                reusableIngredient("slot:recipe/rod/hammer", "mod:hammer", 1)));
        service.add(capture(
                "slot:recipe/hammer",
                "mod:hammer",
                1,
                1,
                ingredient("slot:recipe/hammer/ingot", "mod:ingot", 2)));

        assertEquals(1, service.state().selectedEntry().remainingOutputCount());
    }

    @Test
    void unresolvedAlternativeGroupDoesNotInventConcreteWantedPressure() {
        ItemIdentity bismuthBronze = ItemIdentity.of("mod:bismuth_bronze_double_ingot");
        ItemIdentity bronze = ItemIdentity.of("mod:bronze_double_ingot");
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/bloomery_door",
                "mod:bloomery_door",
                1,
                1,
                choiceIngredient(
                        "slot:recipe/bloomery_door/double_bronze_ingot",
                        "double bronze ingot",
                        8,
                        bismuthBronze,
                        bronze)));

        WorkflowTabTargets.Resolution unresolved = CraftRunTargetOverlay.apply(
                WorkflowTabTargets.Resolution.empty(),
                service.state(),
                CarriedIdentityCounts.empty());

        assertEquals(0, unresolved.wantedCount(bismuthBronze));
        assertEquals(0, unresolved.wantedCount(bronze));

        String entryId = service.state().selectedEntryId();
        assertTrue(service.selectIngredientAlternative(
                entryId,
                "slot:recipe/bloomery_door/double_bronze_ingot",
                bronze));

        WorkflowTabTargets.Resolution selected = CraftRunTargetOverlay.apply(
                WorkflowTabTargets.Resolution.empty(),
                service.state(),
                CarriedIdentityCounts.empty());

        assertEquals(0, selected.wantedCount(bismuthBronze));
        assertEquals(8, selected.wantedCount(bronze));
    }

    @Test
    void dependencyFloorRespectsSelectedAlternative() {
        ItemIdentity bismuthBronze = ItemIdentity.of("mod:bismuth_bronze_double_ingot");
        ItemIdentity bronze = ItemIdentity.of("mod:bronze_double_ingot");
        CraftRunDomainService service = new CraftRunDomainService();

        service.add(capture(
                "slot:recipe/bloomery_door",
                "mod:bloomery_door",
                1,
                1,
                choiceIngredient(
                        "slot:recipe/bloomery_door/double_bronze_ingot",
                        "double bronze ingot",
                        8,
                        bismuthBronze,
                        bronze)));
        String doorEntryId = service.state().selectedEntryId();
        assertTrue(service.selectIngredientAlternative(
                doorEntryId,
                "slot:recipe/bloomery_door/double_bronze_ingot",
                bronze));

        service.add(capture(
                "slot:recipe/bismuth_bronze_double_ingot",
                bismuthBronze.itemId(),
                1,
                1,
                ingredient("slot:recipe/bismuth_bronze_double_ingot/ingot", "mod:bismuth_bronze_ingot", 2)));
        assertEquals(1, service.state().selectedEntry().remainingOutputCount());

        service.add(capture(
                "slot:recipe/bronze_double_ingot",
                bronze.itemId(),
                1,
                1,
                ingredient("slot:recipe/bronze_double_ingot/ingot", "mod:bronze_ingot", 2)));
        assertEquals(8, service.state().selectedEntry().remainingOutputCount());
    }

    private static CraftRunRecipeCapture capture(
            String recipeId,
            String outputItemId,
            String inputItemId,
            int remainingOutputCount
    ) {
        return capture(recipeId, outputItemId, 1, remainingOutputCount, ingredient(recipeId + "/input", inputItemId, 1));
    }

    private static CraftRunRecipeCapture capture(
            String recipeId,
            String outputItemId,
            int outputCountPerBatch,
            int remainingOutputCount,
            CraftRunIngredientGroup... inputs
    ) {
        ItemIdentity output = ItemIdentity.of(outputItemId);
        return new CraftRunRecipeCapture(
                "emi:" + recipeId,
                recipeId,
                outputItemId,
                output,
                outputItemId,
                outputCountPerBatch,
                remainingOutputCount,
                List.of(inputs),
                List.of());
    }

    private static CraftRunIngredientGroup ingredient(
            String groupId,
            String itemId,
            int requiredCountPerBatch
    ) {
        ItemIdentity input = ItemIdentity.of(itemId);
        return new CraftRunIngredientGroup(
                groupId,
                itemId,
                requiredCountPerBatch,
                List.of(new CraftRunAlternative(input, itemId)),
                List.of());
    }

    private static CraftRunIngredientGroup reusableIngredient(
            String groupId,
            String itemId,
            int requiredCountPerBatch
    ) {
        ItemIdentity input = ItemIdentity.of(itemId);
        return new CraftRunIngredientGroup(
                groupId,
                itemId,
                requiredCountPerBatch,
                false,
                List.of(new CraftRunAlternative(input, itemId)),
                List.of());
    }

    private static CraftRunIngredientGroup choiceIngredient(
            String groupId,
            String label,
            int requiredCountPerBatch,
            ItemIdentity... alternatives
    ) {
        java.util.ArrayList<CraftRunAlternative> entries = new java.util.ArrayList<>();
        for (ItemIdentity alternative : alternatives) {
            entries.add(new CraftRunAlternative(alternative, alternative.itemId()));
        }
        return new CraftRunIngredientGroup(
                groupId,
                label,
                requiredCountPerBatch,
                entries,
                List.of());
    }

    private static InventoryActivityEvent activity(
            InventoryActivityKind kind,
            String itemId,
            int count
    ) {
        return new InventoryActivityEvent(
                kind,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.OBSERVED,
                ItemIdentity.of(itemId),
                count,
                null,
                null,
                "",
                "",
                List.of(),
                ""
        );
    }

    private static DomainEventEnvelope envelope(long sequence) {
        return new DomainEventEnvelope(sequence, sequence, DomainEventStreamKind.ACTIVITY, 0L, "test", "", "", "");
    }
}
