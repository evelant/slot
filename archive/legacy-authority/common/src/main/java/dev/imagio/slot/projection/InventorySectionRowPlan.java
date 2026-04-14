package dev.imagio.slot.projection;

import dev.imagio.slot.projection.InventoryViewData;

import java.util.ArrayList;
import java.util.List;

public final class InventorySectionRowPlan {
    private InventorySectionRowPlan() {
    }

    public static Plan plan(
            InventoryViewData.Section section,
            List<InventoryViewData.EntryView> entries,
            Options options
    ) {
        if (section == null || options == null) {
            return Plan.empty();
        }

        List<InventoryViewData.EntryView> safeEntries = entries == null ? List.of() : new ArrayList<>(entries);
        if (safeEntries.isEmpty() && !options.retainEmptySection()) {
            return Plan.empty();
        }

        List<Row> rows = new ArrayList<>();
        rows.add(Row.section(section));
        if (options.includeLoadoutRow()) {
            rows.add(Row.loadout(section));
        }
        if (options.includeLoadoutPreviewRow()) {
            rows.add(Row.loadoutPreview(section));
        }
        if (options.expanded()) {
            for (InventoryViewData.EntryView entry : safeEntries) {
                rows.add(Row.item(section, entry));
            }
        }

        return new Plan(true, List.copyOf(rows), safeEntries.size(), safeEntries.size());
    }

    public record Options(
            boolean retainEmptySection,
            boolean expanded,
            boolean includeLoadoutRow,
            boolean includeLoadoutPreviewRow
    ) {
    }

    public record Plan(
            boolean included,
            List<Row> rows,
            int sectionEntryCount,
            int visibleItemCount
    ) {
        private static Plan empty() {
            return new Plan(false, List.of(), 0, 0);
        }
    }

    public record Row(
            Kind kind,
            InventoryViewData.Section section,
            InventoryViewData.EntryView entry
    ) {
        private static Row section(InventoryViewData.Section section) {
            return new Row(Kind.SECTION, section, null);
        }

        private static Row loadout(InventoryViewData.Section section) {
            return new Row(Kind.LOADOUT, section, null);
        }

        private static Row loadoutPreview(InventoryViewData.Section section) {
            return new Row(Kind.LOADOUT_PREVIEW, section, null);
        }

        private static Row item(InventoryViewData.Section section, InventoryViewData.EntryView entry) {
            return new Row(Kind.ITEM, section, entry);
        }
    }

    public enum Kind {
        SECTION,
        LOADOUT,
        LOADOUT_PREVIEW,
        ITEM
    }
}
