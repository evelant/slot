package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imagio.slot.SlotDebugLog;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ForgeContainerSidebar {
    private static final String SLOT_CLASS_PREFIX = "dev.imagio.slot.";
    private static final String AD_ASTRA_PLANETS_SCREEN = "earth.terrarium.adastra.client.screens.PlanetsScreen";
    private static final String AD_ASTRA_PLANETS_MENU = "earth.terrarium.adastra.common.menus.PlanetsMenu";
    private static final List<SidebarHostResolver> SIDEBAR_HOST_RESOLVERS = new CopyOnWriteArrayList<>();
    private static final Set<String> LOGGED_UNSUPPORTED_HOSTS = ConcurrentHashMap.newKeySet();
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

    /**
     * Used by keyboard/EMI hooks that run before or outside the normal
     * screen-event path. The mounted SLOT surface is the text authority here.
     */
    public static boolean capturesTextInput() {
        return activeHostScreen != null
                && activeSurface != null
                && activeSurface.capturesTextInput();
    }

    public static TextInputCaptureDebugState textInputCaptureDebugState(Screen screen) {
        return new TextInputCaptureDebugState(
                activeHostScreen != null,
                screenClass(activeHostScreen),
                activeHostScreen != null && screen == activeHostScreen,
                activeSurface != null,
                activeSurface == null ? null : activeSurface.textInputDebugState());
    }

    public record TextInputCaptureDebugState(
            boolean hostMounted,
            String activeHostScreenClass,
            boolean currentScreenMatchesHost,
            boolean surfacePresent,
            ForgeWorkspaceSurface.TextInputDebugState surfaceState
    ) {
        public boolean capturesTextInput() {
            return hostMounted && surfaceState != null && surfaceState.capturesTextInput();
        }

        public boolean searchActive() {
            return hostMounted && surfaceState != null && surfaceState.searchActive();
        }

        public String compact() {
            return "sidebar{hostMounted=" + hostMounted
                    + ",activeHost=" + activeHostScreenClass
                    + ",currentScreenMatchesHost=" + currentScreenMatchesHost
                    + ",surfacePresent=" + surfacePresent
                    + ",surface=" + (surfaceState == null ? "null" : surfaceState.compact())
                    + "}";
        }
    }

    private static String screenClass(Screen screen) {
        return screen == null ? "null" : screen.getClass().getName();
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

    public static boolean hideActiveSurfaceUntilNextOpen() {
        if (activeHostScreen == null || activeSurface == null) {
            return false;
        }
        release();
        return true;
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
        AbstractContainerMenu menu = screen.getMenu();
        String screenClassName = screen.getClass().getName();
        String menuClassName = className(menu);
        int menuSlotCount = menu == null ? -1 : menu.slots.size();
        SidebarEligibility eligibility = sidebarEligibility(screenClassName, menuClassName, menuSlotCount);
        if (!eligibility.allowed()) {
            logSkippedSidebarHost(eligibility, screenClassName, menuClassName, menuSlotCount);
        }
        return eligibility.allowed();
    }

    static boolean canUseBackingContainerDescriptor(String screenClassName, String menuClassName, int menuSlotCount) {
        return sidebarEligibility(screenClassName, menuClassName, menuSlotCount).allowed();
    }

    private static SidebarEligibility sidebarEligibility(String screenClassName, String menuClassName, int menuSlotCount) {
        if (screenClassName == null || screenClassName.isBlank()) {
            return SidebarEligibility.deny("missing_screen_class");
        }
        if (screenClassName.startsWith(SLOT_CLASS_PREFIX)) {
            return SidebarEligibility.deny("slot_screen");
        }
        if (AD_ASTRA_PLANETS_SCREEN.equals(screenClassName) || AD_ASTRA_PLANETS_MENU.equals(menuClassName)) {
            return SidebarEligibility.deny("ad_astra_planets_travel_screen");
        }
        if (menuClassName == null || menuClassName.isBlank()) {
            return SidebarEligibility.deny("missing_menu_class");
        }
        if (menuSlotCount <= 0) {
            return SidebarEligibility.deny("slotless_menu");
        }
        return SidebarEligibility.allow();
    }

    private static String className(Object value) {
        return value == null ? null : value.getClass().getName();
    }

    private static void logSkippedSidebarHost(
            SidebarEligibility eligibility,
            String screenClassName,
            String menuClassName,
            int menuSlotCount
    ) {
        String key = eligibility.reason() + "|" + screenClassName + "|" + menuClassName + "|" + menuSlotCount;
        if (LOGGED_UNSUPPORTED_HOSTS.add(key)) {
            SlotDebugLog.log(
                    "Forge sidebar not mounted: reason={} screen={} menu={} slots={}",
                    eligibility.reason(),
                    screenClassName,
                    menuClassName == null ? "null" : menuClassName,
                    menuSlotCount);
        }
    }

    private record SidebarEligibility(boolean allowed, String reason) {
        private static SidebarEligibility allow() {
            return new SidebarEligibility(true, "allowed");
        }

        private static SidebarEligibility deny(String reason) {
            return new SidebarEligibility(false, reason);
        }
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
        if (ForgeWorkspaceClient.shouldSkipCanceledScreenKeyPress(
                event.isCanceled(),
                event.getKeyCode(),
                ForgeWorkspaceClient.matchesOpenVanilla(event.getKeyCode(), event.getScanCode()))) {
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
