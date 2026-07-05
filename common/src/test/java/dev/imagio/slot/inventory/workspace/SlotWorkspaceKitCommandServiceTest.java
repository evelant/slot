package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceKitCommandServiceTest {

    @Test
    void saveBeltAsKitRecordsKit() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.saveBeltAsKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                IDENTITY_RESOLVER,
                "Mining"
        );

        assertTrue(outcome.success());
        assertEquals("workflow saved", outcome.status());
        List<KitDefinition> kits = runtime.kitWorkflow().kits();
        assertEquals(1, kits.size());
        assertEquals("Mining", kits.get(0).name());
        assertWorkflowMetadataInvalidation(outcome, "workflow_saved");
    }

    @Test
    void saveBeltAsKitFallsBackToGeneratedName() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.saveBeltAsKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                IDENTITY_RESOLVER,
                ""
        );

        assertTrue(outcome.success());
        assertEquals("Workflow 1", runtime.kitWorkflow().kits().get(0).name());
    }

    @Test
    void createWorkflowTabCreatesEmptyActiveTab() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.createWorkflowTab(runtime, "");

        assertTrue(outcome.success());
        assertEquals("workflow created", outcome.status());
        List<KitDefinition> kits = runtime.kitWorkflow().kits();
        assertEquals(1, kits.size());
        assertEquals("Workflow 1", kits.get(0).name());
        assertTrue(runtime.kitWorkflow().activation().isActive());
        assertEquals(kits.get(0).id(), runtime.kitWorkflow().activation().kitId());
        assertEquals(0, kits.get(0).pages().get(0).filledSlotCount());
        assertWorkflowMetadataInvalidation(outcome, "workflow_created");
    }

    @Test
    void activateEmptyKitReturnsWorkflowMetadataInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Empty");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                new RecordingActionExecutor(),
                kit.id()
        );

        assertTrue(outcome.success());
        assertEquals(kit.id(), runtime.kitWorkflow().activation().kitId());
        assertWorkflowMetadataInvalidation(outcome, "workflow_activated");
    }

    @Test
    void activateKitFlipsActivationAndReturnsAcceptedOutcome() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        runtime.kitWorkflow().setSlotIdentity(kit.id(), 0, 0, hammer);
        RecordingActionExecutor executor = new RecordingActionExecutor();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                executor,
                kit.id()
        );

        assertTrue(outcome.success());
        assertEquals(kit.id(), runtime.kitWorkflow().activation().kitId());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().contains(hammer));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WORKFLOW));
    }

    @Test
    void activateKitCapturesOnlyActivationTimePutAwayClutter() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().setMember(kit.id(), ItemIdentity.of("minecraft:torch"), true);
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host(),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(
                                new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 16, 64), 16),
                                new InventoryStackSnapshot(1, new ItemStack("minecraft:dirt", 32, 64), 32)),
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:iron_sword", 1, 1), 1))),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 25));

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                new RecordingActionExecutor(),
                kit.id()
        );

        assertTrue(outcome.success());
        assertEquals(
                Set.of(ItemIdentity.of("minecraft:dirt")),
                runtime.kitWorkflow().activation().putAwayIdentities());
        assertTargetInvalidation(outcome, ItemIdentity.of("minecraft:dirt"));
    }

    @Test
    void activateKitCapturesTfcFoodPutAwayAsMovableItemIdentity() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Fieldwork");
        runtime.kitWorkflow().setMember(kit.id(), ItemIdentity.of("minecraft:torch"), true);
        InventoryAuthoritySnapshot authority = InventoryAuthorityFixtures.authority(
                host(),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(
                        1,
                        new ItemStack(
                                "tfc:food/banana",
                                "{tfc:food=>Food[creationDate=100,rotten=false,traits=[]]}",
                                4,
                                32),
                        4))),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 25));

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                authority,
                ProtectionPolicy.allowAll(),
                entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack()),
                new RecordingActionExecutor(),
                kit.id()
        );

        assertTrue(outcome.success());
        assertEquals(
                Set.of(ItemIdentity.of("tfc:food/banana")),
                runtime.kitWorkflow().activation().putAwayIdentities());
    }

    @Test
    void activateKitRejectsUnknownId() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                new RecordingActionExecutor(),
                "missing"
        );

        assertFalse(outcome.success());
        assertEquals("unknown_kit", outcome.diagnostics());
    }

    @Test
    void deactivateKitClearsActivation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        ItemIdentity dirt = ItemIdentity.of("minecraft:dirt");
        runtime.kitWorkflow().setSlotIdentity(kit.id(), 0, 0, hammer);
        runtime.kitWorkflow().activate(kit.id(), 0, Set.of(dirt));

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deactivateKit(runtime);

        assertTrue(outcome.success());
        assertFalse(runtime.kitWorkflow().activation().isActive());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().contains(hammer));
        assertTrue(invalidation.identities().contains(dirt));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WORKFLOW));
    }

    @Test
    void deactivateEmptyKitReturnsWorkflowMetadataInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Empty");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deactivateKit(runtime);

        assertTrue(outcome.success());
        assertFalse(runtime.kitWorkflow().activation().isActive());
        assertWorkflowMetadataInvalidation(outcome, "workflow_deactivated");
    }

    @Test
    void deactivateKitRejectsWhenNoneActive() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deactivateKit(runtime);

        assertFalse(outcome.success());
        assertEquals("no_active_kit", outcome.diagnostics());
    }

    @Test
    void deleteKitRemovesKit() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deleteKit(runtime, kit.id());

        assertTrue(outcome.success());
        assertNull(runtime.kitWorkflow().kit(kit.id()));
        assertWorkflowMetadataInvalidation(outcome, "workflow_deleted");
    }

    @Test
    void deleteKitRejectsUnknownId() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deleteKit(runtime, "missing");

        assertFalse(outcome.success());
        assertEquals("unknown_kit", outcome.diagnostics());
    }

    @Test
    void reorderKitMovesWorkflow() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        KitDefinition combat = runtime.kitWorkflow().create("Combat");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.reorderKit(runtime, combat.id(), 0);

        assertTrue(outcome.success());
        assertEquals("workflow moved", outcome.status());
        assertEquals(List.of(combat.id(), mining.id()), kitIds(runtime.kitWorkflow().kits()));
        assertWorkflowMetadataInvalidation(outcome, "workflow_moved");
    }

    @Test
    void reorderKitRejectsUnknownWorkflow() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.reorderKit(runtime, "missing", 0);

        assertFalse(outcome.success());
        assertEquals("unknown_kit", outcome.diagnostics());
    }

    @Test
    void setKitScopedDesiredCountWritesKitScope() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setKitScopedDesiredCount(
                runtime,
                kit.id(),
                "minecraft:torch",
                "",
                "",
                32
        );

        assertTrue(outcome.success());
        assertEquals("tab_desired_set_32", outcome.status());
        assertEquals(32, runtime.desiredCountWorkflow().getForKit(kit.id(), ItemIdentity.of("minecraft:torch")));
    }

    @Test
    void setKitAcceptedInputWritesAcceptedRuleWithoutDesiredCount() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Smelting");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setKitAcceptedInput(
                runtime,
                kit.id(),
                "ITEM_TAG",
                "",
                "",
                "",
                "forge:ores/iron",
                1);

        assertTrue(outcome.success());
        assertEquals("workflow input accepted", outcome.status());
        assertTrue(runtime.kitWorkflow().kit(kit.id()).acceptedInputs().stream()
                .anyMatch(rule -> rule.itemTag() && "forge:ores/iron".equals(rule.tagId())));
        assertEquals(0, runtime.desiredCountWorkflow().getForKit(kit.id(), ItemIdentity.of("minecraft:iron_ore")));
    }

    @Test
    void activeExactAcceptedInputReturnsTargetInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Smelting");
        runtime.kitWorkflow().activate(kit.id());
        ItemIdentity ore = ItemIdentity.of("minecraft:iron_ore");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setKitAcceptedInput(
                runtime,
                kit.id(),
                "EXACT_ITEM",
                "minecraft:iron_ore",
                "",
                "",
                "",
                1);

        assertTrue(outcome.success());
        assertEquals("workflow input accepted", outcome.status());
        assertEquals(0, runtime.desiredCountWorkflow().getForKit(kit.id(), ore));
        assertTargetInvalidation(outcome, ore);
    }

    @Test
    void tagAcceptedInputReturnsLocalizedTagInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Smelting");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setKitAcceptedInput(
                runtime,
                kit.id(),
                "ITEM_TAG",
                "",
                "",
                "",
                "forge:ores/iron",
                1);

        assertTrue(outcome.success());
        assertEquals("workflow input accepted", outcome.status());
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED, invalidation.reason());
        assertTrue(invalidation.identities().isEmpty());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WORKFLOW));
        assertEquals("workflow_accepted_input_tag_changed", invalidation.diagnostics());
    }

    @Test
    void activeWorkflowMemberChangeReturnsTargetInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setKitMember(
                runtime,
                kit.id(),
                "minecraft:torch",
                "",
                "",
                1);

        assertTrue(outcome.success());
        assertEquals("workflow member added", outcome.status());
        assertTargetInvalidation(outcome, ItemIdentity.of("minecraft:torch"));
    }

    @Test
    void activeWorkflowSlotChangeReturnsTargetInvalidation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setKitSlotIdentity(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                null,
                kit.id(),
                0,
                0,
                "minecraft:torch",
                "",
                "");

        assertTrue(outcome.success());
        assertEquals("workflow slot updated", outcome.status());
        assertTargetInvalidation(outcome, ItemIdentity.of("minecraft:torch"));
    }

    @Test
    void playerDesiredCountUsesActiveKitScopeWhenKitActive() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setPlayerDesiredCount(
                runtime,
                "minecraft:coal",
                "",
                "",
                16
        );

        assertTrue(outcome.success());
        assertEquals("desired_count_kit_16", outcome.status());
        assertEquals(16, runtime.desiredCountWorkflow().getForKit(kit.id(), ItemIdentity.of("minecraft:coal")));
        assertEquals(0, runtime.desiredCountWorkflow().getPlayer(ItemIdentity.of("minecraft:coal")));
        assertTargetInvalidation(outcome, ItemIdentity.of("minecraft:coal"));
    }

    @Test
    void clearPlayerDesiredCountDoesNotLowerAllFallbackWhenKitActive() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:coal");
        runtime.desiredCountWorkflow().setPlayer(identity, 1);
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setPlayerDesiredCount(
                runtime,
                "minecraft:coal",
                "",
                "",
                0
        );

        assertTrue(outcome.success());
        assertEquals("noop", outcome.status());
        assertEquals(1, runtime.desiredCountWorkflow().getPlayer(identity));
        assertEquals(0, runtime.desiredCountWorkflow().getForKit(kit.id(), identity));
    }

    @Test
    void adjustPlayerDesiredCountUsesGlobalScopeWhenNoKitActive() {
        WorkflowDomainRuntime runtime = runtime();
        SlotWorkspaceCommandService.setPlayerDesiredCount(runtime, "minecraft:arrow", "", "", 4);

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.adjustPlayerDesiredCount(
                runtime,
                "minecraft:arrow",
                "",
                "",
                3
        );

        assertTrue(outcome.success());
        assertEquals("desired_count_global_7", outcome.status());
        assertEquals(7, runtime.desiredCountWorkflow().getPlayer(ItemIdentity.of("minecraft:arrow")));
    }

    @Test
    void adjustPlayerDesiredCountClearsGlobalScopeAtZero() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:arrow");
        SlotWorkspaceCommandService.setPlayerDesiredCount(runtime, "minecraft:arrow", "", "", 1);

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.adjustPlayerDesiredCount(
                runtime,
                "minecraft:arrow",
                "",
                "",
                -1
        );

        assertTrue(outcome.success());
        assertEquals("desired_count_cleared", outcome.status());
        assertEquals(0, runtime.desiredCountWorkflow().getPlayer(identity));
        assertFalse(runtime.desiredCountWorkflow().allPlayer().containsKey(identity));
    }

    @Test
    void adjustPlayerDesiredCountDoesNotLowerAllFallbackWhenKitActive() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:arrow");
        runtime.desiredCountWorkflow().setPlayer(identity, 1);
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.adjustPlayerDesiredCount(
                runtime,
                "minecraft:arrow",
                "",
                "",
                -1
        );

        assertTrue(outcome.success());
        assertEquals("noop", outcome.status());
        assertEquals(1, runtime.desiredCountWorkflow().getPlayer(identity));
        assertEquals(0, runtime.desiredCountWorkflow().getForKit(kit.id(), identity));
    }

    @Test
    void wantedCountSeedsFromCarriedCountAndTogglesClear() {
        WorkflowDomainRuntime runtime = runtime();
        InventoryAuthoritySnapshot authority = carriedAuthority("minecraft:torch", 3);

        WorkspaceCommandOutcome set = SlotWorkspaceCommandService.toggleWantedCount(
                runtime, authority, "minecraft:torch", "", "");

        assertTrue(set.success());
        assertEquals(4, runtime.wantedCountWorkflow().getPlayer(ItemIdentity.of("minecraft:torch")));

        WorkspaceCommandOutcome clear = SlotWorkspaceCommandService.toggleWantedCount(
                runtime, authority, "minecraft:torch", "", "");

        assertTrue(clear.success());
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(ItemIdentity.of("minecraft:torch")));
    }

    @Test
    void setWantedCountUsesExactHoverTarget() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setWantedCount(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                "minecraft:torch",
                "",
                "",
                1);

        assertTrue(outcome.success());
        assertEquals("wanted count updated", outcome.status());
        assertEquals(1, runtime.wantedCountWorkflow().getPlayer(ItemIdentity.of("minecraft:torch")));
    }

    @Test
    void setWantedCountUsesActiveWorkflowTabScope() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setWantedCount(
                runtime,
                carriedAuthority("minecraft:torch", 1),
                "minecraft:torch",
                "",
                "",
                1);

        assertTrue(outcome.success());
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(identity));
        assertEquals(1, runtime.wantedCountWorkflow().getForKit(kit.id(), identity));
        assertTargetInvalidation(outcome, identity);
    }

    @Test
    void setWantedCountClearsWhenTargetAlreadyCarried() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");
        runtime.wantedCountWorkflow().setPlayer(identity, 4);

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.setWantedCount(
                runtime,
                carriedAuthority("minecraft:torch", 1),
                "minecraft:torch",
                "",
                "",
                1);

        assertTrue(outcome.success());
        assertEquals("wanted cleared", outcome.status());
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(identity));
        assertFalse(runtime.wantedCountWorkflow().allPlayer().containsKey(identity));
    }

    @Test
    void wantedCountSetParticipatesInUndoRedo() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");

        WorkspaceCommandOutcome set = SlotWorkspaceCommandService.setWantedCount(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                "minecraft:torch",
                "",
                "",
                1);

        assertTrue(set.success());
        assertEquals(1, runtime.wantedCountWorkflow().getPlayer(identity));

        WorkspaceCommandOutcome undo = SlotWorkspaceCommandService.performUndo(runtime);
        assertTrue(undo.success());
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(identity));

        WorkspaceCommandOutcome redo = SlotWorkspaceCommandService.performRedo(runtime);
        assertTrue(redo.success());
        assertEquals(1, runtime.wantedCountWorkflow().getPlayer(identity));
    }

    @Test
    void wantedCountClearRestoresPreviousTargetOnUndo() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");
        runtime.wantedCountWorkflow().setPlayer(identity, 4);
        runtime.undoStack().clear();

        WorkspaceCommandOutcome clear = SlotWorkspaceCommandService.setWantedCount(
                runtime,
                carriedAuthority("minecraft:torch", 1),
                "minecraft:torch",
                "",
                "",
                1);

        assertTrue(clear.success());
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(identity));

        WorkspaceCommandOutcome undo = SlotWorkspaceCommandService.performUndo(runtime);
        assertTrue(undo.success());
        assertEquals(4, runtime.wantedCountWorkflow().getPlayer(identity));
    }

    @Test
    void wantedCountClearUsesMovableToolIdentity() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity stored = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:12}");
        ItemIdentity current = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        runtime.wantedCountWorkflow().setPlayer(stored, 1);

        assertEquals(1, runtime.wantedCountWorkflow().getPlayer(current));
        assertTrue(runtime.wantedCountWorkflow().allPlayer()
                .containsKey(ItemIdentity.of("gtceu:steel_mining_hammer")));

        WorkspaceCommandOutcome clear = SlotWorkspaceCommandService.setWantedCount(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                "gtceu:steel_mining_hammer",
                "ITEM_ID_AND_COMPONENTS",
                "{Damage:512}",
                0);

        assertTrue(clear.success());
        assertEquals("wanted cleared", clear.status());
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(stored));
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(current));
        assertFalse(runtime.wantedCountWorkflow().allPlayer()
                .containsKey(ItemIdentity.of("gtceu:steel_mining_hammer")));
    }

    @Test
    void wantedCountAdjustClearsWhenScrolledToCarriedTarget() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");
        InventoryAuthoritySnapshot carriedThree = carriedAuthority("minecraft:torch", 3);
        runtime.wantedCountWorkflow().setPlayer(identity, 4);

        WorkspaceCommandOutcome adjusted = SlotWorkspaceCommandService.adjustWantedCount(
                runtime, carriedThree, "minecraft:torch", "", "", -1);

        assertTrue(adjusted.success());
        assertEquals("wanted cleared", adjusted.status());
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(identity));
        assertFalse(runtime.wantedCountWorkflow().allPlayer().containsKey(identity));
    }

    @Test
    void wantedCountAdjustKeepsUnsatisfiedTargetAndClearsWhenSatisfied() {
        WorkflowDomainRuntime runtime = runtime();
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");
        InventoryAuthoritySnapshot carriedThree = carriedAuthority("minecraft:torch", 3);
        runtime.wantedCountWorkflow().setPlayer(identity, 8);

        WorkspaceCommandOutcome adjusted = SlotWorkspaceCommandService.adjustWantedCount(
                runtime, carriedThree, "minecraft:torch", "", "", -3);

        assertTrue(adjusted.success());
        assertEquals(5, runtime.wantedCountWorkflow().getPlayer(identity));

        SlotWorkspaceCommandService.clearSatisfiedWantedCounts(runtime, carriedThree);
        assertEquals(5, runtime.wantedCountWorkflow().getPlayer(identity));

        SlotWorkspaceCommandService.clearSatisfiedWantedCounts(runtime, carriedAuthority("minecraft:torch", 5));
        assertEquals(0, runtime.wantedCountWorkflow().getPlayer(identity));
    }

    @Test
    void wantedCountsDoNotWritePersistentDesiredCounts() {
        WorkflowDomainRuntime runtime = runtime();
        InventoryAuthoritySnapshot authority = carriedAuthority("minecraft:arrow", 2);
        SlotWorkspaceCommandService.setPlayerDesiredCount(runtime, "minecraft:arrow", "", "", 12);

        SlotWorkspaceCommandService.toggleWantedCount(runtime, authority, "minecraft:arrow", "", "");
        SlotWorkspaceCommandService.adjustWantedCount(runtime, authority, "minecraft:arrow", "", "", 3);

        ItemIdentity identity = ItemIdentity.of("minecraft:arrow");
        assertEquals(12, runtime.desiredCountWorkflow().getPlayer(identity));
        assertEquals(6, runtime.wantedCountWorkflow().getPlayer(identity));
    }

    private static WorkflowDomainRuntime runtime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }

    private static List<String> kitIds(List<KitDefinition> kits) {
        return kits.stream()
                .map(KitDefinition::id)
                .toList();
    }

    private static void assertTargetInvalidation(WorkspaceCommandOutcome outcome, ItemIdentity identity) {
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().contains(identity));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WAYFINDING));
    }

    private static void assertWorkflowMetadataInvalidation(
            WorkspaceCommandOutcome outcome,
            String diagnostics
    ) {
        assertEquals(1, outcome.invalidations().size());
        WorkspaceInvalidation invalidation = outcome.invalidations().get(0);
        assertEquals(WorkspaceInvalidation.Reason.WORKFLOW_METADATA_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertTrue(invalidation.storageIds().isEmpty());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WORKFLOW));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.FRAME));
        assertEquals(diagnostics, invalidation.diagnostics());
    }

    private static InventoryAuthoritySnapshot carriedAuthority(String itemId, int count) {
        InventoryHostDescriptor host = host();
        return InventoryAuthorityFixtures.authority(
                host,
                Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(new InventoryStackSnapshot(0, new ItemStack(itemId, count, 64), count))),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 27));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "workspace-command.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "workspace-command.test",
                Component.literal("Workspace Command Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty())),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                "");
    }

    private static final Function<InventoryEntrySnapshot, ItemIdentity> IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentity.of(entry.stack().itemId());

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class RecordingActionExecutor implements Function<InventoryActionRequest, InventoryActionOutcome> {
        final List<InventoryActionRequest> requests = new ArrayList<>();

        @Override
        public InventoryActionOutcome apply(InventoryActionRequest request) {
            requests.add(request);
            assertNotNull(request);
            return new InventoryActionOutcome(
                    HostInstanceKey.empty(),
                    new ServerMenuRef("", -1),
                    request.requestId(),
                    request.kind() == null ? InventoryActionKind.TRANSFER : request.kind(),
                    InventoryActionMode.EXECUTE,
                    InventoryActionQuantity.DEFAULT,
                    InventoryActionScope.BEST_SINGLE_SOURCE,
                    request.conflictPolicy(),
                    request.origin(),
                    request.primaryTarget(),
                    request.secondaryTarget(),
                    InventoryActionStatus.SUCCESS,
                    List.<InventoryCommandReasonCode>of(),
                    1,
                    1,
                    false,
                    List.of(),
                    ItemStack.EMPTY,
                    ""
            );
        }
    }
}
