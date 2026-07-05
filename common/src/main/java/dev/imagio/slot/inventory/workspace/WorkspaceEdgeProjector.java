package dev.imagio.slot.inventory.workspace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Storage-edge reuse for the existing wayfinding/depositability projection.
 */
final class WorkspaceEdgeProjector {
    private WorkspaceEdgeProjector() {
    }

    static Result project(SlotWorkspaceViewModel viewModel, State previous) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        State prior = previous == null ? State.empty() : previous;
        LinkedHashMap<String, WayfindingEntry> nextTargets = new LinkedHashMap<>();
        StatsCounter stats = new StatsCounter();
        List<WayfindingTarget> targets =
                projectTargets(resolved.wayfindingTargets(), prior, nextTargets, stats);
        int removed = removedTargetCount(prior.targets().keySet(), nextTargets.keySet());
        String depositabilityKey = WorkspaceProjectionFingerprint.depositableIdentitiesKey(
                resolved.depositableIdentities());
        Set<SlotWorkspaceViewModel.IdentityRef> depositable = resolved.depositableIdentities();
        if (depositabilityKey.equals(prior.depositabilityKey())) {
            depositable = prior.depositableIdentities();
            stats.reuseDepositability();
        } else {
            stats.rebuildDepositability();
        }
        return new Result(
                resolved.withStorageEdges(targets, depositable),
                new State(nextTargets, depositabilityKey, depositable),
                new WorkspaceEdgeProjectionStats(
                        stats.reusedTargets(),
                        stats.rebuiltTargets(),
                        removed,
                        stats.reusedDepositability(),
                        stats.rebuiltDepositability()));
    }

    private static List<WayfindingTarget> projectTargets(
            List<WayfindingTarget> targets,
            State previous,
            Map<String, WayfindingEntry> nextTargets,
            StatsCounter stats
    ) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }
        ArrayList<WayfindingTarget> projected = new ArrayList<>(targets.size());
        for (int index = 0; index < targets.size(); index++) {
            WayfindingTarget target = targets.get(index);
            if (target == null || target.storageId().isBlank()) {
                continue;
            }
            String contentKey = WorkspaceProjectionFingerprint.wayfindingTargetKey(target);
            WayfindingEntry previousTarget = previous.targets().get(target.storageId());
            WayfindingTarget nextTarget = target;
            if (previousTarget != null && previousTarget.contentKey().equals(contentKey)) {
                nextTarget = previousTarget.target();
                stats.reuseTarget();
            } else {
                stats.rebuildTarget();
            }
            projected.add(nextTarget);
            nextTargets.put(target.storageId(), new WayfindingEntry(
                    target.storageId(),
                    contentKey,
                    nextTarget,
                    index));
        }
        return List.copyOf(projected);
    }

    private static int removedTargetCount(Set<String> previous, Set<String> next) {
        if (previous == null || previous.isEmpty()) {
            return 0;
        }
        LinkedHashSet<String> removed = new LinkedHashSet<>(previous);
        if (next != null) {
            removed.removeAll(next);
        }
        return removed.size();
    }

    record State(
            Map<String, WayfindingEntry> targets,
            String depositabilityKey,
            Set<SlotWorkspaceViewModel.IdentityRef> depositableIdentities
    ) {
        State {
            targets = targets == null || targets.isEmpty() ? Map.of() : Map.copyOf(targets);
            depositabilityKey = depositabilityKey == null ? "" : depositabilityKey;
            depositableIdentities = depositableIdentities == null ? Set.of() : Set.copyOf(depositableIdentities);
        }

        static State empty() {
            return new State(Map.of(), "", Set.of());
        }
    }

    record Result(
            SlotWorkspaceViewModel viewModel,
            State state,
            WorkspaceEdgeProjectionStats stats
    ) {
        Result {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            state = state == null ? State.empty() : state;
            stats = stats == null ? WorkspaceEdgeProjectionStats.empty() : stats;
        }
    }

    private record WayfindingEntry(
            String storageId,
            String contentKey,
            WayfindingTarget target,
            int ordinal
    ) {
        private WayfindingEntry {
            storageId = storageId == null ? "" : storageId;
            contentKey = contentKey == null ? "" : contentKey;
            target = target == null
                    ? new WayfindingTarget(storageId, "", 0, 0, 0, Set.of(), 0, WayfindingTarget.Scope.PLAYER)
                    : target;
            ordinal = Math.max(0, ordinal);
        }
    }

    private static final class StatsCounter {
        private int reusedTargets;
        private int rebuiltTargets;
        private int reusedDepositability;
        private int rebuiltDepositability;

        private void reuseTarget() {
            reusedTargets++;
        }

        private void rebuildTarget() {
            rebuiltTargets++;
        }

        private void reuseDepositability() {
            reusedDepositability++;
        }

        private void rebuildDepositability() {
            rebuiltDepositability++;
        }

        private int reusedTargets() {
            return reusedTargets;
        }

        private int rebuiltTargets() {
            return rebuiltTargets;
        }

        private int reusedDepositability() {
            return reusedDepositability;
        }

        private int rebuiltDepositability() {
            return rebuiltDepositability;
        }
    }
}
