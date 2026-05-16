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
    private final KitWorkflowDomainService kitWorkflow;
    private final DesiredCountWorkflowDomainService desiredCountWorkflow;
    private final WantedCountWorkflowDomainService wantedCountWorkflow;
    private final GoalPlanWorkflowDomainService goalPlanWorkflow;
    private final GoalRecipeDefaultWorkflowDomainService goalRecipeDefaultWorkflow;
    private final ContextualSuggestionDomainService contextualSuggestions;
    private final InventoryBrowsePreferencesStore browsePreferences;
    private final InventoryBrowseSessionStateStore browseSessionState;
    private final UndoStack undoStack;
    private boolean savePending;

    public WorkflowDomainRuntime(
            WorkflowDomainStateRepository repository,
            WorkflowDomainPersistenceService persistenceService
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.persistenceService = persistenceService;
        this.browsePreferences = new ObservedInventoryBrowsePreferencesStore(repository.browsePreferences(), this::requestSave);
        this.browseSessionState = new ObservedInventoryBrowseSessionStateStore(repository.browseSessionState(), this::requestSave);
        this.collectionWorkflow = new CollectionWorkflowDomainService(repository, this::requestSave);
        this.visualAtlasWorkflow = new VisualAtlasWorkflowDomainService(repository, this::requestSave);
        this.chestClaimWorkflow = new ChestClaimWorkflowDomainService(repository, this::requestSave);
        this.kitWorkflow = new KitWorkflowDomainService(repository, this::requestSave);
        this.desiredCountWorkflow = new DesiredCountWorkflowDomainService(repository, this::requestSave);
        this.wantedCountWorkflow = new WantedCountWorkflowDomainService(repository, this::requestSave);
        this.goalPlanWorkflow = new GoalPlanWorkflowDomainService(repository, this::requestSave);
        this.goalRecipeDefaultWorkflow = new GoalRecipeDefaultWorkflowDomainService(repository, this::requestSave);
        this.contextualSuggestions = new ContextualSuggestionDomainService(repository, this::requestSave);
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

    public KitWorkflowDomainService kitWorkflow() {
        return kitWorkflow;
    }

    public DesiredCountWorkflowDomainService desiredCountWorkflow() {
        return desiredCountWorkflow;
    }

    public WantedCountWorkflowDomainService wantedCountWorkflow() {
        return wantedCountWorkflow;
    }

    public GoalRecipeDefaultWorkflowDomainService goalRecipeDefaultWorkflow() {
        return goalRecipeDefaultWorkflow;
    }

    public GoalPlanWorkflowDomainService goalPlanWorkflow() {
        return goalPlanWorkflow;
    }

    public ContextualSuggestionDomainService contextualSuggestions() {
        return contextualSuggestions;
    }

    public WorkflowProjection.Snapshot workflowProjection() {
        return repository.workflowProjection();
    }

    public ActivityProjection.Snapshot activityProjection() {
        return repository.activityProjection();
    }

    public ProtectionPolicy protection() {
        WorkflowProjection.Snapshot projection = repository.workflowProjection();
        // Stack: base → player-global desired counts → player-global wanted counts → kit-active.
        // Each layer adds cleanup protection; lookups OR through the
        // chain so any layer that protects an identity wins.
        ProtectionPolicy withGlobalDesired = CarryTargetProtection.compose(
                projection.protection(),
                projection.playerDesiredCounts()
        );
        ProtectionPolicy withWanted = CarryTargetProtection.compose(
                withGlobalDesired,
                projection.playerWantedCounts()
        );
        return KitActiveProtection.compose(
                withWanted,
                projection.kitMap(),
                projection.kitDesiredCounts()
        );
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
        requestSave();
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
        requestSave();
    }

    public void setProtectPortableContainers(boolean enabled) {
        setProtectPortableContainers(enabled, DomainEventMetadata.origin("workflow.protection.portable_containers"));
    }

    public void setProtectPortableContainers(boolean enabled, DomainEventMetadata metadata) {
        repository.appendWorkflowEvent(
                new WorkflowEvent.PortableContainerProtectionSet(enabled),
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("workflow.protection.portable_containers")
        );
        requestSave();
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
        InventoryActivityRecord record = repository.appendActivityEvent(
                activityEvent,
                (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("activity.external")
        );
        contextualSuggestions.observeActivityRecord(record);
        requestSave();
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
        requestSave();
        return true;
    }

    public void saveNow() {
        if (persistenceService != null) {
            savePending = false;
            persistenceService.saveFrom(repository);
        }
    }

    public void requestSave() {
        if (persistenceService != null) {
            savePending = true;
        }
    }

    public boolean savePending() {
        return savePending;
    }

    public boolean flushPendingSave() {
        if (!savePending) {
            return false;
        }
        saveNow();
        return true;
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
                InventoryActivityRecord record = repository.appendActivityEvent(
                        activityEvent,
                        (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin("activity.outcome")
                );
                contextualSuggestions.observeActivityRecord(record);
                recorded = true;
            }
        }
        if (recorded) {
            requestSave();
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
