package dev.imagio.slot.inventory.workspace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Storage-keyed chip reuse for the existing full projection output.
 */
final class WorkspaceStorageProjector {
    private WorkspaceStorageProjector() {
    }

    static Result project(SlotWorkspaceViewModel viewModel, State previous) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        State prior = previous == null ? State.empty() : previous;
        LinkedHashMap<String, ChipEntry> nextChips = new LinkedHashMap<>();
        StatsCounter stats = new StatsCounter();
        List<SlotWorkspaceViewModel.ChestChip> chips =
                projectChips(resolved.chestChips(), prior, nextChips, stats);
        int removed = removedChipCount(prior.chips().keySet(), nextChips.keySet());
        return new Result(
                resolved.withChestChips(chips),
                new State(nextChips),
                new WorkspaceStorageProjectionStats(stats.reused(), stats.rebuilt(), removed));
    }

    private static List<SlotWorkspaceViewModel.ChestChip> projectChips(
            List<SlotWorkspaceViewModel.ChestChip> chips,
            State previous,
            Map<String, ChipEntry> nextChips,
            StatsCounter stats
    ) {
        if (chips == null || chips.isEmpty()) {
            return List.of();
        }
        ArrayList<SlotWorkspaceViewModel.ChestChip> projected = new ArrayList<>(chips.size());
        for (int index = 0; index < chips.size(); index++) {
            SlotWorkspaceViewModel.ChestChip chip = chips.get(index);
            if (chip == null || chip.storageId().isBlank()) {
                continue;
            }
            String contentKey = WorkspaceProjectionFingerprint.storageChipKey(chip);
            ChipEntry previousChip = previous.chips().get(chip.storageId());
            SlotWorkspaceViewModel.ChestChip nextChip = chip;
            if (previousChip != null && previousChip.contentKey().equals(contentKey)) {
                nextChip = previousChip.chip();
                stats.reuse();
            } else {
                stats.rebuild();
            }
            projected.add(nextChip);
            nextChips.put(chip.storageId(), new ChipEntry(chip.storageId(), contentKey, nextChip, index));
        }
        return List.copyOf(projected);
    }

    private static int removedChipCount(Set<String> previous, Set<String> next) {
        if (previous == null || previous.isEmpty()) {
            return 0;
        }
        LinkedHashSet<String> removed = new LinkedHashSet<>(previous);
        if (next != null) {
            removed.removeAll(next);
        }
        return removed.size();
    }

    record State(Map<String, ChipEntry> chips) {
        State {
            chips = chips == null || chips.isEmpty() ? Map.of() : Map.copyOf(chips);
        }

        static State empty() {
            return new State(Map.of());
        }
    }

    record Result(
            SlotWorkspaceViewModel viewModel,
            State state,
            WorkspaceStorageProjectionStats stats
    ) {
        Result {
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            state = state == null ? State.empty() : state;
            stats = stats == null ? WorkspaceStorageProjectionStats.empty() : stats;
        }
    }

    private record ChipEntry(
            String storageId,
            String contentKey,
            SlotWorkspaceViewModel.ChestChip chip,
            int ordinal
    ) {
        private ChipEntry {
            storageId = storageId == null ? "" : storageId;
            contentKey = contentKey == null ? "" : contentKey;
            chip = chip == null
                    ? new SlotWorkspaceViewModel.ChestChip(storageId, "", "", 1, 0, 0, false, 0, 0, 0, 0)
                    : chip;
            ordinal = Math.max(0, ordinal);
        }
    }

    private static final class StatsCounter {
        private int reused;
        private int rebuilt;

        private void reuse() {
            reused++;
        }

        private void rebuild() {
            rebuilt++;
        }

        private int reused() {
            return reused;
        }

        private int rebuilt() {
            return rebuilt;
        }
    }
}
