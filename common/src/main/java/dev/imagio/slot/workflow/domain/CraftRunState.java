package dev.imagio.slot.workflow.domain;

import java.util.Comparator;
import java.util.List;

public record CraftRunState(
        int revision,
        String selectedEntryId,
        List<CraftRunRecipeEntry> entries
) {
    public CraftRunState {
        revision = Math.max(0, revision);
        selectedEntryId = selectedEntryId == null ? "" : selectedEntryId.trim();
        entries = entries == null
                ? List.of()
                : List.copyOf(entries.stream()
                        .filter(entry -> entry != null && entry.active())
                        .sorted(Comparator.comparingLong(CraftRunRecipeEntry::sequence))
                        .toList());
        if (!selectedEntryId.isBlank() && entry(selectedEntryId, entries) == null) {
            selectedEntryId = entries.isEmpty() ? "" : entries.get(entries.size() - 1).entryId();
        }
    }

    public static CraftRunState empty() {
        return new CraftRunState(0, "", List.of());
    }

    public boolean active() {
        return !entries.isEmpty();
    }

    public CraftRunRecipeEntry selectedEntry() {
        CraftRunRecipeEntry selected = entry(selectedEntryId, entries);
        return selected == null && !entries.isEmpty() ? entries.get(entries.size() - 1) : selected;
    }

    public CraftRunRecipeEntry entry(String entryId) {
        return entry(entryId, entries);
    }

    private static CraftRunRecipeEntry entry(String entryId, List<CraftRunRecipeEntry> entries) {
        if (entryId == null || entryId.isBlank() || entries == null || entries.isEmpty()) {
            return null;
        }
        for (CraftRunRecipeEntry entry : entries) {
            if (entry != null && entry.entryId().equals(entryId)) {
                return entry;
            }
        }
        return null;
    }
}
