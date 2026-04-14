package dev.imagio.slot.source;

import java.util.Objects;

public record SourceSlotRef(
        String kind,
        SourceId sourceId,
        String payload
) {
    public SourceSlotRef {
        kind = normalize(kind, "kind");
        Objects.requireNonNull(sourceId, "sourceId");
        payload = normalize(payload, "payload");
    }

    public static SourceSlotRef menuSlot(SourceId sourceId, int menuSlot) {
        return new SourceSlotRef("menu_slot", sourceId, Integer.toString(menuSlot));
    }

    public static SourceSlotRef itemHandlerSlot(SourceId sourceId, int slotIndex) {
        return new SourceSlotRef("item_handler_slot", sourceId, Integer.toString(slotIndex));
    }

    public static SourceSlotRef opaqueEntry(SourceId sourceId, String entryKey) {
        return new SourceSlotRef("opaque_entry", sourceId, entryKey);
    }

    private static String normalize(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
