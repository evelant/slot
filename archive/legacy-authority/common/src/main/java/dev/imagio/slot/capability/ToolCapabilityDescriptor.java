package dev.imagio.slot.capability;

import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.adapter.ExternalToolToggleId;
import dev.imagio.slot.session.ToolOpenCommand;

import java.util.Map;

public record ToolCapabilityDescriptor(
        String id,
        String providerId,
        ExternalToolKind kind,
        ExternalToolSpec toolSpec,
        boolean live,
        ToolOpenCommand activationCommand,
        Map<ExternalToolToggleId, Boolean> toggleStates,
        Map<String, String> statePayload
) {
    public ToolCapabilityDescriptor {
        id = id == null ? "" : id;
        providerId = providerId == null ? "" : providerId;
        kind = kind == null && toolSpec != null ? toolSpec.kind() : kind;
        activationCommand = activationCommand == null ? null : activationCommand;
        toggleStates = toggleStates == null ? Map.of() : Map.copyOf(toggleStates);
        statePayload = statePayload == null ? Map.of() : Map.copyOf(statePayload);
    }

    public boolean matchesToolId(String toolId) {
        return toolId != null && !toolId.isBlank() && toolId.equals(id);
    }

    public boolean toggleEnabled(ExternalToolToggleId toggleId) {
        return toggleId != null && Boolean.TRUE.equals(toggleStates.get(toggleId));
    }
}
