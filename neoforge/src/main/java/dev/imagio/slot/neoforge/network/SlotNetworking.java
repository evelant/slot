package dev.imagio.slot.neoforge.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SlotNetworking {
    private static final String PROTOCOL_VERSION = "28";

    private SlotNetworking() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(SlotNetworking::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(SlotWorkspaceOpenPayload.TYPE, SlotWorkspaceOpenPayload.STREAM_CODEC, SlotWorkspaceOpenPayloadHandler::handle);
        registrar.playToServer(SlotKitPageCyclePayload.TYPE, SlotKitPageCyclePayload.STREAM_CODEC, SlotKitPageCyclePayloadHandler::handle);
        registrar.playToServer(SlotGatherActiveKitPayload.TYPE, SlotGatherActiveKitPayload.STREAM_CODEC, SlotGatherActiveKitPayloadHandler::handle);
        registrar.playToServer(SlotDepositPutAwayPayload.TYPE, SlotDepositPutAwayPayload.STREAM_CODEC, SlotDepositPutAwayPayloadHandler::handle);
        registrar.playToServer(SlotSidebarOpenPayload.TYPE, SlotSidebarOpenPayload.STREAM_CODEC, SlotSidebarOpenPayloadHandler::handle);
        registrar.playToServer(SlotSidebarClosePayload.TYPE, SlotSidebarClosePayload.STREAM_CODEC, SlotSidebarClosePayloadHandler::handle);
        registrar.playToServer(SlotSetWantedCountPayload.TYPE, SlotSetWantedCountPayload.STREAM_CODEC, SlotSetWantedCountPayloadHandler::handle);
        registrar.playToServer(SlotTrashIdentityPayload.TYPE, SlotTrashIdentityPayload.STREAM_CODEC, SlotTrashIdentityPayloadHandler::handle);
        registrar.playToServer(SlotGoalRecipeDefaultPayload.TYPE, SlotGoalRecipeDefaultPayload.STREAM_CODEC, SlotGoalRecipeDefaultPayloadHandler::handle);
        registrar.playToServer(SlotGoalPlanPayload.TYPE, SlotGoalPlanPayload.STREAM_CODEC, SlotGoalPlanPayloadHandler::handle);
    }
}
