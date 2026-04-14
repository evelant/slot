package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.network.BackpackTransferPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class BackpackTransferPayloadHandler {
    private BackpackTransferPayloadHandler() {
    }

    public static void handle(BackpackTransferPayload payload, IPayloadContext context) {
        ActionRequest request = BackpackTransferActionRequests.fromLegacyPayload(payload);
        ActionRequestPayloadHandler.handle(request, context);
    }
}
