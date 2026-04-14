package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record InventoryAuthoritySnapshot(
        InventoryHostDescriptor host,
        Map<String, InventorySourceSnapshot> sourcesById,
        CursorStateSnapshot cursorState
) {
    public InventoryAuthoritySnapshot {
        sourcesById = sourcesById == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(sourcesById));
        cursorState = cursorState == null ? CursorStateSnapshot.empty() : cursorState;
    }

    public static InventoryAuthoritySnapshot empty() {
        return new InventoryAuthoritySnapshot(null, Map.of(), CursorStateSnapshot.empty());
    }

    public InventorySourceDescriptor source(String sourceId) {
        return host == null ? null : host.source(sourceId);
    }

    public List<InventorySourceDescriptor> sourceDescriptors() {
        return host == null ? List.of() : host.sourceDescriptors();
    }

    public List<InventorySourceDescriptor> sourcesInPane(InventoryPaneMembership paneMembership) {
        return host == null ? List.of() : InventoryDomainQueryService.sourcesInPane(host, paneMembership);
    }

    public List<InventorySourceDescriptor> carriedSources() {
        return sourcesInPane(InventoryPaneMembership.CARRIED);
    }

    public InventorySourceSnapshot sourceSnapshot(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return sourcesById.get(sourceId);
    }

    public List<InventoryEntrySnapshot> entries(String sourceId) {
        InventorySourceSnapshot sourceSnapshot = sourceSnapshot(sourceId);
        return sourceSnapshot == null ? List.of() : sourceSnapshot.entries();
    }

    public int slotCapacity(String sourceId) {
        InventorySourceSnapshot sourceSnapshot = sourceSnapshot(sourceId);
        return sourceSnapshot == null ? 0 : sourceSnapshot.slotCapacity();
    }

    public InventoryEntrySnapshot slotEntry(String sourceId, int slotIndex) {
        InventorySourceSnapshot sourceSnapshot = sourceSnapshot(sourceId);
        return sourceSnapshot == null ? null : sourceSnapshot.slotEntry(slotIndex);
    }

    public InventoryEntrySnapshot providerEntry(String sourceId, String entryId) {
        InventorySourceSnapshot sourceSnapshot = sourceSnapshot(sourceId);
        return sourceSnapshot == null ? null : sourceSnapshot.providerEntry(entryId);
    }
}
