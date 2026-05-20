package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ItemIdentityMatcher {
    private ItemIdentityMatcher() {
    }

    public static ItemIdentity create(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("stack must not be empty");
        }
        String itemId = resolveItemId(stack);
        String components = resolveComponentFingerprint(stack);
        if (usesItemOnlyMovableIdentity(stack, components) || hasMovableConditionOnlyFingerprint(components)) {
            return ItemIdentity.of(itemId);
        }
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
        return hasMovableConditionOnlyFingerprint(identity.componentFingerprint())
                ? ItemIdentity.of(identity.itemId())
                : identity;
    }

    public static boolean matchesMovable(ItemIdentity left, ItemIdentity right) {
        if (left == null || right == null) {
            return false;
        }
        ItemIdentity normalizedLeft = normalizeMovable(left);
        ItemIdentity normalizedRight = normalizeMovable(right);
        if (!Objects.equals(normalizedLeft.itemId(), normalizedRight.itemId())) {
            return false;
        }
        if (normalizedLeft.comparisonMode() == ItemComparisonMode.ITEM_ID
                || normalizedRight.comparisonMode() == ItemComparisonMode.ITEM_ID) {
            return true;
        }
        return Objects.equals(normalizedLeft.componentFingerprint(), normalizedRight.componentFingerprint());
    }

    public static boolean matchesMovable(ItemStack stack, ItemIdentity identity) {
        if (stack == null || stack.isEmpty() || identity == null) {
            return false;
        }
        return matchesMovable(create(stack), identity);
    }

    public static boolean usesItemOnlyMovableIdentity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return usesItemOnlyMovableIdentity(stack, resolveComponentFingerprint(stack));
    }

    private static boolean usesItemOnlyMovableIdentity(ItemStack stack, String components) {
        return SlotStackAccess.current().damageable(stack)
                || hasMovableToolTag(stack)
                || hasToolStateFingerprint(components)
                || PortableContainerClassifiers.isPortableContainer(stack);
    }

    private static boolean hasMovableConditionOnlyFingerprint(String fingerprint) {
        String normalized = normalizeFingerprint(fingerprint);
        if (normalized.isBlank()) {
            return false;
        }
        normalized = stripOuter(normalized, '{', '}');
        normalized = stripOuter(normalized, '[', ']');
        if (normalized.isBlank()) {
            return false;
        }
        Set<String> keys = topLevelFingerprintKeys(normalized);
        if (keys.isEmpty()) {
            return false;
        }
        boolean hasMovableKey = false;
        for (String key : keys) {
            if (!isMovableConditionKey(key) && !isToolStateKey(key)) {
                return false;
            }
            hasMovableKey = true;
        }
        return hasMovableKey;
    }

    private static boolean hasMovableToolTag(ItemStack stack) {
        for (String tag : ItemStackTags.itemTagIds(stack)) {
            if (isToolTag(tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isToolTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }
        String normalized = tag.toLowerCase(java.util.Locale.ROOT).trim();
        int namespace = normalized.indexOf(':');
        String path = namespace >= 0 ? normalized.substring(namespace + 1) : normalized;
        if (path.equals("tools") || path.startsWith("tools/")) {
            return true;
        }
        return normalized.equals("minecraft:axes")
                || normalized.equals("minecraft:hoes")
                || normalized.equals("minecraft:pickaxes")
                || normalized.equals("minecraft:shovels")
                || normalized.equals("minecraft:swords")
                || normalized.equals("minecraft:tridents")
                || normalized.equals("minecraft:enchantable/durability")
                || normalized.equals("minecraft:enchantable/mining")
                || normalized.equals("minecraft:enchantable/sharp_weapon")
                || normalized.equals("minecraft:enchantable/weapon");
    }

    private static boolean hasToolStateFingerprint(String fingerprint) {
        String normalized = normalizeFingerprint(fingerprint);
        if (normalized.isBlank()) {
            return false;
        }
        normalized = stripOuter(normalized, '{', '}');
        normalized = stripOuter(normalized, '[', ']');
        for (String key : topLevelFingerprintKeys(normalized)) {
            if (isToolStateKey(key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMovableConditionKey(String key) {
        return key.equals("damage")
                || key.equals("minecraft:damage")
                || key.equals("maxdamage")
                || key.equals("max_damage")
                || key.equals("minecraft:max_damage")
                || key.equals("repaircost")
                || key.equals("repair_cost")
                || key.equals("minecraft:repair_cost")
                || key.equals("unbreakable")
                || key.equals("minecraft:unbreakable")
                || key.equals("hideflags")
                || key.equals("minecraft:hide_additional_tooltip")
                || key.equals("inventory")
                || key.equals("container")
                || key.equals("minecraft:container")
                || key.equals("minecraft:bundle_contents");
    }

    private static boolean isToolStateKey(String key) {
        return key.equals("tool")
                || key.equals("tooldata")
                || key.equals("tool_data")
                || key.equals("toolstats")
                || key.equals("tool_stats")
                || key.equals("gt.tool")
                || key.equals("minecraft:tool")
                || key.endsWith(":tool")
                || key.endsWith(":tool_data")
                || key.endsWith(":tooldata")
                || key.endsWith(":tool_stats")
                || key.endsWith(":toolstats");
    }

    private static String normalizeFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(fingerprint.length());
        for (int index = 0; index < fingerprint.length(); index++) {
            char c = Character.toLowerCase(fingerprint.charAt(index));
            if (!Character.isWhitespace(c) && c != '"') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String stripOuter(String value, char open, char close) {
        String resolved = value == null ? "" : value;
        while (resolved.length() >= 2 && resolved.charAt(0) == open
                && resolved.charAt(resolved.length() - 1) == close) {
            resolved = resolved.substring(1, resolved.length() - 1);
        }
        return resolved;
    }

    private static Set<String> topLevelFingerprintKeys(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        int segmentStart = 0;
        int curlyDepth = 0;
        int squareDepth = 0;
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (c == '{') {
                curlyDepth++;
            } else if (c == '}') {
                curlyDepth = Math.max(0, curlyDepth - 1);
            } else if (c == '[') {
                squareDepth++;
            } else if (c == ']') {
                squareDepth = Math.max(0, squareDepth - 1);
            } else if (c == ',' && curlyDepth == 0 && squareDepth == 0) {
                addFingerprintKey(keys, value.substring(segmentStart, index));
                segmentStart = index + 1;
            }
        }
        addFingerprintKey(keys, value.substring(segmentStart));
        return keys.isEmpty() ? Set.of() : Set.copyOf(keys);
    }

    private static void addFingerprintKey(Set<String> keys, String segment) {
        String key = leadingFingerprintKey(segment == null ? "" : segment.strip());
        if (!key.isBlank()) {
            keys.add(key);
        }
    }

    private static String leadingFingerprintKey(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        int arrow = value.indexOf("=>");
        if (arrow > 0) {
            return value.substring(0, arrow);
        }
        int equals = value.indexOf('=');
        if (equals > 0) {
            return value.substring(0, equals);
        }
        int colon = value.indexOf(':');
        if (colon <= 0) {
            return "";
        }
        if (!value.startsWith("minecraft:")) {
            return value.substring(0, colon);
        }
        int secondColon = value.indexOf(':', colon + 1);
        return secondColon > 0 ? value.substring(0, secondColon) : value.substring(0, colon);
    }
}
