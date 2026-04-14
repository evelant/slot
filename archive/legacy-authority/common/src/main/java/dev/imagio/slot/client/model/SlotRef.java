package dev.imagio.slot.client.model;

public record SlotRef(String sourceId, int slotIndex) {
    public SlotRef {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must not be negative");
        }
    }
}
