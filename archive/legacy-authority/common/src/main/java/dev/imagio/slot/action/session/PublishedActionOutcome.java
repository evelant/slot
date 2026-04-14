package dev.imagio.slot.action.session;

public record PublishedActionOutcome(
        String routingKey,
        String requestId,
        ActionSessionResult result,
        long publishedAtNanos
) {
}
