package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class InventoryWorkingSetProjectionService {
    private InventoryWorkingSetProjectionService() {
    }

    public static InventoryWorkingSetProjection project(
            InventoryAuthoritySnapshot authority,
            InventoryPaneMembership paneMembership,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (authority == null || authority.host() == null || paneMembership == null || identityResolver == null) {
            return InventoryWorkingSetProjection.empty(paneMembership);
        }

        LinkedHashMap<ItemIdentity, RowAccumulator> rowsByIdentity = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : InventoryDomainQueryService.sourcesInPane(authority.host(), paneMembership)) {
            if (source == null) {
                continue;
            }
            List<InventoryEntrySnapshot> entries = authority.entries(source.id()).stream()
                    .filter(Objects::nonNull)
                    .sorted((left, right) -> entryOrder(left.entryKey(), right.entryKey()))
                    .toList();
            for (InventoryEntrySnapshot entry : entries) {
                if (!entry.present()) {
                    continue;
                }
                ItemIdentity identity = identityResolver.apply(entry);
                if (identity == null) {
                    continue;
                }
                rowsByIdentity.computeIfAbsent(identity, ignored -> new RowAccumulator(identity, paneMembership))
                        .add(entry);
            }
        }

        List<ProjectedInventoryRow> rows = rowsByIdentity.values().stream()
                .map(RowAccumulator::toRow)
                .toList();
        return new InventoryWorkingSetProjection(authority, paneMembership, rows);
    }

    private static int entryOrder(InventoryEntryKey left, InventoryEntryKey right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        if (left.kind() != right.kind()) {
            return left.kind().compareTo(right.kind());
        }
        if (left.slotBacked()) {
            return Integer.compare(left.slotIndex(), right.slotIndex());
        }
        return left.entryId().compareTo(right.entryId());
    }

    private static final class RowAccumulator {
        private final ItemIdentity identity;
        private final InventoryPaneMembership paneMembership;
        private final ArrayList<ProjectedEntryRef> backingEntries = new ArrayList<>();
        private final LinkedHashSet<String> backingSources = new LinkedHashSet<>();
        private int totalCount;

        private RowAccumulator(ItemIdentity identity, InventoryPaneMembership paneMembership) {
            this.identity = identity;
            this.paneMembership = paneMembership;
        }

        private void add(InventoryEntrySnapshot entry) {
            backingEntries.add(new ProjectedEntryRef(entry.entryKey(), entry.sourceId(), entry.stack(), entry.count()));
            backingSources.add(entry.sourceId());
            totalCount += entry.count();
        }

        private ProjectedInventoryRow toRow() {
            return new ProjectedInventoryRow(
                    identity,
                    paneMembership,
                    totalCount,
                    List.copyOf(backingEntries),
                    List.copyOf(backingSources),
                    ""
            );
        }
    }
}
