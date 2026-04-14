package dev.imagio.slot.session;

import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;

import java.util.List;

public record ToolRegionDescriptor(
        String id,
        ExternalToolSlotRole role,
        List<Integer> logicalSlots,
        int columns
) {
    public ToolRegionDescriptor {
        id = id == null ? "" : id;
        role = role == null ? ExternalToolSlotRole.INPUT : role;
        logicalSlots = logicalSlots == null ? List.of() : List.copyOf(logicalSlots);
        columns = Math.max(0, columns);
    }
}
