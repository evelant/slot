package dev.imagio.slot.session;

import java.util.LinkedHashMap;
import java.util.Map;

public record PlayerRuntimeStateDescriptor(
        String selectedHotbarLaneId,
        int selectedHotbarSlotIndex,
        String mainHandSourceId,
        String offhandSourceId,
        Map<String, String> equipmentGroupBindings
) {
    public PlayerRuntimeStateDescriptor {
        selectedHotbarLaneId = selectedHotbarLaneId == null ? "" : selectedHotbarLaneId;
        selectedHotbarSlotIndex = Math.max(-1, selectedHotbarSlotIndex);
        mainHandSourceId = mainHandSourceId == null ? "" : mainHandSourceId;
        offhandSourceId = offhandSourceId == null ? "" : offhandSourceId;
        equipmentGroupBindings = equipmentGroupBindings == null
                ? Map.of()
                : Map.copyOf(new LinkedHashMap<>(equipmentGroupBindings));
    }

    public static PlayerRuntimeStateDescriptor vanilla(int selectedHotbarSlotIndex) {
        return new PlayerRuntimeStateDescriptor(
                "0",
                selectedHotbarSlotIndex,
                ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR,
                ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND,
                Map.of("armor", ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR)
        );
    }
}
