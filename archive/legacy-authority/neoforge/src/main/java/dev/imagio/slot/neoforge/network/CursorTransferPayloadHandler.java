package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.CursorTransferActionRequests;
import dev.imagio.slot.network.CursorTransferPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CursorTransferPayloadHandler {
    private CursorTransferPayloadHandler() {
    }

    public static void handle(CursorTransferPayload payload, IPayloadContext context) {
        ActionRequest request = CursorTransferActionRequests.fromLegacyPayload(payload);
        ActionRequestPayloadHandler.handle(request, context);
    }
}
