package dev.imagio.slot.inventory.core;

import net.minecraft.network.chat.Component;

public record InventoryToolToggle(
        String stableId,
        InventoryToolToggleId id,
        Component label,
        Component tooltip,
        boolean simulationSupported
) {
    public InventoryToolToggle(
            String stableId,
            InventoryToolToggleId id,
            Component label,
            Component tooltip
    ) {
        this(stableId, id, label, tooltip, true);
    }

    public InventoryToolToggle {
        stableId = stableId == null ? "" : stableId;
        id = id == null ? InventoryToolToggleId.PROVIDER_DEFINED : id;
        label = label == null ? Component.empty() : label;
        tooltip = tooltip == null ? Component.empty() : tooltip;
        if (stableId.isBlank()) {
            throw new IllegalArgumentException("tool toggle stableId must not be blank");
        }
    }
}
