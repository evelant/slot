package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.screen.AbstractInventoryBrowserScreen.InlineButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class InventoryLoadoutRowSupport {
    private final AbstractInventoryBrowserScreen<?> screen;

    public InventoryLoadoutRowSupport(AbstractInventoryBrowserScreen<?> screen) {
        this.screen = screen;
    }

    public LoadoutRowLayout renderRow(
            GuiGraphics guiGraphics,
            String collectionId,
            HotbarLoadoutDefinition loadout,
            int loadoutCount,
            LoadoutRowOptions options,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.fill(x, y, x + width, y + options.rowHeight(), 0x4A1A1A1A);

        if (loadout == null) {
            screen.drawScaledText(
                    guiGraphics,
                    Component.translatable("slot.screen.collections.inline.empty_loadouts").getString(),
                    x + 6,
                    screen.rowTextY(y),
                    0xA0A0A0,
                    AbstractInventoryBrowserScreen.ROW_TEXT_SCALE
            );
            return null;
        }

        LoadoutRowLayout rowLayout = rowLayout(collectionId, loadout, loadoutCount, options, x, y, width);
        drawButton(guiGraphics, rowLayout.previousButton(), mouseX, mouseY);
        drawButton(guiGraphics, rowLayout.nextButton(), mouseX, mouseY);
        drawButton(guiGraphics, rowLayout.hotkeyButton(), mouseX, mouseY);
        drawButton(guiGraphics, rowLayout.updateButton(), mouseX, mouseY);
        drawButton(guiGraphics, rowLayout.applyButton(), mouseX, mouseY);
        drawButton(guiGraphics, rowLayout.deleteButton(), mouseX, mouseY);

        if (rowLayout.hotkeyButton().contains(mouseX, mouseY)) {
            SlotTooltipRenderer.renderTextTooltip(
                    guiGraphics,
                    screen.textFont(),
                    List.of(Component.literal(loadout.hotkeySlot() == null
                            ? "Click to assign Ctrl/Cmd+1-9"
                            : "Hotkey: Ctrl/Cmd+" + (loadout.hotkeySlot() + 1) + " (right-click to clear)")),
                    mouseX,
                    mouseY,
                    screen.width,
                    screen.height
            );
        }

        if (screen.isInlineLoadoutRenameTarget(collectionId, loadout)) {
            screen.syncInlineLoadoutRenameBox(collectionId, rowLayout.nameX(), rowLayout.nameEditWidth(), loadout, y);
        } else {
            String name = screen.textFont().plainSubstrByWidth(loadout.name(), rowLayout.nameWidth());
            screen.drawScaledText(guiGraphics, name, rowLayout.nameX(), screen.rowTextY(y), 0xEAEAEA, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE);
        }
        return rowLayout;
    }

    public LoadoutRowLayout rowLayout(
            String collectionId,
            HotbarLoadoutDefinition loadout,
            int loadoutCount,
            LoadoutRowOptions options,
            int x,
            int y,
            int width
    ) {
        boolean hasLoadout = loadout != null;
        int buttonY = y + 2;
        String updateLabel = Component.translatable("slot.screen.collections.inline.update").getString();
        String useLabel = Component.translatable("slot.screen.collections.inline.use").getString();

        InlineButton deleteButton = new InlineButton(
                x + width - 4 - loadoutButtonWidth("X"),
                buttonY,
                loadoutButtonWidth("X"),
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                "X",
                hasLoadout
        );
        InlineButton applyButton = new InlineButton(
                deleteButton.x() - AbstractInventoryBrowserScreen.COLLECTION_BUTTON_GAP - loadoutButtonWidth(useLabel),
                buttonY,
                loadoutButtonWidth(useLabel),
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                useLabel,
                hasLoadout
        );
        InlineButton updateButton = new InlineButton(
                applyButton.x() - AbstractInventoryBrowserScreen.COLLECTION_BUTTON_GAP - loadoutButtonWidth(updateLabel),
                buttonY,
                loadoutButtonWidth(updateLabel),
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                updateLabel,
                hasLoadout
        );

        InlineButton previousButton = new InlineButton(
                x + 4,
                buttonY,
                AbstractInventoryBrowserScreen.LOADOUT_ARROW_WIDTH,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                "<",
                loadoutCount > 1
        );
        int nameX = previousButton.x() + previousButton.width() + 6;
        String hotkeyLabel = loadout == null ? "H-" : loadout.hotkeyIndicator();
        int hotkeyButtonWidth = loadoutButtonWidth(hotkeyLabel);
        int previewReserve = options.previewEnabled()
                ? screen.loadoutPreviewSpanWidth(options.previewReserveSlots())
                : 0;
        int maxNameWidth = Math.min(
                AbstractInventoryBrowserScreen.LOADOUT_NAME_WIDTH,
                Math.max(30, updateButton.x() - nameX - AbstractInventoryBrowserScreen.LOADOUT_ARROW_WIDTH - hotkeyButtonWidth - previewReserve - 18)
        );
        String name = loadout == null ? "" : screen.textFont().plainSubstrByWidth(loadout.name(), maxNameWidth);
        int nameWidth = Math.max(30, Math.min(maxNameWidth, screen.scaledTextWidth(name, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE) + 4));
        InlineButton nextButton = new InlineButton(
                nameX + nameWidth + 3,
                buttonY,
                AbstractInventoryBrowserScreen.LOADOUT_ARROW_WIDTH,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                ">",
                loadoutCount > 1
        );
        InlineButton hotkeyButton = new InlineButton(
                nextButton.x() + nextButton.width() + 3,
                buttonY,
                hotkeyButtonWidth,
                AbstractInventoryBrowserScreen.COLLECTION_BUTTON_HEIGHT,
                hotkeyLabel,
                hasLoadout
        );

        int previewX = hotkeyButton.x() + hotkeyButton.width() + 6;
        int previewRight = updateButton.x() - 8;
        int previewWidth = options.previewEnabled() ? Math.max(0, previewRight - previewX) : 0;
        int visiblePreviewSlots = 0;
        while (visiblePreviewSlots < HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT
                && previewWidth >= screen.loadoutPreviewSpanWidth(visiblePreviewSlots + 1)) {
            visiblePreviewSlots++;
        }

        return new LoadoutRowLayout(
                previousButton,
                nextButton,
                hotkeyButton,
                updateButton,
                applyButton,
                deleteButton,
                nameX,
                maxNameWidth,
                Math.max(34, nextButton.x() - nameX - 3),
                previewX,
                visiblePreviewSlots,
                hiddenConfiguredPreviewSlots(loadout, visiblePreviewSlots)
        );
    }

    public LoadoutClickTarget clickTarget(
            LoadoutRowLayout rowLayout,
            int rowTop,
            int rowHeight,
            double mouseX,
            double mouseY
    ) {
        if (rowLayout.previousButton().contains(mouseX, mouseY)) {
            return LoadoutClickTarget.PREVIOUS;
        }
        if (rowLayout.nextButton().contains(mouseX, mouseY)) {
            return LoadoutClickTarget.NEXT;
        }
        if (rowLayout.hotkeyButton().contains(mouseX, mouseY)) {
            return LoadoutClickTarget.HOTKEY;
        }
        if (mouseX >= rowLayout.nameX()
                && mouseX <= rowLayout.nameX() + rowLayout.nameEditWidth()
                && mouseY >= rowTop
                && mouseY <= rowTop + rowHeight) {
            return LoadoutClickTarget.NAME;
        }
        if (rowLayout.updateButton().contains(mouseX, mouseY)) {
            return LoadoutClickTarget.UPDATE;
        }
        if (rowLayout.applyButton().contains(mouseX, mouseY)) {
            return LoadoutClickTarget.APPLY;
        }
        if (rowLayout.deleteButton().contains(mouseX, mouseY)) {
            return LoadoutClickTarget.DELETE;
        }
        return LoadoutClickTarget.NONE;
    }

    private void drawButton(GuiGraphics guiGraphics, InlineButton button, int mouseX, int mouseY) {
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

    private int hiddenConfiguredPreviewSlots(HotbarLoadoutDefinition loadout, int visiblePreviewSlots) {
        if (loadout == null) {
            return 0;
        }

        int hidden = 0;
        for (int slotIndex = visiblePreviewSlots; slotIndex < HotbarLoadoutDefinition.QUICK_ACCESS_SLOT_COUNT; slotIndex++) {
            if (loadout.identityForQuickAccessSlot(slotIndex) != null) {
                hidden++;
            }
        }
        return hidden;
    }

    private int loadoutButtonWidth(String label) {
        return Math.max(
                AbstractInventoryBrowserScreen.LOADOUT_BUTTON_WIDTH,
                screen.scaledTextWidth(label, AbstractInventoryBrowserScreen.ROW_TEXT_SCALE) + 8
        );
    }

    public record LoadoutRowOptions(
            int rowHeight,
            boolean previewEnabled,
            int previewReserveSlots
    ) {
    }

    public record LoadoutRowLayout(
            InlineButton previousButton,
            InlineButton nextButton,
            InlineButton hotkeyButton,
            InlineButton updateButton,
            InlineButton applyButton,
            InlineButton deleteButton,
            int nameX,
            int nameWidth,
            int nameEditWidth,
            int previewX,
            int visiblePreviewSlots,
            int hiddenPreviewSlots
    ) {
    }

    public enum LoadoutClickTarget {
        PREVIOUS,
        NEXT,
        HOTKEY,
        NAME,
        UPDATE,
        APPLY,
        DELETE,
        NONE
    }
}
