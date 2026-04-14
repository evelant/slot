package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.operation.ActionStatus;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotUndoHistoryTest {
    @AfterEach
    void resetHistory() {
        SlotUndoHistory.clear();
        SlotActionRequestRoutingState.clear();
        QuickAccessPendingState.clear();
        QuickAccessFollowUpState.clear();
    }

    @Test
    void confirmsExternalToCarriedRequestIntoUndoHistory() {
        SlotUndoHistory.bindContext("workspace:test");
        ActionRequest request = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                0
        );

        SlotUndoHistory.recordTransferRequest(request);
        SlotUndoHistory.recordTransferOutcome(request.requestId(), ActionStatus.CONFIRMED, 7);

        assertTrue(SlotUndoHistory.canUndo("workspace:test"));
        assertFalse(SlotUndoHistory.canRedo("workspace:test"));
    }

    @Test
    void blockedTransferOutcomeDoesNotCreateUndoHistory() {
        SlotUndoHistory.bindContext("workspace:test");
        ActionRequest request = BackpackTransferActionRequests.carriedToExternal(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:cobblestone"),
                4
        );

        SlotUndoHistory.recordTransferRequest(request);
        SlotUndoHistory.recordTransferOutcome(request.requestId(), ActionStatus.BLOCKED, 0);

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void staleTransferOutcomeIsDroppedWhenContextChanges() {
        SlotUndoHistory.bindContext("workspace:one");
        ActionRequest request = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:dirt"),
                3
        );

        SlotUndoHistory.recordTransferRequest(request);
        SlotUndoHistory.bindContext("workspace:two");
        SlotUndoHistory.recordTransferOutcome(request.requestId(), ActionStatus.CONFIRMED, 3);

        assertFalse(SlotUndoHistory.canUndo("workspace:one"));
        assertFalse(SlotUndoHistory.canUndo("workspace:two"));
    }

    @Test
    void nonReversibleTransferRoutesAreIgnored() {
        SlotUndoHistory.bindContext("workspace:test");
        ActionRequest request = BackpackTransferActionRequests.menuToExternal(12, "fingerprint", 9, 1);

        SlotUndoHistory.recordTransferRequest(request);
        SlotUndoHistory.recordTransferOutcome(request.requestId(), ActionStatus.CONFIRMED, 1);

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void pendingTrackedTransferBlocksUndoUntilOutcomeSettles() {
        SlotUndoHistory.bindContext("workspace:test");
        ActionRequest confirmedRequest = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                2
        );
        SlotUndoHistory.recordTransferRequest(confirmedRequest);
        SlotUndoHistory.recordTransferOutcome(confirmedRequest.requestId(), ActionStatus.CONFIRMED, 2);
        assertTrue(SlotUndoHistory.canUndo("workspace:test"));

        ActionRequest pendingRequest = BackpackTransferActionRequests.externalToCarried(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:dirt"),
                1
        );
        SlotUndoHistory.recordTransferRequest(pendingRequest);

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));

        SlotUndoHistory.recordTransferOutcome(pendingRequest.requestId(), ActionStatus.BLOCKED, 0);

        assertTrue(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void quickAccessUndoWaitsForAuthoritativeOutcome() {
        SlotUndoHistory.bindContext("workspace:test");
        ActionRequest request = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                36,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );

        SlotUndoHistory.recordQuickAccessMutation(
                "workspace:test",
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null),
                QuickAccessMutationResult.of(
                        new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null),
                        new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null),
                        new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                                List.of(new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                                null
                        ),
                        true,
                        List.of(new QuickAccessMutationResult.RequestedChange(0, ItemIdentity.of("minecraft:stone"), request.requestId()))
                )
        );

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));

        SlotUndoHistory.recordTransferOutcome(request.requestId(), ActionStatus.CONFIRMED, 1);

        assertTrue(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void quickAccessUndoReflectsPartialOutcomeSuccess() {
        SlotUndoHistory.bindContext("workspace:test");
        ActionRequest firstRequest = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                36,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );
        ActionRequest secondRequest = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:dirt"),
                37,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );

        dev.imagio.slot.client.collection.HotbarLoadoutCapture before =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null);
        SlotUndoHistory.recordQuickAccessMutation(
                "workspace:test",
                before,
                QuickAccessMutationResult.of(
                        before,
                        before,
                        new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                                List.of(
                                        new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone")),
                                        new dev.imagio.slot.client.collection.HotbarLoadoutSlot(1, ItemIdentity.of("minecraft:dirt"))
                                ),
                                null
                        ),
                        true,
                        List.of(
                                new QuickAccessMutationResult.RequestedChange(0, ItemIdentity.of("minecraft:stone"), firstRequest.requestId()),
                                new QuickAccessMutationResult.RequestedChange(1, ItemIdentity.of("minecraft:dirt"), secondRequest.requestId())
                        )
                )
        );

        SlotUndoHistory.recordTransferOutcome(firstRequest.requestId(), ActionStatus.CONFIRMED, 1);
        assertFalse(SlotUndoHistory.canUndo("workspace:test"));

        SlotUndoHistory.recordTransferOutcome(secondRequest.requestId(), ActionStatus.BLOCKED, 0);
        assertTrue(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void pendingQuickAccessMutationBlocksUndoUntilAllOutcomesSettle() {
        SlotUndoHistory.bindContext("workspace:test");
        dev.imagio.slot.client.collection.HotbarLoadoutCapture before =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null);
        dev.imagio.slot.client.collection.HotbarLoadoutCapture after =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                        List.of(new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                        null
                );
        SlotUndoHistory.recordQuickAccess("workspace:test", before, after);
        assertTrue(SlotUndoHistory.canUndo("workspace:test"));

        ActionRequest pendingRequest = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:dirt"),
                37,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );

        SlotUndoHistory.recordQuickAccessMutation(
                "workspace:test",
                after,
                QuickAccessMutationResult.of(
                        after,
                        after,
                        new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                                List.of(
                                        new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone")),
                                        new dev.imagio.slot.client.collection.HotbarLoadoutSlot(1, ItemIdentity.of("minecraft:dirt"))
                                ),
                                null
                        ),
                        true,
                        List.of(new QuickAccessMutationResult.RequestedChange(1, ItemIdentity.of("minecraft:dirt"), pendingRequest.requestId()))
                )
        );

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));

        SlotUndoHistory.recordTransferOutcome(pendingRequest.requestId(), ActionStatus.CONFIRMED, 1);

        assertTrue(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void globalPendingQuickAccessStateBlocksUndoAcrossContexts() {
        SlotUndoHistory.bindContext("workspace:test");
        dev.imagio.slot.client.collection.HotbarLoadoutCapture before =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null);
        dev.imagio.slot.client.collection.HotbarLoadoutCapture after =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                        List.of(new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                        null
                );
        SlotUndoHistory.recordQuickAccess("workspace:test", before, after);
        assertTrue(SlotUndoHistory.canUndo("workspace:test"));

        ActionRequest pendingRequest = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:dirt"),
                37,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );
        QuickAccessPendingState.recordRequestedChanges(List.of(
                new QuickAccessMutationResult.RequestedChange(1, ItemIdentity.of("minecraft:dirt"), pendingRequest.requestId())
        ));

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));
        assertFalse(SlotUndoHistory.canRedo("workspace:test"));
    }

    @Test
    void undoReturnsNoneWhileGlobalQuickAccessTargetsArePending() {
        SlotUndoHistory.bindContext("workspace:test");
        dev.imagio.slot.client.collection.HotbarLoadoutCapture before =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null);
        dev.imagio.slot.client.collection.HotbarLoadoutCapture after =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                        List.of(new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                        null
                );
        SlotUndoHistory.recordQuickAccess("workspace:test", before, after);

        ActionRequest pendingRequest = BackpackTransferActionRequests.backpackToMenu(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:dirt"),
                37,
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );
        QuickAccessPendingState.recordRequestedChanges(List.of(
                new QuickAccessMutationResult.RequestedChange(1, ItemIdentity.of("minecraft:dirt"), pendingRequest.requestId())
        ));

        SlotUndoHistory.ApplyResult result = SlotUndoHistory.undo(
                new SlotUndoHistory.ActionContext("workspace:test", null, null)
        );

        assertFalse(result.applied());
        assertFalse(SlotUndoHistory.canUndo("workspace:test"));
    }

    @Test
    void pendingQuickAccessFollowUpBlocksUndoAcrossContexts() {
        SlotUndoHistory.bindContext("workspace:test");
        dev.imagio.slot.client.collection.HotbarLoadoutCapture before =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(List.of(), null);
        dev.imagio.slot.client.collection.HotbarLoadoutCapture after =
                new dev.imagio.slot.client.collection.HotbarLoadoutCapture(
                        List.of(new dev.imagio.slot.client.collection.HotbarLoadoutSlot(0, ItemIdentity.of("minecraft:stone"))),
                        null
                );
        SlotUndoHistory.recordQuickAccess("workspace:test", before, after);

        ActionRequest request = BackpackTransferActionRequests.backpackToOffhand(
                12,
                "fingerprint",
                ItemIdentity.of("minecraft:stone"),
                0,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );
        QuickAccessFollowUpState.recordUseOffhand(
                request.requestId(),
                "workflow:test",
                null,
                ItemIdentity.of("minecraft:stone")
        );

        assertFalse(SlotUndoHistory.canUndo("workspace:test"));
        assertFalse(SlotUndoHistory.canRedo("workspace:test"));
    }
}
