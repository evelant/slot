package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.operation.ActionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionSessionStoreTest {
    @AfterEach
    void clearState() {
        ActionSessionStore.clear();
    }

    @Test
    void completingPendingTargetDoesNotDropTrackedFollowUp() {
        ActionRequestId requestId = ActionRequestId.create();
        QuickAccessPendingState.recordRequestedChanges(List.of(
                new QuickAccessMutationResult.RequestedChange(2, ItemIdentity.of("minecraft:stone"), requestId)
        ));
        QuickAccessFollowUpState.recordUseOffhand(
                requestId,
                "screen:test",
                null,
                ItemIdentity.of("minecraft:stone")
        );

        QuickAccessPendingState.completeRequest(requestId);

        assertFalse(QuickAccessPendingState.hasPendingTargets());
        assertTrue(QuickAccessFollowUpState.hasPendingIdentity(ItemIdentity.of("minecraft:stone")));
        assertTrue(QuickAccessFollowUpState.handleTransferOutcome(requestId, ActionStatus.CONFIRMED));
        assertFalse(QuickAccessFollowUpState.readyActions().isEmpty());
    }
}
