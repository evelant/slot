package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Set;

public final class ItemIdentityMatcher {
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

    private ItemIdentityMatcher() {
    }

    public static ItemIdentity create(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("stack must not be empty");
        }
        String itemId = resolveItemId(stack);
        String components = resolveComponentFingerprint(stack);
        if (!stackable(stack) || !components.isBlank()) {
            return ItemIdentity.exact(itemId, components);
        }
        return ItemIdentity.of(itemId);
    }

    private static String resolveItemId(ItemStack stack) {
        return SlotStackAccess.current().itemId(stack);
    }

    private static boolean stackable(ItemStack stack) {
        return SlotStackAccess.current().stackable(stack);
    }

    private static String resolveComponentFingerprint(ItemStack stack) {
        return SlotStackAccess.current().dataFingerprint(stack);
    }

    public static ItemIdentity normalizeMovable(ItemIdentity identity) {
        if (identity == null || identity.comparisonMode() != ItemComparisonMode.ITEM_ID_AND_COMPONENTS) {
            return identity;
        }
        return usesStableMovableIdentity(identity.itemId()) ? ItemIdentity.of(identity.itemId()) : identity;
    }

    public static boolean matchesMovable(ItemIdentity left, ItemIdentity right) {
        if (left == null || right == null) {
            return false;
        }
        return normalizeMovable(left).equals(normalizeMovable(right));
    }

    public static boolean matchesMovable(ItemStack stack, ItemIdentity identity) {
        if (stack == null || stack.isEmpty() || identity == null) {
            return false;
        }
        return matchesMovable(create(stack), identity);
    }

    public static boolean usesStableMovableIdentity(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        int separatorIndex = itemId.indexOf(':');
        String path = separatorIndex >= 0 ? itemId.substring(separatorIndex + 1) : itemId;
        String normalizedPath = "_" + path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_") + "_";
        for (String token : STABLE_IDENTITY_TOKENS) {
            if (normalizedPath.contains("_" + token + "_")) {
                return true;
            }
        }
        return false;
    }
}
