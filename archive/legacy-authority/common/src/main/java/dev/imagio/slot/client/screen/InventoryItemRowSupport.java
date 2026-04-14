package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.client.gui.GuiGraphics;

public final class InventoryItemRowSupport {
    private final AbstractInventoryBrowserScreen<?> owner;

    public InventoryItemRowSupport(AbstractInventoryBrowserScreen<?> owner) {
        this.owner = owner;
    }

    public RowLayout renderRow(
            GuiGraphics guiGraphics,
            RowPresentation presentation,
            RowOptions options,
            int x,
            int y,
            int width
    ) {
        RowLayout layout = rowLayout(x, y, width, options);
        owner.renderScaledItem(guiGraphics, presentation.entry().displayStack(), layout.iconX(), owner.rowItemY(y, options.rowHeight(), options.itemSize()), options.itemScale());

        String leadingCountText = owner.paddedRowCountText(presentation.localCount());
        String countText = presentation.collectionTracked()
                ? presentation.localCount() + "/" + presentation.desiredCount()
                : Integer.toString(presentation.localCount());
        int countWidth = owner.scaledTextWidth(countText, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        int countX = layout.countColumnLeft() + Math.max(0, options.countColumnWidth() - countWidth);

        String secondaryText = presentation.secondaryText();
        if (!secondaryText.isEmpty()) {
            secondaryText = owner.textFont().plainSubstrByWidth(secondaryText, presentation.secondaryMaxWidth());
        }
        int secondaryWidth = secondaryText.isEmpty() ? 0 : owner.scaledTextWidth(secondaryText, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        int secondaryX = secondaryText.isEmpty() ? layout.actionX() : layout.actionX() - secondaryWidth - 10;

        int nameWidth = Math.max(options.nameMinWidth(), secondaryX - layout.nameX() - 6);
        String trimmedName = owner.textFont().plainSubstrByWidth(presentation.entry().displayName(), nameWidth);

        if (presentation.collectionTracked()) {
            owner.syncInlineDesiredCountBox(
                    layout.countColumnLeft() - 2,
                    y,
                    options.countColumnWidth() + 6,
                    presentation.collectionId(),
                    presentation.entry().itemEntry().identity()
            );
        }

        owner.drawScaledText(guiGraphics, leadingCountText, layout.leadingCountX(), owner.rowTextY(y), presentation.countTextColor(), AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        owner.drawScaledText(guiGraphics, trimmedName, layout.nameX(), owner.rowTextY(y), presentation.nameColor(), AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        if (!secondaryText.isEmpty()) {
            owner.drawScaledText(
                    guiGraphics,
                    secondaryText,
                    secondaryX,
                    owner.rowTextY(y),
                    presentation.equippedOnly() ? 0xE0C060 : 0xA0A0A0,
                    AbstractInventoryBrowserScreen.ROW_TEXT_SCALE
            );
        }
        owner.drawScaledText(guiGraphics, "...", layout.actionX(), owner.rowTextY(y), 0xA0A0A0, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        if (presentation.collectionTracked()
                && !owner.isInlineDesiredCountTarget(presentation.collectionId(), presentation.entry().itemEntry().identity())) {
            owner.drawScaledText(guiGraphics, countText, countX, owner.rowTextY(y), presentation.countTextColor(), AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        }
        return layout;
    }

    public RowLayout rowLayout(int rowLeft, int rowTop, int rowWidth, RowOptions options) {
        int iconX = rowLeft + 4;
        int countColumnLeft = rowLeft + rowWidth - options.countColumnWidth() - 4;
        int actionX = countColumnLeft - options.actionColumnWidth() - 8;
        int leadingCountX = iconX + options.itemSize() + 4;
        int nameX = leadingCountX + options.leadingCountColumnWidth() + 4;
        return new RowLayout(
                rowLeft,
                rowTop,
                rowWidth,
                iconX,
                countColumnLeft,
                actionX,
                leadingCountX,
                nameX,
                options.itemSize(),
                options.countColumnWidth(),
                options.actionColumnWidth()
        );
    }

    public ClickTarget clickTarget(RowLayout layout, boolean desiredCountActive, double mouseX) {
        if (mouseX >= layout.iconX() - 2 && mouseX <= layout.iconX() + layout.itemSize() + 2) {
            return ClickTarget.ICON;
        }
        if (desiredCountActive && mouseX >= layout.countColumnLeft() - 2 && mouseX <= layout.countColumnLeft() + layout.countColumnWidth() + 2) {
            return ClickTarget.DESIRED_COUNT;
        }
        if (mouseX >= layout.actionX() - 2 && mouseX <= layout.actionX() + layout.actionColumnWidth() + 2) {
            return ClickTarget.ACTION;
        }
        return ClickTarget.BODY;
    }

    public int actionMenuX(int rowRight) {
        int width = 148;
        return Math.min(owner.contentRight() - width, Math.max(owner.centerPaneX(), rowRight - width));
    }

    public int iconMenuX(RowLayout layout, RowOptions options) {
        int width = 148;
        int desiredX = layout.iconX() + options.itemSize() + 6;
        return Math.min(owner.contentRight() - width, Math.max(owner.centerPaneX(), desiredX));
    }

    public int actionMenuY(RowLayout layout, RowOptions options) {
        int desiredY = layout.rowTop() + options.rowHeight();
        return Math.min(owner.panelBottom() - AbstractInventoryBrowserScreen.OUTER_MARGIN - 20, desiredY);
    }

    public record RowOptions(
            int rowHeight,
            int itemSize,
            float itemScale,
            int actionColumnWidth,
            int countColumnWidth,
            int leadingCountColumnWidth,
            int nameMinWidth
    ) {
    }

    public record RowPresentation(
            InventoryViewData.EntryView entry,
            int localCount,
            String collectionId,
            boolean collectionTracked,
            int desiredCount,
            String secondaryText,
            boolean equippedOnly,
            int countTextColor,
            int nameColor,
            int secondaryMaxWidth
    ) {
    }

    public record RowLayout(
            int rowLeft,
            int rowTop,
            int rowWidth,
            int iconX,
            int countColumnLeft,
            int actionX,
            int leadingCountX,
            int nameX,
            int itemSize,
            int countColumnWidth,
            int actionColumnWidth
    ) {
    }

    public enum ClickTarget {
        ICON,
        DESIRED_COUNT,
        ACTION,
        BODY
    }
}
