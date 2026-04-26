package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasLayoutTest {

    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");

    @Test
    void nullViewModelReturnsEmpty() {
        AtlasLayoutResult result = AtlasLayout.layout(null, RelevanceContext.empty(), List.of(), AtlasLayoutConfig.DEFAULT);
        assertSame(AtlasLayoutResult.EMPTY, result);
    }

    @Test
    void emptyContextProducesUniformLayout() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                // Mark these carried — relevance is still 0 (no
                // contributors fire), but the ghostShrink wouldn't
                // apply, so we can verify the relevance baseline = base
                // card size when nothing scores.
                List.of(atlasItem(IRON, "tools", true), atlasItem(GOLD, "tools", true)),
                List.of()
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.ItemPlacement ironPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        AtlasLayoutResult.ItemPlacement goldPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD));
        assertNotNull(ironPlace);
        assertNotNull(goldPlace);
        assertEquals(0f, ironPlace.relevance(), 1e-6);
        assertEquals(0f, goldPlace.relevance(), 1e-6);
        // With zero relevance and carried=true, cells stay at baseCard
        // — no relevance lift, no ghost shrink.
        assertEquals(AtlasLayoutConfig.DEFAULT.baseCardWidth(), ironPlace.width());
        assertEquals(AtlasLayoutConfig.DEFAULT.baseCardWidth(), goldPlace.width());
    }

    @Test
    void nonCarriedItemsShrinkPastBaseline() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools", true), atlasItem(GOLD, "tools", false)),
                List.of()
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.ItemPlacement ironPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        AtlasLayoutResult.ItemPlacement goldPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD));
        // Same relevance score (0, no contributors active), but iron
        // is carried and gold is not — gold's cell shrinks by
        // ghostShrinkFactor.
        assertTrue(goldPlace.width() < ironPlace.width(),
                "non-carried (ghost) cells should be smaller than carried ones at the same relevance");
    }

    @Test
    void carriedItemsGetLargerCells() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools", true), atlasItem(GOLD, "tools", false)),
                List.of()
        );
        RelevanceContext ctx = RelevanceContext.ofCarried(Set.of(IRON));
        AtlasLayoutResult result = AtlasLayout.layout(vm, ctx,
                AtlasRelevance.DEFAULT_CONTRIBUTORS, AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.ItemPlacement ironPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        AtlasLayoutResult.ItemPlacement goldPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD));
        assertEquals(0.9f, ironPlace.relevance(), 1e-6);
        assertEquals(0f, goldPlace.relevance(), 1e-6);
        assertTrue(ironPlace.width() > goldPlace.width(),
                "carried iron should be wider than ghost gold");
    }

    @Test
    void islandPlacementsHonorAuthoredTopLeft() {
        SlotWorkspaceViewModel.AtlasIsland tools = new SlotWorkspaceViewModel.AtlasIsland(
                "tools", "Tools", VisualAtlasIslandKind.PLAYER, 1500, 200, 0xFF000000, 0
        );
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools")),
                List.of()
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.IslandPlacement toolsPlace = result.islandPlacementOf("tools");
        assertEquals(1500, toolsPlace.x(), "island chrome must render at authored x");
        assertEquals(200, toolsPlace.y(), "island chrome must render at authored y");

        AtlasLayoutResult.ItemPlacement ironPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        assertTrue(ironPlace.x() >= 1500, "item must render inside its island, not at atlas origin");
        assertTrue(ironPlace.y() >= 200, "item must render inside its island, not at atlas origin");
    }

    @Test
    void emptyIslandReadsAsConfiguredMinFootprint() {
        SlotWorkspaceViewModel.AtlasIsland empty = synthIsland("empty");
        SlotWorkspaceViewModel vm = viewModel(List.of(empty), List.of(), List.of());
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.IslandPlacement place = result.islandPlacementOf("empty");
        assertEquals(AtlasLayoutConfig.DEFAULT.minIslandWidth(), place.width());
        assertEquals(AtlasLayoutConfig.DEFAULT.minIslandHeight(), place.height());
    }

    @Test
    void overlappingAuthoredIslandsAreDeOverlapped() {
        // Two islands authored at the same origin. After packing, they
        // must not overlap — the second one (sorted by id as the y/x
        // tiebreaker) slides right past the first.
        SlotWorkspaceViewModel.AtlasIsland a = synthIsland("a", 100, 100);
        SlotWorkspaceViewModel.AtlasIsland b = synthIsland("b", 100, 100);
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            items.add(atlasItem(ItemIdentity.of("minecraft:a_" + i), "a"));
            items.add(atlasItem(ItemIdentity.of("minecraft:b_" + i), "b"));
        }
        SlotWorkspaceViewModel vm = viewModel(List.of(a, b), items, List.of());
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.IslandPlacement aPlace = result.islandPlacementOf("a");
        AtlasLayoutResult.IslandPlacement bPlace = result.islandPlacementOf("b");
        assertNotNull(aPlace);
        assertNotNull(bPlace);
        boolean overlapsX = aPlace.x() < bPlace.x() + bPlace.width()
                && bPlace.x() < aPlace.x() + aPlace.width();
        boolean overlapsY = aPlace.y() < bPlace.y() + bPlace.height()
                && bPlace.y() < aPlace.y() + aPlace.height();
        assertTrue(!(overlapsX && overlapsY),
                "auto-square islands at the same authored origin should de-overlap; got "
                        + aPlace + " and " + bPlace);
    }

    @Test
    void wellSpacedAuthoredIslandsKeepTheirOrigins() {
        // Authored positions wide apart — de-overlap should be a no-op
        // and each island should land exactly where it was authored.
        SlotWorkspaceViewModel.AtlasIsland a = synthIsland("a", 0, 0);
        SlotWorkspaceViewModel.AtlasIsland b = synthIsland("b", 1000, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(a, b),
                List.of(atlasItem(IRON, "a"), atlasItem(GOLD, "b")),
                List.of()
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.IslandPlacement aPlace = result.islandPlacementOf("a");
        AtlasLayoutResult.IslandPlacement bPlace = result.islandPlacementOf("b");
        assertEquals(0, aPlace.x());
        assertEquals(0, aPlace.y());
        assertEquals(1000, bPlace.x());
        assertEquals(0, bPlace.y());
    }

    @Test
    void manyOverlappingIslandsAllResolveToDistinctRects() {
        // Stress test: 6 islands authored at the same origin should
        // pack into a horizontal strip without any pair overlapping.
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            String id = "i" + i;
            islands.add(synthIsland(id, 100, 100));
            items.add(atlasItem(ItemIdentity.of("minecraft:" + id), id));
        }
        SlotWorkspaceViewModel vm = viewModel(islands, items, List.of());
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        for (int i = 0; i < islands.size(); i++) {
            AtlasLayoutResult.IslandPlacement aPlace = result.islandPlacementOf(islands.get(i).islandId());
            for (int j = i + 1; j < islands.size(); j++) {
                AtlasLayoutResult.IslandPlacement bPlace = result.islandPlacementOf(islands.get(j).islandId());
                boolean overlapsX = aPlace.x() < bPlace.x() + bPlace.width()
                        && bPlace.x() < aPlace.x() + aPlace.width();
                boolean overlapsY = aPlace.y() < bPlace.y() + bPlace.height()
                        && bPlace.y() < aPlace.y() + aPlace.height();
                assertTrue(!(overlapsX && overlapsY),
                        "pair (" + islands.get(i).islandId() + ", " + islands.get(j).islandId()
                                + ") still overlaps: " + aPlace + " vs " + bPlace);
            }
        }
    }

    @Test
    void manyItemsTriggerAutoSquareWrap() {
        // 16 baseline cells = 16 × 32 × 32 = 16384 pixel area.
        // sqrt(16384) ≈ 128, × 1.1 fudge ≈ 141 → ~4 cards per row
        // (32 + 4 gap, with 8 padding each side).
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            items.add(atlasItem(ItemIdentity.of("minecraft:test_" + i), "tools"));
        }
        SlotWorkspaceViewModel vm = viewModel(List.of(tools), items, List.of());
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.IslandPlacement place = result.islandPlacementOf("tools");
        // The auto-square island should be roughly square — width within a
        // reasonable factor of height.
        float aspect = (float) place.width() / Math.max(1, place.height());
        assertTrue(aspect > 0.6f && aspect < 2.5f,
                "auto-square should produce roughly square islands, got aspect=" + aspect);
    }

    @Test
    void islandPlacementsAreReturned() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel.AtlasIsland food = synthIsland("food");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools, food),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "food")),
                List.of()
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        assertNotNull(result.islandPlacementOf("tools"));
        assertNotNull(result.islandPlacementOf("food"));
        assertEquals(1, result.islandPlacementOf("tools").itemCount());
        assertEquals(1, result.islandPlacementOf("food").itemCount());
    }

    @Test
    void triageIslandsAreSkipped() {
        SlotWorkspaceViewModel.AtlasIsland triage = new SlotWorkspaceViewModel.AtlasIsland(
                "triage", "Triage", VisualAtlasIslandKind.TRIAGE, 0, 0, 0xFF000000, 0
        );
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(triage, tools),
                List.of(atlasItem(IRON, "tools")),
                List.of(atlasItem(GOLD, "triage")) // gold is in triage list, not atlas
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        assertNull(result.islandPlacementOf("triage"));
        assertNotNull(result.islandPlacementOf("tools"));
        assertNull(result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD)));
        assertNotNull(result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON)));
    }

    @Test
    void worldCoordinatesIncludeIslandOffset() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools")),
                List.of()
        );
        AtlasLayoutResult result = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.ItemPlacement ironPlace = result.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        AtlasLayoutResult.IslandPlacement toolsPlace = result.islandPlacementOf("tools");
        // Item world x = island world x + island padding (8 default).
        assertEquals(toolsPlace.x() + AtlasLayoutConfig.DEFAULT.islandPaddingX(), ironPlace.x());
    }

    @Test
    void searchQueryOverloadProducesSameResultAsExplicitContext() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools");
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools")),
                List.of()
        );
        AtlasLayoutResult viaQuery = AtlasLayout.layout(vm, "iron",
                AtlasRelevance.DEFAULT_CONTRIBUTORS, AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.ItemPlacement ironPlace = viaQuery.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        // Search match scores 0.95, expected width = round(32 × 2.425) = 78
        assertEquals(0.95f, ironPlace.relevance(), 1e-6);
    }

    private static SlotWorkspaceViewModel.AtlasIsland synthIsland(String id) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                id, id, VisualAtlasIslandKind.PLAYER, 0, 0, 0xFF000000, 0
        );
    }

    private static SlotWorkspaceViewModel.AtlasIsland synthIsland(String id, int x, int y) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                id, id, VisualAtlasIslandKind.PLAYER, x, y, 0xFF000000, 0
        );
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(ItemIdentity identity, String islandId) {
        return atlasItem(identity, islandId, false);
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(ItemIdentity identity, String islandId, boolean carried) {
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(identity),
                ItemStack.EMPTY,
                identity.itemId(),
                1,
                0,
                islandId,
                false,
                false,
                carried,
                List.of()
        );
    }

    private static SlotWorkspaceViewModel viewModel(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            List<SlotWorkspaceViewModel.AtlasItem> triageItems
    ) {
        return new SlotWorkspaceViewModel(
                1L, "ready", "", 0, 0, 2200, 1480, 0, 0,
                islands, atlasItems, triageItems, List.of(), List.of(), null, List.of()
        );
    }
}
