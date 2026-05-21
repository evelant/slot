package dev.imagio.slot.workflow.domain;

import java.time.Instant;
import java.util.Objects;

public final class InMemoryWorkflowDomainStateRepository implements WorkflowDomainStateRepository {
    private final WorkflowEventStore workflowEvents;
    private final InventoryActivityStore activityEvents;
    private final InventoryBrowsePreferencesStore browsePreferences;
    private final InventoryBrowseSessionStateStore browseSessionState;

    private WorkflowProjection.Snapshot workflowProjection;
    private ActivityProjection.Snapshot activityProjection;
    private ContextualSuggestionState contextualSuggestionState;
    private CraftRunState craftRunState;
    private long nextGlobalSequence;

    public InMemoryWorkflowDomainStateRepository() {
        this(
                new InMemoryWorkflowEventStore(),
                new InMemoryInventoryActivityStore(),
                new InMemoryInventoryBrowsePreferencesStore(),
                new InMemoryInventoryBrowseSessionStateStore()
        );
    }

    public InMemoryWorkflowDomainStateRepository(
            WorkflowEventStore workflowEvents,
            InventoryActivityStore activityEvents,
            InventoryBrowsePreferencesStore browsePreferences,
            InventoryBrowseSessionStateStore browseSessionState
    ) {
        this.workflowEvents = Objects.requireNonNull(workflowEvents, "workflowEvents");
        this.activityEvents = Objects.requireNonNull(activityEvents, "activityEvents");
        this.browsePreferences = Objects.requireNonNull(browsePreferences, "browsePreferences");
        this.browseSessionState = Objects.requireNonNull(browseSessionState, "browseSessionState");
        this.workflowProjection = WorkflowProjection.Snapshot.empty();
        this.activityProjection = ActivityProjection.Snapshot.empty();
        this.contextualSuggestionState = ContextualSuggestionState.empty();
        this.craftRunState = CraftRunState.empty();
        this.nextGlobalSequence = 1L;
    }

    @Override
    public WorkflowProjection.Snapshot workflowProjection() {
        return workflowProjection;
    }

    @Override
    public ActivityProjection.Snapshot activityProjection() {
        return activityProjection;
    }

    @Override
    public WorkflowEventStore workflowEvents() {
        return workflowEvents;
    }

    @Override
    public InventoryActivityStore activityEvents() {
        return activityEvents;
    }

    @Override
    public ContextualSuggestionState contextualSuggestionState() {
        return contextualSuggestionState;
    }

    @Override
    public CraftRunState craftRunState() {
        return craftRunState;
    }

    @Override
    public WorkflowEventRecord appendWorkflowEvent(WorkflowEvent event, DomainEventMetadata metadata) {
        WorkflowEventRecord record = workflowEvents.append(nextEnvelope(DomainEventStreamKind.WORKFLOW, metadata), event);
        nextGlobalSequence = Math.max(nextGlobalSequence, record.envelope().globalSequence() + 1L);
        workflowProjection = WorkflowProjection.apply(workflowProjection, record);
        activityProjection = ActivityProjection.applyDismissals(
                activityProjection,
                workflowProjection.recentDismissedUpToByIdentity()
        );
        return record;
    }

    @Override
    public InventoryActivityRecord appendActivityEvent(InventoryActivityEvent event, DomainEventMetadata metadata) {
        InventoryActivityRecord record = activityEvents.append(nextEnvelope(DomainEventStreamKind.ACTIVITY, metadata), event);
        nextGlobalSequence = Math.max(nextGlobalSequence, record.envelope().globalSequence() + 1L);
        activityProjection = ActivityProjection.apply(
                activityProjection,
                record,
                workflowProjection.recentDismissedUpToByIdentity()
        );
        return record;
    }

    @Override
    public ContextualSignalRecord appendContextualSignal(ContextualSignalEvent event, DomainEventMetadata metadata) {
        ContextualSignalRecord record = new ContextualSignalRecord(
                nextEnvelope(DomainEventStreamKind.CONTEXTUAL, metadata),
                event);
        nextGlobalSequence = Math.max(nextGlobalSequence, record.envelope().globalSequence() + 1L);
        contextualSuggestionState = contextualSuggestionState.record(record);
        return record;
    }

    @Override
    public void replaceContextualSuggestionState(ContextualSuggestionState state) {
        contextualSuggestionState = state == null ? ContextualSuggestionState.empty() : state;
    }

    @Override
    public void replaceCraftRunState(CraftRunState state) {
        craftRunState = state == null ? CraftRunState.empty() : state;
    }

    @Override
    public InventoryBrowsePreferencesStore browsePreferences() {
        return browsePreferences;
    }

    @Override
    public InventoryBrowseSessionStateStore browseSessionState() {
        return browseSessionState;
    }

    @Override
    public WorkflowDomainSnapshot snapshot() {
        return new WorkflowDomainSnapshot(
                nextGlobalSequence,
                workflowProjection,
                workflowEvents.snapshot(),
                activityProjection,
                activityEvents.snapshot(),
                browsePreferences.current(),
                browseSessionState.current(),
                craftRunState,
                contextualSuggestionState
        );
    }

    @Override
    public void replaceWith(WorkflowDomainSnapshot snapshot) {
        WorkflowDomainSnapshot resolved = snapshot == null ? WorkflowDomainSnapshot.empty() : snapshot;
        nextGlobalSequence = resolved.nextGlobalSequence();
        workflowEvents.replaceWith(resolved.workflowEvents());
        activityEvents.replaceWith(resolved.activityEvents());
        workflowProjection = resolved.workflowProjection();
        activityProjection = ActivityProjection.applyDismissals(
                resolved.activityProjection(),
                workflowProjection.recentDismissedUpToByIdentity()
        );
        contextualSuggestionState = resolved.contextualSuggestions();
        craftRunState = resolved.craftRun();
        browsePreferences.replaceWith(resolved.browsePreferences());
        browseSessionState.replaceWith(resolved.browseSessionState());
    }

    private DomainEventEnvelope nextEnvelope(DomainEventStreamKind streamKind, DomainEventMetadata metadata) {
        DomainEventMetadata resolved = metadata == null ? DomainEventMetadata.origin("") : metadata;
        long streamSequence = switch (streamKind) {
            case WORKFLOW -> workflowEvents.snapshot().nextStreamSequence();
            case ACTIVITY -> activityEvents.snapshot().nextStreamSequence();
            case CONTEXTUAL -> contextualSuggestionState.nextStreamSequence();
        };
        return new DomainEventEnvelope(
                nextGlobalSequence,
                streamSequence,
                streamKind,
                Instant.now().toEpochMilli(),
                resolved.origin(),
                resolved.correlationId(),
                resolved.causationId(),
                resolved.sessionId()
        );
    }
}
