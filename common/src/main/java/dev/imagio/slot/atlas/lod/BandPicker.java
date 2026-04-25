package dev.imagio.slot.atlas.lod;

/**
 * Picks the LOD band an item should render at, given the cell's
 * camera-derived px budget and the item's relevance score.
 *
 * <p>Replaces the camera-only ladder that lived inside
 * {@code AtlasRenderBudget.forScreenBudget}. The render-side budget
 * still derives font/icon px from the cell budget, but the band
 * decision is now in {@code common/} where the layout packer can
 * consult it too.
 */
public final class BandPicker {
    private BandPicker() {
    }

    public static Band pick(int cellBudgetPx, float relevance, BandPickerConfig config) {
        BandPickerConfig cfg = config == null ? BandPickerConfig.DEFAULT : config;
        int effectivePx = cfg.liftedCellPx(cellBudgetPx, relevance);
        if (effectivePx >= cfg.detailCellPx()) {
            return Band.DETAIL;
        }
        if (effectivePx >= cfg.inspectCellPx()) {
            return Band.INSPECT;
        }
        if (effectivePx >= cfg.readCellPx()) {
            return Band.READ;
        }
        if (effectivePx >= cfg.browseCellPx()) {
            return Band.BROWSE;
        }
        if (effectivePx >= cfg.pipCellPx()) {
            return Band.REGION;
        }
        return Band.PIP;
    }
}
