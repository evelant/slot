package dev.imagio.slot.workflow.domain;

public interface WorkflowDomainStateRepository {
    WorkflowProjection.Snapshot workflowProjection();

    ActivityProjection.Snapshot activityProjection();

    WorkflowEventStore workflowEvents();

    InventoryActivityStore activityEvents();

    WorkflowEventRecord appendWorkflowEvent(WorkflowEvent event, DomainEventMetadata metadata);

    InventoryActivityRecord appendActivityEvent(InventoryActivityEvent event, DomainEventMetadata metadata);

    InventoryBrowsePreferencesStore browsePreferences();

    InventoryBrowseSessionStateStore browseSessionState();

    WorkflowDomainSnapshot snapshot();

    void replaceWith(WorkflowDomainSnapshot snapshot);
}
