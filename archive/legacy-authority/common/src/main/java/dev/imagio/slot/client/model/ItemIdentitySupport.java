package dev.imagio.slot.client.model;

import java.util.Set;

public final class ItemIdentitySupport {
    private static final Set<String> STABLE_IDENTITY_TOKENS = Set.of(
            "amulet", "armor", "artifact", "axe", "backpack", "bauble", "battleaxe", "blade",
            "boots", "bow", "bracelet", "bundle", "cannon", "case", "charm", "chestplate", "claymore",
            "clock", "compass", "crossbow", "dagger", "drill", "elytra", "excavator",
            "fishing_rod", "flint_and_steel", "gauntlet", "glaive", "greatsword", "hammer",
            "hatchet", "helmet", "hoe", "knife", "lance", "leggings", "mace", "machete",
            "mattock", "necklace", "offhand", "paxel", "pickaxe", "pouch", "quiver", "relic",
            "ring", "rod", "satchel", "saw", "scanner", "scope", "shears", "shield", "shovel",
            "shulker", "sickle", "spade", "spear", "staff", "sword", "talisman", "tool",
            "totem", "trident", "wand", "weapon", "wrench", "zweihander"
    );

    private ItemIdentitySupport() {
    }

    public static ItemIdentity normalizeTrackedIdentity(ItemIdentity identity) {
        if (identity == null || identity.comparisonMode() != ComparisonMode.ITEM_ID_AND_COMPONENTS) {
            return identity;
        }
        return usesStableTrackedIdentity(identity.itemId()) ? ItemIdentity.of(identity.itemId()) : identity;
    }

    public static ItemIdentity normalizeQuickAccessIdentity(ItemIdentity identity) {
        return normalizeTrackedIdentity(identity);
    }

    public static ItemIdentity normalizeMovableIdentity(ItemIdentity identity) {
        if (identity == null || identity.comparisonMode() != ComparisonMode.ITEM_ID_AND_COMPONENTS) {
            return identity;
        }
        return usesStableMovableIdentity(identity.itemId()) ? ItemIdentity.of(identity.itemId()) : identity;
    }

    public static boolean matchesTrackedIdentity(ItemIdentity left, ItemIdentity right) {
        if (left == null || right == null) {
            return false;
        }
        return normalizeTrackedIdentity(left).equals(normalizeTrackedIdentity(right));
    }

    public static boolean matchesMovableIdentity(ItemIdentity left, ItemIdentity right) {
        if (left == null || right == null) {
            return false;
        }
        return normalizeMovableIdentity(left).equals(normalizeMovableIdentity(right));
    }

    public static boolean usesStableTrackedIdentity(String itemId) {
        return usesStableIdentity(itemId);
    }

    public static boolean usesStableMovableIdentity(String itemId) {
        return usesStableIdentity(itemId);
    }

    private static boolean usesStableIdentity(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        int separatorIndex = itemId.indexOf(':');
        String path = separatorIndex >= 0 ? itemId.substring(separatorIndex + 1) : itemId;
        String normalizedPath = "_" + path + "_";
        for (String token : STABLE_IDENTITY_TOKENS) {
            if (normalizedPath.contains("_" + token + "_")) {
                return true;
            }
        }
        return false;
    }
}
