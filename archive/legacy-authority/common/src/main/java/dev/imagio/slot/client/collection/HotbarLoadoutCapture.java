package dev.imagio.slot.client.collection;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.List;

public record HotbarLoadoutCapture(
        List<HotbarLoadoutSlot> slots,
        ItemIdentity offhandIdentity
) {
    public HotbarLoadoutCapture {
        slots = slots == null ? List.of() : List.copyOf(slots);
    }

    public boolean isEmpty() {
        return slots.isEmpty() && offhandIdentity == null;
    }
}
