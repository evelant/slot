package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ForgeSlotTextInputCapture {
    private ForgeSlotTextInputCapture() {
    }

    public static boolean isActive() {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft == null ? null : minecraft.screen;
        if (screen instanceof ForgeWorkspaceScreen workspace && workspace.slotTextInputCapturesKeyboard()) {
            return true;
        }
        return ForgeContainerSidebar.capturesTextInput(screen);
    }
}
