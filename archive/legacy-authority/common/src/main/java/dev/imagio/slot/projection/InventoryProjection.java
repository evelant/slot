package dev.imagio.slot.projection;

import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InventoryProjection(
        Map<InventoryPane, PaneProjection> panes,
        Map<String, Integer> combinedSectionCounts
) {
    public InventoryProjection {
        panes = immutablePaneMap(panes);
        combinedSectionCounts = Map.copyOf(combinedSectionCounts == null ? Map.of() : combinedSectionCounts);
    }

    public static InventoryProjection empty() {
        EnumMap<InventoryPane, PaneProjection> panes = new EnumMap<>(InventoryPane.class);
        for (InventoryPane pane : InventoryPane.values()) {
            panes.put(pane, PaneProjection.empty(pane));
        }
        return new InventoryProjection(Collections.unmodifiableMap(panes), Map.of());
    }

    public PaneProjection pane(InventoryPane pane) {
        if (pane == null) {
            return PaneProjection.empty(InventoryPane.CARRIED);
        }
        return panes.getOrDefault(pane, PaneProjection.empty(pane));
    }

    private static Map<InventoryPane, PaneProjection> immutablePaneMap(Map<InventoryPane, PaneProjection> panes) {
        EnumMap<InventoryPane, PaneProjection> safePanes = new EnumMap<>(InventoryPane.class);
        if (panes != null) {
            safePanes.putAll(panes);
        }
        for (InventoryPane pane : InventoryPane.values()) {
            safePanes.putIfAbsent(pane, PaneProjection.empty(pane));
        }
        return Collections.unmodifiableMap(safePanes);
    }

    public record PaneProjection(
            InventoryPane pane,
            List<RowProjection> rows,
            Map<String, List<InventoryViewData.EntryView>> visibleEntriesBySection,
            Map<String, Integer> sectionCounts,
            int visibleEntryCount
    ) {
        public PaneProjection {
            pane = Objects.requireNonNull(pane, "pane");
            rows = List.copyOf(rows == null ? List.of() : rows);
            visibleEntriesBySection = immutableEntryMap(visibleEntriesBySection);
            sectionCounts = Map.copyOf(sectionCounts == null ? Map.of() : sectionCounts);
            if (visibleEntryCount < 0) {
                throw new IllegalArgumentException("visibleEntryCount must not be negative");
            }
        }

        public static PaneProjection empty(InventoryPane pane) {
            return new PaneProjection(pane, List.of(), Map.of(), Map.of(), 0);
        }

        private static Map<String, List<InventoryViewData.EntryView>> immutableEntryMap(
                Map<String, List<InventoryViewData.EntryView>> entriesBySection
        ) {
            if (entriesBySection == null || entriesBySection.isEmpty()) {
                return Map.of();
            }

            LinkedHashMap<String, List<InventoryViewData.EntryView>> safeEntries = new LinkedHashMap<>();
            for (Map.Entry<String, List<InventoryViewData.EntryView>> entry : entriesBySection.entrySet()) {
                safeEntries.put(entry.getKey(), List.copyOf(entry.getValue() == null ? List.of() : entry.getValue()));
            }
            return Collections.unmodifiableMap(safeEntries);
        }
    }

    public sealed interface RowProjection
            permits SectionRowProjection, LoadoutRowProjection, LoadoutPreviewRowProjection, ItemRowProjection {
        String rowId();

        InventoryPane pane();

        InventoryViewData.Section section();
    }

    public record SectionRowProjection(
            String rowId,
            InventoryPane pane,
            InventoryViewData.Section section,
            int count
    ) implements RowProjection {
        public SectionRowProjection {
            rowId = requireRowId(rowId);
            pane = Objects.requireNonNull(pane, "pane");
            section = Objects.requireNonNull(section, "section");
            if (count < 0) {
                throw new IllegalArgumentException("count must not be negative");
            }
        }
    }

    public record LoadoutRowProjection(
            String rowId,
            InventoryPane pane,
            InventoryViewData.Section section
    ) implements RowProjection {
        public LoadoutRowProjection {
            rowId = requireRowId(rowId);
            pane = Objects.requireNonNull(pane, "pane");
            section = Objects.requireNonNull(section, "section");
        }
    }

    public record LoadoutPreviewRowProjection(
            String rowId,
            InventoryPane pane,
            InventoryViewData.Section section
    ) implements RowProjection {
        public LoadoutPreviewRowProjection {
            rowId = requireRowId(rowId);
            pane = Objects.requireNonNull(pane, "pane");
            section = Objects.requireNonNull(section, "section");
        }
    }

    public record ItemRowProjection(
            String rowId,
            InventoryPane pane,
            InventoryViewData.Section section,
            InventoryViewData.EntryView entry
    ) implements RowProjection {
        public ItemRowProjection {
            rowId = requireRowId(rowId);
            pane = Objects.requireNonNull(pane, "pane");
            section = Objects.requireNonNull(section, "section");
            entry = Objects.requireNonNull(entry, "entry");
        }
    }

    private static String requireRowId(String rowId) {
        if (rowId == null || rowId.isBlank()) {
            throw new IllegalArgumentException("rowId must not be blank");
        }
        return rowId;
    }
}
