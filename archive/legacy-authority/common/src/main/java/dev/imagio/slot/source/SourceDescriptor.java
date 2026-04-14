package dev.imagio.slot.source;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public record SourceDescriptor(
        SourceId id,
        String label,
        String shortLabel,
        SourceGroup group,
        SourceKind kind,
        Set<SourceCapability> capabilities,
        int stableOrder,
        boolean primaryCarried,
        String bridgeId
) {
    public SourceDescriptor {
        Objects.requireNonNull(id, "id");
        label = label == null || label.isBlank() ? id.value() : label;
        shortLabel = shortLabel == null || shortLabel.isBlank() ? label : shortLabel;
        group = group == null ? SourceGroup.VIRTUAL : group;
        kind = kind == null ? SourceKind.UNKNOWN : kind;
        capabilities = copyCapabilities(capabilities);
        bridgeId = bridgeId == null ? "" : bridgeId;
    }

    public boolean supports(SourceCapability capability) {
        return capability != null && capabilities.contains(capability);
    }

    public boolean canInsert() {
        return supports(SourceCapability.INSERT);
    }

    public boolean canExtract() {
        return supports(SourceCapability.EXTRACT_ONE)
                || supports(SourceCapability.EXTRACT_STACK)
                || supports(SourceCapability.EXTRACT_ALL_MATCHING);
    }

    public String idString() {
        return id.value();
    }

    private static Set<SourceCapability> copyCapabilities(Set<SourceCapability> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(capabilities));
    }
}
