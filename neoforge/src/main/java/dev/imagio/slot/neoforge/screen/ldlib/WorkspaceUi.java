package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.compactCount;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.ACCENT;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.FONT_UI;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.GHOST_ICON_ALPHA_TINT;
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
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

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
        // Keep the button's text from absorbing hover hit-tests. Without
        // this the cursor crossing onto the inner text element fires
        // MOUSE_LEAVE on the button, which breaks hover-driven previews
        // (e.g., the deposit-preview outline that lights up depositable
        // identities while hovering the Deposit button).
        button.text.setAllowHitTest(false);
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
        return itemIcon(stack, size, carried, true);
    }

    /**
     * @param renderVanillaCount when {@code false} the stack's count is
     *     coerced to 1 before the icon is built so vanilla's
     *     {@code renderItemDecorations} skips drawing the count badge.
     *     Atlas cards opt out so they can render their own status-aware
     *     "M / N" badge — the vanilla badge would show the first stack's
     *     count rather than the per-identity aggregate, which is wrong
     *     for an aggregated atlas item.
     */
    static UIElement itemIcon(ItemStack stack, float size, boolean carried, boolean renderVanillaCount) {
        ItemStack iconStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!renderVanillaCount && !iconStack.isEmpty()) {
            iconStack.setCount(1);
        }
        // Ghost stacks use GhostItemTexture so the alpha tint actually
        // blends for 3D blocks too — stock ItemStackTexture's tint hits
        // entityCutout for blocks, which is alpha-test only and ignores
        // the alpha. GhostItemTexture rewrites cutoutBlockSheet to
        // translucentItemSheet at vertex-write time so blocks land on
        // the alpha-blended path. Carried stacks keep using the stock
        // texture — no tint, no rewrite, vanilla appearance.
        ItemStackTexture texture = carried
                ? new ItemStackTexture(iconStack)
                : new GhostItemTexture(iconStack).setColor(GHOST_ICON_ALPHA_TINT);
        UIElement icon = new UIElement().layout(layout -> layout.width(size).height(size))
                .style(style -> style.backgroundTexture(texture));
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

    /**
     * Primitive 1: a square cell of {@code size} world units filled with
     * {@code fillColor}. Hit-testing is left at the UIElement default — a
     * caller that wraps this in a Button (atlas card slot preview) will
     * disable hit testing on the cell itself, while a caller using the
     * cell as the click surface (chest tile cell) leaves it enabled.
     *
     * <p>This is the foundation primitive — the same square that every
     * inventory slot, hotbar slot, kit slot, and chest slot has been
     * open-coding. Centralising it gives a single place to change the
     * size/padding conventions if we ever standardise on a different
     * frame style.
     */
    static UIElement itemSlotShell(float size, int fillColor) {
        return panel(fillColor).layout(layout -> layout.width(size).height(size));
    }

    /**
     * Primitive 2: a complete item-display cell — shell + centered icon
     * (via {@link #itemIcon}) + optional count badge in the bottom-right
     * corner.
     *
     * <p>Caller wraps this in their own button or interactive element to
     * attach behavior (click, drag, context menu). Decorations beyond
     * count (selection outline, kit-slot ready pip, hotbar index badge)
     * stay with the caller — they're surface-specific and don't belong
     * inside a generic primitive.
     *
     * @param stack the stack to render; null/empty draws an empty shell
     * @param size cell width/height in world units
     * @param fillColor cell background ARGB
     * @param activeIcon true → icon at full opacity; false → ghost overlay
     * @param countBadge null or ≤1 → no badge; >1 → badge with the count
     */
    static UIElement itemSlotCard(
            ItemStack stack,
            float size,
            int fillColor,
            boolean activeIcon,
            Integer countBadge
    ) {
        UIElement card = itemSlotShell(size, fillColor);
        if (stack != null && !stack.isEmpty()) {
            float iconSize = Math.max(8f, size - 2f);
            UIElement icon = itemIcon(stack, iconSize, activeIcon);
            icon.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(centeredWorld(size, iconSize))
                    .top(centeredWorld(size, iconSize)));
            card.addChild(icon);
        }
        if (countBadge != null && countBadge > 1) {
            Label badge = label(compactCount(countBadge), ACCENT);
            badge.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .right(1)
                    .bottom(0)
                    .height(6));
            badge.textStyle(style -> style
                    .textColor(ACCENT)
                    .fontSize(6)
                    .textShadow(true)
                    .textAlignHorizontal(Horizontal.RIGHT)
                    .textAlignVertical(Vertical.BOTTOM));
            card.addChild(badge);
        }
        return card;
    }

    static UIElement itemSlotCard(ItemStack stack, float size, int fillColor, boolean activeIcon) {
        return itemSlotCard(stack, size, fillColor, activeIcon, null);
    }

    /**
     * Primitive 3: install the standard vanilla-tooltip handler on the
     * element. The supplier is re-invoked on every hover, so callers
     * whose stack changes between renders don't have to reinstall.
     */
    static void installItemTooltip(UIElement element, ItemStack stack) {
        installItemTooltip(element, () -> stack);
    }

    static void installItemTooltip(UIElement element, Supplier<ItemStack> stackSupplier) {
        if (element == null || stackSupplier == null) {
            return;
        }
        element.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            ItemStack stack = stackSupplier.get();
            if (stack == null || stack.isEmpty()) {
                return;
            }
            event.hoverTooltips = new HoverTooltips(
                    List.copyOf(DrawerHelper.getItemToolTip(stack)),
                    stack.getTooltipImage().orElse(null),
                    null,
                    stack
            );
        });
    }
}
