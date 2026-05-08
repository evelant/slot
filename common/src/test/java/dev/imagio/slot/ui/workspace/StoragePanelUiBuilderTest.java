package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StoragePanelUiBuilderTest {
    @Test
    void hidesWhenNoChestIsProximateAndSearchDoesNotMatch() {
        RecordingContext context = new RecordingContext();
        SlotUiElement overlay = new StoragePanelUiBuilder(context).overlay(viewModel(
                List.of(chip("remote", false)),
                List.of(item("minecraft:stone", "Stone", List.of(), List.of(presence("remote"))))));

        assertNull(overlay);
    }

    @Test
    void showsProximateChestsWithoutSearch() {
        RecordingContext context = new RecordingContext();
        SlotUiElement overlay = new StoragePanelUiBuilder(context).overlay(viewModel(
                List.of(chip("near", true)),
                List.of()));

        assertNotNull(overlay);
        assertNotNull(findStorageChip(overlay));
    }

    @Test
    void showsRemoteChestWhenActiveSearchMatchesAStoredItem() {
        RecordingContext context = new RecordingContext();
        context.normalizedSearchQuery = "stone";
        SlotUiElement overlay = new StoragePanelUiBuilder(context).overlay(viewModel(
                List.of(chip("remote", false)),
                List.of(item("minecraft:stone", "Stone", List.of(), List.of(presence("remote"))))));

        assertNotNull(overlay);
        assertNotNull(findStorageChip(overlay));
    }

    @Test
    void leftClickWhileCarryingDropsCursorIntoChest() {
        RecordingContext context = new RecordingContext();
        context.cursorCarrying = true;
        SlotUiElement overlay = new StoragePanelUiBuilder(context).overlay(viewModel(
                List.of(chip("near", true)),
                List.of()));
        SlotUiElement chip = findStorageChip(overlay);

        chip.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals("near", context.droppedStorageId);
    }

    @Test
    void tickHighlightsChipWhenHoveredIdentityIsPresent() {
        RecordingContext context = new RecordingContext();
        context.hoveredIdentityPresent = true;
        SlotUiElement overlay = new StoragePanelUiBuilder(context).overlay(viewModel(
                List.of(chip("near", true)),
                List.of()));
        SlotUiElement chip = findStorageChip(overlay);

        chip.dispatch(new SlotUiEvent(SlotUiEventKind.TICK, 0, 0, 0, false));

        assertNotNull(chip.overlayColor());
    }

    private static SlotUiElement findStorageChip(SlotUiElement root) {
        if (root == null) {
            return null;
        }
        SlotWorkspaceViewModel.ChestChip chip = root.attachment(
                WorkspaceUiAttachments.STORAGE_CHIP,
                SlotWorkspaceViewModel.ChestChip.class);
        if (chip != null) {
            return root;
        }
        for (SlotUiElement child : root.children()) {
            SlotUiElement found = findStorageChip(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static SlotWorkspaceViewModel viewModel(
            List<SlotWorkspaceViewModel.ChestChip> chips,
            List<SlotWorkspaceViewModel.AtlasItem> items
    ) {
        return new SlotWorkspaceViewModel(
                0,
                "ready",
                "",
                0,
                0,
                1,
                1,
                0,
                0,
                List.of(),
                items,
                List.of(),
                chips,
                List.of(),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(),
                SlotWorkspaceViewModel.LootChestPanel.empty(),
                List.of(),
                Set.of(),
                List.of(),
                SlotWorkspaceViewModel.ActiveChestPanel.empty());
    }

    private static SlotWorkspaceViewModel.ChestChip chip(String storageId, boolean proximate) {
        return new SlotWorkspaceViewModel.ChestChip(
                storageId,
                "minecraft:overworld",
                storageId,
                1,
                27,
                3,
                proximate,
                2,
                1,
                64,
                1,
                "");
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            String itemId,
            String name,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> presence,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere
    ) {
        SlotWorkspaceViewModel.IdentityRef identity = new SlotWorkspaceViewModel.IdentityRef(
                itemId,
                ItemComparisonMode.ITEM_ID.name(),
                "");
        return new SlotWorkspaceViewModel.AtlasItem(
                identity,
                ItemStack.EMPTY,
                name,
                1,
                0,
                "materials",
                false,
                false,
                true,
                false,
                0,
                List.of(),
                presence,
                elsewhere,
                false,
                0,
                0);
    }

    private static SlotWorkspaceViewModel.ChestPresenceEntry presence(String storageId) {
        return new SlotWorkspaceViewModel.ChestPresenceEntry(storageId, storageId, 1);
    }

    private static final class RecordingContext implements StoragePanelUiBuilder.Context {
        String normalizedSearchQuery = "";
        boolean cursorCarrying;
        boolean hoveredIdentityPresent;
        String droppedStorageId;

        @Override
        public String normalizedSearchQuery() {
            return normalizedSearchQuery;
        }

        @Override
        public boolean matchesSearch(SlotWorkspaceViewModel.AtlasItem item) {
            return item != null
                    && !normalizedSearchQuery.isBlank()
                    && item.name().toLowerCase(java.util.Locale.ROOT).contains(normalizedSearchQuery);
        }

        @Override
        public boolean isCursorCarrying() {
            return cursorCarrying;
        }

        @Override
        public WayfindingDisplay.CardText wayfindingText(SlotWorkspaceViewModel.ChestChip chip) {
            return new WayfindingDisplay.CardText(">", "4m");
        }

        @Override
        public boolean isHoveredIdentityPresentInChest(String storageId) {
            return hoveredIdentityPresent;
        }

        @Override
        public void hoverStorage(String storageId) {
        }

        @Override
        public void clearHoveredStorage(String storageId) {
        }

        @Override
        public void dropCursorIntoChest(String storageId) {
            droppedStorageId = storageId;
        }

        @Override
        public void setStatus(String nextStatus) {
        }
    }
}
