package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.neoforge.config.SlotClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Vanilla-screen overlay button: while SLOT is disabled in the client
 * config, every time the player opens the vanilla inventory screen we
 * tack on a small "Re-enable SLOT" pill in the top-right corner so the
 * one-click route back is always present.
 *
 * <p>Companion to the "Disable SLOT" button in the atlas top-right
 * cluster — the two together define a self-contained toggle path:
 * disable → vanilla; from vanilla → re-enable → atlas.
 */
public final class SlotReenableButton {
    private static final int BUTTON_WIDTH = 96;
    private static final int BUTTON_HEIGHT = 14;

    private static boolean registered;

    private SlotReenableButton() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotReenableButton::onScreenInit);
        registered = true;
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen inventoryScreen)) {
            return;
        }
        // Only render the pill while SLOT is disabled — otherwise the
        // vanilla screen is normal SLOT-bypass behaviour and the player
        // doesn't need an extra escape hatch.
        if (SlotClientConfig.CLIENT.slotEnabled.get()) {
            return;
        }
        AbstractContainerScreen<?> screen = inventoryScreen;
        int x = screen.getGuiLeft() + screen.getXSize() - BUTTON_WIDTH;
        int y = screen.getGuiTop() - BUTTON_HEIGHT - 4;
        Button button = Button.builder(
                Component.literal("Re-enable SLOT"),
                btn -> {
                    SlotClientConfig.CLIENT.slotEnabled.set(true);
                    SlotClientConfig.CLIENT.slotEnabled.save();
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft == null || minecraft.player == null) {
                        return;
                    }
                    // Closing the vanilla screen drops us to gameplay; the
                    // next inventory-key press now lands in the SLOT
                    // workspace because the config gate is open again.
                    minecraft.player.closeContainer();
                }
        )
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal(
                        "Restore SLOT — the inventory key opens the SLOT atlas again.")))
                .build();
        event.addListener(button);
    }
}
