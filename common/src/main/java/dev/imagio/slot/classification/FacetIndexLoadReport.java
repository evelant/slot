package dev.imagio.slot.classification;

import java.util.List;

public record FacetIndexLoadReport(
        long loadedAtEpochMillis,
        int totalEntries,
        List<Layer> bundledLayers,
        List<Layer> datapackLayers
) {

    public FacetIndexLoadReport {
        bundledLayers = List.copyOf(bundledLayers == null ? List.of() : bundledLayers);
        datapackLayers = List.copyOf(datapackLayers == null ? List.of() : datapackLayers);
    }

    public static FacetIndexLoadReport unknown(FacetIndex index) {
        int entries = index == null ? 0 : index.size();
        return new FacetIndexLoadReport(
                System.currentTimeMillis(),
                entries,
                List.of(new Layer("unknown", entries, true, null)),
                List.of()
        );
    }

    public int loadedBundledLayerCount() {
        return countLoaded(bundledLayers);
    }

    public int loadedDatapackLayerCount() {
        return countLoaded(datapackLayers);
    }

    public int failedDatapackLayerCount() {
        return countFailed(datapackLayers);
    }

    private static int countLoaded(List<Layer> layers) {
        int count = 0;
        for (Layer layer : layers) {
            if (layer.loaded()) {
                count++;
            }
        }
        return count;
    }

    private static int countFailed(List<Layer> layers) {
        int count = 0;
        for (Layer layer : layers) {
            if (!layer.loaded()) {
                count++;
            }
        }
        return count;
    }

    public record Layer(String description, int entries, boolean loaded, String error) {
        public Layer {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("classification layer description is required");
            }
            if (entries < 0) {
                throw new IllegalArgumentException("classification layer entry count cannot be negative");
            }
        }

        public static Layer loaded(String description, int entries) {
            return new Layer(description, entries, true, null);
        }

        public static Layer failed(String description, String error) {
            return new Layer(description, 0, false, error == null || error.isBlank() ? "unknown error" : error);
        }
    }
}
