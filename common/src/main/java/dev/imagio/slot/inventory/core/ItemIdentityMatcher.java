package dev.imagio.slot.inventory.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
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
        try {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        } catch (RuntimeException | LinkageError ignored) {
        }

        String reflectedItemId = invokeStringMethod(stack, "itemId");
        if (!reflectedItemId.isBlank()) {
            return reflectedItemId;
        }
        return stack.toString();
    }

    private static boolean stackable(ItemStack stack) {
        try {
            return stack.isStackable();
        } catch (RuntimeException | LinkageError ignored) {
        }
        try {
            return stack.getMaxStackSize() > 1;
        } catch (RuntimeException | LinkageError ignored) {
        }
        return true;
    }

    private static String resolveComponentFingerprint(ItemStack stack) {
        try {
            Object patch = stack.getComponentsPatch();
            if (patch == null) {
                return "";
            }
            try {
                Method isEmpty = patch.getClass().getMethod("isEmpty");
                Object empty = isEmpty.invoke(patch);
                if (empty instanceof Boolean booleanValue && booleanValue) {
                    return "";
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
            String fingerprint = patch.toString();
            return "{}".equals(fingerprint) ? "" : fingerprint;
        } catch (RuntimeException | LinkageError ignored) {
        }
        return invokeStringMethod(stack, "componentFingerprint");
    }

    private static String invokeStringMethod(ItemStack stack, String methodName) {
        if (stack == null || methodName == null || methodName.isBlank()) {
            return "";
        }
        try {
            Method method = stack.getClass().getMethod(methodName);
            Object value = method.invoke(stack);
            return value instanceof String string ? string : "";
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return "";
        }
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
        String normalizedPath = "_" + path + "_";
        for (String token : STABLE_IDENTITY_TOKENS) {
            if (normalizedPath.contains("_" + token + "_")) {
                return true;
            }
        }
        return false;
    }
}
