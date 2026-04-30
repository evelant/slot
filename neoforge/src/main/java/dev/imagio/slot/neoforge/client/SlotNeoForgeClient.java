package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.client.screen.SlotReenableButton;
import dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class SlotNeoForgeClient {
    private static boolean setupListenerRegistered;
    private static boolean runtimeInitialized;

    private SlotNeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        if (setupListenerRegistered) {
            return;
        }
        modBus.addListener(SlotNeoForgeClient::onClientSetup);
        modBus.addListener(SlotAtlasKeyMappings::register);
        setupListenerRegistered = true;
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        if (runtimeInitialized) {
            return;
        }
        SlotDebugLog.setEnabledSupplier(() -> SlotClientConfig.CLIENT.debugLogging.get());
        SlotWorkspaceMountController.init();
        SlotReenableButton.init();
        NeoForge.EVENT_BUS.addListener(SlotNeoForgeClient::onClientTick);
        runtimeInitialized = true;
    }

    // Drain every queued click on the "Open Vanilla Inventory" mapping each
    // tick; each consumeClick() returns true once per press, so a loop
    // handles held/repeated presses without re-firing.
    private static void onClientTick(ClientTickEvent.Post event) {
        while (SlotAtlasKeyMappings.openVanillaInventoryMapping().consumeClick()) {
            SlotWorkspaceMountController.openVanillaInventory();
        }
    }
}
