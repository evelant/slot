package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.client.ForgeWorkspaceClient;
import dev.imagio.slot.forge.client.ForgeContainerSidebar;
import dev.imagio.slot.forge.ui.ForgeWorkspaceScreen;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.ui.workspace.GoalWorkspaceIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
    private static boolean recipeGoalButtonEventsRegistered;

    @Override
    public void register(EmiRegistry registry) {
        registerRecipeGoalButtonEvents();
        GoalWorkspaceIntegration.registerDelegate(new GoalWorkspaceIntegration.Delegate() {
            @Override
            public boolean openRecipe(GoalDescriptor goal) {
                return SlotForgeEmiGoalAdapter.openRecipe(goal);
            }

            @Override
            public boolean openUses(ItemIdentity identity) {
                return SlotForgeEmiGoalAdapter.openUses(identity);
            }

            @Override
            public boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
                return SlotForgeEmiGoalAdapter.openChoiceEditor(goal, choiceGroupId);
            }

            @Override
            public boolean openWorkspace() {
                ForgeWorkspaceClient.openWorkspaceScreen();
                return true;
            }
        });
        registry.addRecipeDecorator((recipe, widgets) ->
                SlotForgeEmiGoalAdapter.decorateRecipe(recipe, widgets, ForgeWorkspaceClient::openWorkspaceScreen));
        registry.addGenericDragDropHandler(new EmiDragDropHandler<>() {
            @Override
            public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
                Bounds bounds = goalDropBounds(screen);
                if (!bounds.contains(x, y)) {
                    return false;
                }
                Runnable openWorkspace = shouldOpenWorkspaceAfterDrop(screen)
                        ? ForgeWorkspaceClient::openWorkspaceScreen
                        : null;
                SlotForgeEmiGoalAdapter.createGoalFromIngredient(stack, openWorkspace);
                return true;
            }

            @Override
            public void render(Screen screen, EmiIngredient dragged, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                Bounds bounds = goalDropBounds(screen);
                boolean hovered = bounds.contains(mouseX, mouseY);
                graphics.fill(
                        bounds.x(),
                        bounds.y(),
                        bounds.x() + bounds.width(),
                        bounds.y() + bounds.height(),
                        hovered ? 0xAA2FB56D : 0x662FB56D);
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.literal("SLOT goal"),
                        bounds.x() + 6,
                        bounds.y() + 7,
                        0xFFFFFFFF,
                        true);
            }
        });
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
        SlotCommon.LOGGER.info("[SLOT][emi] registered Forge SLOT exclusion area provider and goal adapter");
    }

    private static void registerRecipeGoalButtonEvents() {
        if (recipeGoalButtonEventsRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(SlotForgeEmiPlugin::onRecipeScreenRender);
        MinecraftForge.EVENT_BUS.addListener(SlotForgeEmiPlugin::onRecipeScreenMousePressed);
        recipeGoalButtonEventsRegistered = true;
    }

    private static void onRecipeScreenRender(ScreenEvent.Render.Post event) {
        SlotForgeEmiGoalAdapter.renderRecipeGoalButtons(
                event.getScreen(),
                event.getGuiGraphics(),
                event.getMouseX(),
                event.getMouseY());
    }

    private static void onRecipeScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (SlotForgeEmiGoalAdapter.handleRecipeGoalButtonClick(
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton(),
                ForgeWorkspaceClient::openWorkspaceScreen)) {
            event.setCanceled(true);
        }
    }

    private static Bounds goalDropBounds(Screen screen) {
        int sidebarWidth = sidebarWidthFor(screen);
        if (sidebarWidth > 0) {
            return new Bounds(0, 0, sidebarWidth, screen.height);
        }
        return new Bounds(8, 8, 96, 22);
    }

    private static boolean shouldOpenWorkspaceAfterDrop(Screen screen) {
        return sidebarWidthFor(screen) <= 0 && !isSlotStandaloneScreen(screen);
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
