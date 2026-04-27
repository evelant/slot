package dev.imagio.slot.neoforge.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SlotNetworking {
    private static final String PROTOCOL_VERSION = "20";

    private SlotNetworking() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(SlotNetworking::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(SlotWorkspaceOpenPayload.TYPE, SlotWorkspaceOpenPayload.STREAM_CODEC, SlotWorkspaceOpenPayloadHandler::handle);
        registrar.playToServer(SlotChestClaimPayload.TYPE, SlotChestClaimPayload.STREAM_CODEC, SlotChestClaimPayloadHandler::handle);
        registrar.playToServer(SlotChestUnclaimPayload.TYPE, SlotChestUnclaimPayload.STREAM_CODEC, SlotChestUnclaimPayloadHandler::handle);
    }
}
