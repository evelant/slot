package dev.imagio.slot.neoforge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings;
import dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.network.SlotCraftRunRecipePayload;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import dev.imagio.slot.ui.workspace.RecipeViewerIntegration;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Tells EMI which parts of the screen SLOT is rendering so EMI's panel
 * (item grid) doesn't draw on top of the sidebar or the standalone
 * workspace. Two cases:
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
 * <p><b>Known limitation</b>: when {@code EmiConfig.centerSearchBar}
 * is on (EMI's default), EMI hardcodes its search field at
 * {@code (screen.width - 160) / 2, screen.height - 21} regardless of
 * exclusion areas (see {@code EmiScreenManager.addWidgets}), so the
 * field still overlaps the SLOT belt. Disable "Center Search Bar" in
 * EMI's in-game config to push the field into EMI's right sidebar
 * panel, which is properly clipped against our exclusion bounds.
 *
 * <p>The plugin compiles against EMI's API jar (declared {@code compileOnly}
 * in {@code neoforge/build.gradle}). At runtime EMI scans for
 * {@link EmiEntrypoint} via NeoForge's mod-file scan; if EMI isn't
 * installed nothing references this class and the classloader never
 * loads it, so no soft-dep guarding is needed.
 */
@EmiEntrypoint
public final class SlotEmiPlugin implements EmiPlugin {
    private static boolean recipeScreenEventsRegistered;

    @Override
    public void register(EmiRegistry registry) {
        SlotContainerSidebar.registerSidebarHostResolver(SlotEmiRecipeSidebarAdapter::sidebarHost);
        registerRecipeScreenEvents();
        RecipeViewerIntegration.registerDelegate(new RecipeViewerIntegration.Delegate() {
            @Override
            public boolean openRecipe(ItemIdentity identity) {
                return SlotEmiRecipeViewerAdapter.openRecipe(identity);
            }

            @Override
            public boolean openUses(ItemIdentity identity) {
                return SlotEmiRecipeViewerAdapter.openUses(identity);
            }
        });
        registry.addGenericExclusionArea((screen, consumer) -> {
            int sidebarWidth = sidebarWidthFor(screen);
            if (sidebarWidth > 0) {
                consumer.accept(new Bounds(0, 0, sidebarWidth, screen.height));
                SlotContainerSidebar.ScreenBounds craftRun = SlotContainerSidebar.activeCraftRunPanelBounds(screen);
                if (craftRun != null) {
                    consumer.accept(new Bounds(craftRun.x(), craftRun.y(), craftRun.width(), craftRun.height()));
                }
                Bounds recents = recentsExclusionBounds(screen);
                if (recents != null) {
                    consumer.accept(recents);
                }
                return;
            }
            Bounds recents = recentsExclusionBounds(screen);
            if (recents != null) {
                consumer.accept(recents);
            }
            if (isSlotStandaloneScreen(screen)) {
                consumer.accept(new Bounds(0, 0, screen.width, screen.height));
            }
        });
        SlotCommon.LOGGER.info("[SLOT][emi] registered SLOT exclusion area provider and craft-run recipe capture");
    }

    private static void registerRecipeScreenEvents() {
        if (recipeScreenEventsRegistered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotEmiPlugin::onRecipeScreenRender);
        NeoForge.EVENT_BUS.addListener(SlotEmiPlugin::onRecipeScreenInit);
        NeoForge.EVENT_BUS.addListener(SlotEmiPlugin::onRecipeScreenKeyPressed);
        recipeScreenEventsRegistered = true;
    }

    private static void onRecipeScreenInit(ScreenEvent.Init.Post event) {
        Bounds recents = recentsExclusionBounds(event.getScreen());
        if (recents != null) {
            SlotEmiRecipeSidebarAdapter.constrainRecipeScreenBelowRecents(
                    event.getScreen(),
                    recents.y() + recents.height());
        }
    }

    private static void onRecipeScreenRender(ScreenEvent.Render.Post event) {
        SlotContainerSidebar.setRecipeSidebarSpec(
                event.getScreen(),
                SlotEmiRecipeSidebarAdapter.recipeSidebarSpec(event.getScreen()));
        SlotContainerSidebar.setCraftRunRecipeCaptures(
                event.getScreen(),
                SlotEmiRecipeSidebarAdapter.craftRunRecipeCaptures(event.getScreen()));
    }

    private static void onRecipeScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!SlotAtlasKeyMappings.matchesAddVisibleEmiRecipe(event.getKeyCode(), event.getScanCode())) {
            return;
        }
        CraftRunRecipeCapture capture = SlotEmiRecipeSidebarAdapter.hoveredCraftRunRecipeCapture(event.getScreen());
        if (capture == null || !capture.active()) {
            return;
        }
        PacketDistributor.sendToServer(SlotCraftRunRecipePayload.add(capture));
        event.setCanceled(true);
    }

    private static int sidebarWidthFor(Screen screen) {
        return SlotContainerSidebar.activeHostScreen() == screen
                ? SlotContainerSidebar.activeSidebarWidth()
                : 0;
    }

    private static Bounds recentsExclusionBounds(Screen screen) {
        if (screen == null) {
            return null;
        }
        if (!SlotClientConfig.CLIENT.slotEnabled.get()) {
            return null;
        }
        SlotContainerSidebar.ScreenBounds active = SlotContainerSidebar.activeRecentsPanelBounds(screen);
        if (active != null) {
            return new Bounds(active.x(), active.y(), active.width(), active.height());
        }
        if (!SlotEmiRecipeSidebarAdapter.hasSidebarHost(screen)) {
            return null;
        }
        return new Bounds(
                RecentsStripUiBuilder.floatingLeft(
                        screen.width,
                        SlotClientConfig.CLIENT.recentsHorizontalOffset.get()),
                RecentsStripUiBuilder.floatingTop(SlotClientConfig.CLIENT.recentsTopOffset.get()),
                RecentsStripUiBuilder.STRIP_WIDTH_PX,
                RecentsStripUiBuilder.STRIP_HEIGHT_PX);
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
