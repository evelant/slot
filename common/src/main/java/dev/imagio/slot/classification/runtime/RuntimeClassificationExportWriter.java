package dev.imagio.slot.classification.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RuntimeClassificationExportWriter {
    private static final Gson COMPACT_GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson PRETTY_GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private RuntimeClassificationExportWriter() {
    }

    public static Result write(
            Path configRoot,
            String requestedPackId,
            String loader,
            String minecraftVersion,
            List<JsonObject> itemRecords,
            JsonObject runtimeSummary
    ) throws IOException {
        List<JsonObject> records = itemRecords == null ? List.of() : itemRecords;
        String packId = sanitizePackId(requestedPackId);
        Path exportRoot = (configRoot == null ? Path.of("config") : configRoot)
                .resolve("slot")
                .resolve("classification")
                .resolve("exports");
        Files.createDirectories(exportRoot);

        Path itemsPath = exportRoot.resolve(packId + ".runtime-items.ndjson");
        Path summaryPath = exportRoot.resolve(packId + ".runtime-summary.json");

        try (Writer writer = Files.newBufferedWriter(itemsPath, StandardCharsets.UTF_8)) {
            for (JsonObject record : records) {
                COMPACT_GSON.toJson(record, writer);
                writer.write('\n');
            }
        }

        JsonObject summary = new JsonObject();
        summary.addProperty("schema_version", 1);
        summary.addProperty("format", "slot-runtime-classification-export");
        summary.addProperty("generated_by", "slot-runtime-export");
        summary.addProperty("generated_at", Instant.now().toString());
        summary.addProperty("pack_id", packId);
        summary.addProperty("requested_pack_id", requestedPackId == null ? "" : requestedPackId);
        summary.addProperty("loader", loader == null ? "" : loader);
        summary.addProperty("minecraft_version", minecraftVersion == null ? "" : minecraftVersion);
        summary.addProperty("item_count", records.size());
        summary.addProperty("items_file", itemsPath.getFileName().toString());

        if (runtimeSummary != null) {
            for (Map.Entry<String, JsonElement> entry : runtimeSummary.entrySet()) {
                if (!summary.has(entry.getKey())) {
                    summary.add(entry.getKey(), entry.getValue());
                }
            }
        }

        try (Writer writer = Files.newBufferedWriter(summaryPath, StandardCharsets.UTF_8)) {
            PRETTY_GSON.toJson(summary, writer);
            writer.write('\n');
        }

        return new Result(packId, itemsPath, summaryPath, records.size());
    }

    private static String sanitizePackId(String value) {
        String raw = value == null || value.isBlank() ? "runtime" : value;
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = Character.toLowerCase(raw.charAt(i));
            boolean allowed = (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_'
                    || c == '-'
                    || c == '.';
            out.append(allowed ? c : '-');
        }
        String result = out.toString()
                .replaceAll("-+", "-")
                .replaceAll("^[.-]+", "")
                .replaceAll("[.-]+$", "");
        return result.isBlank() ? "runtime" : result.toLowerCase(Locale.ROOT);
    }

    public record Result(
            String packId,
            Path itemsPath,
            Path summaryPath,
            int itemCount
    ) {
    }
}
