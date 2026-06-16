package dev.imagio.slot.forge.client;

import dev.imagio.slot.SlotCommon;
import org.lwjgl.glfw.GLFW;

public final class ForgeSlotTextInputKeyGuard {
    private static final String DIAGNOSTIC_PROPERTY = "slot.forgeTextCaptureDiagnostics";

    private ForgeSlotTextInputKeyGuard() {
    }

    public static boolean shouldLetSlotOwnKey(int keyCode, int modifiers) {
        return decide(keyCode, modifiers).slotOwnsKey();
    }

    public static Decision decide(int keyCode, int modifiers) {
        boolean plainTextKey = isPlainTextKey(keyCode, modifiers);
        ForgeSlotTextInputCapture.CaptureDebugState captureState = ForgeSlotTextInputCapture.debugState();
        return new Decision(plainTextKey, captureState, shouldOwnRecipeViewerTextKey(keyCode, plainTextKey, captureState));
    }

    public static void logDiagnostic(
            String hook,
            int keyCode,
            int scanCode,
            int action,
            int modifiers,
            Decision decision
    ) {
        if (!diagnosticsEnabled() || !isRecipeViewerTextKey(keyCode)) {
            return;
        }
        SlotCommon.LOGGER.info(
                "[SLOT][forge-text-capture] hook={} key={} scanCode={} action={} modifiers={} plainTextKey={} captureActive={} searchActive={} slotOwnsKey={} {}",
                hook,
                keyName(keyCode),
                scanCode,
                actionName(action),
                modifiers,
                decision.plainTextKey(),
                decision.captureState().active(),
                decision.captureState().searchActive(),
                decision.slotOwnsKey(),
                decision.captureState().compact());
    }

    public record Decision(
            boolean plainTextKey,
            ForgeSlotTextInputCapture.CaptureDebugState captureState,
            boolean slotOwnsKey
    ) {
    }

    static boolean isPlainTextKey(int keyCode, int modifiers) {
        int passthroughModifiers = GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER;
        if ((modifiers & passthroughModifiers) != 0) {
            return false;
        }
        if ((keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9)
                || (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)) {
            return false;
        }
        return keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_WORLD_2;
    }

    private static boolean diagnosticsEnabled() {
        return Boolean.parseBoolean(System.getProperty(DIAGNOSTIC_PROPERTY, "true"));
    }

    static boolean isRecipeViewerTextKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_R || keyCode == GLFW.GLFW_KEY_U;
    }

    static boolean shouldOwnRecipeViewerTextKey(
            int keyCode,
            boolean plainTextKey,
            ForgeSlotTextInputCapture.CaptureDebugState captureState
    ) {
        return captureState.searchActive() && plainTextKey && isRecipeViewerTextKey(keyCode);
    }

    private static String keyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            return "R(" + keyCode + ")";
        }
        if (keyCode == GLFW.GLFW_KEY_U) {
            return "U(" + keyCode + ")";
        }
        return Integer.toString(keyCode);
    }

    private static String actionName(int action) {
        if (action == GLFW.GLFW_PRESS) {
            return "PRESS(" + action + ")";
        }
        if (action == GLFW.GLFW_REPEAT) {
            return "REPEAT(" + action + ")";
        }
        if (action == GLFW.GLFW_RELEASE) {
            return "RELEASE(" + action + ")";
        }
        if (action < 0) {
            return "N/A";
        }
        return Integer.toString(action);
    }
}
