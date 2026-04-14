package dev.imagio.slot.client.policy;

import java.util.Set;

final class ItemHeuristics {
    private static final Set<String> AUTO_JUNK_VANILLA_PATHS = Set.of(
            "andesite", "cobbled_deepslate", "cobblestone", "coarse_dirt", "deepslate", "diorite",
            "dirt", "fern", "granite", "gravel", "kelp", "large_fern", "netherrack", "rooted_dirt",
            "short_grass", "stone", "stone_bricks", "tall_grass", "tuff", "vine",
            "beetroot_seeds", "melon_seeds", "pumpkin_seeds", "wheat_seeds"
    );
    private static final Set<String> PORTABLE_CONTAINER_TOKENS = Set.of(
            "backpack", "bundle", "case", "pouch", "satchel", "shulker"
    );

    private ItemHeuristics() {
    }

    static boolean hasPortableContainerFallbackToken(String itemId) {
        return hasAnyToken(path(itemId), PORTABLE_CONTAINER_TOKENS);
    }

    static boolean isConservativeVanillaAutoJunkItemId(String itemId) {
        return "minecraft".equals(namespace(itemId)) && AUTO_JUNK_VANILLA_PATHS.contains(path(itemId));
    }

    private static String path(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return "";
        }
        int separatorIndex = itemId.indexOf(':');
        return separatorIndex >= 0 ? itemId.substring(separatorIndex + 1) : itemId;
    }

    private static String namespace(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return "minecraft";
        }
        int separatorIndex = itemId.indexOf(':');
        return separatorIndex >= 0 ? itemId.substring(0, separatorIndex) : "minecraft";
    }

    private static boolean hasAnyToken(String path, Set<String> tokens) {
        for (String token : tokens) {
            if (hasToken(path, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasToken(String path, String token) {
        String normalizedPath = "_" + path + "_";
        String normalizedToken = "_" + token + "_";
        return normalizedPath.contains(normalizedToken);
    }
}
