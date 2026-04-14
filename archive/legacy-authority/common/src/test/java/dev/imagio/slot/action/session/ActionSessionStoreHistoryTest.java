package dev.imagio.slot.action.session;

import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.operation.ActionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionSessionStoreHistoryTest {
    @AfterEach
    void clearState() {
        ActionSessionStore.clear();
    }

    @Test
    void confirmedDeferredTransferRequestPublishesTransferSettlement() {
        String contextKey = "workspace:test";
        ActionRequest request = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                3
        );

        ActionSessionStore.recordDeferredHistoryTransferRequest(contextKey, request);
        assertTrue(ActionSessionStore.hasPendingHistoryMutation(contextKey));

        ActionSessionStore.recordDeferredHistoryOutcome(request.requestId(), ActionStatus.CONFIRMED, 3);

        List<ActionSessionStore.HistorySettlement> settlements = ActionSessionStore.drainHistorySettlements();
        var settlement = assertInstanceOf(ActionSessionStore.ConfirmedTransferRecordSettlement.class, settlements.getFirst());
        assertEquals(contextKey, settlement.contextKey());
        assertEquals(ActionSessionStore.HistoryTransferDirection.EXTERNAL_TO_CARRIED, settlement.direction());
        assertEquals(1, settlement.moved().size());
        assertEquals(3, settlement.moved().getFirst().count());
        assertFalse(ActionSessionStore.hasPendingHistoryMutation(contextKey));
    }

    @Test
    void confirmedDeferredQuickAccessTransitionPublishesQuickAccessSettlement() {
        String contextKey = "workspace:test";
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        var request = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                stone,
                36,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );
        HotbarLoadoutCapture before = new HotbarLoadoutCapture(List.of(), null);
        HotbarLoadoutCapture actionAfter = new HotbarLoadoutCapture(
                List.of(new HotbarLoadoutSlot(0, stone)),
                null
        );

        ActionSessionStore.registerDeferredQuickAccessHistoryTransition(
                contextKey,
                ActionSessionStore.HistoryReplayDirection.UNDO,
                before,
                actionAfter,
                before,
                before,
                List.of(new QuickAccessRequestedTarget(0, stone, request.requestId()))
        );
        assertTrue(ActionSessionStore.hasPendingHistoryMutation(contextKey));

        ActionSessionStore.recordDeferredHistoryOutcome(request.requestId(), ActionStatus.CONFIRMED, 1);

        List<ActionSessionStore.HistorySettlement> settlements = ActionSessionStore.drainHistorySettlements();
        var settlement = assertInstanceOf(ActionSessionStore.QuickAccessTransitionSettlement.class, settlements.getFirst());
        assertEquals(contextKey, settlement.contextKey());
        assertEquals(ActionSessionStore.HistoryReplayDirection.UNDO, settlement.direction());
        assertEquals(actionAfter, settlement.settledAfter());
        assertFalse(ActionSessionStore.hasPendingHistoryMutation(contextKey));
    }
}
