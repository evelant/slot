package dev.imagio.slot.inventory.goal;

public record GoalProjectionOptions(int maxDepth) {
    public static final int DEFAULT_MAX_DEPTH = 4;

    public GoalProjectionOptions {
        maxDepth = Math.max(0, maxDepth);
    }

    public static GoalProjectionOptions defaults() {
        return new GoalProjectionOptions(DEFAULT_MAX_DEPTH);
    }
}
