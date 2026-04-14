package dev.imagio.slot.inventory.core;

import java.util.LinkedHashMap;
import java.util.Map;

public record PlayerRuntimeStateDescriptor(
        String selectedQuickAccessLaneId,
        int selectedQuickAccessSlotIndex,
        String mainHandSourceId,
        String offhandSourceId,
        Map<String, String> equipmentGroupBindings
) {
    public PlayerRuntimeStateDescriptor {
        selectedQuickAccessLaneId = selectedQuickAccessLaneId == null ? "" : selectedQuickAccessLaneId;
        selectedQuickAccessSlotIndex = Math.max(-1, selectedQuickAccessSlotIndex);
        mainHandSourceId = mainHandSourceId == null ? "" : mainHandSourceId;
        offhandSourceId = offhandSourceId == null ? "" : offhandSourceId;
        equipmentGroupBindings = equipmentGroupBindings == null
                ? Map.of()
                : Map.copyOf(new LinkedHashMap<>(equipmentGroupBindings));
    }

    public static PlayerRuntimeStateDescriptor vanilla(int selectedQuickAccessSlotIndex) {
        return new PlayerRuntimeStateDescriptor(
                BuiltinInventoryIds.QUICK_ACCESS_LANE_0,
                selectedQuickAccessSlotIndex,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                BuiltinInventoryIds.PLAYER_OFFHAND,
                Map.of(
                        BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR, BuiltinInventoryIds.PLAYER_ARMOR,
                        BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, BuiltinInventoryIds.PLAYER_OFFHAND
                )
        );
    }
}
