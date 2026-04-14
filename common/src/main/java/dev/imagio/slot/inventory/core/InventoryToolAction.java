package dev.imagio.slot.inventory.core;

import net.minecraft.network.chat.Component;

public record InventoryToolAction(
        String stableId,
        InventoryToolActionId id,
        Component label,
        Component tooltip,
        boolean simulationSupported
) {
    public InventoryToolAction(
            String stableId,
            InventoryToolActionId id,
            Component label,
            Component tooltip
    ) {
        this(stableId, id, label, tooltip, true);
    }

    public InventoryToolAction {
        stableId = stableId == null ? "" : stableId;
        id = id == null ? InventoryToolActionId.PROVIDER_DEFINED : id;
        label = label == null ? Component.empty() : label;
        tooltip = tooltip == null ? Component.empty() : tooltip;
        if (stableId.isBlank()) {
            throw new IllegalArgumentException("tool action stableId must not be blank");
        }
    }
}
