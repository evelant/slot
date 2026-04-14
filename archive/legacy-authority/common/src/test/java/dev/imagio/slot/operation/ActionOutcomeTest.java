package dev.imagio.slot.operation;

import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequestId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionOutcomeTest {
    @Test
    void confirmedOutcomeUsesTerminalSuccessDefaults() {
        ActionOutcome outcome = ActionOutcome.confirmed(ActionRequestId.none(), ActionFamily.TRANSFER, 7, RefreshScope.SESSION);

        assertEquals(ActionStatus.CONFIRMED, outcome.status());
        assertEquals(ActionReason.NONE, outcome.reason());
        assertEquals(7, outcome.affectedCount());
        assertEquals(RefreshScope.SESSION, outcome.refreshScope());
        assertTrue(outcome.successful());
        assertTrue(outcome.status().terminal());
    }

    @Test
    void blockedOutcomeDefaultsToUnspecifiedReasonWhenMissing() {
        ActionOutcome outcome = new ActionOutcome(
                null,
                ActionFamily.DROP,
                ActionStatus.BLOCKED,
                null,
                -5,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(ActionRequestId.none(), outcome.requestId());
        assertEquals(ActionReason.UNSPECIFIED, outcome.reason());
        assertEquals(0, outcome.affectedCount());
        assertEquals(RefreshScope.NONE, outcome.refreshScope());
        assertFalse(outcome.successful());
        assertTrue(outcome.status().terminal());
    }

    @Test
    void outcomePreservesSummaryKeyWhenProvided() {
        ActionOutcome outcome = new ActionOutcome(
                ActionRequestId.none(),
                ActionFamily.CRAFT,
                ActionStatus.CONFIRMED,
                ActionReason.NONE,
                4,
                RefreshScope.SESSION,
                null,
                java.util.List.of("minecraft:crafting_table"),
                "craft_result",
                "slot.screen.action.outcome.craft.extract"
        );

        assertEquals(java.util.List.of("minecraft:crafting_table"), outcome.acquisitionItemIds());
        assertEquals("craft_result", outcome.acquisitionProducerId());
        assertEquals("slot.screen.action.outcome.craft.extract", outcome.summaryKey());
    }
}
