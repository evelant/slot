package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.LinkedHashMap;
import java.util.Map;

public record ContextualSignalEvent(
        ContextualSignalKind kind,
        ItemIdentity identity,
        int count,
        long observedTick,
        String contextKey,
        String contextLabel,
        String sourceKey,
        Map<String, String> metadata
) {
    public ContextualSignalEvent {
        kind = kind == null ? ContextualSignalKind.ITEM_ACQUIRED : kind;
        count = Math.max(0, count);
        observedTick = Math.max(0L, observedTick);
        contextKey = contextKey == null ? "" : contextKey.trim();
        contextLabel = contextLabel == null ? "" : contextLabel.trim();
        sourceKey = sourceKey == null ? "" : sourceKey.trim();
        metadata = copyMetadata(metadata);
    }

    public static ContextualSignalEvent item(
            ContextualSignalKind kind,
            ItemIdentity identity,
            int count,
            String sourceKey
    ) {
        return new ContextualSignalEvent(kind, identity, count, 0L, "", "", sourceKey, Map.of());
    }

    public ContextualSignalEvent withMetadata(String key, String value) {
        if (key == null || key.isBlank()) {
            return this;
        }
        LinkedHashMap<String, String> next = new LinkedHashMap<>(metadata);
        next.put(key.trim(), value == null ? "" : value.trim());
        return new ContextualSignalEvent(kind, identity, count, observedTick, contextKey, contextLabel, sourceKey, next);
    }

    public String metadataValue(String key) {
        return key == null ? "" : metadata.getOrDefault(key, "");
    }

    private static Map<String, String> copyMetadata(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            copy.put(key.trim(), entry.getValue() == null ? "" : entry.getValue().trim());
            if (copy.size() >= 16) {
                break;
            }
        }
        return copy.isEmpty() ? Map.of() : Map.copyOf(copy);
    }
}
