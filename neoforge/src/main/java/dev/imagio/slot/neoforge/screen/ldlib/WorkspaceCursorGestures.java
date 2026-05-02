package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import net.minecraft.client.gui.screens.Screen;

/**
 * Single point of truth for the split-cursor input vocabulary. Every
 * surface that participates in pickup / drop calls
 * {@link #classify(UIEvent, boolean)} on its MOUSE_DOWN handler so the
 * mapping from raw clicks to {@link Result} stays consistent.
 *
 * <p>Vocabulary:
 * <ul>
 *   <li>ctrl + right-click → {@link Result#PICKUP_HALF} (cumulative on
 *   the same source: each subsequent ctrl+right halves the source's
 *   remaining count again, mirroring vanilla right-click pickup)</li>
 *   <li>cursor non-empty + left-click → {@link Result#DROP_ALL}</li>
 *   <li>cursor non-empty + shift + right-click → {@link Result#DROP_HALF}
 *   (half of the cursor count, not the source)</li>
 *   <li>cursor non-empty + right-click → {@link Result#DROP_ONE}</li>
 *   <li>everything else → {@link Result#NONE} (caller falls through to
 *   its existing click handling)</li>
 * </ul>
 *
 * <p>Note: ctrl+right-click is classified as {@link Result#PICKUP_HALF}
 * even when the cursor is already carrying — the cursor logic itself
 * decides whether to add (same source) or refuse (different source).
 */
final class WorkspaceCursorGestures {
    enum Result { PICKUP_HALF, DROP_ALL, DROP_ONE, DROP_HALF, NONE }

    private WorkspaceCursorGestures() {
    }

    static Result classify(UIEvent event, boolean carrying) {
        if (event == null) {
            return Result.NONE;
        }
        boolean ctrl = Screen.hasControlDown();
        boolean shift = Screen.hasShiftDown();
        if (event.button == 1 && ctrl) {
            return Result.PICKUP_HALF;
        }
        if (!carrying) {
            return Result.NONE;
        }
        if (event.button == 0) {
            return Result.DROP_ALL;
        }
        if (event.button == 1 && shift) {
            return Result.DROP_HALF;
        }
        if (event.button == 1) {
            return Result.DROP_ONE;
        }
        return Result.NONE;
    }
}
