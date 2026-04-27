package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AtlasDropResolverTest {

    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");
    private static final ItemIdentity COPPER = ItemIdentity.of("minecraft:copper_ingot");

    @Test
    void dropOnItemReturnsThatItemsOrdinal() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "tools"), atlasItem(COPPER, "tools"))
        );
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.ItemPlacement goldPlace = layout.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD));
        assertNotNull(goldPlace);
        AtlasDropResolver.Resolution resolution = AtlasDropResolver.resolve(
                vm, layout,
                goldPlace.x() + goldPlace.width() / 2,
                goldPlace.y() + goldPlace.height() / 2
        );
        assertNotNull(resolution);
        assertEquals("tools", resolution.islandId());
        // Gold is the second ordinal in the island.
        assertEquals(1, resolution.ordinal());
    }

    @Test
    void dropOnIslandBackgroundAppendsToEnd() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "tools"))
        );
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.IslandPlacement islandPlace = layout.islandPlacementOf("tools");
        assertNotNull(islandPlace);

        // Drop on the bottom-right corner of the island chrome — past every cell.
        AtlasDropResolver.Resolution resolution = AtlasDropResolver.resolve(
                vm, layout,
                islandPlace.x() + islandPlace.width() - 1,
                islandPlace.y() + islandPlace.height() - 1
        );
        assertNotNull(resolution);
        assertEquals("tools", resolution.islandId());
        assertEquals(2, resolution.ordinal(), "drop on island background should append");
    }

    @Test
    void dropInGapBetweenItemsTargetsTheRightOrdinal() {
        // 3 cards in one island. Drop in the chrome strip just LEFT of
        // the second card → insert at ordinal 1 (push GOLD and COPPER
        // down by one), not append.
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "tools"), atlasItem(COPPER, "tools"))
        );
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.ItemPlacement goldPlace = layout.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD));
        assertNotNull(goldPlace);
        // 1 px to the left of GOLD's left edge, vertically centered on GOLD.
        AtlasDropResolver.Resolution resolution = AtlasDropResolver.resolve(
                vm, layout,
                goldPlace.x() - 1,
                goldPlace.y() + goldPlace.height() / 2
        );
        assertNotNull(resolution);
        assertEquals("tools", resolution.islandId());
        assertEquals(1, resolution.ordinal());
    }

    @Test
    void dropInGapBeforeFirstItemTargetsOrdinalZero() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "tools"))
        );
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);
        AtlasLayoutResult.IslandPlacement islandPlace = layout.islandPlacementOf("tools");
        AtlasLayoutResult.ItemPlacement ironPlace = layout.placementOf(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        assertNotNull(islandPlace);
        assertNotNull(ironPlace);

        // Drop in the chrome above the first card.
        AtlasDropResolver.Resolution resolution = AtlasDropResolver.resolve(
                vm, layout,
                ironPlace.x() + ironPlace.width() / 2,
                islandPlace.y() + 1
        );
        assertNotNull(resolution);
        assertEquals("tools", resolution.islandId());
        assertEquals(0, resolution.ordinal());
    }

    @Test
    void dropOutsideAllIslandsReturnsNull() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"))
        );
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        assertNull(AtlasDropResolver.resolve(vm, layout, -10_000, -10_000));
    }

    @Test
    void crossIslandDropTargetsCorrectIsland() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel.AtlasIsland food = synthIsland("food", 1500, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools, food),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "food"))
        );
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        AtlasLayoutResult.ItemPlacement goldPlace = layout.placementOf(SlotWorkspaceViewModel.IdentityRef.from(GOLD));
        assertNotNull(goldPlace);
        AtlasDropResolver.Resolution resolution = AtlasDropResolver.resolve(
                vm, layout,
                goldPlace.x() + goldPlace.width() / 2,
                goldPlace.y() + goldPlace.height() / 2
        );
        assertNotNull(resolution);
        assertEquals("food", resolution.islandId());
        assertEquals(0, resolution.ordinal());
    }

    @Test
    void triageIslandsAreSkipped() {
        SlotWorkspaceViewModel.AtlasIsland triage = new SlotWorkspaceViewModel.AtlasIsland(
                "triage", "Triage", VisualAtlasIslandKind.TRIAGE, 0, 0, 0xFF000000, 0
        );
        SlotWorkspaceViewModel vm = viewModel(List.of(triage), List.of());
        AtlasLayoutResult layout = AtlasLayout.layout(vm, RelevanceContext.empty(),
                List.of(), AtlasLayoutConfig.DEFAULT);

        // Triage isn't part of the layout — even a drop at its origin returns null.
        assertNull(AtlasDropResolver.resolve(vm, layout, 0, 0));
    }

    @Test
    void countItemsInIslandSeesOnlyAtlasItems() {
        SlotWorkspaceViewModel.AtlasIsland tools = synthIsland("tools", 0, 0);
        SlotWorkspaceViewModel vm = viewModel(
                List.of(tools),
                List.of(atlasItem(IRON, "tools"), atlasItem(GOLD, "tools"))
        );
        assertEquals(2, AtlasDropResolver.countItemsInIsland(vm, "tools"));
        assertEquals(0, AtlasDropResolver.countItemsInIsland(vm, "missing"));
    }

    private static SlotWorkspaceViewModel.AtlasIsland synthIsland(String id, int x, int y) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                id, id, VisualAtlasIslandKind.PLAYER, x, y, 0xFF000000, 0
        );
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(ItemIdentity identity, String islandId) {
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(identity),
                ItemStack.EMPTY,
                identity.itemId(),
                1,
                0,
                islandId,
                false,
                false,
                false,
                List.of()
        );
    }

    private static SlotWorkspaceViewModel viewModel(
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems
    ) {
        return new SlotWorkspaceViewModel(
                1L, "ready", "", 0, 0, 2200, 1480, 0, 0,
                islands, atlasItems, List.of(), List.of(), List.of(), null, List.of()
        );
    }
}
