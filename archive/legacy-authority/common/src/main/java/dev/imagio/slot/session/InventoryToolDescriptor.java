package dev.imagio.slot.session;

import dev.imagio.slot.storage.adapter.ExternalToolAction;
import dev.imagio.slot.storage.adapter.ExternalToolActionId;
import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.adapter.ExternalToolToggle;
import dev.imagio.slot.storage.adapter.ExternalToolToggleId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record InventoryToolDescriptor(
        String id,
        String providerId,
        ExternalToolKind kind,
        List<ToolRegionDescriptor> regions,
        List<ExternalToolAction> supportedActions,
        List<ExternalToolToggle> supportedToggles,
        boolean live,
        ToolOpenCommand activationCommand,
        Map<ExternalToolToggleId, Boolean> toggleStates,
        Map<String, String> statePayload,
        ExternalToolSpec presentationSpec
) {
    public InventoryToolDescriptor {
        id = id == null ? "" : id;
        providerId = providerId == null ? "" : providerId;
        kind = kind == null && presentationSpec != null ? presentationSpec.kind() : kind;
        regions = regions == null ? List.of() : List.copyOf(regions);
        supportedActions = supportedActions == null ? List.of() : List.copyOf(supportedActions);
        supportedToggles = supportedToggles == null ? List.of() : List.copyOf(supportedToggles);
        toggleStates = toggleStates == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(toggleStates));
        statePayload = statePayload == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(statePayload));
    }

    public boolean matchesToolId(String toolId) {
        return toolId != null && !toolId.isBlank() && toolId.equals(id);
    }

    public boolean toggleEnabled(ExternalToolToggleId toggleId) {
        return toggleId != null && Boolean.TRUE.equals(toggleStates.get(toggleId));
    }

    public Optional<ExternalToolAction> action(ExternalToolActionId actionId) {
        if (actionId == null) {
            return Optional.empty();
        }
        return supportedActions.stream().filter(action -> action.id() == actionId).findFirst();
    }

    public Set<ExternalToolToggleId> supportedToggleIds() {
        return supportedToggles.stream().map(ExternalToolToggle::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public static InventoryToolDescriptor fromLegacy(
            String providerId,
            ExternalToolSpec toolSpec,
            boolean live,
            ToolOpenCommand activationCommand,
            Map<ExternalToolToggleId, Boolean> toggleStates,
            Map<String, String> statePayload
    ) {
        List<ToolRegionDescriptor> regions = toolSpec == null
                ? List.of()
                : toolSpec.slotRegions().stream()
                .map(region -> new ToolRegionDescriptor(
                        region.id(),
                        region.role(),
                        region.menuSlots(),
                        region.columns()
                ))
                .toList();
        return new InventoryToolDescriptor(
                toolSpec == null ? "" : toolSpec.id(),
                providerId,
                toolSpec == null ? null : toolSpec.kind(),
                regions,
                toolSpec == null ? List.of() : toolSpec.actions(),
                toolSpec == null ? List.of() : toolSpec.toggles(),
                live,
                activationCommand,
                toggleStates,
                statePayload,
                toolSpec
        );
    }
}
