package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.CraftingGridActionRequests;
import dev.imagio.slot.network.CraftingGridPlacementPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CraftingGridPlacementPayloadHandler {
    private CraftingGridPlacementPayloadHandler() {
    }

    public static void handle(CraftingGridPlacementPayload payload, IPayloadContext context) {
        ActionRequest request = CraftingGridActionRequests.fromLegacyPayload(payload);
        ActionRequestPayloadHandler.handle(request, context);
    }
}
