package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record WorkspaceInvalidationSummary(
        List<WorkspaceInvalidation> invalidations,
        Set<ItemIdentity> identities,
        Set<String> storageIds,
        Set<String> sectionIds,
        EnumSet<WorkspaceProjectionSlice> slices,
        boolean requiresFullProjection,
        String fallbackDiagnostics
) {
    private static final WorkspaceInvalidationSummary EMPTY = new WorkspaceInvalidationSummary(
            List.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            WorkspaceProjectionSlice.none(),
            false,
            "");

    public WorkspaceInvalidationSummary {
        invalidations = invalidations == null ? List.of() : List.copyOf(invalidations);
        identities = identities == null ? Set.of() : Set.copyOf(identities);
        storageIds = storageIds == null ? Set.of() : Set.copyOf(storageIds);
        sectionIds = sectionIds == null ? Set.of() : Set.copyOf(sectionIds);
        slices = slices == null || slices.isEmpty() ? WorkspaceProjectionSlice.none() : EnumSet.copyOf(slices);
        fallbackDiagnostics = fallbackDiagnostics == null ? "" : fallbackDiagnostics;
    }

    public static WorkspaceInvalidationSummary empty() {
        return EMPTY;
    }

    public static WorkspaceInvalidationSummary coalesce(Collection<WorkspaceInvalidation> invalidations) {
        if (invalidations == null || invalidations.isEmpty()) {
            return empty();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        LinkedHashSet<String> storageIds = new LinkedHashSet<>();
        LinkedHashSet<String> sectionIds = new LinkedHashSet<>();
        EnumSet<WorkspaceProjectionSlice> slices = WorkspaceProjectionSlice.none();
        java.util.ArrayList<WorkspaceInvalidation> copy = new java.util.ArrayList<>();
        StringBuilder fallback = new StringBuilder();
        boolean full = false;
        for (WorkspaceInvalidation invalidation : invalidations) {
            if (invalidation == null) {
                continue;
            }
            copy.add(invalidation);
            identities.addAll(invalidation.identities());
            storageIds.addAll(invalidation.storageIds());
            sectionIds.addAll(invalidation.sectionIds());
            slices.addAll(invalidation.slices());
            full |= invalidation.requiresFullProjection();
            if (invalidation.requiresFullProjection()) {
                appendFallback(fallback, invalidation);
            }
        }
        if (copy.isEmpty()) {
            return empty();
        }
        return new WorkspaceInvalidationSummary(
                copy,
                identities,
                storageIds,
                sectionIds,
                slices,
                full,
                fallback.toString());
    }

    public int invalidationCount() {
        return invalidations.size();
    }

    public String reasonSummary() {
        if (invalidations.isEmpty()) {
            return "none";
        }
        EnumMap<WorkspaceInvalidation.Reason, Integer> counts =
                new EnumMap<>(WorkspaceInvalidation.Reason.class);
        for (WorkspaceInvalidation invalidation : invalidations) {
            counts.merge(invalidation.reason(), 1, Integer::sum);
        }
        StringBuilder out = new StringBuilder();
        counts.forEach((reason, count) -> {
            if (!out.isEmpty()) {
                out.append(',');
            }
            out.append(reason.name()).append('=').append(count);
        });
        return out.toString();
    }

    public String compactSummary() {
        if (invalidations.isEmpty()) {
            return "none";
        }
        return "count=" + invalidations.size()
                + ",reasons=" + reasonSummary()
                + ",identities=" + identities.size()
                + ",storages=" + storageIds.size()
                + ",sections=" + sectionIds.size()
                + ",slices=" + slices.size()
                + ",full=" + requiresFullProjection;
    }

    private static void appendFallback(StringBuilder fallback, WorkspaceInvalidation invalidation) {
        if (fallback == null || invalidation == null) {
            return;
        }
        if (!fallback.isEmpty()) {
            fallback.append(';');
        }
        fallback.append(invalidation.reason().name());
        if (!invalidation.diagnostics().isBlank()) {
            fallback.append(':').append(invalidation.diagnostics());
        }
    }
}
