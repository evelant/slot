package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.query.ProjectedInventoryRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InventoryBrowseDocumentQueries {
    private InventoryBrowseDocumentQueries() {
    }

    public static InventoryBrowsePane findPane(
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.PaneRef paneRef
    ) {
        if (document == null || paneRef == null) {
            return null;
        }
        return document.panes().stream()
                .filter(Objects::nonNull)
                .filter(pane -> paneRef.equals(pane.subjectRef()))
                .findFirst()
                .orElse(null);
    }

    public static InventoryBrowseSection findSection(
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.SectionRef sectionRef
    ) {
        if (document == null || sectionRef == null) {
            return null;
        }
        for (InventoryBrowsePane pane : document.panes()) {
            if (pane == null) {
                continue;
            }
            for (InventoryBrowseSection section : pane.sections()) {
                if (section != null && sectionRef.equals(section.subjectRef())) {
                    return section;
                }
            }
        }
        return null;
    }

    public static InventoryBrowseEntry.ItemEntry findItemEntry(
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.ItemRowRef subjectRef
    ) {
        if (document == null || subjectRef == null) {
            return null;
        }
        return itemEntries(document).stream()
                .filter(entry -> subjectRef.equals(entry.subjectRef()))
                .findFirst()
                .orElse(null);
    }

    public static InventoryBrowseEntry.PlaceholderEntry findPlaceholderEntry(
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.PlaceholderRef subjectRef
    ) {
        if (document == null || subjectRef == null) {
            return null;
        }
        for (InventoryBrowsePane pane : document.panes()) {
            if (pane == null) {
                continue;
            }
            for (InventoryBrowseSection section : pane.sections()) {
                if (section == null) {
                    continue;
                }
                for (InventoryBrowseEntry entry : section.entries()) {
                    if (entry instanceof InventoryBrowseEntry.PlaceholderEntry placeholderEntry
                            && subjectRef.equals(placeholderEntry.subjectRef())) {
                        return placeholderEntry;
                    }
                }
            }
        }
        return null;
    }

    public static InventoryBrowseEntry.LoadoutEntry findLoadoutEntry(
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef.LoadoutRef subjectRef
    ) {
        if (document == null || subjectRef == null) {
            return null;
        }
        for (InventoryBrowsePane pane : document.panes()) {
            if (pane == null) {
                continue;
            }
            for (InventoryBrowseSection section : pane.sections()) {
                if (section == null) {
                    continue;
                }
                for (InventoryBrowseEntry entry : section.entries()) {
                    if (entry instanceof InventoryBrowseEntry.LoadoutEntry loadoutEntry
                            && subjectRef.equals(loadoutEntry.subjectRef())) {
                        return loadoutEntry;
                    }
                }
            }
        }
        return null;
    }

    public static List<InventoryBrowseEntry.ItemEntry> itemEntries(InventoryBrowseDocument document) {
        if (document == null) {
            return List.of();
        }
        ArrayList<InventoryBrowseEntry.ItemEntry> entries = new ArrayList<>();
        for (InventoryBrowsePane pane : document.panes()) {
            if (pane == null) {
                continue;
            }
            entries.addAll(itemEntries(pane));
        }
        return List.copyOf(entries);
    }

    public static List<InventoryBrowseEntry.ItemEntry> itemEntries(InventoryBrowsePane pane) {
        if (pane == null) {
            return List.of();
        }
        ArrayList<InventoryBrowseEntry.ItemEntry> entries = new ArrayList<>();
        for (InventoryBrowseSection section : pane.sections()) {
            entries.addAll(itemEntries(section));
        }
        return List.copyOf(entries);
    }

    public static List<InventoryBrowseEntry.ItemEntry> itemEntries(InventoryBrowseSection section) {
        if (section == null) {
            return List.of();
        }
        ArrayList<InventoryBrowseEntry.ItemEntry> entries = new ArrayList<>();
        for (InventoryBrowseEntry entry : section.entries()) {
            if (entry instanceof InventoryBrowseEntry.ItemEntry itemEntry) {
                entries.add(itemEntry);
            }
        }
        return List.copyOf(entries);
    }

    public static List<ProjectedInventoryRow> projectedRows(List<InventoryBrowseEntry.ItemEntry> itemEntries) {
        if (itemEntries == null || itemEntries.isEmpty()) {
            return List.of();
        }
        return itemEntries.stream()
                .filter(Objects::nonNull)
                .map(InventoryBrowseEntry.ItemEntry::row)
                .filter(Objects::nonNull)
                .toList();
    }

    public static boolean containsSubject(
            InventoryBrowseDocument document,
            InventoryBrowseSubjectRef subjectRef
    ) {
        if (document == null || subjectRef == null) {
            return false;
        }
        if (subjectRef instanceof InventoryBrowseSubjectRef.PaneRef paneRef) {
            return findPane(document, paneRef) != null;
        }
        if (subjectRef instanceof InventoryBrowseSubjectRef.SectionRef sectionRef) {
            return findSection(document, sectionRef) != null;
        }
        if (subjectRef instanceof InventoryBrowseSubjectRef.ItemRowRef itemRowRef) {
            return findItemEntry(document, itemRowRef) != null;
        }
        if (subjectRef instanceof InventoryBrowseSubjectRef.PlaceholderRef placeholderRef) {
            return findPlaceholderEntry(document, placeholderRef) != null;
        }
        if (subjectRef instanceof InventoryBrowseSubjectRef.LoadoutRef loadoutRef) {
            return findLoadoutEntry(document, loadoutRef) != null;
        }
        return false;
    }
}
