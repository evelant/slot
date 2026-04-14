package dev.imagio.slot.client.screen;

import dev.imagio.slot.intent.ActionRequestId;

import java.util.List;
import java.util.Set;

public final class SlotActionOutcomeState {
    private SlotActionOutcomeState() {
    }

    public static void publish(String routingKey, ActionRequestId requestId, SlotActionResult result) {
        ActionSessionStore.publishOutcome(routingKey, requestId, result);
    }

    public static SlotActionResult poll(String routingKey) {
        List<PublishedOutcome> outcomes = pollAll(routingKey);
        return outcomes.isEmpty() ? SlotActionResult.NONE : outcomes.get(outcomes.size() - 1).result();
    }

    public static List<PublishedOutcome> pollAll(String routingKey) {
        return ActionSessionStore.pollAllOutcomes(routingKey);
    }

    public static List<PublishedOutcome> pollMatching(String routingKey, Set<String> requestIds) {
        return requestIds == null || requestIds.isEmpty()
                ? List.of()
                : ActionSessionStore.pollOutcomes(routingKey, requestIds);
    }

    public static void clear() {
        ActionSessionStore.clear();
    }

    public record PublishedOutcome(String routingKey, String requestId, SlotActionResult result, long publishedAtNanos) {
    }
}
