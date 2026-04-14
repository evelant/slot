package dev.imagio.slot.session;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public record HostTopologyDescriptor(
        Map<String, List<Integer>> menuSlotsBySourceId,
        Map<Integer, String> sourceIdByMenuSlot,
        Map<String, List<Integer>> toolRegionSlots
) {
    public HostTopologyDescriptor {
        menuSlotsBySourceId = copyNestedLists(menuSlotsBySourceId);
        sourceIdByMenuSlot = sourceIdByMenuSlot == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(sourceIdByMenuSlot));
        toolRegionSlots = copyNestedLists(toolRegionSlots);
    }

    public static HostTopologyDescriptor empty() {
        return new HostTopologyDescriptor(Map.of(), Map.of(), Map.of());
    }

    public List<Integer> menuSlotsForSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return List.of();
        }
        return menuSlotsBySourceId.getOrDefault(sourceId, List.of());
    }

    public String sourceIdForMenuSlot(int menuSlot) {
        return sourceIdByMenuSlot.get(menuSlot);
    }

    public Integer resolveMenuSlot(String sourceId, int logicalSlotIndex) {
        List<Integer> menuSlots = menuSlotsForSource(sourceId);
        if (logicalSlotIndex < 0 || logicalSlotIndex >= menuSlots.size()) {
            return null;
        }
        return menuSlots.get(logicalSlotIndex);
    }

    public boolean sourceMenuBacked(String sourceId) {
        return !menuSlotsForSource(sourceId).isEmpty();
    }

    public List<Integer> menuSlotsForToolRegion(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            return List.of();
        }
        return toolRegionSlots.getOrDefault(regionId, List.of());
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
