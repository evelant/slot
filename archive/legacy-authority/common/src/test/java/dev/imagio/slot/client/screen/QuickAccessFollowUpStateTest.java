package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.operation.ActionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickAccessFollowUpStateTest {
    @AfterEach
    void clearState() {
        QuickAccessFollowUpState.clear();
        SlotActionOutcomeState.clear();
    }

    @Test
    void followUpBecomesReadyOnlyAfterConfirmedOutcome() {
        ActionRequestId requestId = ActionRequestId.create();
        QuickAccessFollowUpState.recordUseOffhand(
                requestId,
                "screen:test",
                null,
                ItemIdentity.of("minecraft:stone")
        );

        assertTrue(QuickAccessFollowUpState.readyActions().isEmpty());

        assertTrue(QuickAccessFollowUpState.handleTransferOutcome(requestId, ActionStatus.CONFIRMED));

        var readyActions = QuickAccessFollowUpState.readyActions();
        assertEquals(1, readyActions.size());
        assertEquals(requestId.value(), readyActions.get(0).requestId());
        assertTrue(readyActions.get(0).confirmed());
        assertTrue(SlotActionOutcomeState.pollAll("screen:test").isEmpty());
    }

    @Test
    void blockedOutcomePublishesBlockedActionResult() {
        ActionRequestId requestId = ActionRequestId.create();
        QuickAccessFollowUpState.recordDropMenuSlot(
                requestId,
                "screen:test",
                null,
                ItemIdentity.of("minecraft:dirt"),
                12
        );

        assertTrue(QuickAccessFollowUpState.handleTransferOutcome(requestId, ActionStatus.BLOCKED));

        assertTrue(QuickAccessFollowUpState.readyActions().isEmpty());
        var outcomes = SlotActionOutcomeState.pollAll("screen:test");
        assertEquals(1, outcomes.size());
        assertEquals(SlotActionResult.Status.BLOCKED, outcomes.get(0).result().status());
    }

    @Test
    void completionPublishesAppliedActionResult() {
        ActionRequestId requestId = ActionRequestId.create();
        QuickAccessFollowUpState.recordUseOffhand(
                requestId,
                "screen:test",
                null,
                ItemIdentity.of("minecraft:stone")
        );

        QuickAccessFollowUpState.completeApplied(requestId);

        var outcomes = SlotActionOutcomeState.pollAll("screen:test");
        assertEquals(1, outcomes.size());
        assertEquals(SlotActionResult.Status.APPLIED, outcomes.get(0).result().status());
    }

    @Test
    void pendingIdentityQueryTracksQueuedFollowUps() {
        ActionRequestId requestId = ActionRequestId.create();
        ItemIdentity identity = ItemIdentity.exact("minecraft:stone", "exact");

        assertFalse(QuickAccessFollowUpState.hasPendingIdentity(identity));
        assertFalse(QuickAccessFollowUpState.hasPendingActions());

        QuickAccessFollowUpState.recordUseOffhand(
                requestId,
                "screen:test",
                null,
                identity
        );

        assertTrue(QuickAccessFollowUpState.hasPendingIdentity(identity));
        assertTrue(QuickAccessFollowUpState.hasPendingActions());

        QuickAccessFollowUpState.completeFailed(requestId);

        assertFalse(QuickAccessFollowUpState.hasPendingIdentity(identity));
        assertFalse(QuickAccessFollowUpState.hasPendingActions());
    }
}
