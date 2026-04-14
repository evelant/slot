package dev.imagio.slot.client.category;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.client.model.ItemIdentity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class InventoryGroupingOverrides extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "inventory_groups";

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final InventoryGroupingOverrides INSTANCE = new InventoryGroupingOverrides();

    private static volatile GroupingState state = GroupingState.EMPTY;

    private InventoryGroupingOverrides() {
        super(GSON, DIRECTORY);
    }

    public static InventoryGroupingOverrides listener() {
        return INSTANCE;
    }

    public static GroupingBucket resolveFallbackBucket(ItemIdentity identity, SlotCategory category) {
        if (identity == null || category != SlotCategory.MISC) {
            return null;
        }
        return state.resolve(identity);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        state = GroupingState.from(prepared);
    }

    public record GroupingBucket(String id, String label) {
        public GroupingBucket {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Grouping bucket id must not be blank");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Grouping bucket label must not be blank");
            }
        }
    }

    private record GroupingState(
            Map<String, GroupingBucket> itemBuckets,
            Map<String, GroupingBucket> namespaceBuckets
    ) {
        private static final GroupingState EMPTY = new GroupingState(Map.of(), Map.of());

        private GroupingBucket resolve(ItemIdentity identity) {
            GroupingBucket itemBucket = itemBuckets.get(identity.itemId());
            if (itemBucket != null) {
                return itemBucket;
            }

            String namespace = normalizeNamespace(identity.namespace());
            GroupingBucket namespaceBucket = namespaceBuckets.get(namespace);
            if (namespaceBucket != null) {
                return namespaceBucket;
            }

            if ("minecraft".equals(namespace)) {
                return null;
            }
            return new GroupingBucket(defaultBucketId(namespace), namespaceDisplayLabel(namespace));
        }

        private static GroupingState from(Map<ResourceLocation, JsonElement> prepared) {
            if (prepared.isEmpty()) {
                return EMPTY;
            }

            Map<String, GroupingBucket> itemBuckets = new LinkedHashMap<>();
            Map<String, GroupingBucket> namespaceBuckets = new LinkedHashMap<>();
            prepared.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> parseFile(entry.getKey(), entry.getValue(), itemBuckets, namespaceBuckets));
            return new GroupingState(Map.copyOf(itemBuckets), Map.copyOf(namespaceBuckets));
        }

        private static void parseFile(
                ResourceLocation resourceId,
                JsonElement rootElement,
                Map<String, GroupingBucket> itemBuckets,
                Map<String, GroupingBucket> namespaceBuckets
        ) {
            if (!(rootElement instanceof JsonObject rootObject)) {
                SlotCommon.LOGGER.warn("Ignoring SLOT inventory grouping overrides {} because the root is not a JSON object", resourceId);
                return;
            }

            JsonArray groups = rootObject.getAsJsonArray("groups");
            if (groups == null) {
                SlotCommon.LOGGER.warn("Ignoring SLOT inventory grouping overrides {} because it is missing a groups array", resourceId);
                return;
            }

            int groupIndex = 0;
            for (JsonElement groupElement : groups) {
                groupIndex++;
                if (!(groupElement instanceof JsonObject groupObject)) {
                    SlotCommon.LOGGER.warn("Ignoring SLOT inventory grouping overrides {} group {} because it is not an object", resourceId, groupIndex);
                    continue;
                }

                GroupingBucket bucket = parseBucket(resourceId, groupIndex, groupObject);
                if (bucket == null) {
                    continue;
                }

                readStringArray(groupObject.get("namespaces")).stream()
                        .map(InventoryGroupingOverrides::normalizeNamespace)
                        .filter(namespace -> !namespace.isBlank())
                        .forEach(namespace -> namespaceBuckets.put(namespace, bucket));

                readStringArray(groupObject.get("items")).stream()
                        .map(String::trim)
                        .filter(itemId -> !itemId.isBlank())
                        .forEach(itemId -> itemBuckets.put(itemId, bucket));
            }
        }

        private static GroupingBucket parseBucket(ResourceLocation resourceId, int groupIndex, JsonObject groupObject) {
            String rawId = readString(groupObject, "id");
            String rawLabel = readString(groupObject, "label");
            if (rawId == null || rawLabel == null) {
                SlotCommon.LOGGER.warn("Ignoring SLOT inventory grouping overrides {} group {} because it is missing id or label", resourceId, groupIndex);
                return null;
            }

            String normalizedId = normalizeGroupId(rawId);
            String normalizedLabel = rawLabel.trim();
            if (normalizedId.isBlank() || normalizedLabel.isBlank()) {
                SlotCommon.LOGGER.warn("Ignoring SLOT inventory grouping overrides {} group {} because id or label is blank", resourceId, groupIndex);
                return null;
            }
            return new GroupingBucket(normalizedId, normalizedLabel);
        }
    }

    private static String readString(JsonObject jsonObject, String memberName) {
        JsonElement element = jsonObject.get(memberName);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static List<String> readStringArray(JsonElement element) {
        if (!(element instanceof JsonArray array)) {
            return List.of();
        }

        List<String> values = new java.util.ArrayList<>(array.size());
        for (JsonElement value : array) {
            if (value != null && value.isJsonPrimitive()) {
                values.add(value.getAsString());
            }
        }
        return List.copyOf(values);
    }

    private static String normalizeNamespace(String namespace) {
        return namespace == null ? "" : namespace.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeGroupId(String groupId) {
        String normalized = groupId == null ? "" : groupId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if ((character >= 'a' && character <= 'z')
                    || (character >= '0' && character <= '9')
                    || character == '_'
                    || character == '-'
                    || character == '/'
                    || character == '.') {
                builder.append(character);
            } else if (Character.isWhitespace(character)) {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    private static String defaultBucketId(String namespace) {
        return "mod/" + normalizeNamespace(namespace);
    }

    private static String namespaceDisplayLabel(String namespace) {
        String normalized = normalizeNamespace(namespace);
        if (normalized.isEmpty()) {
            return SlotCategory.MISC.displayName();
        }

        String[] words = normalized.replace('-', ' ').replace('_', ' ').split("\\s+");
        StringJoiner joiner = new StringJoiner(" ");
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            joiner.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        String label = joiner.toString().trim();
        return label.isEmpty() ? normalized : label;
    }
}
