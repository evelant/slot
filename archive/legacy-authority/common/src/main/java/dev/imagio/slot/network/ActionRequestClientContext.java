package dev.imagio.slot.network;

import dev.imagio.slot.client.session.SlotScreenSessionResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ActionRequestClientContext {
    private ActionRequestClientContext() {
    }

    public static String currentSessionFingerprint(int containerId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return "";
        }

        AbstractContainerMenu menu = minecraft.player.containerMenu;
        if (menu == null || menu.containerId != containerId) {
            return "";
        }

        Screen screen = minecraft.screen;
        Component title = screen instanceof AbstractContainerScreen<?> containerScreen
                ? containerScreen.getTitle()
                : Component.empty();
        String screenClassName = screen == null ? "" : screen.getClass().getName();
        return SlotScreenSessionResolver.resolveMenuDescriptor(
                title,
                menu,
                minecraft.player.getInventory(),
                screenClassName
        ).fingerprint();
    }
}
