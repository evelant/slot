package dev.imagio.slot.capability;

import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record MenuCapabilityDescriptor(
        Map<String, List<Integer>> logicalMenuSlotsBySource,
        Set<String> actionableSourceIds,
        List<ToolCapabilityDescriptor> tools
) {
    public MenuCapabilityDescriptor {
        logicalMenuSlotsBySource = logicalMenuSlotsBySource == null ? Map.of() : copyLogicalSlots(logicalMenuSlotsBySource);
        actionableSourceIds = actionableSourceIds == null ? Set.of() : Set.copyOf(actionableSourceIds);
        tools = tools == null ? List.of() : List.copyOf(tools);
    }

    public List<ToolCapabilityDescriptor> liveTools() {
        return tools.stream().filter(ToolCapabilityDescriptor::live).toList();
    }

    public List<ExternalToolSpec> liveToolSpecsOfKind(ExternalToolKind kind) {
        if (kind == null) {
            return List.of();
        }
        return liveTools().stream()
                .filter(tool -> tool.kind() == kind)
                .map(ToolCapabilityDescriptor::toolSpec)
                .filter(spec -> spec != null)
                .toList();
    }

    public ToolCapabilityDescriptor toolById(String toolId) {
        if (toolId == null || toolId.isBlank()) {
            return null;
        }
        return tools.stream()
                .filter(tool -> tool.matchesToolId(toolId))
                .findFirst()
                .orElse(null);
    }

    private static Map<String, List<Integer>> copyLogicalSlots(Map<String, List<Integer>> logicalMenuSlotsBySource) {
        LinkedHashMap<String, List<Integer>> copied = new LinkedHashMap<>();
        logicalMenuSlotsBySource.forEach((sourceId, slots) ->
                copied.put(sourceId == null ? "" : sourceId, slots == null ? List.of() : List.copyOf(slots))
        );
        return Map.copyOf(copied);
    }
}
