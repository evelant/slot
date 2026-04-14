package dev.imagio.slot.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SlotTooltipRenderer {
    private static final int CURSOR_GAP_X = 18;
    private static final int CURSOR_GAP_Y = 18;
    private static final int EDGE_PADDING = 12;

    private SlotTooltipRenderer() {
    }

    public static void renderItemTooltip(GuiGraphics guiGraphics, Font font, ItemStack stack, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || stack == null || stack.isEmpty()) {
            return;
        }

        int anchorX = tooltipAnchorX(mouseX, screenWidth);
        int anchorY = tooltipAnchorY(mouseY, screenHeight);
        guiGraphics.renderTooltip(font, Screen.getTooltipFromItem(minecraft, stack), stack.getTooltipImage(), anchorX, anchorY);
    }

    public static void renderTextTooltip(GuiGraphics guiGraphics, Font font, List<Component> lines, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        int anchorX = tooltipAnchorX(mouseX, screenWidth);
        int anchorY = tooltipAnchorY(mouseY, screenHeight);
        guiGraphics.renderTooltip(font, lines, java.util.Optional.empty(), anchorX, anchorY);
    }

    private static int tooltipAnchorX(int mouseX, int screenWidth) {
        int preferredX = mouseX > (screenWidth * 2) / 3
                ? mouseX - 28
                : mouseX + CURSOR_GAP_X;
        return Math.max(EDGE_PADDING, Math.min(screenWidth - EDGE_PADDING, preferredX));
    }

    private static int tooltipAnchorY(int mouseY, int screenHeight) {
        if (mouseY > (screenHeight * 2) / 3) {
            return Math.max(EDGE_PADDING, mouseY - 42);
        }
        return Math.min(screenHeight - EDGE_PADDING, mouseY + 10);
    }
}
