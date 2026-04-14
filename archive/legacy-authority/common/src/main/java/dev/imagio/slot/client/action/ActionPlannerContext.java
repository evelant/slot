package dev.imagio.slot.client.action;

import dev.imagio.slot.client.source.LegacySourceDescriptors;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.policy.SourceSelectionPreference;
import dev.imagio.slot.source.SourceDescriptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record ActionPlannerContext(
        Map<String, InventorySource> sources,
        String explicitSourceId,
        String focusedSourceId,
        String cursorCompatibleSourceId
) {
    public ActionPlannerContext {
        sources = Map.copyOf(sources);
    }

    public static ActionPlannerContext of(
            Collection<? extends InventorySource> sources,
            String explicitSourceId,
            String focusedSourceId,
            String cursorCompatibleSourceId
    ) {
        Map<String, InventorySource> sourceMap = new LinkedHashMap<>();
        for (InventorySource source : sources) {
            sourceMap.put(source.id(), source);
        }

        return new ActionPlannerContext(sourceMap, explicitSourceId, focusedSourceId, cursorCompatibleSourceId);
    }

    public Map<String, SourceDescriptor> sourceDescriptors() {
        Map<String, SourceDescriptor> descriptors = new LinkedHashMap<>();
        for (InventorySource source : sources.values()) {
            descriptors.put(source.id(), LegacySourceDescriptors.describe(source));
        }
        return Map.copyOf(descriptors);
    }

    public Optional<SourceDescriptor> sourceDescriptor(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return Optional.empty();
        }
        InventorySource source = sources.get(sourceId);
        return source == null ? Optional.empty() : Optional.of(LegacySourceDescriptors.describe(source));
    }

    public SourceSelectionPreference selectionPreference() {
        return LegacySourceDescriptors.preference(explicitSourceId, focusedSourceId, cursorCompatibleSourceId);
    }
}
