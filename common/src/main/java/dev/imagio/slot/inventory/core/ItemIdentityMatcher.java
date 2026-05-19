package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class ItemIdentityMatcher {
    private ItemIdentityMatcher() {
    }

    public static ItemIdentity create(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("stack must not be empty");
        }
        String itemId = resolveItemId(stack);
        String components = resolveComponentFingerprint(stack);
        if (usesItemOnlyMovableIdentity(stack) || hasMovableConditionOnlyFingerprint(components)) {
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
        return SlotStackAccess.current().damageable(stack)
                || PortableContainerClassifiers.isPortableContainer(stack);
    }

    private static boolean hasMovableConditionOnlyFingerprint(String fingerprint) {
        String normalized = normalizeFingerprint(fingerprint);
        if (normalized.isBlank()) {
            return false;
        }
        normalized = stripOuter(normalized, '{', '}');
        normalized = stripOuter(normalized, '[', ']');
        if (normalized.isBlank() || hasTopLevelComma(normalized)) {
            return false;
        }
        String key = leadingFingerprintKey(normalized);
        return key.equals("damage")
                || key.equals("minecraft:damage")
                || key.equals("inventory")
                || key.equals("container")
                || key.equals("minecraft:container")
                || key.equals("minecraft:bundle_contents");
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

    private static boolean hasTopLevelComma(String value) {
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
                return true;
            }
        }
        return false;
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
