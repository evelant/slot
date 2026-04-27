package dev.imagio.slot.workflow.domain;

import java.util.UUID;

public record StorageArea(
        UUID areaId,
        String label,
        int color,
        int atlasX,
        int atlasY,
        int displayOrder
) {
    public StorageArea {
        if (areaId == null) {
            throw new IllegalArgumentException("areaId must not be null");
        }
        label = label == null ? "" : label.trim();
        displayOrder = Math.max(0, displayOrder);
    }

    public StorageArea withLabel(String nextLabel) {
        String normalized = nextLabel == null ? "" : nextLabel.trim();
        if (normalized.equals(label)) {
            return this;
        }
        return new StorageArea(areaId, normalized, color, atlasX, atlasY, displayOrder);
    }

    public StorageArea withColor(int nextColor) {
        if (color == nextColor) {
            return this;
        }
        return new StorageArea(areaId, label, nextColor, atlasX, atlasY, displayOrder);
    }

    public StorageArea withAtlasPosition(int nextAtlasX, int nextAtlasY) {
        if (atlasX == nextAtlasX && atlasY == nextAtlasY) {
            return this;
        }
        return new StorageArea(areaId, label, color, nextAtlasX, nextAtlasY, displayOrder);
    }

    public StorageArea withDisplayOrder(int nextDisplayOrder) {
        int normalized = Math.max(0, nextDisplayOrder);
        if (displayOrder == normalized) {
            return this;
        }
        return new StorageArea(areaId, label, color, atlasX, atlasY, normalized);
    }
}
