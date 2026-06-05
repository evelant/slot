package dev.imagio.slot.neoforge.screen.ldlib;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class SlotWorkspaceTextInputCapture {
    private static final Set<SlotWorkspaceUiController> ACTIVE_CONTROLLERS =
            Collections.newSetFromMap(new WeakHashMap<>());

    private SlotWorkspaceTextInputCapture() {
    }

    static void register(SlotWorkspaceUiController controller) {
        if (controller == null) {
            return;
        }
        synchronized (ACTIVE_CONTROLLERS) {
            ACTIVE_CONTROLLERS.add(controller);
        }
    }

    static void unregister(SlotWorkspaceUiController controller) {
        if (controller == null) {
            return;
        }
        synchronized (ACTIVE_CONTROLLERS) {
            ACTIVE_CONTROLLERS.remove(controller);
        }
    }

    public static boolean isActive() {
        synchronized (ACTIVE_CONTROLLERS) {
            for (SlotWorkspaceUiController controller : ACTIVE_CONTROLLERS) {
                if (controller.capturesTextInput()) {
                    return true;
                }
            }
        }
        return false;
    }
}
