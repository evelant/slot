package dev.imagio.slot.client.action;

import dev.imagio.slot.client.model.ItemEntry;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.policy.SourcePreferencePolicy;
import dev.imagio.slot.source.SourceDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ActionPlanner {
    private final SourcePreferencePolicy sourcePreferencePolicy = new SourcePreferencePolicy();

    public Optional<InventorySource> chooseExistingSource(ItemEntry entry, ActionPlannerContext context) {
        return chooseExistingSource(entry.perSourceCounts().keySet(), context);
    }

    public Optional<InventorySource> chooseExistingSource(Set<String> candidateSourceIds, ActionPlannerContext context) {
        return chooseSource(
                candidateSourceIds,
                context,
                candidates -> sourcePreferencePolicy.chooseExistingSource(candidates, context.selectionPreference())
        );
    }

    public Optional<InventorySource> chooseTargetSource(Set<String> candidateSourceIds, ActionPlannerContext context) {
        return chooseSource(
                candidateSourceIds,
                context,
                candidates -> sourcePreferencePolicy.chooseTargetSource(candidates, context.selectionPreference())
        );
    }

    private Optional<InventorySource> chooseSource(
            Set<String> candidateSourceIds,
            ActionPlannerContext context,
            java.util.function.Function<List<SourceDescriptor>, Optional<SourceDescriptor>> chooser
    ) {
        if (candidateSourceIds.isEmpty()) {
            return Optional.empty();
        }

        List<SourceDescriptor> candidates = candidateSourceIds.stream()
                .map(context::sourceDescriptor)
                .flatMap(Optional::stream)
                .toList();
        return chooser.apply(candidates)
                .map(descriptor -> context.sources().get(descriptor.id().value()));
    }
}
