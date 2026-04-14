package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HeuristicInventoryCategoryResolver implements InventoryCategoryResolver {
    private final InventoryCategoryOverrides overrides;

    public HeuristicInventoryCategoryResolver(InventoryCategoryOverrides overrides) {
        this.overrides = overrides == null ? InventoryCategoryOverrides.empty() : overrides;
    }

    @Override
    public ItemCategory resolve(ItemIdentity identity) {
        if (identity == null || identity.itemId() == null || identity.itemId().isBlank()) {
            return ItemCategory.MISC;
        }
        String itemId = identity.itemId().toLowerCase(Locale.ROOT);
        Map<String, ItemCategory> itemOverrides = overrides.itemOverrides();
        if (itemOverrides.containsKey(itemId)) {
            return itemOverrides.get(itemId);
        }
        int separator = itemId.indexOf(':');
        String namespace = separator >= 0 ? itemId.substring(0, separator) : "";
        if (overrides.namespaceOverrides().containsKey(namespace)) {
            return overrides.namespaceOverrides().get(namespace);
        }
        String path = separator >= 0 ? itemId.substring(separator + 1) : itemId;
        if (containsAny(path, "chest", "barrel", "crate", "backpack", "bag", "pouch", "bundle", "tank", "drawer")) {
            return ItemCategory.STORAGE_TRANSPORT;
        }
        if (containsAny(path, "pickaxe", "axe", "shovel", "hoe", "hammer", "wrench", "shears", "flint_and_steel", "compass", "clock")) {
            return ItemCategory.TOOLS_UTILITY;
        }
        if (containsAny(path, "sword", "bow", "crossbow", "shield", "arrow", "trident", "mace")) {
            return ItemCategory.COMBAT;
        }
        if (containsAny(path, "helmet", "chestplate", "leggings", "boots", "elytra", "ring", "amulet")) {
            return ItemCategory.WEARABLES;
        }
        if (containsAny(path, "apple", "bread", "stew", "meat", "potion", "carrot", "beetroot", "cookie")) {
            return ItemCategory.CONSUMABLES;
        }
        if (containsAny(path, "seed", "sapling", "log", "leaf", "flower", "crop", "egg", "wheat", "dirt")) {
            return ItemCategory.NATURE_FARMING;
        }
        if (containsAny(path, "gear", "plate", "circuit", "wire", "processor", "component")) {
            return ItemCategory.COMPONENTS;
        }
        if (containsAny(path, "machine", "engine", "generator", "furnace", "crusher", "mixer", "press", "assembler")) {
            return ItemCategory.MACHINES_WORKSTATIONS;
        }
        if (containsAny(path, "plank", "brick", "stone", "cobble", "slab", "stairs", "wall", "glass")) {
            return ItemCategory.BUILDING;
        }
        if (containsAny(path, "lamp", "lantern", "banner", "painting", "pot", "carpet")) {
            return ItemCategory.DECORATION;
        }
        if (containsAny(path, "ingot", "nugget", "gem", "dust", "ore", "rod")) {
            return ItemCategory.MATERIALS;
        }
        return ItemCategory.MISC;
    }

    private static boolean containsAny(String path, String... tokens) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = "_" + path.toLowerCase(Locale.ROOT) + "_";
        for (String token : List.of(tokens)) {
            if (normalized.contains("_" + token.toLowerCase(Locale.ROOT) + "_")) {
                return true;
            }
        }
        return false;
    }
}
