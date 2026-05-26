package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.mixin.AbstractContainerScreenAccessor;
import dev.imagio.slot.neoforge.network.SlotSidebarClosePayload;
import dev.imagio.slot.neoforge.network.SlotSidebarOpenPayload;
import dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarClientUi;
import dev.imagio.slot.ui.workspace.CraftRunUiBuilder;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Phase 3a entry point — when an {@link AbstractContainerScreen} other
 * than the SLOT workspace opens, mount the wall as a left sidebar so
 * the player can act on carried inventory without shuffling through
 * the vanilla 36-slot band. See {@code docs/plans/list-view-phase-3a.md}.
 *
 * <p>Mount strategy: {@link SlotSidebarClientUi#mount} builds a
 * sidebar-mode workspace UI and adds it as a child widget of the host
 * screen. LDLib2's children-walking mixins drive the lifecycle from
 * there. Server-side data pump runs through
 * {@link dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarUiHandles},
 * which owns a per-player handle and attaches the sidebar's sync manager
 * to the active host menu. The host menu (chest, crafting, machine)
 * remains the player's {@code containerMenu}; vanilla
 * {@code broadcastChanges()} ticks LDLib sync, while the workspace view
 * itself refreshes only when its structural menu state changes.
 */
public final class SlotContainerSidebar {
    /**
     * Floor on the sidebar's screen-pixel width. The actual width comes
     * from {@link SlotSidebarClientUi#preferredSidebarWidth()} (post-
     * Phase-7 single-column layout this is ~280 px); this floor is a
     * safety net for any future configuration that pushes the workspace
     * narrower than its widget tree can reasonably layout. Pinned at
     * 240 because the wall's 9-card row plus padding doesn't reflow
     * below that, and going wider here adds dead pixels on the side
     * that visually cover the host GUI for no benefit.
     */
    static final int MIN_SIDEBAR_WIDTH = 240;

    private static boolean registered;
    private static final List<SidebarHostResolver> SIDEBAR_HOST_RESOLVERS = new CopyOnWriteArrayList<>();
    private static Screen activeHostScreen;
    private static int activeSidebarWidth;
    private static boolean activeHostSlotBridge;

    private SlotContainerSidebar() {
    }

    @FunctionalInterface
    public interface SidebarHostResolver {
        SidebarHost resolve(Screen screen);
    }

    public record SidebarHost(
            Screen renderScreen,
            AbstractContainerScreen<?> menuScreen,
            boolean hostSlotBridge
    ) {
        public SidebarHost {
            if (renderScreen == null || menuScreen == null) {
                throw new IllegalArgumentException("sidebar host screens must be non-null");
            }
        }

        public AbstractContainerMenu menu() {
            return menuScreen.getMenu();
        }
    }

    /**
     * Currently-mounted host screen, or {@code null} when no SLOT sidebar
     * is active. Read by EMI / JEI compat plugins to publish exclusion
     * bounds only when the sidebar is actually live on a host.
     */
    public static Screen activeHostScreen() {
        return activeHostScreen;
    }

    /**
     * Width (in screen px) of the active sidebar mount, or {@code 0}
     * when no sidebar is mounted. Bounds for compat consumers run from
     * {@code (0, 0)} to {@code (activeSidebarWidth(), screen.height)}.
     */
    public static int activeSidebarWidth() {
        return activeHostScreen == null ? 0 : activeSidebarWidth;
    }

    public record ScreenBounds(int x, int y, int width, int height) {
    }

    public static ScreenBounds activeCraftRunPanelBounds(Screen screen) {
        if (screen == null || activeHostScreen != screen || !SlotSidebarClientUi.craftRunPanelVisible()) {
            return null;
        }
        int width = CraftRunUiBuilder.PANEL_WIDTH_PX;
        int height = Math.max(
                1,
                screen.height
                        - SlotClientConfig.CLIENT.craftRunTopMargin.get()
                        - SlotClientConfig.CLIENT.craftRunBottomMargin.get());
        return new ScreenBounds(
                Math.max(0, screen.width - SlotClientConfig.CLIENT.craftRunRightMargin.get() - width),
                SlotClientConfig.CLIENT.craftRunTopMargin.get(),
                width,
                height);
    }

    public static ScreenBounds activeRecentsPanelBounds(Screen screen) {
        if (screen == null || activeHostScreen != screen) {
            return null;
        }
        return new ScreenBounds(
                RecentsStripUiBuilder.floatingLeft(
                        screen.width,
                        SlotClientConfig.CLIENT.recentsHorizontalOffset.get()),
                RecentsStripUiBuilder.floatingTop(SlotClientConfig.CLIENT.recentsTopOffset.get()),
                RecentsStripUiBuilder.STRIP_WIDTH_PX,
                RecentsStripUiBuilder.STRIP_HEIGHT_PX);
    }

    public static void registerSidebarHostResolver(SidebarHostResolver resolver) {
        if (resolver != null && !SIDEBAR_HOST_RESOLVERS.contains(resolver)) {
            SIDEBAR_HOST_RESOLVERS.add(resolver);
        }
    }

    public static void setRecipeSidebarSpec(Screen screen, RecipeIngredientSidebarSpec spec) {
        if (activeHostScreen == screen) {
            SlotSidebarClientUi.setRecipeSidebarSpec(spec);
        }
    }

    public static void setCraftRunRecipeCaptures(Screen screen, List<CraftRunRecipeCapture> captures) {
        if (activeHostScreen == screen) {
            SlotSidebarClientUi.setCraftRunRecipeCaptures(captures);
        }
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onScreenInit);
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onScreenClosing);
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onMouseReleased);
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onCharTyped);
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onKeyPressed);
        registered = true;
    }

    /**
     * Width (in screen px) the sidebar content will occupy. Defers to the
     * sidebar UI's preferred content size (wall + capped left column
     * + padding) so the sidebar consumes only the space its content
     * needs.
     */
    static int sidebarContentWidthFor() {
        return Math.max(MIN_SIDEBAR_WIDTH, SlotSidebarClientUi.preferredSidebarWidth());
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        // Honor the global escape hatch — if SLOT is disabled in the
        // client config, do not mount anything anywhere.
        if (!SlotClientConfig.CLIENT.slotEnabled.get()) {
            return;
        }
        SidebarHost host = sidebarHost(event.getScreen());
        if (host == null) {
            return;
        }
        Screen renderScreen = host.renderScreen();
        String className = renderScreen.getClass().getName();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        // If the previous host screen's Closing hook didn't fire (e.g.
        // direct screen swap), tell the server to drop any lingering
        // sidebar handle before we open a fresh one.
        if (activeHostScreen != null) {
            PacketDistributor.sendToServer(new SlotSidebarClosePayload());
            activeHostScreen = null;
            activeSidebarWidth = 0;
            activeHostSlotBridge = false;
        }

        int sidebarContentWidth = sidebarContentWidthFor();
        // The host stays in its native centered position. Earlier
        // iterations tried to shift it right (mutate leftPos / re-init
        // with a virtual screen width) so the SLOT sidebar wouldn't
        // overlap it, but every approach broke modded screens that
        // attach side widgets in init (Sophisticated Backpacks upgrade
        // tabs ended up scattered across the workspace). Living with
        // overlap in sidebar mode is the lesser evil; reworking the
        // SLOT UI to fit beside an unshifted host is tracked as a
        // separate design pass.
        boolean mounted = SlotSidebarClientUi.mount(
                minecraft.player,
                renderScreen,
                host.menu(),
                renderScreen.width,
                renderScreen.height);
        if (!mounted) {
            return;
        }
        activeHostScreen = renderScreen;
        activeSidebarWidth = SlotClientConfig.CLIENT.sidebarLeftMargin.get() + sidebarContentWidth;
        activeHostSlotBridge = host.hostSlotBridge();

        PacketDistributor.sendToServer(new SlotSidebarOpenPayload());

        if (SlotDebugLog.enabled()) {
            SlotCommon.LOGGER.info(
                    "[SLOT][sidebar] mounted on {} (sidebarWidth={}, screen={}x{})",
                    className, activeSidebarWidth, renderScreen.width, renderScreen.height
            );
        }
    }

    private static SidebarHost sidebarHost(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            if (!canMountOnContainer(containerScreen)) {
                return null;
            }
            return new SidebarHost(containerScreen, containerScreen, true);
        }
        for (SidebarHostResolver resolver : SIDEBAR_HOST_RESOLVERS) {
            SidebarHost host = resolver.resolve(screen);
            if (host != null && canUseBackingContainer(host.menuScreen())) {
                return host;
            }
        }
        return null;
    }

    private static boolean canMountOnContainer(AbstractContainerScreen<?> screen) {
        if (!canUseBackingContainer(screen)) {
            return false;
        }
        // Vanilla {@link InventoryScreen} only ever shows up as the
        // bypass-to-vanilla escape hatch from the workspace's "open
        // vanilla inventory" button (or when SLOT is disabled below).
        // Mounting a sidebar on it defeats the purpose — and the
        // sidebar widget swallows the Esc keystroke before the host
        // screen sees it, leaving the player unable to dismiss the
        // screen short of killing the client.
        return !(screen instanceof InventoryScreen);
    }

    private static boolean canUseBackingContainer(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return false;
        }
        // The SLOT workspace screen is itself an AbstractContainerScreen
        // wrapper around LDLib2's ModularUI; never sidebar-mount inside
        // our own screen or any other LDLib2-driven workspace.
        String className = screen.getClass().getName();
        if (className.startsWith("dev.imagio.slot.")
                || className.startsWith("com.lowdragmc.lowdraglib2.")) {
            return false;
        }
        return true;
    }

    private static void onScreenClosing(ScreenEvent.Closing event) {
        if (activeHostScreen == null) {
            return;
        }
        if (event.getScreen() != activeHostScreen) {
            return;
        }
        Screen host = activeHostScreen;
        activeHostScreen = null;
        activeSidebarWidth = 0;
        activeHostSlotBridge = false;
        SlotSidebarClientUi.release();
        PacketDistributor.sendToServer(new SlotSidebarClosePayload());
        if (SlotDebugLog.enabled()) {
            SlotCommon.LOGGER.info("[SLOT][sidebar] released on {}", host.getClass().getName());
        }
    }

    /**
     * Cross-surface drag bridge: when the player drag-releases a wall
     * card outside the sidebar but over a vanilla menu slot, intercept
     * the release and route it through the SLOT cross-surface RPC
     * instead of letting vanilla treat it as a stray click.
     *
     * <p>{@code Pre} (not {@code Post}) so we run before
     * {@code AbstractContainerScreen.mouseReleased} processes the
     * release. {@link SlotSidebarClientUi#tryConsumeAtlasDragForHostSlot}
     * checks that an atlas-item drag is actually active and ends it on
     * the LDLib2 side; if it returns true we cancel the screen event
     * so vanilla's drop / drag-pickup logic skips the click.
     */
    private static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (activeHostScreen == null || event.getScreen() != activeHostScreen) {
            return;
        }
        if (!activeHostSlotBridge) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (!SlotSidebarClientUi.isActive()) {
            return;
        }
        net.minecraft.world.inventory.Slot slot = ((AbstractContainerScreenAccessor) screen)
                .slot$findSlot(event.getMouseX(), event.getMouseY());
        Minecraft minecraft = Minecraft.getInstance();
        boolean overPlayerSlot = slot != null
                && minecraft != null
                && minecraft.player != null
                && slot.container == minecraft.player.getInventory();
        boolean carrying = minecraft != null
                && minecraft.player != null
                && minecraft.player.containerMenu != null
                && !minecraft.player.containerMenu.getCarried().isEmpty();

        // Drop-to-world / double-click guard. When the sidebar is mounted
        // and the player has a non-empty cursor, vanilla's
        // {@code AbstractContainerScreen.mouseReleased} runs its own
        // {@code slotClicked} after ours — depending on what's under the
        // cursor that's either a drop-to-world ({@code slot==null} →
        // {@code -999}) or a re-pickup of what we just dropped onto a
        // player slot. Our LDLib click handlers already deposited the
        // cursor through the appropriate RPC, so cancel vanilla's
        // release entirely. We let release on a host-side slot
        // (chest / crafting / machine) through so vanilla's native
        // drop-on-slot keeps working there.
        if (carrying && (slot == null || overPlayerSlot)) {
            event.setCanceled(true);
            return;
        }

        if (event.getButton() != 0) {
            return;
        }
        if (slot == null) {
            return;
        }
        if (overPlayerSlot) {
            return;
        }
        boolean consumed = SlotSidebarClientUi.tryConsumeAtlasDragForHostSlot(
                slot.index, event.getMouseX(), event.getMouseY());
        if (consumed) {
            event.setCanceled(true);
        }
    }

    /**
     * Forward typed characters to the sidebar's widget tree. LDLib2's
     * {@code ScreenMixin.charTyped} is not present — only {@code keyPressed}
     * is mixed in, and even that fires only for the inventory key. So
     * {@code /} (search), digits (belt assign), and any other char input
     * never reach the sidebar when it's mounted on a non-LDLib2 host
     * screen. This listener bridges the gap by dispatching directly into
     * the modular UI's root.
     *
     * <p>Skipped while the host screen has a vanilla {@link EditBox}
     * focused (e.g. anvil rename) so the player's typing flows there
     * instead of getting stolen by SLOT.
     */
    private static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (activeHostScreen == null || event.getScreen() != activeHostScreen) {
            return;
        }
        if (event.getScreen().getFocused() instanceof net.minecraft.client.gui.components.EditBox) {
            return;
        }
        if (SlotSidebarClientUi.dispatchCharTyped(event.getCodePoint(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    /**
     * Forward key presses to the sidebar widget tree, same plumbing as
     * {@link #onCharTyped}. Required for the search modal's
     * Enter/Backspace/Backslash/Tab handlers, the 1–9 belt-assign hotkeys,
     * undo/redo, and the open-vanilla key — none of which fire while
     * the sidebar is mounted on a non-LDLib2 host screen because
     * LDLib2's mixin only forwards the inventory key.
     */
    private static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (activeHostScreen == null || event.getScreen() != activeHostScreen) {
            return;
        }
        if (event.getScreen().getFocused() instanceof net.minecraft.client.gui.components.EditBox) {
            return;
        }
        if (SlotSidebarClientUi.dispatchKeyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }
}
