package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.compat.emi.SlotForgeEmiHoverStackReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

final class ForgeHoveredItemHotkeySupport {
    private ForgeHoveredItemHotkeySupport() {
    }

    static ItemStack hoveredStack(Screen screen) {
        ItemStack emi = emiHoveredStack();
        if (emi != null && !emi.isEmpty()) {
            return focusedInputShouldKeepKey(screen) ? ItemStack.EMPTY : emi;
        }
        if (textInputOrEmiSearchFocused(screen)) {
            return ItemStack.EMPTY;
        }
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return ItemStack.EMPTY;
        }
        Slot slot = hoveredVanillaSlot(containerScreen, guiMouseX(), guiMouseY());
        return slot == null || !slot.hasItem() ? ItemStack.EMPTY : slot.getItem().copy();
    }

    static boolean isSlotOwnedScreen(Screen screen) {
        return screen != null && screen.getClass().getName().startsWith("dev.imagio.slot.");
    }

    private static boolean focusedInputShouldKeepKey(Screen screen) {
        if (isEmiRecipeStackScreen(screen)) {
            return false;
        }
        return textInputOrEmiSearchFocused(screen);
    }

    private static boolean textInputOrEmiSearchFocused(Screen screen) {
        return screen != null
                && (ForgeContainerSidebar.isTextInputFocused(screen.getFocused()) || emiSearchFocused());
    }

    private static ItemStack emiHoveredStack() {
        if (!ModList.get().isLoaded("emi")) {
            return ItemStack.EMPTY;
        }
        try {
            return SlotForgeEmiHoverStackReader.hoveredItemStack();
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static boolean emiSearchFocused() {
        if (!ModList.get().isLoaded("emi")) {
            return false;
        }
        try {
            return SlotForgeEmiHoverStackReader.isSearchFocused();
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
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

    private static boolean isEmiRecipeStackScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        String className = screen.getClass().getName();
        return "dev.emi.emi.screen.RecipeScreen".equals(className)
                || "dev.emi.emi.screen.BoMScreen".equals(className);
    }
}
