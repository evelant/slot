package dev.imagio.slot.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.imagio.slot.client.SlotClientCompat;
import dev.imagio.slot.client.screen.debug.SlotDebugInventoryScreen;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class SlotDebugKeyMappings {
    private static final KeyMapping OPEN_DEBUG_SCREEN = new KeyMapping(
            "key.slot.open_debug_screen",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.slot"
    );

    private SlotDebugKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_DEBUG_SCREEN);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        while (OPEN_DEBUG_SCREEN.consumeClick()) {
            if (!SlotNeoForgeClient.settingsController().slotEnabled()) {
                continue;
            }

            if (minecraft.screen instanceof ChatScreen) {
                continue;
            }

            if (minecraft.screen instanceof SlotDebugInventoryScreen debugScreen) {
                minecraft.setScreen(debugScreen.parentScreen());
            } else {
                minecraft.setScreen(new SlotDebugInventoryScreen(
                        minecraft.screen,
                        SlotNeoForgeClient.collectionStore(),
                        SlotClientCompat.hasEmi(),
                        SlotNeoForgeClient.collectionViewStateController(),
                        SlotNeoForgeClient.settingsController(),
                        SlotNeoForgeClient.searchWorkflow(),
                        SlotNeoForgeClient.inspectionService()
                ));
            }
        }
    }
}
