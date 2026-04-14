package dev.imagio.slot.inventory.browse;

import java.util.LinkedHashMap;
import java.util.Map;

public record InventoryCategoryOverrides(
        Map<String, ItemCategory> itemOverrides,
        Map<String, ItemCategory> namespaceOverrides
) {
    public InventoryCategoryOverrides {
        itemOverrides = itemOverrides == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(itemOverrides));
        namespaceOverrides = namespaceOverrides == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(namespaceOverrides));
    }

    public static InventoryCategoryOverrides empty() {
        return new InventoryCategoryOverrides(Map.of(), Map.of());
    }
}
