package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickAccessPendingStateTest {
    @AfterEach
    void clearState() {
        QuickAccessPendingState.clear();
    }

    @Test
    void recordsAndCompletesPendingTargetByRequestId() {
        ActionRequestId requestId = ActionRequestId.create();
        QuickAccessPendingState.recordRequestedChanges(List.of(
                new QuickAccessMutationResult.RequestedChange(2, ItemIdentity.of("minecraft:stone"), requestId)
        ));

        assertTrue(QuickAccessPendingState.isPendingTarget(2));
        assertTrue(QuickAccessPendingState.hasPendingTargets());

        QuickAccessPendingState.completeRequest(requestId);

        assertFalse(QuickAccessPendingState.isPendingTarget(2));
        assertFalse(QuickAccessPendingState.hasPendingTargets());
    }

    @Test
    void newerRequestReplacesOlderPendingTargetForSameSlot() {
        ActionRequestId firstRequestId = ActionRequestId.create();
        ActionRequestId secondRequestId = ActionRequestId.create();
        QuickAccessPendingState.recordRequestedChanges(List.of(
                new QuickAccessMutationResult.RequestedChange(4, ItemIdentity.of("minecraft:stone"), firstRequestId)
        ));
        QuickAccessPendingState.recordRequestedChanges(List.of(
                new QuickAccessMutationResult.RequestedChange(4, ItemIdentity.of("minecraft:dirt"), secondRequestId)
        ));

        QuickAccessPendingState.completeRequest(firstRequestId);
        assertTrue(QuickAccessPendingState.isPendingTarget(4));

        QuickAccessPendingState.completeRequest(secondRequestId);
        assertFalse(QuickAccessPendingState.isPendingTarget(4));
    }
}
