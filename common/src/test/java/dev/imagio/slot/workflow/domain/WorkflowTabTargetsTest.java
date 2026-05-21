package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowTabTargetsTest {
    @Test
    void allDesiredTargetsApplyInsideWorkflowTab() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity torch = ItemIdentity.of("minecraft:torch");
        runtime.desiredCountWorkflow().setPlayer(torch, 64);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(mining.id());

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());

        assertEquals(64, targets.desiredCount(torch));
        assertTrue(targets.workflowRelevantIdentities().contains(torch));
        assertFalse(targets.missingWorkflowIdentities().contains(torch));
        assertFalse(targets.desiredFromWorkflowTab(torch));
    }

    @Test
    void parentTargetsApplyInsideVariantAndVariantCannotLowerThem() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity hammer = ItemIdentity.of("minecraft:hammer");
        ItemIdentity flux = ItemIdentity.of("minecraft:flux");
        KitDefinition smithing = runtime.kitWorkflow().create("Smithing");
        KitDefinition steel = runtime.kitWorkflow().createVariant(smithing.id(), "Steel Smelting");
        runtime.desiredCountWorkflow().setForKit(smithing.id(), hammer, 1);
        runtime.desiredCountWorkflow().setForKit(smithing.id(), flux, 16);
        runtime.desiredCountWorkflow().setForKit(steel.id(), flux, 4);
        runtime.kitWorkflow().activate(steel.id());

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());

        assertEquals(1, targets.desiredCount(hammer));
        assertEquals(16, targets.desiredCount(flux));
        assertTrue(targets.workflowRelevantIdentities().contains(hammer));
        assertTrue(targets.workflowRelevantIdentities().contains(flux));
    }

    @Test
    void tabMembershipActsAsImplicitWantedOne() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity pan = ItemIdentity.of("minecraft:frying_pan");
        KitDefinition cooking = runtime.kitWorkflow().create("Cooking");
        runtime.kitWorkflow().setMember(cooking.id(), pan, true);
        runtime.kitWorkflow().activate(cooking.id());

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());

        assertEquals(1, targets.wantedCount(pan));
        assertTrue(targets.workflowRelevantIdentities().contains(pan));
        assertTrue(targets.missingWorkflowIdentities().contains(pan));
    }

    @Test
    void stableMovableExactIdentityMatchesItemOnlyTargets() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity pickaxe = ItemIdentity.of("minecraft:iron_pickaxe");
        ItemIdentity damagedPickaxe = ItemIdentity.exact("minecraft:iron_pickaxe", "damage=7");
        runtime.desiredCountWorkflow().setPlayer(pickaxe, 1);
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(mining.id(), pickaxe, true);
        runtime.kitWorkflow().activate(mining.id());

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());

        assertEquals(1, targets.desiredCount(damagedPickaxe));
        assertEquals(1, targets.wantedCount(damagedPickaxe));
        assertEquals(1, targets.reservedCarryCount(damagedPickaxe));
        assertTrue(targets.workflowRelevant(damagedPickaxe));
    }

    @Test
    void scopedMovableDesiredCountsResolveToCanonicalMissingTargets() {
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        ItemIdentity damagedHammer = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        KitDefinition mining = new KitDefinition("kit-1", "Mining", List.of(KitPage.empty()), null);
        KitMap kitMap = new KitMap(List.of(mining), new KitActivation(mining.id(), 0));

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                kitMap,
                Map.of(),
                Map.of(mining.id(), Map.of(damagedHammer, 1)),
                Map.of(),
                Map.of());

        assertEquals(1, targets.desiredCount(hammer));
        assertTrue(targets.desiredFromWorkflowTab(hammer));
        assertTrue(targets.workflowRelevant(hammer));
        assertTrue(targets.missingWorkflowIdentities().contains(hammer));
        assertFalse(targets.missingWorkflowIdentities().contains(damagedHammer));
    }

    @Test
    void kitMembershipUsesMovableIdentitySemantics() {
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        ItemIdentity damagedHammer = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        ItemIdentity toolStateHammer = ItemIdentity.exact(
                "gtceu:steel_mining_hammer",
                "{Damage:12,\"GT.Tool\":{MaxDamage:960}}");

        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, damagedHammer)),
                toolStateHammer,
                "",
                Set.of(damagedHammer));

        assertEquals(Set.of(hammer), kit.members());
        assertEquals(hammer, kit.page(0).slot(0));
        assertEquals(hammer, kit.offhand());
        assertEquals(Set.of(hammer), kit.withMember(toolStateHammer).members());
        assertTrue(kit.withoutMember(toolStateHammer).members().isEmpty());
    }

    @Test
    void acceptedInputsAreRelevantWithoutWantedDesiredOrMissingPressure() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity hematite = ItemIdentity.of("tfg:hematite_ore");
        ItemIdentity limestone = ItemIdentity.of("tfg:limestone");
        KitDefinition steel = runtime.kitWorkflow().create("Steel Smelting");
        runtime.kitWorkflow().setAcceptedInput(
                steel.id(),
                WorkflowAcceptedInputRule.itemTag("forge:ores/iron"),
                true);
        runtime.kitWorkflow().activate(steel.id());

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());

        assertTrue(targets.workflowRelevant(hematite, Set.of("forge:ores/iron", "forge:ores")));
        assertFalse(targets.workflowRelevant(limestone, Set.of("forge:stone")));
        assertEquals(0, targets.desiredCount(hematite));
        assertEquals(0, targets.wantedCount(hematite));
        assertEquals(0, targets.reservedCarryCount(hematite));
        assertFalse(targets.missingWorkflowIdentities().contains(hematite));
    }

    @Test
    void tabWantedCountsClearWhenTabDeactivatesAndDoNotAffectAll() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity onion = ItemIdentity.of("minecraft:onion");
        KitDefinition cooking = runtime.kitWorkflow().create("Cooking");
        runtime.kitWorkflow().activate(cooking.id());
        runtime.wantedCountWorkflow().setForKit(cooking.id(), onion, 3);
        assertEquals(3, WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot()).wantedCount(onion));

        runtime.kitWorkflow().deactivate();

        assertEquals(0, runtime.wantedCountWorkflow().getForKit(cooking.id(), onion));
        assertEquals(0, WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot()).wantedCount(onion));
    }

    @Test
    void craftRunInputsUseWantedTargetsInsteadOfDesiredTargets() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity coal = ItemIdentity.of("minecraft:coal");
        runtime.craftRunWorkflow().add(capture("slot:recipe/torch", "minecraft:torch", coal, 3));

        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                InventoryAuthoritySnapshot.empty(),
                runtime.snapshot());

        assertEquals(0, targets.desiredCount(coal));
        assertFalse(targets.desiredFromWorkflowTab(coal));
        assertEquals(3, targets.wantedCount(coal));
        assertTrue(targets.workflowRelevant(coal));
        assertTrue(targets.missingWorkflowIdentities().contains(coal));
    }

    @Test
    void variantsCannotHaveVariants() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition parent = runtime.kitWorkflow().create("Smithing");
        KitDefinition variant = runtime.kitWorkflow().createVariant(parent.id(), "Steel Smelting");

        assertThrows(
                IllegalArgumentException.class,
                () -> runtime.kitWorkflow().createVariant(variant.id(), "Too Deep"));
    }

    private static WorkflowDomainRuntime runtime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }

    private static CraftRunRecipeCapture capture(
            String recipeId,
            String outputItemId,
            ItemIdentity input,
            int remainingOutputCount
    ) {
        ItemIdentity output = ItemIdentity.of(outputItemId);
        return new CraftRunRecipeCapture(
                "emi:" + recipeId,
                recipeId,
                outputItemId,
                output,
                outputItemId,
                1,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        recipeId + "/input",
                        input.itemId(),
                        1,
                        List.of(new CraftRunAlternative(input, input.itemId())),
                        List.of())),
                List.of());
    }
}
