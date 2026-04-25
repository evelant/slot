package dev.imagio.slot.atlas.lod;

/**
 * Tuning knobs for {@link AtlasLayout}. Per-item world cell size is
 * {@code baseCardSize × (1 + relevanceLift × score)}; the packer uses
 * the resulting heterogeneous cells to position items in canonical
 * order. Atlas-level packing reuses the same gap/padding for the
 * island layer.
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
        float relevanceLift
) {
    /**
     * Defaults match {@code SlotWorkspaceAtlasLayout} constants.
     * {@link #relevanceLift} = {@code 1.5f} so a max-relevance item
     * gets {@code 2.5×} the world cell size of a baseline item; tune
     * in playtest.
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
            1.5f
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
        if (Float.isNaN(relevanceLift) || relevanceLift < 0f) {
            relevanceLift = 0f;
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

    private static float clamp01(float v) {
        if (Float.isNaN(v) || v < 0f) {
            return 0f;
        }
        return Math.min(1f, v);
    }
}
