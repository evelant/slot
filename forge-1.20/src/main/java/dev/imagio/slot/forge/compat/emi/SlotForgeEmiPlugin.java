package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.client.ForgeContainerSidebar;
import dev.imagio.slot.forge.client.ForgeWorkspaceClient;
import dev.imagio.slot.forge.network.SlotForgeNetworking;
import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.forge.ui.ForgeWorkspaceSurface;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.ui.workspace.RecipeViewerIntegration;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;

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
    private static boolean recipeScreenEventsRegistered;

    @Override
    public void register(EmiRegistry registry) {
        ForgeContainerSidebar.registerSidebarHostResolver(SlotForgeEmiRecipeSidebarAdapter::sidebarHost);
        registerRecipeScreenEvents();
        RecipeViewerIntegration.registerDelegate(new RecipeViewerIntegration.Delegate() {
            @Override
            public boolean openRecipe(ItemIdentity identity) {
                return SlotForgeEmiRecipeViewerAdapter.openRecipe(identity);
            }

            @Override
            public boolean openUses(ItemIdentity identity) {
                return SlotForgeEmiRecipeViewerAdapter.openUses(identity);
            }
        });
        registry.addGenericExclusionArea((screen, consumer) -> {
            int sidebarWidth = sidebarWidthFor(screen);
            if (sidebarWidth > 0) {
                consumer.accept(new Bounds(0, 0, sidebarWidth, screen.height));
                ForgeWorkspaceSurface.CraftRunPanelBounds craftRun = ForgeContainerSidebar.activeCraftRunPanelBounds(screen);
                if (craftRun != null) {
                    consumer.accept(new Bounds(craftRun.x(), craftRun.y(), craftRun.width(), craftRun.height()));
                }
                ForgeWorkspaceSurface.RecentsPanelBounds recents = ForgeContainerSidebar.activeRecentsPanelBounds(screen);
                if (recents != null) {
                    consumer.accept(new Bounds(recents.x(), recents.y(), recents.width(), recents.height()));
                }
                return;
            }
            if (isSlotStandaloneScreen(screen)) {
                consumer.accept(new Bounds(0, 0, screen.width, screen.height));
            }
        });
        SlotCommon.LOGGER.info("[SLOT][emi] registered Forge SLOT exclusion area provider and craft-run recipe capture");
    }

    private static void registerRecipeScreenEvents() {
        if (recipeScreenEventsRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(SlotForgeEmiPlugin::onRecipeScreenRender);
        MinecraftForge.EVENT_BUS.addListener(SlotForgeEmiPlugin::onRecipeScreenKeyPressed);
        recipeScreenEventsRegistered = true;
    }

    private static void onRecipeScreenRender(ScreenEvent.Render.Post event) {
        ForgeContainerSidebar.setRecipeSidebarSpec(
                event.getScreen(),
                SlotForgeEmiRecipeSidebarAdapter.recipeSidebarSpec(event.getScreen()));
        ForgeContainerSidebar.setCraftRunRecipeCaptures(
                event.getScreen(),
                SlotForgeEmiRecipeSidebarAdapter.craftRunRecipeCaptures(event.getScreen()));
    }

    private static void onRecipeScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!ForgeWorkspaceClient.matchesAddVisibleEmiRecipe(event.getKeyCode(), event.getScanCode())) {
            return;
        }
        CraftRunRecipeCapture capture = SlotForgeEmiRecipeSidebarAdapter.hoveredCraftRunRecipeCapture(event.getScreen());
        if (capture == null || !capture.active()) {
            return;
        }
        if (SlotForgeNetworking.addCraftRunRecipe(capture)) {
            event.setCanceled(true);
        }
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
