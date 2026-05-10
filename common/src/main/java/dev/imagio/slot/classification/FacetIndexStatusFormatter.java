package dev.imagio.slot.classification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class FacetIndexStatusFormatter {

    private FacetIndexStatusFormatter() {
    }

    public static List<String> format(FacetIndexLoadReport report, boolean enabled) {
        FacetIndexLoadReport safeReport = report == null ? FacetIndexLoadReport.unknown(FacetIndex.empty()) : report;
        List<String> lines = new ArrayList<>();
        lines.add(String.format(
                "[SLOT] classification: enabled=%s entries=%d bundled=%d/%d datapack=%d/%d failed=%d loaded=%s",
                enabled,
                safeReport.totalEntries(),
                safeReport.loadedBundledLayerCount(),
                safeReport.bundledLayers().size(),
                safeReport.loadedDatapackLayerCount(),
                safeReport.datapackLayers().size(),
                safeReport.failedDatapackLayerCount(),
                Instant.ofEpochMilli(safeReport.loadedAtEpochMillis())
        ));
        if (safeReport.datapackLayers().isEmpty()) {
            lines.add("[SLOT] classification datapack layers: none discovered under data/slot/classification/layers/*.json");
        } else {
            lines.add("[SLOT] classification datapack layers:");
            for (FacetIndexLoadReport.Layer layer : safeReport.datapackLayers()) {
                if (layer.loaded()) {
                    lines.add(String.format("[SLOT]   loaded %s entries=%d", layer.description(), layer.entries()));
                } else {
                    lines.add(String.format("[SLOT]   failed %s error=%s", layer.description(), layer.error()));
                }
            }
        }
        return List.copyOf(lines);
    }
}
