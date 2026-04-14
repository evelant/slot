package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.SlotClientCompat;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.client.screen.SlotCarriedInventoryScreen;
import dev.imagio.slot.client.session.SlotScreenSessionResolver;
import dev.imagio.slot.client.screen.container.SlotInventoryWorkspaceScreen;
import dev.imagio.slot.network.BackpackContentsRequestRequester;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.compat.emi.SlotEmiPlugin;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class SlotScreenHooks {
    private static final int SLOT_REENTRY_BUTTON_X = 6;
    private static final int SLOT_REENTRY_BUTTON_Y = 6;
    private static final int SLOT_REENTRY_BUTTON_SIZE = 20;
    private static boolean bypassNextPlayerInventoryReplacement;
    private static boolean bypassNextChestLikeReplacement;

    private SlotScreenHooks() {
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!SlotNeoForgeClient.settingsController().slotEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean emiPresent = emiPresent();

        if (event.getNewScreen() instanceof InventoryScreen) {
            if (minecraft.player == null) {
                return;
            }

            if (bypassNextPlayerInventoryReplacement) {
                bypassNextPlayerInventoryReplacement = false;
                if (SlotClientConfig.CLIENT.debugLogging.get()) {
                    SlotCommon.LOGGER.info("SLOT bypassed player inventory replacement once");
                }
                return;
            }

            if (SlotClientConfig.CLIENT.debugLogging.get()) {
                SlotCommon.LOGGER.info("SLOT observed player inventory screen opening");
            }

            if (SlotNeoForgeClient.settingsController().replacePlayerInventory()) {
                Screen replacement = createPlayerInventoryReplacementScreen(minecraft, event.getCurrentScreen(), event.getCurrentScreen(), emiPresent);
                if (replacement != null) {
                    event.setNewScreen(replacement);
                }
            }
        } else if (event.getNewScreen() instanceof AbstractContainerScreen<?> containerScreen) {
            if (minecraft.player == null) {
                return;
            }

            if (isSlotOwnedScreen(event.getNewScreen())) {
                return;
            }

            if (bypassNextChestLikeReplacement) {
                bypassNextChestLikeReplacement = false;
                if (SlotClientConfig.CLIENT.debugLogging.get()) {
                    SlotCommon.LOGGER.info("SLOT bypassed chest-like replacement once");
                }
                return;
            }

            if (SlotClientConfig.CLIENT.debugLogging.get()) {
                SlotCommon.LOGGER.info(
                        "SLOT observed chest-like container screen opening: screen={} menu={} slots={}",
                        event.getNewScreen().getClass().getName(),
                        containerScreen.getMenu().getClass().getName(),
                        containerScreen.getMenu().slots.size()
                );
            }

            if (!SlotNeoForgeClient.settingsController().replaceChestLikeStorage()) {
                return;
            }

            Screen replacement = createChestLikeReplacementScreen(containerScreen, event.getCurrentScreen(), emiPresent);
            if (replacement != null) {
                event.setNewScreen(replacement);
            }
        }
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!SlotNeoForgeClient.settingsController().slotEnabled()) {
            return;
        }

        Screen screen = event.getScreen();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || isSlotOwnedScreen(screen)) {
            return;
        }

        if (screen instanceof InventoryScreen) {
            if (SlotNeoForgeClient.settingsController().replacePlayerInventory()) {
                return;
            }

            event.addListener(Button.builder(Component.literal("S"), button -> reopenPlayerInventoryInSlot(screen))
                    .tooltip(Tooltip.create(Component.translatable("slot.screen.toggle.player.enable")))
                    .bounds(SLOT_REENTRY_BUTTON_X, SLOT_REENTRY_BUTTON_Y, SLOT_REENTRY_BUTTON_SIZE, SLOT_REENTRY_BUTTON_SIZE)
                    .build());
            return;
        }

        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }
        if (SlotNeoForgeClient.settingsController().replaceChestLikeStorage()) {
            return;
        }

        if (!SlotScreenReplacementFactory.supportsContainerReplacement(containerScreen, minecraft.player)) {
            return;
        }

        event.addListener(Button.builder(Component.literal("S"), button -> reopenChestLikeScreenInSlot(containerScreen))
                .tooltip(Tooltip.create(Component.translatable("slot.screen.toggle.chest.enable")))
                .bounds(SLOT_REENTRY_BUTTON_X, SLOT_REENTRY_BUTTON_Y, SLOT_REENTRY_BUTTON_SIZE, SLOT_REENTRY_BUTTON_SIZE)
                .build());
    }

    public static void openVanillaPlayerInventory() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        bypassNextPlayerInventoryReplacement = true;
        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    public static void disablePlayerInventoryReplacement() {
        SlotNeoForgeClient.settingsController().setReplacePlayerInventory(false);
        openVanillaPlayerInventory();
    }

    private static void reopenPlayerInventoryInSlot(Screen currentScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        SlotNeoForgeClient.settingsController().setReplacePlayerInventory(true);
        Screen replacement = createPlayerInventoryReplacementScreen(minecraft, null, currentScreen, emiPresent());
        if (replacement != null) {
            minecraft.setScreen(replacement);
        }
    }

    private static void openVanillaChestLikeScreen(AbstractContainerScreen<?> vanillaScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        TomsStoragePacketCallbackBridge.restore(vanillaScreen);
        bypassNextChestLikeReplacement = true;
        minecraft.setScreen(vanillaScreen);
    }

    private static void disableChestLikeReplacement(AbstractContainerScreen<?> vanillaScreen) {
        SlotNeoForgeClient.settingsController().setReplaceChestLikeStorage(false);
        openVanillaChestLikeScreen(vanillaScreen);
    }

    private static void reopenChestLikeScreenInSlot(AbstractContainerScreen<?> vanillaScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        SlotNeoForgeClient.settingsController().setReplaceChestLikeStorage(true);
        Screen replacement = createChestLikeReplacementScreen(vanillaScreen, null, emiPresent());
        if (replacement != null) {
            minecraft.setScreen(replacement);
        }
    }

    private static void requestPlayerBackpackContentsSync() {
        if (Minecraft.getInstance().player == null || !SophisticatedBackpackSupport.isAvailable()) {
            return;
        }
        BackpackContentsRequestRequester.requestSync();
    }

    private static boolean isSlotOwnedScreen(Object screen) {
        return screen instanceof SlotCarriedInventoryScreen
                || screen instanceof SlotInventoryWorkspaceScreen<?>;
    }

    private static boolean emiPresent() {
        return ModList.get().isLoaded("emi") || SlotClientCompat.hasEmi();
    }

    private static Screen createPlayerInventoryReplacementScreen(
            Minecraft minecraft,
            Screen parentScreen,
            Screen currentScreen,
            boolean emiPresent
    ) {
        requestPlayerBackpackContentsSync();
        return SlotScreenReplacementFactory.createPlayerInventoryReplacement(
                minecraft,
                parentScreen,
                currentScreen,
                emiPresent,
                SlotScreenHooks::openVanillaPlayerInventory,
                () -> Component.translatable("slot.screen.toggle.player.disable"),
                SlotScreenHooks::disablePlayerInventoryReplacement
        );
    }

    private static Screen createChestLikeReplacementScreen(
            AbstractContainerScreen<?> containerScreen,
            Screen parentScreen,
            boolean emiPresent
    ) {
        requestPlayerBackpackContentsSync();
        TomsStoragePacketCallbackBridge.detach(containerScreen);
        if (emiPresent) {
            SlotEmiPlugin.ensureTomsRecipeHandler(containerScreen.getMenu());
        }
        return SlotScreenReplacementFactory.createContainerReplacement(
                containerScreen,
                parentScreen,
                emiPresent,
                () -> openVanillaChestLikeScreen(containerScreen),
                () -> Component.translatable("slot.screen.toggle.chest.disable"),
                () -> disableChestLikeReplacement(containerScreen)
        );
    }

}
