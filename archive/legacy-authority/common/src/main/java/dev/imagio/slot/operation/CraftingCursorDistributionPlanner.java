package dev.imagio.slot.operation;

import java.util.ArrayList;
import java.util.List;

public final class CraftingCursorDistributionPlanner {
    private CraftingCursorDistributionPlanner() {
    }

    public static Plan plan(int carriedCount, Mode mode, List<Target> targets) {
        if (carriedCount <= 0 || mode == null || targets == null || targets.isEmpty()) {
            return new Plan(List.of(), Math.max(0, carriedCount));
        }

        int requestedPerTarget = switch (mode) {
            case ONE -> 1;
            case STACK -> carriedCount / targets.size();
        };
        if (requestedPerTarget <= 0) {
            return new Plan(emptyAllocations(targets.size()), carriedCount);
        }

        int remainingCount = carriedCount;
        List<Allocation> allocations = new ArrayList<>(targets.size());
        for (Target target : targets) {
            int existingCount = target == null ? 0 : Math.max(0, target.existingCount());
            int maxCount = target == null ? 0 : Math.max(existingCount, target.maxCount());
            int resultingCount = Math.min(existingCount + requestedPerTarget, maxCount);
            int placedCount = Math.max(0, resultingCount - existingCount);
            remainingCount -= placedCount;
            allocations.add(new Allocation(resultingCount, placedCount));
        }

        return new Plan(List.copyOf(allocations), Math.max(0, remainingCount));
    }

    private static List<Allocation> emptyAllocations(int size) {
        if (size <= 0) {
            return List.of();
        }

        List<Allocation> allocations = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            allocations.add(Allocation.NONE);
        }
        return List.copyOf(allocations);
    }

    public record Target(int existingCount, int maxCount) {
        public Target {
            existingCount = Math.max(0, existingCount);
            maxCount = Math.max(existingCount, maxCount);
        }
    }

    public record Allocation(int resultingCount, int placedCount) {
        private static final Allocation NONE = new Allocation(0, 0);

        public Allocation {
            resultingCount = Math.max(0, resultingCount);
            placedCount = Math.max(0, placedCount);
        }
    }

    public record Plan(List<Allocation> allocations, int remainingCount) {
        public Plan {
            allocations = allocations == null ? List.of() : List.copyOf(allocations);
            remainingCount = Math.max(0, remainingCount);
        }

        public int placedCount() {
            int placedCount = 0;
            for (Allocation allocation : allocations) {
                if (allocation != null) {
                    placedCount += allocation.placedCount();
                }
            }
            return placedCount;
        }
    }

    public enum Mode {
        ONE,
        STACK
    }
}
