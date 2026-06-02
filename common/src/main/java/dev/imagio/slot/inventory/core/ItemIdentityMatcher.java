package dev.imagio.slot.inventory.core;

import dev.imagio.slot.platform.SlotStackAccess;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class ItemIdentityMatcher {
    private static final ThreadLocal<Memo> ACTIVE_MEMO = new ThreadLocal<>();

    private ItemIdentityMatcher() {
    }

    public static ItemIdentity create(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("stack must not be empty");
        }
        Memo memo = ACTIVE_MEMO.get();
        if (memo != null) {
            return memo.create(stack, () -> createUncached(stack));
        }
        return createUncached(stack);
    }

    private static ItemIdentity createUncached(ItemStack stack) {
        String itemId = resolveItemId(stack);
        if (usesItemOnlyMovableIdentityWithoutFingerprint(stack)) {
            return ItemIdentity.of(itemId);
        }
        String components = resolveComponentFingerprint(stack);
        String selectorFingerprint = stableSelectorFingerprint(itemId, components);
        if (!selectorFingerprint.isBlank()) {
            return ItemIdentity.exact(itemId, selectorFingerprint);
        }
        if (hasToolStateFingerprint(components) || hasMovableConditionOnlyFingerprint(components)) {
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
        Memo memo = ACTIVE_MEMO.get();
        if (memo != null) {
            return memo.normalize(identity, () -> normalizeMovableUncached(identity));
        }
        return normalizeMovableUncached(identity);
    }

    private static ItemIdentity normalizeMovableUncached(ItemIdentity identity) {
        String selectorFingerprint = stableSelectorFingerprint(identity.itemId(), identity.componentFingerprint());
        if (!selectorFingerprint.isBlank()) {
            return ItemIdentity.exact(identity.itemId(), selectorFingerprint);
        }
        return hasMovableConditionOnlyFingerprint(identity.componentFingerprint())
                ? ItemIdentity.of(identity.itemId())
                : identity;
    }

    public static <T> T withMemo(Memo memo, Supplier<T> supplier) {
        if (memo == null || supplier == null) {
            return supplier == null ? null : supplier.get();
        }
        Memo previous = ACTIVE_MEMO.get();
        ACTIVE_MEMO.set(memo);
        try {
            return supplier.get();
        } finally {
            if (previous == null) {
                ACTIVE_MEMO.remove();
            } else {
                ACTIVE_MEMO.set(previous);
            }
        }
    }

    public static final class Memo {
        private static final int MAX_ENTRIES = 4096;

        private final Map<CreateKey, ItemIdentity> createCache = new LinkedHashMap<>();
        private final Map<ItemIdentity, ItemIdentity> normalizeCache = new LinkedHashMap<>();
        private long createHits;
        private long createMisses;
        private long normalizeHits;
        private long normalizeMisses;

        private ItemIdentity create(ItemStack stack, Supplier<ItemIdentity> factory) {
            CreateKey key = CreateKey.from(stack);
            ItemIdentity cached = createCache.get(key);
            if (cached != null) {
                createHits++;
                return cached;
            }
            createMisses++;
            ItemIdentity created = factory.get();
            putBounded(createCache, key, created);
            return created;
        }

        private ItemIdentity normalize(ItemIdentity identity, Supplier<ItemIdentity> factory) {
            ItemIdentity cached = normalizeCache.get(identity);
            if (cached != null) {
                normalizeHits++;
                return cached;
            }
            normalizeMisses++;
            ItemIdentity normalized = factory.get();
            putBounded(normalizeCache, identity, normalized);
            return normalized;
        }

        public MemoStats stats() {
            return new MemoStats(createHits, createMisses, normalizeHits, normalizeMisses);
        }

        private static <K> void putBounded(Map<K, ItemIdentity> cache, K key, ItemIdentity value) {
            if (cache == null || key == null || value == null) {
                return;
            }
            if (cache.size() >= MAX_ENTRIES) {
                cache.clear();
            }
            cache.put(key, value);
        }
    }

    public record MemoStats(
            long createHits,
            long createMisses,
            long normalizeHits,
            long normalizeMisses
    ) {
    }

    private record CreateKey(
            String itemId,
            String componentFingerprint,
            boolean stackable,
            boolean damageable,
            Set<String> tags
    ) {
        private CreateKey {
            itemId = itemId == null ? "" : itemId;
            componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
            tags = tags == null ? Set.of() : Set.copyOf(tags);
        }

        static CreateKey from(ItemStack stack) {
            return new CreateKey(
                    resolveItemId(stack),
                    resolveComponentFingerprint(stack),
                    ItemIdentityMatcher.stackable(stack),
                    SlotStackAccess.current().damageable(stack),
                    ItemStackTags.itemTagIds(stack));
        }
    }

    public static boolean matchesMovable(ItemIdentity left, ItemIdentity right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        if (!Objects.equals(left.itemId(), right.itemId())) {
            return false;
        }
        if (left.comparisonMode() == ItemComparisonMode.ITEM_ID
                || right.comparisonMode() == ItemComparisonMode.ITEM_ID) {
            return true;
        }
        ItemIdentity normalizedLeft = normalizeMovable(left);
        ItemIdentity normalizedRight = normalizeMovable(right);
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
        return usesItemOnlyMovableIdentityWithoutFingerprint(stack)
                || hasToolStateFingerprint(resolveComponentFingerprint(stack));
    }

    private static boolean usesItemOnlyMovableIdentityWithoutFingerprint(ItemStack stack) {
        return SlotStackAccess.current().damageable(stack)
                || hasMovableToolTag(stack)
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
        return conditionOnlySegments(normalized);
    }

    private static String stableSelectorFingerprint(String itemId, String fingerprint) {
        if (itemId == null || itemId.isBlank()) {
            return "";
        }
        String normalized = normalizeFingerprint(fingerprint);
        if (normalized.isBlank()) {
            return "";
        }
        normalized = stripOuter(normalized, '{', '}');
        normalized = stripOuter(normalized, '[', ']');
        if (normalized.isBlank()) {
            return "";
        }
        // Patchouli multiplexes multiple logical books through one item id.
        // The selected book is stable identity; display/progress data is not.
        String patchouliBook = topLevelSelectorValue(normalized, "patchouli:book");
        if (!patchouliBook.isBlank()) {
            return "patchouli:book=" + patchouliBook;
        }
        return "";
    }

    private static String topLevelSelectorValue(String normalized, String selectorKey) {
        if (normalized == null || normalized.isBlank() || selectorKey == null || selectorKey.isBlank()) {
            return "";
        }
        for (String segment : topLevelFingerprintSegments(normalized)) {
            String value = selectorValue(segment, selectorKey);
            if (!value.isBlank()) {
                return value;
            }
            value = fingerprintSegmentValue(segment);
            if (!value.isBlank()) {
                value = stripOuter(value, '{', '}');
                value = stripOuter(value, '[', ']');
                String nested = topLevelSelectorValue(value, selectorKey);
                if (!nested.isBlank()) {
                    return nested;
                }
            }
            for (String nestedBody : nestedFingerprintBodies(value.isBlank() ? segment : value)) {
                String nested = topLevelSelectorValue(nestedBody, selectorKey);
                if (!nested.isBlank()) {
                    return nested;
                }
            }
        }
        return "";
    }

    private static String selectorValue(String segment, String selectorKey) {
        String normalized = segment == null ? "" : segment.strip();
        if (normalized.isBlank()) {
            return "";
        }
        String arrowPrefix = selectorKey + "=>";
        String equalsPrefix = selectorKey + "=";
        String colonPrefix = selectorKey + ":";
        if (normalized.startsWith(arrowPrefix)) {
            return cleanSelectorValue(normalized.substring(arrowPrefix.length()));
        }
        if (normalized.startsWith(equalsPrefix)) {
            return cleanSelectorValue(normalized.substring(equalsPrefix.length()));
        }
        if (normalized.startsWith(colonPrefix)) {
            return cleanSelectorValue(normalized.substring(colonPrefix.length()));
        }
        return "";
    }

    private static String cleanSelectorValue(String value) {
        String resolved = value == null ? "" : value.strip();
        resolved = stripOuter(resolved, '{', '}');
        resolved = stripOuter(resolved, '[', ']');
        return resolved;
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
                || key.equals("minecraft:bundle_contents")
                || key.equals("tfc:food")
                || key.equals("tfc:heat");
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
        for (String segment : topLevelFingerprintSegments(value)) {
            addFingerprintKey(keys, segment);
        }
        return keys.isEmpty() ? Set.of() : Set.copyOf(keys);
    }

    private static List<String> topLevelFingerprintSegments(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        ArrayList<String> segments = new ArrayList<>();
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
                addFingerprintSegment(segments, value.substring(segmentStart, index));
                segmentStart = index + 1;
            }
        }
        addFingerprintSegment(segments, value.substring(segmentStart));
        return segments.isEmpty() ? List.of() : List.copyOf(segments);
    }

    private static boolean conditionOnlySegments(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        boolean hasMovableKey = false;
        for (String segment : topLevelFingerprintSegments(value)) {
            String key = leadingFingerprintKey(segment);
            if (key.isBlank()) {
                return false;
            }
            if (isMovableConditionKey(key) || isToolStateKey(key)) {
                hasMovableKey = true;
                continue;
            }
            if (!isConditionWrapperKey(key)) {
                return false;
            }
            String nested = fingerprintSegmentValue(segment);
            nested = stripOuter(nested, '{', '}');
            nested = stripOuter(nested, '[', ']');
            if (!conditionOnlySegments(nested)) {
                return false;
            }
            hasMovableKey = true;
        }
        return hasMovableKey;
    }

    private static boolean isConditionWrapperKey(String key) {
        return key.equals("forgecaps")
                || key.equals("forge_caps")
                || key.equals("minecraft:custom_data");
    }

    private static void addFingerprintSegment(List<String> segments, String segment) {
        String normalized = segment == null ? "" : segment.strip();
        if (!normalized.isBlank()) {
            segments.add(normalized);
        }
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
        int nestedStart = firstNestedStart(value, 0);
        int arrow = value.indexOf("=>");
        if (arrow > 0 && beforeNested(arrow, nestedStart)) {
            return value.substring(0, arrow);
        }
        int equals = value.indexOf('=');
        if (equals > 0 && beforeNested(equals, nestedStart)) {
            return value.substring(0, equals);
        }
        int colon = value.indexOf(':');
        if (colon <= 0 || !beforeNested(colon, nestedStart)) {
            return "";
        }
        int secondColon = value.indexOf(':', colon + 1);
        if (secondColon > 0 && beforeNested(secondColon, nestedStart)) {
            return value.substring(0, secondColon);
        }
        return value.substring(0, colon);
    }

    private static String fingerprintSegmentValue(String segment) {
        String normalized = segment == null ? "" : segment.strip();
        if (normalized.isBlank()) {
            return "";
        }
        int nestedStart = firstNestedStart(normalized, 0);
        int arrow = normalized.indexOf("=>");
        if (arrow > 0 && beforeNested(arrow, nestedStart)) {
            return normalized.substring(arrow + 2).strip();
        }
        int equals = normalized.indexOf('=');
        if (equals > 0 && beforeNested(equals, nestedStart)) {
            return normalized.substring(equals + 1).strip();
        }
        String key = leadingFingerprintKey(normalized);
        if (key.isBlank()) {
            return "";
        }
        String colonPrefix = key + ":";
        return normalized.startsWith(colonPrefix)
                ? normalized.substring(colonPrefix.length()).strip()
                : "";
    }

    private static boolean beforeNested(int index, int nestedStart) {
        return index >= 0 && (nestedStart < 0 || index < nestedStart);
    }

    private static List<String> nestedFingerprintBodies(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        ArrayList<String> bodies = new ArrayList<>();
        int start = -1;
        char expectedClose = 0;
        int depth = 0;
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (start < 0) {
                if (c == '{' || c == '[') {
                    start = index + 1;
                    expectedClose = c == '{' ? '}' : ']';
                    depth = 1;
                }
                continue;
            }
            if ((expectedClose == '}' && c == '{') || (expectedClose == ']' && c == '[')) {
                depth++;
            } else if (c == expectedClose) {
                depth--;
                if (depth == 0) {
                    addFingerprintSegment(bodies, value.substring(start, index));
                    start = -1;
                    expectedClose = 0;
                }
            }
        }
        return bodies.isEmpty() ? List.of() : List.copyOf(bodies);
    }

    private static int firstNestedStart(String value, int start) {
        int curly = value.indexOf('{', Math.max(0, start));
        int square = value.indexOf('[', Math.max(0, start));
        if (curly < 0) {
            return square;
        }
        if (square < 0) {
            return curly;
        }
        return Math.min(curly, square);
    }
}
