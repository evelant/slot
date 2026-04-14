package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.network.CursorTransferActionRequests;
import dev.imagio.slot.recent.AcquisitionProducerId;

import java.util.List;
import dev.imagio.slot.projection.InventoryPane;

final class SlotRecentAcquisitionSupport {
    private SlotRecentAcquisitionSupport() {
    }

    static RecentAcquisitionAttribution forBackpackTransfer(
            BackpackTransferActionRequests.Route route,
            String itemId
    ) {
        if (route != BackpackTransferActionRequests.Route.EXTERNAL_TO_CARRIED) {
            return RecentAcquisitionAttribution.NONE;
        }
        return externalWithdrawal(itemId);
    }

    static RecentAcquisitionAttribution forCursorTransfer(
            CursorTransferActionRequests.Route route,
            boolean openInventoryPane,
            String itemId
    ) {
        if (route != CursorTransferActionRequests.Route.PICKUP_MATCHING
                || !openInventoryPane) {
            return RecentAcquisitionAttribution.NONE;
        }
        return externalWithdrawal(itemId);
    }

    private static RecentAcquisitionAttribution externalWithdrawal(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return RecentAcquisitionAttribution.NONE;
        }
        return new RecentAcquisitionAttribution(
                AcquisitionProducerId.EXTERNAL_WITHDRAWAL,
                List.of(itemId)
        );
    }
}