package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.inventory.action.InventoryActionOutcomePayload;
import dev.imagio.slot.inventory.action.InventoryActionRequestPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SlotNetworking {
    private static final String PROTOCOL_VERSION = "18";

    private SlotNetworking() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(SlotNetworking::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(InventoryActionRequestPayload.TYPE, InventoryActionRequestPayload.STREAM_CODEC, InventoryActionRequestPayloadHandler::handle);
        registrar.playToClient(InventoryActionOutcomePayload.TYPE, InventoryActionOutcomePayload.STREAM_CODEC, InventoryActionOutcomePayloadHandler::handle);
    }
}
