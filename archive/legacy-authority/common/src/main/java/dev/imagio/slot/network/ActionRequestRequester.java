package dev.imagio.slot.network;

import dev.imagio.slot.client.SlotClientCompat;
import dev.imagio.slot.client.screen.SlotActionRequestRoutingState;
import dev.imagio.slot.client.screen.SlotUndoHistory;
import dev.imagio.slot.intent.ActionRequest;

public final class ActionRequestRequester {
    private ActionRequestRequester() {
    }

    public static boolean request(ActionRequest request) {
        return requestTracked(request);
    }

    public static boolean requestTracked(ActionRequest request) {
        if (request == null) {
            return false;
        }
        boolean sent = SlotClientCompat.sendToServer(new ActionRequestPayload(request));
        if (sent) {
            SlotActionRequestRoutingState.recordRequest(request);
            SlotUndoHistory.recordTransferRequest(request);
        }
        return sent;
    }

    public static boolean requestRouted(ActionRequest request, String routingContextKey) {
        if (request == null) {
            return false;
        }

        boolean sent = SlotClientCompat.sendToServer(new ActionRequestPayload(request));
        if (sent) {
            SlotActionRequestRoutingState.recordRequest(request, routingContextKey);
        }
        return sent;
    }
}
