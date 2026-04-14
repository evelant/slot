package dev.imagio.slot.client.screen;

import dev.imagio.slot.intent.ActionRequestId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotActionOutcomeStateTest {
    @AfterEach
    void clearState() {
        SlotActionOutcomeState.clear();
    }

    @Test
    void pollAllReturnsEveryQueuedOutcomeForMenu() {
        ActionRequestId firstRequestId = ActionRequestId.create();
        ActionRequestId secondRequestId = ActionRequestId.create();
        SlotActionOutcomeState.publish("menu:test", firstRequestId, SlotActionResult.applied(net.minecraft.network.chat.Component.literal("first")));
        SlotActionOutcomeState.publish("menu:test", secondRequestId, SlotActionResult.blocked(net.minecraft.network.chat.Component.literal("second")));

        List<SlotActionOutcomeState.PublishedOutcome> outcomes = SlotActionOutcomeState.pollAll("menu:test");

        assertEquals(2, outcomes.size());
        assertEquals(firstRequestId.value(), outcomes.get(0).requestId());
        assertEquals(secondRequestId.value(), outcomes.get(1).requestId());
        assertEquals(SlotActionResult.Status.BLOCKED, outcomes.get(1).result().status());
    }

    @Test
    void pollMatchingConsumesOnlyTrackedRequestIds() {
        ActionRequestId matchedRequestId = ActionRequestId.create();
        ActionRequestId unmatchedRequestId = ActionRequestId.create();
        SlotActionOutcomeState.publish("menu:test", matchedRequestId, SlotActionResult.applied(net.minecraft.network.chat.Component.literal("matched")));
        SlotActionOutcomeState.publish("menu:test", unmatchedRequestId, SlotActionResult.blocked(net.minecraft.network.chat.Component.literal("unmatched")));

        List<SlotActionOutcomeState.PublishedOutcome> matched =
                SlotActionOutcomeState.pollMatching("menu:test", Set.of(matchedRequestId.value()));

        assertEquals(1, matched.size());
        assertEquals(matchedRequestId.value(), matched.get(0).requestId());

        List<SlotActionOutcomeState.PublishedOutcome> remaining = SlotActionOutcomeState.pollAll("menu:test");
        assertEquals(1, remaining.size());
        assertEquals(unmatchedRequestId.value(), remaining.get(0).requestId());
        assertTrue(remaining.get(0).result().visible());
    }
}
