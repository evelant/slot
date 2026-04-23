package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.FONT_UI;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.GHOST_ICON_OVERLAY_COLOR;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.MUTED;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.PANEL_ALT;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ROW;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ROW_DIM;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ROW_HOVER;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.SELECTED;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.TEXT;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class WorkspaceUi {
    private WorkspaceUi() {
    }

    static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    static UIElement panel(int color) {
        return new UIElement().style(style -> style.backgroundTexture(rect(color)));
    }

    static Button button(String text, boolean active) {
        return button(text, active, active ? ROW : PANEL_ALT);
    }

    static Button button(String text, boolean active, int color) {
        Button button = new Button();
        button.setText(Component.literal(text));
        button.setActive(active);
        applyButtonColors(button, active, color);
        return button;
    }

    static void applyButtonColors(Button button, boolean active, int color) {
        button.buttonStyle(style -> {
            style.baseTexture(rect(color));
            style.hoverTexture(rect(active ? hoverColor(color) : color));
            style.pressedTexture(rect(active ? SELECTED : color));
        });
        button.textStyle(style -> style.font(FONT_UI).textColor(active ? TEXT : MUTED).textShadow(false).fontSize(8));
    }

    static int hoverColor(int color) {
        if (color == ROW_DIM) {
            return ROW;
        }
        int baseAlpha = (color >>> 24) & 0xFF;
        if (baseAlpha < 0x80) {
            // Dim / ghost cards: keep hover within the same alpha envelope so they
            // don't suddenly look as prominent as a full-opacity carried card.
            return (baseAlpha << 24) | (ROW_HOVER & 0x00FFFFFF);
        }
        return ROW_HOVER;
    }

    static Label label(String text, int color) {
        Label label = new Label();
        label.setText(Component.literal(text == null ? "" : text));
        label.textStyle(style -> style
                .font(FONT_UI)
                .textColor(color)
                .fontSize(8)
                .textShadow(false)
                .textAlignVertical(Vertical.CENTER)
                .textAlignHorizontal(Horizontal.LEFT));
        label.setAllowHitTest(false);
        return label;
    }

    static Label wrappedLabel(String text, int color) {
        Label label = label(text, color);
        label.layout(layout -> layout.widthPercent(100));
        label.textStyle(style -> style.textWrap(TextWrap.WRAP).textAlignVertical(Vertical.TOP));
        return label;
    }

    static ColorRectTexture rect(int color) {
        return new ColorRectTexture(color);
    }

    static UIElement itemIcon(ItemStack stack, float size) {
        return itemIcon(stack, size, true);
    }

    static UIElement itemIcon(ItemStack stack, float size, boolean carried) {
        ItemStack iconStack = stack == null ? ItemStack.EMPTY : stack.copy();
        ItemStackTexture texture = new ItemStackTexture(iconStack);
        UIElement icon = new UIElement().layout(layout -> layout.width(size).height(size))
                .style(style -> {
                    style.backgroundTexture(texture);
                    if (!carried) {
                        style.overlayTexture(rect(GHOST_ICON_OVERLAY_COLOR));
                    }
                });
        icon.setAllowHitTest(false);
        return icon;
    }

    static UIElement emptyIcon() {
        UIElement icon = panel(0x80323B44).layout(layout -> layout.width(16).height(16));
        icon.setAllowHitTest(false);
        return icon;
    }

    static Label anchorLabel(String text, int color, float fontSize) {
        Label label = label(text, color);
        label.textStyle(style -> style
                .fontSize(fontSize)
                .textAlignVertical(Vertical.CENTER)
                .textAlignHorizontal(Horizontal.LEFT));
        return label;
    }

    static float centeredWorld(float container, float child) {
        return Math.max(0f, (container - child) / 2f);
    }
}
