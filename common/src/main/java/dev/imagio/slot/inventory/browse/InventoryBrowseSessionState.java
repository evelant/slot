package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;

import java.util.LinkedHashSet;
import java.util.Set;

public record InventoryBrowseSessionState(
        InventoryBrowseFilter filter,
        InventoryBrowseSortMode sortMode,
        InventoryBrowseGroupingMode groupingMode,
        InventoryBrowsePaneMode paneMode,
        InventoryPaneMembership activePane,
        String selectedCollectionId,
        String selectedLoadoutId,
        String pinnedToolId,
        InventoryActionScope bulkActionScope,
        InventoryBrowseSubjectRef selectedSubject,
        Set<String> expandedSectionIds
) {
    public InventoryBrowseSessionState {
        filter = filter == null ? InventoryBrowseFilter.empty() : filter;
        sortMode = sortMode == null ? InventoryBrowseSortMode.NAME : sortMode;
        groupingMode = groupingMode == null ? InventoryBrowseGroupingMode.FLAT : groupingMode;
        paneMode = paneMode == null ? InventoryBrowsePaneMode.CARRIED_ONLY : paneMode;
        activePane = activePane == null ? InventoryPaneMembership.CARRIED : activePane;
        selectedCollectionId = selectedCollectionId == null ? "" : selectedCollectionId;
        selectedLoadoutId = selectedLoadoutId == null ? "" : selectedLoadoutId;
        pinnedToolId = pinnedToolId == null ? "" : pinnedToolId;
        bulkActionScope = bulkActionScope == null ? InventoryActionScope.VISIBLE_MATCHES : bulkActionScope;
        selectedSubject = selectedSubject == null ? null : InventoryBrowseSubjectRef.parse(selectedSubject.stableKey());
        expandedSectionIds = expandedSectionIds == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(expandedSectionIds));
    }

    public static InventoryBrowseSessionState defaults(InventoryBrowsePreferences preferences) {
        InventoryBrowsePreferences resolved = preferences == null ? InventoryBrowsePreferences.defaults() : preferences;
        return new InventoryBrowseSessionState(
                InventoryBrowseFilter.empty(),
                resolved.defaultSortMode(),
                resolved.defaultGroupingMode(),
                resolved.defaultPaneMode(),
                InventoryPaneMembership.CARRIED,
                "",
                "",
                "",
                resolved.defaultBulkActionScope(),
                null,
                Set.of()
        );
    }
}
