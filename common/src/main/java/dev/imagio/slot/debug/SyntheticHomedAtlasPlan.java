package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;

import java.util.List;
import java.util.Map;

public record SyntheticHomedAtlasPlan(
        List<VisualAtlasIsland> islands,
        Map<ItemIdentity, VisualHomeAssignment> assignments
) {
    public SyntheticHomedAtlasPlan {
        islands = islands == null ? List.of() : List.copyOf(islands);
        assignments = assignments == null ? Map.of() : Map.copyOf(assignments);
    }
}
