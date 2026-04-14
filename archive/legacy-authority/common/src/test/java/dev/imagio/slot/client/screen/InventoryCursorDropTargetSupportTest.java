package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryCursorDropTargetSupportTest {
    @Test
    void carriedDropRequiresValidButtonCursorAndDropZone() {
        assertFalse(InventoryCursorDropTargetSupport.carriedDropTarget(2, true, false, false, true).present());
        assertFalse(InventoryCursorDropTargetSupport.carriedDropTarget(0, false, false, false, true).present());
        assertFalse(InventoryCursorDropTargetSupport.carriedDropTarget(0, true, false, false, false).present());

        InventoryCursorDropTargetSupport.DropTarget target = InventoryCursorDropTargetSupport.carriedDropTarget(
                1,
                true,
                false,
                false,
                true
        );

        assertTrue(target.present());
        assertEquals(InventoryPane.CARRIED, target.pane());
    }

    @Test
    void dockedPanelAndRowsBlockCarriedDrop() {
        assertFalse(InventoryCursorDropTargetSupport.carriedDropTarget(0, true, true, false, true).present());
        assertFalse(InventoryCursorDropTargetSupport.carriedDropTarget(0, true, false, true, true).present());
    }

    @Test
    void paneDropRequiresHoveredPane() {
        assertFalse(InventoryCursorDropTargetSupport.paneDropTarget(0, true, false, false, null).present());
        assertFalse(InventoryCursorDropTargetSupport.paneDropTarget(0, true, true, false, InventoryPane.CARRIED).present());
        assertFalse(InventoryCursorDropTargetSupport.paneDropTarget(0, true, false, true, InventoryPane.CARRIED).present());

        InventoryCursorDropTargetSupport.DropTarget target = InventoryCursorDropTargetSupport.paneDropTarget(
                0,
                true,
                false,
                false,
                InventoryPane.OPEN_CONTAINER
        );

        assertTrue(target.present());
        assertEquals(InventoryPane.OPEN_CONTAINER, target.pane());
    }
}
