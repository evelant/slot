package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.network.CursorTransferActionRequests;
import dev.imagio.slot.recent.AcquisitionProducerId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotRecentAcquisitionSupportTest {
    private static final ItemIdentity STONE = ItemIdentity.of("minecraft:stone");

    @Test
    void marksExternalToCarriedTransferAsExternalWithdrawal() {
        RecentAcquisitionAttribution attribution = SlotRecentAcquisitionSupport.forBackpackTransfer(
                BackpackTransferActionRequests.Route.EXTERNAL_TO_CARRIED,
                STONE.itemId()
        );

        assertTrue(attribution.present());
        assertEquals(AcquisitionProducerId.EXTERNAL_WITHDRAWAL, attribution.producerId());
        assertEquals(java.util.List.of("minecraft:stone"), attribution.itemIds());
    }

    @Test
    void ignoresNonWithdrawalBackpackTransfers() {
        RecentAcquisitionAttribution attribution = SlotRecentAcquisitionSupport.forBackpackTransfer(
                BackpackTransferActionRequests.Route.BACKPACK_TO_MENU,
                STONE.itemId()
        );

        assertFalse(attribution.present());
        assertEquals(AcquisitionProducerId.UNKNOWN, attribution.producerId());
    }

    @Test
    void marksOpenContainerCursorPickupAsExternalWithdrawal() {
        RecentAcquisitionAttribution attribution = SlotRecentAcquisitionSupport.forCursorTransfer(
                CursorTransferActionRequests.Route.PICKUP_MATCHING,
                true,
                STONE.itemId()
        );

        assertTrue(attribution.present());
        assertEquals(AcquisitionProducerId.EXTERNAL_WITHDRAWAL, attribution.producerId());
        assertEquals(java.util.List.of("minecraft:stone"), attribution.itemIds());
    }

    @Test
    void ignoresCarriedCursorPickupForRecentAttribution() {
        RecentAcquisitionAttribution attribution = SlotRecentAcquisitionSupport.forCursorTransfer(
                CursorTransferActionRequests.Route.PICKUP_MATCHING,
                false,
                STONE.itemId()
        );

        assertFalse(attribution.present());
        assertEquals(AcquisitionProducerId.UNKNOWN, attribution.producerId());
    }
}
