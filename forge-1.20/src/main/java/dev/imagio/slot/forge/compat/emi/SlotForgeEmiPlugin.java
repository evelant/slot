package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.client.ForgeContainerSidebar;
import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Publishes SLOT's Forge-rendered screen regions to EMI so EMI's panel
 * avoids drawing on top of the sidebar or standalone workspace.
 *
 * <p>The class is discovered by EMI through {@link EmiEntrypoint}. EMI is a
 * compile-only, optional dependency; if EMI is not installed, nothing loads
 * this class at runtime.
 */
@EmiEntrypoint
public final class SlotForgeEmiPlugin implements EmiPlugin {
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
        SlotCommon.LOGGER.info("[SLOT][emi] registered Forge SLOT exclusion area provider");
    }

    private static int sidebarWidthFor(Screen screen) {
        return ForgeContainerSidebar.activeHostScreen() == screen
                ? ForgeContainerSidebar.activeSidebarWidth()
                : 0;
    }

    private static boolean isSlotStandaloneScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        return screen instanceof ForgeWorkspaceScreen
                || screen.getClass().getName().startsWith("dev.imagio.slot.");
    }
}
