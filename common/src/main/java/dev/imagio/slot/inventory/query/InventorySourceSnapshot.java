package dev.imagio.slot.inventory.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record InventorySourceSnapshot(
        String sourceId,
        int slotCapacity,
        List<InventoryEntrySnapshot> entries,
        String diagnostics
) {
    public InventorySourceSnapshot {
        sourceId = sourceId == null ? "" : sourceId;
        slotCapacity = Math.max(0, slotCapacity);
        entries = entries == null ? List.of() : List.copyOf(entries);
        diagnostics = diagnostics == null ? "" : diagnostics;
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("source snapshot id must not be blank");
        }
    }

    public static InventorySourceSnapshot empty(String sourceId) {
        return new InventorySourceSnapshot(sourceId, 0, List.of(), "");
    }

    public InventoryEntrySnapshot slotEntry(int slotIndex) {
        return entries.stream()
                .filter(entry -> entry != null && entry.slotBacked() && entry.slotIndex() == slotIndex)
                .findFirst()
                .orElse(null);
    }

    public InventoryEntrySnapshot providerEntry(String entryId) {
        if (entryId == null || entryId.isBlank()) {
            return null;
        }
        return entries.stream()
                .filter(entry -> entry != null && entry.entryKey().providerEntry() && entryId.equals(entry.entryId()))
                .findFirst()
                .orElse(null);
    }

    public Map<String, InventoryEntrySnapshot> entriesByStableKey() {
        LinkedHashMap<String, InventoryEntrySnapshot> entriesByStableKey = new LinkedHashMap<>();
        for (InventoryEntrySnapshot entry : entries) {
            if (entry != null) {
                entriesByStableKey.put(entry.entryKey().stableKey(), entry);
            }
        }
        return Map.copyOf(entriesByStableKey);
    }
}
