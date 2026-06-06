package dev.imagio.slot.forge.client;

import org.lwjgl.glfw.GLFW;

public final class ForgeSlotTextInputKeyGuard {
    private ForgeSlotTextInputKeyGuard() {
    }

    public static boolean shouldLetSlotOwnKey(int keyCode, int modifiers) {
        return ForgeSlotTextInputCapture.isActive() && isPlainTextKey(keyCode, modifiers);
    }

    static boolean isPlainTextKey(int keyCode, int modifiers) {
        int passthroughModifiers = GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER;
        if ((modifiers & passthroughModifiers) != 0) {
            return false;
        }
        return keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_WORLD_2;
    }
}
