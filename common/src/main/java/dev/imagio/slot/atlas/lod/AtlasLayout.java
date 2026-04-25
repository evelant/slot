package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;

import java.util.ArrayList;
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

        // Group atlas items by island, preserving the order they appear in the view model.
        // That order is the canonical order for Phase 2 (it inherits from the prior
        // (islandId, y, x, name) sort and is the seed for the future explicit ordinal field
        // on VisualHomeAssignment).
        LinkedHashMap<String, List<ItemRow>> rowsByIsland = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item == null) {
                continue;
            }
            String islandId = item.islandId();
            if (!atlasIslandsById.containsKey(islandId)) {
                continue;
            }
            ItemIdentity identity = item.identity().toIdentity();
            float relevance = identity == null
                    ? 0f
                    : RelevanceScore.compute(identity, ctx, chain).value();
            int width = cfg.liftedWidth(relevance);
            int height = cfg.liftedHeight(relevance);
            rowsByIsland.computeIfAbsent(islandId, k -> new ArrayList<>())
                    .add(new ItemRow(item.identity(), relevance, width, height));
        }

        // Per-island packing: position items inside each island in canonical order,
        // collect island bounding boxes for the atlas-level pack.
        LinkedHashMap<String, IslandPack> packsByIsland = new LinkedHashMap<>();
        for (Map.Entry<String, SlotWorkspaceViewModel.AtlasIsland> entry : atlasIslandsById.entrySet()) {
            String islandId = entry.getKey();
            SlotWorkspaceViewModel.AtlasIsland island = entry.getValue();
            List<ItemRow> rows = rowsByIsland.getOrDefault(islandId, List.of());
            packsByIsland.put(islandId, packIsland(island, rows, cfg));
        }

        // Atlas-level packing: each island's effective bbox becomes a cell at the
        // atlas level. Container width = view-model canvasWidth.
        ArrayList<WeightedGridPacker.Cell> islandCells = new ArrayList<>(packsByIsland.size());
        ArrayList<String> islandOrder = new ArrayList<>(packsByIsland.size());
        for (Map.Entry<String, IslandPack> entry : packsByIsland.entrySet()) {
            islandOrder.add(entry.getKey());
            IslandPack pack = entry.getValue();
            islandCells.add(new WeightedGridPacker.Cell(pack.width(), pack.height()));
        }
        List<WeightedGridPacker.Placement> islandPlacements = WeightedGridPacker.pack(
                islandCells,
                Math.max(cfg.baseCardWidth(), viewModel.canvasWidth()),
                cfg.atlasMargin(),
                cfg.atlasMargin(),
                cfg.atlasIslandGap()
        );

        // Translate per-island item placements to world coordinates.
        LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, AtlasLayoutResult.ItemPlacement> itemResults = new LinkedHashMap<>();
        LinkedHashMap<String, AtlasLayoutResult.IslandPlacement> islandResults = new LinkedHashMap<>();
        for (int i = 0; i < islandOrder.size(); i++) {
            String islandId = islandOrder.get(i);
            IslandPack pack = packsByIsland.get(islandId);
            WeightedGridPacker.Placement islandPlacement = islandPlacements.get(i);
            islandResults.put(islandId, new AtlasLayoutResult.IslandPlacement(
                    islandId,
                    islandPlacement.localX(),
                    islandPlacement.localY(),
                    pack.width(),
                    pack.height(),
                    pack.itemCount()
            ));
            for (PackedItem packed : pack.items()) {
                itemResults.put(packed.identity(), new AtlasLayoutResult.ItemPlacement(
                        islandId,
                        islandPlacement.localX() + packed.localX(),
                        islandPlacement.localY() + packed.localY(),
                        packed.width(),
                        packed.height(),
                        packed.relevance()
                ));
            }
        }

        return new AtlasLayoutResult(itemResults, islandResults);
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

    private static IslandPack packIsland(
            SlotWorkspaceViewModel.AtlasIsland island,
            List<ItemRow> rows,
            AtlasLayoutConfig cfg
    ) {
        ArrayList<WeightedGridPacker.Cell> cells = new ArrayList<>(rows.size());
        for (ItemRow row : rows) {
            cells.add(new WeightedGridPacker.Cell(row.width(), row.height()));
        }
        // Container width = the island's stored width. For Phase 2 we still
        // honour the island's authored width as the wrap target so widening an
        // island remains a player-driven choice.
        int containerWidth = Math.max(cfg.baseCardWidth() + cfg.islandPaddingX() * 2, island.width());
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
                ? Math.max(cfg.baseCardWidth() + cfg.islandPaddingX() * 2, island.width())
                : maxRight + cfg.islandPaddingX();
        int packedHeight = items.isEmpty()
                ? island.height()
                : maxBottom + cfg.islandPaddingY();

        // Honour the island's authored size as a floor — collapsing to content
        // size is a Phase 3+ concern (chip-mode islands). For now an empty
        // island keeps its authored real estate.
        int finalWidth = Math.max(island.width(), packedWidth);
        int finalHeight = Math.max(island.height(), packedHeight);
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
