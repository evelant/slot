package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadoutCaptureServiceTest {
    @Test
    void capturesTypedQuickAccessAndEquipmentTargetsFromDescriptorDrivenHost() {
        InventorySourceDescriptor utilitySource = InventorySourceDescriptor.builder("curios:utility_lane")
                .label(Component.literal("Utility"))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.QUICK_ACCESS)
                .laneId("curios:utility")
                .logicalSlotCount(2)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT, InventoryCapability.QUICK_ACCESS_ASSIGN))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .build();
        InventorySourceDescriptor charmSource = InventorySourceDescriptor.builder("curios:charm_slots")
                .label(Component.literal("Charms"))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.EQUIPMENT)
                .groupId("curios:charms")
                .logicalSlotCount(2)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT, InventoryCapability.EQUIP, InventoryCapability.UNEQUIP))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .build();

        TestMenu menu = new TestMenu();
        InventoryHostDescriptor host = new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                dev.imagio.slot.inventory.integration.InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty()),
                        utilitySource,
                        charmSource
                ),
                List.of(
                        BuiltinInventoryDescriptors.builtInQuickAccessLanes().getFirst(),
                        new QuickAccessLaneDescriptor(
                                "curios:utility",
                                Component.literal("Utility"),
                                utilitySource.id(),
                                2,
                                Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT, InventoryCapability.QUICK_ACCESS_ASSIGN),
                                "",
                                200
                        )
                ),
                List.of(
                        BuiltinInventoryDescriptors.builtInEquipmentGroups().get(0),
                        BuiltinInventoryDescriptors.builtInEquipmentGroups().get(1),
                        new EquipmentGroupDescriptor(
                                "curios:charms",
                                Component.literal("Charms"),
                                charmSource.id(),
                                2,
                                Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT, InventoryCapability.EQUIP, InventoryCapability.UNEQUIP),
                                "",
                                200
                        )
                ),
                List.of(),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );

        var authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 32, 64), 32)),
                        BuiltinInventoryIds.PLAYER_OFFHAND, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:shield", 1, 1), 1)),
                        utilitySource.id(), List.of(new InventoryStackSnapshot(1, new ItemStack("minecraft:spyglass", 1, 1), 1)),
                        charmSource.id(), List.of(new InventoryStackSnapshot(0, new ItemStack("curios:charm_of_depths", 1, 1), 1))
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, 9,
                        BuiltinInventoryIds.PLAYER_OFFHAND, 1,
                        utilitySource.id(), 2,
                        charmSource.id(), 2
                )
        );

        Set<QuickAccessLoadoutEntry> entries = LoadoutCaptureService.captureEntries(
                authority,
                snapshot -> ItemIdentity.of(snapshot.stack().itemId())
        );

        assertEquals(4, entries.size());
        assertTrue(entries.contains(new QuickAccessLoadoutEntry(
                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                ItemIdentity.of("minecraft:torch")
        )));
        assertTrue(entries.contains(new QuickAccessLoadoutEntry(
                new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                ItemIdentity.of("minecraft:shield")
        )));
        assertTrue(entries.contains(new QuickAccessLoadoutEntry(
                new LoadoutTarget.QuickAccessLaneTarget("curios:utility", 1),
                ItemIdentity.of("minecraft:spyglass")
        )));
        assertTrue(entries.contains(new QuickAccessLoadoutEntry(
                new LoadoutTarget.EquipmentSlotTarget("curios:charms", 0),
                ItemIdentity.of("curios:charm_of_depths")
        )));
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
