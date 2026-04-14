package dev.imagio.slot.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.ToIntFunction;

public final class PopupActionMenu {
    private static final int MIN_WIDTH = 120;
    private static final int ROW_HEIGHT = 12;
    private static final int PADDING = 4;

    private final int x;
    private final int y;
    private final int width;
    private final List<ActionMenuItem> actions;

    public PopupActionMenu(
            int anchorX,
            int anchorY,
            List<ActionMenuItem> actions,
            ToIntFunction<String> textWidth,
            int minX,
            int maxRight,
            int minY,
            int maxBottom
    ) {
        this.actions = List.copyOf(actions);

        int computedWidth = MIN_WIDTH;
        for (ActionMenuItem action : this.actions) {
            computedWidth = Math.max(computedWidth, textWidth.applyAsInt(action.label()) + (PADDING * 2));
        }
        this.width = computedWidth;
        this.x = Math.min(maxRight - computedWidth, Math.max(minX, anchorX));
        int height = height();
        this.y = Math.min(maxBottom - height, Math.max(minY, anchorY));
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, TextRenderer textRenderer) {
        int height = height();
        guiGraphics.fill(x, y, x + width, y + height, 0xE0101010);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xF0181818);
        for (int index = 0; index < actions.size(); index++) {
            int rowTop = y + 1 + (index * ROW_HEIGHT);
            if (containsRow(mouseX, mouseY, index)) {
                guiGraphics.fill(x + 1, rowTop, x + width - 1, rowTop + ROW_HEIGHT, 0x664B7F35);
            }
            String label = actions.get(index).label();
            textRenderer.draw(guiGraphics, label, x + PADDING, rowTop + 2, width - (PADDING * 2));
        }
    }

    public boolean contains(double mouseX, double mouseY) {
        int height = height();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean click(double mouseX, double mouseY) {
        for (int index = 0; index < actions.size(); index++) {
            if (containsRow(mouseX, mouseY, index)) {
                actions.get(index).handler().run();
                return true;
            }
        }
        return false;
    }

    private boolean containsRow(double mouseX, double mouseY, int rowIndex) {
        int rowTop = y + 1 + (rowIndex * ROW_HEIGHT);
        return mouseX >= x + 1 && mouseX <= x + width - 1 && mouseY >= rowTop && mouseY <= rowTop + ROW_HEIGHT;
    }

    private int height() {
        return actions.size() * ROW_HEIGHT + 2;
    }

    @FunctionalInterface
    public interface TextRenderer {
        void draw(GuiGraphics guiGraphics, String label, int x, int y, int maxWidth);
    }
}
