package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.WithinIslandOrdering;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceItemTargets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Wall sections intentionally ignore stored home ordinals for in-section
 * presentation. Home assignment decides which section an item belongs to;
 * facet-backed ordering decides where it reads inside that section.
 */
public final class WallSectionItemSorter {

    private WallSectionItemSorter() {
    }

    public record Groups(
            List<SlotWorkspaceViewModel.AtlasItem> carried,
            List<SlotWorkspaceViewModel.AtlasItem> ghosts
    ) {
        public Groups {
            carried = carried == null ? List.of() : List.copyOf(carried);
            ghosts = ghosts == null ? List.of() : List.copyOf(ghosts);
        }

        public boolean isEmpty() {
            return carried.isEmpty() && ghosts.isEmpty();
        }

        public int size() {
            return carried.size() + ghosts.size();
        }
    }

    public static Groups groupAndSort(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return new Groups(List.of(), List.of());
        }
        FacetIndex index = FacetIndexHolder.get();
        ArrayList<SortEntry> carried = new ArrayList<>();
        ArrayList<SortEntry> ghosts = new ArrayList<>();
        int ordinal = 0;
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null) {
                continue;
            }
            SortEntry entry = new SortEntry(item, descriptor(index, item), ordinal++);
            if (item.carried()) {
                carried.add(entry);
            } else {
                ghosts.add(entry);
            }
        }
        carried.sort(ENTRY_COMPARATOR);
        ghosts.sort(GHOST_COMPARATOR);
        return new Groups(itemsOf(carried), itemsOf(ghosts));
    }

    private static List<SlotWorkspaceViewModel.AtlasItem> itemsOf(List<SortEntry> entries) {
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>(entries.size());
        for (SortEntry entry : entries) {
            items.add(entry.item());
        }
        return List.copyOf(items);
    }

    private static final Comparator<SortEntry> ENTRY_COMPARATOR = Comparator
            .comparingInt((SortEntry entry) -> WithinIslandOrdering.carryRank(entry.descriptor()))
            .thenComparing(entry -> WithinIslandOrdering.clusterKey(entry.itemId(), entry.descriptor()))
            .thenComparingInt(entry -> WithinIslandOrdering.canonicalDyeIndex(
                    entry.descriptor() == null ? null : entry.descriptor().dyeColor()))
            .thenComparing(entry -> entry.item().name().toLowerCase(Locale.ROOT))
            .thenComparing(SortEntry::itemId)
            .thenComparingInt(entry -> entry.item().firstSlotIndex())
            .thenComparingInt(SortEntry::ordinal);

    private static final Comparator<SortEntry> GHOST_COMPARATOR = Comparator
            .comparingInt(WallSectionItemSorter::ghostIntentRank)
            .thenComparing(ENTRY_COMPARATOR);

    private static int ghostIntentRank(SortEntry entry) {
        if (entry == null || entry.item() == null) {
            return 1;
        }
        WorkspaceItemTargets targets = WorkspaceItemTargets.from(entry.item());
        int carriedCount = entry.item().carried() ? entry.item().totalCount() : 0;
        return targets.hasAnyGap(carriedCount) || entry.item().kitNeeded() ? 0 : 1;
    }

    private static IslandSignalDescriptor descriptor(
            FacetIndex index,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        ItemIdentity identity = item.identity() == null ? null : item.identity().toIdentity();
        if (identity == null) {
            return null;
        }
        String itemId = identity.itemId();
        if (index == null) {
            return IslandSignalDescriptor.empty(identity);
        }
        return new IslandSignalDescriptor(
                identity,
                Set.of(),
                Set.of(),
                namespaceOf(itemId),
                "",
                index.role(itemId).orElse(null),
                index.roleAlternatives(itemId),
                index.materialFamily(itemId).orElse(null),
                index.subsystems(itemId),
                index.organizationGroups(itemId),
                index.activities(itemId),
                index.flavor(itemId).orElse(null),
                index.carryFrequency(itemId).orElse(null),
                index.rarity(itemId).orElse(null),
                index.origin(itemId).orElse(null),
                index.dyeColor(itemId).orElse(null),
                index.palette(itemId),
                index.form(itemId).orElse(null),
                index.emitsLight(itemId)
        );
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }

    private record SortEntry(
            SlotWorkspaceViewModel.AtlasItem item,
            IslandSignalDescriptor descriptor,
            int ordinal
    ) {
        String itemId() {
            return item.identity() == null ? "" : item.identity().itemId();
        }
    }
}
