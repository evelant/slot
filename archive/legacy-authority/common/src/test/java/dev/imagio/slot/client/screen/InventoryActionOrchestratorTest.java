package dev.imagio.slot.client.screen;

import dev.imagio.slot.intent.ActionRequestId;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryActionOrchestratorTest {
    @AfterEach
    void clearState() {
        SlotActionOutcomeState.clear();
    }

    @Test
    void summarizeOutcomesUsesPartialFeedbackForMixedBatchResults() {
        InventoryActionOrchestrator.OutcomeSummary summary = InventoryActionOrchestrator.summarizeOutcomes(List.of(
                new SlotActionOutcomeState.PublishedOutcome(
                        "screen:test",
                        ActionRequestId.create().value(),
                        SlotActionResult.applied(Component.literal("applied")),
                        System.nanoTime()
                ),
                new SlotActionOutcomeState.PublishedOutcome(
                        "screen:test",
                        ActionRequestId.create().value(),
                        SlotActionResult.blocked(Component.literal("blocked")),
                        System.nanoTime()
                )
        ));

        assertTrue(summary.anySuccessful());
        assertEquals(SlotActionResult.Status.APPLIED, summary.feedback().status());
        assertEquals("slot.screen.action.outcome.batch.partial", summary.feedback().message().getString());
    }

    @Test
    void applyConfirmedOutcomeRefreshesDynamicButtonsAfterSettlement() {
        ActionRequestId requestId = ActionRequestId.create();
        SlotActionOutcomeState.publish(
                "screen:test",
                requestId,
                SlotActionResult.blocked(Component.literal("blocked"))
        );

        AtomicBoolean updatedButtons = new AtomicBoolean(false);
        InventoryActionOrchestrator.applyConfirmedActionOutcome(new InventoryActionOrchestrator.Hooks() {
            @Override
            public void showActionFeedback(SlotActionResult result) {
            }

            @Override
            public void schedulePostActionRefresh() {
            }

            @Override
            public void refreshInventoryData() {
            }

            @Override
            public void clearPendingPostActionRefresh() {
            }

            @Override
            public void updateDynamicButtons() {
                updatedButtons.set(true);
            }

            @Override
            public String historyContextKey() {
                return "screen:test";
            }
        });

        assertTrue(updatedButtons.get());
    }
}
