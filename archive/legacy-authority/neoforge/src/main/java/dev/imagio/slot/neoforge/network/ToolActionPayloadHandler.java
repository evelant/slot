package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.ToolActionRequests;
import dev.imagio.slot.network.ToolActionPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ToolActionPayloadHandler {
    private ToolActionPayloadHandler() {
    }

    public static void handle(ToolActionPayload payload, IPayloadContext context) {
        ActionRequest request = ToolActionRequests.fromLegacyPayload(payload);
        ActionRequestPayloadHandler.handle(request, context);
    }
}
