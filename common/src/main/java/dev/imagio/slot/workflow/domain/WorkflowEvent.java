package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalPlanState;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public sealed interface WorkflowEvent permits
        WorkflowEvent.CollectionCreated,
        WorkflowEvent.CollectionRenamed,
        WorkflowEvent.CollectionDeleted,
        WorkflowEvent.CollectionItemAdded,
        WorkflowEvent.CollectionItemRemoved,
        WorkflowEvent.LoadoutCreated,
        WorkflowEvent.LoadoutRenamed,
        WorkflowEvent.LoadoutUpdated,
        WorkflowEvent.LoadoutDeleted,
        WorkflowEvent.FavoriteMarked,
        WorkflowEvent.FavoriteUnmarked,
        WorkflowEvent.JunkMarked,
        WorkflowEvent.JunkUnmarked,
        WorkflowEvent.ProtectedIdentityMarked,
        WorkflowEvent.ProtectedIdentityUnmarked,
        WorkflowEvent.ProtectedTargetMarked,
        WorkflowEvent.ProtectedTargetUnmarked,
        WorkflowEvent.PortableContainerProtectionSet,
        WorkflowEvent.RecentDismissedUpTo,
        WorkflowEvent.VisualIslandCreated,
        WorkflowEvent.VisualIslandMoved,
        WorkflowEvent.VisualIslandRenamed,
        WorkflowEvent.VisualIslandRecolored,
        WorkflowEvent.VisualIslandIconChanged,
        WorkflowEvent.VisualIslandDeleted,
        WorkflowEvent.VisualIslandReordered,
        WorkflowEvent.VisualHomeAssigned,
        WorkflowEvent.VisualHomeCleared,
        WorkflowEvent.TemplateIslandDismissed,
        WorkflowEvent.ClaimedChestCreated,
        WorkflowEvent.ClaimedChestMoved,
        WorkflowEvent.ClaimedChestAnchorsChanged,
        WorkflowEvent.ClaimedChestRelabeled,
        WorkflowEvent.ClaimedChestDeleted,
        WorkflowEvent.ChestDepositObserved,
        WorkflowEvent.ChestAffinityForgotten,
        WorkflowEvent.ChestAffinityCleared,
        WorkflowEvent.ChestClusterRelabeled,
        WorkflowEvent.KitCreated,
        WorkflowEvent.KitUpdated,
        WorkflowEvent.KitDeleted,
        WorkflowEvent.KitActivated,
        WorkflowEvent.KitDeactivated,
        WorkflowEvent.KitPageSwitched,
        WorkflowEvent.PlayerDesiredCountSet,
        WorkflowEvent.KitDesiredCountSet,
        WorkflowEvent.PlayerWantedCountSet,
        WorkflowEvent.GoalPlanSaved,
        WorkflowEvent.GoalPlanRemoved,
        WorkflowEvent.GoalRecipeDefaultSet {

    record CollectionCreated(
            String collectionId,
            String name
    ) implements WorkflowEvent {
        public CollectionCreated {
            collectionId = collectionId == null ? "" : collectionId;
            name = name == null ? "" : name;
        }
    }

    record CollectionRenamed(
            String collectionId,
            String name
    ) implements WorkflowEvent {
        public CollectionRenamed {
            collectionId = collectionId == null ? "" : collectionId;
            name = name == null ? "" : name;
        }
    }

    record CollectionDeleted(
            String collectionId
    ) implements WorkflowEvent {
        public CollectionDeleted {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record CollectionItemAdded(
            String collectionId,
            ItemIdentity identity
    ) implements WorkflowEvent {
        public CollectionItemAdded {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record CollectionItemRemoved(
            String collectionId,
            ItemIdentity identity
    ) implements WorkflowEvent {
        public CollectionItemRemoved {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record LoadoutCreated(
            String collectionId,
            QuickAccessLoadoutDefinition loadout
    ) implements WorkflowEvent {
        public LoadoutCreated {
            collectionId = collectionId == null ? "" : collectionId;
        }
    }

    record LoadoutRenamed(
            String collectionId,
            String loadoutId,
            String name
    ) implements WorkflowEvent {
        public LoadoutRenamed {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
            name = name == null ? "" : name;
        }
    }

    record LoadoutUpdated(
            String collectionId,
            String loadoutId,
            Set<QuickAccessLoadoutEntry> entries
    ) implements WorkflowEvent {
        public LoadoutUpdated {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
            entries = entries == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(entries));
        }
    }

    record LoadoutDeleted(
            String collectionId,
            String loadoutId
    ) implements WorkflowEvent {
        public LoadoutDeleted {
            collectionId = collectionId == null ? "" : collectionId;
            loadoutId = loadoutId == null ? "" : loadoutId;
        }
    }

    record FavoriteMarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record FavoriteUnmarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record JunkMarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record JunkUnmarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record ProtectedIdentityMarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record ProtectedIdentityUnmarked(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record ProtectedTargetMarked(
            InventoryActionTarget target
    ) implements WorkflowEvent {
    }

    record ProtectedTargetUnmarked(
            InventoryActionTarget target
    ) implements WorkflowEvent {
    }

    record PortableContainerProtectionSet(
            boolean enabled
    ) implements WorkflowEvent {
    }

    record RecentDismissedUpTo(
            ItemIdentity identity,
            long dismissedUpToGlobalSequence
    ) implements WorkflowEvent {
        public RecentDismissedUpTo {
            dismissedUpToGlobalSequence = Math.max(0L, dismissedUpToGlobalSequence);
        }
    }

    record VisualIslandCreated(
            VisualAtlasIsland island
    ) implements WorkflowEvent {
    }

    record VisualIslandMoved(
            String islandId,
            double x,
            double y
    ) implements WorkflowEvent {
        public VisualIslandMoved {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualIslandRenamed(
            String islandId,
            String label
    ) implements WorkflowEvent {
        public VisualIslandRenamed {
            islandId = islandId == null ? "" : islandId;
            label = label == null ? "" : label;
        }
    }

    record VisualIslandRecolored(
            String islandId,
            int color
    ) implements WorkflowEvent {
        public VisualIslandRecolored {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualIslandIconChanged(
            String islandId,
            ItemIdentity iconIdentity
    ) implements WorkflowEvent {
        public VisualIslandIconChanged {
            islandId = islandId == null ? "" : islandId;
        }
    }

    record VisualIslandDeleted(
            String islandId
    ) implements WorkflowEvent {
        public VisualIslandDeleted {
            islandId = islandId == null ? "" : islandId;
        }
    }

    /**
     * Move {@code islandId} to position {@code targetIndex} in the
     * {@link VisualHomeMap#playerIslands()} list. The projection removes
     * the island from its current position and inserts it at the clamped
     * target index, so dropping it onto its own slot is a no-op. Drives
     * the TOC drag-to-reorder gesture (see docs/plans/list-view.md §
     * Phase 2).
     */
    record VisualIslandReordered(
            String islandId,
            int targetIndex
    ) implements WorkflowEvent {
        public VisualIslandReordered {
            islandId = islandId == null ? "" : islandId;
            targetIndex = Math.max(0, targetIndex);
        }
    }

    record VisualHomeAssigned(
            VisualHomeAssignment assignment
    ) implements WorkflowEvent {
    }

    record VisualHomeCleared(
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    record TemplateIslandDismissed(
            String templateId
    ) implements WorkflowEvent {
        public TemplateIslandDismissed {
            templateId = templateId == null ? "" : templateId;
        }
    }

    record ClaimedChestCreated(
            ClaimedChest chest
    ) implements WorkflowEvent {
    }

    record ClaimedChestMoved(
            UUID storageId,
            int atlasX,
            int atlasY
    ) implements WorkflowEvent {
    }

    record ClaimedChestAnchorsChanged(
            UUID storageId,
            Set<ChestAnchor> anchors
    ) implements WorkflowEvent {
        public ClaimedChestAnchorsChanged {
            anchors = anchors == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(anchors));
        }
    }

    record ClaimedChestRelabeled(
            UUID storageId,
            String label
    ) implements WorkflowEvent {
        public ClaimedChestRelabeled {
            label = label == null ? "" : label;
        }
    }

    record ClaimedChestDeleted(
            UUID storageId
    ) implements WorkflowEvent {
    }

    /**
     * The player observably deposited {@code count} of {@code identity}
     * into chest {@code storageId} at server tick {@code tick}. Bumps
     * affinity[storageId, identity] by one increment (count is recorded
     * for future weighting but doesn't currently scale the bump).
     */
    record ChestDepositObserved(
            UUID storageId,
            ItemIdentity identity,
            int count,
            long tick
    ) implements WorkflowEvent {
        public ChestDepositObserved {
            count = Math.max(1, count);
            tick = Math.max(0L, tick);
        }
    }

    /** Forget affinity[storageId, identity]. Targeted "this isn't iron's chest anymore" reset. */
    record ChestAffinityForgotten(
            UUID storageId,
            ItemIdentity identity
    ) implements WorkflowEvent {
    }

    /** Forget all affinity for this chest. Used on chest delete and on player "Forget chest". */
    record ChestAffinityCleared(
            UUID storageId
    ) implements WorkflowEvent {
    }

    /**
     * Player-authored label for a derived chest cluster. {@code clusterId}
     * is the same key {@code ChestClusterMap} hands out (derived from the
     * smallest-uuid chest in the cluster); the projection layers this on
     * top of the default "Storage Area N" labels.
     */
    record ChestClusterRelabeled(
            String clusterId,
            String label
    ) implements WorkflowEvent {
        public ChestClusterRelabeled {
            clusterId = clusterId == null ? "" : clusterId;
            label = label == null ? "" : label;
        }
    }

    record KitCreated(KitDefinition kit) implements WorkflowEvent {
    }

    record KitUpdated(KitDefinition kit) implements WorkflowEvent {
    }

    record KitDeleted(String kitId) implements WorkflowEvent {
        public KitDeleted {
            kitId = kitId == null ? "" : kitId;
        }
    }

    record KitActivated(String kitId, int pageIndex) implements WorkflowEvent {
        public KitActivated {
            kitId = kitId == null ? "" : kitId;
            pageIndex = Math.max(0, pageIndex);
        }
    }

    record KitDeactivated() implements WorkflowEvent {
    }

    record KitPageSwitched(int pageIndex) implements WorkflowEvent {
        public KitPageSwitched {
            pageIndex = Math.max(0, pageIndex);
        }
    }

    /**
     * Player-scoped "I want to keep N of this carried at all times" intent.
     * {@code count == 0} clears the desired count for the identity. Distinct
     * from the legacy collection-scoped {@link DesiredCountSet} because it
     * isn't gated on collection membership and reflects a bare player
     * standing order — the foundation for the desired-counts feature in
     * docs/design/gestures.md. Kit-scoped desired counts will follow a
     * separate event when implemented.
     */
    record PlayerDesiredCountSet(ItemIdentity identity, int count) implements WorkflowEvent {
        public PlayerDesiredCountSet {
            count = Math.max(0, count);
        }
    }

    /**
     * Kit-scoped desired count: an override that takes precedence over the
     * player-global value while the kit is active. {@code count == 0}
     * clears the entry. Resolution rule (applied at view-model build):
     * if a kit is active and has a non-zero entry for the identity, use
     * it; else fall back to {@link PlayerDesiredCountSet}'s value.
     */
    record KitDesiredCountSet(String kitId, ItemIdentity identity, int count) implements WorkflowEvent {
        public KitDesiredCountSet {
            kitId = kitId == null ? "" : kitId;
            count = Math.max(0, count);
        }
    }

    /**
     * Player-scoped wanted count: a persisted fetch target that clears once
     * the player carries at least {@code count}. {@code count == 0} clears
     * the entry. This is intentionally separate from desired counts so a
     * future desired-count rule cannot accidentally consume wanted state.
     */
    record PlayerWantedCountSet(ItemIdentity identity, int count) implements WorkflowEvent {
        public PlayerWantedCountSet {
            count = Math.max(0, count);
        }
    }

    /**
     * Player-scoped remembered producer recipe for a concrete output item id.
     * The client still keeps a session-local copy for immediate feedback, but
     * this event is the durable source used after reconnect. {@code recipeId}
     * blank clears the default for the output.
     */
    record GoalRecipeDefaultSet(String outputItemId, String recipeId) implements WorkflowEvent {
        public GoalRecipeDefaultSet {
            outputItemId = outputItemId == null ? "" : outputItemId.trim();
            recipeId = recipeId == null ? "" : recipeId.trim();
        }
    }

    record GoalPlanSaved(GoalPlanState goal) implements WorkflowEvent {
    }

    record GoalPlanRemoved(String goalId) implements WorkflowEvent {
        public GoalPlanRemoved {
            goalId = goalId == null ? "" : goalId.trim();
        }
    }
}
