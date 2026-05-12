package dev.imagio.slot.neoforge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalProjectionEntry;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.network.SlotGoalPlanPayload;
import dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController;
import dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar;
import dev.imagio.slot.ui.workspace.GoalWorkspaceClientState;
import dev.imagio.slot.ui.workspace.GoalWorkspaceIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
    private static boolean recipeGoalButtonEventsRegistered;

    @Override
    public void register(EmiRegistry registry) {
        registerRecipeGoalButtonEvents();
        GoalWorkspaceIntegration.registerDelegate(new GoalWorkspaceIntegration.Delegate() {
            @Override
            public boolean openRecipe(GoalDescriptor goal) {
                return SlotEmiGoalAdapter.openRecipe(goal);
            }

            @Override
            public boolean openRecipe(GoalDescriptor goal, GoalProjectionEntry entry) {
                return SlotEmiGoalAdapter.openRecipe(goal, entry);
            }

            @Override
            public boolean openUses(ItemIdentity identity) {
                return SlotEmiGoalAdapter.openUses(identity);
            }

            @Override
            public boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
                return SlotEmiGoalAdapter.openChoiceEditor(goal, choiceGroupId);
            }

            @Override
            public boolean openChoiceEditor(GoalDescriptor goal, GoalProjectionEntry entry) {
                return SlotEmiGoalAdapter.openChoiceEditor(goal, entry);
            }

            @Override
            public boolean openWorkspace() {
                SlotWorkspaceMountController.openSlotWorkspace();
                return true;
            }

            @Override
            public boolean persistGoal(GoalWorkspaceClientState.GoalTab goal) {
                PacketDistributor.sendToServer(SlotGoalPlanPayload.save(GoalWorkspaceClientState.planState(goal)));
                return true;
            }

            @Override
            public boolean removePersistedGoal(String goalId) {
                PacketDistributor.sendToServer(SlotGoalPlanPayload.remove(goalId));
                return true;
            }

            @Override
            public GoalDescriptor enrichVisibleAlternatives(GoalDescriptor goal, SlotWorkspaceViewModel source) {
                return SlotEmiGoalAdapter.enrichVisibleAlternatives(goal, source);
            }
        });
        registry.addRecipeDecorator((recipe, widgets) ->
                SlotEmiGoalAdapter.decorateRecipe(recipe, widgets, SlotWorkspaceMountController::openSlotWorkspace));
        registry.addGenericDragDropHandler(new EmiDragDropHandler<>() {
            @Override
            public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
                Bounds bounds = goalDropBounds(screen);
                if (!bounds.contains(x, y)) {
                    return false;
                }
                Runnable openWorkspace = shouldOpenWorkspaceAfterDrop(screen)
                        ? SlotWorkspaceMountController::openSlotWorkspace
                        : null;
                SlotEmiGoalAdapter.createGoalFromIngredient(stack, openWorkspace);
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
        SlotCommon.LOGGER.info("[SLOT][emi] registered SLOT exclusion area provider and goal adapter");
    }

    private static void registerRecipeGoalButtonEvents() {
        if (recipeGoalButtonEventsRegistered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotEmiPlugin::onRecipeScreenRender);
        NeoForge.EVENT_BUS.addListener(SlotEmiPlugin::onRecipeScreenMousePressed);
        recipeGoalButtonEventsRegistered = true;
    }

    private static void onRecipeScreenRender(ScreenEvent.Render.Post event) {
        SlotEmiGoalAdapter.renderRecipeGoalButtons(
                event.getScreen(),
                event.getGuiGraphics(),
                event.getMouseX(),
                event.getMouseY());
    }

    private static void onRecipeScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (SlotEmiGoalAdapter.handleRecipeGoalButtonClick(
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton(),
                SlotWorkspaceMountController::openSlotWorkspace)) {
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
