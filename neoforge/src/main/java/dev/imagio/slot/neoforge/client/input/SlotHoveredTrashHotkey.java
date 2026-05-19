package dev.imagio.slot.neoforge.client.input;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.compat.emi.SlotEmiHoverStackReader;
import dev.imagio.slot.neoforge.network.SlotTrashIdentityPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Global GUI shortcut for trashing carried stacks matching the item under the
 * cursor. Mirrors the wanted hover path so EMI recipe widgets and vanilla
 * container slots resolve identities the same way.
 */
public final class SlotHoveredTrashHotkey {
    private static boolean registered;
    private static boolean pressed;

    private SlotHoveredTrashHotkey() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotHoveredTrashHotkey::onKeyPressed);
        NeoForge.EVENT_BUS.addListener(SlotHoveredTrashHotkey::onClientTick);
        registered = true;
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (!SlotAtlasKeyMappings.trashHoverDown()) {
            pressed = false;
        }
    }

    private static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!SlotAtlasKeyMappings.matchesTrashHover(event.getKeyCode(), event.getScanCode())) {
            return;
        }
        Screen screen = event.getScreen();
        if (screen == null || isSlotOwnedScreen(screen)) {
            return;
        }
        if (pressed) {
            event.setCanceled(true);
            return;
        }
        ItemStack stack = hoveredStack(screen);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
        PacketDistributor.sendToServer(new SlotTrashIdentityPayload(
                ref.itemId(),
                ref.comparisonMode(),
                ref.componentFingerprint()));
        pressed = true;
        event.setCanceled(true);
    }

    private static ItemStack hoveredStack(Screen screen) {
        ItemStack emi = emiHoveredStack();
        if (emi != null && !emi.isEmpty()) {
            if (focusedInputShouldKeepKey(screen)) {
                return ItemStack.EMPTY;
            }
            return emi;
        }
        if (isTextInputFocused(screen.getFocused()) || emiSearchFocused()) {
            return ItemStack.EMPTY;
        }
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return ItemStack.EMPTY;
        }
        Slot slot = hoveredVanillaSlot(containerScreen, guiMouseX(), guiMouseY());
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        return slot.getItem().copy();
    }

    private static Slot hoveredVanillaSlot(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        if (screen == null) {
            return null;
        }
        Slot current = screen.getSlotUnderMouse();
        if (slotContains(screen, current, mouseX, mouseY)) {
            return current;
        }
        for (Slot slot : screen.getMenu().slots) {
            if (slotContains(screen, slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private static boolean slotContains(AbstractContainerScreen<?> screen, Slot slot, double mouseX, double mouseY) {
        if (screen == null || slot == null || !slot.isActive()) {
            return false;
        }
        int x = screen.getGuiLeft() + slot.x;
        int y = screen.getGuiTop() + slot.y;
        return mouseX >= x && mouseX < x + 16
                && mouseY >= y && mouseY < y + 16;
    }

    private static boolean focusedInputShouldKeepKey(Screen screen) {
        if (isEmiRecipeStackScreen(screen)) {
            return false;
        }
        return isTextInputFocused(screen.getFocused()) || emiSearchFocused();
    }

    private static ItemStack emiHoveredStack() {
        if (!ModList.get().isLoaded("emi")) {
            return ItemStack.EMPTY;
        }
        try {
            return SlotEmiHoverStackReader.hoveredItemStack();
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static boolean emiSearchFocused() {
        if (!ModList.get().isLoaded("emi")) {
            return false;
        }
        try {
            return SlotEmiHoverStackReader.isSearchFocused();
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static double guiMouseX() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.mouseHandler == null) {
            return 0;
        }
        return minecraft.mouseHandler.xpos()
                * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
    }

    private static double guiMouseY() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.mouseHandler == null) {
            return 0;
        }
        return minecraft.mouseHandler.ypos()
                * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
    }

    private static boolean isSlotOwnedScreen(Screen screen) {
        String className = screen.getClass().getName();
        return className.startsWith("dev.imagio.slot.")
                || className.startsWith("com.lowdragmc.lowdraglib2.");
    }

    private static boolean isEmiRecipeStackScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        String className = screen.getClass().getName();
        return "dev.emi.emi.screen.RecipeScreen".equals(className)
                || "dev.emi.emi.screen.BoMScreen".equals(className);
    }

    private static boolean isTextInputFocused(GuiEventListener listener) {
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
}
