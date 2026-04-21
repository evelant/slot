package dev.imagio.slot.neoforge.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class SlotAtlasKeyMappings {
    private static final String CATEGORY = "key.categories.slot.atlas_navigation";

    private static final KeyMapping CAMERA_BACK = new KeyMapping(
            "key.slot.camera_back",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            CATEGORY
    );

    private static final KeyMapping CAMERA_FORWARD = new KeyMapping(
            "key.slot.camera_forward",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            CATEGORY
    );

    private static final KeyMapping CAMERA_BACK_MOUSE = new KeyMapping(
            "key.slot.camera_back_mouse",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE,
            3,
            CATEGORY
    );

    private static final KeyMapping CAMERA_FORWARD_MOUSE = new KeyMapping(
            "key.slot.camera_forward_mouse",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE,
            4,
            CATEGORY
    );

    private SlotAtlasKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CAMERA_BACK);
        event.register(CAMERA_FORWARD);
        event.register(CAMERA_BACK_MOUSE);
        event.register(CAMERA_FORWARD_MOUSE);
    }

    public static boolean matchesBackKey(int keyCode, int scanCode) {
        return keyMatches(CAMERA_BACK, keyCode, scanCode);
    }

    public static boolean matchesForwardKey(int keyCode, int scanCode) {
        return keyMatches(CAMERA_FORWARD, keyCode, scanCode);
    }

    public static boolean matchesBackMouse(int button) {
        return mouseMatches(CAMERA_BACK_MOUSE, button);
    }

    public static boolean matchesForwardMouse(int button) {
        return mouseMatches(CAMERA_FORWARD_MOUSE, button);
    }

    private static boolean keyMatches(KeyMapping mapping, int keyCode, int scanCode) {
        InputConstants.Key bound = mapping.getKey();
        if (bound.getType() != InputConstants.Type.KEYSYM) {
            return false;
        }
        return bound.getValue() == keyCode;
    }

    private static boolean mouseMatches(KeyMapping mapping, int button) {
        InputConstants.Key bound = mapping.getKey();
        if (bound.getType() != InputConstants.Type.MOUSE) {
            return false;
        }
        return bound.getValue() == button;
    }
}
