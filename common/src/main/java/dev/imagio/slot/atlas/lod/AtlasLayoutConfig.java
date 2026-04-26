package dev.imagio.slot.atlas.lod;

/**
 * Tuning knobs for {@link AtlasLayout}. Per-item world cell size is
 * {@code baseCardSize × (1 + relevanceLift × score)}; the packer uses
 * the resulting heterogeneous cells to position items in canonical
 * order.
 *
 * <p>Phase 2.2 introduced auto-square island sizing. The container
 * width passed to the packer for each island is
 * {@code round(sqrt(totalCellArea) × targetAspectFudge)}, clamped to a
 * floor that keeps empty / single-card islands legible. The result is
 * square-ish without authoring width/height on
 * {@link dev.imagio.slot.workflow.domain.VisualAtlasIsland}.
 */
public record AtlasLayoutConfig(
        int baseCardWidth,
        int baseCardHeight,
        int islandPaddingX,
        int islandContentTop,
        int islandPaddingY,
        int cardGap,
        int atlasIslandGap,
        int atlasMargin,
        int minIslandWidth,
        int minIslandHeight,
        float relevanceLift,
        float targetAspectFudge,
        float ghostShrinkFactor
) {
    /**
     * Defaults match {@code SlotWorkspaceAtlasLayout} constants.
     * {@link #relevanceLift} = {@code 1.5f} so a max-relevance item
     * gets {@code 2.5×} the world cell size of a baseline item.
     * {@link #targetAspectFudge} = {@code 1.2f} biases the auto-square
     * shape slightly wider than tall so labels read naturally and the
     * wrap math reliably picks the next-up column count when the area
     * sqrt sits near a column boundary — playtest-tunable.
     * {@link #ghostShrinkFactor} = {@code 0.65f} pushes non-carried
     * (ghost) cards even smaller than the relevance-zero baseline so
     * the carried items dominate the visual hierarchy at a glance.
     */
    public static final AtlasLayoutConfig DEFAULT = new AtlasLayoutConfig(
            32,
            32,
            8,
            4,
            8,
            4,
            16,
            24,
            96,
            72,
            1.5f,
            1.2f,
            0.65f
    );

    public AtlasLayoutConfig {
        baseCardWidth = Math.max(1, baseCardWidth);
        baseCardHeight = Math.max(1, baseCardHeight);
        islandPaddingX = Math.max(0, islandPaddingX);
        islandContentTop = Math.max(0, islandContentTop);
        islandPaddingY = Math.max(0, islandPaddingY);
        cardGap = Math.max(0, cardGap);
        atlasIslandGap = Math.max(0, atlasIslandGap);
        atlasMargin = Math.max(0, atlasMargin);
        minIslandWidth = Math.max(baseCardWidth + islandPaddingX * 2, minIslandWidth);
        minIslandHeight = Math.max(baseCardHeight + islandContentTop + islandPaddingY, minIslandHeight);
        if (Float.isNaN(relevanceLift) || relevanceLift < 0f) {
            relevanceLift = 0f;
        }
        if (Float.isNaN(targetAspectFudge) || targetAspectFudge <= 0f) {
            targetAspectFudge = 1f;
        }
        if (Float.isNaN(ghostShrinkFactor) || ghostShrinkFactor <= 0f) {
            ghostShrinkFactor = 1f;
        } else if (ghostShrinkFactor > 1f) {
            ghostShrinkFactor = 1f;
        }
    }

    /**
     * Apply the per-item lift: score in {@code [0, 1]} maps to a
     * world-size multiplier in {@code [1, 1 + lift]}.
     */
    public int liftedWidth(float score) {
        return Math.max(1, Math.round(baseCardWidth * (1f + relevanceLift * clamp01(score))));
    }

    public int liftedHeight(float score) {
        return Math.max(1, Math.round(baseCardHeight * (1f + relevanceLift * clamp01(score))));
    }

    /**
     * Auto-square wrap target for an island whose cells have the given
     * total pixel area. Falls back to the empty-island floor when the
     * island has no content. Includes side padding so the packer wraps
     * at the right intra-island column count.
     */
    public int autoSquareWrapWidth(int totalCellArea) {
        if (totalCellArea <= 0) {
            return minIslandWidth;
        }
        int rawTarget = Math.round((float) Math.sqrt(totalCellArea) * targetAspectFudge);
        int contentTarget = Math.max(baseCardWidth, rawTarget);
        return Math.max(minIslandWidth, contentTarget + islandPaddingX * 2);
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v) || v < 0f) {
            return 0f;
        }
        return Math.min(1f, v);
    }
}
