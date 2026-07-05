package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Typed description of why a workspace projection is stale.
 *
 * <p>The current implementation still falls back to full projection for most
 * reasons. Keeping the reason, affected keys, and intended slices in common is
 * the contract later incremental slices will consume.
 */
public record WorkspaceInvalidation(
        Reason reason,
        Set<ItemIdentity> identities,
        Set<String> storageIds,
        Set<String> sectionIds,
        EnumSet<WorkspaceProjectionSlice> slices,
        boolean requiresFullProjection,
        String diagnostics
) {
    public WorkspaceInvalidation {
        reason = reason == null ? Reason.UNKNOWN : reason;
        identities = copyIdentities(identities);
        storageIds = copyStrings(storageIds);
        sectionIds = copyStrings(sectionIds);
        slices = copySlices(slices);
        diagnostics = diagnostics == null ? "" : diagnostics;
    }

    public static WorkspaceInvalidation frame(Reason reason, String diagnostics) {
        return new WorkspaceInvalidation(
                reason,
                Set.of(),
                Set.of(),
                Set.of(),
                EnumSet.of(WorkspaceProjectionSlice.FRAME),
                false,
                diagnostics);
    }

    public static WorkspaceInvalidation full(Reason reason, String diagnostics) {
        return new WorkspaceInvalidation(
                reason,
                Set.of(),
                Set.of(),
                Set.of(),
                WorkspaceProjectionSlice.all(),
                true,
                diagnostics);
    }

    public static WorkspaceInvalidation hotbarFrame(Reason reason, String diagnostics) {
        return new WorkspaceInvalidation(
                reason,
                Set.of(),
                Set.of(),
                Set.of(),
                EnumSet.of(WorkspaceProjectionSlice.HOTBAR, WorkspaceProjectionSlice.FRAME),
                false,
                diagnostics);
    }

    public static WorkspaceInvalidation identityLocal(
            Reason reason,
            ItemIdentity identity,
            EnumSet<WorkspaceProjectionSlice> slices,
            String diagnostics
    ) {
        return new WorkspaceInvalidation(
                reason,
                identity == null ? Set.of() : Set.of(identity),
                Set.of(),
                Set.of(),
                slices,
                true,
                diagnostics);
    }

    public static WorkspaceInvalidation localizedIdentity(
            Reason reason,
            ItemIdentity identity,
            EnumSet<WorkspaceProjectionSlice> slices,
            String diagnostics
    ) {
        return new WorkspaceInvalidation(
                reason,
                identity == null ? Set.of() : Set.of(identity),
                Set.of(),
                Set.of(),
                slices,
                false,
                diagnostics);
    }

    public static WorkspaceInvalidation storageLocal(
            Reason reason,
            String storageId,
            EnumSet<WorkspaceProjectionSlice> slices,
            String diagnostics
    ) {
        return new WorkspaceInvalidation(
                reason,
                Set.of(),
                storageId == null || storageId.isBlank() ? Set.of() : Set.of(storageId),
                Set.of(),
                slices,
                true,
                diagnostics);
    }

    public static WorkspaceInvalidation localizedStorage(
            Reason reason,
            String storageId,
            EnumSet<WorkspaceProjectionSlice> slices,
            String diagnostics
    ) {
        return new WorkspaceInvalidation(
                reason,
                Set.of(),
                storageId == null || storageId.isBlank() ? Set.of() : Set.of(storageId),
                Set.of(),
                slices,
                false,
                diagnostics);
    }

    public enum Reason {
        SESSION_OPEN,
        COMMAND_OUTCOME,
        MENU_SLOT_CHANGED,
        CARRIED_REVISION_CHANGED,
        WORKFLOW_SEQUENCE_CHANGED,
        WORKFLOW_METADATA_CHANGED,
        PROXIMITY_CHANGED,
        SEARCH_QUERY_CHANGED,
        REMOTE_STORAGE_DETAIL_CHANGED,
        AUTO_HOME_REPROJECTED,
        PARITY_ORACLE,
        UNKNOWN
    }

    private static Set<ItemIdentity> copyIdentities(Collection<ItemIdentity> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> copy = new LinkedHashSet<>();
        for (ItemIdentity value : values) {
            if (value != null) {
                copy.add(value);
            }
        }
        return copy.isEmpty() ? Set.of() : Set.copyOf(copy);
    }

    private static Set<String> copyStrings(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                copy.add(value);
            }
        }
        return copy.isEmpty() ? Set.of() : Set.copyOf(copy);
    }

    private static EnumSet<WorkspaceProjectionSlice> copySlices(Collection<WorkspaceProjectionSlice> values) {
        EnumSet<WorkspaceProjectionSlice> copy = WorkspaceProjectionSlice.none();
        if (values != null) {
            for (WorkspaceProjectionSlice value : values) {
                if (value != null) {
                    copy.add(value);
                }
            }
        }
        return copy;
    }
}
