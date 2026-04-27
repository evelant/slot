package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side atlas layout pass. Consumes the view model + a relevance
 * context, produces world-space placements for every atlas item and
 * island.
 *
 * <p>Triage items + the triage island are intentionally excluded — the
 * triage surface is a docked panel, not part of the atlas, and runs
 * its own layout. See
 * {@code docs/decisions/0005-relevance-score-and-layout-locality.md}
 * for the broader architectural choice (layout is client-owned;
 * scoring is a derivation).
 *
 * <p>Phase 2.2: islands no longer carry authored width/height. The
 * packer wraps to an auto-square target derived from the sum of cell
 * areas (see {@link AtlasLayoutConfig#autoSquareWrapWidth(int)}), so
 * the rendered island shape tracks the packed content with a slight
 * aspect bias.
 */
public final class AtlasLayout {
    private AtlasLayout() {
    }

    /**
     * Run the layout pass with the supplied context + contributors.
     */
    public static AtlasLayoutResult layout(
            SlotWorkspaceViewModel viewModel,
            RelevanceContext context,
            List<RelevanceContributor> contributors,
            AtlasLayoutConfig config
    ) {
        if (viewModel == null) {
            return AtlasLayoutResult.EMPTY;
        }
        AtlasLayoutConfig cfg = config == null ? AtlasLayoutConfig.DEFAULT : config;
        RelevanceContext ctx = context == null ? RelevanceContext.empty() : context;
        List<RelevanceContributor> chain = contributors == null ? List.of() : contributors;

        // Atlas-eligible islands keyed by id, in the order the view model emits them.
        // TRIAGE is its own surface (docked panel); it does not participate in atlas packing.
        LinkedHashMap<String, SlotWorkspaceViewModel.AtlasIsland> atlasIslandsById = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
            if (island == null || island.kind() == VisualAtlasIslandKind.TRIAGE) {
                continue;
            }
            atlasIslandsById.put(island.islandId(), island);
        }

        // First pass: collect per-island items in canonical order plus
        // record which items are carried so the next pass can shrink
        // trailing ghosts. Canonical order = the order viewModel emits
        // them in (server already sorted by (islandId, ordinal, name)).
        LinkedHashMap<String, List<SlotWorkspaceViewModel.AtlasItem>> itemsByIsland = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item == null) {
                continue;
            }
            String islandId = item.islandId();
            if (!atlasIslandsById.containsKey(islandId)) {
                continue;
            }
            itemsByIsland.computeIfAbsent(islandId, k -> new ArrayList<>()).add(item);
        }

        // Second pass: build cell rows. Ghosts after the last carried in
        // canonical order (or every ghost when the island has no carried
        // items at all) shrink to a pip so a row of trailing ghosts
        // packs into a thin strip and an island that's purely ghosts
        // collapses to header-only. Ghosts interleaved between carrieds
        // keep the regular ghostShrinkFactor so the visual ordering
        // stays legible.
        LinkedHashMap<String, List<ItemRow>> rowsByIsland = new LinkedHashMap<>();
        java.util.Set<String> ghostOnlyIslands = new java.util.HashSet<>();
        for (Map.Entry<String, List<SlotWorkspaceViewModel.AtlasItem>> entry : itemsByIsland.entrySet()) {
            List<SlotWorkspaceViewModel.AtlasItem> items = entry.getValue();
            int lastCarriedIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).carried()) {
                    lastCarriedIndex = i;
                }
            }
            if (lastCarriedIndex < 0) {
                ghostOnlyIslands.add(entry.getKey());
            }
            ArrayList<ItemRow> rows = new ArrayList<>(items.size());
            for (int i = 0; i < items.size(); i++) {
                SlotWorkspaceViewModel.AtlasItem item = items.get(i);
                ItemIdentity identity = item.identity().toIdentity();
                float relevance = identity == null
                        ? 0f
                        : RelevanceScore.compute(identity, ctx, chain).value();
                int width = cfg.liftedWidth(relevance);
                int height = cfg.liftedHeight(relevance);
                if (!item.carried()) {
                    boolean trailing = i > lastCarriedIndex;
                    float shrink = trailing
                            ? cfg.ghostShrinkFactor() * cfg.trailingGhostExtraShrink()
                            : cfg.ghostShrinkFactor();
                    int floor = trailing ? cfg.cardGap() + 1 : cfg.cardGap() + 1;
                    width = Math.max(floor, Math.round(width * shrink));
                    height = Math.max(floor, Math.round(height * shrink));
                }
                rows.add(new ItemRow(item.identity(), relevance, width, height));
            }
            rowsByIsland.put(entry.getKey(), rows);
        }

        // Per-island packing: compute the chrome bounds (auto-square)
        // and per-item local placements first. Atlas-level de-overlap
        // happens in the next pass so the bumps below operate on the
        // already-packed footprints.
        LinkedHashMap<String, IslandPack> packsById = new LinkedHashMap<>();
        for (Map.Entry<String, SlotWorkspaceViewModel.AtlasIsland> entry : atlasIslandsById.entrySet()) {
            String islandId = entry.getKey();
            packsById.put(islandId, packIsland(
                    rowsByIsland.getOrDefault(islandId, List.of()),
                    cfg,
                    ghostOnlyIslands.contains(islandId)));
        }

        // Atlas-level de-overlap: islands keep their authored top-left
        // when there's room, but slide right (and wrap to a new row)
        // when their packed footprint would collide with an already-
        // placed neighbour. Walk in (authored y, x, id) order so the
        // visual reading order matches what the player put down.
        LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> islandResults = packAtlas(atlasIslandsById, packsById, cfg);

        // Item placements are anchored to each island's final origin.
        LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, AtlasLayoutResult.ItemPlacement> itemResults = new LinkedHashMap<>();
        for (Map.Entry<String, AtlasLayoutResult.IslandPlacement> entry : islandResults.entrySet()) {
            String islandId = entry.getKey();
            AtlasLayoutResult.IslandPlacement placement = entry.getValue();
            IslandPack pack = packsById.get(islandId);
            if (pack == null) {
                continue;
            }
            for (PackedItem packed : pack.items()) {
                itemResults.put(packed.identity(), new AtlasLayoutResult.ItemPlacement(
                        islandId,
                        placement.x() + packed.localX(),
                        placement.y() + packed.localY(),
                        packed.width(),
                        packed.height(),
                        packed.relevance()
                ));
            }
        }

        return new AtlasLayoutResult(itemResults, islandResults);
    }

    /**
     * Place every island so it doesn't overlap any other. Islands keep
     * their authored top-left as the preferred origin; when that would
     * collide with an already-placed neighbour the placement slides
     * right past the obstruction (with {@code atlasIslandGap}). If
     * sliding right can't make progress (collider is to our left), the
     * placement drops to a row underneath the obstructing rect and
     * resumes from the authored x.
     *
     * <p>The header strip sits ~16 px above the body so we reserve
     * that band on each rect's claimed footprint; otherwise an island
     * whose body just clears would still have its label crashing into
     * the rect above it.
     *
     * <p>Walks islands in {@code (authored y, x, id)} order so the
     * reading order matches what the player put down. Authored
     * positions drive the *preference*; the de-overlap step only
     * kicks in when needed.
     */
    private static LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> packAtlas(
            LinkedHashMap<String, SlotWorkspaceViewModel.AtlasIsland> atlasIslandsById,
            LinkedHashMap<String, IslandPack> packsById,
            AtlasLayoutConfig cfg
    ) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> ordered = new ArrayList<>(atlasIslandsById.values());
        ordered.sort(Comparator
                .comparingInt(SlotWorkspaceViewModel.AtlasIsland::y)
                .thenComparingInt(SlotWorkspaceViewModel.AtlasIsland::x)
                .thenComparing(SlotWorkspaceViewModel.AtlasIsland::islandId));

        int gap = cfg.atlasIslandGap();
        // Reserve the header strip + carried badge band above each
        // body so neighbours don't overlap the label. Sourced from
        // SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE — the same
        // ceiling IslandChestBuilder.applyHeaderScale clamps the
        // world header height to. As long as both sides consult the
        // same constant, even at extreme zoom-out the header can't
        // grow past the reserved band.
        int headerBand = SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE;

        // Placed rects carry their *claimed* footprint (y - headerBand,
        // height + headerBand) so a plain AABB test catches header /
        // body collisions symmetrically.
        ArrayList<PlacedRect> placed = new ArrayList<>(ordered.size());
        LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> results = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.AtlasIsland island : ordered) {
            IslandPack pack = packsById.get(island.islandId());
            int width = pack == null ? cfg.minIslandWidth() : pack.width();
            int height = pack == null ? cfg.minIslandHeight() : pack.height();

            int targetX = island.x();
            int targetY = island.y();
            int placeX = targetX;
            int placeY = targetY;
            int safety = 0;
            int safetyLimit = 4 * Math.max(1, ordered.size());
            while (true) {
                PlacedRect collider = firstOverlap(placed, placeX, placeY - headerBand,
                        width, height + headerBand, gap);
                if (collider == null) {
                    break;
                }
                int slideTo = collider.x() + collider.width() + gap;
                if (slideTo > placeX) {
                    placeX = slideTo;
                } else {
                    // Collider sits to our left or behind us; sliding
                    // right won't make progress. Drop below the rect we
                    // can't slide past and reset to the authored x.
                    placeY = collider.y() + collider.height() + gap;
                    placeX = targetX;
                }
                if (++safety > safetyLimit) {
                    // Pathological input only — bail out at the authored
                    // origin so we still produce a deterministic result.
                    placeX = targetX;
                    placeY = targetY;
                    break;
                }
            }

            placed.add(new PlacedRect(
                    island.islandId(),
                    placeX,
                    placeY - headerBand,
                    width,
                    height + headerBand
            ));
            results.put(island.islandId(), new AtlasLayoutResult.IslandPlacement(
                    island.islandId(),
                    placeX,
                    placeY,
                    width,
                    height,
                    pack == null ? 0 : pack.itemCount()
            ));
        }
        return results;
    }

    /**
     * AABB overlap test with a uniform {@code gap} inflated on the
     * candidate side. {@code (x, y, width, height)} is the candidate's
     * full claimed footprint (header band already included by the
     * caller); each entry in {@code placed} likewise carries its own
     * inflated footprint, so the comparison is symmetric.
     */
    private static PlacedRect firstOverlap(
            List<PlacedRect> placed,
            int x, int y, int width, int height, int gap
    ) {
        int left = x - gap;
        int top = y - gap;
        int right = x + width + gap;
        int bottom = y + height + gap;
        for (PlacedRect rect : placed) {
            if (right <= rect.x() || left >= rect.x() + rect.width()) {
                continue;
            }
            if (bottom <= rect.y() || top >= rect.y() + rect.height()) {
                continue;
            }
            return rect;
        }
        return null;
    }

    private record PlacedRect(String islandId, int x, int y, int width, int height) {
    }

    /**
     * Convenience overload: builds a context from the view model with
     * the supplied search query, then runs the layout.
     */
    public static AtlasLayoutResult layout(
            SlotWorkspaceViewModel viewModel,
            String activeSearchQuery,
            List<RelevanceContributor> contributors,
            AtlasLayoutConfig config
    ) {
        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel, activeSearchQuery);
        return layout(viewModel, ctx, contributors, config);
    }

    private static IslandPack packIsland(List<ItemRow> rows, AtlasLayoutConfig cfg, boolean ghostOnly) {
        ArrayList<WeightedGridPacker.Cell> cells = new ArrayList<>(rows.size());
        long totalCellArea = 0L;
        for (ItemRow row : rows) {
            cells.add(new WeightedGridPacker.Cell(row.width(), row.height()));
            totalCellArea += (long) row.width() * (long) row.height();
        }
        // Auto-square wrap target: sqrt(sumOfCellAreas) × aspectFudge,
        // floored at the empty-island min so single-card islands still
        // read as islands.
        int containerWidth = cfg.autoSquareWrapWidth((int) Math.min(Integer.MAX_VALUE, totalCellArea));
        List<WeightedGridPacker.Placement> packed = WeightedGridPacker.pack(
                cells,
                containerWidth,
                cfg.islandPaddingX(),
                cfg.islandContentTop(),
                cfg.cardGap()
        );

        ArrayList<PackedItem> items = new ArrayList<>(packed.size());
        int maxRight = cfg.islandPaddingX();
        int maxBottom = cfg.islandContentTop();
        for (int i = 0; i < packed.size(); i++) {
            ItemRow row = rows.get(i);
            WeightedGridPacker.Placement p = packed.get(i);
            items.add(new PackedItem(row.identity(), p.localX(), p.localY(), p.width(), p.height(), row.relevance()));
            maxRight = Math.max(maxRight, p.localX() + p.width());
            maxBottom = Math.max(maxBottom, p.localY() + p.height());
        }

        int packedWidth = items.isEmpty()
                ? cfg.minIslandWidth()
                : maxRight + cfg.islandPaddingX();
        int packedHeight = items.isEmpty()
                ? cfg.minIslandHeight()
                : maxBottom + cfg.islandPaddingY();

        // Ghost-only islands collapse to header-only chrome: the body
        // shrinks to the configured min strip so only the title bar
        // reads. Trailing-ghost shrink already keeps their packed
        // height tight, so finalHeight tracks the actual packed content
        // floored at the smaller ghost-only minimum.
        int minWidth = cfg.minIslandWidth();
        int minHeight = ghostOnly ? cfg.ghostOnlyIslandMinHeight() : cfg.minIslandHeight();
        int finalWidth = Math.max(minWidth, packedWidth);
        int finalHeight = Math.max(minHeight, packedHeight);
        return new IslandPack(finalWidth, finalHeight, items);
    }

    private record ItemRow(
            SlotWorkspaceViewModel.IdentityRef identity,
            float relevance,
            int width,
            int height
    ) {
    }

    private record PackedItem(
            SlotWorkspaceViewModel.IdentityRef identity,
            int localX,
            int localY,
            int width,
            int height,
            float relevance
    ) {
    }

    private record IslandPack(int width, int height, List<PackedItem> items) {
        int itemCount() {
            return items.size();
        }
    }
}
