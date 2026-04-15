package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryBindingResolverTest {
    @Test
    void resolvesSourceLaneEquipmentAndToolRegionTargetsFromHostTopology() {
        InventoryHostDescriptor host = new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "test.provider", ""),
                InventoryHostDescriptor.serverMenuRef(new TestMenu()),
                "test.screen",
                Component.literal("Test"),
                new TestMenu(),
                new InventoryTopologyDescriptor(
                        Map.of(
                                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0, List.of(10, 11, 12),
                                BuiltinInventoryIds.PLAYER_ARMOR, List.of(20, 21, 22, 23),
                                BuiltinInventoryIds.PLAYER_OFFHAND, List.of(24)
                        ),
                        Map.of(),
                        Map.of("tool:craft/input", List.of(30, 31, 32))
                ),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(1),
                List.of(
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty())
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(new InventoryToolDescriptor(
                        "tool:craft",
                        "test",
                        InventoryToolKind.CRAFTING_GRID,
                        Component.literal("Craft"),
                        new ToolPresentationHints("Craft", 0, "docked", 70),
                        0,
                        true,
                        true,
                        null,
                        List.of(new ToolRegionDescriptor(
                                "tool:craft/input",
                                ToolRegionRole.INPUT,
                                3,
                                InventoryBindingRoute.MENU,
                                Set.of(InventoryCapability.TOOL_REGION_MUTATION),
                                false,
                                "",
                                ""
                        )),
                        List.of(),
                        List.of(),
                        Map.of(),
                        Map.of(),
                        ""
                )),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );

        assertEquals(11, InventoryBindingResolver.resolveMenuSlot(
                host,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 1)
        ));
        assertEquals(21, InventoryBindingResolver.resolveMenuSlot(
                host,
                new InventoryActionTarget.EquipmentTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR, 1)
        ));
        assertEquals(24, InventoryBindingResolver.resolveMenuSlot(
                host,
                new InventoryActionTarget.EquipmentTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0)
        ));
        assertEquals(31, InventoryBindingResolver.resolveMenuSlot(
                host,
                new InventoryActionTarget.ToolRegionTarget("tool:craft", "tool:craft/input", 1)
        ));
        assertEquals(List.of(10, 11, 12), InventoryBindingResolver.resolveMenuSlotsForSource(
                host,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0
        ));
        assertEquals(null, InventoryBindingResolver.resolveMenuSlot(
                host,
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0)
        ));
        assertEquals(true, InventoryBindingResolver.isMenuBacked(
                host,
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0)
        ));
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
