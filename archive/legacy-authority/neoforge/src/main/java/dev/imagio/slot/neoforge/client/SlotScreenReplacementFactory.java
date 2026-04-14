package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.screen.SlotCarriedInventoryScreen;
import dev.imagio.slot.client.session.SlotScreenSession;
import dev.imagio.slot.client.session.SlotScreenSessionResolver;
import dev.imagio.slot.client.session.SlotSessionKind;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.client.screen.container.SlotInventoryWorkspaceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.function.Supplier;

final class SlotScreenReplacementFactory {
    private SlotScreenReplacementFactory() {
    }

    static Screen createPlayerInventoryReplacement(
            Minecraft minecraft,
            Screen parentScreen,
            Screen currentScreen,
            boolean emiPresent,
            Runnable openVanillaAction,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction
    ) {
        if (minecraft.player == null) {
            return null;
        }

        Component title = Component.translatable("slot.screen.inventory.title");
        SlotScreenSession activeSession = SlotScreenSessionResolver.resolveActiveCarriedMenu(minecraft, title, currentScreen);
        InventoryHostDescriptor activeHost = activeSession == null ? null : activeSession.host();
        Screen resolvedParentScreen = parentScreen;
        if (activeHost != null && currentScreenOwnsMenu(parentScreen, activeHost.menu())) {
            resolvedParentScreen = null;
        }

        if (activeHost != null) {
            return new SlotCarriedInventoryScreen(
                    resolvedParentScreen,
                    SlotNeoForgeClient.collectionStore(),
                    openVanillaAction,
                    emiPresent,
                    SlotNeoForgeClient.settingsController(),
                    SlotNeoForgeClient.collectionViewStateController(),
                    currentScreenToggleLabel,
                    currentScreenToggleAction,
                    activeHost,
                    SlotNeoForgeClient.searchWorkflow(),
                    SlotNeoForgeClient.inspectionService()
            );
        }

        return new SlotCarriedInventoryScreen(
                resolvedParentScreen,
                SlotNeoForgeClient.collectionStore(),
                openVanillaAction,
                emiPresent,
                SlotNeoForgeClient.settingsController(),
                SlotNeoForgeClient.collectionViewStateController(),
                currentScreenToggleLabel,
                currentScreenToggleAction,
                SlotNeoForgeClient.searchWorkflow(),
                SlotNeoForgeClient.inspectionService()
        );
    }

    static boolean supportsContainerReplacement(AbstractContainerScreen<?> containerScreen, LocalPlayer player) {
        return SlotScreenSessionResolver.resolveContainerScreen(containerScreen, player).hasStorageView();
    }

    static Screen createContainerReplacement(
            AbstractContainerScreen<?> containerScreen,
            Screen parentScreen,
            boolean emiPresent,
            Runnable openVanillaAction,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }

        SlotScreenSession session = SlotScreenSessionResolver.resolveContainerScreen(containerScreen, minecraft.player);
        InventoryHostDescriptor host = session.host();
        if (host == null) {
            SlotDebugLog.log(
                    "Did not replace unsupported container screen: screen={} menu={} slots={}",
                    containerScreen.getClass().getName(),
                    containerScreen.getMenu().getClass().getName(),
                    containerScreen.getMenu().slots.size()
            );
            return null;
        }

        SlotDebugLog.log(
                "Replacing container screen with SLOT: screen={} menu={} slots={} primaryStorageIsCarried={} openSources={} carriedSources={}",
                containerScreen.getClass().getName(),
                containerScreen.getMenu().getClass().getName(),
                containerScreen.getMenu().slots.size(),
                host.layout().primaryStorageIsCarried(),
                host.layout().sourceIdsForPane(InventoryPane.OPEN_CONTAINER),
                host.layout().sourceIdsForPane(InventoryPane.CARRIED)
        );

        if (session.kind() == SlotSessionKind.CARRIED_CONTAINER) {
            return new SlotCarriedInventoryScreen(
                    parentScreen,
                    SlotNeoForgeClient.collectionStore(),
                    openVanillaAction,
                    emiPresent,
                    SlotNeoForgeClient.settingsController(),
                    SlotNeoForgeClient.collectionViewStateController(),
                    currentScreenToggleLabel,
                    currentScreenToggleAction,
                    host,
                    SlotNeoForgeClient.searchWorkflow(),
                    SlotNeoForgeClient.inspectionService()
            );
        }

        return new SlotInventoryWorkspaceScreen<>(
                containerScreen.getMenu(),
                minecraft.player.getInventory(),
                containerScreen.getTitle(),
                host,
                SlotNeoForgeClient.collectionStore(),
                openVanillaAction,
                emiPresent,
                SlotNeoForgeClient.settingsController(),
                SlotNeoForgeClient.collectionViewStateController(),
                currentScreenToggleLabel,
                currentScreenToggleAction,
                SlotNeoForgeClient.searchWorkflow(),
                SlotNeoForgeClient.inspectionService()
        );
    }

    private static boolean currentScreenOwnsMenu(Screen screen, AbstractContainerMenu menu) {
        return screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getMenu() == menu;
    }
}
