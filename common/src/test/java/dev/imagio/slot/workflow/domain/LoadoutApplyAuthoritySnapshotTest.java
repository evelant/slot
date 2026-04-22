package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class LoadoutApplyAuthoritySnapshotTest {
    @Test
    void planUsesProviderEntryCandidateButStagesIntoExactSlotBackedCarriedSource() {
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        InventorySourceDescriptor quickAccess = BuiltinInventoryDescriptors.quickAccessLane0Source(topology);
        InventorySourceDescriptor providerEntrySource = InventorySourceDescriptor.builder("carried.terminal")
                .label(Component.literal("Terminal"))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(0)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(21)
                .build();
        InventorySourceDescriptor backpack = InventorySourceDescriptor.builder("carried.backpack")
                .label(Component.literal("Backpack"))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(3)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(22)
                .build();
        InventoryHostDescriptor host = host(List.of(
                quickAccess,
                providerEntrySource,
                backpack
        ));

        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        quickAccess.id(), new InventorySourceSnapshot(
                                quickAccess.id(),
                                9,
                                List.of(new InventoryEntrySnapshot(
                                        InventoryEntryKey.slot(quickAccess.id(), 0),
                                        new ItemStack("minecraft:dirt", 8, 64),
                                        8,
                                        ""
                                )),
                                ""
                        ),
                        providerEntrySource.id(), new InventorySourceSnapshot(
                                providerEntrySource.id(),
                                0,
                                List.of(new InventoryEntrySnapshot(
                                        InventoryEntryKey.providerEntry(providerEntrySource.id(), "torch-entry"),
                                        new ItemStack("minecraft:torch", 64, 64),
                                        64,
                                        ""
                                )),
                                ""
                        ),
                        backpack.id(), new InventorySourceSnapshot(backpack.id(), 3, List.of(), "")
                ),
                CursorStateSnapshot.empty()
        );

        QuickAccessLoadoutDefinition loadout = new QuickAccessLoadoutDefinition(
                "loadout",
                "Loadout",
                Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                        identity(new ItemStack("minecraft:torch", 1, 64))
                ))
        );

        LoadoutApplyService.LoadoutApplyPlan plan = LoadoutApplyService.plan(
                loadout,
                authority,
                ProtectionPolicy.allowAll(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                entry -> identity(entry.stack())
        );

        assertEquals(1, plan.operations().size());
        LoadoutApplyService.PlannedTargetOperation operation = plan.operations().getFirst();
        assertEquals(2, operation.requests().size());

        InventoryActionRequest stageRequest = operation.requests().getFirst();
        assertInstanceOf(InventoryActionTarget.QuickAccessTarget.class, stageRequest.primaryTarget());
        InventoryActionTarget.SourceSlotTarget stagingTarget = assertInstanceOf(InventoryActionTarget.SourceSlotTarget.class, stageRequest.secondaryTarget());
        org.junit.jupiter.api.Assertions.assertNotEquals("carried.terminal", stagingTarget.sourceId());
        org.junit.jupiter.api.Assertions.assertTrue(stagingTarget.slotIndex() >= 0);

        InventoryActionRequest applyRequest = operation.requests().get(1);
        InventoryActionTarget.SourceEntryTarget providerTarget = assertInstanceOf(InventoryActionTarget.SourceEntryTarget.class, applyRequest.primaryTarget());
        assertEquals("carried.terminal", providerTarget.sourceId());
        assertEquals("torch-entry", providerTarget.entryId());
        assertInstanceOf(InventoryActionTarget.QuickAccessTarget.class, applyRequest.secondaryTarget());
    }

    @Test
    void planClearTargetsStageCurrentOccupantOutOfTheHotbar() {
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        InventorySourceDescriptor quickAccess = BuiltinInventoryDescriptors.quickAccessLane0Source(topology);
        InventorySourceDescriptor main = BuiltinInventoryDescriptors.playerMain(topology);
        InventoryHostDescriptor host = host(List.of(quickAccess, main));

        ItemStack sword = new ItemStack("minecraft:iron_sword", 1, 1);

        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        quickAccess.id(), new InventorySourceSnapshot(
                                quickAccess.id(),
                                9,
                                List.of(new InventoryEntrySnapshot(
                                        InventoryEntryKey.slot(quickAccess.id(), 3), sword, 1, "")),
                                ""
                        ),
                        main.id(), new InventorySourceSnapshot(main.id(), 27, List.of(), "")
                ),
                CursorStateSnapshot.empty()
        );

        QuickAccessLoadoutDefinition loadout = new QuickAccessLoadoutDefinition("empty-page", "Empty", Set.of());
        Set<LoadoutTarget> clearTargets = Set.of(
                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 3)
        );

        LoadoutApplyService.LoadoutApplyPlan plan = LoadoutApplyService.plan(
                loadout,
                clearTargets,
                authority,
                ProtectionPolicy.allowAll(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                entry -> identity(entry.stack())
        );

        assertEquals(1, plan.operations().size());
        LoadoutApplyService.PlannedTargetOperation op = plan.operations().getFirst();
        assertEquals(1, op.requests().size());
        InventoryActionRequest clearRequest = op.requests().getFirst();
        assertEquals("workflow:loadout_clear", clearRequest.origin());
        assertInstanceOf(InventoryActionTarget.QuickAccessTarget.class, clearRequest.primaryTarget());
        assertInstanceOf(InventoryActionTarget.SourceSlotTarget.class, clearRequest.secondaryTarget());
        InventoryActionTarget.SourceSlotTarget stagingTarget =
                (InventoryActionTarget.SourceSlotTarget) clearRequest.secondaryTarget();
        assertEquals(main.id(), stagingTarget.sourceId());
    }

    @Test
    void planClearTargetsIsNoOpWhenSlotAlreadyEmpty() {
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        InventorySourceDescriptor quickAccess = BuiltinInventoryDescriptors.quickAccessLane0Source(topology);
        InventorySourceDescriptor main = BuiltinInventoryDescriptors.playerMain(topology);
        InventoryHostDescriptor host = host(List.of(quickAccess, main));

        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        quickAccess.id(), new InventorySourceSnapshot(quickAccess.id(), 9, List.of(), ""),
                        main.id(), new InventorySourceSnapshot(main.id(), 27, List.of(), "")
                ),
                CursorStateSnapshot.empty()
        );

        Set<LoadoutTarget> clearTargets = Set.of(
                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0)
        );
        LoadoutApplyService.LoadoutApplyPlan plan = LoadoutApplyService.plan(
                new QuickAccessLoadoutDefinition("empty", "Empty", Set.of()),
                clearTargets,
                authority,
                ProtectionPolicy.allowAll(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                entry -> identity(entry.stack())
        );

        org.junit.jupiter.api.Assertions.assertTrue(plan.operations().isEmpty());
        assertEquals(1, plan.satisfiedTargets().size());
    }

    @Test
    void planReorderingBeltItemsDoesNotProduceStaleStagingFromEmptiedCandidateSlot() {
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        InventorySourceDescriptor quickAccess = BuiltinInventoryDescriptors.quickAccessLane0Source(topology);
        InventorySourceDescriptor main = BuiltinInventoryDescriptors.playerMain(topology);
        InventoryHostDescriptor host = host(List.of(quickAccess, main));

        ItemStack pickaxe = new ItemStack("minecraft:iron_pickaxe", 1, 1);
        ItemStack sword = new ItemStack("minecraft:iron_sword", 1, 1);

        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                host,
                Map.of(
                        quickAccess.id(), new InventorySourceSnapshot(
                                quickAccess.id(),
                                9,
                                List.of(
                                        new InventoryEntrySnapshot(
                                                InventoryEntryKey.slot(quickAccess.id(), 0), pickaxe, 1, ""),
                                        new InventoryEntrySnapshot(
                                                InventoryEntryKey.slot(quickAccess.id(), 1), sword, 1, "")
                                ),
                                ""
                        ),
                        main.id(), new InventorySourceSnapshot(main.id(), 27, List.of(), "")
                ),
                CursorStateSnapshot.empty()
        );

        // Target: swap sword and pickaxe positions on the belt
        QuickAccessLoadoutDefinition loadout = new QuickAccessLoadoutDefinition(
                "swap",
                "Swap",
                Set.of(
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                                identity(sword)
                        ),
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 1),
                                identity(pickaxe)
                        )
                )
        );

        LoadoutApplyService.LoadoutApplyPlan plan = LoadoutApplyService.plan(
                loadout,
                authority,
                ProtectionPolicy.allowAll(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                entry -> identity(entry.stack())
        );

        // Both operations should be planned; neither should appear in missing.
        assertEquals(2, plan.operations().size());
        org.junit.jupiter.api.Assertions.assertTrue(plan.missingTargets().isEmpty(),
                "reorder should not produce missing targets: " + plan.diagnostics());

        // Second op must not stage from slot 1 expecting a sword — that slot is empty
        // after op 1 moves the sword to slot 0.
        LoadoutApplyService.PlannedTargetOperation secondOp = plan.operations().get(1);
        for (InventoryActionRequest request : secondOp.requests()) {
            if ("workflow:loadout_stage".equals(request.origin())
                    && request.primaryTarget() instanceof InventoryActionTarget.QuickAccessTarget qat
                    && qat.slotIndex() == 1) {
                org.junit.jupiter.api.Assertions.fail(
                        "second op should not stage from slot 1 after slot 1 was emptied by op 1");
            }
        }
    }

    private static ItemIdentity identity(ItemStack stack) {
        return new ItemIdentity(stack.itemId(), ItemComparisonMode.ITEM_ID, stack.componentFingerprint());
    }

    private static InventoryHostDescriptor host(List<InventorySourceDescriptor> sources) {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 1, "test.provider", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                sources,
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
