package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.network.ForgeWorkspaceViewModelClientCache;
import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ForgeWorkspaceClient {
    private static final String CATEGORY = "key.categories.slot";

    public static final KeyMapping OPEN_WORKSPACE_SCREEN = new KeyMapping(
            "key.slot.open_workspace_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping OPEN_VANILLA_INVENTORY = new KeyMapping(
            "key.slot.open_vanilla_inventory",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static final KeyMapping CYCLE_KIT_PAGE = new KeyMapping(
            "key.slot.cycle_kit_page",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping UNDO = new KeyMapping(
            "key.slot.undo",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY
    );

    public static final KeyMapping REDO = new KeyMapping(
            "key.slot.redo",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            CATEGORY
    );

    public static final KeyMapping GATHER_ACTIVE_KIT = new KeyMapping(
            "key.slot.gather_active_kit",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping DEPOSIT_PUT_AWAY = new KeyMapping(
            "key.slot.deposit_put_away",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping TOGGLE_WAYFINDING_HUD = new KeyMapping(
            "key.slot.toggle_wayfinding_hud",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping MARK_WANTED = new KeyMapping(
            "key.slot.mark_wanted_modifier",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );

    public static final KeyMapping SET_WANTED_HOVER = new KeyMapping(
            "key.slot.set_wanted_hover",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            CATEGORY
    );

    public static final KeyMapping ADD_VISIBLE_EMI_RECIPE = new KeyMapping(
            "key.slot.add_visible_emi_recipe",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping TRASH_HOVER = new KeyMapping(
            "key.slot.trash_hovered_item",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping STORAGE_XRAY = new KeyMapping(
            "key.slot.storage_xray",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
    );

    public static final KeyMapping MOVE_TO_MAIN_INVENTORY = new KeyMapping(
            "key.slot.move_to_main_inventory",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY
    );

    public static final KeyMapping MOVE_TO_BACKPACK = new KeyMapping(
            "key.slot.move_to_backpack",
            KeyConflictContext.GUI,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_GRAVE_ACCENT),
            CATEGORY
    );

    private static boolean wayfindingHudEnabled = true;

    private ForgeWorkspaceClient() {
    }

    public static boolean matchesOpenVanilla(int keyCode, int scanCode) {
        return OPEN_VANILLA_INVENTORY.matches(keyCode, scanCode);
    }

    public static boolean matchesCycleKitPage(int keyCode, int scanCode) {
        return CYCLE_KIT_PAGE.matches(keyCode, scanCode);
    }

    public static boolean matchesGatherActiveKit(int keyCode, int scanCode) {
        return GATHER_ACTIVE_KIT.matches(keyCode, scanCode);
    }

    public static boolean matchesDepositPutAway(int keyCode, int scanCode) {
        return DEPOSIT_PUT_AWAY.matches(keyCode, scanCode);
    }

    public static boolean matchesUndo(int keyCode, int scanCode) {
        return UNDO.matches(keyCode, scanCode);
    }

    public static boolean matchesRedo(int keyCode, int scanCode) {
        return REDO.matches(keyCode, scanCode);
    }

    public static boolean matchesMarkWanted(int keyCode, int scanCode) {
        return MARK_WANTED.matches(keyCode, scanCode);
    }

    public static boolean matchesSetWantedHover(int keyCode, int scanCode) {
        return SET_WANTED_HOVER.matches(keyCode, scanCode);
    }

    public static boolean matchesAddVisibleEmiRecipe(int keyCode, int scanCode) {
        return ADD_VISIBLE_EMI_RECIPE.matches(keyCode, scanCode);
    }

    public static boolean setWantedHoverDown() {
        return SET_WANTED_HOVER.isDown() || keyPhysicallyDown(SET_WANTED_HOVER);
    }

    public static boolean matchesTrashHover(int keyCode, int scanCode) {
        return TRASH_HOVER.matches(keyCode, scanCode);
    }

    public static boolean trashHoverDown() {
        return TRASH_HOVER.isDown() || keyPhysicallyDown(TRASH_HOVER);
    }

    public static boolean matchesStorageXray(int keyCode, int scanCode) {
        return STORAGE_XRAY.matches(keyCode, scanCode);
    }

    public static boolean matchesMoveToMainInventory(int keyCode, int scanCode) {
        return MOVE_TO_MAIN_INVENTORY.matches(keyCode, scanCode) && !matchesMoveToBackpack(keyCode, scanCode);
    }

    public static boolean matchesMoveToBackpack(int keyCode, int scanCode) {
        return MOVE_TO_BACKPACK.matches(keyCode, scanCode)
                && MOVE_TO_BACKPACK.getKeyModifier().isActive(MOVE_TO_BACKPACK.getKeyConflictContext());
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

    public static String storageXrayKeyLabel() {
        return STORAGE_XRAY.getTranslatedKeyMessage().getString();
    }

    public static boolean wayfindingHudEnabled() {
        return wayfindingHudEnabled;
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

    public static void openWorkspaceScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        closeActiveContainerForScreenSwap(minecraft);
        minecraft.setScreen(new ForgeWorkspaceScreen());
    }

    static void closeActiveContainerForScreenSwap(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        if (minecraft.player.containerMenu != minecraft.player.inventoryMenu) {
            minecraft.player.closeContainer();
        }
    }

    @Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_WORKSPACE_SCREEN);
            event.register(OPEN_VANILLA_INVENTORY);
            event.register(CYCLE_KIT_PAGE);
            event.register(UNDO);
            event.register(REDO);
            event.register(GATHER_ACTIVE_KIT);
            event.register(DEPOSIT_PUT_AWAY);
            event.register(TOGGLE_WAYFINDING_HUD);
            event.register(MARK_WANTED);
            event.register(SET_WANTED_HOVER);
            event.register(ADD_VISIBLE_EMI_RECIPE);
            event.register(TRASH_HOVER);
            event.register(STORAGE_XRAY);
            event.register(MOVE_TO_MAIN_INVENTORY);
            event.register(MOVE_TO_BACKPACK);
        }
    }

    @Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class GameBus {
        private GameBus() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            while (OPEN_WORKSPACE_SCREEN.consumeClick()) {
                openWorkspaceScreen();
            }
            while (TOGGLE_WAYFINDING_HUD.consumeClick()) {
                wayfindingHudEnabled = !wayfindingHudEnabled;
            }
            Minecraft minecraft = Minecraft.getInstance();
            while (CYCLE_KIT_PAGE.consumeClick()) {
                if (minecraft == null || minecraft.screen != null) {
                    continue;
                }
                int direction = Screen.hasShiftDown() ? -1 : 1;
                SlotForgeNetworking.cycleKitPage(direction);
            }
            while (GATHER_ACTIVE_KIT.consumeClick()) {
                if (minecraft == null || minecraft.screen != null) {
                    continue;
                }
                SlotForgeNetworking.gatherActiveKit();
            }
            while (DEPOSIT_PUT_AWAY.consumeClick()) {
                if (minecraft == null || minecraft.screen != null) {
                    continue;
                }
                SlotForgeNetworking.depositPutAway();
            }
            ForgeHoveredWantedHotkey.onClientTick();
            ForgeHoveredTrashHotkey.onClientTick();
            ForgeContainerSidebar.onClientTick();
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            ForgeWayfindingHudRenderer.onRenderGui(event);
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            ForgeWayfindingChestGlowRenderer.onRenderLevelStage(event);
        }

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            ForgeContainerSidebar.onScreenInit(event);
        }

        @SubscribeEvent
        public static void onScreenClosing(ScreenEvent.Closing event) {
            ForgeContainerSidebar.onScreenClosing(event);
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            ForgeContainerSidebar.onScreenRender(event);
        }

        @SubscribeEvent
        public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
            ForgeContainerSidebar.onMousePressed(event);
        }

        @SubscribeEvent
        public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
            ForgeContainerSidebar.onMouseReleased(event);
        }

        @SubscribeEvent
        public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
            ForgeContainerSidebar.onMouseScrolled(event);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
            ForgeHoveredWantedHotkey.onKeyPressed(event);
            ForgeHoveredTrashHotkey.onKeyPressed(event);
            ForgeContainerSidebar.onKeyPressed(event);
        }

        @SubscribeEvent
        public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
            ForgeContainerSidebar.onCharTyped(event);
        }

        @SubscribeEvent
        public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ForgeContainerSidebar.clearClientState();
            ForgeWorkspaceViewModelClientCache.clear();
        }
    }
}
