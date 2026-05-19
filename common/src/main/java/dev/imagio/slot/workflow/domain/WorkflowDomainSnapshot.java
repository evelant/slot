package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.browse.InventoryBrowsePreferences;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;

public record WorkflowDomainSnapshot(
        long nextGlobalSequence,
        WorkflowProjection.Snapshot workflowProjection,
        WorkflowEventStore.Snapshot workflowEvents,
        ActivityProjection.Snapshot activityProjection,
        InventoryActivityStore.Snapshot activityEvents,
        InventoryBrowsePreferences browsePreferences,
        InventoryBrowseSessionState browseSessionState,
        ContextualSuggestionState contextualSuggestions
) {
    public WorkflowDomainSnapshot {
        nextGlobalSequence = Math.max(1L, nextGlobalSequence);
        workflowProjection = workflowProjection == null ? WorkflowProjection.Snapshot.empty() : workflowProjection;
        workflowEvents = workflowEvents == null ? WorkflowEventStore.Snapshot.empty() : workflowEvents;
        activityProjection = activityProjection == null ? ActivityProjection.Snapshot.empty() : activityProjection;
        activityEvents = activityEvents == null ? InventoryActivityStore.Snapshot.empty() : activityEvents;
        browsePreferences = browsePreferences == null ? InventoryBrowsePreferences.defaults() : browsePreferences;
        browseSessionState = browseSessionState == null
                ? InventoryBrowseSessionState.defaults(browsePreferences)
                : browseSessionState;
        contextualSuggestions = contextualSuggestions == null ? ContextualSuggestionState.empty() : contextualSuggestions;
    }

    public static WorkflowDomainSnapshot empty() {
        InventoryBrowsePreferences defaults = InventoryBrowsePreferences.defaults();
        return new WorkflowDomainSnapshot(
                1L,
                WorkflowProjection.Snapshot.empty(),
                WorkflowEventStore.Snapshot.empty(),
                ActivityProjection.Snapshot.empty(),
                InventoryActivityStore.Snapshot.empty(),
                defaults,
                InventoryBrowseSessionState.defaults(defaults),
                ContextualSuggestionState.empty()
        );
    }

    public WorkflowDomainSnapshot(
            long nextGlobalSequence,
            WorkflowProjection.Snapshot workflowProjection,
            WorkflowEventStore.Snapshot workflowEvents,
            ActivityProjection.Snapshot activityProjection,
            InventoryActivityStore.Snapshot activityEvents,
            InventoryBrowsePreferences browsePreferences,
            InventoryBrowseSessionState browseSessionState
    ) {
        this(
                nextGlobalSequence,
                workflowProjection,
                workflowEvents,
                activityProjection,
                activityEvents,
                browsePreferences,
                browseSessionState,
                ContextualSuggestionState.empty()
        );
    }

    public CollectionProjection collections() {
        return workflowProjection.collections();
    }

    public RecentView recents() {
        return activityProjection.recents();
    }

    public ProtectionSnapshotPolicy protection() {
        return workflowProjection.protection();
    }

    public VisualHomeMap visualHomeMap() {
        return workflowProjection.visualHomeMap();
    }

    public ClaimedChestMap claimedChestMap() {
        return workflowProjection.claimedChestMap();
    }

    public ChestAffinityMap chestAffinityMap() {
        return workflowProjection.chestAffinityMap();
    }

    public java.util.Map<String, String> clusterLabels() {
        return workflowProjection.clusterLabels();
    }

    public KitMap kitMap() {
        return workflowProjection.kitMap();
    }

    public java.util.Map<dev.imagio.slot.inventory.core.ItemIdentity, Integer> playerDesiredCounts() {
        return workflowProjection.playerDesiredCounts();
    }

    public java.util.Map<String, java.util.Map<dev.imagio.slot.inventory.core.ItemIdentity, Integer>> kitDesiredCounts() {
        return workflowProjection.kitDesiredCounts();
    }

    public java.util.Map<dev.imagio.slot.inventory.core.ItemIdentity, Integer> playerWantedCounts() {
        return workflowProjection.playerWantedCounts();
    }

    public java.util.Map<String, java.util.Map<dev.imagio.slot.inventory.core.ItemIdentity, Integer>> kitWantedCounts() {
        return workflowProjection.kitWantedCounts();
    }

    public java.util.List<dev.imagio.slot.inventory.goal.GoalPlanState> goalPlans() {
        return workflowProjection.goalPlans();
    }

    public java.util.Map<String, String> goalRecipeDefaults() {
        return workflowProjection.goalRecipeDefaults();
    }
}
