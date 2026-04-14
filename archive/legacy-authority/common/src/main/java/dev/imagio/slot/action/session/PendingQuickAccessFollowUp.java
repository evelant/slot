package dev.imagio.slot.action.session;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.Objects;

public record PendingQuickAccessFollowUp(
        String requestId,
        String routingKey,
        QuickAccessFollowUpActionType type,
        Object expectedMenu,
        ItemIdentity identity,
        int targetMenuSlot,
        long createdAtNanos,
        long confirmedAtNanos
) {
    public PendingQuickAccessFollowUp {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(routingKey, "routingKey");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(identity, "identity");
    }
}
