package dev.imagio.slot.atlas;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FitCarriedCameraTest {
    private static final float VIEWPORT_W = 800f;
    private static final float VIEWPORT_H = 600f;
    private static final float MIN_SCALE = 0.30f;
    private static final float MAX_SCALE = 4.50f;
    private static final float READABILITY_MIN = 0.75f;
    private static final float PADDING = 72f;

    @Test
    void fitCentersCameraOnContent() {
        FitCarriedCamera.Rect bbox = new FitCarriedCamera.Rect(100f, 100f, 300f, 300f);

        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                bbox, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING);

        assertNotNull(camera);
        float centerWorldX = camera.offsetX() + VIEWPORT_W / (2f * camera.scale());
        float centerWorldY = camera.offsetY() + VIEWPORT_H / (2f * camera.scale());
        assertEquals(200f, centerWorldX, 0.01f);
        assertEquals(200f, centerWorldY, 0.01f);
    }

    @Test
    void fitClampsScaleToMaxWhenContentIsTiny() {
        FitCarriedCamera.Rect bbox = new FitCarriedCamera.Rect(0f, 0f, 4f, 4f);

        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                bbox, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING);

        assertNotNull(camera);
        assertEquals(MAX_SCALE, camera.scale(), 0.001f);
    }

    @Test
    void fitClampsScaleToMinWhenContentIsHuge() {
        FitCarriedCamera.Rect bbox = new FitCarriedCamera.Rect(0f, 0f, 10000f, 10000f);

        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                bbox, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING);

        assertNotNull(camera);
        assertEquals(MIN_SCALE, camera.scale(), 0.001f);
    }

    @Test
    void fitViewportContainsAllContent() {
        FitCarriedCamera.Rect bbox = new FitCarriedCamera.Rect(100f, 100f, 400f, 350f);

        FitCarriedCamera.Camera camera = FitCarriedCamera.fit(
                bbox, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING);

        assertNotNull(camera);
        float worldViewW = VIEWPORT_W / camera.scale();
        float worldViewH = VIEWPORT_H / camera.scale();
        float viewMinX = camera.offsetX();
        float viewMinY = camera.offsetY();
        float viewMaxX = viewMinX + worldViewW;
        float viewMaxY = viewMinY + worldViewH;
        assertTrue(viewMinX <= bbox.minX(), "view minX " + viewMinX + " > bbox minX " + bbox.minX());
        assertTrue(viewMinY <= bbox.minY(), "view minY " + viewMinY + " > bbox minY " + bbox.minY());
        assertTrue(viewMaxX >= bbox.maxX(), "view maxX " + viewMaxX + " < bbox maxX " + bbox.maxX());
        assertTrue(viewMaxY >= bbox.maxY(), "view maxY " + viewMaxY + " < bbox maxY " + bbox.maxY());
    }

    @Test
    void fitReturnsNullForNullOrInvalidBbox() {
        assertNull(FitCarriedCamera.fit(null, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING));
        FitCarriedCamera.Rect invalid = new FitCarriedCamera.Rect(100f, 100f, 50f, 50f);
        assertNull(FitCarriedCamera.fit(invalid, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING));
    }

    @Test
    void fitReturnsNullForEmptyViewport() {
        FitCarriedCamera.Rect bbox = new FitCarriedCamera.Rect(0f, 0f, 100f, 100f);
        assertNull(FitCarriedCamera.fit(bbox, 0f, VIEWPORT_H, MIN_SCALE, MAX_SCALE, PADDING));
    }

    @Test
    void unionReturnsBoundingBoxOverAllRects() {
        FitCarriedCamera.Rect union = FitCarriedCamera.union(List.of(
                new FitCarriedCamera.Rect(50f, 50f, 70f, 80f),
                new FitCarriedCamera.Rect(200f, 300f, 220f, 330f),
                new FitCarriedCamera.Rect(10f, 500f, 30f, 530f)
        ));

        assertNotNull(union);
        assertEquals(10f, union.minX(), 0.001f);
        assertEquals(50f, union.minY(), 0.001f);
        assertEquals(220f, union.maxX(), 0.001f);
        assertEquals(530f, union.maxY(), 0.001f);
    }

    @Test
    void fitOrFallbackReturnsUnionFitWhenInBand() {
        List<FitCarriedCamera.Rect> items = List.of(
                FitCarriedCamera.Rect.of(100f, 100f, 32f, 32f),
                FitCarriedCamera.Rect.of(200f, 150f, 32f, 32f),
                FitCarriedCamera.Rect.of(180f, 120f, 32f, 32f)
        );

        FitCarriedCamera.Camera camera = FitCarriedCamera.fitOrFallback(
                items, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, READABILITY_MIN, PADDING);

        assertNotNull(camera);
        assertTrue(camera.scale() >= READABILITY_MIN,
                "expected scale >= " + READABILITY_MIN + " got " + camera.scale());
    }

    @Test
    void fitOrFallbackFallsBackToLargestClusterWhenUnionIsTooWide() {
        FitCarriedCamera.Rect[] cluster = new FitCarriedCamera.Rect[]{
                FitCarriedCamera.Rect.of(1000f, 1000f, 32f, 32f),
                FitCarriedCamera.Rect.of(1020f, 1020f, 32f, 32f),
                FitCarriedCamera.Rect.of(1040f, 1010f, 32f, 32f),
                FitCarriedCamera.Rect.of(1030f, 1040f, 32f, 32f)
        };
        FitCarriedCamera.Rect[] outliers = new FitCarriedCamera.Rect[]{
                FitCarriedCamera.Rect.of(50000f, 50000f, 32f, 32f),
                FitCarriedCamera.Rect.of(-50000f, -50000f, 32f, 32f)
        };
        List<FitCarriedCamera.Rect> items = new java.util.ArrayList<>();
        for (FitCarriedCamera.Rect r : cluster) items.add(r);
        for (FitCarriedCamera.Rect r : outliers) items.add(r);

        FitCarriedCamera.Camera camera = FitCarriedCamera.fitOrFallback(
                items, VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, READABILITY_MIN, PADDING);

        assertNotNull(camera);
        assertTrue(camera.scale() >= READABILITY_MIN,
                "fallback scale should be at least readability min");
        float centerWorldX = camera.offsetX() + VIEWPORT_W / (2f * camera.scale());
        float centerWorldY = camera.offsetY() + VIEWPORT_H / (2f * camera.scale());
        assertTrue(Math.abs(centerWorldX - 1020f) < 200f,
                "fallback should center near the dense cluster (got " + centerWorldX + ")");
        assertTrue(Math.abs(centerWorldY - 1020f) < 200f,
                "fallback should center near the dense cluster (got " + centerWorldY + ")");
    }

    @Test
    void fitOrFallbackReturnsNullForEmptyList() {
        assertNull(FitCarriedCamera.fitOrFallback(
                List.of(), VIEWPORT_W, VIEWPORT_H, MIN_SCALE, MAX_SCALE, READABILITY_MIN, PADDING));
    }
}
