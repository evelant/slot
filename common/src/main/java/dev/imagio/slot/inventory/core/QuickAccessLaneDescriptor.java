package dev.imagio.slot.inventory.core;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashSet;
import java.util.Set;

public record QuickAccessLaneDescriptor(
        String id,
        Component label,
        String sourceId,
        int logicalSlotCount,
        Set<InventoryCapability> capabilities,
        String diagnostics,
        int stableOrder
) {
    public QuickAccessLaneDescriptor {
        id = id == null ? "" : id;
        label = label == null ? Component.empty() : label;
        sourceId = sourceId == null ? "" : sourceId;
        logicalSlotCount = Math.max(0, logicalSlotCount);
        capabilities = capabilities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(capabilities));
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public boolean supports(InventoryCapability capability) {
        return capability != null && capabilities.contains(capability);
    }
}
