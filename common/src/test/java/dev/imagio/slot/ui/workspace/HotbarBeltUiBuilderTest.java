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
    private static final int HOTBAR_CHILD_OFFSET = 2;

    @Test
    void shiftLeftClickOccupiedSlotQuickMovesToHost() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = hotbarChild(belt(context, hotbar(occupied(1))), 1);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, true));

        assertEquals(1, context.quickMovedHotbarIndex);
        assertEquals(-99, context.returnedHotbarIndex);
    }

    @Test
    void shiftRightClickOccupiedSlotReturnsToStorage() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = hotbarChild(belt(context, hotbar(occupied(1))), 1);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 1, 0, 0, true));

        assertEquals(1, context.returnedHotbarIndex);
        assertEquals(-99, context.quickMovedHotbarIndex);
    }

    @Test
    void emptySlotClickReportsStatusWithoutSelecting() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = hotbarChild(belt(context, hotbar()), 3);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals("belt 4 is empty", context.status);
    }

    @Test
    void occupiedPlainClickDoesNotEnterSelectionMode() {
        RecordingContext context = new RecordingContext();
        SlotUiElement slot = hotbarChild(belt(context, hotbar(occupied(2))), 2);

        slot.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals("ready", context.status);
        assertEquals(-99, context.returnedHotbarIndex);
    }

    @Test
    void cursorClickDropsCursorAtHotbarSlot() {
        RecordingContext context = new RecordingContext();
        context.cursorCarrying = true;
        SlotUiElement slot = hotbarChild(belt(context, hotbar(occupied(2))), 2);

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
        assertTrue(strip.children().get(0).hasAttachment(WorkspaceUiAttachments.OFFHAND_SLOT));
        assertEquals(HotbarBeltUiBuilder.VANILLA_OFFHAND_GAP_PX, strip.children().get(1).layout().width());
        assertSame(occupied, hotbarChild(strip, 2).attachment(
                WorkspaceUiAttachments.HOTBAR_SLOT,
                SlotWorkspaceViewModel.HotbarSlot.class));
    }

    private static SlotUiElement hotbarChild(SlotUiElement belt, int hotbarIndex) {
        return belt.children().get(HOTBAR_CHILD_OFFSET + hotbarIndex);
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
        int quickMovedHotbarIndex = -99;
        String status;

        @Override
        public void returnHotbarToHome(int hotbarIndex) {
            returnedHotbarIndex = hotbarIndex;
        }

        @Override
        public void quickMoveHotbarToHost(int hotbarIndex) {
            quickMovedHotbarIndex = hotbarIndex;
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
