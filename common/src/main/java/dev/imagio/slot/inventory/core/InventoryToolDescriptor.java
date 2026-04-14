package dev.imagio.slot.inventory.core;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record InventoryToolDescriptor(
        String id,
        String providerId,
        InventoryToolKind kind,
        Component title,
        ToolPresentationHints presentationHints,
        int priority,
        boolean live,
        boolean open,
        boolean activationSimulationSupported,
        ToolActivationToken activationToken,
        List<ToolRegionDescriptor> regions,
        List<InventoryToolAction> actions,
        List<InventoryToolToggle> toggles,
        Map<InventoryToolToggleId, Boolean> toggleStates,
        Map<String, String> statePayload,
        CraftingSurfaceDescriptor craftingSurface,
        String diagnostics
) {
    public InventoryToolDescriptor(
            String id,
            String providerId,
            InventoryToolKind kind,
            Component title,
            ToolPresentationHints presentationHints,
            int priority,
            boolean live,
            boolean open,
            ToolActivationToken activationToken,
            List<ToolRegionDescriptor> regions,
            List<InventoryToolAction> actions,
            List<InventoryToolToggle> toggles,
            Map<InventoryToolToggleId, Boolean> toggleStates,
            Map<String, String> statePayload,
            String diagnostics
    ) {
        this(
                id,
                providerId,
                kind,
                title,
                presentationHints,
                priority,
                live,
                open,
                true,
                activationToken,
                regions,
                actions,
                toggles,
                toggleStates,
                statePayload,
                null,
                diagnostics
        );
    }

    public InventoryToolDescriptor(
            String id,
            String providerId,
            InventoryToolKind kind,
            Component title,
            ToolPresentationHints presentationHints,
            int priority,
            boolean live,
            boolean open,
            boolean activationSimulationSupported,
            ToolActivationToken activationToken,
            List<ToolRegionDescriptor> regions,
            List<InventoryToolAction> actions,
            List<InventoryToolToggle> toggles,
            Map<InventoryToolToggleId, Boolean> toggleStates,
            Map<String, String> statePayload,
            String diagnostics
    ) {
        this(
                id,
                providerId,
                kind,
                title,
                presentationHints,
                priority,
                live,
                open,
                activationSimulationSupported,
                activationToken,
                regions,
                actions,
                toggles,
                toggleStates,
                statePayload,
                null,
                diagnostics
        );
    }

    public InventoryToolDescriptor {
        id = id == null ? "" : id;
        providerId = providerId == null ? "" : providerId;
        kind = kind == null ? InventoryToolKind.PROVIDER_DEFINED : kind;
        title = title == null ? Component.empty() : title;
        regions = regions == null ? List.of() : List.copyOf(regions);
        actions = actions == null ? List.of() : List.copyOf(actions);
        toggles = toggles == null ? List.of() : List.copyOf(toggles);
        toggleStates = toggleStates == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(toggleStates));
        statePayload = statePayload == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(statePayload));
        craftingSurface = craftingSurface != null && craftingSurface.present() ? craftingSurface : null;
        diagnostics = diagnostics == null ? "" : diagnostics;
        if (id.isBlank()) {
            throw new IllegalArgumentException("tool id must not be blank");
        }
    }

    public boolean matchesToolId(String toolId) {
        return toolId != null && !toolId.isBlank() && toolId.equals(id);
    }

    public boolean toggleEnabled(InventoryToolToggleId toggleId) {
        return toggleId != null && Boolean.TRUE.equals(toggleStates.get(toggleId));
    }
}
