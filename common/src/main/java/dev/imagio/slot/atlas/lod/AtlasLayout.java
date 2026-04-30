package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger("dev.imagio.slot.atlas.layout");
    private AtlasLayout() {
    }

    /**
     * Run the layout pass with the supplied context + contributors.
     *
     * <p>Convenience overload: passes a transient empty nudge state, so
     * every island is treated as new (no carry-over from a previous frame).
     * Production callers pass a persistent state map via the 5-arg overload
     * so islands keep their pushed-aside positions across frames; tests
     * use this no-state form.
     */
    public static AtlasLayoutResult layout(
            SlotWorkspaceViewModel viewModel,
            RelevanceContext context,
            List<RelevanceContributor> contributors,
            AtlasLayoutConfig config
    ) {
        return layout(viewModel, context, contributors, config, new java.util.HashMap<>());
    }

    /**
     * As {@link #layout(SlotWorkspaceViewModel, RelevanceContext, List, AtlasLayoutConfig)}
     * but with a persistent {@link AtlasNudgeLayout.PrevIslandState} map.
     * The map is mutated in place — keys for deleted islands are removed,
     * surviving keys get this frame's render position written back. Pass
     * the same map in on each subsequent call so push-aside / pull-home
     * carry over correctly.
     */
    public static AtlasLayoutResult layout(
            SlotWorkspaceViewModel viewModel,
            RelevanceContext context,
            List<RelevanceContributor> contributors,
            AtlasLayoutConfig config,
            Map<String, AtlasNudgeLayout.PrevIslandState> nudgeState
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
                // Ghost cards (proximate-chest stock the player can grab)
                // render at the same size as carried cards so they're
                // easy to click on without zooming. We bump their sizing
                // relevance to the CarriedContributor's score (0.9) so
                // liftedWidth/Height match a carried card. We do NOT add
                // ghost identities to the real carried set — kit-missing
                // and other contributors still need to know the player
                // isn't actually holding the item.
                // See docs/plans/learned-storage.md.
                float sizingRelevance = item.carried()
                        ? relevance
                        : Math.max(relevance, GHOST_SIZING_RELEVANCE);
                int width = cfg.liftedWidth(sizingRelevance);
                int height = cfg.liftedHeight(sizingRelevance);
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
        LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> islandResults = packAtlas(atlasIslandsById, packsById, cfg, nudgeState);

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
     * Place every island via {@link AtlasNudgeLayout}: islands sit at their
     * authored home unless a neighbour grew into them (push) or a former
     * obstruction shrank/disappeared (pull-home recovery). See
     * {@code docs/plans/atlas-nudge-layout.md} for design notes.
     *
     * <p>Each island's effective layout footprint includes the header band
     * above its body and the configured inter-island gap padded around its
     * rect, so neighbours can sit flush without their labels crashing into
     * the body above. The body top-left rendered downstream is recovered
     * by stripping the padding back off the resolved padded rect.
     */
    private static LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> packAtlas(
            LinkedHashMap<String, SlotWorkspaceViewModel.AtlasIsland> atlasIslandsById,
            LinkedHashMap<String, IslandPack> packsById,
            AtlasLayoutConfig cfg,
            Map<String, AtlasNudgeLayout.PrevIslandState> nudgeState
    ) {
        int gap = cfg.atlasIslandGap();
        int headerBand = SlotWorkspaceAtlasLayout.ISLAND_HEADER_RESERVE;
        double leftPad = gap / 2.0;
        double rightPad = gap - leftPad;
        double bottomPad = gap;

        ArrayList<AtlasNudgeLayout.IslandSpec> specs = new ArrayList<>(atlasIslandsById.size());
        for (SlotWorkspaceViewModel.AtlasIsland island : atlasIslandsById.values()) {
            IslandPack pack = packsById.get(island.islandId());
            int width = pack == null ? cfg.minIslandWidth() : pack.width();
            int height = pack == null ? cfg.minIslandHeight() : pack.height();
            // Inflate: header band on top, gap on sides + bottom. Nudge
            // operates on padded rects so labels don't crash into bodies
            // above when two islands sit flush.
            double padX = island.x() - leftPad;
            double padY = island.y() - headerBand;
            double padW = width + leftPad + rightPad;
            double padH = height + headerBand + bottomPad;
            specs.add(new AtlasNudgeLayout.IslandSpec(island.islandId(), padX, padY, padW, padH));
        }

        Map<String, AtlasNudgeLayout.PrevIslandState> state = nudgeState == null
                ? new java.util.HashMap<>() : nudgeState;
        List<AtlasNudgeLayout.IslandPlacement> placed = AtlasNudgeLayout.layout(specs, state);

        LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> results = new LinkedHashMap<>();
        for (AtlasNudgeLayout.IslandPlacement r : placed) {
            IslandPack pack = packsById.get(r.id());
            int width = pack == null ? cfg.minIslandWidth() : pack.width();
            int height = pack == null ? cfg.minIslandHeight() : pack.height();
            int bodyX = (int) Math.round(r.renderX() + leftPad);
            int bodyY = (int) Math.round(r.renderY() + headerBand);
            results.put(r.id(), new AtlasLayoutResult.IslandPlacement(
                    r.id(),
                    bodyX,
                    bodyY,
                    width,
                    height,
                    pack == null ? 0 : pack.itemCount()
            ));
        }
        // Diagnostic: post-pack overlap audit on the BODY rects the
        // renderer actually consumes (un-padded, post-rounding). If
        // AtlasNudgeLayout reports clean but this audit reports
        // overlaps, the bug is in the pack post-processing (padding /
        // rounding / size mismatch with the nudge specs). If both
        // report clean, the layout result is correct and the bug is
        // downstream in the renderer.
        if (LOG.isInfoEnabled()) {
            ArrayList<AtlasLayoutResult.IslandPlacement> bodies = new ArrayList<>(results.values());
            int overlaps = 0;
            StringBuilder detail = new StringBuilder();
            for (int i = 0; i < bodies.size(); i++) {
                AtlasLayoutResult.IslandPlacement a = bodies.get(i);
                for (int j = i + 1; j < bodies.size(); j++) {
                    AtlasLayoutResult.IslandPlacement b = bodies.get(j);
                    if (a.x() < b.x() + b.width() && b.x() < a.x() + a.width()
                            && a.y() < b.y() + b.height() && b.y() < a.y() + a.height()) {
                        overlaps++;
                        detail.append(" {").append(a.islandId()).append("@(").append(a.x()).append(",").append(a.y())
                                .append(",").append(a.width()).append("x").append(a.height())
                                .append(") <-> ").append(b.islandId()).append("@(").append(b.x()).append(",").append(b.y())
                                .append(",").append(b.width()).append("x").append(b.height()).append(")}");
                    }
                }
            }
            if (overlaps > 0) {
                LOG.warn("[SLOT][layout] BODY OVERLAPS after pack: count={}{}", overlaps, detail.toString());
            }
        }
        // Preserve the original iteration order for downstream consumers.
        LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> ordered = new LinkedHashMap<>();
        for (String id : atlasIslandsById.keySet()) {
            AtlasLayoutResult.IslandPlacement placement = results.get(id);
            if (placement != null) {
                ordered.put(id, placement);
            }
        }
        return ordered;
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
        return layout(viewModel, activeSearchQuery, contributors, config, new java.util.HashMap<>());
    }

    /**
     * Convenience overload: builds a context from the view model with the
     * supplied search query and threads through a persistent
     * {@link AtlasNudgeLayout.PrevIslandState} map.
     */
    public static AtlasLayoutResult layout(
            SlotWorkspaceViewModel viewModel,
            String activeSearchQuery,
            List<RelevanceContributor> contributors,
            AtlasLayoutConfig config,
            Map<String, AtlasNudgeLayout.PrevIslandState> nudgeState
    ) {
        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel, activeSearchQuery);
        return layout(viewModel, ctx, contributors, config, nudgeState);
    }

    /**
     * Sizing-only relevance floor for ghost cards. Matches
     * CarriedContributor.CARRIED_SCORE so a proximate-chest item
     * renders at the same width/height as the same item carried.
     * Used only for {@code liftedWidth/Height} — does not feed back
     * into actual relevance contributors.
     */
    private static final float GHOST_SIZING_RELEVANCE = 0.9f;

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
