package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

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
        setupListenerRegistered = true;
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        if (runtimeInitialized) {
            return;
        }
        SlotDebugLog.setEnabledSupplier(() -> SlotClientConfig.CLIENT.debugLogging.get());
        SlotWorkspaceMountController.init();
        runtimeInitialized = true;
    }
}
