package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.ui.ForgeWorkspaceSpiDebugScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ForgeWorkspaceClient {
    public static final KeyMapping OPEN_WORKSPACE_SCREEN = new KeyMapping(
            "key.slot.open_debug_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.slot"
    );

    private ForgeWorkspaceClient() {
    }

    @Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_WORKSPACE_SCREEN);
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
                Minecraft.getInstance().setScreen(new ForgeWorkspaceSpiDebugScreen());
            }
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
    }
}
