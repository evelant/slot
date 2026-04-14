package dev.imagio.slot.workflow.domain;

import java.util.LinkedHashSet;
import java.util.Set;

public record QuickAccessLoadoutDefinition(
        String id,
        String name,
        Set<QuickAccessLoadoutEntry> entries
) {
    public QuickAccessLoadoutDefinition {
        id = id == null ? "" : id;
        name = name == null ? "" : name;
        entries = entries == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(entries));
    }
}
