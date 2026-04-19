package dev.imagio.slot.inventory.workspace;

import java.util.List;

public record DepositPlan(List<Assignment> assignments) {
    public DepositPlan {
        assignments = assignments == null ? List.of() : List.copyOf(assignments);
    }

    public static DepositPlan empty() {
        return new DepositPlan(List.of());
    }

    public boolean isEmpty() {
        return assignments.isEmpty();
    }

    public record Assignment(
            String laneId,
            int slotIndex,
            String itemId,
            List<String> candidateStorageIds
    ) {
        public Assignment {
            laneId = laneId == null ? "" : laneId;
            slotIndex = Math.max(0, slotIndex);
            itemId = itemId == null ? "" : itemId;
            candidateStorageIds = candidateStorageIds == null ? List.of() : List.copyOf(candidateStorageIds);
        }
    }
}
