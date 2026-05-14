package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

/**
 * UI-local cache for goal projection. A projection walks the captured EMI
 * recipe graph and current authority, so render/tooltip paths must not rebuild
 * it repeatedly while the underlying view state is unchanged.
 */
public final class GoalWorkspaceProjectionCache {
    private SlotWorkspaceViewModel sourceRef;
    private long sourceToken = Long.MIN_VALUE;
    private int goalRevision = Integer.MIN_VALUE;
    private String goalId = "";
    private GoalWorkspaceProjection projection;

    public GoalWorkspaceProjection get(SlotWorkspaceViewModel viewModel) {
        GoalWorkspaceClientState.GoalTab active = GoalWorkspaceClientState.activeGoal();
        if (active == null) {
            clear();
            return null;
        }
        SlotWorkspaceViewModel source = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        int nextGoalRevision = GoalWorkspaceClientState.revision();
        String nextGoalId = active.goalId();
        if (projection != null
                && goalRevision == nextGoalRevision
                && goalId.equals(nextGoalId)
                && source == sourceRef) {
            return projection;
        }
        long nextSourceToken = sourceToken(source);
        if (projection != null
                && sourceToken == nextSourceToken
                && goalRevision == nextGoalRevision
                && goalId.equals(nextGoalId)) {
            sourceRef = source;
            return projection;
        }
        GoalWorkspaceProjection next = GoalWorkspaceProjection.fromGoal(source, active);
        sourceRef = source;
        sourceToken = nextSourceToken;
        goalRevision = nextGoalRevision;
        goalId = nextGoalId;
        projection = next;
        return projection;
    }

    public void invalidate() {
        clear();
    }

    private void clear() {
        sourceRef = null;
        sourceToken = Long.MIN_VALUE;
        goalRevision = Integer.MIN_VALUE;
        goalId = "";
        projection = null;
    }

    private static long sourceToken(SlotWorkspaceViewModel source) {
        SlotWorkspaceViewModel resolved = source == null ? SlotWorkspaceViewModel.empty() : source;
        long hash = 0xcbf29ce484222325L;
        hash = mix(hash, resolved.goalRecipeDefaults().recipeChoicesByOutputItemId().hashCode());
        for (SlotWorkspaceViewModel.AtlasIsland island : resolved.islands()) {
            hash = mix(hash, island.islandId());
            hash = mix(hash, island.label());
            hash = mix(hash, island.kind().name());
            hash = mix(hash, Double.doubleToLongBits(island.x()));
            hash = mix(hash, Double.doubleToLongBits(island.y()));
            hash = mix(hash, island.color());
            hash = mix(hash, island.itemCount());
            hash = mix(hash, island.carriedCount());
        }
        hash = mixItems(hash, resolved.atlasItems());
        hash = mixItems(hash, resolved.triageItems());
        for (SlotWorkspaceViewModel.ChestChip chip : resolved.chestChips()) {
            if (chip == null) {
                continue;
            }
            hash = mix(hash, chip.storageId());
            hash = mix(hash, chip.proximate() ? 1 : 0);
            for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
                if (summary == null || summary.count() <= 0) {
                    continue;
                }
                hash = mix(hash, summary.itemId());
                hash = mix(hash, summary.componentFingerprint());
                hash = mix(hash, summary.count());
            }
        }
        return hash;
    }

    private static long mixItems(long hash, Iterable<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null) {
            return hash;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || item.identity() == null) {
                continue;
            }
            hash = mix(hash, item.identity().itemId());
            hash = mix(hash, item.identity().comparisonMode());
            hash = mix(hash, item.identity().componentFingerprint());
            hash = mix(hash, item.islandId());
            hash = mix(hash, item.totalCount());
            hash = mix(hash, item.proximateCount());
            hash = mix(hash, item.carried() ? 1 : 0);
            hash = mix(hash, item.ghost() ? 1 : 0);
            hash = mix(hash, item.playerPlaced() ? 1 : 0);
            hash = mix(hash, item.isCarriedContainer() ? 1 : 0);
            hash = mix(hash, item.containerFreeSlotCount());
            hash = mix(hash, item.containerSlotCapacity());
            hash = mix(hash, item.largestCarriedSourceId());
            hash = mix(hash, item.largestCarriedSlotIndex());
            hash = mix(hash, item.largestCarriedSlotCount());
            hash = mixPresence(hash, item.presence());
            hash = mixPresence(hash, item.elsewhere());
        }
        return hash;
    }

    private static long mixPresence(long hash, Iterable<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        if (entries == null) {
            return hash;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry == null || entry.count() <= 0) {
                continue;
            }
            hash = mix(hash, entry.storageId());
            hash = mix(hash, entry.count());
        }
        return hash;
    }

    private static long mix(long hash, Object value) {
        return mix(hash, value == null ? 0 : value.hashCode());
    }

    private static long mix(long hash, long value) {
        long out = hash;
        out ^= value;
        out *= 0x100000001b3L;
        return out;
    }
}
