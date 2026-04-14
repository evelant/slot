package dev.imagio.slot.action.session;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionRequestId;

import java.util.Objects;

public record QuickAccessRequestedTarget(int quickAccessIndex, ItemIdentity identity, ActionRequestId requestId) {
    public QuickAccessRequestedTarget {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(requestId, "requestId");
    }
}
