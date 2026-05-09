package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.forge.ui.ForgeWorkspaceSurface;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;

public final class ForgeContainerSidebar {
    private static Screen activeHostScreen;
    private static ForgeWorkspaceSurface activeSurface;

    private ForgeContainerSidebar() {
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ForgeWorkspaceScreen) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (screen instanceof InventoryScreen) {
            return;
        }
        String className = screen.getClass().getName();
        if (className.startsWith("dev.imagio.slot.")) {
            return;
        }
        release();
        activeHostScreen = screen;
        activeSurface = new ForgeWorkspaceSurface(ForgeWorkspaceSurface.Mode.SIDEBAR);
        activeSurface.openSessionIfNeeded();
    }

    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == activeHostScreen) {
            release();
        }
    }

    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        activeSurface.tick(event.getScreen().width, event.getScreen().height);
        activeSurface.render(
                event.getGuiGraphics(),
                event.getMouseX(),
                event.getMouseY(),
                event.getScreen().width,
                event.getScreen().height);
    }

    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        if (event.getMouseX() >= ForgeWorkspaceSurface.WIDTH) {
            return;
        }
        if (activeSurface.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        boolean handled = activeSurface.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
        if (event.getMouseX() < ForgeWorkspaceSurface.WIDTH && handled) {
            event.setCanceled(true);
        }
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        if (event.getMouseX() >= ForgeWorkspaceSurface.WIDTH) {
            return;
        }
        if (activeSurface.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
            event.setCanceled(true);
        }
    }

    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        boolean hostTextInputFocused = hostTextInputFocused(event.getScreen());
        if (hostTextInputFocused && !activeSurface.wantsKeyboardInput()) {
            return;
        }
        if (activeSurface.keyPressed(event.getKeyCode(), event.getScanCode(), hostTextInputFocused)) {
            event.setCanceled(true);
        }
    }

    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        boolean hostTextInputFocused = hostTextInputFocused(event.getScreen());
        if (hostTextInputFocused && !activeSurface.wantsKeyboardInput()) {
            return;
        }
        if (activeSurface.charTyped(event.getCodePoint(), hostTextInputFocused)) {
            event.setCanceled(true);
        }
    }

    private static boolean hostTextInputFocused(Screen screen) {
        if (screen == null) {
            return false;
        }
        return isTextInputFocused(screen.getFocused());
    }

    private static boolean isTextInputFocused(GuiEventListener listener) {
        if (listener == null) {
            return false;
        }
        if (listener instanceof EditBox editBox) {
            return editBox.isFocused();
        }
        if (listener instanceof ContainerEventHandler container) {
            return isTextInputFocused(container.getFocused());
        }
        return listener.isFocused();
    }

    private static void release() {
        activeHostScreen = null;
        activeSurface = null;
    }
}
