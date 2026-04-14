package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackpackTransferActionRequestsTest {
    @Test
    void externalToCarriedRequestResolvesBackToLegacyTransfer() {
        ItemIdentity identity = ItemIdentity.exact("minecraft:oak_log", "components");
        ActionRequest request = BackpackTransferActionRequests.externalToCarried(17, "fingerprint", identity, Integer.MAX_VALUE);

        BackpackTransferActionRequests.LegacyResolution resolution = BackpackTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.TRANSFER, request.actionFamily());
        assertEquals(17, request.expectedContainerId());
        assertEquals("fingerprint", request.expectedSessionFingerprint());
        assertEquals(BackpackTransferActionRequests.KIND_EXTERNAL_IDENTITY, request.primarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(BackpackTransferActionRequests.Route.EXTERNAL_TO_CARRIED, resolution.route());
        assertEquals(Integer.MAX_VALUE, resolution.spec().requestedCount());
        assertEquals(identity.itemId(), resolution.spec().itemId());
        assertEquals(identity.comparisonMode(), resolution.spec().comparisonMode());
        assertEquals(identity.componentFingerprint(), resolution.spec().componentFingerprint());
    }

    @Test
    void menuToExternalRequestResolvesMenuSlot() {
        ActionRequest request = BackpackTransferActionRequests.menuToExternal(8, "fp", 23, 1);

        BackpackTransferActionRequests.LegacyResolution resolution = BackpackTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.STORE, request.actionFamily());
        assertEquals("menu_slot", request.primarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(BackpackTransferActionRequests.Route.MENU_TO_EXTERNAL, resolution.route());
        assertEquals(23, resolution.spec().menuSlot());
        assertEquals(1, resolution.spec().requestedCount());
    }

    @Test
    void dynamicStackRequestedCountsRoundTripWithoutLegacySixtyFourFallback() {
        ActionRequest externalRequest = BackpackTransferActionRequests.externalToCarried(
                8,
                "fp",
                ItemIdentity.of("minecraft:cobblestone"),
                0
        );
        ActionRequest menuRequest = BackpackTransferActionRequests.menuToExternal(
                8,
                "fp",
                23,
                0
        );

        BackpackTransferActionRequests.LegacyResolution externalResolution = BackpackTransferActionRequests.resolve(externalRequest);
        BackpackTransferActionRequests.LegacyResolution menuResolution = BackpackTransferActionRequests.resolve(menuRequest);

        assertEquals(0, externalRequest.requestedCount());
        assertEquals(0, menuRequest.requestedCount());
        assertNotNull(externalResolution);
        assertNotNull(menuResolution);
        assertEquals(0, externalResolution.spec().requestedCount());
        assertEquals(0, menuResolution.spec().requestedCount());
    }

    @Test
    void backpackToMenuRequestCarriesSecondaryTargetSlot() {
        ActionRequest request = BackpackTransferActionRequests.backpackToMenu(
                4,
                "fp",
                ItemIdentity.of("minecraft:torch"),
                9,
                12
        );
        BackpackTransferActionRequests.LegacyResolution resolution = BackpackTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.STORE, request.actionFamily());
        assertEquals(12, request.requestedCount());
        assertEquals(BackpackTransferActionRequests.KIND_BACKPACK_IDENTITY, request.primarySourceRef().kind());
        assertEquals("menu_slot", request.secondarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(BackpackTransferActionRequests.Route.BACKPACK_TO_MENU, resolution.route());
        assertEquals(9, resolution.spec().menuSlot());
        assertEquals("minecraft:torch", resolution.spec().itemId());
        assertEquals(BackpackTransferActionRequests.TargetPolicy.FILL_ONLY, resolution.spec().targetPolicy());
    }

    @Test
    void backpackToMenuReplaceRequestCarriesReplacementPolicy() {
        ActionRequest request = BackpackTransferActionRequests.backpackToMenu(
                4,
                "fp",
                ItemIdentity.of("minecraft:torch"),
                9,
                12,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );

        BackpackTransferActionRequests.LegacyResolution resolution = BackpackTransferActionRequests.resolve(request);

        assertEquals("menu_slot_replace", request.secondarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(BackpackTransferActionRequests.Route.BACKPACK_TO_MENU, resolution.route());
        assertEquals(BackpackTransferActionRequests.TargetType.MENU_SLOT, resolution.spec().targetType());
        assertEquals(BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING, resolution.spec().targetPolicy());
    }

    @Test
    void backpackToOffhandRequestCarriesSemanticOffhandTarget() {
        ActionRequest request = BackpackTransferActionRequests.backpackToOffhand(
                4,
                "fp",
                ItemIdentity.of("minecraft:shield"),
                1,
                BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING
        );

        BackpackTransferActionRequests.LegacyResolution resolution = BackpackTransferActionRequests.resolve(request);

        assertEquals(ActionFamily.STORE, request.actionFamily());
        assertEquals("player_offhand_replace", request.secondarySourceRef().kind());
        assertNotNull(resolution);
        assertEquals(BackpackTransferActionRequests.Route.BACKPACK_TO_MENU, resolution.route());
        assertEquals(BackpackTransferActionRequests.TargetType.PLAYER_OFFHAND, resolution.spec().targetType());
        assertEquals(-1, resolution.spec().menuSlot());
        assertEquals(BackpackTransferActionRequests.TargetPolicy.REPLACE_EXISTING, resolution.spec().targetPolicy());
    }
}
