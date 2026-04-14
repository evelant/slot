package dev.imagio.slot.recent;

import dev.imagio.slot.client.model.ItemIdentity;

public record AcquisitionEvent(
        ItemIdentity identity,
        int count,
        AcquisitionProducerId producerId
) {
    public AcquisitionEvent {
        count = Math.max(0, count);
        producerId = producerId == null ? AcquisitionProducerId.UNKNOWN : producerId;
    }

    public boolean valid() {
        return identity != null && count > 0;
    }
}
