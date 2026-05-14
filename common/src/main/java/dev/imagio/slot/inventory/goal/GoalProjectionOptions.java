package dev.imagio.slot.inventory.goal;

public record GoalProjectionOptions(int maxDepth, int maxRecipeExpansions, int maxEntries) {
    public static final int DEFAULT_MAX_DEPTH = 4;
    public static final int DEFAULT_MAX_RECIPE_EXPANSIONS = 128;
    public static final int DEFAULT_MAX_ENTRIES = 256;

    public GoalProjectionOptions(int maxDepth) {
        this(maxDepth, DEFAULT_MAX_RECIPE_EXPANSIONS, DEFAULT_MAX_ENTRIES);
    }

    public GoalProjectionOptions {
        maxDepth = Math.max(0, maxDepth);
        maxRecipeExpansions = Math.max(1, maxRecipeExpansions);
        maxEntries = Math.max(1, maxEntries);
    }

    public static GoalProjectionOptions defaults() {
        return new GoalProjectionOptions(DEFAULT_MAX_DEPTH, DEFAULT_MAX_RECIPE_EXPANSIONS, DEFAULT_MAX_ENTRIES);
    }
}
