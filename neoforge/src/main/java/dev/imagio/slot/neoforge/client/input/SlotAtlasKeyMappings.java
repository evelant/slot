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

    // Escape hatch to the vanilla inventory screen — works both in a GUI and
    // in-world. Unbound by default so it doesn't collide with anyone's setup;
    // discoverable via the "Vanilla" pill in the atlas top-right action cluster.
    private static final KeyMapping OPEN_VANILLA_INVENTORY = new KeyMapping(
            "key.slot.open_vanilla_inventory",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Unbound by default. Cycles the active Kit's pages while the atlas is open.
    // Shift+key cycles backward. Only fires when a Kit is active and has >1 page.
    private static final KeyMapping CYCLE_KIT_PAGE = new KeyMapping(
            "key.slot.cycle_kit_page",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    private SlotAtlasKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CAMERA_BACK);
        event.register(CAMERA_FORWARD);
        event.register(CAMERA_BACK_MOUSE);
        event.register(CAMERA_FORWARD_MOUSE);
        event.register(OPEN_VANILLA_INVENTORY);
        event.register(CYCLE_KIT_PAGE);
    }

    public static KeyMapping openVanillaInventoryMapping() {
        return OPEN_VANILLA_INVENTORY;
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

    public static boolean matchesCycleKitPage(int keyCode, int scanCode) {
        return keyMatches(CYCLE_KIT_PAGE, keyCode, scanCode);
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
