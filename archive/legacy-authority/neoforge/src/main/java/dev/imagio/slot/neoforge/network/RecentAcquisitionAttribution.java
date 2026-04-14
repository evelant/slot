package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.recent.AcquisitionProducerId;

import java.util.List;

record RecentAcquisitionAttribution(
        AcquisitionProducerId producerId,
        List<String> itemIds
) {
    static final RecentAcquisitionAttribution NONE = new RecentAcquisitionAttribution(AcquisitionProducerId.UNKNOWN, List.of());

    RecentAcquisitionAttribution {
        producerId = producerId == null ? AcquisitionProducerId.UNKNOWN : producerId;
        itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
    }

    boolean present() {
        return producerId != AcquisitionProducerId.UNKNOWN && !itemIds.isEmpty();
    }
}
