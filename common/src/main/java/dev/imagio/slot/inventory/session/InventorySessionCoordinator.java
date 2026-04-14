package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryTargetCanonicalizer;
import dev.imagio.slot.inventory.browse.HeuristicInventoryCategoryResolver;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocumentQueries;
import dev.imagio.slot.inventory.browse.InventoryBrowseRequest;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryCategoryOverrides;
import dev.imagio.slot.inventory.browse.InventoryCategoryResolver;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class InventorySessionCoordinator {
    private final InventorySessionSource sessionSource;
    private final WorkflowDomainRuntime workflowRuntime;
    private final InventoryActionDispatcher actionDispatcher;
    private final Function<InventoryEntrySnapshot, ItemIdentity> identityResolver;
    private final InventoryCategoryResolver categoryResolver;
    private final String sessionId;
    private final Map<String, PendingInventoryAction> pendingActionsByRequestId = new LinkedHashMap<>();
    private final Map<String, DispatchContinuation> continuationsByRequestId = new LinkedHashMap<>();

    private InventoryHostDescriptor host;
    private InventoryAuthoritySnapshot authority;
    private InventorySessionSnapshot snapshot;
    private long nextRevision;

    public InventorySessionCoordinator(
            InventorySessionSource sessionSource,
            WorkflowDomainRuntime workflowRuntime,
            InventoryActionDispatcher actionDispatcher
    ) {
        this(
                sessionSource,
                workflowRuntime,
                actionDispatcher,
                entry -> entry == null || !entry.present() ? null : ItemIdentityMatcher.create(entry.stack()),
                new HeuristicInventoryCategoryResolver(InventoryCategoryOverrides.empty())
        );
    }

    public InventorySessionCoordinator(
            InventorySessionSource sessionSource,
            WorkflowDomainRuntime workflowRuntime,
            InventoryActionDispatcher actionDispatcher,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            InventoryCategoryResolver categoryResolver
    ) {
        this.sessionSource = Objects.requireNonNull(sessionSource, "sessionSource");
        this.workflowRuntime = Objects.requireNonNull(workflowRuntime, "workflowRuntime");
        this.actionDispatcher = actionDispatcher == null ? request -> { } : actionDispatcher;
        this.identityResolver = identityResolver == null
                ? entry -> entry == null || !entry.present() ? null : ItemIdentityMatcher.create(entry.stack())
                : identityResolver;
        this.categoryResolver = categoryResolver == null
                ? new HeuristicInventoryCategoryResolver(InventoryCategoryOverrides.empty())
                : categoryResolver;
        this.sessionId = UUID.randomUUID().toString();
        this.authority = InventoryAuthoritySnapshot.empty();
        this.snapshot = InventorySessionSnapshot.empty();
        this.nextRevision = 1L;
        refresh("session.init");
    }

    public WorkflowDomainRuntime workflowRuntime() {
        return workflowRuntime;
    }

    public InventorySessionSnapshot snapshot() {
        return snapshot;
    }

    public InventorySessionToken sessionToken() {
        return snapshot.token();
    }

    public InventorySessionSnapshot refresh(String origin) {
        InventoryHostDescriptor resolvedHost = sessionSource.resolveHost();
        InventoryAuthoritySnapshot resolvedAuthority = readAuthority(resolvedHost);
        resetPendingIfHostChanged(resolvedHost);
        return rebuildSnapshot(resolvedHost, resolvedAuthority, origin == null ? "" : origin);
    }

    public InventorySessionSnapshot invalidate(String origin) {
        InventoryHostDescriptor previousHost = host;
        InventoryAuthoritySnapshot previousAuthority = authority;
        InventoryHostDescriptor resolvedHost = sessionSource.resolveHost();
        InventoryAuthoritySnapshot resolvedAuthority = readAuthority(resolvedHost);
        resetPendingIfHostChanged(resolvedHost);

        if (pendingActionsByRequestId.isEmpty()
                && previousHost != null
                && resolvedHost != null
                && previousHost.hostId().equals(resolvedHost.hostId())) {
            for (InventoryActivityEvent activityEvent : InventoryAuthorityDiffClassifier.classifyCarriedAcquisitions(
                    previousAuthority,
                    resolvedAuthority,
                    identityResolver
            )) {
                workflowRuntime.recordActivityEvent(
                        activityEvent,
                        new DomainEventMetadata("activity.authority_diff", "", "", sessionId)
                );
            }
        }

        return rebuildSnapshot(resolvedHost, resolvedAuthority, origin == null ? "" : origin);
    }

    public InventorySessionSnapshot publishCurrent(String diagnostics) {
        return rebuildSnapshot(host, authority, diagnostics);
    }

    public InventorySessionSnapshot dispatch(String sequenceId, InventoryActionDispatchNode node) {
        if (node == null || node.request() == null) {
            return snapshot;
        }
        dispatchNode(sequenceId, node);
        return publishCurrent("dispatch:" + node.request().requestId());
    }

    public InventorySessionSnapshot dispatchAll(String sequenceId, List<InventoryActionDispatchNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return snapshot;
        }
        boolean dispatched = false;
        for (InventoryActionDispatchNode node : nodes) {
            if (node == null || node.request() == null) {
                continue;
            }
            dispatchNode(sequenceId, node);
            dispatched = true;
        }
        return dispatched ? publishCurrent("dispatch_batch:" + sequenceId) : snapshot;
    }

    public InventorySessionSnapshot ingestOutcome(InventoryActionOutcome outcome) {
        if (outcome == null) {
            return snapshot;
        }

        pendingActionsByRequestId.remove(outcome.requestId());
        DispatchContinuation continuation = continuationsByRequestId.remove(outcome.requestId());

        if (outcome.successful()) {
            workflowRuntime.recordOutcome(outcome);
        }

        if (continuation != null) {
            InventoryActionDispatchNode next = outcome.successful() ? continuation.onSuccess() : continuation.onFailure();
            if (next != null && next.request() != null) {
                dispatchNode(continuation.sequenceId(), next);
            }
        }

        InventoryHostDescriptor resolvedHost = sessionSource.resolveHost();
        InventoryAuthoritySnapshot resolvedAuthority = readAuthority(resolvedHost);
        resetPendingIfHostChanged(resolvedHost);
        return rebuildSnapshot(resolvedHost, resolvedAuthority, "outcome:" + outcome.requestId());
    }

    public boolean hasPendingConflict(Set<InventoryActionTarget> targets) {
        if (host == null || targets == null || targets.isEmpty()) {
            return false;
        }
        LinkedHashSet<String> targetKeys = new LinkedHashSet<>();
        for (InventoryActionTarget target : targets) {
            if (target != null) {
                targetKeys.add(InventoryTargetCanonicalizer.canonicalKey(host, target));
            }
        }
        if (targetKeys.isEmpty()) {
            return false;
        }
        for (PendingInventoryAction pendingAction : pendingActionsByRequestId.values()) {
            if (pendingAction == null || !host.hostId().equals(pendingAction.hostId())) {
                continue;
            }
            for (String targetKey : targetKeys) {
                if (pendingAction.targetKeys().contains(targetKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String nextSequenceId() {
        return UUID.randomUUID().toString();
    }

    private void dispatchNode(String sequenceId, InventoryActionDispatchNode node) {
        if (node == null || node.request() == null) {
            return;
        }
        InventoryActionRequest request = node.request();
        pendingActionsByRequestId.put(request.requestId(), PendingInventoryAction.of(sequenceId, host, request));
        continuationsByRequestId.put(request.requestId(), new DispatchContinuation(sequenceId, node.onSuccess(), node.onFailure()));
        actionDispatcher.dispatch(request);
    }

    private InventorySessionSnapshot rebuildSnapshot(
            InventoryHostDescriptor nextHost,
            InventoryAuthoritySnapshot nextAuthority,
            String diagnostics
    ) {
        InventoryAuthoritySnapshot resolvedAuthority = nextAuthority == null ? InventoryAuthoritySnapshot.empty() : nextAuthority;
        WorkflowDomainSnapshot workflow = workflowRuntime.snapshot();
        InventoryBrowseDocument browseDocument = dev.imagio.slot.inventory.browse.InventoryBrowseService.browse(new InventoryBrowseRequest(
                resolvedAuthority,
                workflow,
                workflow.browsePreferences(),
                workflow.browseSessionState(),
                identityResolver,
                categoryResolver
        ));

        InventoryBrowseSessionState browseSessionState = workflow.browseSessionState();
        if (browseSessionState.selectedSubject() != null
                && !InventoryBrowseDocumentQueries.containsSubject(browseDocument, browseSessionState.selectedSubject())) {
            workflowRuntime.browseSessionState().update(state -> new InventoryBrowseSessionState(
                    state.filter(),
                    state.sortMode(),
                    state.groupingMode(),
                    state.paneMode(),
                    state.activePane(),
                    state.selectedCollectionId(),
                    state.selectedLoadoutId(),
                    state.pinnedToolId(),
                    state.bulkActionScope(),
                    null,
                    state.expandedSectionIds()
            ));
            workflow = workflowRuntime.snapshot();
            browseDocument = dev.imagio.slot.inventory.browse.InventoryBrowseService.browse(new InventoryBrowseRequest(
                    resolvedAuthority,
                    workflow,
                    workflow.browsePreferences(),
                    workflow.browseSessionState(),
                    identityResolver,
                    categoryResolver
            ));
        }

        this.host = nextHost;
        this.authority = resolvedAuthority;
        this.snapshot = new InventorySessionSnapshot(
                new InventorySessionToken(sessionId, nextRevision++),
                nextHost,
                resolvedAuthority,
                workflow,
                browseDocument,
                List.copyOf(pendingActionsByRequestId.values()),
                diagnostics == null ? "" : diagnostics
        );
        return snapshot;
    }

    private InventoryAuthoritySnapshot readAuthority(InventoryHostDescriptor resolvedHost) {
        if (resolvedHost == null) {
            return InventoryAuthoritySnapshot.empty();
        }
        InventoryAuthoritySnapshot resolved = sessionSource.readAuthority(resolvedHost);
        return resolved == null ? InventoryAuthoritySnapshot.empty() : resolved;
    }

    private void resetPendingIfHostChanged(InventoryHostDescriptor resolvedHost) {
        if (host == null || resolvedHost == null || host.hostId().equals(resolvedHost.hostId())) {
            return;
        }
        pendingActionsByRequestId.clear();
        continuationsByRequestId.clear();
    }

    private record DispatchContinuation(
            String sequenceId,
            InventoryActionDispatchNode onSuccess,
            InventoryActionDispatchNode onFailure
    ) {
    }
}
