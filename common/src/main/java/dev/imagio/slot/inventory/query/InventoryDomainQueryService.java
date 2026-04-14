package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import java.util.List;

public final class InventoryDomainQueryService {
    private InventoryDomainQueryService() {
    }

    public static List<InventorySourceDescriptor> sourcesInPane(
            InventoryHostDescriptor host,
            InventoryPaneMembership paneMembership
    ) {
        if (host == null || paneMembership == null) {
            return List.of();
        }
        return host.sourceDescriptors().stream()
                .filter(source -> source != null && source.paneMembership() == paneMembership)
                .sorted((left, right) -> Integer.compare(left.stableOrder(), right.stableOrder()))
                .toList();
    }

    public static InventorySourceSnapshot readSource(
            InventoryAuthoritySnapshot authority,
            String sourceId
    ) {
        if (authority == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return authority.sourceSnapshot(sourceId);
    }

    public static SourceCapacity sourceCapacity(
            InventoryAuthoritySnapshot authority,
            String sourceId
    ) {
        if (authority == null || authority.host() == null || sourceId == null || sourceId.isBlank()) {
            return SourceCapacity.empty();
        }
        InventorySourceDescriptor source = authority.host().source(sourceId);
        InventorySourceSnapshot snapshot = authority.sourceSnapshot(sourceId);
        if (source == null || snapshot == null) {
            return SourceCapacity.empty();
        }
        int occupiedSlots = (int) snapshot.entries().stream()
                .filter(entry -> entry != null && entry.slotBacked() && entry.present())
                .count();
        return new SourceCapacity(sourceId, snapshot.slotCapacity(), occupiedSlots);
    }

    public static PaneCapacity summarizePane(
            InventoryAuthoritySnapshot authority,
            InventoryPaneMembership paneMembership
    ) {
        if (authority == null || authority.host() == null) {
            return new PaneCapacity(paneMembership, 0, 0, 0);
        }
        List<InventorySourceDescriptor> sources = sourcesInPane(authority.host(), paneMembership);
        int totalSlots = 0;
        int occupiedSlots = 0;
        for (InventorySourceDescriptor source : sources) {
            SourceCapacity capacity = sourceCapacity(authority, source.id());
            totalSlots += capacity.totalSlots();
            occupiedSlots += capacity.occupiedSlots();
        }
        return new PaneCapacity(paneMembership, sources.size(), totalSlots, occupiedSlots);
    }

    public record SourceCapacity(String sourceId, int totalSlots, int occupiedSlots) {
        public SourceCapacity {
            sourceId = sourceId == null ? "" : sourceId;
            totalSlots = Math.max(0, totalSlots);
            occupiedSlots = Math.max(0, occupiedSlots);
        }

        public static SourceCapacity empty() {
            return new SourceCapacity("", 0, 0);
        }

        public int freeSlots() {
            return Math.max(0, totalSlots - occupiedSlots);
        }
    }

    public record PaneCapacity(
            InventoryPaneMembership paneMembership,
            int sourceCount,
            int totalSlots,
            int occupiedSlots
    ) {
        public PaneCapacity {
            paneMembership = paneMembership == null ? InventoryPaneMembership.HIDDEN : paneMembership;
            sourceCount = Math.max(0, sourceCount);
            totalSlots = Math.max(0, totalSlots);
            occupiedSlots = Math.max(0, occupiedSlots);
        }

        public int freeSlots() {
            return Math.max(0, totalSlots - occupiedSlots);
        }
    }
}
