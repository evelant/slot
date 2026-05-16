package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemIdentityMatcher {
    private static final Set<String> STABLE_IDENTITY_TOKENS = Set.of(
            "amulet", "armor", "artifact", "axe", "backpack", "bauble", "battleaxe", "blade",
            "boots", "bow", "bracelet", "bundle", "cannon", "canteen", "case", "charm", "chestplate", "claymore",
            "clock", "compass", "crossbow", "dagger", "drill", "elytra", "excavator",
            "fishing_rod", "flask", "flint_and_steel", "gadget", "gauntlet", "glaive", "greatsword", "hammer",
            "hatchet", "helmet", "hoe", "hook", "knife", "lance", "leggings", "mace", "machete",
            "mattock", "necklace", "offhand", "paxel", "pickaxe", "pouch", "quiver", "relic",
            "ring", "rod", "satchel", "saw", "scanner", "scepter", "sceptre", "scope", "shears", "shield", "shovel",
            "shulker", "sickle", "spade", "spear", "staff", "sword", "talisman", "tool",
            "totem", "trident", "wand", "water_skin", "waterskin", "weapon", "wrench", "zweihander"
    );
    private static final Set<String> STABLE_IDENTITY_SUFFIX_EXCLUSIONS = Set.of("case", "shulker");
    private static final Map<String, Boolean> STABLE_IDENTITY_CACHE = new ConcurrentHashMap<>();

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

    public static ItemIdentity itemOnly(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("stack must not be empty");
        }
        return ItemIdentity.of(resolveItemId(stack));
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
        return STABLE_IDENTITY_CACHE.computeIfAbsent(itemId, ItemIdentityMatcher::computeStableMovableIdentity);
    }

    private static boolean computeStableMovableIdentity(String itemId) {
        int separatorIndex = itemId.indexOf(':');
        String path = separatorIndex >= 0 ? itemId.substring(separatorIndex + 1) : itemId;
        String normalizedPath = boundaryTokenPath(path);
        for (String token : STABLE_IDENTITY_TOKENS) {
            if (normalizedPath.contains("_" + token + "_")) {
                return true;
            }
        }
        String compactPath = compactToken(path);
        for (String token : STABLE_IDENTITY_TOKENS) {
            if (STABLE_IDENTITY_SUFFIX_EXCLUSIONS.contains(token)) {
                continue;
            }
            String compactToken = compactToken(token);
            if (!compactToken.isBlank() && compactPath.endsWith(compactToken)) {
                return true;
            }
        }
        return false;
    }

    private static String boundaryTokenPath(String path) {
        String resolved = path == null ? "" : path;
        StringBuilder builder = new StringBuilder(resolved.length() + 2);
        builder.append('_');
        boolean previousSeparator = true;
        for (int index = 0; index < resolved.length(); index++) {
            char character = Character.toLowerCase(resolved.charAt(index));
            if (isAsciiAlphanumeric(character)) {
                builder.append(character);
                previousSeparator = false;
            } else if (!previousSeparator) {
                builder.append('_');
                previousSeparator = true;
            }
        }
        if (!previousSeparator) {
            builder.append('_');
        }
        return builder.toString();
    }

    private static String compactToken(String input) {
        String resolved = input == null ? "" : input;
        StringBuilder builder = new StringBuilder(resolved.length());
        for (int index = 0; index < resolved.length(); index++) {
            char character = Character.toLowerCase(resolved.charAt(index));
            if (isAsciiAlphanumeric(character)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private static boolean isAsciiAlphanumeric(char character) {
        return (character >= 'a' && character <= 'z') || (character >= '0' && character <= '9');
    }
}
