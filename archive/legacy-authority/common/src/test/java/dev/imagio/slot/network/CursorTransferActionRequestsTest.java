package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CursorTransferActionRequestsTest {
    @Test
    void pickupHalfRequestResolvesBackToLegacyCursorPayload() {
        ItemIdentity identity = ItemIdentity.exact("minecraft:oak_log", "components");
        ActionRequest request = CursorTransferActionRequests.pickupMatching(
                17,
                "fingerprint",
                InventoryPane.CARRIED,
                identity,
                CursorTransferActionRequests.Mode.HALF
        );

        CursorTransferActionRequests.LegacyResolution resolution = CursorTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.PICKUP, request.actionFamily());
        assertEquals("fingerprint", request.expectedSessionFingerprint());
        assertEquals(CursorTransferActionRequests.KIND_PANE_IDENTITY, request.primarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(CursorTransferActionRequests.Route.PICKUP_MATCHING, resolution.route());
        assertEquals(CursorTransferActionRequests.Mode.HALF, resolution.spec().mode());
        assertEquals(InventoryPane.CARRIED, resolution.spec().pane());
        assertEquals(identity.itemId(), resolution.spec().itemId());
    }

    @Test
    void dropToMenuSlotCarriesSecondaryTargetSlot() {
        ActionRequest request = CursorTransferActionRequests.dropCarriedToMenuSlot(
                8,
                "fp",
                23,
                CursorTransferActionRequests.Mode.STACK
        );

        CursorTransferActionRequests.LegacyResolution resolution = CursorTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.DROP, request.actionFamily());
        assertEquals(CursorTransferActionRequests.KIND_CARRIED_CURSOR, request.primarySourceRef().kind());
        assertEquals("menu_slot", request.secondarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(CursorTransferActionRequests.Route.DROP_CARRIED_TO_SLOT, resolution.route());
        assertEquals(23, resolution.spec().targetMenuSlot());
        assertEquals(CursorTransferActionRequests.Mode.STACK, resolution.spec().mode());
    }

    @Test
    void voidMatchingCarriedRequestRetainsIdentity() {
        ItemIdentity identity = ItemIdentity.of("minecraft:torch");
        ActionRequest request = CursorTransferActionRequests.voidMatchingCarried(
                4,
                "fp",
                identity,
                CursorTransferActionRequests.Mode.STACK
        );

        CursorTransferActionRequests.LegacyResolution resolution = CursorTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.VOID, request.actionFamily());
        assertEquals(CursorTransferActionRequests.KIND_PANE_IDENTITY, request.primarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(CursorTransferActionRequests.Route.VOID_MATCHING_CARRIED, resolution.route());
        assertEquals(CursorTransferActionRequests.Mode.STACK, resolution.spec().mode());
        assertEquals(identity.itemId(), resolution.spec().itemId());
    }
}
