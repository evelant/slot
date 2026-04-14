package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.CollectionStockSummary;
import dev.imagio.slot.projection.InventoryViewData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class InventorySectionHeaderSupport {
    private final AbstractInventoryBrowserScreen<?> screen;

    public InventorySectionHeaderSupport(AbstractInventoryBrowserScreen<?> screen) {
        this.screen = screen;
    }

    public SectionHeaderState buildState(
            InventoryViewData.Section section,
            int x,
            int y,
            int width,
            SectionHeaderOptions options
    ) {
        if (section == null) {
            return SectionHeaderState.empty();
        }

        int labelLeft = x + 6;
        int labelRight = x + width - 6;

        AbstractInventoryBrowserScreen.InlineButton menuButton = null;
        if (options.showMenuButton()) {
            menuButton = menuButton(x, y, width, options.menuEnabled());
            if (menuButton.enabled()) {
                labelRight = menuButton.x() - 6;
            }
        }

        AbstractInventoryBrowserScreen.InlineButton toggleButton = null;
        AbstractInventoryBrowserScreen.InlineButton pinButton = null;
        AbstractInventoryBrowserScreen.InlineButton restockButton = null;
        CollectionStockSummary stockSummary = CollectionStockSummary.NONE;
        String stockSummaryText = "";
        int summaryX = -1;
        int summaryWidth = 0;

        if (options.collectionControls()) {
            toggleButton = toggleButton(x, y, section.collectionId());
            labelLeft = toggleButton.x() + toggleButton.width() + 6;

            int rightAnchor = menuButton == null ? x + width : menuButton.x();
            pinButton = pinButton(y, rightAnchor, section.collectionId());
            if (pinButton.enabled()) {
                labelRight = Math.min(labelRight, pinButton.x() - 6);
            }

            if (options.showRestockButton()) {
                restockButton = restockButton(y, pinButton.x(), options.restockEnabled());
                if (restockButton.enabled()) {
                    labelRight = Math.min(labelRight, restockButton.x() - 6);
                }
            }

            stockSummary = screen.collectionStockSummary(section.collectionId());
            if (stockSummary.hasShortfall()) {
                stockSummaryText = stockSummaryText(stockSummary);
                summaryWidth = screen.scaledTextWidth(stockSummaryText, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
                summaryX = Math.max(options.summaryMinX(), labelRight - summaryWidth);
                labelRight = summaryX - 6;
            }
        }

        return new SectionHeaderState(
                sectionBackgroundColor(section),
                sectionAccentColor(section),
                sectionLabelColor(section),
                toggleButton,
                pinButton,
                menuButton,
                restockButton,
                stockSummary,
                stockSummaryText,
                labelLeft,
                labelRight,
                summaryX,
                summaryWidth
        );
    }

    public void render(
            GuiGraphics guiGraphics,
            InventoryViewData.Section section,
            SectionHeaderState state,
            int x,
            int y,
            int width,
            int height,
            int count,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.fill(x, y, x + width, y + height, state.backgroundColor());
        guiGraphics.fill(x, y, x + 2, y + height, state.accentColor());
        guiGraphics.fill(x + 2, y, x + width, y + 1, 0x303A3A3A);
        guiGraphics.fill(x + 2, y + height - 1, x + width, y + height, 0x40202020);

        if (state.menuButton() != null && state.menuButton().enabled()) {
            drawButton(guiGraphics, state.menuButton(), mouseX, mouseY);
        }
        if (state.toggleButton() != null) {
            drawButton(guiGraphics, state.toggleButton(), mouseX, mouseY);
        }
        if (state.restockButton() != null) {
            drawButton(guiGraphics, state.restockButton(), mouseX, mouseY);
        }
        if (state.pinButton() != null && state.pinButton().enabled()) {
            drawButton(guiGraphics, state.pinButton(), mouseX, mouseY);
        }

        if (state.pinButton() != null && state.pinButton().enabled() && state.pinButton().contains(mouseX, mouseY)) {
                    SlotTooltipRenderer.renderTextTooltip(
                            guiGraphics,
                            screen.textFont(),
                            List.of(Component.translatable(
                                    screen.collectionViewStateController.pinLoadoutsWhenCollectionCollapsed(section.collectionId())
                                            ? "slot.screen.collections.unpin_loadouts_when_collapsed"
                                    : "slot.screen.collections.pin_loadouts_when_collapsed")),
                    mouseX,
                    mouseY,
                    screen.width,
                    screen.height
            );
        }

        if (state.stockSummary().hasShortfall()) {
            int summaryColor = state.stockSummary().missingCount() > 0 ? 0xE0A0A0 : 0xE0C090;
            screen.drawScaledText(
                    guiGraphics,
                    state.stockSummaryText(),
                    state.summaryX(),
                    screen.rowTextY(y),
                    summaryColor,
                    AbstractInventoryBrowserScreen.ROW_TEXT_SCALE
            );
            if (summaryContains(state, y, height, mouseX, mouseY)) {
                SlotTooltipRenderer.renderTextTooltip(
                        guiGraphics,
                        screen.textFont(),
                        List.of(
                                Component.translatable("slot.screen.collections.stock.missing", state.stockSummary().missingCount()),
                                Component.translatable("slot.screen.collections.stock.low", state.stockSummary().lowCount())
                        ),
                        mouseX,
                        mouseY,
                        screen.width,
                        screen.height
                );
            }
        }

        int labelRight = state.labelRight();
        String countText = Integer.toString(count);
        int countWidth = screen.scaledTextWidth(countText, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        int countX = Math.max(x + 28, labelRight - countWidth);
        screen.drawScaledText(
                guiGraphics,
                countText,
                countX,
                screen.rowTextY(y),
                0xAEB8C0,
                AbstractInventoryBrowserScreen.ROW_TEXT_SCALE
        );
        labelRight = countX - 6;

        String trimmedLabel = screen.textFont().plainSubstrByWidth(section.label(), Math.max(24, labelRight - state.labelLeft()));
        screen.drawScaledText(
                guiGraphics,
                trimmedLabel,
                state.labelLeft(),
                screen.rowTextY(y),
                state.labelColor(),
                AbstractInventoryBrowserScreen.ROW_TEXT_SCALE
        );
    }

    public SectionHeaderClickTarget clickTarget(SectionHeaderState state, double mouseX, double mouseY) {
        if (state.toggleButton() != null && state.toggleButton().contains(mouseX, mouseY)) {
            return SectionHeaderClickTarget.TOGGLE;
        }
        if (state.pinButton() != null && state.pinButton().enabled() && state.pinButton().contains(mouseX, mouseY)) {
            return SectionHeaderClickTarget.PIN;
        }
        if (state.menuButton() != null && state.menuButton().enabled() && state.menuButton().contains(mouseX, mouseY)) {
            return SectionHeaderClickTarget.MENU;
        }
        if (state.restockButton() != null && state.restockButton().contains(mouseX, mouseY)) {
            return SectionHeaderClickTarget.RESTOCK;
        }
        return SectionHeaderClickTarget.NAVIGATE;
    }

    private void drawButton(
            GuiGraphics guiGraphics,
            AbstractInventoryBrowserScreen.InlineButton button,
            int mouseX,
            int mouseY
    ) {
        screen.drawInlineButton(
                guiGraphics,
                button.x(),
                button.y(),
                button.width(),
                button.height(),
                button.label(),
                button.enabled(),
                button.contains(mouseX, mouseY)
        );
    }

    private boolean summaryContains(
            SectionHeaderState state,
            int y,
            int rowHeight,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= state.summaryX() - 2
                && mouseX <= state.summaryX() + state.summaryWidth() + 2
                && mouseY >= y
                && mouseY <= y + rowHeight
                && mouseX <= rightControlLeft(state) - 2;
    }

    private int rightControlLeft(SectionHeaderState state) {
        int rightControlLeft = Integer.MAX_VALUE;
        if (state.pinButton() != null && state.pinButton().enabled()) {
            rightControlLeft = Math.min(rightControlLeft, state.pinButton().x());
        }
        if (state.menuButton() != null && state.menuButton().enabled()) {
            rightControlLeft = Math.min(rightControlLeft, state.menuButton().x());
        }
        if (state.restockButton() != null && state.restockButton().enabled()) {
            rightControlLeft = Math.min(rightControlLeft, state.restockButton().x());
        }
        return rightControlLeft == Integer.MAX_VALUE ? screen.contentRight() : rightControlLeft;
    }

    private AbstractInventoryBrowserScreen.InlineButton toggleButton(int x, int y, String collectionId) {
        int buttonY = y + 2;
        int buttonX = x + 4;
        String label = screen.isCollectionCollapsed(collectionId) ? ">" : "v";
        return new AbstractInventoryBrowserScreen.InlineButton(
                buttonX,
                buttonY,
                AbstractInventoryBrowserScreen.COLLECTION_TOGGLE_BUTTON_WIDTH,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                label,
                true
        );
    }

    private AbstractInventoryBrowserScreen.InlineButton pinButton(int y, int rightAnchor, String collectionId) {
        int buttonY = y + 2;
        int buttonX = rightAnchor - AbstractInventoryBrowserScreen.COLLECTION_BUTTON_GAP - AbstractInventoryBrowserScreen.COLLECTION_PIN_BUTTON_WIDTH;
        String label = Component.translatable(
                screen.collectionViewStateController.pinLoadoutsWhenCollectionCollapsed(collectionId)
                        ? "slot.screen.collections.inline.pin_on"
                        : "slot.screen.collections.inline.pin_off"
        ).getString();
        return new AbstractInventoryBrowserScreen.InlineButton(
                buttonX,
                buttonY,
                AbstractInventoryBrowserScreen.COLLECTION_PIN_BUTTON_WIDTH,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                label,
                screen.collectionHasLoadouts(collectionId)
        );
    }

    private AbstractInventoryBrowserScreen.InlineButton menuButton(int x, int y, int width, boolean enabled) {
        int buttonY = y + 2;
        int buttonX = x + width - AbstractInventoryBrowserScreen.COLLECTION_MENU_BUTTON_WIDTH;
        return new AbstractInventoryBrowserScreen.InlineButton(
                buttonX,
                buttonY,
                AbstractInventoryBrowserScreen.COLLECTION_MENU_BUTTON_WIDTH,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                "...",
                enabled
        );
    }

    private AbstractInventoryBrowserScreen.InlineButton restockButton(int y, int pinX, boolean enabled) {
        int buttonY = y + 2;
        int buttonX = pinX - AbstractInventoryBrowserScreen.COLLECTION_BUTTON_GAP - AbstractInventoryBrowserScreen.COLLECTION_RESTOCK_BUTTON_WIDTH;
        return new AbstractInventoryBrowserScreen.InlineButton(
                buttonX,
                buttonY,
                AbstractInventoryBrowserScreen.COLLECTION_RESTOCK_BUTTON_WIDTH,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                Component.translatable("slot.screen.collections.inline.restock").getString(),
                enabled
        );
    }

    private String stockSummaryText(CollectionStockSummary stockSummary) {
        if (stockSummary.missingCount() > 0 && stockSummary.lowCount() > 0) {
            return Component.translatable("slot.screen.collections.stock.summary.both", stockSummary.missingCount(), stockSummary.lowCount()).getString();
        }
        if (stockSummary.missingCount() > 0) {
            return Component.translatable("slot.screen.collections.stock.summary.missing", stockSummary.missingCount()).getString();
        }
        if (stockSummary.lowCount() > 0) {
            return Component.translatable("slot.screen.collections.stock.summary.low", stockSummary.lowCount()).getString();
        }
        return "";
    }

    private int sectionBackgroundColor(InventoryViewData.Section section) {
        if (section.isCollection()) {
            return 0x8A253424;
        }
        if (section.isRecent()) {
            return 0x8A392C20;
        }
        if (section.isModBucket()) {
            return 0x7A263243;
        }
        return 0x74262A30;
    }

    private int sectionAccentColor(InventoryViewData.Section section) {
        if (section.isCollection()) {
            return 0xC09DAA58;
        }
        if (section.isRecent()) {
            return 0xC0C78A3A;
        }
        if (section.isModBucket()) {
            return 0xC07896BE;
        }
        return 0xC06B7D90;
    }

    private int sectionLabelColor(InventoryViewData.Section section) {
        if (section.isCollection()) {
            return 0xF0E7D0;
        }
        if (section.isRecent()) {
            return 0xF0DFC8;
        }
        if (section.isModBucket()) {
            return 0xE2E8F0;
        }
        return 0xE0E4E8;
    }

    public record SectionHeaderOptions(
            boolean collectionControls,
            boolean showMenuButton,
            boolean menuEnabled,
            boolean showRestockButton,
            boolean restockEnabled,
            int summaryMinX
    ) {
    }

    public record SectionHeaderState(
            int backgroundColor,
            int accentColor,
            int labelColor,
            AbstractInventoryBrowserScreen.InlineButton toggleButton,
            AbstractInventoryBrowserScreen.InlineButton pinButton,
            AbstractInventoryBrowserScreen.InlineButton menuButton,
            AbstractInventoryBrowserScreen.InlineButton restockButton,
            CollectionStockSummary stockSummary,
            String stockSummaryText,
            int labelLeft,
            int labelRight,
            int summaryX,
            int summaryWidth
    ) {
        private static SectionHeaderState empty() {
            return new SectionHeaderState(
                    0,
                    0,
                    0,
                    null,
                    null,
                    null,
                    null,
                    CollectionStockSummary.NONE,
                    "",
                    0,
                    0,
                    -1,
                    0
            );
        }
    }

    public enum SectionHeaderClickTarget {
        TOGGLE,
        PIN,
        MENU,
        RESTOCK,
        NAVIGATE
    }
}
