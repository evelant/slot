package dev.imagio.slot.debug;

import java.util.List;
import java.util.Objects;

public record SubBucketRule(
        SemanticBucket parent,
        String subId,
        String label,
        int priority,
        List<String> keywords
) {
    public SubBucketRule {
        Objects.requireNonNull(parent, "parent");
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("subId must not be blank");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        keywords = keywords == null ? List.of() : List.copyOf(keywords);
    }

    public String islandId() {
        return RealisticAtlasGenerator.SYNTHETIC_ISLAND_ID_PREFIX + subId;
    }
}
