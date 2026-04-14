package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotActionRequestRoutingStateTest {
    @AfterEach
    void clearState() {
        SlotActionRequestRoutingState.clear();
    }

    @Test
    void resolvesSentRequestToBoundHistoryContext() {
        SlotActionRequestRoutingState.bindContext("screen:one");
        ActionRequest request = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                2
        );

        SlotActionRequestRoutingState.recordRequest(request);
        SlotActionRequestRoutingState.bindContext("screen:two");

        assertEquals(
                "screen:one",
                SlotActionRequestRoutingState.resolveContextKey(request.requestId(), "menu:test")
        );
    }

    @Test
    void fallsBackWhenRequestWasNotTracked() {
        ActionRequest request = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                2
        );

        assertEquals(
                "menu:test",
                SlotActionRequestRoutingState.resolveContextKey(request.requestId(), "menu:test")
        );
    }

    @Test
    void resolvesExplicitRoutingKeyWithoutBoundScreenContext() {
        ActionRequest request = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                36,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );

        SlotActionRequestRoutingState.recordRequest(request, "workflow:loadout-hotkey");

        assertEquals(
                "workflow:loadout-hotkey",
                SlotActionRequestRoutingState.resolveContextKey(request.requestId(), "menu:test")
        );
    }
}
