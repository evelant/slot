package dev.imagio.slot.classification.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RuntimeClassificationExportBuilder {
    public static final String RESOLVED_RUNTIME_ITEM_TAGS = "resolved_runtime";

    private RuntimeClassificationExportBuilder() {
    }

    public static RuntimeClassificationExportWriter.Result write(
            Path configRoot,
            String requestedPackId,
            String defaultPackId,
            String loader,
            String minecraftVersion,
            List<ItemEntry> items,
            RecipeIndex recipeIndex
    ) throws IOException {
        RecipeIndex recipes = recipeIndex == null ? new RecipeIndex() : recipeIndex;
        SummaryAccumulator summary = new SummaryAccumulator();
        ArrayList<JsonObject> records = new ArrayList<>();
        for (ItemEntry item : items == null ? List.<ItemEntry>of() : items) {
            if (item == null || item.id().isBlank()) {
                continue;
            }
            records.add(recordFor(item, recipes, summary));
        }
        records.sort(Comparator.comparing(record -> record.get("id").getAsString()));
        String packId = requestedPackId == null || requestedPackId.isBlank() ? defaultPackId : requestedPackId;
        return RuntimeClassificationExportWriter.write(
                configRoot,
                packId,
                loader,
                minecraftVersion,
                records,
                summary.toJson(recipes)
        );
    }

    public static List<String> sortedStrings(Collection<String> values) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        if (values != null) {
            values.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .forEach(unique::add);
        }
        ArrayList<String> sorted = new ArrayList<>(unique);
        sorted.sort(String::compareTo);
        return List.copyOf(sorted);
    }

    private static JsonObject recordFor(ItemEntry item, RecipeIndex recipes, SummaryAccumulator summary) {
        summary.addNamespace(item.namespace());
        summary.addItemTags(item.id(), item.itemTags());

        JsonObject record = new JsonObject();
        record.addProperty("id", item.id());
        record.addProperty("namespace", item.namespace());
        record.addProperty("path", item.path());
        record.addProperty("display_name", item.displayName());
        record.add("minecraft_tags", stringArray(item.itemTags()));
        record.add("minecraft_tags_direct", new JsonArray());
        record.add("recipe_role", recipes.recipeRole(item.id()));
        record.add("model_parents", new JsonArray());
        record.add("loot_table_sources", new JsonArray());
        record.add("creative_tabs", stringArray(item.creativeTabs()));
        record.add("component_data", copyObject(item.componentData()));
        if (!item.semanticText().isEmpty()) {
            record.add("semantic_text", semanticTextArray(item.semanticText()));
        }
        record.add("extractor_meta", extractorMeta(item, summary));
        return record;
    }

    private static JsonObject extractorMeta(ItemEntry item, SummaryAccumulator summary) {
        JsonObject meta = new JsonObject();
        meta.addProperty("extractor", "slot-runtime-export");
        meta.addProperty("translation_key", item.translationKey());
        meta.addProperty("item_tag_membership", RESOLVED_RUNTIME_ITEM_TAGS);
        meta.addProperty("direct_item_tags_available", false);

        BlockEntry block = item.block();
        meta.addProperty("is_block_item", block != null);
        if (block != null) {
            meta.addProperty("block_id", block.blockId());
            meta.addProperty("block_requires_correct_tool", block.requiresCorrectTool());
            meta.add("block_tags", stringArray(block.blockTags()));
            if (!block.blockId().isBlank()) {
                summary.addBlockItem(block.blockId());
                summary.addBlockTags(block.blockId(), block.blockTags());
            }
        }
        return meta;
    }

    private static JsonObject copyObject(JsonObject object) {
        JsonObject copy = new JsonObject();
        if (object == null) {
            return copy;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            copy.add(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    private static JsonArray stringArray(Collection<String> values) {
        JsonArray array = new JsonArray();
        for (String value : sortedStrings(values)) {
            array.add(value);
        }
        return array;
    }

    private static JsonArray semanticTextArray(Collection<SemanticEntry> values) {
        JsonArray array = new JsonArray();
        for (SemanticEntry value : semanticEntries(values)) {
            JsonObject entry = new JsonObject();
            entry.addProperty("source", value.source());
            if (!value.key().isBlank()) {
                entry.addProperty("key", value.key());
            }
            entry.addProperty("text", value.text());
            array.add(entry);
        }
        return array;
    }

    public static SemanticEntry runtimeTooltip(String text) {
        return new SemanticEntry("runtime-tooltip", "", text);
    }

    private static List<SemanticEntry> semanticEntries(Collection<SemanticEntry> values) {
        LinkedHashMap<String, SemanticEntry> unique = new LinkedHashMap<>();
        if (values != null) {
            values.stream()
                    .filter(Objects::nonNull)
                    .map(SemanticEntry::normalized)
                    .filter(value -> !value.text().isBlank())
                    .forEach(value -> unique.putIfAbsent(value.source() + "\u0000" + value.key() + "\u0000" + value.text(), value));
        }
        return List.copyOf(unique.values());
    }

    private static JsonObject countObject(Map<String, Integer> values) {
        JsonObject object = new JsonObject();
        if (values == null) {
            return object;
        }
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> object.addProperty(entry.getKey(), entry.getValue()));
        return object;
    }

    private static JsonObject stringSetMap(Map<String, Set<String>> map) {
        JsonObject object = new JsonObject();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> object.add(entry.getKey(), stringArray(entry.getValue())));
        return object;
    }

    public record ItemEntry(
            String id,
            String namespace,
            String path,
            String displayName,
            String translationKey,
            List<String> itemTags,
            List<String> creativeTabs,
            List<SemanticEntry> semanticText,
            JsonObject componentData,
            BlockEntry block
    ) {
        public ItemEntry {
            id = Objects.requireNonNullElse(id, "");
            namespace = Objects.requireNonNullElse(namespace, "");
            path = Objects.requireNonNullElse(path, "");
            displayName = Objects.requireNonNullElse(displayName, "");
            translationKey = Objects.requireNonNullElse(translationKey, "");
            itemTags = sortedStrings(itemTags);
            creativeTabs = sortedStrings(creativeTabs);
            semanticText = semanticEntries(semanticText);
            componentData = componentData == null ? new JsonObject() : componentData;
        }
    }

    public record SemanticEntry(
            String source,
            String key,
            String text
    ) {
        public SemanticEntry {
            source = Objects.requireNonNullElse(source, "runtime-tooltip").trim();
            key = Objects.requireNonNullElse(key, "").trim();
            text = Objects.requireNonNullElse(text, "").trim();
        }

        private SemanticEntry normalized() {
            return new SemanticEntry(source, key, text.replaceAll("\\s+", " ").trim());
        }
    }

    public record BlockEntry(
            String blockId,
            boolean requiresCorrectTool,
            List<String> blockTags
    ) {
        public BlockEntry {
            blockId = Objects.requireNonNullElse(blockId, "");
            blockTags = sortedStrings(blockTags);
        }
    }

    public static final class RecipeIndex {
        private final Map<String, Set<String>> ingredientOf = new LinkedHashMap<>();
        private final Map<String, Set<String>> outputOf = new LinkedHashMap<>();
        private final Map<String, Map<String, Integer>> ingredientCounts = new LinkedHashMap<>();
        private final Map<String, Map<String, Integer>> outputCounts = new LinkedHashMap<>();
        private final Map<String, Integer> recipeTypeCounts = new LinkedHashMap<>();
        private int recipeCount;

        public void addRecipe(String kind) {
            recipeCount++;
            String recipeKind = recipeKind(kind);
            recipeTypeCounts.put(recipeKind, recipeTypeCounts.getOrDefault(recipeKind, 0) + 1);
        }

        public void addIngredient(String itemId, String recipeId, String kind) {
            add(ingredientOf, ingredientCounts, itemId, recipeId, kind);
        }

        public void addOutput(String itemId, String recipeId, String kind) {
            add(outputOf, outputCounts, itemId, recipeId, kind);
        }

        private JsonObject recipeRole(String itemId) {
            Set<String> ingredients = ingredientOf.getOrDefault(itemId, Set.of());
            Set<String> outputs = outputOf.getOrDefault(itemId, Set.of());
            JsonObject role = new JsonObject();
            role.add("ingredient_of", stringArray(ingredients));
            role.add("output_of", stringArray(outputs));
            role.addProperty("in_degree", ingredients.size());
            role.addProperty("out_degree", outputs.size());
            role.add("ingredient_of_counts", countObject(ingredientCounts.get(itemId)));
            role.add("output_of_counts", countObject(outputCounts.get(itemId)));
            return role;
        }

        private void add(
                Map<String, Set<String>> recipesByItem,
                Map<String, Map<String, Integer>> countsByItem,
                String itemId,
                String recipeId,
                String kind
        ) {
            if (itemId == null || itemId.isBlank() || recipeId == null || recipeId.isBlank()) {
                return;
            }
            Set<String> recipes = recipesByItem.computeIfAbsent(itemId, ignored -> new LinkedHashSet<>());
            if (recipes.add(recipeId)) {
                String recipeKind = recipeKind(kind);
                Map<String, Integer> counts = countsByItem.computeIfAbsent(itemId, ignored -> new LinkedHashMap<>());
                counts.put(recipeKind, counts.getOrDefault(recipeKind, 0) + 1);
            }
        }

        private static String recipeKind(String kind) {
            if (kind == null || kind.isBlank()) {
                return "unknown";
            }
            return kind;
        }

        private int ingredientLinkCount() {
            return ingredientOf.values().stream().mapToInt(Set::size).sum();
        }

        private int outputLinkCount() {
            return outputOf.values().stream().mapToInt(Set::size).sum();
        }
    }

    private static final class SummaryAccumulator {
        private final Map<String, Integer> namespaceCounts = new LinkedHashMap<>();
        private final Map<String, Set<String>> itemTagMembers = new LinkedHashMap<>();
        private final Map<String, Set<String>> blockTagMembers = new LinkedHashMap<>();
        private final Set<String> blockItems = new LinkedHashSet<>();

        private void addNamespace(String namespace) {
            if (namespace == null || namespace.isBlank()) {
                return;
            }
            namespaceCounts.put(namespace, namespaceCounts.getOrDefault(namespace, 0) + 1);
        }

        private void addItemTags(String itemId, List<String> tags) {
            for (String tag : tags == null ? List.<String>of() : tags) {
                itemTagMembers.computeIfAbsent(tag, ignored -> new LinkedHashSet<>()).add(itemId);
            }
        }

        private void addBlockItem(String blockId) {
            blockItems.add(blockId);
        }

        private void addBlockTags(String blockId, List<String> tags) {
            for (String tag : tags == null ? List.<String>of() : tags) {
                blockTagMembers.computeIfAbsent(tag, ignored -> new LinkedHashSet<>()).add(blockId);
            }
        }

        private JsonObject toJson(RecipeIndex recipes) {
            JsonObject object = new JsonObject();
            object.addProperty("item_tag_membership", RESOLVED_RUNTIME_ITEM_TAGS);
            object.addProperty("direct_item_tags_available", false);
            object.add("namespace_counts", countObject(namespaceCounts));
            object.add("block_items", stringArray(blockItems));
            object.add("item_tag_members", stringSetMap(itemTagMembers));
            object.add("block_tag_members", stringSetMap(blockTagMembers));
            object.addProperty("recipe_count", recipes.recipeCount);
            object.addProperty("recipe_ingredient_link_count", recipes.ingredientLinkCount());
            object.addProperty("recipe_output_link_count", recipes.outputLinkCount());
            object.add("recipe_type_counts", countObject(recipes.recipeTypeCounts));

            JsonArray notes = new JsonArray();
            notes.add("Item tags are live resolved runtime membership; minecraft_tags_direct is intentionally empty because runtime tag APIs do not expose direct provenance.");
            notes.add("Recipe outputs use Recipe#getResultItem, so multi-output custom recipes may need a later extractor pass.");
            notes.add("Creative tabs are rebuilt from live runtime tab contents when the loader exposes them; search/inventory/hotbar/op tabs are omitted.");
            notes.add("Model and loot-table fields are intentionally empty in runtime export v1.");
            object.add("runtime_notes", notes);
            return object;
        }
    }
}
