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
                false,
                true,
                false,
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
