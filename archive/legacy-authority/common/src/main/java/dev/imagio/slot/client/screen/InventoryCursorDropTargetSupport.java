package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryPane;

public final class InventoryCursorDropTargetSupport {
    private InventoryCursorDropTargetSupport() {
    }

    public static DropTarget carriedDropTarget(
            int button,
            boolean cursorCarryingStack,
            boolean overDockedToolPanel,
            boolean overItemRow,
            boolean insideCarriedDropZone
    ) {
        if (!validDropButton(button) || !cursorCarryingStack || overDockedToolPanel || overItemRow || !insideCarriedDropZone) {
            return DropTarget.none();
        }
        return DropTarget.carried();
    }

    public static DropTarget paneDropTarget(
            int button,
            boolean cursorCarryingStack,
            boolean overDockedToolPanel,
            boolean overItemRow,
            InventoryPane hoveredPane
    ) {
        if (!validDropButton(button) || !cursorCarryingStack || overDockedToolPanel || overItemRow || hoveredPane == null) {
            return DropTarget.none();
        }
        return DropTarget.pane(hoveredPane);
    }

    private static boolean validDropButton(int button) {
        return button == 0 || button == 1;
    }

    public record DropTarget(
            boolean present,
            InventoryPane pane
    ) {
        private static DropTarget none() {
            return new DropTarget(false, null);
        }

        private static DropTarget carried() {
            return pane(InventoryPane.CARRIED);
        }

        private static DropTarget pane(InventoryPane pane) {
            return new DropTarget(true, pane);
        }
    }
}
