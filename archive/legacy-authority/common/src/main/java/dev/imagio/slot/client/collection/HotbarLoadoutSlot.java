package dev.imagio.slot.client.collection;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.Objects;

public record HotbarLoadoutSlot(
        int slotIndex,
        ItemIdentity identity
) {
    public HotbarLoadoutSlot {
        if (slotIndex < 0 || slotIndex >= HotbarLoadoutDefinition.HOTBAR_SLOT_COUNT) {
            throw new IllegalArgumentException("Hotbar slot index must be between 0 and 8");
        }
        Objects.requireNonNull(identity, "identity");
    }
}
