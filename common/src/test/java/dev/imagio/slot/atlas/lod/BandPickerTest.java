package dev.imagio.slot.atlas.lod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BandPickerTest {

    @Test
    void defaultConfigReproducesAtlasRenderBudgetLadder() {
        BandPickerConfig cfg = BandPickerConfig.DEFAULT;
        assertEquals(Band.PIP, BandPicker.pick(0, 0f, cfg));
        assertEquals(Band.PIP, BandPicker.pick(3, 0f, cfg));
        assertEquals(Band.REGION, BandPicker.pick(4, 0f, cfg));
        assertEquals(Band.REGION, BandPicker.pick(15, 0f, cfg));
        assertEquals(Band.BROWSE, BandPicker.pick(16, 0f, cfg));
        assertEquals(Band.BROWSE, BandPicker.pick(21, 0f, cfg));
        assertEquals(Band.READ, BandPicker.pick(22, 0f, cfg));
        assertEquals(Band.READ, BandPicker.pick(43, 0f, cfg));
        assertEquals(Band.INSPECT, BandPicker.pick(44, 0f, cfg));
        assertEquals(Band.INSPECT, BandPicker.pick(95, 0f, cfg));
        assertEquals(Band.DETAIL, BandPicker.pick(96, 0f, cfg));
        assertEquals(Band.DETAIL, BandPicker.pick(256, 0f, cfg));
    }

    @Test
    void relevanceIsIgnoredWhenLiftIsZero() {
        BandPickerConfig cfg = BandPickerConfig.DEFAULT;
        for (float r : new float[] {0f, 0.5f, 0.9f, 1.0f}) {
            assertEquals(Band.READ, BandPicker.pick(32, r, cfg));
        }
    }

    @Test
    void relevanceLiftsBandWhenEnabled() {
        BandPickerConfig phase2 = new BandPickerConfig(
                BandPickerConfig.DEFAULT.pipCellPx(),
                BandPickerConfig.DEFAULT.browseCellPx(),
                BandPickerConfig.DEFAULT.readCellPx(),
                BandPickerConfig.DEFAULT.inspectCellPx(),
                BandPickerConfig.DEFAULT.detailCellPx(),
                1.5f
        );
        assertEquals(Band.READ, BandPicker.pick(32, 0f, phase2));
        assertEquals(Band.INSPECT, BandPicker.pick(32, 0.9f, phase2));
        assertEquals(Band.PIP, BandPicker.pick(2, 0f, phase2));
        assertEquals(Band.REGION, BandPicker.pick(2, 1f, phase2));
    }

    @Test
    void nullConfigUsesDefault() {
        assertEquals(Band.READ, BandPicker.pick(32, 0f, null));
    }

    @Test
    void nanRelevanceTreatedAsZero() {
        BandPickerConfig phase2 = new BandPickerConfig(4, 16, 22, 44, 96, 1.5f);
        assertEquals(Band.READ, BandPicker.pick(32, Float.NaN, phase2));
    }

    @Test
    void thresholdsAreClampedMonotonic() {
        BandPickerConfig cfg = new BandPickerConfig(50, 10, 20, 30, 40, 0f);
        // pip > browse → browse clamped up to pip; read clamped to browse; etc.
        assertEquals(50, cfg.browseCellPx());
        assertEquals(50, cfg.readCellPx());
        assertEquals(50, cfg.inspectCellPx());
        assertEquals(50, cfg.detailCellPx());
    }
}
