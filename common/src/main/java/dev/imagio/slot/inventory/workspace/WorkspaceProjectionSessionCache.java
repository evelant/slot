package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackTags;
import dev.imagio.slot.inventory.query.CarriedIdentityCounts;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.workflow.domain.ChestClusterMap;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowProjection;
import dev.imagio.slot.workflow.domain.WorkflowTabTargets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WorkspaceProjectionSessionCache {
    private final WorkspaceProjectionEngine engine;
    private final ItemIdentityMatcher.Memo identityMemo = new ItemIdentityMatcher.Memo();

    private String lastStructuralKey = "";
    private WorkspaceProjectionFrame lastFrame;
    private SlotWorkspaceViewModel lastStructuralView;
    private SlotWorkspaceViewModel lastView;
    private WorkspaceProjectionStore lastStore = WorkspaceProjectionStore.empty();
    private WorkspaceCardProjector.State lastCardProjection;
    private WorkspaceStorageProjector.State lastStorageProjection;
    private WorkspaceEdgeProjector.State lastEdgeProjection;
    private WorkspaceViewSliceKeys lastProjectionSliceKeys;
    private WorkspaceProjectedSlices lastProjectionSlices;
    private String lastContentFingerprint = "";
    private long projectionCount;
    private long structuralHits;
    private long structuralMisses;

    public WorkspaceProjectionSessionCache() {
        this(new WorkspaceProjectionEngine());
    }

    WorkspaceProjectionSessionCache(WorkspaceProjectionEngine engine) {
        this.engine = engine == null ? new WorkspaceProjectionEngine() : engine;
    }

    public WorkspaceProjectionResult project(WorkspaceProjectionRequest request) {
        return project(request, (Collection<WorkspaceInvalidation>) null);
    }

    public WorkspaceProjectionResult project(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidation invalidation
    ) {
        return project(request, invalidation == null ? null : java.util.List.of(invalidation));
    }

    public WorkspaceProjectionResult project(
            WorkspaceProjectionRequest request,
            Collection<WorkspaceInvalidation> invalidations
    ) {
        long totalStart = System.nanoTime();
        WorkspaceInvalidationSummary invalidationSummary = WorkspaceInvalidationSummary.coalesce(invalidations);
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        projectionCount++;
        WorkspaceProjectionFrame frame = resolved.frame();
        long inputStart = System.nanoTime();
        String structuralKey = WorkspaceProjectionFingerprint.inputKey(resolved, identityMemo);
        long inputNanos = System.nanoTime() - inputStart;
        boolean structuralKeyHit = lastStructuralView != null && structuralKey.equals(lastStructuralKey);
        boolean structuralHit = structuralKeyHit && !invalidationRequiresStructuralRefresh(invalidationSummary);
        SlotWorkspaceViewModel projected;
        long projectNanos = 0L;
        boolean fullProjectionRan = false;
        long localizedFactsUpdated = 0L;
        if (structuralHit) {
            structuralHits++;
            projected = lastStructuralView.withFrame(
                    frame.revision(),
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot());
        } else {
            structuralMisses++;
            LocalizedProjection localized = projectLocalized(resolved, frame, invalidationSummary);
            if (localized.applied()) {
                lastStore = localized.store();
                projected = localized.viewModel();
                localizedFactsUpdated = localized.factsUpdated();
            } else {
                fullProjectionRan = true;
                long projectStart = System.nanoTime();
                projected = engine.project(resolved.withFrame(new WorkspaceProjectionFrame(
                        frame.status(),
                        frame.diagnostics(),
                        frame.pendingCount(),
                        frame.selectedQuickAccessSlot(),
                        0L)), identityMemo);
                projectNanos = System.nanoTime() - projectStart;
                lastStore = engine.lastStore();
            }
            lastStructuralKey = structuralKey;
            lastStructuralView = projected.withFrame(
                    0L,
                    frame.status(),
                    frame.diagnostics(),
                    frame.pendingCount(),
                    frame.selectedQuickAccessSlot());
        }

        CardReuse cardReuse = reuseCards(projected);
        projected = cardReuse.viewModel();

        StorageReuse storageReuse = reuseStorage(projected);
        projected = storageReuse.viewModel();

        EdgeReuse edgeReuse = reuseEdges(projected);
        projected = edgeReuse.viewModel();

        SliceReuse sliceReuse = reuseProjectionSlices(projected);
        projected = sliceReuse.viewModel();

        long contentNanos = 0L;
        String contentFingerprint;
        if (structuralHit && frame.equals(lastFrame) && lastView != null) {
            contentFingerprint = lastContentFingerprint;
        } else {
            long contentStart = System.nanoTime();
            contentFingerprint = WorkspaceProjectionFingerprint.contentKey(projected);
            contentNanos = System.nanoTime() - contentStart;
        }
        lastFrame = frame;
        lastView = projected;
        lastContentFingerprint = contentFingerprint;
        int projectedFactCount = Math.max(projectionFactCount(projected), lastStore.factCount());
        long factsUpdated = structuralHit
                ? 0L
                : fullProjectionRan
                        ? projectedFactCount
                        : Math.min(Math.max(0L, localizedFactsUpdated), projectedFactCount);
        long factsReused = structuralHit
                ? projectedFactCount
                : fullProjectionRan
                        ? 0L
                        : Math.max(0L, projectedFactCount - factsUpdated);
        String fullProjectionReason = fullProjectionRan ? fullProjectionReason(invalidationSummary) : "";
        WorkspaceProjectionTiming timing = new WorkspaceProjectionTiming(
                inputNanos,
                projectNanos,
                contentNanos,
                System.nanoTime() - totalStart);
        return new WorkspaceProjectionResult(
                projected,
                contentFingerprint,
                diagnostics(
                        structuralHit,
                        timing,
                        invalidationSummary,
                        fullProjectionReason,
                        factsUpdated,
                        factsReused,
                        cardReuse.stats(),
                        storageReuse.stats(),
                        edgeReuse.stats(),
                        sliceReuse.stats()));
    }

    public void clear() {
        lastStructuralKey = "";
        lastFrame = null;
        lastStructuralView = null;
        lastView = null;
        lastStore = WorkspaceProjectionStore.empty();
        lastCardProjection = null;
        lastStorageProjection = null;
        lastEdgeProjection = null;
        lastProjectionSliceKeys = null;
        lastProjectionSlices = null;
        lastContentFingerprint = "";
    }

    public Diagnostics diagnostics() {
        return diagnostics(lastStructuralView != null && lastView != null, WorkspaceProjectionTiming.empty());
    }

    private Diagnostics diagnostics(boolean structuralHit) {
        return diagnostics(structuralHit, WorkspaceProjectionTiming.empty());
    }

    private Diagnostics diagnostics(boolean structuralHit, WorkspaceProjectionTiming timing) {
        return diagnostics(
                structuralHit,
                timing,
                WorkspaceInvalidationSummary.empty(),
                "",
                0L,
                0L,
                WorkspaceCardProjectionStats.empty(),
                WorkspaceStorageProjectionStats.empty(),
                WorkspaceEdgeProjectionStats.empty(),
                WorkspaceProjectionSliceStats.empty());
    }

    private Diagnostics diagnostics(
            boolean structuralHit,
            WorkspaceProjectionTiming timing,
            WorkspaceInvalidationSummary invalidations,
            String fullProjectionReason,
            long projectionFactsUpdated,
            long projectionFactsReused,
            WorkspaceCardProjectionStats cardProjectionStats,
            WorkspaceStorageProjectionStats storageProjectionStats,
            WorkspaceEdgeProjectionStats edgeProjectionStats,
            WorkspaceProjectionSliceStats projectionSliceStats
    ) {
        return new Diagnostics(
                projectionCount,
                structuralHits,
                structuralMisses,
                structuralHit,
                identityMemo.stats(),
                timing,
                invalidations,
                fullProjectionReason,
                projectionFactsUpdated,
                projectionFactsReused,
                cardProjectionStats,
                storageProjectionStats,
                edgeProjectionStats,
                projectionSliceStats);
    }

    private CardReuse reuseCards(SlotWorkspaceViewModel projected) {
        WorkspaceCardProjector.Result result = WorkspaceCardProjector.project(projected, lastCardProjection);
        lastCardProjection = result.state();
        return new CardReuse(result.viewModel(), result.stats());
    }

    private StorageReuse reuseStorage(SlotWorkspaceViewModel projected) {
        WorkspaceStorageProjector.Result result = WorkspaceStorageProjector.project(projected, lastStorageProjection);
        lastStorageProjection = result.state();
        return new StorageReuse(result.viewModel(), result.stats());
    }

    private EdgeReuse reuseEdges(SlotWorkspaceViewModel projected) {
        WorkspaceEdgeProjector.Result result = WorkspaceEdgeProjector.project(projected, lastEdgeProjection);
        lastEdgeProjection = result.state();
        return new EdgeReuse(result.viewModel(), result.stats());
    }

    private SliceReuse reuseProjectionSlices(SlotWorkspaceViewModel projected) {
        SlotWorkspaceViewModel resolved = projected == null ? SlotWorkspaceViewModel.empty() : projected;
        WorkspaceViewSliceKeys nextKeys = WorkspaceViewSliceKeys.from(resolved);
        WorkspaceProjectedSlices nextSlices = WorkspaceProjectedSlices.from(resolved);
        WorkspaceProjectedSlices.ReuseResult reuse = nextSlices.reuseAgainst(
                lastProjectionSliceKeys,
                lastProjectionSlices,
                nextKeys);
        SlotWorkspaceViewModel composed = reuse.slices().compose(resolved.revision());
        lastProjectionSliceKeys = nextKeys;
        lastProjectionSlices = reuse.slices();
        return new SliceReuse(composed, reuse.stats());
    }

    private LocalizedProjection projectLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        LocalizedProjection remoteDetail = projectSimpleRemoteDetailLocalized(request, frame, invalidations);
        if (remoteDetail.applied()) {
            return remoteDetail;
        }
        LocalizedProjection panel = projectPanelLocalized(request, frame, invalidations);
        if (panel.applied()) {
            return panel;
        }
        LocalizedProjection simpleSectionMetadata = projectSimpleSectionMetadataLocalized(request, frame, invalidations);
        if (simpleSectionMetadata.applied()) {
            return simpleSectionMetadata;
        }
        LocalizedProjection simpleWorkflow = projectSimpleWorkflowLocalized(request, frame, invalidations);
        if (simpleWorkflow.applied()) {
            return simpleWorkflow;
        }
        LocalizedProjection simpleCraftRun = projectSimpleCraftRunLocalized(request, frame, invalidations);
        if (simpleCraftRun.applied()) {
            return simpleCraftRun;
        }
        LocalizedProjection simpleCarriedStorage = projectSimpleCarriedAndStorageLocalized(request, frame, invalidations);
        if (simpleCarriedStorage.applied()) {
            return simpleCarriedStorage;
        }
        LocalizedProjection simpleWorkflowStoragePresence =
                projectSimpleWorkflowStoragePresenceLocalized(request, frame, invalidations);
        if (simpleWorkflowStoragePresence.applied()) {
            return simpleWorkflowStoragePresence;
        }
        LocalizedProjection simpleStoragePresence = projectSimpleStoragePresenceLocalized(request, frame, invalidations);
        if (simpleStoragePresence.applied()) {
            return simpleStoragePresence;
        }
        LocalizedProjection simpleCarried = projectSimpleCarriedIdentityLocalized(request, frame, invalidations);
        if (simpleCarried.applied()) {
            return simpleCarried;
        }
        LocalizedProjection simpleStorage = projectSimpleStorageLocalized(request, frame, invalidations);
        if (simpleStorage.applied()) {
            return simpleStorage;
        }
        if (!canProjectHotbarFrameOnly(invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        WorkspaceProjectionFrame resolvedFrame = frame == null
                ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                : frame;
        SlotWorkspaceViewModel base = lastStructuralView.withHotbar(
                SlotWorkspaceViewModel.projectHotbarSlots(
                        request == null ? null : request.authority(),
                        resolvedFrame.selectedQuickAccessSlot()),
                SlotWorkspaceViewModel.projectOffhand(request == null ? null : request.authority()),
                SlotWorkspaceViewModel.projectRecentIdentityRefs(request == null ? null : request.workflow()));
        SlotWorkspaceViewModel projected = base.withFrame(
                resolvedFrame.revision(),
                resolvedFrame.status(),
                resolvedFrame.diagnostics(),
                resolvedFrame.pendingCount(),
                resolvedFrame.selectedQuickAccessSlot());
        long factsUpdated = Math.max(1, projected.hotbarSlots().size())
                + (projected.offhand() == null ? 0 : 1)
                + projected.recentIdentities().size();
        return new LocalizedProjection(true, projected, storeUpdate.store(), factsUpdated + storeUpdate.factsUpdated());
    }

    private LocalizedProjection projectSimpleCarriedIdentityLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleCarriedIdentity(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        if (!storeUpdate.localized()) {
            return LocalizedProjection.notApplied();
        }
        SlotWorkspaceViewModel projected = applySimpleCarriedIdentityProjection(
                lastStructuralView,
                storeUpdate.store(),
                invalidations.identities(),
                request,
                frame);
        if (projected == null) {
            return LocalizedProjection.notApplied();
        }
        long factsUpdated = storeUpdate.factsUpdated() + invalidations.identities().size();
        if (invalidations.slices().contains(WorkspaceProjectionSlice.HOTBAR)) {
            factsUpdated += Math.max(1, projected.hotbarSlots().size())
                    + (projected.offhand() == null ? 0 : 1)
                    + projected.recentIdentities().size();
        }
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, storeUpdate.store(), factsUpdated);
    }

    private LocalizedProjection projectSimpleCarriedAndStorageLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleCarriedAndStorage(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        if (!storeUpdate.localized()) {
            return LocalizedProjection.notApplied();
        }
        SlotWorkspaceViewModel withCards = applySimpleCarriedIdentityProjection(
                lastStructuralView,
                storeUpdate.store(),
                invalidations.identities(),
                request,
                frame);
        if (withCards == null) {
            if (!simpleTrackedXrayNoVisibleCardChange(request, invalidations)) {
                return LocalizedProjection.notApplied();
            }
            WorkspaceProjectionFrame resolvedFrame = frame == null
                    ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                    : frame;
            withCards = lastStructuralView.withFrame(
                    resolvedFrame.revision(),
                    resolvedFrame.status(),
                    resolvedFrame.diagnostics(),
                    resolvedFrame.pendingCount(),
                    resolvedFrame.selectedQuickAccessSlot());
        }
        SlotWorkspaceViewModel projected = applySimpleStorageProjection(
                withCards,
                storeUpdate.store(),
                invalidations.storageIds(),
                request,
                frame);
        if (projected == null) {
            if (!simpleTrackedXrayNoVisibleCardChange(request, invalidations)) {
                return LocalizedProjection.notApplied();
            }
            projected = withCards;
        }
        long factsUpdated = storeUpdate.factsUpdated()
                + invalidations.identities().size()
                + invalidations.storageIds().size();
        if (invalidations.slices().contains(WorkspaceProjectionSlice.HOTBAR)) {
            factsUpdated += Math.max(1, projected.hotbarSlots().size())
                    + (projected.offhand() == null ? 0 : 1)
                    + projected.recentIdentities().size();
        }
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, storeUpdate.store(), factsUpdated);
    }

    private static boolean simpleTrackedXrayNoVisibleCardChange(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        return simpleTrackedXray(request)
                && !summary.identities().isEmpty()
                && !summary.storageIds().isEmpty()
                && summary.slices().contains(WorkspaceProjectionSlice.REMOTE_SEARCH);
    }

    private LocalizedProjection projectSimpleStoragePresenceLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleStoragePresence(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore previousStore = lastStore;
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        if (!storeUpdate.localized()) {
            return LocalizedProjection.notApplied();
        }
        Set<ItemIdentity> affectedIdentities = storageAffectedIdentities(
                previousStore,
                storeUpdate.store(),
                request,
                invalidations,
                lastStructuralView);
        if (!affectedIdentities.isEmpty()) {
            WorkspaceInvalidationSummary expandedInvalidations =
                    storageInvalidationsWithAffectedIdentities(
                            invalidations,
                            affectedIdentities,
                            "storage_presence_identity_expanded");
            storeUpdate = previousStore.updateFrom(request, expandedInvalidations);
            if (!storeUpdate.localized()) {
                return LocalizedProjection.notApplied();
            }
        }
        SlotWorkspaceViewModel base = lastStructuralView;
        if (!affectedIdentities.isEmpty()) {
            SlotWorkspaceViewModel withCards = applySimpleCarriedIdentityProjection(
                    base,
                    storeUpdate.store(),
                    affectedIdentities,
                    request,
                    frame);
            if (withCards != null) {
                base = withCards;
            }
        }
        SlotWorkspaceViewModel projected = applySimpleStorageProjection(
                base,
                storeUpdate.store(),
                invalidations.storageIds(),
                request,
                frame);
        if (projected == null) {
            return LocalizedProjection.notApplied();
        }
        long factsUpdated = storeUpdate.factsUpdated()
                + invalidations.storageIds().size()
                + affectedIdentities.size();
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, storeUpdate.store(), factsUpdated);
    }

    private LocalizedProjection projectSimpleWorkflowStoragePresenceLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleWorkflowStoragePresence(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore previousStore = lastStore;
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        if (!storeUpdate.localized()) {
            return LocalizedProjection.notApplied();
        }
        Set<ItemIdentity> affectedIdentities = simpleWorkflowStorageAffectedIdentities(
                previousStore,
                storeUpdate.store(),
                request,
                invalidations,
                lastStructuralView);
        if (!affectedIdentities.isEmpty()) {
            WorkspaceInvalidationSummary expandedInvalidations =
                    storageInvalidationsWithAffectedIdentities(
                            invalidations,
                            affectedIdentities,
                            "workflow_storage_identity_expanded");
            storeUpdate = previousStore.updateFrom(request, expandedInvalidations);
            if (!storeUpdate.localized()) {
                return LocalizedProjection.notApplied();
            }
        }
        SlotWorkspaceViewModel base = lastStructuralView;
        if (!affectedIdentities.isEmpty()) {
            SlotWorkspaceViewModel withCards = applySimpleCarriedIdentityProjection(
                    base,
                    storeUpdate.store(),
                    affectedIdentities,
                    request,
                    frame,
                    false,
                    true);
            if (withCards == null) {
                return LocalizedProjection.notApplied();
            }
            base = withCards;
        }
        SlotWorkspaceViewModel projected = applySimpleStorageProjection(
                base,
                storeUpdate.store(),
                invalidations.storageIds(),
                request,
                frame);
        if (projected == null) {
            return LocalizedProjection.notApplied();
        }
        long factsUpdated = storeUpdate.factsUpdated()
                + invalidations.storageIds().size()
                + affectedIdentities.size();
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, storeUpdate.store(), factsUpdated);
    }

    private LocalizedProjection projectSimpleWorkflowLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleWorkflow(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        Set<ItemIdentity> affectedIdentities =
                simpleWorkflowAffectedIdentities(request, invalidations, lastStructuralView);
        WorkspaceInvalidationSummary expandedInvalidations =
                simpleWorkflowInvalidationsWithAffectedIdentities(invalidations, affectedIdentities);
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, expandedInvalidations);
        if (!storeUpdate.localized() && !affectedIdentities.isEmpty()) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore store = storeUpdate.localized() ? storeUpdate.store() : lastStore;
        SlotWorkspaceViewModel projected = applySimpleCarriedIdentityProjection(
                lastStructuralView,
                store,
                affectedIdentities,
                request,
                frame,
                false,
                true);
        if (projected == null) {
            WorkspaceProjectionFrame resolvedFrame = frame == null
                    ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                    : frame;
            projected = lastStructuralView.withFrame(
                    resolvedFrame.revision(),
                    resolvedFrame.status(),
                    resolvedFrame.diagnostics(),
                    resolvedFrame.pendingCount(),
                    resolvedFrame.selectedQuickAccessSlot());
        }
        projected = projected.withKits(SlotWorkspaceViewModel.projectSimpleKitCards(
                store,
                request == null ? WorkflowDomainSnapshot.empty() : request.workflow()));
        long factsUpdated = storeUpdate.factsUpdated() + affectedIdentities.size() + 1L;
        if (expandedInvalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, store, factsUpdated);
    }

    private LocalizedProjection projectSimpleCraftRunLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleCraftRun(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        if (!storeUpdate.localized() && !simpleCraftRunAllowsNoFactUpdate(request, invalidations)) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore store = storeUpdate.localized() ? storeUpdate.store() : lastStore;
        SlotWorkspaceViewModel projected = applySimpleCarriedIdentityProjection(
                lastStructuralView,
                store,
                invalidations.identities(),
                request,
                frame,
                true);
        if (projected == null) {
            WorkspaceProjectionFrame resolvedFrame = frame == null
                    ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                    : frame;
            projected = lastStructuralView.withFrame(
                    resolvedFrame.revision(),
                    resolvedFrame.status(),
                    resolvedFrame.diagnostics(),
                    resolvedFrame.pendingCount(),
                    resolvedFrame.selectedQuickAccessSlot());
        }
        CraftRunState craftRun = request == null
                ? CraftRunState.empty()
                : request.workflow().craftRun();
        projected = projected.withCraftRun(craftRun);
        long factsUpdated = storeUpdate.factsUpdated() + invalidations.identities().size() + 1L;
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, store, factsUpdated);
    }

    private LocalizedProjection projectSimpleSectionMetadataLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleSectionMetadata(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        SlotWorkspaceViewModel projected = applySimpleSectionMetadataProjection(
                lastStructuralView,
                request,
                frame);
        if (projected == null) {
            return LocalizedProjection.notApplied();
        }
        long factsUpdated = Math.max(1, invalidations == null ? 0 : invalidations.sectionIds().size());
        if (invalidations != null && invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, lastStore, factsUpdated);
    }

    private static boolean simpleCraftRunAllowsNoFactUpdate(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        return request != null
                && simpleCraftRunRequest(request)
                && summary.storageIds().isEmpty()
                && summary.slices().contains(WorkspaceProjectionSlice.WORKFLOW);
    }

    private LocalizedProjection projectSimpleStorageLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleStorage(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionStore.UpdateResult storeUpdate = lastStore.updateFrom(request, invalidations);
        if (!storeUpdate.localized()) {
            return LocalizedProjection.notApplied();
        }
        SlotWorkspaceViewModel projected = applySimpleStorageProjection(
                lastStructuralView,
                storeUpdate.store(),
                invalidations.storageIds(),
                request,
                frame);
        if (projected == null) {
            return LocalizedProjection.notApplied();
        }
        long factsUpdated = storeUpdate.factsUpdated() + invalidations.storageIds().size();
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, storeUpdate.store(), factsUpdated);
    }

    private static WorkspaceInvalidationSummary simpleWorkflowInvalidationsWithAffectedIdentities(
            WorkspaceInvalidationSummary invalidations,
            Set<ItemIdentity> affectedIdentities
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (affectedIdentities == null || affectedIdentities.isEmpty()) {
            return summary;
        }
        LinkedHashSet<ItemIdentity> expanded = new LinkedHashSet<>(summary.identities());
        for (ItemIdentity identity : affectedIdentities) {
            ItemIdentityCollections.add(expanded, identity);
        }
        if (expanded.size() == summary.identities().size()) {
            return summary;
        }
        ArrayList<WorkspaceInvalidation> merged = new ArrayList<>(summary.invalidations());
        merged.add(new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                expanded,
                Set.of(),
                Set.of(),
                summary.slices().isEmpty()
                        ? EnumSet.of(
                                WorkspaceProjectionSlice.CARD,
                                WorkspaceProjectionSlice.SECTION,
                                WorkspaceProjectionSlice.WORKFLOW,
                                WorkspaceProjectionSlice.CONTEXTUAL,
                                WorkspaceProjectionSlice.WAYFINDING,
                                WorkspaceProjectionSlice.FRAME)
                        : summary.slices(),
                false,
                "workflow_accepted_input_tag_expanded"));
        return WorkspaceInvalidationSummary.coalesce(merged);
    }

    private static WorkspaceInvalidationSummary storageInvalidationsWithAffectedIdentities(
            WorkspaceInvalidationSummary invalidations,
            Set<ItemIdentity> affectedIdentities,
            String diagnostics
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (affectedIdentities == null || affectedIdentities.isEmpty()) {
            return summary;
        }
        LinkedHashSet<ItemIdentity> expanded = new LinkedHashSet<>(summary.identities());
        for (ItemIdentity identity : affectedIdentities) {
            ItemIdentityCollections.add(expanded, identity);
        }
        if (expanded.size() == summary.identities().size()) {
            return summary;
        }
        ArrayList<WorkspaceInvalidation> merged = new ArrayList<>(summary.invalidations());
        merged.add(new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                expanded,
                summary.storageIds(),
                Set.of(),
                summary.slices().isEmpty()
                        ? EnumSet.of(
                                WorkspaceProjectionSlice.CARD,
                                WorkspaceProjectionSlice.SECTION,
                                WorkspaceProjectionSlice.STORAGE,
                                WorkspaceProjectionSlice.WAYFINDING,
                                WorkspaceProjectionSlice.DEPOSITABILITY,
                                WorkspaceProjectionSlice.WORKFLOW,
                                WorkspaceProjectionSlice.CONTEXTUAL,
                                WorkspaceProjectionSlice.FRAME)
                        : summary.slices(),
                false,
                diagnostics == null || diagnostics.isBlank()
                        ? "storage_identity_expanded"
                        : diagnostics));
        return WorkspaceInvalidationSummary.coalesce(merged);
    }

    private static Set<ItemIdentity> simpleWorkflowAffectedIdentities(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations,
            SlotWorkspaceViewModel previousView
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (ItemIdentity identity : summary.identities()) {
            ItemIdentityCollections.add(identities, identity);
        }
        if (simpleWorkflowShouldExpandAcceptedTags(request, summary, previousView)) {
            addAcceptedTagEvidenceIdentities(identities, request);
            addPreviousAcceptedWorkflowInputCards(identities, previousView);
        }
        return identities.isEmpty() ? Set.of() : Set.copyOf(identities);
    }

    private static boolean simpleWorkflowAllowsEmptyAffectedCards(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations,
            SlotWorkspaceViewModel previousView
    ) {
        return simpleWorkflowShouldExpandAcceptedTags(request, invalidations, previousView)
                || simpleWorkflowAcceptedInputTagInvalidation(invalidations)
                || simpleWorkflowMetadataInvalidation(invalidations);
    }

    private static boolean simpleWorkflowShouldExpandAcceptedTags(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations,
            SlotWorkspaceViewModel previousView
    ) {
        if (!simpleWorkflowSequenceInvalidation(invalidations)) {
            return false;
        }
        return activeWorkflowHasAcceptedTagInput(request == null ? null : request.workflow())
                || previousViewHasAcceptedWorkflowInput(previousView)
                || simpleWorkflowAcceptedInputTagInvalidation(invalidations);
    }

    private static boolean simpleWorkflowSequenceInvalidation(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        for (WorkspaceInvalidation invalidation : summary.invalidations()) {
            if (invalidation != null
                    && invalidation.reason() == WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED) {
                return true;
            }
        }
        return false;
    }

    private static boolean simpleWorkflowAcceptedInputTagInvalidation(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        for (WorkspaceInvalidation invalidation : summary.invalidations()) {
            if (invalidation != null
                    && invalidation.reason() == WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED
                    && invalidation.diagnostics().startsWith("workflow_accepted_input_tag_")) {
                return true;
            }
        }
        return false;
    }

    private static boolean simpleWorkflowMetadataInvalidation(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        for (WorkspaceInvalidation invalidation : summary.invalidations()) {
            if (invalidation == null
                    || invalidation.reason() != WorkspaceInvalidation.Reason.WORKFLOW_METADATA_CHANGED) {
                continue;
            }
            EnumSet<WorkspaceProjectionSlice> slices = invalidation.slices();
            if (slices == null || slices.isEmpty()) {
                return false;
            }
            for (WorkspaceProjectionSlice slice : slices) {
                if (slice != WorkspaceProjectionSlice.WORKFLOW
                        && slice != WorkspaceProjectionSlice.FRAME) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean activeWorkflowHasAcceptedTagInput(WorkflowDomainSnapshot workflow) {
        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                CarriedIdentityCounts.empty(),
                workflow == null ? WorkflowDomainSnapshot.empty() : workflow);
        for (WorkflowAcceptedInputRule rule : targets.acceptedInputs()) {
            if (rule != null && rule.itemTag() && !rule.tagId().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static boolean previousViewHasAcceptedWorkflowInput(SlotWorkspaceViewModel previousView) {
        if (previousView == null) {
            return false;
        }
        return cardsHaveAcceptedWorkflowInput(previousView.atlasItems())
                || cardsHaveAcceptedWorkflowInput(previousView.triageItems());
    }

    private static boolean cardsHaveAcceptedWorkflowInput(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && item.acceptedWorkflowInput()) {
                return true;
            }
        }
        return false;
    }

    private static void addPreviousAcceptedWorkflowInputCards(
            Set<ItemIdentity> identities,
            SlotWorkspaceViewModel previousView
    ) {
        if (identities == null || previousView == null) {
            return;
        }
        addPreviousAcceptedWorkflowInputCards(identities, previousView.atlasItems());
        addPreviousAcceptedWorkflowInputCards(identities, previousView.triageItems());
    }

    private static void addPreviousAcceptedWorkflowInputCards(
            Set<ItemIdentity> identities,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        if (identities == null || items == null || items.isEmpty()) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null
                    && item.acceptedWorkflowInput()
                    && item.identity() != null) {
                ItemIdentityCollections.add(identities, item.identity().toIdentity());
            }
        }
    }

    private static void addAcceptedTagEvidenceIdentities(
            Set<ItemIdentity> identities,
            WorkspaceProjectionRequest request
    ) {
        if (identities == null || request == null) {
            return;
        }
        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                CarriedIdentityCounts.from(request.authority()),
                request.workflow());
        for (WorkflowAcceptedInputRule rule : targets.acceptedInputs()) {
            if (rule == null || !rule.itemTag() || rule.tagId().isBlank()) {
                continue;
            }
            addTaggedAuthorityIdentities(identities, request, rule.tagId());
            addTaggedStorageIdentities(identities, request, rule.tagId());
        }
    }

    private static void addTaggedAuthorityIdentities(
            Set<ItemIdentity> identities,
            WorkspaceProjectionRequest request,
            String tagId
    ) {
        if (identities == null || request == null || tagId == null || tagId.isBlank()) {
            return;
        }
        ProjectionIdentityContext context = ProjectionIdentityContext.from(request.authority());
        for (Map.Entry<ItemIdentity, net.minecraft.world.item.ItemStack> entry :
                context.displayStacksByIdentity().entrySet()) {
            if (ItemStackTags.itemTagIds(entry.getValue()).contains(tagId)) {
                ItemIdentityCollections.add(identities, entry.getKey());
            }
        }
    }

    private static void addTaggedStorageIdentities(
            Set<ItemIdentity> identities,
            WorkspaceProjectionRequest request,
            String tagId
    ) {
        if (identities == null || request == null || tagId == null || tagId.isBlank()) {
            return;
        }
        WorkspaceStorageIndex storageIndex = request.storageIndex() == null
                ? WorkspaceStorageIndex.empty()
                : request.storageIndex();
        for (WorkspaceStorageIndex.StorageEntry entry : storageIndex.entries()) {
            if (entry == null || entry.snapshot() == null) {
                continue;
            }
            for (net.minecraft.world.item.ItemStack stack : entry.snapshot().contents()) {
                if (stack == null
                        || stack.isEmpty()
                        || !ItemStackTags.itemTagIds(stack).contains(tagId)) {
                    continue;
                }
                ItemIdentityCollections.add(
                        identities,
                        ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack)));
            }
        }
    }

    private LocalizedProjection projectSimpleRemoteDetailLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectSimpleRemoteDetail(request, invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        SlotWorkspaceViewModel projected = applySimpleRemoteSearchProjection(
                lastStructuralView,
                lastStore,
                request,
                frame);
        if (projected == null) {
            return LocalizedProjection.notApplied();
        }
        long factsUpdated = Math.max(1, projected.atlasItems().size() + projected.triageItems().size());
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, lastStore, factsUpdated);
    }

    private LocalizedProjection projectPanelLocalized(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            WorkspaceInvalidationSummary invalidations
    ) {
        if (!canProjectPanelOnly(invalidations) || lastStructuralView == null) {
            return LocalizedProjection.notApplied();
        }
        WorkspaceProjectionFrame resolvedFrame = frame == null
                ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                : frame;
        SlotWorkspaceViewModel projected = lastStructuralView
                .withActiveChestPanel(request == null
                        ? SlotWorkspaceViewModel.ActiveChestPanel.empty()
                        : request.activeChestPanel())
                .withFrame(
                        resolvedFrame.revision(),
                        resolvedFrame.status(),
                        resolvedFrame.diagnostics(),
                        resolvedFrame.pendingCount(),
                        resolvedFrame.selectedQuickAccessSlot());
        long factsUpdated = 1L;
        if (invalidations.slices().contains(WorkspaceProjectionSlice.FRAME)) {
            factsUpdated++;
        }
        return new LocalizedProjection(true, projected, lastStore, factsUpdated);
    }

    private static SlotWorkspaceViewModel applySimpleStorageProjection(
            SlotWorkspaceViewModel base,
            WorkspaceProjectionStore store,
            Set<String> storageIds,
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame
    ) {
        if (base == null || store == null || storageIds == null || storageIds.isEmpty()) {
            return null;
        }
        LinkedHashSet<String> affected = new LinkedHashSet<>();
        for (String storageId : storageIds) {
            if (storageId != null && !storageId.isBlank()) {
                affected.add(storageId);
            }
        }
        if (affected.isEmpty()) {
            return null;
        }
        ArrayList<SlotWorkspaceViewModel.ChestChip> chips = new ArrayList<>(base.chestChips());
        Map<String, String> previousClusterIds = new LinkedHashMap<>();
        Map<String, Integer> previousAffinityCounts = new LinkedHashMap<>();
        Map<String, String> currentClusterIds = simpleStorageClusterIds(request);
        for (SlotWorkspaceViewModel.ChestChip chip : chips) {
            if (chip != null && !chip.storageId().isBlank()) {
                if (!chip.clusterId().isBlank()) {
                    previousClusterIds.put(chip.storageId(), chip.clusterId());
                }
                if (chip.affinityIdentities() > 0) {
                    previousAffinityCounts.put(chip.storageId(), chip.affinityIdentities());
                }
            }
        }
        List<SlotWorkspaceViewModel.ChestClusterDescriptor> chestClusters =
                simpleStorageClusterDescriptors(request);
        Map<String, Integer> clusterOrdinals = simpleClusterOrdinals(chestClusters);
        chips.removeIf(chip -> chip != null && affected.contains(chip.storageId()));
        for (String storageId : affected) {
            SlotWorkspaceViewModel.ChestChip chip = SlotWorkspaceViewModel.projectSimpleStorageChip(store, storageId);
            if (chip != null) {
                chips.add(withStorageChipChrome(
                        chip,
                        currentClusterIds.getOrDefault(storageId, previousClusterIds.get(storageId)),
                        simpleStorageAffinityCount(request, storageId, previousAffinityCounts.getOrDefault(storageId, 0))));
            }
        }
        chips.sort(Comparator
                .comparing((SlotWorkspaceViewModel.ChestChip chip) -> !chip.proximate())
                .thenComparingInt(chip -> clusterOrdinals.getOrDefault(chip.clusterId(), Integer.MAX_VALUE))
                .thenComparing(SlotWorkspaceViewModel.ChestChip::label, String.CASE_INSENSITIVE_ORDER));
        WorkspaceProjectionFrame resolvedFrame = frame == null
                ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                : frame;
        return base.withChestChipsAndClusters(List.copyOf(chips), chestClusters).withFrame(
                resolvedFrame.revision(),
                resolvedFrame.status(),
                resolvedFrame.diagnostics(),
                resolvedFrame.pendingCount(),
                resolvedFrame.selectedQuickAccessSlot());
    }

    private static SlotWorkspaceViewModel applySimpleRemoteSearchProjection(
            SlotWorkspaceViewModel base,
            WorkspaceProjectionStore store,
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame
    ) {
        if (base == null || store == null || request == null) {
            return null;
        }
        WorkflowDomainSnapshot workflow = request.workflow();
        VisualHomeMap visualHomeMap = workflow.visualHomeMap();
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> projectedIslands =
                new ArrayList<>(SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap));
        ArrayList<SlotWorkspaceViewModel.AtlasItem> atlasItems = new ArrayList<>(base.atlasItems());
        ArrayList<SlotWorkspaceViewModel.AtlasItem> triageItems = new ArrayList<>(base.triageItems());
        atlasItems.removeIf(WorkspaceProjectionSessionCache::simpleRemoteOnlyGhostCard);
        triageItems.removeIf(WorkspaceProjectionSessionCache::simpleRemoteOnlyGhostCard);
        List<SlotWorkspaceViewModel.AtlasItem> remoteCards =
                request != null && request.remoteStorageDetailIntent() == RemoteStorageDetailIntent.SEARCH
                        ? SlotWorkspaceViewModel.projectSimpleRemoteSearchGhostCards(
                                store,
                                request.searchQuery(),
                                request.trackedDisplayStorageEntries(),
                                visualHomeMap,
                                projectedIslands,
                                request.carriedContainerInfoResolver())
                        : List.of();
        for (SlotWorkspaceViewModel.AtlasItem card : remoteCards) {
            if (card == null) {
                continue;
            }
            if (SlotWorkspaceAtlasLayout.ISLAND_MISC.equals(card.islandId())) {
                ensureSimpleMiscIsland(projectedIslands);
            }
            if (!SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(card.islandId())
                    && SlotWorkspaceAtlasLayout.island(projectedIslands, card.islandId()) != null) {
                atlasItems.add(card);
            } else {
                triageItems.add(card);
            }
        }
        atlasItems.sort(simpleAtlasCardComparator(visualHomeMap));
        triageItems.sort(Comparator
                .comparing((SlotWorkspaceViewModel.AtlasItem item) -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));
        List<SlotWorkspaceViewModel.AtlasIsland> islands =
                SlotWorkspaceViewModel.projectIslandsWithCarriedCounts(projectedIslands, atlasItems);
        WorkspaceProjectionFrame resolvedFrame = frame == null
                ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                : frame;
        SlotWorkspaceViewModel withWall = base.withWallCardsCarriedStatsAndIslands(
                store.identityContext().carriedFreeSlotCount(),
                store.identityContext().carriedSlotCapacity(),
                islands,
                List.copyOf(atlasItems),
                List.copyOf(triageItems));
        return withWall.withFrame(
                resolvedFrame.revision(),
                resolvedFrame.status(),
                resolvedFrame.diagnostics(),
                resolvedFrame.pendingCount(),
                resolvedFrame.selectedQuickAccessSlot());
    }

    private static boolean simpleRemoteOnlyGhostCard(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null
                && !item.carried()
                && item.ghost()
                && item.proximateCount() == 0
                && item.desiredCount() == 0
                && item.wantedCount() == 0
                && !item.junk()
                && !item.kitNeeded()
                && !item.desiredCountFromKit()
                && !item.acceptedWorkflowInput()
                && item.putAwayState() == SlotWorkspaceViewModel.PutAwayState.NONE
                && simplePresenceTotal(item.presence()) == 0
                && simplePresenceTotal(item.elsewhere()) > 0;
    }

    private static SlotWorkspaceViewModel.ChestChip withStorageChipChrome(
            SlotWorkspaceViewModel.ChestChip chip,
            String clusterId,
            int affinityIdentities
    ) {
        if (chip == null) {
            return null;
        }
        int nextAffinity = Math.max(0, affinityIdentities);
        String nextCluster = clusterId == null || clusterId.isBlank() ? chip.clusterId() : clusterId;
        if (nextCluster.equals(chip.clusterId()) && nextAffinity == chip.affinityIdentities()) {
            return chip;
        }
        return new SlotWorkspaceViewModel.ChestChip(
                chip.storageId(),
                chip.dimensionId(),
                chip.label(),
                chip.anchorCount(),
                chip.slotCapacity(),
                chip.filledSlots(),
                chip.proximate(),
                nextAffinity,
                chip.worldX(),
                chip.worldY(),
                chip.worldZ(),
                nextCluster,
                chip.contents());
    }

    private static Set<SlotWorkspaceViewModel.IdentityRef> projectSimpleDepositableIdentities(
            WorkspaceProjectionStore store,
            WorkspaceProjectionRequest request,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems
    ) {
        if (store == null
                || request == null
                || atlasItems == null
                || atlasItems.isEmpty()
                || request.depositEligibleStorageIds().isEmpty()) {
            return Set.of();
        }
        ClaimedChestMap claimedChests = request.workflow().claimedChestMap();
        if (claimedChests.chests().isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> depositable = new LinkedHashSet<>();
        for (SlotWorkspaceViewModel.AtlasItem item : atlasItems) {
            if (item == null || !item.carried() || item.identity() == null) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            if (identity == null || item.totalCount() <= simpleReservedCarryCount(store, identity)) {
                continue;
            }
            if (simpleDepositTargetExists(store, request, claimedChests, identity)) {
                depositable.add(item.identity());
            }
        }
        return depositable.isEmpty() ? Set.of() : Set.copyOf(depositable);
    }

    private static int simpleReservedCarryCount(WorkspaceProjectionStore store, ItemIdentity identity) {
        WorkspaceProjectionStore.TargetFact target = store == null
                ? null
                : ItemIdentityCollections.find(store.targetFacts(), identity);
        if (target == null) {
            return 0;
        }
        return Math.max(target.desiredCount(), target.wantedCount());
    }

    private static boolean simpleDepositTargetExists(
            WorkspaceProjectionStore store,
            WorkspaceProjectionRequest request,
            ClaimedChestMap claimedChests,
            ItemIdentity identity
    ) {
        DepositPlanner.ChestContentPresence presence =
                request.liveChestContentPresence() == null
                        ? (chest, candidate) -> simpleStorageContainsIdentity(store, chest, candidate)
                        : request.liveChestContentPresence();
        DepositPlanner.ChestEligibility eligibility =
                request.liveStorageAffinityEligibility() == null
                        ? chest -> simpleStorageEligible(store, chest)
                        : request.liveStorageAffinityEligibility();
        return !DepositPlanner.rankChestsForIdentity(
                identity,
                claimedChests,
                request.workflow().chestAffinityMap().decayed(request.currentTick()),
                request.depositEligibleStorageIds(),
                presence,
                eligibility).isEmpty();
    }

    private static boolean simpleStorageContainsIdentity(
            WorkspaceProjectionStore store,
            ClaimedChest chest,
            ItemIdentity identity
    ) {
        if (store == null || chest == null || identity == null || !simpleStorageEligible(store, chest)) {
            return false;
        }
        WorkspaceProjectionStore.StoragePresenceFact fact = store.storagePresence().get(
                new WorkspaceProjectionStore.StoragePresenceKey(chest.storageId().toString(), identity));
        return fact != null && fact.count() > 0;
    }

    private static boolean simpleStorageEligible(WorkspaceProjectionStore store, ClaimedChest chest) {
        if (store == null || chest == null || !chest.role().quickDepositTarget()) {
            return false;
        }
        WorkspaceProjectionStore.StorageContentsFact contents =
                store.storageContents().get(chest.storageId().toString());
        return contents != null && StorageAffinityPolicy.isEligibleSlotCount(contents.slotCount());
    }

    private static Set<ItemIdentity> storageAffectedIdentities(
            WorkspaceProjectionStore previousStore,
            WorkspaceProjectionStore nextStore,
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations,
            SlotWorkspaceViewModel previousView
    ) {
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (summary.storageIds().isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        addStoragePresenceIdentities(identities, previousStore, summary.storageIds());
        addStoragePresenceIdentities(identities, nextStore, summary.storageIds());
        addStorageAffinityIdentities(identities, request, summary.storageIds());
        if (summary.slices().contains(WorkspaceProjectionSlice.DEPOSITABILITY)) {
            addIdentityRefs(identities, previousView == null ? Set.of() : previousView.depositableIdentities());
        }
        return identities.isEmpty() ? Set.of() : Set.copyOf(identities);
    }

    private static Set<ItemIdentity> simpleWorkflowStorageAffectedIdentities(
            WorkspaceProjectionStore previousStore,
            WorkspaceProjectionStore nextStore,
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations,
            SlotWorkspaceViewModel previousView
    ) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>(storageAffectedIdentities(
                previousStore,
                nextStore,
                request,
                invalidations,
                previousView));
        WorkspaceInvalidationSummary summary = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (summary.slices().contains(WorkspaceProjectionSlice.CARD)
                || summary.slices().contains(WorkspaceProjectionSlice.WORKFLOW)
                || summary.slices().contains(WorkspaceProjectionSlice.CONTEXTUAL)
                || summary.slices().contains(WorkspaceProjectionSlice.WAYFINDING)
                || summary.slices().contains(WorkspaceProjectionSlice.DEPOSITABILITY)) {
            addSimpleWorkflowPutAwayIdentities(identities, previousView);
        }
        return identities.isEmpty() ? Set.of() : Set.copyOf(identities);
    }

    private static void addSimpleWorkflowPutAwayIdentities(
            Set<ItemIdentity> out,
            SlotWorkspaceViewModel previousView
    ) {
        if (out == null || previousView == null) {
            return;
        }
        addSimpleWorkflowPutAwayIdentities(out, previousView.atlasItems());
        addSimpleWorkflowPutAwayIdentities(out, previousView.triageItems());
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : previousView.contextualSuggestionLanes()) {
            if (lane != null && lane.putAway()) {
                addSimpleWorkflowPutAwayIdentities(out, lane.items());
            }
        }
    }

    private static void addSimpleWorkflowPutAwayIdentities(
            Set<ItemIdentity> out,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        if (out == null || items == null || items.isEmpty()) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && item.identity() != null && item.putAwayState().active()) {
                ItemIdentityCollections.add(out, item.identity().toIdentity());
            }
        }
    }

    private static void addStoragePresenceIdentities(
            Set<ItemIdentity> out,
            WorkspaceProjectionStore store,
            Set<String> storageIds
    ) {
        if (out == null || store == null || storageIds == null || storageIds.isEmpty()) {
            return;
        }
        for (WorkspaceProjectionStore.StoragePresenceKey key : store.storagePresence().keySet()) {
            if (key != null && storageIds.contains(key.storageId())) {
                ItemIdentityCollections.add(out, key.identity());
            }
        }
    }

    private static void addStorageAffinityIdentities(
            Set<ItemIdentity> out,
            WorkspaceProjectionRequest request,
            Set<String> storageIds
    ) {
        if (out == null || request == null || storageIds == null || storageIds.isEmpty()) {
            return;
        }
        ChestAffinityMap affinityMap = request.workflow().chestAffinityMap().decayed(request.currentTick());
        for (String storageId : storageIds) {
            UUID uuid = parseStorageUuid(storageId);
            if (uuid == null) {
                continue;
            }
            for (ItemIdentity identity : affinityMap.forChest(uuid).keySet()) {
                ItemIdentityCollections.add(out, identity);
            }
        }
    }

    private static void addIdentityRefs(
            Set<ItemIdentity> out,
            Set<SlotWorkspaceViewModel.IdentityRef> refs
    ) {
        if (out == null || refs == null || refs.isEmpty()) {
            return;
        }
        for (SlotWorkspaceViewModel.IdentityRef ref : refs) {
            if (ref != null) {
                ItemIdentityCollections.add(out, ref.toIdentity());
            }
        }
    }

    private static Map<String, String> simpleStorageClusterIds(WorkspaceProjectionRequest request) {
        if (request == null || request.workflow().claimedChestMap().chests().isEmpty()) {
            return Map.of();
        }
        ChestClusterMap clusters = ChestClusterMap.derive(request.workflow().claimedChestMap());
        LinkedHashMap<String, String> ids = new LinkedHashMap<>();
        for (ClaimedChest chest : request.workflow().claimedChestMap().chests()) {
            if (chest == null) {
                continue;
            }
            String clusterId = clusters.clusterId(chest.storageId());
            if (clusterId != null && !clusterId.isBlank()) {
                ids.put(chest.storageId().toString(), clusterId);
            }
        }
        return ids.isEmpty() ? Map.of() : Map.copyOf(ids);
    }

    private static Map<String, String> simpleClusterLabels(WorkspaceProjectionRequest request) {
        if (request == null || request.workflow() == null) {
            return Map.of();
        }
        Map<String, String> labels = request.workflow().clusterLabels();
        return labels == null ? Map.of() : labels;
    }

    private static List<SlotWorkspaceViewModel.ChestClusterDescriptor> simpleStorageClusterDescriptors(
            WorkspaceProjectionRequest request
    ) {
        if (request == null || request.workflow().claimedChestMap().chests().isEmpty()) {
            return List.of();
        }
        ChestClusterMap clusters = ChestClusterMap.derive(request.workflow().claimedChestMap());
        if (clusters.clusters().isEmpty()) {
            return List.of();
        }
        Map<String, String> labels = request.workflow().clusterLabels();
        ArrayList<SlotWorkspaceViewModel.ChestClusterDescriptor> descriptors =
                new ArrayList<>(clusters.clusters().size());
        for (ChestClusterMap.Cluster cluster : clusters.clusters()) {
            String custom = labels == null ? null : labels.get(cluster.clusterId());
            String label = custom == null || custom.isBlank() ? cluster.defaultLabel() : custom;
            descriptors.add(new SlotWorkspaceViewModel.ChestClusterDescriptor(
                    cluster.clusterId(),
                    label,
                    cluster.ordinal()));
        }
        return List.copyOf(descriptors);
    }

    private static int simpleStorageAffinityCount(
            WorkspaceProjectionRequest request,
            String storageId,
            int fallback
    ) {
        if (request == null || storageId == null || storageId.isBlank()) {
            return Math.max(0, fallback);
        }
        UUID uuid = parseStorageUuid(storageId);
        if (uuid == null || request.workflow().claimedChestMap().chest(uuid) == null) {
            return Math.max(0, fallback);
        }
        return request.workflow().chestAffinityMap().decayed(request.currentTick()).forChest(uuid).size();
    }

    private static UUID parseStorageUuid(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(storageId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static SlotWorkspaceViewModel applySimpleCarriedIdentityProjection(
            SlotWorkspaceViewModel base,
            WorkspaceProjectionStore store,
            Set<ItemIdentity> identities,
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame
    ) {
        return applySimpleCarriedIdentityProjection(base, store, identities, request, frame, false);
    }

    private static SlotWorkspaceViewModel applySimpleCarriedIdentityProjection(
            SlotWorkspaceViewModel base,
            WorkspaceProjectionStore store,
            Set<ItemIdentity> identities,
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            boolean allowCraftRunMissingGhosts
    ) {
        return applySimpleCarriedIdentityProjection(
                base,
                store,
                identities,
                request,
                frame,
                allowCraftRunMissingGhosts,
                false);
    }

    private static SlotWorkspaceViewModel applySimpleCarriedIdentityProjection(
            SlotWorkspaceViewModel base,
            WorkspaceProjectionStore store,
            Set<ItemIdentity> identities,
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame,
            boolean allowCraftRunMissingGhosts,
            boolean allowWorkflowTargetGhosts
    ) {
        if (base == null || store == null || identities == null || identities.isEmpty()) {
            return null;
        }
        LinkedHashSet<ItemIdentity> affected = new LinkedHashSet<>();
        for (ItemIdentity identity : identities) {
            ItemIdentityCollections.add(affected, identity);
        }
        if (affected.isEmpty()) {
            return null;
        }
        WorkflowDomainSnapshot workflow = request == null
                ? WorkflowDomainSnapshot.empty()
                : request.workflow();
        VisualHomeMap visualHomeMap = workflow.visualHomeMap();
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> projectedIslands =
                new ArrayList<>(SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap));
        ArrayList<SlotWorkspaceViewModel.AtlasItem> atlasItems = new ArrayList<>(base.atlasItems());
        ArrayList<SlotWorkspaceViewModel.AtlasItem> triageItems = new ArrayList<>(base.triageItems());
        int previousAtlasSize = atlasItems.size();
        int previousTriageSize = triageItems.size();
        atlasItems.removeIf(item -> affectedCard(item, affected));
        triageItems.removeIf(item -> affectedCard(item, affected));
        int removedCards = (previousAtlasSize - atlasItems.size()) + (previousTriageSize - triageItems.size());
        int rebuiltCards = 0;
        ensureSimpleMiscIslandForCards(projectedIslands, atlasItems);
        boolean includeProximatePresence = request != null
                && (!request.worldDisplaySources().isEmpty() || !request.proximateStorageIds().isEmpty());
        boolean includeRemotePresence = !allowWorkflowTargetGhosts;
        for (ItemIdentity identity : affected) {
            SlotWorkspaceViewModel.AtlasItem card = SlotWorkspaceViewModel.projectSimpleCarriedCard(
                    store,
                    identity,
                    visualHomeMap,
                    projectedIslands,
                    request == null ? null : request.carriedContainerInfoResolver(),
                    includeProximatePresence,
                    includeRemotePresence);
            if (card == null) {
                card = allowWorkflowTargetGhosts
                        ? SlotWorkspaceViewModel.projectSimpleWorkflowTargetGhostCard(
                                store,
                                identity,
                                visualHomeMap,
                                projectedIslands,
                                request == null ? null : request.carriedContainerInfoResolver(),
                                includeProximatePresence)
                        : allowCraftRunMissingGhosts
                        ? SlotWorkspaceViewModel.projectSimpleCraftRunTargetGhostCard(
                                store,
                                identity,
                                visualHomeMap,
                                projectedIslands,
                                request == null ? null : request.carriedContainerInfoResolver(),
                                includeProximatePresence)
                        : SlotWorkspaceViewModel.projectSimpleTargetGhostCard(
                                store,
                                identity,
                                visualHomeMap,
                                projectedIslands,
                                request == null ? null : request.carriedContainerInfoResolver(),
                                includeProximatePresence);
            }
            if (card == null && includeProximatePresence) {
                card = SlotWorkspaceViewModel.projectSimpleProximateStorageGhostCard(
                        store,
                        identity,
                        visualHomeMap,
                        projectedIslands,
                        request == null ? null : request.carriedContainerInfoResolver());
            }
            if (card == null && simpleTrackedXray(request)) {
                card = SlotWorkspaceViewModel.projectSimpleRemoteStorageGhostCard(
                        store,
                        identity,
                        visualHomeMap,
                        projectedIslands,
                        request == null ? null : request.carriedContainerInfoResolver());
            }
            if (card != null && allowWorkflowTargetGhosts) {
                card = simpleWorkflowDecoratedCard(card, store, request);
            }
            if (card != null) {
                if (SlotWorkspaceAtlasLayout.ISLAND_MISC.equals(card.islandId())) {
                    ensureSimpleMiscIsland(projectedIslands);
                }
                if (!SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(card.islandId())
                        && SlotWorkspaceAtlasLayout.island(projectedIslands, card.islandId()) != null) {
                    atlasItems.add(card);
                } else {
                    triageItems.add(card);
                }
                rebuiltCards++;
            }
        }
        if (removedCards == 0 && rebuiltCards == 0) {
            return null;
        }
        atlasItems.sort(simpleAtlasCardComparator(visualHomeMap));
        triageItems.sort(Comparator
                .comparing((SlotWorkspaceViewModel.AtlasItem item) -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId()));
        List<SlotWorkspaceViewModel.AtlasIsland> islands =
                SlotWorkspaceViewModel.projectIslandsWithCarriedCounts(projectedIslands, atlasItems);
        WorkspaceProjectionFrame resolvedFrame = frame == null
                ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                : frame;
        SlotWorkspaceViewModel withWall = base.withWallCardsCarriedStatsAndIslands(
                store.identityContext().carriedFreeSlotCount(),
                store.identityContext().carriedSlotCapacity(),
                islands,
                List.copyOf(atlasItems),
                List.copyOf(triageItems));
        SlotWorkspaceViewModel withHotbar = withWall.withHotbar(
                SlotWorkspaceViewModel.projectHotbarSlots(
                        request == null ? null : request.authority(),
                        resolvedFrame.selectedQuickAccessSlot()),
                SlotWorkspaceViewModel.projectOffhand(request == null ? null : request.authority()),
                SlotWorkspaceViewModel.projectRecentIdentityRefs(request == null ? null : request.workflow()));
        List<WayfindingTarget> wayfindingTargets = request != null && request.chestContentsResolver() != null
                ? allowWorkflowTargetGhosts || allowCraftRunMissingGhosts
                        ? mergeSimpleWayfindingTargets(
                                SlotWorkspaceViewModel.projectSimpleWorkflowWayfindingTargets(store),
                                allowWorkflowTargetGhosts
                                        ? projectSimpleWorkflowPutAwayTargets(
                                                List.copyOf(atlasItems),
                                                List.copyOf(triageItems),
                                                request)
                                        : List.of())
                        : SlotWorkspaceViewModel.projectSimpleWayfindingTargets(store)
                : List.of();
        Set<SlotWorkspaceViewModel.IdentityRef> depositableIdentities =
                projectSimpleDepositableIdentities(store, request, atlasItems);
        SlotWorkspaceViewModel withEdges = withHotbar.withStorageEdges(wayfindingTargets, depositableIdentities);
        SlotWorkspaceViewModel withContextual = withEdges.withContextualSuggestionLanes(
                allowWorkflowTargetGhosts
                        ? SlotWorkspaceViewModel.projectSimpleWorkflowLanes(
                                List.copyOf(atlasItems),
                                List.copyOf(triageItems))
                        : SlotWorkspaceViewModel.projectSimpleFetchLanes(
                                List.copyOf(atlasItems),
                                List.copyOf(triageItems)));
        return withContextual.withFrame(
                resolvedFrame.revision(),
                resolvedFrame.status(),
                resolvedFrame.diagnostics(),
                resolvedFrame.pendingCount(),
                resolvedFrame.selectedQuickAccessSlot());
    }

    private static SlotWorkspaceViewModel applySimpleSectionMetadataProjection(
            SlotWorkspaceViewModel base,
            WorkspaceProjectionRequest request,
            WorkspaceProjectionFrame frame
    ) {
        if (base == null || request == null) {
            return null;
        }
        VisualHomeMap visualHomeMap = request.workflow().visualHomeMap();
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> projectedIslands =
                new ArrayList<>(SlotWorkspaceAtlasLayout.baseIslands(visualHomeMap));
        for (SlotWorkspaceViewModel.AtlasItem item : base.atlasItems()) {
            if (item != null && SlotWorkspaceAtlasLayout.ISLAND_MISC.equals(item.islandId())) {
                ensureSimpleMiscIsland(projectedIslands);
                break;
            }
        }
        List<SlotWorkspaceViewModel.AtlasIsland> islands =
                SlotWorkspaceViewModel.projectIslandsWithCarriedCounts(projectedIslands, base.atlasItems());
        WorkspaceProjectionFrame resolvedFrame = frame == null
                ? new WorkspaceProjectionFrame("ready", "", 0, -1, 0L)
                : frame;
        return base.withWallCardsCarriedStatsAndIslands(
                        base.carriedFreeSlotCount(),
                        base.carriedSlotCapacity(),
                        islands,
                        base.atlasItems(),
                        base.triageItems())
                .withFrame(
                        resolvedFrame.revision(),
                        resolvedFrame.status(),
                        resolvedFrame.diagnostics(),
                        resolvedFrame.pendingCount(),
                        resolvedFrame.selectedQuickAccessSlot());
    }

    private static SlotWorkspaceViewModel.AtlasItem simpleWorkflowDecoratedCard(
            SlotWorkspaceViewModel.AtlasItem card,
            WorkspaceProjectionStore store,
            WorkspaceProjectionRequest request
    ) {
        if (card == null || request == null || !simpleWorkflowPutAwayCandidate(card, request)) {
            return card == null ? null : card.withPutAwayState(SlotWorkspaceViewModel.PutAwayState.NONE);
        }
        boolean routed = !simpleWorkflowPutAwayRoutes(card, request).isEmpty();
        ItemIdentity identity = card.identity().toIdentity();
        if (!routed && identity != null) {
            routed = simpleDepositTargetExists(store, request, request.workflow().claimedChestMap(), identity);
        }
        return card.withPutAwayState(routed
                ? SlotWorkspaceViewModel.PutAwayState.ROUTED
                : SlotWorkspaceViewModel.PutAwayState.NO_ROUTE);
    }

    private static boolean simpleWorkflowPutAwayCandidate(
            SlotWorkspaceViewModel.AtlasItem item,
            WorkspaceProjectionRequest request
    ) {
        if (item == null
                || !item.carried()
                || item.ghost()
                || item.identity() == null
                || request == null
                || protectedPutAwaySource(item.largestCarriedSourceId())) {
            return false;
        }
        WorkflowDomainSnapshot workflow = request.workflow();
        KitMap kitMap = workflow.kitMap();
        if (kitMap == null
                || !kitMap.activation().isActive()
                || kitMap.activation().putAwayIdentities().isEmpty()) {
            return false;
        }
        ItemIdentity identity = item.identity().toIdentity();
        if (identity == null
                || !ItemIdentityCollections.containsCanonical(kitMap.activation().putAwayIdentities(), identity)) {
            return false;
        }
        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                CarriedIdentityCounts.from(request.authority()),
                kitMap,
                workflow.playerDesiredCounts(),
                workflow.kitDesiredCounts(),
                workflow.playerWantedCounts(),
                workflow.kitWantedCounts());
        return !targets.workflowRelevant(identity, ItemStackTags.itemTagIds(item.displayStack()));
    }

    private static List<WayfindingTarget> projectSimpleWorkflowPutAwayTargets(
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            List<SlotWorkspaceViewModel.AtlasItem> triageItems,
            WorkspaceProjectionRequest request
    ) {
        if (request == null) {
            return List.of();
        }
        LinkedHashMap<String, SimplePutAwayAccumulator> byStorage = new LinkedHashMap<>();
        collectSimpleWorkflowPutAwayTargets(byStorage, atlasItems, request);
        collectSimpleWorkflowPutAwayTargets(byStorage, triageItems, request);
        if (byStorage.isEmpty()) {
            return List.of();
        }
        ArrayList<WayfindingTarget> targets = new ArrayList<>(byStorage.size());
        for (SimplePutAwayAccumulator accumulator : byStorage.values()) {
            WayfindingTarget target = accumulator.toTarget();
            if (target != null) {
                targets.add(target);
            }
        }
        return targets.isEmpty() ? List.of() : List.copyOf(targets);
    }

    private static void collectSimpleWorkflowPutAwayTargets(
            Map<String, SimplePutAwayAccumulator> byStorage,
            List<SlotWorkspaceViewModel.AtlasItem> items,
            WorkspaceProjectionRequest request
    ) {
        if (byStorage == null || items == null || items.isEmpty()) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || item.putAwayState() != SlotWorkspaceViewModel.PutAwayState.ROUTED) {
                continue;
            }
            for (SimplePutAwayRoute route : simpleWorkflowPutAwayRoutes(item, request)) {
                byStorage.computeIfAbsent(
                                route.storageId(),
                                ignored -> new SimplePutAwayAccumulator(route))
                        .add(item.identity().toIdentity(), item.totalCount());
            }
        }
    }

    private static List<SimplePutAwayRoute> simpleWorkflowPutAwayRoutes(
            SlotWorkspaceViewModel.AtlasItem item,
            WorkspaceProjectionRequest request
    ) {
        if (item == null || item.identity() == null || request == null
                || !simpleWorkflowPutAwayCandidate(item, request)) {
            return List.of();
        }
        ItemIdentity identity = item.identity().toIdentity();
        if (identity == null) {
            return List.of();
        }
        ArrayList<SimplePutAwayRoute> routes = new ArrayList<>();
        ClaimedChestMap claimedChests = request.workflow().claimedChestMap();
        Set<String> candidateStorageIds = simpleClaimedPutAwayStorageIds(claimedChests);
        if (!candidateStorageIds.isEmpty()) {
            ChestAffinityMap affinity = request.workflow().chestAffinityMap().decayed(request.currentTick());
            for (UUID storageId : DepositPlanner.rankChestsForIdentity(
                    identity,
                    claimedChests,
                    affinity,
                    candidateStorageIds,
                    (chest, candidate) -> simpleChestContains(request, chest, candidate),
                    chest -> simpleChestEligible(request, chest))) {
                ClaimedChest chest = claimedChests.chest(storageId);
                if (chest == null || chest.anchors().isEmpty()) {
                    continue;
                }
                ChestAnchor anchor = chest.anchors().iterator().next();
                routes.add(new SimplePutAwayRoute(
                        storageId.toString(),
                        anchor.dimensionId(),
                        anchor.x(),
                        anchor.y(),
                        anchor.z()));
            }
        }
        for (WorkspaceStorageIndex.StorageEntry entry : simpleDisplayPutAwayRouteEntries(
                request.trackedDisplayStorageEntries(),
                identity)) {
            StorageTargetRef target = entry.target();
            routes.add(new SimplePutAwayRoute(
                    target.storageId(),
                    target.dimensionId(),
                    target.x(),
                    target.y(),
                    target.z()));
        }
        return routes.isEmpty() ? List.of() : List.copyOf(routes);
    }

    private static List<WorkspaceStorageIndex.StorageEntry> simpleDisplayPutAwayRouteEntries(
            Collection<WorkspaceStorageIndex.StorageEntry> trackedDisplayEntries,
            ItemIdentity identity
    ) {
        if (trackedDisplayEntries == null || trackedDisplayEntries.isEmpty() || identity == null) {
            return List.of();
        }
        ArrayList<WorkspaceStorageIndex.StorageEntry> entries = new ArrayList<>();
        for (WorkspaceStorageIndex.StorageEntry entry : trackedDisplayEntries) {
            if (!simpleDisplayPutAwayRouteEntry(entry)
                    || !simpleStorageEntryContainsMatchingContent(entry, identity)) {
                continue;
            }
            entries.add(entry);
        }
        return entries.isEmpty() ? List.of() : List.copyOf(entries);
    }

    private static boolean simpleDisplayPutAwayRouteEntry(WorkspaceStorageIndex.StorageEntry entry) {
        if (entry == null || entry.target() == null) {
            return false;
        }
        StorageTargetRef target = entry.target();
        return target.displayTarget()
                && !target.proximate()
                && target.depositTarget()
                && target.displayKind() != null
                && target.displayKind().trackedStorage();
    }

    private static boolean simpleStorageEntryContainsMatchingContent(
            WorkspaceStorageIndex.StorageEntry entry,
            ItemIdentity identity
    ) {
        if (entry == null || identity == null || entry.snapshot() == null) {
            return false;
        }
        for (net.minecraft.world.item.ItemStack stack : entry.snapshot().contents()) {
            if (stack != null && !stack.isEmpty()
                    && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> simpleClaimedPutAwayStorageIds(ClaimedChestMap claimedChests) {
        if (claimedChests == null || claimedChests.chests().isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (ClaimedChest chest : claimedChests.chests()) {
            if (chest != null && chest.role().quickDepositTarget()) {
                ids.add(chest.storageId().toString());
            }
        }
        return ids.isEmpty() ? Set.of() : Set.copyOf(ids);
    }

    private static boolean simpleChestContains(
            WorkspaceProjectionRequest request,
            ClaimedChest chest,
            ItemIdentity identity
    ) {
        if (request == null || chest == null || identity == null || !chest.role().quickDepositTarget()) {
            return false;
        }
        String storageId = chest.storageId().toString();
        if (request.liveChestContentPresence() != null && request.proximateStorageIds().contains(storageId)) {
            return request.liveChestContentPresence().contains(chest, identity);
        }
        if (request.chestContentsResolver() == null) {
            return false;
        }
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot =
                request.chestContentsResolver().apply(storageId);
        if (snapshot == null || snapshot.contents().isEmpty()) {
            return false;
        }
        if (!snapshot.countsByIdentity().isEmpty()) {
            return ItemIdentityCollections.contains(snapshot.countsByIdentity().keySet(), identity);
        }
        for (net.minecraft.world.item.ItemStack stack : snapshot.contents()) {
            if (stack != null && !stack.isEmpty()
                    && ItemIdentityMatcher.matchesMovable(stack, identity)) {
                return true;
            }
        }
        return false;
    }

    private static boolean simpleChestEligible(
            WorkspaceProjectionRequest request,
            ClaimedChest chest
    ) {
        if (request == null || chest == null || !chest.role().quickDepositTarget()) {
            return false;
        }
        String storageId = chest.storageId().toString();
        if (request.liveStorageAffinityEligibility() != null && request.proximateStorageIds().contains(storageId)) {
            return request.liveStorageAffinityEligibility().isEligible(chest);
        }
        if (request.chestContentsResolver() == null) {
            return false;
        }
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot =
                request.chestContentsResolver().apply(storageId);
        return snapshot != null && StorageAffinityPolicy.isEligibleSlotCount(snapshot.slotCount());
    }

    private static boolean simpleWorkflowAcceptedInputsLocalizable(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        WorkflowDomainSnapshot workflow = request.workflow();
        KitMap kitMap = workflow.kitMap();
        if (kitMap == null || kitMap.activeLineage().isEmpty()) {
            return true;
        }
        WorkflowTabTargets.Resolution targets = WorkflowTabTargets.resolve(
                CarriedIdentityCounts.from(request.authority()),
                workflow);
        for (WorkflowAcceptedInputRule rule : targets.acceptedInputs()) {
            if (rule == null) {
                return false;
            }
            if (rule.exactItem()) {
                ItemIdentity identity = ItemIdentityCollections.key(rule.identity());
                if (identity == null) {
                    return false;
                }
            } else if (!rule.itemTag() || rule.tagId().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean protectedPutAwaySource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return false;
        }
        return BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId);
    }

    private static boolean affectedCard(
            SlotWorkspaceViewModel.AtlasItem item,
            Set<ItemIdentity> affected
    ) {
        return item != null
                && item.identity() != null
                && ItemIdentityCollections.contains(affected, item.identity().toIdentity());
    }

    private static Comparator<SlotWorkspaceViewModel.AtlasItem> simpleAtlasCardComparator(
            VisualHomeMap visualHomeMap
    ) {
        VisualHomeMap homes = visualHomeMap == null ? VisualHomeMap.empty() : visualHomeMap;
        return Comparator
                .comparing(SlotWorkspaceViewModel.AtlasItem::islandId)
                .thenComparingInt(item -> simpleAssignmentOrdinal(homes, item))
                .thenComparing(item -> item.name().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.identity().itemId())
                .thenComparingInt(SlotWorkspaceViewModel.AtlasItem::firstSlotIndex);
    }

    private static int simpleAssignmentOrdinal(
            VisualHomeMap visualHomeMap,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        if (visualHomeMap == null || item == null || item.identity() == null) {
            return Integer.MAX_VALUE;
        }
        VisualHomeAssignment assignment = visualHomeMap.assignment(item.identity().toIdentity());
        return assignment == null ? Integer.MAX_VALUE : assignment.ordinal();
    }

    private static void ensureSimpleMiscIsland(ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands) {
        if (islands == null || SlotWorkspaceAtlasLayout.island(islands, SlotWorkspaceAtlasLayout.ISLAND_MISC) != null) {
            return;
        }
        islands.add(new SlotWorkspaceViewModel.AtlasIsland(
                SlotWorkspaceAtlasLayout.ISLAND_MISC,
                SlotWorkspaceAtlasLayout.ISLAND_MISC_LABEL,
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                SlotWorkspaceAtlasLayout.ISLAND_MISC_COLOR,
                0,
                0));
    }

    private static void ensureSimpleMiscIslandForCards(
            ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item != null && SlotWorkspaceAtlasLayout.ISLAND_MISC.equals(item.islandId())) {
                ensureSimpleMiscIsland(islands);
                return;
            }
        }
    }

    private boolean canProjectSimpleCarriedIdentity(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (resolved.identities().isEmpty() || !resolved.storageIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.DEPOSITABILITY,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.CONTEXTUAL,
                WorkspaceProjectionSlice.HOTBAR,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        if (simpleCarriedRequest(resolvedRequest) && simpleCarriedCachedView(lastStructuralView)) {
            return true;
        }
        return simpleCarriedStorageRequest(resolvedRequest)
                && simpleCarriedStorageCachedViewForStorageChange(
                        lastStructuralView,
                        resolvedRequest,
                        resolved.storageIds(),
                        resolved.identities());
    }

    private boolean canProjectSimpleCraftRun(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (resolved.identities().isEmpty() || !resolved.storageIds().isEmpty()) {
            return false;
        }
        CraftRunState nextCraftRun = resolvedRequest.workflow().craftRun();
        CraftRunState previousCraftRun = lastStructuralView == null
                ? CraftRunState.empty()
                : lastStructuralView.craftRun();
        if (!nextCraftRun.active() && !previousCraftRun.active()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.CONTEXTUAL,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        if (simpleCraftRunRequest(resolvedRequest) && simpleCraftRunCachedView(lastStructuralView)) {
            return true;
        }
        return simpleCraftRunStorageRequest(resolvedRequest)
                && simpleCraftRunStorageCachedView(lastStructuralView, resolvedRequest);
    }

    private boolean canProjectSimpleSectionMetadata(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (!resolved.identities().isEmpty() || !resolved.storageIds().isEmpty() || resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        return simpleSectionMetadataRequestAndView(resolvedRequest, lastStructuralView);
    }

    private boolean canProjectSimpleWorkflow(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        Set<ItemIdentity> affectedIdentities =
                simpleWorkflowAffectedIdentities(resolvedRequest, resolved, lastStructuralView);
        if ((affectedIdentities.isEmpty()
                        && !simpleWorkflowAllowsEmptyAffectedCards(resolvedRequest, resolved, lastStructuralView))
                || !resolved.storageIds().isEmpty()
                || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.CONTEXTUAL,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        return simpleWorkflowRequest(resolvedRequest) && simpleWorkflowCachedView(lastStructuralView, resolvedRequest);
    }

    private boolean canProjectSimpleRemoteDetail(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (!resolved.identities().isEmpty()
                || !resolved.storageIds().isEmpty()
                || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        for (WorkspaceInvalidation invalidation : resolved.invalidations()) {
            if (invalidation.reason() != WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED
                    && invalidation.reason() != WorkspaceInvalidation.Reason.REMOTE_STORAGE_DETAIL_CHANGED) {
                return false;
            }
        }
        return simpleRemoteDetailRequest(resolvedRequest)
                && simpleRemoteDetailCachedView(lastStructuralView);
    }

    private static boolean simpleSectionMetadataRequestAndView(
            WorkspaceProjectionRequest request,
            SlotWorkspaceViewModel view
    ) {
        return (simpleCarriedRequest(request) && simpleCarriedCachedView(view))
                || (simpleCraftRunRequest(request) && simpleCraftRunCachedView(view))
                || (simpleCarriedStorageRequest(request) && simpleCarriedStorageCachedView(view, request))
                || (simpleWorkflowRequest(request) && simpleWorkflowCachedView(view, request))
                || (simpleRemoteDetailRequest(request) && simpleRemoteDetailCachedView(view));
    }

    private static boolean simpleCarriedRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomesPlayerTargetsAndJunk(request.workflow())) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()
                || !request.searchQuery().isBlank()
                || (request.remoteStorageDetailIntent().includesSearchMatches() && !simpleTrackedXray(request))
                || (request.remoteStorageDetailIntent().includesAllRemote() && !simpleTrackedXray(request))) {
            return false;
        }
        if (!request.proximateStorageIds().isEmpty()
                || !request.worldDisplaySources().isEmpty()
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()
                || !request.trackedDisplayStorageEntries().isEmpty()
                || !request.depositEligibleStorageIds().isEmpty()
                || !request.storageIndex().entries().isEmpty()) {
            return false;
        }
        return request.signalExtractor() == null
                && request.chestContentsResolver() == null
                && request.lootChestSource() == null
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleCraftRunRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomesPlayerTargetsJunkAndCraftRun(request.workflow())) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()
                || !request.searchQuery().isBlank()
                || request.remoteStorageDetailIntent().includesSearchMatches()
                || request.remoteStorageDetailIntent().includesAllRemote()) {
            return false;
        }
        if (!request.proximateStorageIds().isEmpty()
                || !request.worldDisplaySources().isEmpty()
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()
                || !request.trackedDisplayStorageEntries().isEmpty()
                || !request.depositEligibleStorageIds().isEmpty()
                || !request.storageIndex().entries().isEmpty()) {
            return false;
        }
        return request.signalExtractor() == null
                && request.chestContentsResolver() == null
                && request.lootChestSource() == null
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleCraftRunStorageRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(request.workflow(), true, true, true)) {
            return false;
        }
        if (!request.workflow().claimedChestMap().chests().isEmpty()
                && request.chestContentsResolver() == null) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()
                || !request.searchQuery().isBlank()
                || request.remoteStorageDetailIntent().includesSearchMatches()
                || request.remoteStorageDetailIntent().includesAllRemote()) {
            return false;
        }
        if (!simpleProximateStorageIds(request)
                || !simpleWorldDisplaySources(request)
                || !simpleDepositEligibleStorageIds(request)
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()
                || !request.trackedDisplayStorageEntries().isEmpty()) {
            return false;
        }
        return request.signalExtractor() == null
                && request.lootChestSource() == null
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleRemoteDetailRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomes(request.workflow())) {
            return false;
        }
        if (request.remoteStorageDetailIntent() == RemoteStorageDetailIntent.TRACKED_XRAY) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()) {
            return false;
        }
        if (!request.proximateStorageIds().isEmpty()
                || !request.worldDisplaySources().isEmpty()
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()
                || !request.depositEligibleStorageIds().isEmpty()) {
            return false;
        }
        if (!simpleRemoteSearchStorageIndex(request)) {
            return false;
        }
        return request.signalExtractor() == null
                && request.lootChestSource() == null
                && request.liveChestContentPresence() == null
                && request.liveStorageAffinityEligibility() == null
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleRemoteDetailCachedView(SlotWorkspaceViewModel view) {
        if (view == null) {
            return false;
        }
        return simpleCarriedStorageCards(view.atlasItems(), true)
                && simpleCarriedStorageCards(view.triageItems(), true)
                && simpleCarriedStorageChips(view.chestChips())
                && simpleStorageClusters(view.chestClusters())
                && view.kits().isEmpty()
                && simpleCarriedStorageWayfindingTargets(view.wayfindingTargets())
                && simpleIdentityRefs(view.depositableIdentities())
                && view.recentIdentities().isEmpty()
                && simpleCarriedStorageContextualSuggestionLanes(view.contextualSuggestionLanes())
                && SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel());
    }

    private static boolean simpleRemoteSearchStorageIndex(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        WorkspaceStorageIndex index = request.storageIndex();
        if (!request.trackedDisplayStorageEntries().equals(index.liveTrackedDisplayEntries())) {
            return false;
        }
        for (WorkspaceStorageIndex.StorageEntry entry : index.entries()) {
            if (entry == null || entry.target() == null) {
                return false;
            }
            StorageTargetRef target = entry.target();
            if (!target.displayTarget()
                    || target.proximate()
                    || target.displayKind() == null
                    || !target.displayKind().trackedStorage()) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCarriedCachedView(SlotWorkspaceViewModel view) {
        if (view == null) {
            return false;
        }
        return simpleCarriedCards(view.atlasItems())
                && simpleCarriedCards(view.triageItems())
                && view.chestChips().isEmpty()
                && view.chestClusters().isEmpty()
                && view.kits().isEmpty()
                && view.wayfindingTargets().isEmpty()
                && view.depositableIdentities().isEmpty()
                && view.recentIdentities().isEmpty()
                && simpleContextualSuggestionLanes(view.contextualSuggestionLanes())
                && SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel());
    }

    private static boolean simpleCraftRunCachedView(SlotWorkspaceViewModel view) {
        if (view == null) {
            return false;
        }
        return simpleCraftRunCards(view.atlasItems())
                && simpleCraftRunCards(view.triageItems())
                && view.chestChips().isEmpty()
                && view.chestClusters().isEmpty()
                && view.kits().isEmpty()
                && view.wayfindingTargets().isEmpty()
                && view.depositableIdentities().isEmpty()
                && view.recentIdentities().isEmpty()
                && simpleCraftRunContextualSuggestionLanes(view.contextualSuggestionLanes())
                && SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel());
    }

    private static boolean simpleCraftRunStorageCachedView(
            SlotWorkspaceViewModel view,
            WorkspaceProjectionRequest request
    ) {
        if (view == null) {
            return false;
        }
        if (!simpleCraftRunStorageCards(view.atlasItems())
                || !simpleCraftRunStorageCards(view.triageItems())
                || !simpleCarriedStorageChips(view.chestChips())
                || !simpleStorageClusters(view.chestClusters())
                || !view.kits().isEmpty()
                || !simpleWorkflowWayfindingTargets(view.wayfindingTargets())
                || !simpleIdentityRefs(view.depositableIdentities())
                || !view.recentIdentities().isEmpty()
                || !simpleCraftRunStorageContextualSuggestionLanes(view.contextualSuggestionLanes())
                || !SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                || !SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel())) {
            return false;
        }
        ClaimedChestMap claimedChests = request == null
                ? ClaimedChestMap.empty()
                : request.workflow().claimedChestMap();
        ChestClusterMap expectedClusters = ChestClusterMap.derive(claimedChests);
        ChestAffinityMap affinityMap = request == null
                ? ChestAffinityMap.empty()
                : request.workflow().chestAffinityMap().decayed(request.currentTick());
        return simpleStorageClustersMatch(view.chestClusters(), expectedClusters, simpleClusterLabels(request))
                && simpleStorageChipClustersMatch(view.chestChips(), expectedClusters)
                && simpleStorageChipAffinitiesMatch(view.chestChips(), affinityMap);
    }

    private static boolean workflowHasOnlyVisualHomes(WorkflowDomainSnapshot workflow) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        WorkflowProjection.Snapshot projection = resolved.workflowProjection();
        return workflowHasOnlyVisualHomesAndPlayerTargets(resolved)
                && projection.playerDesiredCounts().isEmpty()
                && projection.playerWantedCounts().isEmpty();
    }

    private static boolean workflowHasOnlyVisualHomesAndPlayerTargets(WorkflowDomainSnapshot workflow) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(resolved, false)
                && resolved.workflowProjection().claimedChestMap().chests().isEmpty();
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsAndJunk(WorkflowDomainSnapshot workflow) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(resolved, false, true)
                && resolved.workflowProjection().claimedChestMap().chests().isEmpty();
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsJunkAndCraftRun(WorkflowDomainSnapshot workflow) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(resolved, false, true, true)
                && resolved.workflowProjection().claimedChestMap().chests().isEmpty();
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(WorkflowDomainSnapshot workflow) {
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(workflow, false);
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsClaimedChestsAndAffinity(
            WorkflowDomainSnapshot workflow
    ) {
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(workflow, true);
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(
            WorkflowDomainSnapshot workflow,
            boolean allowAffinity
    ) {
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(workflow, allowAffinity, false);
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(
            WorkflowDomainSnapshot workflow,
            boolean allowAffinity,
            boolean allowJunk
    ) {
        return workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(
                workflow,
                allowAffinity,
                allowJunk,
                false);
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsAndClaimedChests(
            WorkflowDomainSnapshot workflow,
            boolean allowAffinity,
            boolean allowJunk,
            boolean allowCraftRun
    ) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        WorkflowProjection.Snapshot projection = resolved.workflowProjection();
        WorkflowProjection.Snapshot empty = WorkflowProjection.Snapshot.empty();
        return projection.userCollections().isEmpty()
                && projection.memberships().isEmpty()
                && projection.loadoutsByCollection().isEmpty()
                && projection.favoriteTags().isEmpty()
                && (allowJunk || projection.junkTags().isEmpty())
                && (allowJunk || projection.junkMarkedAtEpochMillis().isEmpty())
                && projection.protection().equals(empty.protection())
                && projection.recentDismissedUpToByIdentity().isEmpty()
                && (allowAffinity || projection.chestAffinityMap().entries().isEmpty())
                && projection.kitMap().equals(empty.kitMap())
                && projection.kitDesiredCounts().isEmpty()
                && projection.kitWantedCounts().isEmpty()
                && resolved.recents().visibleItems().isEmpty()
                && (allowCraftRun || !resolved.craftRun().active())
                && resolved.contextualSuggestions().itemAggregates().isEmpty()
                && resolved.contextualSuggestions().contextAggregates().isEmpty()
                && resolved.contextualSuggestions().recentSignals().isEmpty()
                && resolved.contextualSuggestions().activeContextKey().isBlank();
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsKitsAndClaimedChests(
            WorkflowDomainSnapshot workflow
    ) {
        return workflowHasOnlyVisualHomesPlayerTargetsKitsAndClaimedChests(workflow, false);
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsKitsClaimedChestsAndAffinity(
            WorkflowDomainSnapshot workflow
    ) {
        return workflowHasOnlyVisualHomesPlayerTargetsKitsAndClaimedChests(workflow, true);
    }

    private static boolean workflowHasOnlyVisualHomesPlayerTargetsKitsAndClaimedChests(
            WorkflowDomainSnapshot workflow,
            boolean allowAffinity
    ) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        WorkflowProjection.Snapshot projection = resolved.workflowProjection();
        WorkflowProjection.Snapshot empty = WorkflowProjection.Snapshot.empty();
        return projection.userCollections().isEmpty()
                && projection.memberships().isEmpty()
                && projection.loadoutsByCollection().isEmpty()
                && projection.favoriteTags().isEmpty()
                && projection.junkTags().isEmpty()
                && projection.junkMarkedAtEpochMillis().isEmpty()
                && projection.protection().equals(empty.protection())
                && projection.recentDismissedUpToByIdentity().isEmpty()
                && (allowAffinity || projection.chestAffinityMap().entries().isEmpty())
                && simpleWorkflowKitMap(projection.kitMap())
                && simpleWorkflowKitCountMap(projection.kitDesiredCounts(), projection.kitMap())
                && simpleWorkflowKitCountMap(projection.kitWantedCounts(), projection.kitMap())
                && resolved.recents().visibleItems().isEmpty()
                && !resolved.craftRun().active()
                && resolved.contextualSuggestions().itemAggregates().isEmpty()
                && resolved.contextualSuggestions().contextAggregates().isEmpty()
                && resolved.contextualSuggestions().recentSignals().isEmpty()
                && resolved.contextualSuggestions().activeContextKey().isBlank();
    }

    private static boolean simpleWorkflowKitMap(KitMap kitMap) {
        KitMap resolved = kitMap == null ? KitMap.empty() : kitMap;
        for (KitDefinition kit : resolved.kits()) {
            if (kit == null
                    || kit.id().isBlank()
                    || kit.name().isBlank()
                    || !simpleWorkflowIdentitySet(kit.members())
                    || !simpleWorkflowAcceptedInputs(kit.acceptedInputs())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowAcceptedInputs(Set<WorkflowAcceptedInputRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        for (WorkflowAcceptedInputRule rule : rules) {
            if (rule == null) {
                return false;
            }
            if (rule.exactItem()) {
                if (ItemIdentityCollections.key(rule.identity()) == null) {
                    return false;
                }
            } else if (!rule.itemTag() || rule.tagId().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowIdentitySet(Set<ItemIdentity> identities) {
        if (identities == null || identities.isEmpty()) {
            return true;
        }
        for (ItemIdentity identity : identities) {
            if (identity == null || ItemIdentityCollections.key(identity) == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowKitCountMap(
            Map<String, Map<ItemIdentity, Integer>> counts,
            KitMap kitMap
    ) {
        if (counts == null || counts.isEmpty()) {
            return true;
        }
        KitMap resolved = kitMap == null ? KitMap.empty() : kitMap;
        for (Map.Entry<String, Map<ItemIdentity, Integer>> scoped : counts.entrySet()) {
            if (scoped.getKey() == null || scoped.getKey().isBlank() || resolved.kit(scoped.getKey()) == null) {
                return false;
            }
            Map<ItemIdentity, Integer> entries = scoped.getValue();
            if (entries == null || entries.isEmpty()) {
                continue;
            }
            for (Map.Entry<ItemIdentity, Integer> entry : entries.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean simpleCarriedCards(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (!simpleCarriedCard(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCraftRunCards(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (!simpleCraftRunCard(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowCards(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (!simpleWorkflowCard(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCraftRunStorageCards(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (!simpleCraftRunStorageCard(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCarriedCard(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null
                || item.recent()
                || item.proximateCount() != 0
                || !item.chipSuggestions().isEmpty()
                || !item.presence().isEmpty()
                || !item.elsewhere().isEmpty()
                || item.kitNeeded()
                || item.desiredCountFromKit()
                || item.acceptedWorkflowInput()
                || item.putAwayState() != SlotWorkspaceViewModel.PutAwayState.NONE) {
            return false;
        }
        if (item.carried()) {
            return !item.ghost();
        }
        return item.ghost()
                && item.totalCount() == 0
                && !item.junk()
                && (item.desiredCount() > 0 || item.wantedCount() > 0);
    }

    private static boolean simpleCraftRunCard(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null
                || item.recent()
                || item.proximateCount() != 0
                || !item.chipSuggestions().isEmpty()
                || !item.presence().isEmpty()
                || !item.elsewhere().isEmpty()
                || item.desiredCountFromKit()
                || item.acceptedWorkflowInput()
                || item.putAwayState() != SlotWorkspaceViewModel.PutAwayState.NONE) {
            return false;
        }
        if (item.carried()) {
            return !item.ghost() && !item.kitNeeded();
        }
        return item.ghost()
                && item.totalCount() == 0
                && !item.junk()
                && (item.kitNeeded() || item.desiredCount() > 0 || item.wantedCount() > 0);
    }

    private static boolean simpleCraftRunStorageCard(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null
                || item.recent()
                || item.proximateCount() != simplePresenceTotal(item.presence())
                || !item.chipSuggestions().isEmpty()
                || !simplePresence(item.presence())
                || !simpleElsewhere(item.elsewhere())
                || item.desiredCountFromKit()
                || item.acceptedWorkflowInput()
                || item.putAwayState() != SlotWorkspaceViewModel.PutAwayState.NONE) {
            return false;
        }
        if (item.carried()) {
            return !item.ghost() && !item.kitNeeded();
        }
        int storageTotal = item.proximateCount() > 0
                ? item.proximateCount()
                : simplePresenceTotal(item.elsewhere());
        boolean storageOnly = storageTotal > 0 && item.totalCount() == storageTotal;
        return item.ghost()
                && item.totalCount() == storageTotal
                && !item.junk()
                && (storageOnly || item.kitNeeded() || item.desiredCount() > 0 || item.wantedCount() > 0);
    }

    private static boolean simpleWorkflowCard(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null
                || item.recent()
                || !item.chipSuggestions().isEmpty()
                || !simpleElsewhere(item.elsewhere())
                || !simplePresence(item.presence())) {
            return false;
        }
        int proximateTotal = simplePresenceTotal(item.presence());
        if (item.proximateCount() != proximateTotal) {
            return false;
        }
        if (item.putAwayState().active()
                && (!item.carried() || item.ghost())) {
            return false;
        }
        if (item.acceptedWorkflowInput()
                && item.putAwayState() != SlotWorkspaceViewModel.PutAwayState.NONE) {
            return false;
        }
        if (item.carried()) {
            return !item.ghost();
        }
        boolean workflowTarget = item.kitNeeded()
                || item.desiredCount() > 0
                || item.desiredCountFromKit()
                || item.wantedCount() > 0;
        boolean storageOnly = proximateTotal > 0 && item.totalCount() == proximateTotal;
        return item.ghost()
                && (item.totalCount() == 0 || item.totalCount() == proximateTotal)
                && !item.junk()
                && (workflowTarget || storageOnly);
    }

    private static boolean simpleContextualSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes
    ) {
        if (lanes == null || lanes.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null
                    || !lane.fetch()
                    || !lane.placeholderText().isBlank()
                    || !lane.debugInfo().isEmpty()
                    || !simpleCarriedCards(lane.items())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCraftRunContextualSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes
    ) {
        if (lanes == null || lanes.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null
                    || !lane.fetch()
                    || !lane.placeholderText().isBlank()
                    || !lane.debugInfo().isEmpty()
                    || !simpleCraftRunCards(lane.items())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCraftRunStorageContextualSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes
    ) {
        if (lanes == null || lanes.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null
                    || !lane.fetch()
                    || !lane.placeholderText().isBlank()
                    || !lane.debugInfo().isEmpty()
                    || !simpleCraftRunStorageCards(lane.items())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowContextualSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes
    ) {
        if (lanes == null || lanes.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null
                    || !lane.placeholderText().isBlank()
                    || !lane.debugInfo().isEmpty()
                    || (!lane.fetch() && !lane.putAway())
                    || !simpleWorkflowCards(lane.items())) {
                return false;
            }
            if (lane.putAway() && !simpleWorkflowPutAwayCards(lane.items())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowPutAwayCards(List<SlotWorkspaceViewModel.AtlasItem> items) {
        return simpleWorkflowPutAwayCards(items, Set.of());
    }

    private static boolean simpleWorkflowPutAwayCards(
            List<SlotWorkspaceViewModel.AtlasItem> items,
            Set<ItemIdentity> ignoredIdentities
    ) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        boolean checked = false;
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (ignoredAtlasItem(item, ignoredIdentities)) {
                continue;
            }
            if (item == null || !item.putAwayState().active()) {
                return false;
            }
            checked = true;
        }
        return checked || (ignoredIdentities != null && !ignoredIdentities.isEmpty());
    }

    private static boolean simpleCarriedStorageContextualSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes
    ) {
        return simpleCarriedStorageContextualSuggestionLanes(lanes, Set.of());
    }

    private static boolean simpleCarriedStorageContextualSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes,
            Set<ItemIdentity> ignoredIdentities
    ) {
        if (lanes == null || lanes.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null
                    || !lane.placeholderText().isBlank()
                    || !lane.debugInfo().isEmpty()
                    || (!lane.fetch() && !lane.putAway())
                    || !simpleCarriedStorageCards(lane.items(), false, ignoredIdentities)) {
                return false;
            }
            if (lane.putAway() && !simpleWorkflowPutAwayCards(lane.items(), ignoredIdentities)) {
                return false;
            }
        }
        return true;
    }

    private boolean canProjectSimpleCarriedAndStorage(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (resolved.identities().isEmpty() || resolved.storageIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.STORAGE,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.DEPOSITABILITY,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.HOTBAR,
                WorkspaceProjectionSlice.FRAME,
                WorkspaceProjectionSlice.REMOTE_SEARCH);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        return simpleCarriedStorageRequest(resolvedRequest)
                && simpleCarriedStorageCachedViewForStorageChange(
                        lastStructuralView,
                        resolvedRequest,
                        resolved.storageIds(),
                        resolved.identities());
    }

    private boolean canProjectSimpleStoragePresence(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (resolved.storageIds().isEmpty() || !resolved.identities().isEmpty() || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.STORAGE,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.DEPOSITABILITY,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.HOTBAR,
                WorkspaceProjectionSlice.FRAME,
                WorkspaceProjectionSlice.REMOTE_SEARCH);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        if (!WorkspaceProjectionFingerprint.authorityKey(resolvedRequest.authority())
                .equals(WorkspaceProjectionFingerprint.authorityKey(lastStore.identityContext().authority()))) {
            return false;
        }
        return simpleCarriedStorageRequest(resolvedRequest)
                && simpleCarriedStorageCachedViewForStorageChange(
                        lastStructuralView,
                        resolvedRequest,
                        resolved.storageIds());
    }

    private boolean canProjectSimpleWorkflowStoragePresence(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (resolved.storageIds().isEmpty() || !resolved.identities().isEmpty() || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.CARD,
                WorkspaceProjectionSlice.SECTION,
                WorkspaceProjectionSlice.STORAGE,
                WorkspaceProjectionSlice.WAYFINDING,
                WorkspaceProjectionSlice.DEPOSITABILITY,
                WorkspaceProjectionSlice.WORKFLOW,
                WorkspaceProjectionSlice.CONTEXTUAL,
                WorkspaceProjectionSlice.HOTBAR,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        if (!WorkspaceProjectionFingerprint.authorityKey(resolvedRequest.authority())
                .equals(WorkspaceProjectionFingerprint.authorityKey(lastStore.identityContext().authority()))) {
            return false;
        }
        return simpleWorkflowRequest(resolvedRequest)
                && simpleWorkflowCachedViewForStorageChange(
                        lastStructuralView,
                        resolvedRequest,
                        resolved.storageIds());
    }

    private static boolean canProjectHotbarFrameOnly(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (!resolved.identities().isEmpty() || !resolved.storageIds().isEmpty() || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> slices = resolved.slices();
        if (slices.isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.FRAME,
                WorkspaceProjectionSlice.HOTBAR);
        for (WorkspaceProjectionSlice slice : slices) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        return true;
    }

    private static boolean canProjectPanelOnly(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (!resolved.identities().isEmpty() || !resolved.storageIds().isEmpty() || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        if (!resolved.slices().contains(WorkspaceProjectionSlice.PANEL)) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.PANEL,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        return true;
    }

    private boolean canProjectSimpleStorage(
            WorkspaceProjectionRequest request,
            WorkspaceInvalidationSummary invalidations
    ) {
        WorkspaceProjectionRequest resolvedRequest = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0 || resolved.requiresFullProjection()) {
            return false;
        }
        if (resolved.storageIds().isEmpty() || !resolved.identities().isEmpty() || !resolved.sectionIds().isEmpty()) {
            return false;
        }
        EnumSet<WorkspaceProjectionSlice> allowed = EnumSet.of(
                WorkspaceProjectionSlice.STORAGE,
                WorkspaceProjectionSlice.FRAME);
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (!allowed.contains(slice)) {
                return false;
            }
        }
        return simpleStorageRequest(resolvedRequest, lastStore) && simpleStorageCachedView(lastStructuralView);
    }

    private static boolean simpleStorageRequest(
            WorkspaceProjectionRequest request,
            WorkspaceProjectionStore cachedStore
    ) {
        if (request == null) {
            return false;
        }
        if (!simpleStorageBaseRequest(request)) {
            return false;
        }
        WorkspaceProjectionStore resolvedStore = cachedStore == null ? WorkspaceProjectionStore.empty() : cachedStore;
        if (!WorkspaceProjectionFingerprint.authorityKey(request.authority())
                .equals(WorkspaceProjectionFingerprint.authorityKey(resolvedStore.identityContext().authority()))) {
            return false;
        }
        return true;
    }

    private static boolean simpleCarriedStorageRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomesPlayerTargetsClaimedChestsAndAffinity(request.workflow())) {
            return false;
        }
        if (!request.workflow().claimedChestMap().chests().isEmpty()
                && request.chestContentsResolver() == null) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()
                || !request.searchQuery().isBlank()
                || (request.remoteStorageDetailIntent().includesSearchMatches() && !simpleTrackedXray(request))
                || (request.remoteStorageDetailIntent().includesAllRemote() && !simpleTrackedXray(request))) {
            return false;
        }
        if (!simpleProximateStorageIds(request)
                || !simpleWorldDisplaySources(request)
                || !simpleDepositEligibleStorageIds(request)
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()) {
            return false;
        }
        return request.signalExtractor() == null
                && request.lootChestSource() == null
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleWorkflowRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomesPlayerTargetsKitsClaimedChestsAndAffinity(request.workflow())) {
            return false;
        }
        if (!request.workflow().claimedChestMap().chests().isEmpty()
                && request.chestContentsResolver() == null) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()
                || !request.searchQuery().isBlank()
                || request.remoteStorageDetailIntent().includesSearchMatches()
                || request.remoteStorageDetailIntent().includesAllRemote()) {
            return false;
        }
        if (!simpleWorkflowStorageIndex(request)
                || !simpleDepositEligibleStorageIds(request)
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()) {
            return false;
        }
        if (!simpleWorkflowAcceptedInputsLocalizable(request)) {
            return false;
        }
        return request.signalExtractor() == null
                && request.carriedContainerInfoResolver() == null
                && request.lootChestSource() == null
                && simpleWorkflowLiveHooksLocalizable(request)
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleWorkflowLiveHooksLocalizable(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        boolean hasLiveHooks = request.liveChestContentPresence() != null
                || request.liveStorageAffinityEligibility() != null;
        if (!hasLiveHooks) {
            return true;
        }
        KitMap kitMap = request.workflow().kitMap();
        if (kitMap == null
                || !kitMap.activation().isActive()
                || kitMap.activation().putAwayIdentities().isEmpty()) {
            return true;
        }
        if (request.proximateStorageIds().isEmpty()) {
            return true;
        }
        ClaimedChestMap claimedChests = request.workflow().claimedChestMap();
        for (String storageId : request.proximateStorageIds()) {
            try {
                ClaimedChest chest = claimedChests.chest(UUID.fromString(storageId));
                if (chest != null && chest.role().quickDepositTarget()) {
                    return true;
                }
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    private static boolean simpleWorkflowStorageIndex(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!simpleProximateStorageIds(request)
                || !simpleWorldDisplaySources(request)
                || !simpleTrackedDisplayStorageEntries(request)) {
            return false;
        }
        for (WorkspaceStorageIndex.StorageEntry entry : request.storageIndex().entries()) {
            if (entry == null || entry.target() == null) {
                return false;
            }
            if (entry.target().proximate()) {
                continue;
            }
            if (!simpleDisplayPutAwayRouteEntry(entry)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleTrackedDisplayStorageEntries(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        WorkspaceStorageIndex index = request.storageIndex();
        List<WorkspaceStorageIndex.StorageEntry> expected = simpleLiveDisplayPutAwayRouteEntries(index);
        if (!request.trackedDisplayStorageEntries().equals(expected)) {
            return false;
        }
        for (WorkspaceStorageIndex.StorageEntry entry : request.trackedDisplayStorageEntries()) {
            if (!simpleDisplayPutAwayRouteEntry(entry)) {
                return false;
            }
        }
        return true;
    }

    private static List<WorkspaceStorageIndex.StorageEntry> simpleLiveDisplayPutAwayRouteEntries(
            WorkspaceStorageIndex index
    ) {
        if (index == null) {
            return List.of();
        }
        ArrayList<WorkspaceStorageIndex.StorageEntry> out = new ArrayList<>();
        for (WorkspaceStorageIndex.StorageEntry entry : index.liveTrackedDisplayEntries()) {
            if (simpleDisplayPutAwayRouteEntry(entry)) {
                out.add(entry);
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static boolean simpleStorageBaseRequest(WorkspaceProjectionRequest request) {
        if (request == null) {
            return false;
        }
        if (!workflowHasOnlyVisualHomes(request.workflow())) {
            return false;
        }
        if (!request.remoteDetailIdentities().isEmpty()
                || !request.searchQuery().isBlank()
                || request.remoteStorageDetailIntent().includesSearchMatches()
                || request.remoteStorageDetailIntent().includesAllRemote()) {
            return false;
        }
        if (!request.proximateStorageIds().isEmpty()
                || !request.worldDisplaySources().isEmpty()
                || !request.contextualSuggestionStorageIds().isEmpty()
                || !request.contextualSuggestionDisplaySources().isEmpty()
                || !request.depositEligibleStorageIds().isEmpty()) {
            return false;
        }
        return request.signalExtractor() == null
                && request.chestContentsResolver() == null
                && request.carriedContainerInfoResolver() == null
                && request.lootChestSource() == null
                && request.liveChestContentPresence() == null
                && request.liveStorageAffinityEligibility() == null
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(request.activeChestPanel());
    }

    private static boolean simpleStorageCachedView(SlotWorkspaceViewModel view) {
        if (view == null) {
            return false;
        }
        return simpleCarriedCards(view.atlasItems())
                && simpleCarriedCards(view.triageItems())
                && simpleStorageChips(view.chestChips())
                && view.chestClusters().isEmpty()
                && view.kits().isEmpty()
                && view.wayfindingTargets().isEmpty()
                && view.depositableIdentities().isEmpty()
                && view.recentIdentities().isEmpty()
                && view.contextualSuggestionLanes().isEmpty()
                && SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel());
    }

    private static boolean simpleCarriedStorageCachedView(SlotWorkspaceViewModel view) {
        if (view == null) {
            return false;
        }
        return simpleCarriedStorageCards(view.atlasItems())
                && simpleCarriedStorageCards(view.triageItems())
                && simpleCarriedStorageChips(view.chestChips())
                && simpleStorageClusters(view.chestClusters())
                && view.kits().isEmpty()
                && simpleWayfindingTargets(view.wayfindingTargets())
                && simpleIdentityRefs(view.depositableIdentities())
                && view.recentIdentities().isEmpty()
                && simpleCarriedStorageContextualSuggestionLanes(view.contextualSuggestionLanes())
                && SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                && SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel());
    }

    private static boolean simpleCarriedStorageCachedView(
            SlotWorkspaceViewModel view,
            WorkspaceProjectionRequest request
    ) {
        if (view == null) {
            return false;
        }
        boolean allowRemoteXray = simpleTrackedXray(request);
        if (!simpleCarriedStorageCards(view.atlasItems(), allowRemoteXray)
                || !simpleCarriedStorageCards(view.triageItems(), allowRemoteXray)
                || !simpleCarriedStorageChips(view.chestChips())
                || !simpleStorageClusters(view.chestClusters())
                || !view.kits().isEmpty()
                || !simpleCarriedStorageWayfindingTargets(view.wayfindingTargets())
                || !simpleIdentityRefs(view.depositableIdentities())
                || !view.recentIdentities().isEmpty()
                || !simpleCarriedStorageContextualSuggestionLanes(view.contextualSuggestionLanes())
                || !SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                || !SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel())) {
            return false;
        }
        ClaimedChestMap claimedChests = request == null
                ? ClaimedChestMap.empty()
                : request.workflow().claimedChestMap();
        ChestClusterMap expectedClusters = ChestClusterMap.derive(claimedChests);
        ChestAffinityMap affinityMap = request == null
                ? ChestAffinityMap.empty()
                : request.workflow().chestAffinityMap().decayed(request.currentTick());
        return simpleStorageClustersMatch(view.chestClusters(), expectedClusters, simpleClusterLabels(request))
                && simpleStorageChipClustersMatch(view.chestChips(), expectedClusters)
                && simpleStorageChipAffinitiesMatch(view.chestChips(), affinityMap);
    }

    private static boolean simpleWorkflowCachedView(
            SlotWorkspaceViewModel view,
            WorkspaceProjectionRequest request
    ) {
        if (view == null) {
            return false;
        }
        if (!simpleWorkflowCards(view.atlasItems())
                || !simpleWorkflowCards(view.triageItems())
                || !simpleCarriedStorageChips(view.chestChips())
                || !simpleStorageClusters(view.chestClusters())
                || !simpleKitCards(view.kits())
                || !simpleWorkflowWayfindingTargets(view.wayfindingTargets())
                || !simpleWorkflowDepositableIdentities(view.depositableIdentities(), request)
                || !view.recentIdentities().isEmpty()
                || !simpleWorkflowContextualSuggestionLanes(view.contextualSuggestionLanes())
                || !SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                || !SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel())) {
            return false;
        }
        ClaimedChestMap claimedChests = request == null
                ? ClaimedChestMap.empty()
                : request.workflow().claimedChestMap();
        ChestClusterMap expectedClusters = ChestClusterMap.derive(claimedChests);
        ChestAffinityMap affinityMap = request == null
                ? ChestAffinityMap.empty()
                : request.workflow().chestAffinityMap().decayed(request.currentTick());
        return simpleStorageClustersMatch(view.chestClusters(), expectedClusters, simpleClusterLabels(request))
                && simpleStorageChipClustersMatch(view.chestChips(), expectedClusters)
                && simpleStorageChipAffinitiesMatch(view.chestChips(), affinityMap);
    }

    private static boolean simpleWorkflowCachedViewForStorageChange(
            SlotWorkspaceViewModel view,
            WorkspaceProjectionRequest request,
            Set<String> changedStorageIds
    ) {
        if (view == null) {
            return false;
        }
        if (!simpleWorkflowCards(view.atlasItems())
                || !simpleWorkflowCards(view.triageItems())
                || !simpleCarriedStorageChips(view.chestChips())
                || !simpleStorageClusters(view.chestClusters())
                || !simpleKitCards(view.kits())
                || !simpleWorkflowWayfindingTargets(view.wayfindingTargets())
                || !simpleWorkflowDepositableIdentities(view.depositableIdentities(), request)
                || !view.recentIdentities().isEmpty()
                || !simpleWorkflowContextualSuggestionLanes(view.contextualSuggestionLanes())
                || !SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                || !SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel())) {
            return false;
        }
        ClaimedChestMap claimedChests = request == null
                ? ClaimedChestMap.empty()
                : request.workflow().claimedChestMap();
        ChestClusterMap expectedClusters = ChestClusterMap.derive(claimedChests);
        ChestAffinityMap affinityMap = request == null
                ? ChestAffinityMap.empty()
                : request.workflow().chestAffinityMap().decayed(request.currentTick());
        return simpleStorageClusterTopologyMatches(view.chestClusters(), expectedClusters, changedStorageIds)
                && simpleStorageChipClustersMatch(view.chestChips(), expectedClusters, changedStorageIds)
                && simpleStorageChipAffinitiesMatch(view.chestChips(), affinityMap, changedStorageIds);
    }

    private static boolean simpleWorkflowDepositableIdentities(
            Set<SlotWorkspaceViewModel.IdentityRef> refs,
            WorkspaceProjectionRequest request
    ) {
        if (refs == null || refs.isEmpty()) {
            return true;
        }
        return request != null
                && !request.depositEligibleStorageIds().isEmpty()
                && simpleIdentityRefs(refs);
    }

    private static boolean simpleCarriedStorageCachedViewForStorageChange(
            SlotWorkspaceViewModel view,
            WorkspaceProjectionRequest request,
            Set<String> changedStorageIds
    ) {
        return simpleCarriedStorageCachedViewForStorageChange(view, request, changedStorageIds, Set.of());
    }

    private static boolean simpleCarriedStorageCachedViewForStorageChange(
            SlotWorkspaceViewModel view,
            WorkspaceProjectionRequest request,
            Set<String> changedStorageIds,
            Set<ItemIdentity> ignoredIdentities
    ) {
        if (view == null) {
            return false;
        }
        boolean allowRemoteXray = simpleTrackedXray(request);
        if (!simpleCarriedStorageCards(view.atlasItems(), allowRemoteXray, ignoredIdentities)
                || !simpleCarriedStorageCards(view.triageItems(), allowRemoteXray, ignoredIdentities)
                || !simpleCarriedStorageChips(view.chestChips())
                || !simpleStorageClusters(view.chestClusters())
                || !view.kits().isEmpty()
                || !simpleCarriedStorageWayfindingTargets(view.wayfindingTargets())
                || !simpleIdentityRefs(view.depositableIdentities())
                || !view.recentIdentities().isEmpty()
                || !simpleCarriedStorageContextualSuggestionLanes(view.contextualSuggestionLanes(), ignoredIdentities)
                || !SlotWorkspaceViewModel.LootChestPanel.empty().equals(view.lootChestPanel())
                || !SlotWorkspaceViewModel.ActiveChestPanel.empty().equals(view.activeChestPanel())) {
            return false;
        }
        ClaimedChestMap claimedChests = request == null
                ? ClaimedChestMap.empty()
                : request.workflow().claimedChestMap();
        ChestClusterMap expectedClusters = ChestClusterMap.derive(claimedChests);
        ChestAffinityMap affinityMap = request == null
                ? ChestAffinityMap.empty()
                : request.workflow().chestAffinityMap().decayed(request.currentTick());
        return simpleStorageClusterTopologyMatches(view.chestClusters(), expectedClusters, changedStorageIds)
                && simpleStorageChipClustersMatch(view.chestChips(), expectedClusters, changedStorageIds)
                && simpleStorageChipAffinitiesMatch(view.chestChips(), affinityMap, changedStorageIds);
    }

    private static boolean simpleStorageChips(List<SlotWorkspaceViewModel.ChestChip> chips) {
        if (chips == null || chips.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ChestChip chip : chips) {
            if (chip == null || chip.affinityIdentities() != 0 || !simpleStorageClusterId(chip.clusterId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCarriedStorageChips(List<SlotWorkspaceViewModel.ChestChip> chips) {
        if (chips == null || chips.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ChestChip chip : chips) {
            if (chip == null || !simpleStorageClusterId(chip.clusterId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleIdentityRefs(Set<SlotWorkspaceViewModel.IdentityRef> refs) {
        if (refs == null || refs.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.IdentityRef ref : refs) {
            if (ref == null || ref.itemId().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleKitCards(List<SlotWorkspaceViewModel.KitCard> kits) {
        if (kits == null || kits.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.KitCard kit : kits) {
            if (kit == null || kit.kitId().isBlank() || kit.name().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleStorageClusters(List<SlotWorkspaceViewModel.ChestClusterDescriptor> clusters) {
        if (clusters == null || clusters.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : clusters) {
            if (cluster == null
                    || !simpleStorageClusterId(cluster.clusterId())
                    || cluster.label().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleStorageClustersMatch(
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> actual,
            ChestClusterMap expected
    ) {
        return simpleStorageClustersMatch(actual, expected, Map.of());
    }

    private static boolean simpleStorageClustersMatch(
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> actual,
            ChestClusterMap expected,
            Map<String, String> customLabels
    ) {
        List<SlotWorkspaceViewModel.ChestClusterDescriptor> descriptors =
                actual == null ? List.of() : actual;
        ChestClusterMap clusters = expected == null ? ChestClusterMap.empty() : expected;
        Map<String, String> labels = customLabels == null ? Map.of() : customLabels;
        if (descriptors.size() != clusters.clusters().size()) {
            return false;
        }
        for (ChestClusterMap.Cluster cluster : clusters.clusters()) {
            SlotWorkspaceViewModel.ChestClusterDescriptor descriptor =
                    simpleStorageClusterDescriptor(descriptors, cluster.clusterId());
            String custom = labels.get(cluster.clusterId());
            String expectedLabel = custom == null || custom.isBlank()
                    ? cluster.defaultLabel()
                    : custom;
            if (descriptor == null
                    || !expectedLabel.equals(descriptor.label())
                    || cluster.ordinal() != descriptor.ordinal()) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleStorageClusterTopologyMatches(
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> actual,
            ChestClusterMap expected
    ) {
        return simpleStorageClusterTopologyMatches(actual, expected, Set.of());
    }

    private static boolean simpleStorageClusterTopologyMatches(
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> actual,
            ChestClusterMap expected,
            Set<String> ignoredStorageIds
    ) {
        List<SlotWorkspaceViewModel.ChestClusterDescriptor> descriptors =
                actual == null ? List.of() : actual;
        ChestClusterMap clusters = expected == null ? ChestClusterMap.empty() : expected;
        Set<String> ignored = ignoredStorageIds == null ? Set.of() : ignoredStorageIds;
        for (ChestClusterMap.Cluster cluster : clusters.clusters()) {
            SlotWorkspaceViewModel.ChestClusterDescriptor descriptor =
                    simpleStorageClusterDescriptor(descriptors, cluster.clusterId());
            if (descriptor == null) {
                if (clusterOnlyContainsIgnoredStorage(cluster, ignored)) {
                    continue;
                }
                return false;
            }
            if (cluster.ordinal() != descriptor.ordinal()) {
                return false;
            }
        }
        for (SlotWorkspaceViewModel.ChestClusterDescriptor descriptor : descriptors) {
            if (descriptor == null) {
                return false;
            }
            if (clusters.cluster(descriptor.clusterId()) == null
                    && !clusterDescriptorTouchesIgnoredStorage(descriptor, ignored)) {
                return false;
            }
        }
        return true;
    }

    private static boolean clusterOnlyContainsIgnoredStorage(
            ChestClusterMap.Cluster cluster,
            Set<String> ignoredStorageIds
    ) {
        if (cluster == null || cluster.storageIds().isEmpty()
                || ignoredStorageIds == null || ignoredStorageIds.isEmpty()) {
            return false;
        }
        for (UUID storageId : cluster.storageIds()) {
            if (storageId == null || !ignoredStorageIds.contains(storageId.toString())) {
                return false;
            }
        }
        return true;
    }

    private static boolean clusterDescriptorTouchesIgnoredStorage(
            SlotWorkspaceViewModel.ChestClusterDescriptor descriptor,
            Set<String> ignoredStorageIds
    ) {
        if (descriptor == null || descriptor.clusterId().isBlank()
                || ignoredStorageIds == null || ignoredStorageIds.isEmpty()) {
            return false;
        }
        String prefix = "cluster-";
        if (!descriptor.clusterId().startsWith(prefix)) {
            return false;
        }
        return ignoredStorageIds.contains(descriptor.clusterId().substring(prefix.length()));
    }

    private static SlotWorkspaceViewModel.ChestClusterDescriptor simpleStorageClusterDescriptor(
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> clusters,
            String clusterId
    ) {
        if (clusters == null || clusterId == null || clusterId.isBlank()) {
            return null;
        }
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : clusters) {
            if (cluster != null && clusterId.equals(cluster.clusterId())) {
                return cluster;
            }
        }
        return null;
    }

    private static boolean simpleStorageChipClustersMatch(
            List<SlotWorkspaceViewModel.ChestChip> chips,
            ChestClusterMap expected
    ) {
        return simpleStorageChipClustersMatch(chips, expected, Set.of());
    }

    private static boolean simpleStorageChipClustersMatch(
            List<SlotWorkspaceViewModel.ChestChip> chips,
            ChestClusterMap expected,
            Set<String> ignoredStorageIds
    ) {
        if (chips == null || chips.isEmpty()) {
            return true;
        }
        ChestClusterMap clusters = expected == null ? ChestClusterMap.empty() : expected;
        Set<String> ignored = ignoredStorageIds == null ? Set.of() : ignoredStorageIds;
        for (SlotWorkspaceViewModel.ChestChip chip : chips) {
            if (chip == null) {
                return false;
            }
            if (ignored.contains(chip.storageId())) {
                continue;
            }
            String expectedClusterId = simpleExpectedClusterId(clusters, chip.storageId());
            if (expectedClusterId == null) {
                if (!chip.clusterId().isBlank()) {
                    return false;
                }
                continue;
            }
            if (!expectedClusterId.equals(chip.clusterId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleStorageChipAffinitiesMatch(
            List<SlotWorkspaceViewModel.ChestChip> chips,
            ChestAffinityMap expected
    ) {
        return simpleStorageChipAffinitiesMatch(chips, expected, Set.of());
    }

    private static boolean simpleStorageChipAffinitiesMatch(
            List<SlotWorkspaceViewModel.ChestChip> chips,
            ChestAffinityMap expected,
            Set<String> ignoredStorageIds
    ) {
        if (chips == null || chips.isEmpty()) {
            return true;
        }
        ChestAffinityMap affinityMap = expected == null ? ChestAffinityMap.empty() : expected;
        Set<String> ignored = ignoredStorageIds == null ? Set.of() : ignoredStorageIds;
        for (SlotWorkspaceViewModel.ChestChip chip : chips) {
            if (chip != null && ignored.contains(chip.storageId())) {
                continue;
            }
            if (chip == null || chip.affinityIdentities() != simpleExpectedAffinityCount(
                    affinityMap,
                    chip.storageId())) {
                return false;
            }
        }
        return true;
    }

    private static int simpleExpectedAffinityCount(ChestAffinityMap affinityMap, String storageId) {
        if (affinityMap == null || storageId == null || storageId.isBlank()) {
            return 0;
        }
        try {
            return affinityMap.forChest(UUID.fromString(storageId)).size();
        } catch (IllegalArgumentException exception) {
            return 0;
        }
    }

    private static String simpleExpectedClusterId(ChestClusterMap clusters, String storageId) {
        if (clusters == null || storageId == null || storageId.isBlank()) {
            return null;
        }
        try {
            return clusters.clusterId(UUID.fromString(storageId));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean simpleStorageClusterId(String clusterId) {
        return clusterId == null || clusterId.isBlank() || clusterId.startsWith("cluster-");
    }

    private static Map<String, Integer> simpleClusterOrdinals(
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> clusters
    ) {
        if (clusters == null || clusters.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> ordinals = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : clusters) {
            if (cluster != null && !cluster.clusterId().isBlank()) {
                ordinals.put(cluster.clusterId(), cluster.ordinal());
            }
        }
        return Map.copyOf(ordinals);
    }

    private static boolean simpleCarriedStorageCards(List<SlotWorkspaceViewModel.AtlasItem> items) {
        return simpleCarriedStorageCards(items, false);
    }

    private static boolean simpleCarriedStorageCards(
            List<SlotWorkspaceViewModel.AtlasItem> items,
            boolean allowRemoteXray
    ) {
        return simpleCarriedStorageCards(items, allowRemoteXray, Set.of());
    }

    private static boolean simpleCarriedStorageCards(
            List<SlotWorkspaceViewModel.AtlasItem> items,
            boolean allowRemoteXray,
            Set<ItemIdentity> ignoredIdentities
    ) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (ignoredAtlasItem(item, ignoredIdentities)) {
                continue;
            }
            if (!simpleCarriedStorageCard(item, allowRemoteXray)) {
                return false;
            }
        }
        return true;
    }

    private static boolean ignoredAtlasItem(
            SlotWorkspaceViewModel.AtlasItem item,
            Set<ItemIdentity> ignoredIdentities
    ) {
        return item != null
                && ignoredIdentities != null
                && !ignoredIdentities.isEmpty()
                && ItemIdentityCollections.contains(ignoredIdentities, item.identity().toIdentity());
    }

    private static boolean simpleCarriedStorageCard(SlotWorkspaceViewModel.AtlasItem item) {
        return simpleCarriedStorageCard(item, false);
    }

    private static boolean simpleCarriedStorageCard(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean allowRemoteOnlyGhost
    ) {
        if (item == null
                || item.recent()
                || item.proximateCount() != simplePresenceTotal(item.presence())
                || !item.chipSuggestions().isEmpty()
                || !simplePresence(item.presence())
                || !simpleElsewhere(item.elsewhere())
                || item.kitNeeded()
                || item.desiredCountFromKit()
                || item.acceptedWorkflowInput()
                || item.putAwayState() != SlotWorkspaceViewModel.PutAwayState.NONE) {
            return false;
        }
        if (item.carried()) {
            return !item.ghost();
        }
        int storageTotal = item.proximateCount() > 0
                ? item.proximateCount()
                : simplePresenceTotal(item.elsewhere());
        return item.ghost()
                && item.totalCount() == storageTotal
                && !item.junk()
                && (item.desiredCount() > 0
                        || item.wantedCount() > 0
                        || (allowRemoteOnlyGhost
                                && item.proximateCount() == 0
                                && storageTotal > 0
                                && !SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(item.islandId())
                                && !SlotWorkspaceAtlasLayout.ISLAND_MISC.equals(item.islandId())));
    }

    private static boolean simpleTrackedXray(WorkspaceProjectionRequest request) {
        return request != null && request.remoteStorageDetailIntent() == RemoteStorageDetailIntent.TRACKED_XRAY;
    }

    private static boolean simpleWorldDisplaySources(WorkspaceProjectionRequest request) {
        if (request == null || request.worldDisplaySources().isEmpty()) {
            return true;
        }
        WorkspaceStorageIndex index = request.storageIndex();
        LinkedHashSet<String> sourceIds = new LinkedHashSet<>();
        for (WorldDisplayStorageSource source : request.worldDisplaySources()) {
            if (source == null || source.storageId().isBlank()) {
                return false;
            }
            sourceIds.add(source.storageId());
            StorageTargetRef target = index.target(source.storageId());
            if (target == null || !target.displayTarget() || !target.proximate()) {
                return false;
            }
        }
        for (WorkspaceStorageIndex.StorageEntry entry : index.entries()) {
            if (entry == null || entry.target() == null || !entry.target().proximate()) {
                continue;
            }
            if (!entry.target().displayTarget() || !sourceIds.contains(entry.target().storageId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleProximateStorageIds(WorkspaceProjectionRequest request) {
        if (request == null || request.proximateStorageIds().isEmpty()) {
            return true;
        }
        if (request.chestContentsResolver() == null) {
            return false;
        }
        for (String storageId : request.proximateStorageIds()) {
            if (storageId == null || storageId.isBlank()) {
                return false;
            }
            StorageTargetRef target = request.storageIndex().target(storageId);
            if (target == null || target.displayTarget() || !target.proximate()) {
                return false;
            }
            try {
                if (request.workflow().claimedChestMap().chest(java.util.UUID.fromString(storageId)) == null) {
                    return false;
                }
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        for (WorkspaceStorageIndex.StorageEntry entry : request.storageIndex().entries()) {
            if (entry == null || entry.target() == null || !entry.target().proximate()
                    || entry.target().displayTarget()) {
                continue;
            }
            if (!request.proximateStorageIds().contains(entry.target().storageId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleDepositEligibleStorageIds(WorkspaceProjectionRequest request) {
        if (request == null || request.depositEligibleStorageIds().isEmpty()) {
            return true;
        }
        if (request.proximateStorageIds().isEmpty() || request.chestContentsResolver() == null) {
            return false;
        }
        for (String storageId : request.depositEligibleStorageIds()) {
            if (storageId == null || storageId.isBlank() || !request.proximateStorageIds().contains(storageId)) {
                return false;
            }
            StorageTargetRef target = request.storageIndex().target(storageId);
            if (target == null || target.displayTarget() || !target.proximate()) {
                return false;
            }
            try {
                if (request.workflow().claimedChestMap().chest(UUID.fromString(storageId)) == null) {
                    return false;
                }
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return true;
    }

    private static boolean simplePresence(List<SlotWorkspaceViewModel.ChestPresenceEntry> presence) {
        if (presence == null || presence.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : presence) {
            if (entry == null || entry.storageId().isBlank() || entry.count() <= 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleElsewhere(List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere) {
        if (elsewhere == null || elsewhere.isEmpty()) {
            return true;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : elsewhere) {
            if (entry == null || entry.storageId().isBlank() || entry.count() <= 0) {
                return false;
            }
        }
        return true;
    }

    private static int simplePresenceTotal(List<SlotWorkspaceViewModel.ChestPresenceEntry> presence) {
        if (presence == null || presence.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : presence) {
            if (entry != null) {
                total += Math.max(0, entry.count());
            }
        }
        return total;
    }

    private static boolean simpleWayfindingTargets(List<WayfindingTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return true;
        }
        for (WayfindingTarget target : targets) {
            if (target == null
                    || target.hasKitMissing()
                    || target.hasPutAway()
                    || target.missingIdentities().isEmpty()
                    || (!target.hasDesiredMissing() && !target.hasWantedMissing())
                    || target.totalMissingCount() <= 0
                    || (target.scope() != WayfindingTarget.Scope.PLAYER
                            && target.scope() != WayfindingTarget.Scope.WANTED)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleCarriedStorageWayfindingTargets(List<WayfindingTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return true;
        }
        for (WayfindingTarget target : targets) {
            if (target == null) {
                return false;
            }
            if (target.putAwayOnly()) {
                if (target.scope() != WayfindingTarget.Scope.PUT_AWAY
                        || target.missingIdentities().isEmpty()
                        || target.totalMissingCount() <= 0) {
                    return false;
                }
                continue;
            }
            if (target.hasPutAway()
                    || target.hasKitMissing()
                    || target.missingIdentities().isEmpty()
                    || (!target.hasDesiredMissing() && !target.hasWantedMissing())
                    || target.totalMissingCount() <= 0
                    || (target.scope() != WayfindingTarget.Scope.PLAYER
                            && target.scope() != WayfindingTarget.Scope.WANTED)) {
                return false;
            }
        }
        return true;
    }

    private static boolean simpleWorkflowWayfindingTargets(List<WayfindingTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return true;
        }
        for (WayfindingTarget target : targets) {
            if (target == null
                    || target.missingIdentities().isEmpty()
                    || (!target.hasKitMissing()
                            && !target.hasDesiredMissing()
                            && !target.hasWantedMissing()
                            && !target.hasPutAway())
                    || target.totalMissingCount() <= 0
                    || (target.hasKitMissing() && target.scope() != WayfindingTarget.Scope.KIT)
                    || (target.putAwayOnly() && target.scope() != WayfindingTarget.Scope.PUT_AWAY)
                    || (!target.hasKitMissing() && target.scope() == WayfindingTarget.Scope.KIT)
                    || (target.scope() != WayfindingTarget.Scope.KIT
                            && target.scope() != WayfindingTarget.Scope.PLAYER
                            && target.scope() != WayfindingTarget.Scope.WANTED
                            && target.scope() != WayfindingTarget.Scope.PUT_AWAY)) {
                return false;
            }
        }
        return true;
    }

    private static List<WayfindingTarget> mergeSimpleWayfindingTargets(
            List<WayfindingTarget> left,
            List<WayfindingTarget> right
    ) {
        if ((left == null || left.isEmpty()) && (right == null || right.isEmpty())) {
            return List.of();
        }
        LinkedHashMap<String, SimpleWayfindingMerge> byStorage = new LinkedHashMap<>();
        mergeSimpleWayfindingTargetsInto(byStorage, left);
        mergeSimpleWayfindingTargetsInto(byStorage, right);
        if (byStorage.isEmpty()) {
            return List.of();
        }
        ArrayList<WayfindingTarget> out = new ArrayList<>(byStorage.size());
        for (SimpleWayfindingMerge merge : byStorage.values()) {
            WayfindingTarget target = merge.toTarget();
            if (target != null) {
                out.add(target);
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static void mergeSimpleWayfindingTargetsInto(
            Map<String, SimpleWayfindingMerge> byStorage,
            List<WayfindingTarget> targets
    ) {
        if (byStorage == null || targets == null || targets.isEmpty()) {
            return;
        }
        for (WayfindingTarget target : targets) {
            if (target == null || target.storageId().isBlank()) {
                continue;
            }
            byStorage
                    .computeIfAbsent(target.storageId(), ignored -> new SimpleWayfindingMerge(target))
                    .add(target);
        }
    }

    private record SimplePutAwayRoute(
            String storageId,
            String dimensionId,
            int worldX,
            int worldY,
            int worldZ
    ) {
        private SimplePutAwayRoute {
            storageId = storageId == null ? "" : storageId;
            dimensionId = dimensionId == null ? "" : dimensionId;
        }
    }

    private static final class SimplePutAwayAccumulator {
        private final SimplePutAwayRoute route;
        private final LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        private int totalCount;

        private SimplePutAwayAccumulator(SimplePutAwayRoute route) {
            this.route = route;
        }

        private void add(ItemIdentity identity, int count) {
            if (identity == null || count <= 0) {
                return;
            }
            identities.add(ItemIdentityCollections.key(identity));
            totalCount += Math.max(0, count);
        }

        private WayfindingTarget toTarget() {
            if (route == null || route.storageId().isBlank() || identities.isEmpty()) {
                return null;
            }
            return new WayfindingTarget(
                    route.storageId(),
                    route.dimensionId(),
                    route.worldX(),
                    route.worldY(),
                    route.worldZ(),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.copyOf(identities),
                    totalCount,
                    WayfindingTarget.Scope.PUT_AWAY);
        }
    }

    private static final class SimpleWayfindingMerge {
        private final String storageId;
        private final String dimensionId;
        private final int worldX;
        private final int worldY;
        private final int worldZ;
        private final LinkedHashSet<ItemIdentity> missing = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> kit = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> desired = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> wanted = new LinkedHashSet<>();
        private final LinkedHashSet<ItemIdentity> putAway = new LinkedHashSet<>();
        private int totalCount;

        private SimpleWayfindingMerge(WayfindingTarget target) {
            this.storageId = target == null ? "" : target.storageId();
            this.dimensionId = target == null ? "" : target.dimensionId();
            this.worldX = target == null ? 0 : target.worldX();
            this.worldY = target == null ? 0 : target.worldY();
            this.worldZ = target == null ? 0 : target.worldZ();
        }

        private void add(WayfindingTarget target) {
            if (target == null) {
                return;
            }
            addAll(missing, target.missingIdentities());
            addAll(kit, target.kitMissingIdentities());
            addAll(desired, target.desiredMissingIdentities());
            addAll(wanted, target.wantedMissingIdentities());
            addAll(putAway, target.putAwayIdentities());
            totalCount += Math.max(0, target.totalMissingCount());
        }

        private WayfindingTarget toTarget() {
            if (storageId.isBlank() || missing.isEmpty() || totalCount <= 0) {
                return null;
            }
            return new WayfindingTarget(
                    storageId,
                    dimensionId,
                    worldX,
                    worldY,
                    worldZ,
                    Set.copyOf(missing),
                    Set.copyOf(kit),
                    Set.copyOf(desired),
                    Set.copyOf(wanted),
                    Set.copyOf(putAway),
                    totalCount,
                    null);
        }

        private static void addAll(LinkedHashSet<ItemIdentity> output, Set<ItemIdentity> identities) {
            if (output == null || identities == null || identities.isEmpty()) {
                return;
            }
            for (ItemIdentity identity : identities) {
                if (identity != null) {
                    output.add(ItemIdentityCollections.key(identity));
                }
            }
        }
    }

    private static String fullProjectionReason(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.requiresFullProjection()) {
            return resolved.fallbackDiagnostics().isBlank()
                    ? "invalidation_requires_full_projection"
                    : resolved.fallbackDiagnostics();
        }
        return resolved.invalidationCount() == 0 ? "structural_key_changed" : "localized_incremental_projection_not_enabled";
    }

    private static boolean invalidationRequiresStructuralRefresh(WorkspaceInvalidationSummary invalidations) {
        WorkspaceInvalidationSummary resolved = invalidations == null
                ? WorkspaceInvalidationSummary.empty()
                : invalidations;
        if (resolved.invalidationCount() == 0) {
            return false;
        }
        if (resolved.requiresFullProjection()
                || !resolved.identities().isEmpty()
                || !resolved.storageIds().isEmpty()
                || !resolved.sectionIds().isEmpty()) {
            return true;
        }
        if (resolved.slices().isEmpty()) {
            return true;
        }
        for (WorkspaceProjectionSlice slice : resolved.slices()) {
            if (slice != WorkspaceProjectionSlice.FRAME) {
                return true;
            }
        }
        return false;
    }

    private static int projectionFactCount(SlotWorkspaceViewModel viewModel) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        int count = 0;
        count += resolved.islands().size();
        count += resolved.atlasItems().size();
        count += resolved.triageItems().size();
        count += resolved.chestChips().size();
        count += resolved.chestClusters().size();
        count += resolved.hotbarSlots().size();
        count += resolved.offhand() == null ? 0 : 1;
        count += resolved.kits().size();
        count += resolved.wayfindingTargets().size();
        count += resolved.depositableIdentities().size();
        count += resolved.contextualSuggestionLanes().size();
        if (resolved.lootChestPanel() != null) {
            count++;
            count += resolved.lootChestPanel().items().size();
        }
        if (resolved.activeChestPanel() != null) {
            count++;
        }
        return Math.max(0, count);
    }

    public record Diagnostics(
            long projectionCount,
            long structuralHits,
            long structuralMisses,
            boolean structuralCacheHit,
            ItemIdentityMatcher.MemoStats identityMemoStats,
            WorkspaceProjectionTiming timing,
            WorkspaceInvalidationSummary invalidations,
            String fullProjectionReason,
            long projectionFactsUpdated,
            long projectionFactsReused,
            WorkspaceCardProjectionStats cardProjectionStats,
            WorkspaceStorageProjectionStats storageProjectionStats,
            WorkspaceEdgeProjectionStats edgeProjectionStats,
            WorkspaceProjectionSliceStats projectionSliceStats
    ) {
        public Diagnostics {
            identityMemoStats = identityMemoStats == null
                    ? ItemIdentityMatcher.MemoStats.empty()
                    : identityMemoStats;
            timing = timing == null ? WorkspaceProjectionTiming.empty() : timing;
            invalidations = invalidations == null ? WorkspaceInvalidationSummary.empty() : invalidations;
            fullProjectionReason = fullProjectionReason == null ? "" : fullProjectionReason;
            projectionFactsUpdated = Math.max(0L, projectionFactsUpdated);
            projectionFactsReused = Math.max(0L, projectionFactsReused);
            cardProjectionStats = cardProjectionStats == null
                    ? WorkspaceCardProjectionStats.empty()
                    : cardProjectionStats;
            storageProjectionStats = storageProjectionStats == null
                    ? WorkspaceStorageProjectionStats.empty()
                    : storageProjectionStats;
            edgeProjectionStats = edgeProjectionStats == null
                    ? WorkspaceEdgeProjectionStats.empty()
                    : edgeProjectionStats;
            projectionSliceStats = projectionSliceStats == null
                    ? WorkspaceProjectionSliceStats.empty()
                    : projectionSliceStats;
        }

        static Diagnostics empty() {
            return new Diagnostics(
                    0L,
                    0L,
                    0L,
                    false,
                    ItemIdentityMatcher.MemoStats.empty(),
                    WorkspaceProjectionTiming.empty(),
                    WorkspaceInvalidationSummary.empty(),
                    "",
                    0L,
                    0L,
                    WorkspaceCardProjectionStats.empty(),
                    WorkspaceStorageProjectionStats.empty(),
                    WorkspaceEdgeProjectionStats.empty(),
                    WorkspaceProjectionSliceStats.empty());
        }
    }

    private record CardReuse(
            SlotWorkspaceViewModel viewModel,
            WorkspaceCardProjectionStats stats
    ) {
        private CardReuse {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            stats = stats == null ? WorkspaceCardProjectionStats.empty() : stats;
        }
    }

    private record StorageReuse(
            SlotWorkspaceViewModel viewModel,
            WorkspaceStorageProjectionStats stats
    ) {
        private StorageReuse {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            stats = stats == null ? WorkspaceStorageProjectionStats.empty() : stats;
        }
    }

    private record EdgeReuse(
            SlotWorkspaceViewModel viewModel,
            WorkspaceEdgeProjectionStats stats
    ) {
        private EdgeReuse {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            stats = stats == null ? WorkspaceEdgeProjectionStats.empty() : stats;
        }
    }

    private record SliceReuse(
            SlotWorkspaceViewModel viewModel,
            WorkspaceProjectionSliceStats stats
    ) {
        private SliceReuse {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            stats = stats == null ? WorkspaceProjectionSliceStats.empty() : stats;
        }
    }

    private record LocalizedProjection(
            boolean applied,
            SlotWorkspaceViewModel viewModel,
            WorkspaceProjectionStore store,
            long factsUpdated
    ) {
        private LocalizedProjection {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            store = store == null ? WorkspaceProjectionStore.empty() : store;
            factsUpdated = Math.max(0L, factsUpdated);
        }

        private static LocalizedProjection notApplied() {
            return new LocalizedProjection(false, SlotWorkspaceViewModel.empty(), WorkspaceProjectionStore.empty(), 0L);
        }
    }
}
