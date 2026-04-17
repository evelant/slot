package dev.imagio.slot.workflow.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record ClaimedChest(
        UUID storageId,
        Set<ChestAnchor> anchors,
        int atlasX,
        int atlasY,
        String label
) {
    public ClaimedChest {
        if (storageId == null) {
            throw new IllegalArgumentException("storageId must not be null");
        }
        anchors = copyAnchors(anchors);
        if (anchors.isEmpty()) {
            throw new IllegalArgumentException("anchors must not be empty");
        }
        label = label == null ? "" : label.trim();
    }

    public ClaimedChest withAtlasPosition(int atlasX, int atlasY) {
        if (this.atlasX == atlasX && this.atlasY == atlasY) {
            return this;
        }
        return new ClaimedChest(storageId, anchors, atlasX, atlasY, label);
    }

    public ClaimedChest withAnchors(Set<ChestAnchor> nextAnchors) {
        Set<ChestAnchor> copied = copyAnchors(nextAnchors);
        if (copied.equals(anchors)) {
            return this;
        }
        return new ClaimedChest(storageId, copied, atlasX, atlasY, label);
    }

    public ClaimedChest withLabel(String nextLabel) {
        String normalized = nextLabel == null ? "" : nextLabel.trim();
        if (normalized.equals(label)) {
            return this;
        }
        return new ClaimedChest(storageId, anchors, atlasX, atlasY, normalized);
    }

    public boolean hasAnchor(ChestAnchor anchor) {
        return anchor != null && anchors.contains(anchor);
    }

    public static Set<ChestAnchor> copyAnchors(Set<ChestAnchor> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<ChestAnchor> copied = new LinkedHashSet<>();
        for (ChestAnchor anchor : source) {
            if (anchor != null) {
                copied.add(anchor);
            }
        }
        return Set.copyOf(copied);
    }
}
