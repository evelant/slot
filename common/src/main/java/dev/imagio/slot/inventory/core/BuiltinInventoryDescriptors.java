package dev.imagio.slot.inventory.core;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

public final class BuiltinInventoryDescriptors {
    private BuiltinInventoryDescriptors() {
    }

    public static List<InventorySourceDescriptor> builtInPlayerSources(InventoryTopologyDescriptor topology) {
        return List.of(
                playerMain(topology),
                quickAccessLane0Source(topology),
                armorSource(topology),
                offhandSource(topology)
        );
    }

    public static List<QuickAccessLaneDescriptor> builtInQuickAccessLanes() {
        return List.of(new QuickAccessLaneDescriptor(
                BuiltinInventoryIds.QUICK_ACCESS_LANE_0,
                Component.translatable("slot.source.hotbar"),
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                9,
                Set.of(
                        InventoryCapability.INSERT,
                        InventoryCapability.EXTRACT,
                        InventoryCapability.USE,
                        InventoryCapability.DROP,
                        InventoryCapability.QUICK_ACCESS_ASSIGN
                ),
                "",
                20
        ));
    }

    public static List<EquipmentGroupDescriptor> builtInEquipmentGroups() {
        return List.of(
                new EquipmentGroupDescriptor(
                        BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR,
                        Component.translatable("slot.source.armor"),
                        BuiltinInventoryIds.PLAYER_ARMOR,
                        4,
                        Set.of(
                                InventoryCapability.INSERT,
                                InventoryCapability.EXTRACT,
                                InventoryCapability.EQUIP,
                                InventoryCapability.UNEQUIP
                        ),
                        "",
                        25
                ),
                new EquipmentGroupDescriptor(
                        BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND,
                        Component.translatable("slot.source.offhand"),
                        BuiltinInventoryIds.PLAYER_OFFHAND,
                        1,
                        Set.of(
                                InventoryCapability.INSERT,
                                InventoryCapability.EXTRACT,
                                InventoryCapability.USE,
                                InventoryCapability.DROP,
                                InventoryCapability.EQUIP,
                                InventoryCapability.UNEQUIP
                        ),
                        "",
                        30
                )
        );
    }

    // Carried-source stableOrder ordering: lower = tried first.
    //
    // Intent: "backpack is overflow, main is workspace." Insertion / displacement
    // prefers the overflow area so the player's main-inventory layout stays intact.
    // Extraction follows the same order — if an identity sits in both backpack and
    // main, pull from the backpack first to preserve the visible main layout.
    //
    //   Sophisticated Backpack sources:  15 + carrierSlotIndex  (15–50 range)
    //   PLAYER_MAIN:                     100
    //   PLAYER_QUICK_ACCESS_LANE_0:      110   (hotbar — last resort, actively used)
    //   PLAYER_ARMOR:                    120
    //   PLAYER_OFFHAND:                  130
    public static InventorySourceDescriptor playerMain(InventoryTopologyDescriptor topology) {
        boolean menuBacked = topology.menuBacksSource(BuiltinInventoryIds.PLAYER_MAIN);
        return InventorySourceDescriptor.builder(BuiltinInventoryIds.PLAYER_MAIN)
                .label(Component.translatable("slot.source.main"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.MAIN)
                .groupId("inventory")
                .logicalSlotCount(Math.max(27, topology.menuSlotsForSource(BuiltinInventoryIds.PLAYER_MAIN).size()))
                .bindingRoute(menuBacked ? InventoryBindingRoute.MENU : InventoryBindingRoute.PLAYER)
                .capabilities(Set.of(
                        InventoryCapability.INSERT,
                        InventoryCapability.EXTRACT,
                        InventoryCapability.QUICK_ACCESS_ASSIGN
                ))
                .actionRoute(menuBacked ? InventoryActionRoute.MENU_MUTATION : InventoryActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(100)
                .build();
    }

    public static InventorySourceDescriptor quickAccessLane0Source(InventoryTopologyDescriptor topology) {
        boolean menuBacked = topology.menuBacksSource(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0);
        return InventorySourceDescriptor.builder(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0)
                .label(Component.translatable("slot.source.hotbar"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.QUICK_ACCESS)
                .laneId(BuiltinInventoryIds.QUICK_ACCESS_LANE_0)
                .groupId("inventory")
                .logicalSlotCount(Math.max(9, topology.menuSlotsForSource(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0).size()))
                .bindingRoute(menuBacked ? InventoryBindingRoute.MENU : InventoryBindingRoute.PLAYER)
                .capabilities(Set.of(
                        InventoryCapability.INSERT,
                        InventoryCapability.EXTRACT,
                        InventoryCapability.USE,
                        InventoryCapability.DROP,
                        InventoryCapability.QUICK_ACCESS_ASSIGN
                ))
                .actionRoute(menuBacked ? InventoryActionRoute.MENU_MUTATION : InventoryActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(110)
                .build();
    }

    public static InventorySourceDescriptor armorSource(InventoryTopologyDescriptor topology) {
        boolean menuBacked = topology.menuBacksSource(BuiltinInventoryIds.PLAYER_ARMOR);
        return InventorySourceDescriptor.builder(BuiltinInventoryIds.PLAYER_ARMOR)
                .label(Component.translatable("slot.source.armor"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.EQUIPMENT)
                .groupId(BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR)
                .logicalSlotCount(Math.max(4, topology.menuSlotsForSource(BuiltinInventoryIds.PLAYER_ARMOR).size()))
                .bindingRoute(menuBacked ? InventoryBindingRoute.MENU : InventoryBindingRoute.PLAYER)
                .capabilities(Set.of(
                        InventoryCapability.INSERT,
                        InventoryCapability.EXTRACT,
                        InventoryCapability.EQUIP,
                        InventoryCapability.UNEQUIP
                ))
                .actionRoute(menuBacked ? InventoryActionRoute.MENU_MUTATION : InventoryActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(120)
                .build();
    }

    public static InventorySourceDescriptor offhandSource(InventoryTopologyDescriptor topology) {
        boolean menuBacked = topology.menuBacksSource(BuiltinInventoryIds.PLAYER_OFFHAND);
        return InventorySourceDescriptor.builder(BuiltinInventoryIds.PLAYER_OFFHAND)
                .label(Component.translatable("slot.source.offhand"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.OFFHAND)
                .groupId(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND)
                .logicalSlotCount(1)
                .bindingRoute(menuBacked ? InventoryBindingRoute.MENU : InventoryBindingRoute.PLAYER)
                .capabilities(Set.of(
                        InventoryCapability.INSERT,
                        InventoryCapability.EXTRACT,
                        InventoryCapability.USE,
                        InventoryCapability.DROP,
                        InventoryCapability.EQUIP,
                        InventoryCapability.UNEQUIP
                ))
                .actionRoute(menuBacked ? InventoryActionRoute.MENU_MUTATION : InventoryActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(130)
                .build();
    }
}
