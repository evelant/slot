package dev.imagio.slot.inventory.core;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public record InventoryTopologyDescriptor(
        Map<String, List<Integer>> menuSlotsBySourceId,
        Map<Integer, String> sourceIdByMenuSlot,
        Map<String, List<Integer>> menuSlotsByToolRegionId
) {
    public InventoryTopologyDescriptor {
        menuSlotsBySourceId = copyNestedLists(menuSlotsBySourceId);
        sourceIdByMenuSlot = sourceIdByMenuSlot == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(sourceIdByMenuSlot));
        menuSlotsByToolRegionId = copyNestedLists(menuSlotsByToolRegionId);
    }

    public static InventoryTopologyDescriptor empty() {
        return new InventoryTopologyDescriptor(Map.of(), Map.of(), Map.of());
    }

    public List<Integer> menuSlotsForSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return List.of();
        }
        return menuSlotsBySourceId.getOrDefault(sourceId, List.of());
    }

    public Integer resolveMenuSlot(String sourceId, int logicalSlotIndex) {
        List<Integer> menuSlots = menuSlotsForSource(sourceId);
        if (logicalSlotIndex < 0 || logicalSlotIndex >= menuSlots.size()) {
            return null;
        }
        return menuSlots.get(logicalSlotIndex);
    }

    public String sourceIdForMenuSlot(int menuSlot) {
        return sourceIdByMenuSlot.get(menuSlot);
    }

    public boolean menuBacksSource(String sourceId) {
        return !menuSlotsForSource(sourceId).isEmpty();
    }

    public List<Integer> menuSlotsForToolRegion(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            return List.of();
        }
        return menuSlotsByToolRegionId.getOrDefault(regionId, List.of());
    }

    private static Map<String, List<Integer>> copyNestedLists(Map<String, List<Integer>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, List<Integer>> copied = new LinkedHashMap<>();
        source.forEach((key, value) -> copied.put(
                key == null ? "" : key,
                value == null ? List.of() : List.copyOf(new LinkedHashSet<>(value))
        ));
        return Map.copyOf(copied);
    }
}
