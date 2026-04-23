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
            GLFW.GLFW_KEY_Q,
            CATEGORY
    );

    private static final KeyMapping CAMERA_FORWARD = new KeyMapping(
            "key.slot.camera_forward",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
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

    // Undo/redo for workspace mutations (home moves, island CRUD, chip accepts,
    // eventually kit activations + chest deposit/take). Active while the atlas
    // is open; user-rebindable through the Controls menu. Default Z / Y chosen
    // because Ctrl-chords in GUI context aren't natively supported by
    // KeyMapping — we handle modifiers on the event side if needed. These
    // single-key defaults are safe because the atlas opens into a GUI where
    // movement keys don't fire.
    private static final KeyMapping UNDO = new KeyMapping(
            "key.slot.undo",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY
    );

    private static final KeyMapping REDO = new KeyMapping(
            "key.slot.redo",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
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
        event.register(UNDO);
        event.register(REDO);
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

    public static boolean matchesUndo(int keyCode, int scanCode) {
        return keyMatches(UNDO, keyCode, scanCode);
    }

    public static boolean matchesRedo(int keyCode, int scanCode) {
        return keyMatches(REDO, keyCode, scanCode);
    }

    /** Returns the user's current display label for the undo binding (e.g. "Z", "Ctrl+Z"). */
    public static String undoKeyLabel() {
        return UNDO.getTranslatedKeyMessage().getString();
    }

    /** Returns the user's current display label for the redo binding. */
    public static String redoKeyLabel() {
        return REDO.getTranslatedKeyMessage().getString();
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
