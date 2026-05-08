package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.action.ProjectedRowTransferIntent;
import dev.imagio.slot.inventory.action.ProjectedRowTransferPlan;
import dev.imagio.slot.inventory.action.ProjectedRowTransferPlanner;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.inventory.intent.InventoryBrowseIntent;
import dev.imagio.slot.inventory.intent.InventoryIntent;
import dev.imagio.slot.inventory.intent.InventoryMutationIntent;
import dev.imagio.slot.inventory.intent.InventoryWorkflowIntent;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.LoadoutApplyService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class InventoryIntentRouter {
    private final InventorySessionCoordinator coordinator;

    public InventoryIntentRouter(InventorySessionCoordinator coordinator) {
        this.coordinator = Objects.requireNonNull(coordinator, "coordinator");
    }

    public InventoryCommandPreflight preflight(InventoryCommandInvocation invocation) {
        return InventoryCommandPreflightService.preflight(coordinator.snapshot(), invocation);
    }

    public InventoryIntentRoutingResult route(InventoryCommandInvocation invocation) {
        InventoryCommandPreflight preflight = preflight(invocation);
        if (!preflight.availability().available()) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    preflight.availability().reasonCodes(),
                    preflight.diagnostics()
            );
        }
        if (preflight.resolvedIntent() == null) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    preflight.availability().reasonCodes(),
                    preflight.diagnostics().isBlank() ? "missing_preflight_intent" : preflight.diagnostics()
            );
        }
        return route(
                invocation.sessionToken(),
                preflight.resolvedIntent(),
                preflight.transferPlan(),
                preflight.loadoutPlan()
        );
    }

    public InventoryIntentRoutingResult route(
            InventorySessionToken expectedToken,
            InventoryIntent intent
    ) {
        return route(expectedToken, intent, null, null);
    }

    private InventoryIntentRoutingResult route(
            InventorySessionToken expectedToken,
            InventoryIntent intent,
            ProjectedRowTransferPlan precomputedTransferPlan,
            LoadoutApplyService.LoadoutApplyPlan precomputedLoadoutPlan
    ) {
        InventorySessionSnapshot session = coordinator.snapshot();
        if (!sameToken(session.token(), expectedToken)) {
            return InventoryIntentRoutingResult.rejected(
                    session,
                    List.of(InventoryCommandReasonCode.INVALID_INTENT),
                    "stale_session_revision"
            );
        }
        if (intent == null) {
            return InventoryIntentRoutingResult.rejected(
                    session,
                    List.of(InventoryCommandReasonCode.INVALID_INTENT),
                    "missing_intent"
            );
        }

        RouteTrace trace = new RouteTrace(
                coordinator.nextSequenceId(),
                UUID.randomUUID().toString(),
                session.token().sessionId() + ":" + session.token().revision(),
                session.token().sessionId()
        );

        if (intent instanceof InventoryBrowseIntent.UpdateBrowseState updateBrowseState) {
            coordinator.workflowRuntime().browseSessionState().replaceWith(updateBrowseState.state());
            return applied(coordinator.publishCurrent(updateBrowseState.origin()), updateBrowseState.origin());
        }
        if (intent instanceof InventoryBrowseIntent.UpdateFilter updateFilter) {
            coordinator.workflowRuntime().browseSessionState().update(state -> new InventoryBrowseSessionState(
                    updateFilter.filter(),
                    state.sortMode(),
                    state.groupingMode(),
                    state.paneMode(),
                    state.activePane(),
                    state.selectedCollectionId(),
                    state.selectedLoadoutId(),
                    state.pinnedToolId(),
                    state.bulkActionScope(),
                    state.selectedSubject(),
                    state.expandedSectionIds()
            ));
            return applied(coordinator.publishCurrent(updateFilter.origin()), updateFilter.origin());
        }
        if (intent instanceof InventoryBrowseIntent.SelectPane selectPane) {
            coordinator.workflowRuntime().browseSessionState().update(state -> new InventoryBrowseSessionState(
                    state.filter(),
                    state.sortMode(),
                    state.groupingMode(),
                    state.paneMode(),
                    selectPane.paneMembership(),
                    state.selectedCollectionId(),
                    state.selectedLoadoutId(),
                    state.pinnedToolId(),
                    state.bulkActionScope(),
                    state.selectedSubject(),
                    state.expandedSectionIds()
            ));
            return applied(coordinator.publishCurrent(selectPane.origin()), selectPane.origin());
        }
        if (intent instanceof InventoryBrowseIntent.SelectSubject selectSubject) {
            coordinator.workflowRuntime().browseSessionState().update(state -> new InventoryBrowseSessionState(
                    state.filter(),
                    state.sortMode(),
                    state.groupingMode(),
                    state.paneMode(),
                    state.activePane(),
                    state.selectedCollectionId(),
                    state.selectedLoadoutId(),
                    state.pinnedToolId(),
                    state.bulkActionScope(),
                    selectSubject.subjectRef(),
                    state.expandedSectionIds()
            ));
            return applied(coordinator.publishCurrent(selectSubject.origin()), selectSubject.origin());
        }
        if (intent instanceof InventoryBrowseIntent.PinTool pinTool) {
            coordinator.workflowRuntime().browseSessionState().update(state -> new InventoryBrowseSessionState(
                    state.filter(),
                    state.sortMode(),
                    state.groupingMode(),
                    state.paneMode(),
                    state.activePane(),
                    state.selectedCollectionId(),
                    state.selectedLoadoutId(),
                    pinTool.toolId(),
                    state.bulkActionScope(),
                    state.selectedSubject(),
                    state.expandedSectionIds()
            ));
            return applied(coordinator.publishCurrent(pinTool.origin()), pinTool.origin());
        }
        if (intent instanceof InventoryWorkflowIntent.ToggleFavorite toggleFavorite) {
            boolean changed = coordinator.workflowRuntime().collectionWorkflow().toggleFavorite(
                    toggleFavorite.identity(),
                    metadata(toggleFavorite.origin(), trace)
            );
            return changed
                    ? applied(coordinator.publishCurrent(toggleFavorite.origin()), toggleFavorite.origin())
                    : noOp(toggleFavorite.origin(), null, null);
        }
        if (intent instanceof InventoryWorkflowIntent.ToggleCollectionMembership toggleCollectionMembership) {
            boolean changed = coordinator.workflowRuntime().collectionWorkflow().toggleCollectionMembership(
                    toggleCollectionMembership.identity(),
                    toggleCollectionMembership.collectionId(),
                    metadata(toggleCollectionMembership.origin(), trace)
            );
            return changed
                    ? applied(coordinator.publishCurrent(toggleCollectionMembership.origin()), toggleCollectionMembership.origin())
                    : noOp(toggleCollectionMembership.origin(), null, null);
        }
        if (intent instanceof InventoryWorkflowIntent.SelectLoadout selectLoadout) {
            boolean changed = coordinator.workflowRuntime().collectionWorkflow().selectLoadout(
                    selectLoadout.collectionId(),
                    selectLoadout.loadoutId()
            );
            if (changed) {
                setSelectedSubject(new InventoryBrowseSubjectRef.LoadoutRef(selectLoadout.collectionId(), selectLoadout.loadoutId()));
            }
            return changed
                    ? applied(coordinator.publishCurrent(selectLoadout.origin()), selectLoadout.origin())
                    : noOp(selectLoadout.origin(), null, null);
        }
        if (intent instanceof InventoryWorkflowIntent.CaptureLoadout captureLoadout) {
            coordinator.workflowRuntime().collectionWorkflow().captureNewLoadout(
                    captureLoadout.collectionId(),
                    captureLoadout.loadoutName(),
                    coordinator.snapshot().authority(),
                    candidate -> candidate == null || !candidate.present()
                            ? null
                            : dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(candidate.stack()),
                    metadata(captureLoadout.origin(), trace)
            );
            return applied(coordinator.publishCurrent(captureLoadout.origin()), captureLoadout.origin());
        }
        if (intent instanceof InventoryWorkflowIntent.ApplyLoadout applyLoadout) {
            return routeApplyLoadout(
                    applyLoadout,
                    trace,
                    precomputedLoadoutPlan
            );
        }
        if (intent instanceof InventoryWorkflowIntent.DismissRecent dismissRecent) {
            boolean changed = coordinator.workflowRuntime().dismissRecent(
                    dismissRecent.identity(),
                    metadata(dismissRecent.origin(), trace)
            );
            return changed
                    ? applied(coordinator.publishCurrent(dismissRecent.origin()), dismissRecent.origin())
                    : noOp(dismissRecent.origin(), null, null);
        }
        if (intent instanceof InventoryMutationIntent.ExecuteRequest executeRequest) {
            return routeConcreteRequest(executeRequest, trace);
        }
        if (intent instanceof InventoryMutationIntent.ProjectedRowTransfer projectedRowTransfer) {
            return routeProjectedTransfer(
                    projectedRowTransfer,
                    trace,
                    precomputedTransferPlan
            );
        }
        if (intent instanceof InventoryMutationIntent.ToolAction toolAction) {
            return routeCraftingMutation(toolAction, trace);
        }
        if (intent instanceof InventoryMutationIntent.ToolToggle toolToggle) {
            return routeCraftingMutation(toolToggle, trace);
        }
        if (intent instanceof InventoryMutationIntent.CraftingPlaceSelected craftingPlaceSelected) {
            return routeCraftingMutation(craftingPlaceSelected, trace);
        }
        if (intent instanceof InventoryMutationIntent.CraftingPlaceCursor craftingPlaceCursor) {
            return routeCraftingMutation(craftingPlaceCursor, trace);
        }
        if (intent instanceof InventoryMutationIntent.CraftingDragCursor craftingDragCursor) {
            return routeCraftingMutation(craftingDragCursor, trace);
        }
        if (intent instanceof InventoryMutationIntent.CraftingExtractResult craftingExtractResult) {
            return routeCraftingMutation(craftingExtractResult, trace);
        }
        if (intent instanceof InventoryMutationIntent.TrashEntry) {
            return InventoryIntentRoutingResult.rejected(session, List.of(InventoryCommandReasonCode.UNSUPPORTED), "trash_not_yet_specified");
        }
        if (intent instanceof InventoryMutationIntent.VoidEntry) {
            return InventoryIntentRoutingResult.rejected(session, List.of(InventoryCommandReasonCode.UNSUPPORTED), "void_not_yet_specified");
        }
        return InventoryIntentRoutingResult.rejected(session, List.of(InventoryCommandReasonCode.UNSUPPORTED), "unsupported_intent");
    }

    private InventoryIntentRoutingResult routeConcreteRequest(
            InventoryMutationIntent.ExecuteRequest executeRequest,
            RouteTrace trace
    ) {
        InventoryActionRequest tracedRequest = traceRequest(executeRequest.request(), trace, executeRequest.origin());
        if (coordinator.hasPendingConflict(tracedRequest.targets())) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    List.of(InventoryCommandReasonCode.INVALID_INTENT),
                    "pending_conflict"
            );
        }
        InventorySessionSnapshot updated = coordinator.dispatch(trace.sequenceId(), InventoryActionDispatchNode.of(tracedRequest));
        return new InventoryIntentRoutingResult(
                InventoryRoutingStatus.DISPATCHED,
                updated,
                List.of(),
                List.of(tracedRequest),
                null,
                null,
                executeRequest.origin()
        );
    }

    private InventoryIntentRoutingResult routeProjectedTransfer(
            InventoryMutationIntent.ProjectedRowTransfer projectedRowTransfer,
            RouteTrace trace,
            ProjectedRowTransferPlan precomputedPlan
    ) {
        ProjectedRowTransferIntent transferIntent = projectedRowTransfer.transferIntent();
        ProjectedRowTransferPlan plan = precomputedPlan == null
                ? ProjectedRowTransferPlanner.plan(transferIntent)
                : precomputedPlan;
        if (plan.requests().isEmpty()) {
            return new InventoryIntentRoutingResult(
                    InventoryRoutingStatus.NO_OP,
                    coordinator.snapshot(),
                    plan.reasonCodes(),
                    List.of(),
                    plan,
                    null,
                    String.join(",", plan.diagnostics())
            );
        }

        List<InventoryActionRequest> tracedRequests = plan.requests().stream()
                .map(request -> traceRequest(request, trace, projectedRowTransfer.origin()))
                .toList();
        if (hasPendingConflict(tracedRequests)) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    List.of(InventoryCommandReasonCode.INVALID_INTENT),
                    "pending_conflict"
            );
        }

        List<InventoryActionDispatchNode> nodes = tracedRequests.stream()
                .map(InventoryActionDispatchNode::of)
                .toList();
        InventorySessionSnapshot updated = coordinator.dispatchAll(trace.sequenceId(), nodes);
        return new InventoryIntentRoutingResult(
                InventoryRoutingStatus.DISPATCHED,
                updated,
                plan.reasonCodes(),
                tracedRequests,
                plan,
                null,
                String.join(",", plan.diagnostics())
        );
    }

    private InventoryIntentRoutingResult routeCraftingMutation(
            InventoryMutationIntent mutationIntent,
            RouteTrace trace
    ) {
        InventoryCraftingPlan plan = InventoryCraftingPreflightService.preflight(coordinator.snapshot(), mutationIntent);
        if (!plan.dispatchable()) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    plan.reasonCodes(),
                    plan.diagnostics().isBlank() ? "missing_crafting_plan" : plan.diagnostics()
            );
        }

        List<InventoryActionRequest> tracedRequests = plan.requests().stream()
                .map(request -> traceRequest(request, trace, mutationOrigin(mutationIntent)))
                .toList();
        if (hasPendingConflict(tracedRequests)) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    List.of(InventoryCommandReasonCode.INVALID_INTENT),
                    "pending_conflict"
            );
        }

        InventorySessionSnapshot updated = tracedRequests.size() == 1
                ? coordinator.dispatch(trace.sequenceId(), InventoryActionDispatchNode.of(tracedRequests.get(0)))
                : coordinator.dispatchAll(
                trace.sequenceId(),
                tracedRequests.stream().map(InventoryActionDispatchNode::of).toList()
        );
        return new InventoryIntentRoutingResult(
                InventoryRoutingStatus.DISPATCHED,
                updated,
                plan.reasonCodes(),
                tracedRequests,
                null,
                null,
                plan.diagnostics()
        );
    }

    private String mutationOrigin(InventoryMutationIntent mutationIntent) {
        if (mutationIntent == null) {
            return "";
        }
        if (mutationIntent instanceof InventoryMutationIntent.ExecuteRequest executeRequest) {
            return executeRequest.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.ProjectedRowTransfer projectedRowTransfer) {
            return projectedRowTransfer.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.TrashEntry trashEntry) {
            return trashEntry.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.VoidEntry voidEntry) {
            return voidEntry.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.ToolAction toolAction) {
            return toolAction.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.ToolToggle toolToggle) {
            return toolToggle.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingPlaceSelected craftingPlaceSelected) {
            return craftingPlaceSelected.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingPlaceCursor craftingPlaceCursor) {
            return craftingPlaceCursor.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingDragCursor craftingDragCursor) {
            return craftingDragCursor.origin();
        }
        if (mutationIntent instanceof InventoryMutationIntent.CraftingExtractResult craftingExtractResult) {
            return craftingExtractResult.origin();
        }
        return "";
    }

    private InventoryIntentRoutingResult routeApplyLoadout(
            InventoryWorkflowIntent.ApplyLoadout applyLoadout,
            RouteTrace trace,
            LoadoutApplyService.LoadoutApplyPlan precomputedPlan
    ) {
        LoadoutApplyService.LoadoutApplyPlan plan = precomputedPlan == null
                ? buildLoadoutPlan(applyLoadout)
                : precomputedPlan;
        if (plan.operations().isEmpty()) {
            return new InventoryIntentRoutingResult(
                    InventoryRoutingStatus.NO_OP,
                    coordinator.snapshot(),
                    List.of(),
                    List.of(),
                    null,
                    plan,
                    String.join(",", plan.diagnostics())
            );
        }

        List<InventoryActionDispatchNode> dispatchNodes = new ArrayList<>();
        List<InventoryActionRequest> dispatchedRequests = new ArrayList<>();
        for (LoadoutApplyService.PlannedTargetOperation operation : plan.operations()) {
            InventoryActionDispatchNode node = dispatchNodeForOperation(operation, trace, applyLoadout.origin());
            if (node != null && node.request() != null) {
                dispatchNodes.add(node);
                collectRequests(node, dispatchedRequests);
            }
        }

        if (dispatchNodes.isEmpty()) {
            return new InventoryIntentRoutingResult(
                    InventoryRoutingStatus.NO_OP,
                    coordinator.snapshot(),
                    List.of(),
                    List.of(),
                    null,
                    plan,
                    String.join(",", plan.diagnostics())
            );
        }
        if (hasPendingConflict(dispatchedRequests)) {
            return InventoryIntentRoutingResult.rejected(
                    coordinator.snapshot(),
                    List.of(InventoryCommandReasonCode.INVALID_INTENT),
                    "pending_conflict"
            );
        }
        InventorySessionSnapshot updated = coordinator.dispatchAll(trace.sequenceId(), dispatchNodes);
        return new InventoryIntentRoutingResult(
                InventoryRoutingStatus.DISPATCHED,
                updated,
                List.of(),
                List.copyOf(dispatchedRequests),
                null,
                plan,
                String.join(",", plan.diagnostics())
        );
    }

    private LoadoutApplyService.LoadoutApplyPlan buildLoadoutPlan(InventoryWorkflowIntent.ApplyLoadout applyLoadout) {
        dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition loadout = coordinator.workflowRuntime()
                .collectionWorkflow()
                .selectedLoadout(applyLoadout.collectionId());
        if (loadout == null || !loadout.id().equals(applyLoadout.loadoutId())) {
            List<dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition> loadouts = coordinator.snapshot().workflow()
                    .collections()
                    .loadoutsByCollection()
                    .getOrDefault(applyLoadout.collectionId(), List.of());
            loadout = loadouts.stream()
                    .filter(candidate -> candidate != null && applyLoadout.loadoutId().equals(candidate.id()))
                    .findFirst()
                    .orElse(null);
        }
        return LoadoutApplyService.plan(
                loadout,
                coordinator.snapshot().authority(),
                applyLoadout.protectionPolicy(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                candidate -> candidate == null || !candidate.present()
                        ? null
                        : dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(candidate.stack())
        );
    }

    private InventoryActionDispatchNode dispatchNodeForOperation(
            LoadoutApplyService.PlannedTargetOperation operation,
            RouteTrace trace,
            String origin
    ) {
        if (operation == null || operation.requests().isEmpty()) {
            return null;
        }
        InventoryActionDispatchNode rollbackNode = operation.rollbackRequest() == null
                ? null
                : InventoryActionDispatchNode.of(traceRequest(operation.rollbackRequest(), trace, origin));

        List<InventoryActionRequest> requests = operation.requests().stream()
                .map(request -> traceRequest(request, trace, origin))
                .toList();
        if (requests.isEmpty()) {
            return null;
        }
        if (requests.size() == 1) {
            return rollbackNode == null
                    ? InventoryActionDispatchNode.of(requests.get(0))
                    : InventoryActionDispatchNode.chain(requests.get(0), null, rollbackNode);
        }
        InventoryActionDispatchNode tail = rollbackNode == null
                ? InventoryActionDispatchNode.of(requests.get(requests.size() - 1))
                : InventoryActionDispatchNode.chain(requests.get(requests.size() - 1), null, rollbackNode);
        for (int index = requests.size() - 2; index >= 0; index--) {
            tail = InventoryActionDispatchNode.chain(requests.get(index), tail, null);
        }
        return tail;
    }

    private void collectRequests(
            InventoryActionDispatchNode node,
            List<InventoryActionRequest> collected
    ) {
        if (node == null || node.request() == null || collected == null) {
            return;
        }
        collected.add(node.request());
        collectRequests(node.onSuccess(), collected);
        collectRequests(node.onFailure(), collected);
    }

    private boolean hasPendingConflict(List<InventoryActionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return false;
        }
        LinkedHashSet<InventoryActionTarget> targets = new LinkedHashSet<>();
        for (InventoryActionRequest request : requests) {
            if (request != null) {
                targets.addAll(request.targets());
            }
        }
        return coordinator.hasPendingConflict(targets);
    }

    private InventoryActionRequest traceRequest(
            InventoryActionRequest request,
            RouteTrace trace,
            String origin
    ) {
        if (request == null) {
            return null;
        }
        HostInstanceKey hostId = request.hostId();
        ServerMenuRef serverMenuRef = request.serverMenuRef();
        if (coordinator.snapshot().host() != null) {
            if (hostId == null || hostId.equals(HostInstanceKey.empty())) {
                hostId = coordinator.snapshot().host().hostId();
            }
            if (serverMenuRef == null || serverMenuRef.containerId() < 0) {
                serverMenuRef = coordinator.snapshot().host().serverMenuRef();
            }
        }
        return new InventoryActionRequest(
                hostId,
                serverMenuRef,
                request.requestId().isBlank() ? UUID.randomUUID().toString() : request.requestId(),
                request.kind(),
                request.mode(),
                request.quantity(),
                request.scope(),
                request.conflictPolicy(),
                request.origin().isBlank() ? origin : request.origin(),
                trace.correlationId(),
                trace.causationId(),
                trace.sessionId(),
                request.primaryTarget(),
                request.secondaryTarget(),
                request.requestedCount(),
                request.identity(),
                request.stack(),
                request.toolActionId() == null ? InventoryToolActionId.PROVIDER_DEFINED : request.toolActionId(),
                request.toolToggleId() == null ? InventoryToolToggleId.PROVIDER_DEFINED : request.toolToggleId(),
                request.desiredToggleState(),
                request.diagnostics()
        );
    }

    private DomainEventMetadata metadata(String origin, RouteTrace trace) {
        return new DomainEventMetadata(origin, trace.correlationId(), trace.causationId(), trace.sessionId());
    }

    private void setSelectedSubject(InventoryBrowseSubjectRef subjectRef) {
        coordinator.workflowRuntime().browseSessionState().update(state -> new InventoryBrowseSessionState(
                state.filter(),
                state.sortMode(),
                state.groupingMode(),
                state.paneMode(),
                state.activePane(),
                state.selectedCollectionId(),
                state.selectedLoadoutId(),
                state.pinnedToolId(),
                state.bulkActionScope(),
                subjectRef,
                state.expandedSectionIds()
        ));
    }

    private InventoryIntentRoutingResult applied(
            InventorySessionSnapshot session,
            String diagnostics
    ) {
        return new InventoryIntentRoutingResult(
                InventoryRoutingStatus.APPLIED,
                session,
                List.of(),
                List.of(),
                null,
                null,
                diagnostics
        );
    }

    private InventoryIntentRoutingResult noOp(
            String diagnostics,
            ProjectedRowTransferPlan transferPlan,
            LoadoutApplyService.LoadoutApplyPlan loadoutPlan
    ) {
        return new InventoryIntentRoutingResult(
                InventoryRoutingStatus.NO_OP,
                coordinator.snapshot(),
                List.of(),
                List.of(),
                transferPlan,
                loadoutPlan,
                diagnostics == null ? "" : diagnostics
        );
    }

    private static boolean sameToken(InventorySessionToken left, InventorySessionToken right) {
        return left != null
                && right != null
                && left.sessionId().equals(right.sessionId())
                && left.revision() == right.revision();
    }

    private record RouteTrace(
            String sequenceId,
            String correlationId,
            String causationId,
            String sessionId
    ) {
    }
}
