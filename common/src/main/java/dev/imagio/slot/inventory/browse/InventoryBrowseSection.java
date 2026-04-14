package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryCommandAvailability;
import dev.imagio.slot.inventory.action.InventoryCommandId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InventoryBrowseSection(
        InventoryBrowseSubjectRef.SectionRef subjectRef,
        String id,
        String title,
        InventoryBrowseSectionKind kind,
        boolean expanded,
        List<InventoryBrowseEntry> entries,
        Map<InventoryCommandId, InventoryCommandAvailability> commands,
        String diagnostics
) {
    public InventoryBrowseSection {
        id = id == null ? "" : id;
        subjectRef = subjectRef == null ? new InventoryBrowseSubjectRef.SectionRef(null, id) : subjectRef;
        title = title == null ? "" : title;
        kind = kind == null ? InventoryBrowseSectionKind.ITEMS : kind;
        entries = entries == null ? List.of() : List.copyOf(entries.stream().filter(Objects::nonNull).toList());
        commands = commands == null ? Map.of() : Map.copyOf(commands);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }
}
