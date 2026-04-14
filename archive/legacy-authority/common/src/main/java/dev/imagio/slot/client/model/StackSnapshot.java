package dev.imagio.slot.client.model;

public record StackSnapshot(ItemIdentity identity, SlotRef slotRef, int count) {
    public StackSnapshot {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        if (slotRef == null) {
            throw new IllegalArgumentException("slotRef must not be null");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }
}
