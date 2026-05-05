package dev.imagio.slot.neoforge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar;
import net.minecraft.client.gui.screens.Screen;

/**
 * Tells EMI which parts of the screen SLOT is rendering so EMI's panel
 * (item grid, search field) doesn't draw on top of the sidebar or the
 * standalone workspace. Without this, EMI's bottom search input collides
 * with the SLOT belt strip.
 *
 * <p>Two cases:
 * <ul>
 *   <li><b>Sidebar mount</b>: while the sidebar is active on a host
 *       screen, exclude the full vertical strip the sidebar occupies
 *       ({@code (0, 0)} → {@code (sidebarWidth, screen.height)}). The
 *       belt sits inside this strip so the same one rect covers it.</li>
 *   <li><b>Standalone workspace</b>: when the workspace is open as its
 *       own screen, exclude the entire screen — SLOT owns the surface
 *       and EMI has nowhere useful to draw anyway.</li>
 * </ul>
 *
 * <p>The plugin compiles against EMI's API jar (declared {@code compileOnly}
 * in {@code neoforge/build.gradle}). At runtime EMI scans for
 * {@link EmiEntrypoint} via NeoForge's mod-file scan; if EMI isn't
 * installed nothing references this class and the classloader never loads
 * it, so no soft-dep guarding is needed.
 */
@EmiEntrypoint
public final class SlotEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            int sidebarWidth = sidebarWidthFor(screen);
            if (sidebarWidth > 0) {
                consumer.accept(new Bounds(0, 0, sidebarWidth, screen.height));
                return;
            }
            if (isSlotStandaloneScreen(screen)) {
                consumer.accept(new Bounds(0, 0, screen.width, screen.height));
            }
        });
    }

    private static int sidebarWidthFor(Screen screen) {
        return SlotContainerSidebar.activeHostScreen() == screen
                ? SlotContainerSidebar.activeSidebarWidth()
                : 0;
    }

    /**
     * Standalone SLOT workspace screens are LDLib2-driven container
     * screens whose class lives under {@code dev.imagio.slot} or the
     * LDLib2 modular UI package. Mirrors the guard in
     * {@link SlotContainerSidebar} that prevents the sidebar from
     * mounting on its own surface.
     */
    private static boolean isSlotStandaloneScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        String className = screen.getClass().getName();
        return className.startsWith("dev.imagio.slot.")
                || className.startsWith("com.lowdragmc.lowdraglib2.");
    }
}
