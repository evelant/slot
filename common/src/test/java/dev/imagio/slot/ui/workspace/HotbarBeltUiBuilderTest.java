package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotbarBeltUiBuilderTest {
    @Test
    void shiftClickOccupiedSlotReturnsBeforeAssigningSelection() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = belt(context, hotbar(occupied(1))).children().get(1);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, true));

        assertEquals(1, context.returnedHotbarIndex);
    }

    @Test
    void emptySlotClickReportsStatusWithoutSelecting() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = belt(context, hotbar()).children().get(3);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals("belt 4 is empty", context.status);
    }

    @Test
    void occupiedPlainClickDoesNotEnterSelectionMode() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = belt(context, hotbar(occupied(2))).children().get(2);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals("ready", context.status);
        assertEquals(-99, context.returnedHotbarIndex);
    }

    @Test
    void cursorClickDropsCursorAtHotbarSlot() {
        RecordingContext context = new RecordingContext();
        context.cursorCarrying = true;
        SlotUiElement slot = belt(context, hotbar(occupied(2))).children().get(2);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 1, 0, 0, false));

        assertEquals(2, context.cursorDropHotbarIndex);
        assertEquals(1, context.cursorDropButton);
        assertEquals(-99, context.returnedHotbarIndex);
    }

    @Test
    void stripCarriesSlotAndOffhandAttachments() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.HotbarSlot occupied = occupied(2);
        SlotUiElement strip = belt(context, hotbar(occupied));

        assertTrue(strip.hasAttachment(WorkspaceUiAttachments.HOTBAR_STRIP));
        assertSame(occupied, strip.children().get(2).attachment(
                WorkspaceUiAttachments.HOTBAR_SLOT,
                SlotWorkspaceViewModel.HotbarSlot.class));
        assertTrue(strip.children().get(9).hasAttachment(WorkspaceUiAttachments.OFFHAND_SLOT));
    }

    private static SlotUiElement belt(
            RecordingContext context,
            List<SlotWorkspaceViewModel.HotbarSlot> hotbar
    ) {
        return new HotbarBeltUiBuilder(context).belt(hotbar, SlotWorkspaceViewModel.OffhandSlot.empty());
    }

    private static List<SlotWorkspaceViewModel.HotbarSlot> hotbar(SlotWorkspaceViewModel.HotbarSlot... occupied) {
        ArrayList<SlotWorkspaceViewModel.HotbarSlot> slots = new ArrayList<>(SlotWorkspaceViewModel.emptyHotbar());
        for (SlotWorkspaceViewModel.HotbarSlot slot : occupied) {
            slots.set(slot.hotbarIndex(), slot);
        }
        return slots;
    }

    private static SlotWorkspaceViewModel.HotbarSlot occupied(int index) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                index,
                false,
                true,
                new ItemStack("minecraft:stone", 12, 64),
                12);
    }

    private static final class RecordingContext implements HotbarBeltUiBuilder.Context {
        int returnedHotbarIndex = -99;
        boolean cursorCarrying;
        int cursorDropHotbarIndex = -99;
        int cursorDropButton = -99;
        String status;

        @Override
        public void returnHotbarToHome(int hotbarIndex) {
            returnedHotbarIndex = hotbarIndex;
        }

        @Override
        public boolean isCursorCarrying() {
            return cursorCarrying;
        }

        @Override
        public void dropCursorAtHotbar(int hotbarIndex, int button) {
            cursorDropHotbarIndex = hotbarIndex;
            cursorDropButton = button;
        }

        @Override
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
