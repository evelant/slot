package dev.imagio.slot.inventory.core;

import java.util.LinkedHashSet;
import java.util.Set;

public record ToolRegionDescriptor(
        String id,
        ToolRegionRole role,
        int logicalSlotCount,
        InventoryBindingRoute bindingRoute,
        Set<InventoryCapability> capabilities,
        boolean simulationSupported,
        boolean sourceLike,
        String linkedSourceId,
        String diagnostics
) {
    public ToolRegionDescriptor(
            String id,
            ToolRegionRole role,
            int logicalSlotCount,
            InventoryBindingRoute bindingRoute,
            Set<InventoryCapability> capabilities,
            boolean sourceLike,
            String linkedSourceId,
            String diagnostics
    ) {
        this(id, role, logicalSlotCount, bindingRoute, capabilities, true, sourceLike, linkedSourceId, diagnostics);
    }

    public ToolRegionDescriptor {
        id = id == null ? "" : id;
        role = role == null ? ToolRegionRole.PROVIDER_DEFINED : role;
        logicalSlotCount = Math.max(0, logicalSlotCount);
        bindingRoute = bindingRoute == null ? InventoryBindingRoute.TOOL : bindingRoute;
        capabilities = capabilities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(capabilities));
        linkedSourceId = linkedSourceId == null ? "" : linkedSourceId;
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public boolean supports(InventoryCapability capability) {
        return capability != null && capabilities.contains(capability);
    }
}
