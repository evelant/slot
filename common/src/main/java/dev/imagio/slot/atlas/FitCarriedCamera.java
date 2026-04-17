package dev.imagio.slot.atlas;

import java.util.ArrayList;
import java.util.List;

public final class FitCarriedCamera {
    private FitCarriedCamera() {
    }

    public static Camera fit(
            Rect bbox,
            float viewportWidth,
            float viewportHeight,
            float minScale,
            float maxScale,
            float paddingPx
    ) {
        if (bbox == null || !bbox.valid()) {
            return null;
        }
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            return null;
        }
        float contentWidth = Math.max(1f, bbox.width());
        float contentHeight = Math.max(1f, bbox.height());
        float effectiveWidth = Math.max(1f, viewportWidth - 2f * paddingPx);
        float effectiveHeight = Math.max(1f, viewportHeight - 2f * paddingPx);
        float scale = Math.min(effectiveWidth / contentWidth, effectiveHeight / contentHeight);
        scale = clamp(scale, minScale, maxScale);
        float offsetX = bbox.centerX() - viewportWidth / (2f * scale);
        float offsetY = bbox.centerY() - viewportHeight / (2f * scale);
        return new Camera(offsetX, offsetY, scale);
    }

    public static Camera fitOrFallback(
            List<Rect> items,
            float viewportWidth,
            float viewportHeight,
            float minScale,
            float maxScale,
            float readabilityMinScale,
            float paddingPx
    ) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        Rect union = union(items);
        Camera fit = fit(union, viewportWidth, viewportHeight, minScale, maxScale, paddingPx);
        if (fit != null && fit.scale() >= readabilityMinScale) {
            return fit;
        }
        Rect cluster = largestClusterBounds(items, viewportWidth, viewportHeight, readabilityMinScale);
        Camera fallback = fit(cluster, viewportWidth, viewportHeight, readabilityMinScale, maxScale, paddingPx);
        if (fallback != null) {
            return fallback;
        }
        return fit;
    }

    private static final float CLUSTER_WINDOW_SHRINK = 0.65f;

    static Rect largestClusterBounds(
            List<Rect> items,
            float viewportWidth,
            float viewportHeight,
            float readabilityMinScale
    ) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        float windowWidth = viewportWidth / Math.max(0.0001f, readabilityMinScale) * CLUSTER_WINDOW_SHRINK;
        float windowHeight = viewportHeight / Math.max(0.0001f, readabilityMinScale) * CLUSTER_WINDOW_SHRINK;
        float halfW = windowWidth / 2f;
        float halfH = windowHeight / 2f;

        int bestIndex = 0;
        int bestCount = -1;
        for (int index = 0; index < items.size(); index++) {
            Rect center = items.get(index);
            if (center == null || !center.valid()) {
                continue;
            }
            float cx = center.centerX();
            float cy = center.centerY();
            int count = 0;
            for (Rect candidate : items) {
                if (candidate == null || !candidate.valid()) {
                    continue;
                }
                float dx = Math.abs(candidate.centerX() - cx);
                float dy = Math.abs(candidate.centerY() - cy);
                if (dx <= halfW && dy <= halfH) {
                    count++;
                }
            }
            if (count > bestCount) {
                bestCount = count;
                bestIndex = index;
            }
        }

        Rect pivot = items.get(bestIndex);
        if (pivot == null || !pivot.valid()) {
            return union(items);
        }
        float pivotCx = pivot.centerX();
        float pivotCy = pivot.centerY();
        ArrayList<Rect> cluster = new ArrayList<>();
        for (Rect candidate : items) {
            if (candidate == null || !candidate.valid()) {
                continue;
            }
            float dx = Math.abs(candidate.centerX() - pivotCx);
            float dy = Math.abs(candidate.centerY() - pivotCy);
            if (dx <= halfW && dy <= halfH) {
                cluster.add(candidate);
            }
        }
        if (cluster.isEmpty()) {
            return pivot;
        }
        return union(cluster);
    }

    public static Rect union(List<Rect> items) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean any = false;
        for (Rect rect : items) {
            if (rect == null || !rect.valid()) {
                continue;
            }
            minX = Math.min(minX, rect.minX());
            minY = Math.min(minY, rect.minY());
            maxX = Math.max(maxX, rect.maxX());
            maxY = Math.max(maxY, rect.maxY());
            any = true;
        }
        if (!any) {
            return null;
        }
        return new Rect(minX, minY, maxX, maxY);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Rect(float minX, float minY, float maxX, float maxY) {
        public float width() {
            return maxX - minX;
        }

        public float height() {
            return maxY - minY;
        }

        public float centerX() {
            return (minX + maxX) * 0.5f;
        }

        public float centerY() {
            return (minY + maxY) * 0.5f;
        }

        public boolean valid() {
            return maxX >= minX && maxY >= minY;
        }

        public static Rect of(float x, float y, float width, float height) {
            return new Rect(x, y, x + width, y + height);
        }
    }

    public record Camera(float offsetX, float offsetY, float scale) {
    }
}
