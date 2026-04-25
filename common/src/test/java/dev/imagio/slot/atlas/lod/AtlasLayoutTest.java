package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

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
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 200, 200);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "tools")),
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
        // With zero relevance, both cells are baseCard.
        assertEquals(AtlasLayoutConfig.DEFAULT.baseCardWidth(), ironPlace.width());
        assertEquals(AtlasLayoutConfig.DEFAULT.baseCardWidth(), goldPlace.width());
    }

    @Test
    void carriedItemsGetLargerCells() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 200, 200);
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
    void islandPlacementsAreReturned() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 200, 200);
        SlotWorkspaceViewModel.AtlasIsland food = synthIsland("food", 200, 200);
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
                "triage", "Triage", VisualAtlasIslandKind.TRIAGE, 0, 0, 200, 200, 0xFF000000, 0
        );
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 200, 200);
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
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 200, 200);
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
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 200, 200);
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

    private static SlotWorkspaceViewModel.AtlasIsland synthIsland(String id, int width, int height) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                id, id, VisualAtlasIslandKind.PLAYER, 0, 0, width, height, 0xFF000000, 0
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
