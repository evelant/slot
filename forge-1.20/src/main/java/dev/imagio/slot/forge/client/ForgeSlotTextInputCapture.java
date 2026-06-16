package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.forge.ui.ForgeWorkspaceSurface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ForgeSlotTextInputCapture {
    private ForgeSlotTextInputCapture() {
    }

    public static boolean isActive() {
        return debugState().active();
    }

    public static CaptureDebugState debugState() {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft == null ? null : minecraft.screen;
        boolean workspaceScreen = screen instanceof ForgeWorkspaceScreen;
        ForgeWorkspaceSurface.TextInputDebugState workspaceState = workspaceScreen
                ? ((ForgeWorkspaceScreen) screen).slotTextInputDebugState()
                : null;
        ForgeContainerSidebar.TextInputCaptureDebugState sidebarState =
                ForgeContainerSidebar.textInputCaptureDebugState(screen);
        boolean active = (workspaceState != null && workspaceState.capturesTextInput())
                || sidebarState.capturesTextInput();
        return new CaptureDebugState(screenClass(screen), workspaceScreen, workspaceState, sidebarState, active);
    }

    public record CaptureDebugState(
            String currentScreenClass,
            boolean workspaceScreen,
            ForgeWorkspaceSurface.TextInputDebugState workspaceState,
            ForgeContainerSidebar.TextInputCaptureDebugState sidebarState,
            boolean active
    ) {
        public String compact() {
            return "capture{currentScreen=" + currentScreenClass
                    + ",workspaceScreen=" + workspaceScreen
                    + ",workspace=" + (workspaceState == null ? "null" : workspaceState.compact())
                    + "," + sidebarState.compact()
                    + ",active=" + active
                    + "}";
        }

        public boolean searchActive() {
            return (workspaceState != null && workspaceState.searchActive())
                    || sidebarState.searchActive();
        }
    }

    private static String screenClass(Screen screen) {
        return screen == null ? "null" : screen.getClass().getName();
    }
}
