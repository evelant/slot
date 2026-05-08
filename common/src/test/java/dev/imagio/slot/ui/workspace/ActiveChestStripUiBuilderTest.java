package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActiveChestStripUiBuilderTest {
    @Test
    void absentPanelDoesNotRenderStrip() {
        RecordingContext context = new RecordingContext();

        SlotUiElement strip = new ActiveChestStripUiBuilder(context)
                .strip(SlotWorkspaceViewModel.ActiveChestPanel.empty());

        assertNull(strip);
    }

    @Test
    void unclaimedPanelRendersClaimAction() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.ActiveChestPanel panel = new SlotWorkspaceViewModel.ActiveChestPanel(
                "",
                "",
                "",
                "",
                0,
                1,
                2,
                3,
                "minecraft:overworld");

        SlotUiElement strip = new ActiveChestStripUiBuilder(context).strip(panel);
        strip.children().get(1).dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertTrue(strip.hasAttachment(WorkspaceUiAttachments.ACTIVE_CHEST_STRIP));
        assertSame(panel, strip.attachment(
                WorkspaceUiAttachments.ACTIVE_CHEST_STRIP,
                SlotWorkspaceViewModel.ActiveChestPanel.class));
        assertSame(panel, context.claimedPanel);
    }

    @Test
    void claimedPanelShowsClusterAndChestLabel() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.ActiveChestPanel panel = new SlotWorkspaceViewModel.ActiveChestPanel(
                "storage-1",
                "Ore Overflow",
                "cluster-a",
                "Main Base",
                0,
                1,
                2,
                3,
                "minecraft:overworld");

        SlotUiElement strip = new ActiveChestStripUiBuilder(context).strip(panel);

        assertEquals("Main Base / Ore Overflow", strip.children().get(0).text());
    }

    @Test
    void claimedPanelRendersForgetAction() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.ActiveChestPanel panel = new SlotWorkspaceViewModel.ActiveChestPanel(
                "storage-1",
                "Ore Overflow",
                "cluster-a",
                "Main Base",
                0,
                1,
                2,
                3,
                "minecraft:overworld");

        SlotUiElement strip = new ActiveChestStripUiBuilder(context).strip(panel);
        strip.children().get(1).dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals("storage-1", context.forgottenStorageId);
    }

    private static final class RecordingContext implements ActiveChestStripUiBuilder.Context {
        SlotWorkspaceViewModel.ActiveChestPanel claimedPanel;
        String forgottenStorageId;

        @Override
        public void claimChestAt(SlotWorkspaceViewModel.ActiveChestPanel panel) {
            claimedPanel = panel;
        }

        @Override
        public void forgetChest(String storageId) {
            forgottenStorageId = storageId;
        }
    }
}
