package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WorkspaceAuthorityInvalidations {
    private static final EnumSet<WorkspaceProjectionSlice> CARRIED_IDENTITY_SLICES = EnumSet.of(
            WorkspaceProjectionSlice.CARD,
            WorkspaceProjectionSlice.SECTION,
            WorkspaceProjectionSlice.WAYFINDING,
            WorkspaceProjectionSlice.DEPOSITABILITY,
            WorkspaceProjectionSlice.HOTBAR,
            WorkspaceProjectionSlice.WORKFLOW,
            WorkspaceProjectionSlice.CONTEXTUAL,
            WorkspaceProjectionSlice.FRAME);

    private WorkspaceAuthorityInvalidations() {
    }

    public static List<WorkspaceInvalidation> localizeCarriedRevisionInvalidations(
            InventoryAuthoritySnapshot previousAuthority,
            InventoryAuthoritySnapshot currentAuthority,
            List<WorkspaceInvalidation> invalidations
    ) {
        if (invalidations == null || invalidations.isEmpty()) {
            return List.of();
        }
        ArrayList<WorkspaceInvalidation> resolved = new ArrayList<>(invalidations.size());
        CarriedDiffResult carriedDiff = null;
        for (WorkspaceInvalidation invalidation : invalidations) {
            if (invalidation == null) {
                continue;
            }
            if (!localizableCarriedRevision(invalidation)) {
                resolved.add(invalidation);
                continue;
            }
            if (carriedDiff == null) {
                carriedDiff = diffCarriedIdentities(previousAuthority, currentAuthority);
            }
            resolved.add(carriedDiff.toInvalidation(invalidation));
        }
        return resolved.isEmpty() ? List.of() : List.copyOf(resolved);
    }

    private static boolean localizableCarriedRevision(WorkspaceInvalidation invalidation) {
        return invalidation != null
                && invalidation.reason() == WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED
                && invalidation.requiresFullProjection();
    }

    private static CarriedDiffResult diffCarriedIdentities(
            InventoryAuthoritySnapshot previousAuthority,
            InventoryAuthoritySnapshot currentAuthority
    ) {
        if (previousAuthority == null || currentAuthority == null
                || previousAuthority.host() == null || currentAuthority.host() == null) {
            return CarriedDiffResult.unbounded("carried_authority_snapshot_missing");
        }
        List<SourceShape> previousSources = carriedSourceShapes(previousAuthority);
        List<SourceShape> currentSources = carriedSourceShapes(currentAuthority);
        if (previousSources == null || currentSources == null) {
            return CarriedDiffResult.unbounded("carried_source_snapshot_missing");
        }
        if (!previousSources.equals(currentSources)) {
            return CarriedDiffResult.unbounded("carried_source_shape_changed");
        }

        LinkedHashSet<ItemIdentity> affected = new LinkedHashSet<>();
        try {
            for (SourceShape source : previousSources) {
                InventorySourceSnapshot previousSource = previousAuthority.sourceSnapshot(source.id());
                InventorySourceSnapshot currentSource = currentAuthority.sourceSnapshot(source.id());
                if (previousSource == null || currentSource == null) {
                    return CarriedDiffResult.unbounded("carried_source_snapshot_missing");
                }
                collectSourceDiff(affected, previousSource, currentSource);
            }
        } catch (RuntimeException ex) {
            return CarriedDiffResult.unbounded("carried_identity_resolution_failed");
        }
        return CarriedDiffResult.bounded(affected);
    }

    private static List<SourceShape> carriedSourceShapes(InventoryAuthoritySnapshot authority) {
        if (authority == null || authority.host() == null) {
            return null;
        }
        ArrayList<SourceShape> shapes = new ArrayList<>();
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            InventorySourceSnapshot snapshot = authority.sourceSnapshot(source.id());
            if (snapshot == null) {
                return null;
            }
            shapes.add(new SourceShape(
                    source.id(),
                    source.domain(),
                    source.role(),
                    source.bindingRoute(),
                    source.actionRoute(),
                    source.paneMembership(),
                    source.logicalSlotCount(),
                    snapshot.slotCapacity(),
                    source.stableOrder(),
                    source.capabilities(),
                    source.diagnostics(),
                    snapshot.diagnostics()));
        }
        return List.copyOf(shapes);
    }

    private static void collectSourceDiff(
            LinkedHashSet<ItemIdentity> affected,
            InventorySourceSnapshot previousSource,
            InventorySourceSnapshot currentSource
    ) {
        Map<String, InventoryEntrySnapshot> previousEntries = previousSource.entriesByStableKey();
        Map<String, InventoryEntrySnapshot> currentEntries = currentSource.entriesByStableKey();
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        keys.addAll(previousEntries.keySet());
        keys.addAll(currentEntries.keySet());
        for (String key : keys) {
            InventoryEntrySnapshot previousEntry = previousEntries.get(key);
            InventoryEntrySnapshot currentEntry = currentEntries.get(key);
            if (!entryChanged(previousEntry, currentEntry)) {
                continue;
            }
            ItemIdentityCollections.add(affected, entryIdentity(previousEntry));
            ItemIdentityCollections.add(affected, entryIdentity(currentEntry));
        }
    }

    private static boolean entryChanged(InventoryEntrySnapshot previousEntry, InventoryEntrySnapshot currentEntry) {
        boolean previousPresent = previousEntry != null && previousEntry.present();
        boolean currentPresent = currentEntry != null && currentEntry.present();
        if (previousPresent != currentPresent) {
            return true;
        }
        if (!previousPresent) {
            return false;
        }
        ItemIdentity previousIdentity = entryIdentity(previousEntry);
        ItemIdentity currentIdentity = entryIdentity(currentEntry);
        return previousEntry.count() != currentEntry.count()
                || !previousIdentity.equals(currentIdentity)
                || !previousEntry.diagnostics().equals(currentEntry.diagnostics());
    }

    private static ItemIdentity entryIdentity(InventoryEntrySnapshot entry) {
        if (entry == null || !entry.present()) {
            return null;
        }
        return ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(entry.stack()));
    }

    private record SourceShape(
            String id,
            InventorySourceDomain domain,
            InventorySourceRole role,
            InventoryBindingRoute bindingRoute,
            InventoryActionRoute actionRoute,
            InventoryPaneMembership paneMembership,
            int logicalSlotCount,
            int slotCapacity,
            int stableOrder,
            Set<InventoryCapability> capabilities,
            String sourceDiagnostics,
            String snapshotDiagnostics
    ) {
        private SourceShape {
            id = id == null ? "" : id;
            capabilities = capabilities == null || capabilities.isEmpty()
                    ? Set.of()
                    : Set.copyOf(capabilities);
            sourceDiagnostics = sourceDiagnostics == null ? "" : sourceDiagnostics;
            snapshotDiagnostics = snapshotDiagnostics == null ? "" : snapshotDiagnostics;
        }
    }

    private record CarriedDiffResult(boolean bounded, Set<ItemIdentity> identities, String diagnostics) {
        private static CarriedDiffResult bounded(Set<ItemIdentity> identities) {
            Set<ItemIdentity> normalized = ItemIdentityCollections.normalizedSet(identities);
            return new CarriedDiffResult(
                    true,
                    normalized,
                    normalized.isEmpty()
                            ? "carried_revision_no_identity_delta"
                            : "carried_revision_identity_diff");
        }

        private static CarriedDiffResult unbounded(String diagnostics) {
            return new CarriedDiffResult(
                    false,
                    Set.of(),
                    diagnostics == null || diagnostics.isBlank() ? "carried_authority_diff_unbounded" : diagnostics);
        }

        private WorkspaceInvalidation toInvalidation(WorkspaceInvalidation original) {
            if (!bounded) {
                return original;
            }
            if (identities.isEmpty()) {
                return WorkspaceInvalidation.frame(
                        WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED,
                        diagnostics);
            }
            return new WorkspaceInvalidation(
                    WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED,
                    identities,
                    Set.of(),
                    Set.of(),
                    CARRIED_IDENTITY_SLICES,
                    false,
                    diagnostics);
        }
    }
}
