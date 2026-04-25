package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

import java.util.Map;

/**
 * Output of one {@link AtlasLayout} pass — per-item world placements
 * keyed by identity, plus per-island world bounding boxes (the island
 * positions/sizes the renderer should draw the panel chrome at).
 *
 * <p>All coordinates are in world units (the LDLib2 GraphView
 * coordinate system the renderer projects through). Camera-scale
 * conversion happens at render time.
 */
public record AtlasLayoutResult(
        Map<SlotWorkspaceViewModel.IdentityRef, ItemPlacement> itemPlacements,
        Map<String, IslandPlacement> islandPlacements
) {
    public static final AtlasLayoutResult EMPTY = new AtlasLayoutResult(Map.of(), Map.of());

    public AtlasLayoutResult {
        itemPlacements = itemPlacements == null ? Map.of() : Map.copyOf(itemPlacements);
        islandPlacements = islandPlacements == null ? Map.of() : Map.copyOf(islandPlacements);
    }

    /**
     * Look up an item's world placement. Returns {@code null} if the
     * identity wasn't laid out (unknown identity, or an island the
     * layout pass didn't include — e.g., triage, which lives in a
     * docked panel and doesn't go through the atlas packer).
     */
    public ItemPlacement placementOf(SlotWorkspaceViewModel.IdentityRef identity) {
        return identity == null ? null : itemPlacements.get(identity);
    }

    public IslandPlacement islandPlacementOf(String islandId) {
        return islandId == null ? null : islandPlacements.get(islandId);
    }

    public record ItemPlacement(
            String islandId,
            int x,
            int y,
            int width,
            int height,
            float relevance
    ) {
        public ItemPlacement {
            islandId = islandId == null ? "" : islandId;
            width = Math.max(1, width);
            height = Math.max(1, height);
            if (Float.isNaN(relevance) || relevance < 0f) {
                relevance = 0f;
            } else if (relevance > 1f) {
                relevance = 1f;
            }
        }
    }

    public record IslandPlacement(
            String islandId,
            int x,
            int y,
            int width,
            int height,
            int itemCount
    ) {
        public IslandPlacement {
            islandId = islandId == null ? "" : islandId;
            width = Math.max(1, width);
            height = Math.max(1, height);
            itemCount = Math.max(0, itemCount);
        }
    }
}
