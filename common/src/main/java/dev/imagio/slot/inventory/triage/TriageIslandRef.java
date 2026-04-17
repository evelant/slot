package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Objects;

public record TriageIslandRef(
        String islandId,
        String label,
        int color,
        ItemIdentity iconIdentity
) {
    public TriageIslandRef {
        Objects.requireNonNull(islandId, "islandId");
        if (islandId.isBlank()) {
            throw new IllegalArgumentException("islandId must not be blank");
        }
        label = label == null ? "" : label;
    }
}
