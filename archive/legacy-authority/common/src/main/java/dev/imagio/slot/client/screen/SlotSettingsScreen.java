package dev.imagio.slot.client.screen;

import dev.imagio.slot.workflow.SettingsService;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SlotSettingsScreen extends Screen {
    private final Screen parentScreen;
    private final SettingsService controller;
    private final Runnable onChanged;

    private Button slotEnabledButton;
    private Button replacePlayerInventoryButton;
    private Button replaceChestLikeStorageButton;
    private Button syncSearchWithEmiButton;

    public SlotSettingsScreen(Screen parentScreen, SettingsService controller, Runnable onChanged) {
        super(Component.translatable("slot.screen.settings.title"));
        this.parentScreen = parentScreen;
        this.controller = controller;
        this.onChanged = onChanged;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 220;
        int buttonX = (width - buttonWidth) / 2;
        int y = 56;

        slotEnabledButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    controller.setSlotEnabled(!controller.slotEnabled());
                    updateButtonLabels();
                    onChanged.run();
                })
                .bounds(buttonX, y, buttonWidth, 20)
                .build());
        y += 24;

        replacePlayerInventoryButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    controller.setReplacePlayerInventory(!controller.replacePlayerInventory());
                    updateButtonLabels();
                    onChanged.run();
                })
                .bounds(buttonX, y, buttonWidth, 20)
                .build());
        y += 24;

        replaceChestLikeStorageButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    controller.setReplaceChestLikeStorage(!controller.replaceChestLikeStorage());
                    updateButtonLabels();
                    onChanged.run();
                })
                .bounds(buttonX, y, buttonWidth, 20)
                .build());
        y += 32;

        syncSearchWithEmiButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    controller.setSyncSearchWithEmi(!controller.syncSearchWithEmi());
                    updateButtonLabels();
                    onChanged.run();
                })
                .bounds(buttonX, y, buttonWidth, 20)
                .build());
        y += 32;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(buttonX, y, buttonWidth, 20)
                .build());

        updateButtonLabels();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
        guiGraphics.fill(width / 2 - 140, 32, width / 2 + 140, 188, 0xC0101010);
        guiGraphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, Component.translatable("slot.screen.settings.subtitle"), width / 2, 36, 0xB0B0B0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void updateButtonLabels() {
        slotEnabledButton.setMessage(toggleMessage("slot.screen.settings.enable_slot", controller.slotEnabled()));
        replacePlayerInventoryButton.setMessage(toggleMessage("slot.screen.settings.replace_player_inventory", controller.replacePlayerInventory()));
        replaceChestLikeStorageButton.setMessage(toggleMessage("slot.screen.settings.replace_chest_like_storage", controller.replaceChestLikeStorage()));
        syncSearchWithEmiButton.setMessage(toggleMessage("slot.screen.settings.sync_search_with_emi", controller.syncSearchWithEmi()));
    }

    private Component toggleMessage(String key, boolean enabled) {
        return Component.translatable(key, Component.translatable(enabled ? "slot.screen.settings.value.on" : "slot.screen.settings.value.off"));
    }
}
