package dev.imagio.slot.workflow.domain;

public interface WorkflowDomainStateRepository {
    WorkflowProjection.Snapshot workflowProjection();

    ActivityProjection.Snapshot activityProjection();

    WorkflowEventStore workflowEvents();

    InventoryActivityStore activityEvents();

    ContextualSuggestionState contextualSuggestionState();

    CraftRunState craftRunState();

    WorkflowEventRecord appendWorkflowEvent(WorkflowEvent event, DomainEventMetadata metadata);

    InventoryActivityRecord appendActivityEvent(InventoryActivityEvent event, DomainEventMetadata metadata);

    ContextualSignalRecord appendContextualSignal(ContextualSignalEvent event, DomainEventMetadata metadata);

    void replaceContextualSuggestionState(ContextualSuggestionState state);

    void replaceCraftRunState(CraftRunState state);

    InventoryBrowsePreferencesStore browsePreferences();

    InventoryBrowseSessionStateStore browseSessionState();

    WorkflowDomainSnapshot snapshot();

    void replaceWith(WorkflowDomainSnapshot snapshot);

    default void compactWorkflowEvents() {
        WorkflowEventStore store = workflowEvents();
        if (store == null) {
            return;
        }
        store.compact();
    }
}
