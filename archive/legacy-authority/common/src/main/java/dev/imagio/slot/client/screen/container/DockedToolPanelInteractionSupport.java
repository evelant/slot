package dev.imagio.slot.client.screen.container;

import java.util.function.BooleanSupplier;

public final class DockedToolPanelInteractionSupport {
    private DockedToolPanelInteractionSupport() {
    }

    public static boolean isOver(DockedToolPanel panel, double mouseX, double mouseY) {
        if (panel == null) {
            return false;
        }
        if (panel.contains(mouseX, mouseY)) {
            return true;
        }
        return panel instanceof SlotBackedToolPanel slotBackedToolPanel
                && slotBackedToolPanel.slotAt(mouseX, mouseY) >= 0;
    }

    public static boolean dispatchSlotBackedClick(
            DockedToolPanel panel,
            double mouseX,
            double mouseY,
            int button,
            BooleanSupplier beforeDispatch,
            Runnable afterDispatch
    ) {
        if (!(panel instanceof SlotBackedToolPanel)
                || (button != 0 && button != 1)
                || !isOver(panel, mouseX, mouseY)) {
            return false;
        }
        return dispatchClick(panel, mouseX, mouseY, button, beforeDispatch, afterDispatch);
    }

    public static boolean dispatchClick(
            DockedToolPanel panel,
            double mouseX,
            double mouseY,
            int button,
            BooleanSupplier beforeDispatch,
            Runnable afterDispatch
    ) {
        if (panel == null || !before(beforeDispatch) || !panel.mouseClicked(mouseX, mouseY, button)) {
            return false;
        }
        after(afterDispatch);
        return true;
    }

    public static boolean dispatchRelease(
            DockedToolPanel panel,
            double mouseX,
            double mouseY,
            int button,
            Runnable afterDispatch
    ) {
        if (panel == null || !panel.mouseReleased(mouseX, mouseY, button)) {
            return false;
        }
        after(afterDispatch);
        return true;
    }

    public static boolean dispatchDrag(
            DockedToolPanel panel,
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY,
            Runnable afterDispatch
    ) {
        if (panel == null || !panel.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return false;
        }
        after(afterDispatch);
        return true;
    }

    public static boolean dispatchSlotBackedScroll(
            DockedToolPanel panel,
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY,
            BooleanSupplier beforeDispatch,
            Runnable afterDispatch
    ) {
        if (!(panel instanceof SlotBackedToolPanel slotBackedToolPanel)
                || scrollY == 0.0D
                || slotBackedToolPanel.slotAt(mouseX, mouseY) < 0) {
            return false;
        }
        return dispatchScroll(panel, mouseX, mouseY, scrollX, scrollY, beforeDispatch, afterDispatch);
    }

    public static boolean dispatchScroll(
            DockedToolPanel panel,
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY,
            BooleanSupplier beforeDispatch,
            Runnable afterDispatch
    ) {
        if (panel == null || !before(beforeDispatch) || !panel.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return false;
        }
        after(afterDispatch);
        return true;
    }

    public static void consumeRefreshRequest(DockedToolPanel panel, Runnable onRefreshRequested) {
        if (panel == null || !panel.consumeRefreshRequested()) {
            return;
        }
        after(onRefreshRequested);
    }

    private static boolean before(BooleanSupplier beforeDispatch) {
        return beforeDispatch == null || beforeDispatch.getAsBoolean();
    }

    private static void after(Runnable afterDispatch) {
        if (afterDispatch != null) {
            afterDispatch.run();
        }
    }
}
