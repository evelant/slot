package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.undo.UndoStack;

import java.util.List;
import java.util.Objects;

public final class WorkflowDomainRuntime {
    private final WorkflowDomainStateRepository repository;
    private final WorkflowDomainPersistenceService persistenceService;
    private final CollectionWorkflowDomainService collectionWorkflow;
    private final VisualAtlasWorkflowDomainService visualAtlasWorkflow;
    private final ChestClaimWorkflowDomainService chestClaimWorkflow;
    private final StorageAreaWorkflowDomainService storageAreaWorkflow;
    private final ChestLinkWorkflowDomainService chestLinkWorkflow;
    private final KitWorkflowDomainService kitWorkflow;
    private final InventoryBrowsePreferencesStore browsePreferences;
    private final InventoryBrowseSessionStateStore browseSessionState;
    private final UndoStack undoStack;

    public WorkflowDomainRuntime(
            WorkflowDomainStateRepository repository,
            WorkflowDomainPersistenceService persistenceService
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.persistenceService = persistenceService;
        this.browsePreferences = new ObservedInventoryBrowsePreferencesStore(repository.browsePreferences(), this::saveNow);
        this.browseSessionState = new ObservedInventoryBrowseSessionStateStore(repository.browseSessionState(), this::saveNow);
        this.collectionWorkflow = new CollectionWorkflowDomainService(repository, this::saveNow);
        this.visualAtlasWorkflow = new VisualAtlasWorkflowDomainService(repository, this::saveNow);
        this.storageAreaWorkflow = new StorageAreaWorkflowDomainService(repository, this::saveNow);
        this.chestClaimWorkflow = new ChestClaimWorkflowDomainService(repository, this.storageAreaWorkflow, this::saveNow);
        this.chestLinkWorkflow = new ChestLinkWorkflowDomainService(repository, this::saveNow);
        this.kitWorkflow = new KitWorkflowDomainService(repository, this::saveNow);
        this.undoStack = new UndoStack();
    }

    public UndoStack undoStack() {
        return undoStack;
    }

    public CollectionWorkflowDomainService collectionWorkflow() {
        return collectionWorkflow;
    }

    public VisualAtlasWorkflowDomainService visualAtlasWorkflow() {
        return visualAtlasWorkflow;
    }

    public ChestClaimWorkflowDomainService chestClaimWorkflow() {
        return chestClaimWorkflow;
    }

    public StorageAreaWorkflowDomainService storageAreaWorkflow() {
        return storageAreaWorkflow;
    }

    public ChestLinkWorkflowDomainService chestLinkWorkflow() {
        return chestLinkWorkflow;
    }

    public KitWorkflowDomainService kitWorkflow() {
        return kitWorkflow;
    }

    public WorkflowProjection.Snapshot workflowProjection() {
        return repository.workflowProjection();
    }

    public ActivityProjection.Snapshot activityProjection() {
        return repository.activityProjection();
    }

    public ProtectionPolicy protection() {
        WorkflowProjection.Snapshot projection = repository.workflowProjection();
        return KitActiveProtection.compose(projection.protection(), projection.kitMap());
    }

    public ProtectionSnapshotPolicy baseProtection() {
        return repository.workflowProjection().protection();
    }

    public void setProtectedIdentity(ItemIdentity identity, boolean protectedState) {
        setProtectedIdentity(identity, protectedState, DomainEventMetadata.origin("workflow.protection.identity"));
    }

    public void setProtectedIdentity(
            ItemIdentity identity,
            boolean protectedState,
            DomainEventMetadata metadata
    ) {
        if (identity == null) {
            return;
        }
        repository.appendWorkflowEvent(
                protectedState ? new WorkflowEvent.ProtectedIdentityMarked(identity) : new WorkflowEvent.ProtectedIdentityUnmarked(identity),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.protection.identity")
        );
        saveNow();
    }

    public void setProtectedTarget(InventoryActionTarget target, boolean protectedState) {
        setProtectedTarget(target, protectedState, DomainEventMetadata.origin("workflow.protection.target"));
    }

    public void setProtectedTarget(
            InventoryActionTarget target,
            boolean protectedState,
            DomainEventMetadata metadata
    ) {
        if (target == null) {
            return;
        }
        repository.appendWorkflowEvent(
                protectedState ? new WorkflowEvent.ProtectedTargetMarked(target) : new WorkflowEvent.ProtectedTargetUnmarked(target),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.protection.target")
        );
        saveNow();
    }

    public void setProtectPortableContainers(boolean enabled) {
        setProtectPortableContainers(enabled, DomainEventMetadata.origin("workflow.protection.portable_containers"));
    }

    public void setProtectPortableContainers(boolean enabled, DomainEventMetadata metadata) {
        repository.appendWorkflowEvent(
                new WorkflowEvent.PortableContainerProtectionSet(enabled),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.protection.portable_containers")
        );
        saveNow();
    }

    public InventoryBrowsePreferencesStore browsePreferences() {
        return browsePreferences;
    }

    public InventoryBrowseSessionStateStore browseSessionState() {
        return browseSessionState;
    }

    public WorkflowDomainSnapshot snapshot() {
        return repository.snapshot();
    }

    public boolean recordOutcome(InventoryActionOutcome outcome) {
        return outcome != null && outcome.successful() && recordActivityEvents(
                outcome.activityEvents(),
                new DomainEventMetadata("activity.outcome", outcome.correlationId(), outcome.causationId(), outcome.sessionId())
        );
    }

    public boolean recordActivityEvent(InventoryActivityEvent activityEvent) {
        return recordActivityEvent(activityEvent, DomainEventMetadata.origin("activity.external"));
    }

    public boolean recordActivityEvent(
            InventoryActivityEvent activityEvent,
            DomainEventMetadata metadata
    ) {
        if (activityEvent == null || !activityEvent.present()) {
            return false;
        }
        repository.appendActivityEvent(
                activityEvent,
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("activity.external")
        );
        saveNow();
        return true;
    }

    public boolean dismissRecent(ItemIdentity identity) {
        return dismissRecent(identity, DomainEventMetadata.origin("workflow.recent.dismiss"));
    }

    public boolean dismissRecent(
            ItemIdentity identity,
            DomainEventMetadata metadata
    ) {
        if (identity == null) {
            return false;
        }
        long sequence = repository.activityProjection().recents().latestSequenceByIdentity().getOrDefault(identity, 0L);
        if (sequence <= 0L) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.RecentDismissedUpTo(identity, sequence),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.recent.dismiss")
        );
        saveNow();
        return true;
    }

    public void saveNow() {
        if (persistenceService != null) {
            persistenceService.saveFrom(repository);
        }
    }

    private boolean recordActivityEvents(List<InventoryActivityEvent> activityEvents) {
        return recordActivityEvents(activityEvents, DomainEventMetadata.origin("activity.outcome"));
    }

    private boolean recordActivityEvents(
            List<InventoryActivityEvent> activityEvents,
            DomainEventMetadata metadata
    ) {
        if (activityEvents == null || activityEvents.isEmpty()) {
            return false;
        }
        boolean recorded = false;
        for (InventoryActivityEvent activityEvent : activityEvents) {
            if (activityEvent != null && activityEvent.present()) {
                repository.appendActivityEvent(
                        activityEvent,
                        (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("activity.outcome")
                );
                recorded = true;
            }
        }
        if (recorded) {
            saveNow();
        }
        return recorded;
    }

    private record ObservedInventoryBrowsePreferencesStore(
            InventoryBrowsePreferencesStore delegate,
            Runnable mutationObserver
    ) implements InventoryBrowsePreferencesStore {
        private ObservedInventoryBrowsePreferencesStore {
            Objects.requireNonNull(delegate, "delegate");
            mutationObserver = mutationObserver == null ? () -> {
            } : mutationObserver;
        }

        @Override
        public InventoryBrowsePreferences current() {
            return delegate.current();
        }

        @Override
        public void replaceWith(InventoryBrowsePreferences preferences) {
            delegate.replaceWith(preferences);
            mutationObserver.run();
        }
    }

    private record ObservedInventoryBrowseSessionStateStore(
            InventoryBrowseSessionStateStore delegate,
            Runnable mutationObserver
    ) implements InventoryBrowseSessionStateStore {
        private ObservedInventoryBrowseSessionStateStore {
            Objects.requireNonNull(delegate, "delegate");
            mutationObserver = mutationObserver == null ? () -> {
            } : mutationObserver;
        }

        @Override
        public InventoryBrowseSessionState current() {
            return delegate.current();
        }

        @Override
        public void replaceWith(InventoryBrowseSessionState state) {
            delegate.replaceWith(state);
            mutationObserver.run();
        }
    }
}
