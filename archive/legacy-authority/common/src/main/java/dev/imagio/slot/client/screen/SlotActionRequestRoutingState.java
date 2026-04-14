package dev.imagio.slot.client.screen;

import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;

public final class SlotActionRequestRoutingState {
    private SlotActionRequestRoutingState() {
    }

    public static void bindContext(String key) {
        ActionSessionStore.bindContext(key);
    }

    public static void recordRequest(ActionRequest request) {
        ActionSessionStore.recordRequest(request);
    }

    public static void recordRequest(ActionRequest request, String routingKey) {
        ActionSessionStore.recordRequest(request, routingKey);
    }

    public static String resolveContextKey(ActionRequestId requestId, String fallbackKey) {
        return ActionSessionStore.resolveContextKey(requestId, fallbackKey);
    }

    public static void completeRequest(ActionRequestId requestId) {
        ActionSessionStore.completeRequest(requestId);
    }

    public static void clear() {
        ActionSessionStore.clear();
    }
}
