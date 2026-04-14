package dev.imagio.slot.policy;

import dev.imagio.slot.source.SourceDescriptor;
import dev.imagio.slot.source.SourceGroup;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class SourcePreferencePolicy {
    private static final Comparator<SourceDescriptor> DEFAULT_PRECEDENCE = Comparator
            .comparingInt(SourcePreferencePolicy::precedenceBucket)
            .thenComparingInt(SourceDescriptor::stableOrder)
            .thenComparing(descriptor -> descriptor.id().value());

    public Optional<SourceDescriptor> chooseExistingSource(
            Collection<SourceDescriptor> candidates,
            SourceSelectionPreference preference
    ) {
        return choose(candidates, preference, SourceDescriptor::canExtract);
    }

    public Optional<SourceDescriptor> chooseTargetSource(
            Collection<SourceDescriptor> candidates,
            SourceSelectionPreference preference
    ) {
        return choose(candidates, preference, SourceDescriptor::canInsert);
    }

    private Optional<SourceDescriptor> choose(
            Collection<SourceDescriptor> candidates,
            SourceSelectionPreference preference,
            Predicate<SourceDescriptor> allowed
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        Map<String, SourceDescriptor> descriptorsById = new LinkedHashMap<>();
        for (SourceDescriptor candidate : candidates) {
            if (candidate != null) {
                descriptorsById.put(candidate.id().value(), candidate);
            }
        }
        if (descriptorsById.isEmpty()) {
            return Optional.empty();
        }

        SourceSelectionPreference resolvedPreference = preference == null
                ? new SourceSelectionPreference(null, null, null)
                : preference;

        Optional<SourceDescriptor> explicit = resolvePreferred(
                resolvedPreference.explicitSourceId() == null ? null : resolvedPreference.explicitSourceId().value(),
                descriptorsById,
                allowed
        );
        if (explicit.isPresent()) {
            return explicit;
        }

        Optional<SourceDescriptor> focused = resolvePreferred(
                resolvedPreference.focusedSourceId() == null ? null : resolvedPreference.focusedSourceId().value(),
                descriptorsById,
                allowed
        );
        if (focused.isPresent()) {
            return focused;
        }

        Optional<SourceDescriptor> cursorCompatible = resolvePreferred(
                resolvedPreference.cursorCompatibleSourceId() == null ? null : resolvedPreference.cursorCompatibleSourceId().value(),
                descriptorsById,
                allowed
        );
        if (cursorCompatible.isPresent()) {
            return cursorCompatible;
        }

        return descriptorsById.values().stream()
                .filter(allowed)
                .min(DEFAULT_PRECEDENCE);
    }

    private Optional<SourceDescriptor> resolvePreferred(
            String sourceId,
            Map<String, SourceDescriptor> descriptorsById,
            Predicate<SourceDescriptor> allowed
    ) {
        if (sourceId == null || sourceId.isBlank()) {
            return Optional.empty();
        }

        SourceDescriptor descriptor = descriptorsById.get(sourceId);
        if (descriptor == null || !allowed.test(descriptor)) {
            return Optional.empty();
        }

        return Optional.of(descriptor);
    }

    private static int precedenceBucket(SourceDescriptor descriptor) {
        SourceGroup group = descriptor.group();
        return switch (group) {
            case PLAYER_MAIN -> 0;
            case PLAYER_HOTBAR -> 1;
            case CARRIED -> descriptor.primaryCarried() ? 2 : 3;
            case EXTERNAL -> 4;
            case EQUIPMENT -> 5;
            case TOOL -> 6;
            case VIRTUAL -> 7;
        };
    }
}
