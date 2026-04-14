package dev.imagio.slot.client.screen.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface DockedToolPanel {
    Component title();

    int preferredHeight();

    void layout(int x, int y, int width);

    void containerTick();

    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    boolean isFocused();

    default boolean contains(double mouseX, double mouseY) {
        return false;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseReleased(double mouseX, double mouseY, int button);

    boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);

    boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean charTyped(char codePoint, int modifiers);

    default boolean consumeRefreshRequested() {
        return false;
    }
}
