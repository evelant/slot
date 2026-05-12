package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
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
        assertTrue(text.contains("Desired badge: 7/16 kit"));
        assertTrue(text.contains("Nearby pip: 12 in Main Base: 8, Mine: 4"));
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

        assertTrue(text.contains("Wanted: 0/5"));
        assertTrue(WorkspaceGatherUiSupport.isGatherableItem(item));
    }

    private static SlotWorkspaceViewModel.IdentityRef identity(String itemId) {
        return new SlotWorkspaceViewModel.IdentityRef(
                itemId,
                ItemComparisonMode.ITEM_ID.name(),
                "");
    }
}
