package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceItemTooltipBuilderTest {
    @Test
    void tooltipExplainsDesiredAndStoragePipsConcisely() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:torch"),
                new ItemStack("minecraft:torch", 7, 64),
                "Torch",
                7,
                0,
                "lighting",
                true,
                true,
                true,
                false,
                12,
                List.of(),
                List.of(
                        new SlotWorkspaceViewModel.ChestPresenceEntry("a", "Main Base", 8),
                        new SlotWorkspaceViewModel.ChestPresenceEntry("b", "Mine", 4)),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("c", "Warehouse", 30)),
                false,
                0,
                0,
                false,
                16,
                true,
                "player.main",
                0,
                7);

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item).stream()
                .map(Component::getString)
                .toList();

        assertEquals("", text.get(0));
        assertEquals("SLOT", text.get(1));
        assertTrue(text.contains("Desired target: 7/16 tab"));
        assertTrue(text.contains("Nearby stored: 12 in Main Base: 8, Mine: 4"));
        assertTrue(text.contains("Stored elsewhere: 30 in Warehouse: 30"));
    }

    @Test
    void tooltipIsAbsentWhenThereIsNoSlotSpecificSignal() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:stone"),
                new ItemStack("minecraft:stone", 1, 64),
                "Stone",
                1,
                0,
                "blocks",
                false,
                true,
                true,
                false,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                "player.main",
                0,
                1);

        assertTrue(WorkspaceItemTooltipBuilder.slotLines(item).isEmpty());
    }

    @Test
    void tooltipExplainsProximateDepositRouteWithoutStoredCount() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:stone"),
                new ItemStack("minecraft:stone", 1, 64),
                "Stone",
                1,
                0,
                "blocks",
                false,
                true,
                true,
                false,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                "player.main",
                0,
                1);

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item, true).stream()
                .map(Component::getString)
                .toList();

        assertTrue(text.contains("Nearby route: deposit route available"));
    }

    @Test
    void tooltipDoesNotCallRemotePutAwayDestinationNearby() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:dirt"),
                new ItemStack("minecraft:dirt", 64, 64),
                "Dirt",
                64,
                0,
                "blocks",
                false,
                true,
                true,
                false,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                "player.main",
                0,
                64,
                SlotWorkspaceViewModel.PutAwayState.ROUTED);

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item).stream()
                .map(Component::getString)
                .toList();

        assertTrue(text.contains("Put away: destination known"));
        assertTrue(text.stream().noneMatch(line -> line.startsWith("Nearby route:")));
    }

    @Test
    void tooltipExplainsPutAwayItemsWithoutKnownHomes() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:dirt"),
                new ItemStack("minecraft:dirt", 64, 64),
                "Dirt",
                64,
                0,
                "blocks",
                false,
                true,
                true,
                false,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                "player.main",
                0,
                64,
                SlotWorkspaceViewModel.PutAwayState.NO_ROUTE);

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item).stream()
                .map(Component::getString)
                .toList();

        assertTrue(text.contains("Put away: no learned destination"));
    }

    @Test
    void tooltipIncludesContextualDebugReasonsWhenEnabled() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:charcoal"),
                new ItemStack("minecraft:charcoal", 1, 64),
                "Charcoal",
                1,
                0,
                "fuel",
                false,
                false,
                true,
                false,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                "player.main",
                0,
                1);
        SlotWorkspaceViewModel.ContextualSuggestionLane lane =
                new SlotWorkspaceViewModel.ContextualSuggestionLane(
                        SlotWorkspaceViewModel.ContextualSuggestionLane.USEFUL_NOW,
                        "Useful Now",
                        List.of(item),
                        "",
                        List.of(new SlotWorkspaceViewModel.ContextualSuggestionDebugInfo(
                                item.identity(),
                                1.25D,
                                0.80D,
                                List.of(
                                        "candidate: carried=true, source=player.main",
                                        "context relevance 0.80 = active 0.80 + passive 0.00",
                                        "score terms: relevance 0.80, carried +0.45"))));

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item, lane, true).stream()
                .map(Component::getString)
                .toList();

        assertTrue(text.contains("Contextual score (Useful Now)"));
        assertTrue(text.contains("  candidate: carried=true, source=player.main"));
        assertTrue(text.contains("  context relevance 0.80 = active 0.80 + passive 0.00"));
        assertTrue(text.contains("  score terms: relevance 0.80, carried +0.45"));
    }

    @Test
    void fluidTooltipUsesFluidTitleAndFormatsMillibuckets() {
        SlotResourceIdentity oxygen = SlotResourceIdentity.fluid("gtceu:oxygen");
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity(oxygen.syntheticItemId()),
                ItemStack.EMPTY,
                "Oxygen",
                1000,
                0,
                "fluids",
                false,
                false,
                true,
                false,
                0,
                List.of(),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("tank", "Machine Tank", 2000)),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("drum", "Remote Drum", 3000)),
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                false,
                false,
                "",
                -1,
                0,
                SlotWorkspaceViewModel.PutAwayState.NONE,
                SlotWorkspaceViewModel.ResourceRef.from(oxygen),
                1000L);

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item).stream()
                .map(Component::getString)
                .toList();

        assertEquals("Oxygen", text.get(0));
        assertEquals("", text.get(1));
        assertEquals("SLOT", text.get(2));
        assertTrue(text.contains("Carried amount: 1 B"));
        assertTrue(text.contains("Nearby stored: 2 B in Machine Tank: 2 B"));
        assertTrue(text.contains("Stored elsewhere: 3 B in Remote Drum: 3 B"));
    }

    @Test
    void wantedItemsReadAsTemporaryCountTargets() {
        SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                identity("minecraft:lantern"),
                new ItemStack("minecraft:lantern", 4, 64),
                "Lantern",
                4,
                0,
                "lighting",
                false,
                true,
                false,
                true,
                4,
                List.of(),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("a", "Supplies", 4)),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                5,
                "",
                -1,
                0);

        List<String> text = WorkspaceItemTooltipBuilder.slotLines(item).stream()
                .map(Component::getString)
                .toList();

        assertTrue(text.contains("Wanted target: 0/5"));
        assertTrue(WorkspaceGatherUiSupport.isGatherableItem(item));
    }

    private static SlotWorkspaceViewModel.IdentityRef identity(String itemId) {
        return new SlotWorkspaceViewModel.IdentityRef(
                itemId,
                ItemComparisonMode.ITEM_ID.name(),
                "");
    }
}
