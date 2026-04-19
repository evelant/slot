package dev.imagio.slot.workflow.domain;

import java.util.UUID;

public record ChestLink(
        String islandId,
        UUID storageId
) {
    public ChestLink {
        if (islandId == null || islandId.isBlank()) {
            throw new IllegalArgumentException("islandId must not be blank");
        }
        if (storageId == null) {
            throw new IllegalArgumentException("storageId must not be null");
        }
        islandId = islandId.trim();
    }
}
