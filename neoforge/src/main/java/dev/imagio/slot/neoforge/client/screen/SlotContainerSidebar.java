package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.SlotCommon;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Phase 3a entry point — when an {@link AbstractContainerScreen} other
 * than the SLOT workspace opens, mount the wall as a left sidebar so
 * the player can act on carried inventory without shuffling through
 * the vanilla 36-slot band. See {@code docs/plans/list-view.md} §
 * Phase 3.
 *
 * <p><strong>Status:</strong> hook point only. Mounting an LDLib2
 * widget tree inside a vanilla {@code AbstractContainerScreen} is
 * non-trivial — we either wrap the screen with a custom subclass
 * before the player sees it (Screen.Opening event), or mixin into
 * the render path. The sidebar also needs:
 *
 * <ul>
 *   <li>Cross-surface drag routing (wall card ↔ machine slot via the
 *       existing intent router and {@code largestCarriedSlot*} fields
 *       on each {@code AtlasItem}).</li>
 *   <li>Z-order management vs. EMI's right-edge real estate.</li>
 *   <li>Width policy: ratio (~1/3 viewport) with min/max clamps.</li>
 * </ul>
 *
 * <p>Phase 3b (hide vanilla 36-slot band) and Phase 3c
 * (mod-observer transparency: shuffle through invisible vanilla slots
 * for shift-click / hotkey-move so EMI / sorting / hotkey-transfer
 * mods keep working) are separate follow-on work each.
 */
public final class SlotContainerSidebar {
    private static boolean registered;

    private SlotContainerSidebar() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotContainerSidebar::onScreenInit);
        registered = true;
    }

    /**
     * Detect a container/machine screen open. The actual sidebar mount
     * is TODO (see class javadoc). For now we just diagnostic-log so
     * the integration surface exists and we can verify our event hook
     * fires for the right screen types during playtest.
     */
    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        // The SLOT workspace screen is itself an AbstractContainerScreen
        // wrapper around LDLib2's ModularUI; don't try to mount a sidebar
        // inside our own screen.
        String className = screen.getClass().getName();
        if (className.startsWith("dev.imagio.slot.")
                || className.startsWith("com.lowdragmc.lowdraglib2.")) {
            return;
        }
        if (SlotDebugLog.enabled()) {
            SlotCommon.LOGGER.info("[SLOT][sidebar] container screen opened: {} (sidebar mount TODO)", className);
        }
    }
}
