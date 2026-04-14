package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryCommandAvailability;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InventoryBrowsePane(
        InventoryBrowseSubjectRef.PaneRef subjectRef,
        InventoryPaneMembership paneMembership,
        List<InventoryBrowseSection> sections,
        Map<InventoryCommandId, InventoryCommandAvailability> commands,
        String diagnostics
) {
    public InventoryBrowsePane {
        subjectRef = subjectRef == null ? new InventoryBrowseSubjectRef.PaneRef(paneMembership) : subjectRef;
        paneMembership = paneMembership == null ? InventoryPaneMembership.HIDDEN : paneMembership;
        sections = sections == null ? List.of() : List.copyOf(sections.stream().filter(Objects::nonNull).toList());
        commands = commands == null ? Map.of() : Map.copyOf(commands);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
