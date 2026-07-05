package dev.imagio.slot.neoforge.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public final class SlotAtlasKeyMappings {
    private static final String CATEGORY = "key.categories.slot.atlas_navigation";

    private static final KeyMapping CAMERA_BACK = new KeyMapping(
            "key.slot.camera_back",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    private static final KeyMapping CAMERA_FORWARD = new KeyMapping(
            "key.slot.camera_forward",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
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

    // Escape hatch from the SLOT GUI to the vanilla inventory screen.
    // Intentionally not consumed from the global client tick: outside SLOT,
    // the binding must not behave like a second inventory key.
    private static final KeyMapping OPEN_VANILLA_INVENTORY = new KeyMapping(
            "key.slot.open_vanilla_inventory",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    // Unbound by default. Cycles the active Kit's pages from anywhere
    // (in-world OR inside the SLOT atlas). Shift+key cycles backward.
    // Only fires when a Kit is active and has >1 page. UNIVERSAL
    // context so the in-world client-tick handler in SlotNeoForgeClient
    // can fire it without the atlas being open.
    private static final KeyMapping CYCLE_KIT_PAGE = new KeyMapping(
            "key.slot.cycle_kit_page",
            KeyConflictContext.UNIVERSAL,
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

    // Toggles the per-card relevance-score debug overlay. Unbound by default —
    // dev-only switch surfaced through the Controls menu so it doesn't compete
    // with player-facing bindings.
    private static final KeyMapping RELEVANCE_DEBUG_OVERLAY = new KeyMapping(
            "key.slot.relevance_debug_overlay",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Toggles the wayfinding HUD chip stack. Default-on (the HUD respects
    // {@link #wayfindingHudEnabled()}, which starts true); a press flips
    // the flag. Unbound by default so the player can opt-in to a hotkey
    // through the Controls menu — most players will never need to toggle.
    private static final KeyMapping TOGGLE_WAYFINDING_HUD = new KeyMapping(
            "key.slot.toggle_wayfinding_hud",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // One-press gather for desired/wanted-count gaps and active-kit needs from
    // nearby chests. Works from inside the SLOT atlas AND in-world.
    // Unbound by default — the action is a small button-click away
    // inside the atlas, and the in-world hotkey is opt-in for power
    // users who refresh kits often.
    private static final KeyMapping GATHER_ACTIVE_KIT = new KeyMapping(
            "key.slot.gather_active_kit",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Same explicit deposit route as the workspace button. Unbound by default
    // so players can opt into a one-key cleanup flow once they trust their
    // learned storage homes.
    private static final KeyMapping DEPOSIT_PUT_AWAY = new KeyMapping(
            "key.slot.deposit_put_away",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Modifier-style default so holding it for wanted-count scroll adjust
    // does not generate text-key repeat toggles. The translation id changed
    // from the original A-key binding so existing dev-profile options do not
    // preserve the repeat-prone default.
    private static final KeyMapping MARK_WANTED = new KeyMapping(
            "key.slot.mark_wanted_modifier",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );

    private static final KeyMapping SET_WANTED_HOVER = new KeyMapping(
            "key.slot.set_wanted_hover",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            CATEGORY
    );

    private static final KeyMapping ADD_VISIBLE_EMI_RECIPE = new KeyMapping(
            "key.slot.add_visible_emi_recipe",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    private static final KeyMapping TRASH_HOVER = new KeyMapping(
            "key.slot.trash_hovered_item",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    private static final KeyMapping STORAGE_XRAY = new KeyMapping(
            "key.slot.storage_xray",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
    );

    private static final KeyMapping MOVE_TO_MAIN_INVENTORY = new KeyMapping(
            "key.slot.move_to_main_inventory",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY
    );

    private static final KeyMapping MOVE_TO_BACKPACK = new KeyMapping(
            "key.slot.move_to_backpack",
            KeyConflictContext.GUI,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_GRAVE_ACCENT),
            CATEGORY
    );

    private static final KeyMapping TAKE_HOVERED_STACK = new KeyMapping(
            "key.slot.take_hovered_stack",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_A,
            CATEGORY
    );

    private static final KeyMapping PUT_HOVERED_STACK = new KeyMapping(
            "key.slot.put_hovered_stack",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_D,
            CATEGORY
    );

    private static final KeyMapping TAKE_HOVERED_ONE = new KeyMapping(
            "key.slot.take_hovered_one",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            CATEGORY
    );

    private static final KeyMapping PUT_HOVERED_ONE = new KeyMapping(
            "key.slot.put_hovered_one",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY
    );

    private static final KeyMapping TAKE_HOVERED_ALL = new KeyMapping(
            "key.slot.take_hovered_all",
            KeyConflictContext.GUI,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_A),
            CATEGORY
    );

    private static final KeyMapping PUT_HOVERED_ALL = new KeyMapping(
            "key.slot.put_hovered_all",
            KeyConflictContext.GUI,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_D),
            CATEGORY
    );

    private static final KeyMapping TAKE_HOVERED_FIVE_STACKS = new KeyMapping(
            "key.slot.take_hovered_five_stacks",
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_A),
            CATEGORY
    );

    private static final KeyMapping PUT_HOVERED_FIVE_STACKS = new KeyMapping(
            "key.slot.put_hovered_five_stacks",
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_D),
            CATEGORY
    );

    private static boolean wayfindingHudEnabled = true;

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
        event.register(RELEVANCE_DEBUG_OVERLAY);
        event.register(TOGGLE_WAYFINDING_HUD);
        event.register(GATHER_ACTIVE_KIT);
        event.register(DEPOSIT_PUT_AWAY);
        event.register(MARK_WANTED);
        event.register(SET_WANTED_HOVER);
        event.register(ADD_VISIBLE_EMI_RECIPE);
        event.register(TRASH_HOVER);
        event.register(STORAGE_XRAY);
        event.register(MOVE_TO_MAIN_INVENTORY);
        event.register(MOVE_TO_BACKPACK);
        event.register(TAKE_HOVERED_STACK);
        event.register(PUT_HOVERED_STACK);
        event.register(TAKE_HOVERED_ONE);
        event.register(PUT_HOVERED_ONE);
        event.register(TAKE_HOVERED_ALL);
        event.register(PUT_HOVERED_ALL);
        event.register(TAKE_HOVERED_FIVE_STACKS);
        event.register(PUT_HOVERED_FIVE_STACKS);
    }

    public static KeyMapping gatherActiveKitMapping() {
        return GATHER_ACTIVE_KIT;
    }

    public static boolean matchesGatherActiveKit(int keyCode, int scanCode) {
        return keyMatches(GATHER_ACTIVE_KIT, keyCode, scanCode);
    }

    public static KeyMapping depositPutAwayMapping() {
        return DEPOSIT_PUT_AWAY;
    }

    public static boolean matchesDepositPutAway(int keyCode, int scanCode) {
        return keyMatches(DEPOSIT_PUT_AWAY, keyCode, scanCode);
    }

    public static boolean matchesMarkWanted(int keyCode, int scanCode) {
        return keyMatches(MARK_WANTED, keyCode, scanCode);
    }

    public static boolean matchesSetWantedHover(int keyCode, int scanCode) {
        return keyMatches(SET_WANTED_HOVER, keyCode, scanCode);
    }

    public static boolean matchesAddVisibleEmiRecipe(int keyCode, int scanCode) {
        return keyMatches(ADD_VISIBLE_EMI_RECIPE, keyCode, scanCode);
    }

    public static boolean setWantedHoverDown() {
        return SET_WANTED_HOVER.isDown() || keyPhysicallyDown(SET_WANTED_HOVER);
    }

    public static boolean matchesTrashHover(int keyCode, int scanCode) {
        return keyMatches(TRASH_HOVER, keyCode, scanCode);
    }

    public static boolean trashHoverDown() {
        return TRASH_HOVER.isDown() || keyPhysicallyDown(TRASH_HOVER);
    }

    public static boolean markWantedDown() {
        if (MARK_WANTED.isDown()) {
            return true;
        }
        InputConstants.Key bound = MARK_WANTED.getKey();
        if (bound.getType() != InputConstants.Type.KEYSYM || !isAltKey(bound.getValue())) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }
        long window = minecraft.getWindow().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
    }

    private static boolean isAltKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
    }

    public static boolean storageXrayDown() {
        return STORAGE_XRAY.isDown() || keyPhysicallyDown(STORAGE_XRAY);
    }

    public static boolean matchesStorageXray(int keyCode, int scanCode) {
        return keyMatches(STORAGE_XRAY, keyCode, scanCode);
    }

    public static boolean matchesMoveToBackpack(int keyCode, int scanCode) {
        return keyMatches(MOVE_TO_BACKPACK, keyCode, scanCode)
                && MOVE_TO_BACKPACK.getKeyModifier().isActive(MOVE_TO_BACKPACK.getKeyConflictContext());
    }

    public static boolean matchesMoveToMainInventory(int keyCode, int scanCode) {
        return keyMatches(MOVE_TO_MAIN_INVENTORY, keyCode, scanCode)
                && !matchesMoveToBackpack(keyCode, scanCode);
    }

    public static WallCardTransferGesturePolicy.KeyboardShortcut hoveredCardShortcut(int keyCode, int scanCode) {
        if (shortcutMatches(TAKE_HOVERED_FIVE_STACKS, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.TAKE_FIVE_STACKS;
        }
        if (shortcutMatches(PUT_HOVERED_FIVE_STACKS, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.PUT_FIVE_STACKS;
        }
        if (shortcutMatches(TAKE_HOVERED_ALL, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.TAKE_ALL;
        }
        if (shortcutMatches(PUT_HOVERED_ALL, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.PUT_ALL;
        }
        if (shortcutMatches(TAKE_HOVERED_STACK, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.TAKE_STACK;
        }
        if (shortcutMatches(PUT_HOVERED_STACK, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.PUT_STACK;
        }
        if (shortcutMatches(TAKE_HOVERED_ONE, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.TAKE_ONE;
        }
        if (shortcutMatches(PUT_HOVERED_ONE, keyCode, scanCode)) {
            return WallCardTransferGesturePolicy.KeyboardShortcut.PUT_ONE;
        }
        return null;
    }

    public static String storageXrayKeyLabel() {
        return STORAGE_XRAY.getTranslatedKeyMessage().getString();
    }

    public static KeyMapping toggleWayfindingHudMapping() {
        return TOGGLE_WAYFINDING_HUD;
    }

    public static boolean wayfindingHudEnabled() {
        return wayfindingHudEnabled;
    }

    public static void setWayfindingHudEnabled(boolean enabled) {
        wayfindingHudEnabled = enabled;
    }

    public static KeyMapping cycleKitPageMapping() {
        return CYCLE_KIT_PAGE;
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

    public static boolean matchesRelevanceDebugOverlay(int keyCode, int scanCode) {
        return keyMatches(RELEVANCE_DEBUG_OVERLAY, keyCode, scanCode);
    }

    /**
     * Returns true if the given keyCode matches the open-vanilla binding.
     * Used by in-screen handlers (e.g. the SLOT atlas) so the binding fires
     * even when {@code KeyMapping#consumeClick} is suppressed by the active
     * screen.
     */
    public static boolean matchesOpenVanilla(int keyCode, int scanCode) {
        return keyMatches(OPEN_VANILLA_INVENTORY, keyCode, scanCode);
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

    private static boolean shortcutMatches(KeyMapping mapping, int keyCode, int scanCode) {
        return mapping.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }

    private static boolean keyPhysicallyDown(KeyMapping mapping) {
        InputConstants.Key bound = mapping.getKey();
        if (bound.getType() != InputConstants.Type.KEYSYM
                || bound.getValue() == InputConstants.UNKNOWN.getValue()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), bound.getValue());
    }

    private static boolean mouseMatches(KeyMapping mapping, int button) {
        InputConstants.Key bound = mapping.getKey();
        if (bound.getType() != InputConstants.Type.MOUSE) {
            return false;
        }
        return bound.getValue() == button;
    }
}
