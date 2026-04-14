package dev.imagio.slot.session;

import dev.imagio.slot.source.SourceDescriptor;
import dev.imagio.slot.source.SourceGroup;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record SlotSessionDescriptor(
        String sessionId,
        String fingerprint,
        SlotSessionMode mode,
        String screenClassName,
        String menuClassName,
        int containerId,
        List<SourceDescriptor> sources,
        Set<SlotSessionCapability> capabilities
) {
    public SlotSessionDescriptor {
        sessionId = sessionId == null ? "" : sessionId;
        fingerprint = fingerprint == null ? "" : fingerprint;
        mode = mode == null ? SlotSessionMode.UNSUPPORTED : mode;
        screenClassName = screenClassName == null ? "" : screenClassName;
        menuClassName = menuClassName == null ? "" : menuClassName;
        containerId = Math.max(-1, containerId);
        sources = sources == null ? List.of() : List.copyOf(sources);
        capabilities = copyCapabilities(capabilities);
    }

    public boolean recordsRecentLoot() {
        return capabilities.contains(SlotSessionCapability.RECORDS_RECENT) || mode.recordsRecentLoot();
    }

    public boolean slotOwned() {
        return capabilities.contains(SlotSessionCapability.SLOT_OWNED) || mode.slotOwned();
    }

    public boolean carriedOnlyMode() {
        return mode.carriedOnly();
    }

    public boolean hasStorageView() {
        return capabilities.contains(SlotSessionCapability.HAS_STORAGE_VIEW);
    }

    public boolean hasExternalSources() {
        return capabilities.contains(SlotSessionCapability.HAS_EXTERNAL_SOURCES)
                || sources.stream().anyMatch(source -> source.group() == SourceGroup.EXTERNAL);
    }

    public boolean hasCarriedSources() {
        return capabilities.contains(SlotSessionCapability.HAS_CARRIED_SOURCES)
                || sources.stream().anyMatch(source -> source.group().carried());
    }

    public Optional<SourceDescriptor> findSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return Optional.empty();
        }
        return sources.stream()
                .filter(source -> Objects.equals(source.id().value(), sourceId))
                .findFirst();
    }

    private static Set<SlotSessionCapability> copyCapabilities(Set<SlotSessionCapability> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(capabilities));
    }
}
