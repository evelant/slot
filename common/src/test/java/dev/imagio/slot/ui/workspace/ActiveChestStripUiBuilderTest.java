package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.workflow.domain.ChestRole;
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
    void unclaimedPanelRendersIgnoreRoleAndCyclesToStorage() {
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
        assertEquals("Ignore", strip.children().get(1).text());
        strip.children().get(1).dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertTrue(strip.hasAttachment(WorkspaceUiAttachments.ACTIVE_CHEST_STRIP));
        assertSame(panel, strip.attachment(
                WorkspaceUiAttachments.ACTIVE_CHEST_STRIP,
                SlotWorkspaceViewModel.ActiveChestPanel.class));
        assertSame(panel, context.rolePanel);
        assertEquals(ChestRole.STORAGE, context.role);
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
    void claimedPanelCyclesStorageToBuffer() {
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
        assertEquals("Storage", strip.children().get(1).text());
        strip.children().get(1).dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertSame(panel, context.rolePanel);
        assertEquals(ChestRole.BUFFER, context.role);
    }

    private static final class RecordingContext implements ActiveChestStripUiBuilder.Context {
        SlotWorkspaceViewModel.ActiveChestPanel rolePanel;
        ChestRole role;

        @Override
        public void setChestRoleAt(SlotWorkspaceViewModel.ActiveChestPanel panel, ChestRole role) {
            rolePanel = panel;
            this.role = role;
        }
    }
}
