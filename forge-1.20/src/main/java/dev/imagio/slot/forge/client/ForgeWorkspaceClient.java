package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.network.ForgeWorkspaceViewModelClientCache;
import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
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
            KeyConflictContext.UNIVERSAL,
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

    public static final KeyMapping GATHER_ACTIVE_KIT = new KeyMapping(
            "key.slot.gather_active_kit",
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

    public static boolean wayfindingHudEnabled() {
        return wayfindingHudEnabled;
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
            event.register(GATHER_ACTIVE_KIT);
            event.register(TOGGLE_WAYFINDING_HUD);
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
                Minecraft.getInstance().setScreen(new ForgeWorkspaceScreen());
            }
            while (TOGGLE_WAYFINDING_HUD.consumeClick()) {
                wayfindingHudEnabled = !wayfindingHudEnabled;
            }
            Minecraft minecraft = Minecraft.getInstance();
            while (OPEN_VANILLA_INVENTORY.consumeClick()) {
                if (minecraft == null || minecraft.screen != null || minecraft.player == null) {
                    continue;
                }
                minecraft.setScreen(new InventoryScreen(minecraft.player));
            }
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

        @SubscribeEvent
        public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
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
