package dev.imagio.slot.inventory.core;

import java.util.LinkedHashMap;
import java.util.Map;

public record ToolActivationToken(
        String providerId,
        String toolId,
        Map<String, String> arguments
) {
    public ToolActivationToken {
        providerId = providerId == null ? "" : providerId;
        toolId = toolId == null ? "" : toolId;
        arguments = arguments == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(arguments));
    }

    public boolean present() {
        return !providerId.isBlank() && !toolId.isBlank();
    }
}
