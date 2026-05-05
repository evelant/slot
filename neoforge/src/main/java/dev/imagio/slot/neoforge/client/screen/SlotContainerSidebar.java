package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.mixin.AbstractContainerScreenAccessor;
import dev.imagio.slot.neoforge.network.SlotSidebarClosePayload;
import dev.imagio.slot.neoforge.network.SlotSidebarOpenPayload;
import dev.imagio.slot.neoforge.screen.ldlib.SlotSidebarClientUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

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
 * which owns a per-player handle and ticks it from
 * {@code ServerTickEvent.Post}. This sidebar runs entirely outside
 * the vanilla menu lifecycle: the host menu (chest, crafting, machine)
 * remains the player's {@code containerMenu}.
 */
public final class SlotContainerSidebar {
    /** Min sidebar width in screen pixels — narrow enough to fit on a 720p screen, wide enough to host the wall. */
    static final int MIN_SIDEBAR_WIDTH = 320;
    /**
     * Pixel gap between the sidebar's right edge and the host GUI's
     * left edge. The host is anchored flush against this gap (not
     * centered in the remaining space) so the player doesn't see a big
     * empty corridor between the SLOT panel and the crafting / chest
     * UI; whatever right-side space is left over is where EMI / JEI
     * naturally live.
     */
    static final int HOST_GUI_GAP = 8;
    /**
     * Pixel space we keep clear on the right of the host GUI for
     * EMI / JEI / similar right-edge tooling. Sidebar width grows to
     * consume everything not claimed by the host or this reserve, so
     * on wide screens the sidebar can take well over half the screen
     * without crashing into the recipe panel.
     */
    static final int RIGHT_PANEL_RESERVE = 200;

    private static boolean registered;
    private static Screen activeHostScreen;
    private static int activeSidebarWidth;

    private SlotContainerSidebar() {
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

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onScreenInit);
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onScreenClosing);
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onMouseReleased);
        registered = true;
    }

    /**
     * Width (in screen px) the sidebar will occupy. Defers to the
     * sidebar UI's preferred content size (wall + capped left column
     * + padding) so the sidebar consumes only the space its content
     * needs — leaving the host GUI flush against the sidebar's right
     * edge with no dead strip between them. {@code screenWidth} +
     * {@code hostImageWidth} are accepted for the case where we'd
     * want to clamp on a very narrow screen, but in practice the
     * preferred width fits comfortably on every reasonable resolution.
     */
    static int sidebarWidthFor(int screenWidth, int hostImageWidth) {
        return Math.max(MIN_SIDEBAR_WIDTH, SlotSidebarClientUi.preferredSidebarWidth());
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        // The SLOT workspace screen is itself an AbstractContainerScreen
        // wrapper around LDLib2's ModularUI; never sidebar-mount inside
        // our own screen or any other LDLib2-driven workspace.
        String className = screen.getClass().getName();
        if (className.startsWith("dev.imagio.slot.")
                || className.startsWith("com.lowdragmc.lowdraglib2.")) {
            return;
        }
        // Honor the global escape hatch — if SLOT is disabled in the
        // client config, do not mount anything anywhere.
        if (!SlotClientConfig.CLIENT.slotEnabled.get()) {
            return;
        }
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
        }

        int hostImageWidth = screen.getXSize();
        int sidebarWidth = sidebarWidthFor(screen.width, hostImageWidth);

        // Push the host into the right strip BEFORE mounting our sidebar:
        // re-init it with a virtual screen width that, when centered by
        // AbstractContainerScreen.init(), lands leftPos at sidebarWidth +
        // HOST_GUI_GAP. This way any side widgets the host attaches in
        // init() (Sophisticated Backpacks upgrade tabs, Curios slots,
        // etc.) compute their positions from the post-shift leftPos
        // instead of the centered default, so they don't end up floating
        // inside the SLOT sidebar's space. After init we restore the
        // real screen width — leftPos is already where we want it and
        // the sidebar widget tree uses the full viewport for its own
        // bounds. Skipped on screens too narrow to fit the host without
        // overlap, where we fall back to the simple leftPos shift.
        int gap = HOST_GUI_GAP;
        int virtualScreenWidth = hostImageWidth + 2 * (sidebarWidth + gap);
        if (virtualScreenWidth <= screen.width) {
            int originalScreenWidth = screen.width;
            try {
                screen.resize(minecraft, virtualScreenWidth, screen.height);
            } finally {
                screen.width = originalScreenWidth;
            }
        } else {
            ((AbstractContainerScreenAccessor) screen).slot$setLeftPos(sidebarWidth + gap);
        }

        boolean mounted = SlotSidebarClientUi.mount(minecraft.player, screen, sidebarWidth, screen.height);
        if (!mounted) {
            return;
        }
        activeHostScreen = screen;
        activeSidebarWidth = sidebarWidth;

        PacketDistributor.sendToServer(new SlotSidebarOpenPayload());

        if (SlotDebugLog.enabled()) {
            SlotCommon.LOGGER.info(
                    "[SLOT][sidebar] mounted on {} (sidebarWidth={}, screen={}x{})",
                    className, sidebarWidth, screen.width, screen.height
            );
        }
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
}
