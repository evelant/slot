package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallSectionVisibilityTest {
    @Test
    void collapsedWorkflowTabHidesOrdinaryNearbyGhostOnlySections() {
        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(proximateGhost("minecraft:dirt")),
                false,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertFalse(result.hasVisibleContent());
        assertFalse(result.showNearbyToggle());
        assertEquals(List.of(), result.visibleCards());
    }

    @Test
    void proximateXrayRevealsWorkflowTabNearbyGhostOnlySections() {
        SlotWorkspaceViewModel.AtlasItem ghost = proximateGhost("minecraft:dirt");

        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(ghost),
                false,
                false,
                StorageGhostRevealMode.PROXIMATE,
                false,
                false);

        assertTrue(result.hasVisibleContent());
        assertEquals(List.of(ghost), result.visibleCards());
    }

    @Test
    void collapsedWorkflowTabIgnoresPreviouslyExpandedNearbySection() {
        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(proximateGhost("minecraft:dirt")),
                false,
                true,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertFalse(result.hasVisibleContent());
        assertFalse(result.nearbyExpanded());
        assertEquals(List.of(), result.visibleCards());
    }

    @Test
    void expandedAllViewRevealsNearbyGhostOnlySections() {
        SlotWorkspaceViewModel.AtlasItem ghost = proximateGhost("minecraft:dirt");

        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(ghost),
                false,
                true,
                StorageGhostRevealMode.COLLAPSED,
                false,
                true);

        assertTrue(result.hasVisibleContent());
        assertTrue(result.nearbyExpanded());
        assertEquals(List.of(ghost), result.visibleCards());
    }

    @Test
    void trackedXrayRevealsWorkflowTabTrackedGhostOnlySections() {
        SlotWorkspaceViewModel.AtlasItem ghost = trackedGhost("minecraft:dirt");

        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(ghost),
                false,
                false,
                StorageGhostRevealMode.TRACKED,
                false,
                false);

        assertTrue(result.hasVisibleContent());
        assertEquals(List.of(ghost), result.visibleCards());
    }

    @Test
    void collapsedWorkflowTabRevealsNearbyAcceptedInputGhosts() {
        SlotWorkspaceViewModel.AtlasItem ghost = proximateGhost("tfc:coke")
                .withAcceptedWorkflowInput(true);

        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(ghost),
                false,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertTrue(result.hasVisibleContent());
        assertFalse(result.showNearbyToggle());
        assertEquals(List.of(ghost), result.visibleCards());
    }

    @Test
    void collapsedWorkflowTabDoesNotRevealTrackedOnlyAcceptedInputGhosts() {
        SlotWorkspaceViewModel.AtlasItem ghost = trackedGhost("tfc:coke")
                .withAcceptedWorkflowInput(true);

        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(ghost),
                false,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertFalse(result.hasVisibleContent());
        assertEquals(List.of(), result.visibleCards());
    }

    @Test
    void collapsedWallRevealsJunkGhostsWithoutStorageXray() {
        SlotWorkspaceViewModel.AtlasItem ghost = junkGhost("minecraft:rotten_flesh");

        WallSectionVisibility.Result result = WallSectionVisibility.classify(
                List.of(ghost),
                false,
                false,
                StorageGhostRevealMode.COLLAPSED,
                false,
                false);

        assertTrue(result.hasVisibleContent());
        assertFalse(result.showNearbyToggle());
        assertEquals(List.of(ghost), result.visibleCards());
    }

    @Test
    void usefulNowSuggestionLaneStaysHiddenButPutAwayLaneRenders() {
        SlotWorkspaceViewModel.AtlasItem item = proximateGhost("minecraft:dirt");

        assertFalse(WallSectionUiBuilder.shouldRenderSuggestionLane(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                "Useful Now",
                List.of(item))));
        assertTrue(WallSectionUiBuilder.shouldRenderSuggestionLane(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY,
                "Put Away",
                List.of(item))));
        assertTrue(WallSectionUiBuilder.shouldRenderSuggestionLane(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                SlotWorkspaceViewModel.ContextualSuggestionLane.FETCH,
                "Fetch",
                List.of(item))));
    }

    private static SlotWorkspaceViewModel.AtlasItem proximateGhost(String itemId) {
        return ghost(itemId, 8, List.of());
    }

    private static SlotWorkspaceViewModel.AtlasItem trackedGhost(String itemId) {
        return ghost(itemId, 0, List.of(new SlotWorkspaceViewModel.ChestPresenceEntry(
                "tracked-storage",
                "Storage",
                8)));
    }

    private static SlotWorkspaceViewModel.AtlasItem junkGhost(String itemId) {
        return ghost(itemId, 0, List.of(), true);
    }

    private static SlotWorkspaceViewModel.AtlasItem ghost(
            String itemId,
            int proximateCount,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        return ghost(itemId, proximateCount, elsewhere, false);
    }

    private static SlotWorkspaceViewModel.AtlasItem ghost(
            String itemId,
            int proximateCount,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere,
            boolean junk
    ) {
        ItemIdentity identity = ItemIdentity.of(itemId);
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(identity),
                new ItemStack(itemId, 1, 64),
                itemId,
                1,
                0,
                "blocks",
                false,
                true,
                false,
                true,
                proximateCount,
                List.of(),
                List.of(),
                elsewhere,
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                junk,
                "",
                -1,
                0);
    }
}
