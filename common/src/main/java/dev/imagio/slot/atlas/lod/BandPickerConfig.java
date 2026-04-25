package dev.imagio.slot.atlas.lod;

/**
 * Tuning knobs for {@link BandPicker}. Px thresholds match the
 * neoforge {@code AtlasRenderBudget} ladder so layout-side band picks
 * stay consistent with render-side budget tiers.
 *
 * <p>{@link #relevanceLift} controls how much per-item relevance
 * multiplies the effective cell px before the threshold ladder is
 * applied. Phase 1 ships with {@code 0f} (relevance ignored — atlas
 * looks like today). Phase 2 raises it (design-doc starting point:
 * {@code ~1.5f}, so a max-relevance item gets {@code 2.5×} the cell
 * budget of a baseline item at the same camera scale).
 */
public record BandPickerConfig(
        int pipCellPx,
        int browseCellPx,
        int readCellPx,
        int inspectCellPx,
        int detailCellPx,
        float relevanceLift
) {
    /**
     * Defaults match {@code WorkspaceTheme.{BROWSE,READ,INSPECT,DETAIL}_CELL_PX}.
     * {@link #pipCellPx} sits below them so PIP is reachable only when
     * relevance is genuinely low (or camera is pulled out further than
     * the carried-fit camera ever drops to). Relevance is a no-op
     * ({@link #relevanceLift} = {@code 0f}) until Phase 2 tunes it.
     */
    public static final BandPickerConfig DEFAULT = new BandPickerConfig(
            4,
            16,
            22,
            44,
            96,
            0f
    );

    public BandPickerConfig {
        pipCellPx = Math.max(0, pipCellPx);
        browseCellPx = Math.max(pipCellPx, browseCellPx);
        readCellPx = Math.max(browseCellPx, readCellPx);
        inspectCellPx = Math.max(readCellPx, inspectCellPx);
        detailCellPx = Math.max(inspectCellPx, detailCellPx);
        if (Float.isNaN(relevanceLift) || relevanceLift < 0f) {
            relevanceLift = 0f;
        }
    }

    /**
     * Effective cellBudgetPx after the relevance multiplier is
     * applied. Used by {@link BandPicker} and exposed for callers
     * (debug overlays, the layout packer) that need the same lifted
     * size for downstream sizing.
     */
    public int liftedCellPx(int cellBudgetPx, float relevance) {
        if (cellBudgetPx <= 0) {
            return 0;
        }
        float clampedRelevance = Math.max(0f, Math.min(1f, Float.isNaN(relevance) ? 0f : relevance));
        float lifted = cellBudgetPx * (1f + relevanceLift * clampedRelevance);
        return Math.max(1, Math.round(lifted));
    }
}
