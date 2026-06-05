package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.forge.ui.ForgeWorkspaceSurface;
import dev.imagio.slot.forge.config.SlotForgeClientConfig;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ForgeContainerSidebar {
    private static final List<SidebarHostResolver> SIDEBAR_HOST_RESOLVERS = new CopyOnWriteArrayList<>();
    private static Screen activeHostScreen;
    private static ForgeWorkspaceSurface activeSurface;
    private static boolean bypassNextInventorySidebar;

    private ForgeContainerSidebar() {
    }

    @FunctionalInterface
    public interface SidebarHostResolver {
        SidebarHost resolve(Screen screen);
    }

    public record SidebarHost(Screen renderScreen, AbstractContainerScreen<?> menuScreen) {
        public SidebarHost {
            if (renderScreen == null || menuScreen == null) {
                throw new IllegalArgumentException("sidebar host screens must be non-null");
            }
        }
    }

    /**
     * Currently-mounted host screen, or {@code null} when no Forge sidebar
     * is active. Read by recipe-viewer compat plugins to publish exclusion
     * bounds only for the screen SLOT is actually rendering into.
     */
    public static Screen activeHostScreen() {
        return activeHostScreen;
    }

    /**
     * Width (in screen px) of the active Forge sidebar mount, or {@code 0}
     * when no sidebar is mounted.
     */
    public static int activeSidebarWidth() {
        if (activeHostScreen == null) {
            return 0;
        }
        int contentWidth = activeSurface == null ? ForgeWorkspaceSurface.WIDTH : activeSurface.contentWidth();
        return SlotForgeClientConfig.sidebarLeftMargin() + contentWidth;
    }

    public static ForgeWorkspaceSurface.TaskPanelBounds activeTaskPanelBounds(Screen screen) {
        if (screen != activeHostScreen || activeSurface == null) {
            return null;
        }
        return activeSurface.taskPanelBounds(screen.width, screen.height);
    }

    public static ForgeWorkspaceSurface.RecentsPanelBounds activeRecentsPanelBounds(Screen screen) {
        if (screen != activeHostScreen || activeSurface == null) {
            return null;
        }
        return activeSurface.recentsPanelBounds(screen.width);
    }

    public static void registerSidebarHostResolver(SidebarHostResolver resolver) {
        if (resolver != null && !SIDEBAR_HOST_RESOLVERS.contains(resolver)) {
            SIDEBAR_HOST_RESOLVERS.add(resolver);
        }
    }

    public static void setRecipeSidebarSpec(Screen screen, RecipeIngredientSidebarSpec spec) {
        if (screen == activeHostScreen && activeSurface != null) {
            activeSurface.setRecipeSidebarSpec(spec);
        }
    }

    public static void setCraftRunRecipeCaptures(Screen screen, List<CraftRunRecipeCapture> captures) {
        if (screen == activeHostScreen && activeSurface != null) {
            activeSurface.setCraftRunRecipeCaptures(captures);
        }
    }

    public static boolean capturesTextInput(Screen screen) {
        return screen == activeHostScreen
                && activeSurface != null
                && activeSurface.capturesTextInput();
    }

    public static void clearClientState() {
        release();
        bypassNextInventorySidebar = false;
    }

    public static void openVanillaInventory() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        ForgeWorkspaceClient.closeActiveContainerForScreenSwap(minecraft);
        bypassNextInventorySidebar = true;
        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ForgeWorkspaceScreen) {
            return;
        }
        Screen renderScreen = event.getScreen();
        AbstractContainerScreen<?> backingMenuScreen;
        if (renderScreen instanceof AbstractContainerScreen<?> screen) {
            if (screen instanceof InventoryScreen) {
                if (bypassNextInventorySidebar) {
                    bypassNextInventorySidebar = false;
                    return;
                }
            } else {
                bypassNextInventorySidebar = false;
            }
            if (!canMountOnContainer(screen)) {
                return;
            }
            backingMenuScreen = screen;
        } else {
            SidebarHost host = sidebarHost(renderScreen);
            if (host == null || !canUseBackingContainer(host.menuScreen())) {
                return;
            }
            bypassNextInventorySidebar = false;
            backingMenuScreen = host.menuScreen();
        }
        release();
        activeHostScreen = renderScreen;
        activeSurface = new ForgeWorkspaceSurface(
                ForgeWorkspaceSurface.Mode.SIDEBAR,
                menuContainerId(backingMenuScreen));
        activeSurface.openSessionIfNeeded();
    }

    private static SidebarHost sidebarHost(Screen screen) {
        for (SidebarHostResolver resolver : SIDEBAR_HOST_RESOLVERS) {
            SidebarHost host = resolver.resolve(screen);
            if (host != null) {
                return host;
            }
        }
        return null;
    }

    private static boolean canMountOnContainer(AbstractContainerScreen<?> screen) {
        return canUseBackingContainer(screen);
    }

    private static boolean canUseBackingContainer(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return false;
        }
        return !screen.getClass().getName().startsWith("dev.imagio.slot.");
    }

    static int menuContainerId(AbstractContainerScreen<?> screen) {
        if (screen == null || screen.getMenu() == null) {
            return WorkspaceActionEnvelope.NO_MENU_CONTAINER;
        }
        return screen.getMenu().containerId;
    }

    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == activeHostScreen) {
            release();
        }
    }

    public static void onClientTick() {
        if (activeHostScreen == null || activeSurface == null) {
            return;
        }
        activeSurface.tick(activeHostScreen.width, activeHostScreen.height);
    }

    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        RenderSystem.disableDepthTest();
        try {
            activeSurface.render(
                    event.getGuiGraphics(),
                    event.getMouseX(),
                    event.getMouseY(),
                    event.getScreen().width,
                    event.getScreen().height);
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        if (!insideInteractiveSurface(event.getMouseX(), event.getMouseY())) {
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
        if (insideInteractiveSurface(event.getMouseX(), event.getMouseY()) && handled) {
            event.setCanceled(true);
        }
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() != activeHostScreen || activeSurface == null) {
            return;
        }
        if (!insideInteractiveSurface(event.getMouseX(), event.getMouseY())) {
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
        if (event.isCanceled() && event.getKeyCode() != GLFW.GLFW_KEY_TAB) {
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

    static boolean isTextInputFocused(GuiEventListener listener) {
        if (listener == null) {
            return false;
        }
        if (listener instanceof EditBox editBox) {
            return editBox.isFocused();
        }
        if (listener instanceof MultiLineEditBox editBox) {
            return editBox.isFocused();
        }
        if (listener instanceof ContainerEventHandler container) {
            return isTextInputFocused(container.getFocused());
        }
        return false;
    }

    private static boolean insideInteractiveSurface(double mouseX, double mouseY) {
        if (activeHostScreen == null || activeSurface == null) {
            return false;
        }
        int left = SlotForgeClientConfig.sidebarLeftMargin();
        int top = SlotForgeClientConfig.sidebarTopMargin();
        int right = left + activeSurface.contentWidth();
        int bottom = activeHostScreen.height - SlotForgeClientConfig.sidebarBottomMargin();
        return insideInteractiveSurface(
                mouseX,
                mouseY,
                activeSurface.hasActiveOverlay(),
                left,
                top,
                right,
                bottom,
                activeRecentsPanelBounds(activeHostScreen),
                activeTaskPanelBounds(activeHostScreen));
    }

    static boolean insideInteractiveSurface(
            double mouseX,
            double mouseY,
            boolean overlayActive,
            int sidebarLeft,
            int sidebarTop,
            int sidebarRight,
            int sidebarBottom,
            ForgeWorkspaceSurface.RecentsPanelBounds recentsBounds,
            ForgeWorkspaceSurface.TaskPanelBounds taskPanelBounds
    ) {
        if (overlayActive) {
            return true;
        }
        if (mouseX >= sidebarLeft && mouseX < sidebarRight && mouseY >= sidebarTop && mouseY < sidebarBottom) {
            return true;
        }
        if (contains(recentsBounds, mouseX, mouseY)) {
            return true;
        }
        return contains(taskPanelBounds, mouseX, mouseY);
    }

    private static boolean contains(ForgeWorkspaceSurface.RecentsPanelBounds bounds, double mouseX, double mouseY) {
        return bounds != null
                && mouseX >= bounds.x()
                && mouseX < bounds.x() + bounds.width()
                && mouseY >= bounds.y()
                && mouseY < bounds.y() + bounds.height();
    }

    private static boolean contains(ForgeWorkspaceSurface.TaskPanelBounds bounds, double mouseX, double mouseY) {
        return bounds != null
                && mouseX >= bounds.x()
                && mouseX < bounds.x() + bounds.width()
                && mouseY >= bounds.y()
                && mouseY < bounds.y() + bounds.height();
    }

    private static void release() {
        if (activeSurface != null) {
            activeSurface.closeSession();
        }
        activeHostScreen = null;
        activeSurface = null;
    }
}
