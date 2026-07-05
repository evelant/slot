package dev.imagio.slot.inventory.workspace;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class WorkspaceProximityInvalidations {
    private static final EnumSet<WorkspaceProjectionSlice> STORAGE_PROXIMITY_SLICES = EnumSet.of(
            WorkspaceProjectionSlice.CARD,
            WorkspaceProjectionSlice.SECTION,
            WorkspaceProjectionSlice.STORAGE,
            WorkspaceProjectionSlice.WAYFINDING,
            WorkspaceProjectionSlice.DEPOSITABILITY,
            WorkspaceProjectionSlice.WORKFLOW,
            WorkspaceProjectionSlice.FRAME);

    private WorkspaceProximityInvalidations() {
    }

    public static WorkspaceInvalidation storageProximityChange(
            Set<String> previousProximateStorageIds,
            Set<String> currentProximateStorageIds,
            Set<String> previousContextualStorageIds,
            Set<String> currentContextualStorageIds
    ) {
        LinkedHashSet<String> changedStorageIds = new LinkedHashSet<>();
        addSymmetricDifference(changedStorageIds, previousProximateStorageIds, currentProximateStorageIds);
        addSymmetricDifference(changedStorageIds, previousContextualStorageIds, currentContextualStorageIds);
        if (changedStorageIds.isEmpty()) {
            return null;
        }
        boolean proximateChanged = !clean(previousProximateStorageIds).equals(clean(currentProximateStorageIds));
        boolean contextualChanged = !clean(previousContextualStorageIds).equals(clean(currentContextualStorageIds));
        return new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.PROXIMITY_CHANGED,
                Set.of(),
                changedStorageIds,
                Set.of(),
                STORAGE_PROXIMITY_SLICES,
                false,
                diagnostics(proximateChanged, contextualChanged));
    }

    private static void addSymmetricDifference(
            LinkedHashSet<String> changed,
            Set<String> previous,
            Set<String> current
    ) {
        Set<String> before = clean(previous);
        Set<String> after = clean(current);
        for (String storageId : before) {
            if (!after.contains(storageId)) {
                changed.add(storageId);
            }
        }
        for (String storageId : after) {
            if (!before.contains(storageId)) {
                changed.add(storageId);
            }
        }
    }

    private static Set<String> clean(Set<String> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String value : source) {
            if (value != null && !value.isBlank()) {
                cleaned.add(value);
            }
        }
        return cleaned.isEmpty() ? Set.of() : Set.copyOf(cleaned);
    }

    private static String diagnostics(boolean proximateChanged, boolean contextualChanged) {
        if (proximateChanged && contextualChanged) {
            return "storage_proximity_and_contextual_changed";
        }
        if (proximateChanged) {
            return "storage_proximity_changed";
        }
        return contextualChanged ? "storage_contextual_proximity_changed" : "storage_proximity_unchanged";
    }
}
