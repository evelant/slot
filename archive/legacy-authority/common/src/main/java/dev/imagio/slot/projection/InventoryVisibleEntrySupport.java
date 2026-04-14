package dev.imagio.slot.projection;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntBiFunction;

public final class InventoryVisibleEntrySupport {
    private InventoryVisibleEntrySupport() {
    }

    public static List<InventoryViewData.EntryView> carriedSectionEntries(
            InventoryViewData.Section section,
            List<InventoryViewData.EntryView> entries,
            List<InventoryViewData.EntryView> recentEntries,
            String query,
            CollectionStore collectionStore,
            Comparator<InventoryViewData.EntryView> comparator
    ) {
        if (section.isRecent()) {
            return recentEntries == null ? List.of() : List.copyOf(recentEntries);
        }

        LinkedHashMap<ItemIdentity, InventoryViewData.EntryView> matchingEntries = new LinkedHashMap<>();
        for (InventoryViewData.EntryView entry : entries) {
            if (!matchesSectionAndQuery(section, entry, query)) {
                continue;
            }
            matchingEntries.putIfAbsent(entry.itemEntry().identity(), entry);
        }

        appendGhostCollectionEntries(matchingEntries, section, query, collectionStore);
        return sortedEntries(matchingEntries, comparator);
    }

    public static PaneSectionEntries paneSectionEntries(
            InventoryViewData.Section section,
            List<InventoryViewData.EntryView> entries,
            List<InventoryViewData.EntryView> recentCarriedEntries,
            String query,
            CollectionStore collectionStore,
            Comparator<InventoryViewData.EntryView> comparator,
            boolean includeOpenPane,
            ToIntBiFunction<InventoryViewData.EntryView, InventoryPane> localCount
    ) {
        LinkedHashMap<ItemIdentity, InventoryViewData.EntryView> openPaneEntries = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, InventoryViewData.EntryView> carriedPaneEntries = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, InventoryViewData.EntryView> combinedEntries = new LinkedHashMap<>();

        if (section.isRecent()) {
            for (InventoryViewData.EntryView entry : recentCarriedEntries == null ? List.<InventoryViewData.EntryView>of() : recentCarriedEntries) {
                carriedPaneEntries.putIfAbsent(entry.itemEntry().identity(), entry);
                combinedEntries.putIfAbsent(entry.itemEntry().identity(), entry);
            }
            return new PaneSectionEntries(
                    List.of(),
                    sortedEntries(carriedPaneEntries, comparator),
                    sortedEntries(combinedEntries, comparator)
            );
        }

        for (InventoryViewData.EntryView entry : entries) {
            if (!matchesSectionAndQuery(section, entry, query)) {
                continue;
            }

            int openCount = includeOpenPane ? Math.max(0, localCount.applyAsInt(entry, InventoryPane.OPEN_CONTAINER)) : 0;
            int carriedCount = Math.max(0, localCount.applyAsInt(entry, InventoryPane.CARRIED));
            if (openCount <= 0 && carriedCount <= 0) {
                continue;
            }

            if (carriedCount > 0) {
                carriedPaneEntries.putIfAbsent(entry.itemEntry().identity(), entry);
            }
            if (openCount > 0) {
                openPaneEntries.putIfAbsent(entry.itemEntry().identity(), entry);
            }
            combinedEntries.putIfAbsent(entry.itemEntry().identity(), entry);
        }

        appendGhostCollectionEntries(carriedPaneEntries, section, query, collectionStore);
        appendGhostCollectionEntries(combinedEntries, section, query, collectionStore);
        return new PaneSectionEntries(
                sortedEntries(openPaneEntries, comparator),
                sortedEntries(carriedPaneEntries, comparator),
                sortedEntries(combinedEntries, comparator)
        );
    }

    private static boolean matchesSectionAndQuery(
            InventoryViewData.Section section,
            InventoryViewData.EntryView entry,
            String query
    ) {
        return section.matches(entry.itemEntry()) && (query == null || query.isBlank() || entry.searchKey().contains(query));
    }

    private static void appendGhostCollectionEntries(
            Map<ItemIdentity, InventoryViewData.EntryView> entries,
            InventoryViewData.Section section,
            String query,
            CollectionStore collectionStore
    ) {
        if (!section.isCollection()) {
            return;
        }

        for (CollectionStore.CollectionItemTarget target : collectionStore.trackedItems(section.collectionId())) {
            boolean alreadyRepresented = entries.keySet().stream()
                    .anyMatch(identity -> ItemBehaviorPolicy.matchesTrackedIdentity(identity, target.identity()));
            if (alreadyRepresented) {
                continue;
            }
            InventoryViewData.EntryView ghostEntry = InventoryViewDataSupport.buildGhostCollectionEntryView(collectionStore, target.identity());
            if (ghostEntry == null) {
                continue;
            }
            if (query != null && !query.isBlank() && !ghostEntry.searchKey().contains(query)) {
                continue;
            }
            entries.put(target.identity(), ghostEntry);
        }
    }

    private static List<InventoryViewData.EntryView> sortedEntries(
            Map<ItemIdentity, InventoryViewData.EntryView> entries,
            Comparator<InventoryViewData.EntryView> comparator
    ) {
        return entries.values().stream()
                .sorted(comparator)
                .toList();
    }

    public record PaneSectionEntries(
            List<InventoryViewData.EntryView> openPaneEntries,
            List<InventoryViewData.EntryView> carriedPaneEntries,
            List<InventoryViewData.EntryView> combinedEntries
    ) {
    }
}
