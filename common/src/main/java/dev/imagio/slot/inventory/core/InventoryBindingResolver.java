package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.action.InventoryActionTarget;

import java.util.List;

public final class InventoryBindingResolver {
    private InventoryBindingResolver() {
    }

    public static Integer resolveMenuSlot(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (host == null || target == null) {
            return null;
        }

        return switch (target) {
            case InventoryActionTarget.CursorTarget ignored -> null;
            case InventoryActionTarget.SourceTarget ignored -> null;
            case InventoryActionTarget.SourceSlotTarget slotTarget ->
                    host.topology().resolveMenuSlot(slotTarget.sourceId(), slotTarget.slotIndex());
            case InventoryActionTarget.SourceEntryTarget ignored -> null;
            case InventoryActionTarget.QuickAccessTarget laneTarget -> {
                QuickAccessLaneDescriptor lane = host.quickAccessLane(laneTarget.laneId());
                yield lane == null ? null : host.topology().resolveMenuSlot(lane.sourceId(), laneTarget.slotIndex());
            }
            case InventoryActionTarget.EquipmentTarget equipmentTarget -> {
                EquipmentGroupDescriptor group = host.equipmentGroup(equipmentTarget.groupId());
                yield group == null ? null : host.topology().resolveMenuSlot(group.sourceId(), equipmentTarget.slotIndex());
            }
            case InventoryActionTarget.ToolRegionTarget regionTarget -> {
                List<Integer> slots = host.topology().menuSlotsForToolRegion(regionTarget.regionId());
                yield regionTarget.slotIndex() < 0 || regionTarget.slotIndex() >= slots.size()
                        ? null
                        : slots.get(regionTarget.slotIndex());
            }
            case InventoryActionTarget.ToolControlTarget ignored -> null;
        };
    }

    public static List<Integer> resolveMenuSlotsForSource(InventoryHostDescriptor host, String sourceId) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return List.of();
        }
        return host.topology().menuSlotsForSource(sourceId);
    }

    public static boolean isMenuBacked(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (target instanceof InventoryActionTarget.SourceTarget sourceTarget) {
            return !resolveMenuSlotsForSource(host, sourceTarget.sourceId()).isEmpty();
        }
        return resolveMenuSlot(host, target) != null;
    }
}
