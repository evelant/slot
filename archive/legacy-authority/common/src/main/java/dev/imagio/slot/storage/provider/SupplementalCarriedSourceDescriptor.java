package dev.imagio.slot.storage.provider;

import java.util.Objects;

public record SupplementalCarriedSourceDescriptor(
        String providerId,
        String sourceId,
        String referenceKey,
        dev.imagio.slot.session.InventorySourceDescriptor sourceDescriptor
) {
    public SupplementalCarriedSourceDescriptor {
        providerId = providerId == null ? "" : providerId;
        sourceId = sourceId == null ? "" : sourceId;
        referenceKey = referenceKey == null ? "" : referenceKey;
        sourceDescriptor = sourceDescriptor == null ? null : sourceDescriptor;
        if (providerId.isBlank()) {
            throw new IllegalArgumentException("providerId must not be blank");
        }
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (sourceDescriptor == null) {
            throw new IllegalArgumentException("sourceDescriptor must not be null");
        }
    }

    public boolean matchesSource(String sourceId) {
        return Objects.equals(this.sourceId, sourceId);
    }
}
